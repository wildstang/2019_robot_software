package org.wildstang.year2019.subsystems.lift;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;

import org.wildstang.framework.core.Core;
import org.wildstang.framework.io.IInputManager;
import org.wildstang.framework.io.Input;
import org.wildstang.framework.io.inputs.AnalogInput;
import org.wildstang.framework.io.inputs.DigitalInput;
import org.wildstang.framework.subsystems.Subsystem;
import org.wildstang.year2019.robot.CANConstants;
import org.wildstang.year2019.robot.WSInputs;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * Class:       Lift.java
 * Inputs:      7 buttons, 2 limit switches, and 1 joystick axis
 * Outputs:     2 motors, a master and a slave
 * Description: A manual adaptation of the Lift subsystem.
 *              All state changes and motor manipulation is handled in the set functions.
 *              inputUpdate focues on divying out work to simpler functions.
 *              No limit switch or PID override support.
 */
public class LiftMark3 implements Subsystem {

    // stopping positions in inches above the bottom limit switch
    private static double POSITION_1 =  0.0;  // low goal
    private static double POSITION_2 = 10.62; // cargo goal - cargo only
    private static double POSITION_3 = 22.0;  // mid goal
    private static double POSITION_4 = 44.0;  // high goal

    // encoder rotations per inch of vertical travel
    private static final double REVS_PER_INCH = 1/9.087;     
    // encoder ticks per rotation of the encoder
    private static final double TICKS_PER_REV = 4096; 
    // computation of number of encoder ticks per inch of vertical travel
    private static final double TICKS_PER_INCH = TICKS_PER_REV * REVS_PER_INCH;

    private static final boolean INVERTED = false;
    private static final boolean SENSOR_PHASE = true;

    // manual lift control joystick deadband
    private static final double MANUAL_DEADBAND = 0.2;
    // approximate force needed from the motor to resist gravity
    private static final double HOLDING_POWER = .2;
    // manual lift control gain
    private static final double MANUAL_GAIN = 0.3;

    // lift control motors (sync'd)
    private TalonSRX liftMaster;
    private VictorSPX liftSlave;

    // buttons to home in on a specific position
    private DigitalInput position1Button;
    private DigitalInput position2Button;
    private DigitalInput position3Button;
    private DigitalInput position4Button;

    // joystick axis to manual adjust position
    public AnalogInput manualAdjustmentJoystick;

    // enum of all available control modes
    private enum LiftCtrlMode {
        TRACK, MANUAL, STOPPED;
    }
    // current control mode
    LiftCtrlMode controlMode;

    @Override
    public void init() {
        // initialize everything in their private functions
        initMotors();
        initInputs();
        resetState();
    }

    @Override
    public void inputUpdate(Input source) {
        // set the target based on which button is pressed
        if (source == position1Button &&
            position1Button.getValue()) {
            setTrackingTarget(POSITION_1);
        }
        else if (source == position2Button &&
                 position2Button.getValue()) {
            setTrackingTarget(POSITION_2);
        }
        else if (source == position3Button &&
                 position3Button.getValue()) {
            setTrackingTarget(POSITION_3);
        }
        else if (source == position4Button &&
                 position4Button.getValue()) {
            setTrackingTarget(POSITION_4);
        }
        // manual fine tuning
        else if (source == manualAdjustmentJoystick) {
            double commandOutput = deadband(manualAdjustmentJoystick.getValue(), MANUAL_DEADBAND);
            if (commandOutput != 0) {
                setManualPower(commandOutput);
            } else if (controlMode == LiftCtrlMode.MANUAL) {
                setStopped();
            }
        }
    }

    @Override
    public void selfTest() { }

    @Override
    public void update() {
        // update the Smart Dashboard
        SmartDashboard.putNumber("Lift Encoder Value", liftMaster.getSelectedSensorPosition());
        SmartDashboard.putNumber("Lift Voltage", liftMaster.getMotorOutputVoltage());
        SmartDashboard.putString("Current Command", controlMode.name());
    }

    @Override
    public void resetState() {
        // turn off the lift and set to tracking mode
        liftMaster.set(ControlMode.PercentOutput, 0);
        controlMode = LiftCtrlMode.TRACK;
    }

    @Override
    public String getName() {
        return "LiftMark3";
    }

    // initialize lift motors
    private void initMotors() {
        // Talon is primary motor output
        liftMaster = new TalonSRX(CANConstants.LIFT_TALON);
        liftMaster.setInverted(INVERTED);
        liftMaster.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 0); // econder
        liftMaster.setSensorPhase(SENSOR_PHASE);

        // configure Talon PID values
        for (LiftPID constants : LiftPID.values()) {
            liftMaster.config_kF(constants.k.slot, constants.k.f, 0);
            liftMaster.config_kP(constants.k.slot, constants.k.p, 0);
            liftMaster.config_kI(constants.k.slot, constants.k.i, 0);
            liftMaster.config_kD(constants.k.slot, constants.k.d, 0);
        }

        // Victor follows Talon output
        liftSlave = new VictorSPX(CANConstants.LIFT_VICTOR);
        liftSlave.setInverted(INVERTED);
        liftSlave.follow(liftMaster);
        liftSlave.setNeutralMode(NeutralMode.Brake);
    }

    // initialize buttons
    private void initInputs() {
        IInputManager inputManager = Core.getInputManager();
        position1Button = (DigitalInput) inputManager.getInput(WSInputs.LIFT_PRESET_1);
        position1Button.addInputListener(this);
        position2Button = (DigitalInput) inputManager.getInput(WSInputs.LIFT_PRESET_2);
        position2Button.addInputListener(this);
        position3Button = (DigitalInput) inputManager.getInput(WSInputs.LIFT_PRESET_3);
        position3Button.addInputListener(this);
        position4Button = (DigitalInput) inputManager.getInput(WSInputs.LIFT_PRESET_4);
        position4Button.addInputListener(this);
        manualAdjustmentJoystick = (AnalogInput) inputManager.getInput(WSInputs.LIFT_MANUAL);
        manualAdjustmentJoystick.addInputListener(this);
    }

    // begin tracking a new target (position in inches)
    private void setTrackingTarget(double target) {
        controlMode = LiftCtrlMode.TRACK;
        
        double targetTicks = target * TICKS_PER_INCH;
        boolean goingDown = liftMaster.getSelectedSensorPosition() > targetTicks;

        // switch PID profile base on lift direction
        if (goingDown) {
            liftMaster.selectProfileSlot(LiftPID.DOWNTRACK.k.slot, 0);
        } else {
            liftMaster.selectProfileSlot(LiftPID.TRACKING.k.slot, 0);
        }
        liftMaster.set(ControlMode.Position, targetTicks);

        // update Smart Dashboard
        SmartDashboard.putBoolean("Is Down", goingDown);
        SmartDashboard.putNumber("Target", target);
    }

    // manually control the lift with a given motor power
    private void setManualPower(double power) {
        controlMode = LiftCtrlMode.MANUAL;

        // scale output
        double output = HOLDING_POWER + power * MANUAL_GAIN;
        liftMaster.set(ControlMode.PercentOutput, output);

        // update Smart Dashboard
        SmartDashboard.putNumber("Lift manual output", output);
    }

    // force stop the lift
    private void setStopped() {
        controlMode = LiftCtrlMode.STOPPED;

        // aim for the current position (don't move)
        double positionTicks = liftMaster.getSelectedSensorPosition();
        liftMaster.selectProfileSlot(LiftPID.HOMING.k.slot, 0);
        liftMaster.set(ControlMode.Position, positionTicks);

        // update Smart Dashboard
        SmartDashboard.putNumber("Target", positionTicks / TICKS_PER_INCH);
    }

    // update joystick input relative to deadband
    private static double deadband(double value, double deadband) {
        double gain = 1.0 / (1.0 - deadband);
        double deadbanded_value;
        if (value > deadband) {
            deadbanded_value = (value - deadband) * gain;
        }
        else if (value < deadband) {
            deadbanded_value = (value + deadband) * gain;
        }
        else {
            deadbanded_value = 0;
        }
        return deadbanded_value;
    }
}
