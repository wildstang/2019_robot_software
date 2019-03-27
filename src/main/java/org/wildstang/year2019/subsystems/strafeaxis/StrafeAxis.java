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
    private DigitalInput rezero;

    /** # of ticks in millimeters for encoders */
    // private static double TICKS_PER_MM = 17.746; - not sure where this is from,
    // but not accurate relative to testing

    /** # of ticks in one inch of axis movement */
    // private static final double TICKS_PER_INCH = 25.4 * TICKS_PER_MM; unit
    // conversions guys cmon
    private static final double TICKS_PER_INCH = 4096 * 4.0237;
    // private static final double TICKS_PER_MM = TICKS_PER_INCH * 25.4;
    private static final double TICKS_PER_MM = 652.381;

    /** The maximum speed the operator can command to move in fine-tuning */
    private static final double MANUAL_SPEED = 2; // in/s
    private static final double TRACKING_MAX_SPEED = 20; // in/s
    private static final double TRACKING_MAX_ACCEL = 100; // in/s^2
    private static final double HOMING_MAX_SPEED = 2; // in/s
    private static final double HOMING_MAX_ACCEL = 2; // in/s^2

    /** millimeters from center for each of the sensors */
    // private static int[] SENSOR_POSITIONS = { -120, -104, -88, -72, -56, -40,
    // -24, -8, 0, 8, 24, 40, 56, 72, 88, 104,
    // 120 };//assumes we start in the middle, a bad assumption. likely need to all
    // be positive, moving left -> right
    // mech has 8.3215 in of travel, so +- 4.15625 in, which is 105.56875 mm
    // thus, assign everything relative to that "0" which would be -105.56875 in the
    // initial coord system
    // also there's 17 numbers above and we have 16 sensors. i'll assume the 0 is
    // wrong, but #TODO
    private static int[] SENSOR_POSITIONS = { 0, 1, 17, 33, 49, 65, 81, 97, 113, 129, 145, 161, 177, 193, 209, 210 };// positions,
                                                                                                                     // zero
                                                                                                                     // encoder
                                                                                                                     // on
                                                                                                                     // left
                                                                                                                     // side
    private static double[] SENSOR_LOW_CALIBRATED = { 8, 216, 144, 4000, 76, 144, 144, 216, 144, 144, 216, 216, 216,
            296, 296, 296 };// to be filled with calibrated low values
    private static double[] SENSOR_HIGH_CALIBRATED = { 8, 292, 216, 4000, 76, 144, 216, 292, 216, 144, 216, 292, 292,
            368, 368, 368 };// to be filled with calibrated high values

    private static final double LEFT_STOP_POS = -6;
    private static final double LEFT_MAX_TRAVEL = -5;
    private static final double RIGHT_MAX_TRAVEL = 5;
    private static final double PHYS_DIR_CHANGE = -1;

    private static final double AXIS_IN_RANGE_THRESHOLD = TICKS_PER_INCH * 0.5;

    private TalonSRX motor;

    public boolean isStopped = true;

    public boolean zeroing = false;

    public double target = 0.0;

    public boolean isManual = false;
    public double sensorLocation = 0.0;

    public boolean zeroPressed = false;
    public boolean zero2 = false;
    public double realTarget = 0.0;

    public double lastSensor = 0;
    /** Line detector class talks to Arduino with line sensors on it */
    public LineDetector arduino = new LineDetector();

    /** The axis configuration we pass up to the axis initialization */
    private AxisConfig axisConfig = new AxisConfig();

    @Override
    public void inputUpdate(Input source) {
        // if (axisConfig.pidOverrideButton.getValue()) {
        // // motor.set(ControlMode.Position, arduino.getLinePosition());
        // }

        // init motor; use if needed
        // if (axisConfig.overrideButtonValue) {
        // initMotor();
        // }

        if (source == rezero) {
            if (rezero.getValue()) {
                zeroPressed = true;
            } else {
                zeroPressed = false;
            }
        }
        if (source == automaticStrafeButton) {
            if (automaticStrafeButton.getValue() && !zeroPressed) {
                isTrackingAutomatically = true;
                isManual = false;

            } else if (automaticStrafeButton.getValue() && zeroPressed) {
                zeroing = true;
                isManual = false;
                isTrackingAutomatically = false;
            } else {
                isTrackingAutomatically = false;
            } // makes sure that the strafe stops moving if the button isn't pressed and the
              // joystick isn't in motion

        }

        if (source == axisConfig.manualAdjustmentJoystick) {
            double joystickValue = axisConfig.manualAdjustmentJoystick.getValue();
            if ((joystickValue < -0.25 || joystickValue > 0.25) && !zeroing) {
                isTrackingAutomatically = false;
                isManual = true;
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
        // arduino.run();
        double manualMotorSpeed = axisConfig.manualAdjustmentJoystick.getValue();
        // if (!axisConfig.lowerLimitSwitch.getValue() && manualMotorSpeed > 0) {
        // manualMotorSpeed = 0;
        // }
        // else if (!axisConfig.upperLimitSwitch.getValue() && manualMotorSpeed < 0) {
        // manualMotorSpeed = 0;
        // }
        // if (manualMotorSpeed > 0.1 || manualMotorSpeed < -0.1) {
        // motor.set(ControlMode.PercentOutput, -manualMotorSpeed);
        // } else motor.set(ControlMode.PercentOutput, 0);
        // arduino.getLinePosition();

        // System.out.println(axisConfig.manualAdjustmentJoystick.getValue());

        // motor.set(ControlMode.PercentOutput, PHYS_DIR_CHANGE *
        // axisConfig.manualAdjustmentJoystick.getValue());
        // lightValues = arduino.getLineSensorData();

        // for (int i = 0; i < 16; i++) {
        // String smartName = i + "lightValue";
        // SmartDashboard.putNumber(smartName, lightValues[i]);
        // }

        // SmartDashboard.putBoolean("Upper limit switch", true);
        // SmartDashboard.putBoolean("Lower limit switch",
        // axisConfig.lowerLimitSwitch.getValue());

        // SmartDashboard.putNumber("Strafe Encoder Value",
        // motor.getSelectedSensorPosition());
        // SmartDashboard.putNumber("Joystick Position",
        // axisConfig.manualAdjustmentJoystick.getValue());
        // int brightestSensor = 0;//convert to inches - find ticks
        // /**minimum value */
        // double min = getModifiedValue(1);//see getModifiedValue method
        // /**index which has a minimum value */
        // int minIndex = 1;
        // for (int i = 2; i < (double)lightValues.length; i++)
        // {
        // if (getModifiedValue(i) < min && getModifiedValue(i) >= 1.0 && (i != 3 &&
        // i!=4 && i!=5 && i!=10)){//if value is lowest and in acceptable calibrated
        // range (i.e. not noise)

        // min = (double)lightValues[i];
        // minIndex = i;
        // }

        // }
        // double linePositionTicks = TICKS_PER_MM *
        // SENSOR_POSITIONS[minIndex];//converts sensor over line to position of hatch
        // mech
        // if (zeroing){
        // resetEncoder();
        // } else
        // if (false){//isTrackingAutomatically) {
        // motor.set(ControlMode.Position, linePositionTicks);
        // } else {
        // if (isStopped){
        // motor.set(ControlMode.PercentOutput, 0.0);
        // System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        // }
        // else {

        // motor.set(ControlMode.PercentOutput, PHYS_DIR_CHANGE *
        // axisConfig.manualAdjustmentJoystick.getValue());
        // // System.out.println("\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\");
        // // //phys_dir_change above to make it move relative to the operator's field
        // of view

        // }
        // motor.set(ControlMode.PercentOutput, manualMotorSpeed);
        // TILT
        // if (zeroing){
        // resetEncoder();
        // } else{
        // target = 105*TICKS_PER_MM * manualMotorSpeed;
        // motor.set(ControlMode.Position, target);
        // }
        // SmartDashboard.putNumber("strafe target",target);
        SmartDashboard.putNumber("Strafe encoder", motor.getSelectedSensorPosition());
        // SmartDashboard.putNumber("Strafe target",linePositionTicks);
        // arduino.run();
        sensorLocation = (double) arduino.getLineSensorData();
        if (isManual) {
            if (manualMotorSpeed > 0.25 || manualMotorSpeed < -0.25) {
                motor.set(ControlMode.PercentOutput, manualMotorSpeed);
            } else
                motor.set(ControlMode.PercentOutput, 0.0);

        } else if (isTrackingAutomatically) {
            if (sensorLocation >= 0 && sensorLocation <= 255) {
                realTarget = (255 - sensorLocation) * 137000 / 255;
                realTarget = realTarget + Math.abs(127 - sensorLocation) / 127 * 137000 / 8.75 * 2;
                motor.set(ControlMode.Position, realTarget);
            }
        } else if (zeroing) {
            resetState();
            zeroing = false;
        }
        SmartDashboard.putBoolean("upper limit", motor.getSensorCollection().isFwdLimitSwitchClosed());
        SmartDashboard.putBoolean("lower limit", motor.getSensorCollection().isRevLimitSwitchClosed());
        SmartDashboard.putNumber("Arduino Strafe Target", sensorLocation);
        SmartDashboard.putNumber(" target", sensorLocation * 137000 / 15.0);

    }

    public double getModifiedValue(int i) { // rearranges values from 1-10, with 1 being the lowest calibrated value and
                                            // 10 the highest
        // this allows us to compare the "confidence" that the sensor sees the line with
        // no regard to the sensor's differences in values
        return ((double) lightValues[i] - SENSOR_LOW_CALIBRATED[i]) * 10
                / (SENSOR_HIGH_CALIBRATED[i] - SENSOR_LOW_CALIBRATED[i]);

    }

    @Override
    public void resetState() {
        super.resetState();
        motor.set(ControlMode.Position, 137000 / 2.0);
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
        rezero = (DigitalInput) Core.getInputManager().getInput(WSInputs.WEDGE_SAFETY_1);
        rezero.addInputListener(this);
    }

    private void initOutputs() {
        motor = new TalonSRX(CANConstants.STRAFE_TALON);
        motor.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, -1);
        motor.configNominalOutputForward(0, -1);
        motor.configNominalOutputReverse(0, -1);
        motor.configPeakOutputForward(1, -1);
        motor.configPeakOutputReverse(-1, -1);
        motor.configPeakCurrentLimit(20); // 20 amp current limit
        // peak output managed by axis
        // speed and accel managed by axis
        motor.setInverted(INVERTED);
        motor.setSensorPhase(!SENSOR_PHASE);
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

    // private void centerOfStrafeMotor() { // Strafe axis 15' across, mechanism 5'
    // across
    // while (!axisConfig.lowerLimitSwitch.getValue()) {
    // motor.set(ControlMode.PercentOutput, 0.25);
    // }
    // motor.setSelectedSensorPosition(0);
    // while (!axisConfig.upperLimitSwitch.getValue()) {
    // motor.set(ControlMode.PercentOutput, -0.25);
    // }
    // CENTER = motor.getSelectedSensorPosition() / 2;
    // // This function finds the center. Should motor be set to
    // // center somewhere locally?

    // }
    // private void resetEncoder(){
    // if (!axisConfig.upperLimitSwitch.getValue()) {
    // motor.set(ControlMode.PercentOutput, 0.5);
    // } else{
    // motor.setSelectedSensorPosition(0);
    // zero2 = true;
    // }
    // if (zero2){
    // motor.set(ControlMode.PercentOutput, -0.5);
    // if (motor.getSelectedSensorPosition(0) > 105*TICKS_PER_MM){
    // zero2 = false;
    // }
    // } else{
    // motor.set(ControlMode.PercentOutput, 0.0);
    // zeroing = false;
    // }
    // }
}
