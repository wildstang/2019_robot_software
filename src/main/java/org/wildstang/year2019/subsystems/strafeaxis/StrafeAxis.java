package org.wildstang.year2019.subsystems.strafeaxis;

import java.util.Arrays;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.ControlMode;
//import org.wildstang.framework.CoreUtils.CTREException;
import org.wildstang.framework.CoreUtils;
import org.wildstang.framework.core.Core;
import org.wildstang.framework.io.IInputManager;
import org.wildstang.framework.io.Input;
import org.wildstang.framework.io.inputs.AnalogInput;
import org.wildstang.framework.io.inputs.DigitalInput;
import org.wildstang.framework.io.inputs.RemoteAnalogInput;
import org.wildstang.framework.subsystems.Subsystem;
import org.wildstang.year2019.robot.CANConstants;
import org.wildstang.year2019.robot.WSInputs;
import org.wildstang.year2019.subsystems.common.Axis;
import org.wildstang.year2019.subsystems.strafeaxis.StrafePID;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/*
 * This subsystem is responsible for lining up hatch panels left-to-right.
 * 
 * There should probably be a PID loop controlling the position of this axis.
 * 
 * Sensors:
 * <ul>
 * <li>Line detection photocells (handled by LineDetector.java? or RasPi?)
 * <li>Limit switch(es). TODO: left, right or both?
 * <l   i>Encoder on lead screw Talon.
 * </ul>
 * 
 * Actuators:
 * <ul>
 * <li>Talon controlling lead screw motor.
 * </ul>
 * 
 */

public class StrafeAxis extends Axis implements Subsystem {

    private static final boolean INVERTED = false;
    private static final boolean SENSOR_PHASE = true;

    private int CENTER = 100; // needs to set manually once axis is created
    private byte[] lightValues = new byte[16];
    private boolean isTrackingAutomatically = false;
    private DigitalInput automaticStrafeButton;

    /** # of ticks in millimeters for encoders */
    private static double TICKS_PER_MM = 17.746;
    /** # of ticks in one inch of axis movement */
    private static final double TICKS_PER_INCH = 25.4 * TICKS_PER_MM;

    /** The maximum speed the operator can command to move in fine-tuning */
    private static final double MANUAL_SPEED = 2; // in/s
    private static final double TRACKING_MAX_SPEED = 20; // in/s
    private static final double TRACKING_MAX_ACCEL = 100; // in/s^2
    private static final double HOMING_MAX_SPEED = 2; // in/s
    private static final double HOMING_MAX_ACCEL = 2; // in/s^2
    /** millimeters from center for each of the sensors */
    private static int[] SENSOR_POSITIONS = { -120, -104, -88, -72, -56, -40, -24, -8, 0, 8, 24, 40, 56, 72, 88, 104,
            120 };
    private static final double LEFT_STOP_POS = -6;
    private static final double LEFT_MAX_TRAVEL = -5;
    private static final double RIGHT_MAX_TRAVEL = 5;

    private static final double AXIS_IN_RANGE_THRESHOLD = TICKS_PER_INCH * 0.5;

    private TalonSRX motor;

    /** Line detector class talks to Arduino with line sensors on it */
    public LineDetector arduino = new LineDetector();

    /** The axis configuration we pass up to the axis initialization */
    private AxisConfig axisConfig = new AxisConfig();

    @Override
    public void inputUpdate(Input source) {
        if (axisConfig.pidOverrideButton.getValue()) {
            // motor.set(ControlMode.Position, arduino.getLinePosition());
        }

        // init motor; use if needed
        // if (axisConfig.overrideButtonValue) {
        // initMotor();
        // }

        if (source == automaticStrafeButton) {
            if (automaticStrafeButton.getValue()) {
                isTrackingAutomatically = true;

            }

        }

        if (source == axisConfig.manualAdjustmentJoystick) {
            double joystickValue = axisConfig.manualAdjustmentJoystick.getValue();
            if (joystickValue < -0.25 || joystickValue > 0.25) {
                isTrackingAutomatically = false;
            }
        }
    }

    @Override
    public void init() {
        // System.out.println("\n\n\n\n\n\n STRAFE ENABLED\n\n\n\n\n\n");
        initInputs();
        // System.out.println("\n\n\n\n\n\n STRAFE ENABLED\n\n\n\n\n\n");
        initOutputs();
        // System.out.println("\n\n\n\n\n\n STRAFE ENABLED\n\n\n\n\n\n");
        initAxis();
        // System.out.println("\n\n\n\n\n\n STRAFE ENABLED\n\n\n\n\n\n");
        resetState();
        // System.out.println("\n\n\n\n\n\n STRAFE ENABLED\n\n\n\n\n\n");

        // Start the thread reading from the arduino serial port
        arduino.start();
    }

    @Override
    public void selfTest() {
        // TODO
    }

    @Override
    public void update() {

        // super.update();

        // double manualMotorSpeed = axisConfig.manualAdjustmentJoystick.getValue();
        // if (!axisConfig.lowerLimitSwitch.getValue() && manualMotorSpeed > 0) {
        // manualMotorSpeed = 0;
        // }
        // else if (!axisConfig.upperLimitSwitch.getValue() && manualMotorSpeed < 0) {
        // manualMotorSpeed = 0;
        // }
        // if (manualMotorSpeed > 0.1 || manualMotorSpeed < -0.1) {
        // motor.set(ControlMode.PercentOutput, manualMotorSpeed);
        // }
        // arduino.getLinePosition();

        // System.out.println(axisConfig.manualAdjustmentJoystick.getValue());

        motor.set(ControlMode.PercentOutput, axisConfig.manualAdjustmentJoystick.getValue());
        lightValues = arduino.getLineSensorData();

        for (int i = 0; i < 16; i++) {
            String smartName = i + "lightValue";
            SmartDashboard.putNumber(smartName, lightValues[i]);
        }

        SmartDashboard.putBoolean("Upper limit switch", axisConfig.upperLimitSwitch.getValue());
        SmartDashboard.putBoolean("Lower limit switch", axisConfig.lowerLimitSwitch.getValue());
        SmartDashboard.putNumber("Strafe Encoder Value", motor.getSelectedSensorPosition());
        SmartDashboard.putNumber("Joystick Position", axisConfig.manualAdjustmentJoystick.getValue());
        int brightestSensor = 0;// convert to inches - find ticks
        /** minimum value */
        int min = lightValues[0];
        /** index which has a minimum value */
        int minIndex = 0;
        for (int i = 0; i < lightValues.length; i++) {
            if (lightValues[i] < min) {
                min = lightValues[i];
                minIndex = i;
            }

        }
        double linePositionTicks = TICKS_PER_MM * SENSOR_POSITIONS[minIndex];
        if (isTrackingAutomatically) {
            motor.set(ControlMode.Position, linePositionTicks);
        }
        /*
         * START of finding the 3 lowest values and calculating the weighted average
         * This is the average of the three lowest values. Would four values be better?
         * Remember, The line width of the 2019 game is 2 inches.
         */
        final byte[] lightValuesBeforeSort = lightValues;
        Arrays.sort(lightValues);

        byte smallest = Byte.MAX_VALUE;
        byte secondSmallest = Byte.MAX_VALUE;
        byte thirdSmallest = Byte.MAX_VALUE;

        for (int i = 0; i < lightValues.length; i++) {

            if (lightValues[i] < smallest) {

                secondSmallest = smallest;
                smallest = lightValues[i];
            } else if (lightValues[i] < secondSmallest) {
                thirdSmallest = secondSmallest;
                secondSmallest = lightValues[i];

            } else if (lightValues[i] < thirdSmallest) {
                thirdSmallest = lightValues[i];
            }

        }
        // START-Find the distance of the the three smallest values in ticks from
        // center. The following three distance values are at default zero which is the
        // center of StrafeAxis
        double distanceOfSmallestValueIndexFromCenterInTicks = 0;
        double distanceOfSecondSmallestValueIndexFromCenterInTicks = 0;
        double distanceOfThirdSmallestValueIndexFromCenterInTicks = 0;
        for (int i = 0; i < lightValues.length; i++) {
            /*
             * Should I weigh when the distance is in ticks or in millimeters? All three
             * distance values are currently in ticks. In short, what is more precise ticks
             * or millimeters? The percentage used for weighing is based on
             * theSmallestValue, this gives higher importance to the lowest value. In simple
             * terms (distanceValue/smallestValue) This does mean that extraneous points
             * could have consequences.
             * 
             * To counter the extraneous smallest values, which are values that are not over
             * the white lines, probabibility may be needed. Pretend the strip below is the
             * line sensors. C is center. All measurements are done from the cneter of one
             * line sensors to another Line sensors 7 and 8 are 8-millimeters from the
             * center. The distance between sequential line sensors is 16-millimeters. The
             * most likely sensors to detect the line, which is approximately 2 inches(50.8
             * mm) in width, are the ones in the middle. There would need to be testing to
             * see which sensors are more probably over the light sensor. Once the
             * measurements from a pool of data is collected, we can use graph the
             * probabilities. Standard Deviation of the graph will then be found. (Left) 0 1
             * 2 3 4 5 6 7 C 8 9 10 11 12 13 14 15 (Right) TLDR- Some sensor points are more
             * likely to detect light and needs to be implemented in this average weighting.
             * 
             * 
             */
            if (smallest == lightValuesBeforeSort[i]) {

                distanceOfSmallestValueIndexFromCenterInTicks = TICKS_PER_MM * SENSOR_POSITIONS[i];
                /*
                 * The multiplied percent can be changed or change to a variable for quick
                 * changes
                 */
                // the smallestValue is the most trusted
                distanceOfSmallestValueIndexFromCenterInTicks *= 1;
            }
            if (secondSmallest == lightValuesBeforeSort[i]) {
                distanceOfSecondSmallestValueIndexFromCenterInTicks = TICKS_PER_MM * SENSOR_POSITIONS[i];
                /*
                 * The multiplied percent can be changed or change to a variable for quick
                 * changes
                 * 
                 */
                distanceOfSecondSmallestValueIndexFromCenterInTicks *= (distanceOfSecondSmallestValueIndexFromCenterInTicks
                        / distanceOfSmallestValueIndexFromCenterInTicks);
            }
            if (thirdSmallest == lightValuesBeforeSort[i]) {
                distanceOfThirdSmallestValueIndexFromCenterInTicks = TICKS_PER_MM * SENSOR_POSITIONS[i];
                /*
                 * The multiplied percent can be changed or change to a variable for quick
                 * changes
                 * 
                 */
                distanceOfThirdSmallestValueIndexFromCenterInTicks *= (distanceOfThirdSmallestValueIndexFromCenterInTicks
                        / distanceOfSmallestValueIndexFromCenterInTicks);
            }

        }
        // The next line is the average ticks, which are weighted to where the line is
        /**
         * The average consists of the three distances in ticks
         */
        double AverageTicksToLine = (distanceOfSmallestValueIndexFromCenterInTicks
                + distanceOfSecondSmallestValueIndexFromCenterInTicks
                + distanceOfThirdSmallestValueIndexFromCenterInTicks) / 3;
        if (isTrackingAutomatically) {
            motor.set(ControlMode.Position, AverageTicksToLine);
        }
        // END if the Variables are too long feel, free change the name but do not
        // forget to do "/**definition */"
        // before the declaration to help others
    }

    @Override
    public void resetState() {
        super.resetState();
    }

    @Override
    public String getName() {
        return "StrafeAxis";
    }

    ////////////////////////////////////////
    // Private methods

    private void initInputs() {
        IInputManager inputManager = Core.getInputManager();
        automaticStrafeButton = (DigitalInput) Core.getInputManager().getInput(WSInputs.AUTOMATIC_STRAFE_SWITCH);
        automaticStrafeButton.addInputListener(this);
    }

    private void initOutputs() {
        motor = new TalonSRX(CANConstants.STRAFE_TALON);
        motor.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, -1);
        motor.configNominalOutputForward(0, -1);
        motor.configNominalOutputReverse(0, -1);
        motor.configPeakOutputForward(1, -1);
        motor.configPeakOutputReverse(-1, -1);
        // peak output managed by axis
        // speed and accel managed by axis
        motor.setInverted(INVERTED);
        motor.setSensorPhase(SENSOR_PHASE);
    }

    private void initAxis() {
        IInputManager inputManager = Core.getInputManager();
        axisConfig.lowerLimitSwitch = (DigitalInput) inputManager.getInput(WSInputs.STRAFE_LEFT_LIMIT);
        axisConfig.lowerLimitSwitch.addInputListener(this);
        axisConfig.upperLimitSwitch = (DigitalInput) inputManager.getInput(WSInputs.STRAFE_RIGHT_LIMIT);
        axisConfig.upperLimitSwitch.addInputListener(this);
        axisConfig.manualAdjustmentJoystick = (AnalogInput) inputManager.getInput(WSInputs.STRAFE_MANUAL);
        axisConfig.manualAdjustmentJoystick.addInputListener(this);
        axisConfig.overrideButtonModifier = (DigitalInput) inputManager.getInput(WSInputs.WEDGE_SAFETY_1);
        axisConfig.overrideButtonModifier.addInputListener(this);
        axisConfig.limitSwitchOverrideButton = (DigitalInput) inputManager
                .getInput(WSInputs.STRAFE_LIMIT_SWITCH_OVERRIDE);
        axisConfig.limitSwitchOverrideButton.addInputListener(this);

        axisConfig.motor = motor;
        axisConfig.ticksPerInch = TICKS_PER_INCH;
        axisConfig.runAcceleration = TRACKING_MAX_ACCEL;
        axisConfig.runSpeed = TRACKING_MAX_SPEED;
        axisConfig.homingAcceleration = HOMING_MAX_ACCEL;
        axisConfig.homingSpeed = HOMING_MAX_SPEED;
        axisConfig.manualSpeed = MANUAL_SPEED;
        axisConfig.minTravel = LEFT_MAX_TRAVEL;
        axisConfig.maxTravel = RIGHT_MAX_TRAVEL;
        axisConfig.runSlot = StrafePID.TRACKING.slot;
        axisConfig.runK = StrafePID.TRACKING.k;
        axisConfig.homingSlot = StrafePID.HOMING.slot;
        axisConfig.homingK = StrafePID.HOMING.k;
        axisConfig.lowerLimitPosition = LEFT_STOP_POS;
        axisConfig.axisInRangeThreshold = AXIS_IN_RANGE_THRESHOLD;

        initAxis(axisConfig);
    }

    private void centerOfStrafeMotor() { // Strafe axis 15' across, mechanism 5' across
        while (!axisConfig.lowerLimitSwitch.getValue()) {
            motor.set(ControlMode.PercentOutput, 0.25);
        }
        motor.setSelectedSensorPosition(0);
        while (!axisConfig.upperLimitSwitch.getValue()) {
            motor.set(ControlMode.PercentOutput, -0.25);
        }
        CENTER = motor.getSelectedSensorPosition() / 2;
    }
}
