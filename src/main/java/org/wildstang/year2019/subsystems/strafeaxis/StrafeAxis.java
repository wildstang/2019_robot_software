package org.wildstang.year2019.subsystems.strafeaxis;

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

    /** # of rotations of encoder in one inch of axis travel */
    private static final double REVS_PER_INCH = 10;
    /** Number of encoder ticks in one revolution */
    private static final double TICKS_PER_REV = 1024;
    /** # of ticks in one inch of axis movement */
    private static final double TICKS_PER_INCH = TICKS_PER_REV * REVS_PER_INCH;
    /**# of ticks in millimeters for encoders */
    private static double TICKS_PER_MM = 17.746;

    /** The maximum speed the operator can command to move in fine-tuning */
    private static final double MANUAL_SPEED = 2; // in/s
    private static final double TRACKING_MAX_SPEED = 20; // in/s
    private static final double TRACKING_MAX_ACCEL = 100; // in/s^2
    private static final double HOMING_MAX_SPEED = 2; // in/s
    private static final double HOMING_MAX_ACCEL = 2; // in/s^2
    /**millimeters from center for each of the sensors */
    private static int[] SENSOR_POSITIONS = { -120, -104, 88, -72, -56, -40, -24, -8, 0, 8, 24, 40, 56, 72, 88, 104,
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

        if (isTrackingAutomatically == false) {
            motor.set(ControlMode.PercentOutput, axisConfig.manualAdjustmentJoystick.getValue());
        }
        lightValues = arduino.getLineSensor();

        for (int i = 0; i < 16; i++) {
            String smartName = i + " Position";
            SmartDashboard.putNumber(smartName, lightValues[i]);
        }

        SmartDashboard.putBoolean("Upper limit switch", axisConfig.upperLimitSwitch.getValue());
        SmartDashboard.putBoolean("Lower limit switch", axisConfig.lowerLimitSwitch.getValue());
        SmartDashboard.putNumber("Strafe Encoder Value", motor.getSelectedSensorPosition()); 
        SmartDashboard.putNumber("Joystick Position", axisConfig.manualAdjustmentJoystick.getValue());  
        int brightestSensor = 0;//convert to inches - find ticks
        
        int min = lightValues[0];
        int minIndex = 0;
        for (int i = 0; i < lightValues.length; i++)
        {
            if (lightValues[i] < min){
                min = lightValues[i];
                minIndex = i;
            }
        }
        
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
        axisConfig.limitSwitchOverrideButton = (DigitalInput) inputManager.getInput(WSInputs.STRAFE_LIMIT_SWITCH_OVERRIDE);
        axisConfig.limitSwitchOverrideButton.addInputListener(this);
        axisConfig.pidOverrideButton = (DigitalInput) Core.getInputManager().getInput(WSInputs.STRAFE_OVERRIDE);
        axisConfig.pidOverrideButton.addInputListener(this);

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

    private void centerOfStrafeMotor() {  //Strafe axis 15' across, mechanism 5' across
        while(!axisConfig.lowerLimitSwitch.getValue()) {
            motor.set(ControlMode.PercentOutput, 0.25);
        }
        motor.setSelectedSensorPosition(0);
        while (!axisConfig.upperLimitSwitch.getValue()) {
            motor.set(ControlMode.PercentOutput, -0.25);
        }
        CENTER = motor.getSelectedSensorPosition() / 2;
    }
}
