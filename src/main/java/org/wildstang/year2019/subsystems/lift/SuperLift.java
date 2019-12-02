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
import org.wildstang.year2019.subsystems.lift.LiftPID;
import org.wildstang.framework.timer.StopWatch;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import org.wildstang.framework.pid.PIDConstants;

/**
 * Class:       SuperLift.java
 * Inputs:      7 buttons, 2 limit switches, and 1 joystick axis
 * Outputs:     2 motors, a master and a slave
 * Description: The current implementation of the lift subsystem.
 *              This lift is continuous and not staged so PID constants do not need to change.
 *              Springs cancel out the weight of the lift so no brake is needed.
 */
public class SuperLift implements Subsystem {

    private static final boolean INVERTED = false;
    private static final boolean SENSOR_PHASE = true;

    // stopping positions in inches above the bottom limit switch
    private static double POSITION_1 =   0.0;  // low goal
    private static double POSITION_2 = -10.62; // cargo goal - cargo only
    private static double POSITION_3 = -22.0;  // mid goal
    private static double POSITION_4 = -44.0;  // high goal

    // encoder rotations per inch of vertical travel
    private static final double REVS_PER_INCH = 1/9.087;     
    // encoder ticks per rotation of the encoder
    private static final double TICKS_PER_REV = 4096; 
    // computation of number of encoder ticks per inch of vertical travel
    private static final double TICKS_PER_INCH = TICKS_PER_REV * REVS_PER_INCH;

    // buttons to home in on a specific position
    private DigitalInput position1Button;
    private DigitalInput position2Button;
    private DigitalInput position3Button;
    private DigitalInput position4Button;

    // lift control motors (sync'd)
    private TalonSRX liftMaster;
    private VictorSPX liftSlave;

    public static final double MAX_UPDATE_DT = 0.04;

    // rough target for the lift position
    private double target;

    // timer
    public StopWatch timer = new StopWatch();
    public double lastUpdateTime;

    // state booleans
    public boolean isHoming;
    public boolean isDown;
    private boolean isLimitSwitchOverridden;
    private boolean isPIDOverridden;

    // maximum motor accelerations and speeds, in in/s and in/s^2
    public double MAX_RUN_ACCEL = 2;
    public double MAX_RUN_SPEED = 2;
    public double MAX_HOMING_ACCEL = 1;
    public double MAX_HOMING_SPEED = 1;
    public double MAX_MANUAL_SPEED = 1;

    // furthest the axis may travel in inches in the negative and positive direction
    public double NEGATIVE_TRAVEL_LIMIT = 0;
    public double POSITIVE_TRAVEL_LIMIT = 0;

    // fine tuned manual control joystick
    public AnalogInput manualAdjustmentJoystick;

    // override buttons
    public DigitalInput overrideButtonModifier;
    public DigitalInput limitSwitchOverrideButton;
    public DigitalInput pidOverrideButton;
    
    public boolean overrideButtonValue;
    
    // max travel limit switches
    public DigitalInput lowerLimitSwitch; 
    public DigitalInput upperLimitSwitch;

    /** The position of the lower limit switch (used in homing) */
    public double lowerLimitPosition = 0;

    /** The PID slot to use while moving the axis to a target */
    public int runSlot = 0;
    /** PID constants to use while moving the axis to a target */
    public PIDConstants runK = LiftPID.TRACKING.k;
    /** The PID slot to use while homing the axis */
    public int homingSlot = 1;
    /** PID constants to use while homing the axis */
    public PIDConstants homingK= LiftPID.HOMING.k;

    public int downSlot = 2;
    public PIDConstants downK = LiftPID.DOWNTRACK.k;

    /** Maximum motor output during normal operation */
    public double maxMotorOutput = 1;
    /** Maximum motor output when we've hit a limit switch */
    public double maxLimitedOutput = 0.05;

    public double manualControlModifier = 1.0;

    /** Error within which we consider ourselves "on target" (inches). */
    public double targetWindow = 0.02;
    /**
     * If we go this long without making it into the target window,
     * we assume that we're jammed and go into override (seconds).
     *
     * Ideally, this is less than the time it takes to burn a motor out if
     * we get jammed.
     */
    public double maxTimeToTarget = 200;

    // Threshold (in ticks) for which axis motor can be considered in range of target
    public double axisInRangeThreshold;

    public boolean needtargetUpdate=false;

    // enum of all available control modes
    private enum LiftCtrlMode {
        TRACK, MANUAL, HOME;
    }
    // current control mode
    LiftCtrlMode controlMode;

    @Override
    public void inputUpdate(Input source) {
        // check witch input is being activated
        // lift preset buttons
        if (source == position1Button) {
            if (position1Button.getValue()){
                target = POSITION_1 * TICKS_PER_INCH;
                controlMode = LiftCtrlMode.TRACK;
                needtargetUpdate=false;
            }
        }
        else if (source == position2Button) {
            if (position2Button.getValue()){
                target = POSITION_2 * TICKS_PER_INCH;
                controlMode = LiftCtrlMode.TRACK;
                needtargetUpdate=false;
            }
        }
        else if (source == position3Button) {
            if (position3Button.getValue()){
                target = POSITION_3 * TICKS_PER_INCH;
                controlMode = LiftCtrlMode.TRACK;
                needtargetUpdate=false;
            }
        }
        else if (source == position4Button) {
            if (position4Button.getValue()){
                target = POSITION_4 * TICKS_PER_INCH;
                controlMode = LiftCtrlMode.TRACK;
                needtargetUpdate=false;
                SmartDashboard.putBoolean("High triggered",true);
            }
        }
        // manual adjustment joystick
        else if (source == manualAdjustmentJoystick) {
            if (Math.abs(manualAdjustmentJoystick.getValue()) > 0.2){
                needtargetUpdate=true;
                controlMode = LiftCtrlMode.MANUAL;
            } else if (needtargetUpdate){
                controlMode = LiftCtrlMode.HOME;
            }
        }
        // override buttons
        else if (source == overrideButtonModifier) {
            if (overrideButtonModifier.getValue() == true) {
                overrideButtonValue = true;
            } else {
                overrideButtonValue = false;
            }
        }
        else if (source == pidOverrideButton) {
            if (pidOverrideButton.getValue() == true && overrideButtonValue == true) {
                isPIDOverridden = !isPIDOverridden;
            }
        }
        else if (source == limitSwitchOverrideButton) {
            if (limitSwitchOverrideButton.getValue() == true && overrideButtonValue == true) {
                isLimitSwitchOverridden = !isLimitSwitchOverridden;
            }
        }
        // limit switches
        else if (source == lowerLimitSwitch) {
            SmartDashboard.putBoolean("Lower Limit Switch", lowerLimitSwitch.getValue());
            
            if (lowerLimitSwitch.getValue() == true && !isLimitSwitchOverridden) {
                liftMaster.configPeakOutputForward(maxLimitedOutput, -1);
            } else {
                liftMaster.configPeakOutputForward(maxMotorOutput, -1);
            }
        }
        else if (source == upperLimitSwitch) {
            SmartDashboard.putBoolean("Upper Limit Switch", upperLimitSwitch.getValue());

            if (upperLimitSwitch.getValue() == true && !isLimitSwitchOverridden) {
                liftMaster.configPeakOutputReverse(-maxLimitedOutput, -1);
            } else {
                liftMaster.configPeakOutputReverse(-maxMotorOutput, -1);
            }
        }
    }

    @Override
    public void init() {
        // initialize everything in their private functions
        initInputs();
        initOutputs();
        resetState();
    }

    @Override
    public void selfTest() { }

    @Override
    public void update() {
        // update the smart dashboard
        SmartDashboard.putNumber("Lift Encoder Value", liftMaster.getSensorCollection().getQuadraturePosition());
        SmartDashboard.putNumber("Lift Voltage", liftMaster.getMotorOutputVoltage());
        SmartDashboard.putNumber("Current Command", controlMode.ordinal());
        SmartDashboard.putNumber("Target", target);
        SmartDashboard.putBoolean("Is Down", isDown);
        SmartDashboard.putNumber("Lift Target Difference", Math.abs(liftMaster.getSensorCollection().getQuadraturePosition() -target));

        // switch control mode
        if (isPIDOverridden) {
            controlMode = LiftCtrlMode.MANUAL;
            manualControlModifier = 1.0;
            needtargetUpdate=true;
            manualDrive();
        }
        else if (controlMode == LiftCtrlMode.MANUAL) {
            manualControlModifier = 0.3;
            manualDrive();
        }
        else if (controlMode == LiftCtrlMode.TRACK) {
            track();
        }
        else if (controlMode == LiftCtrlMode.HOME) {
            if (needtargetUpdate){
                target = liftMaster.getSensorCollection().getQuadraturePosition();
                needtargetUpdate=false;
            } 
            
            home();
        }
    }

    // switch to manual mode
    public void manualDrive() {
        liftMaster.set(ControlMode.PercentOutput, manualControlModifier * manualAdjustmentJoystick.getValue());
    }

    // switch to the home profile
    public void home() {
        liftMaster.selectProfileSlot(homingSlot, 0);
        liftMaster.set(ControlMode.Position, -target);
    }

    // track the lift with the current command
    public void track() {
        if (Math.abs(Math.abs(liftMaster.getSensorCollection().getQuadraturePosition()) - Math.abs(target)) < getEncoderLocation(2)) {
            controlMode = LiftCtrlMode.HOME;
            home();
        }
        else {
            if(Math.abs(liftMaster.getSensorCollection().getQuadraturePosition()) < Math.abs(-target)) {
                liftMaster.selectProfileSlot(runSlot, 0);
                isDown = false;
                liftMaster.set(ControlMode.Position, -target-200);
            }
            else if(Math.abs(liftMaster.getSensorCollection().getQuadraturePosition()) > Math.abs(-target)) {
                liftMaster.selectProfileSlot(downSlot, 0);
                isDown = true;
                liftMaster.set(ControlMode.Position, -target + 800);
            }

        }
    }

    @Override
    public void resetState() {
        // set default state of lift
        isHoming = false;
        isDown = false;
        isLimitSwitchOverridden = false;
        isPIDOverridden = false;
        target = POSITION_1 * TICKS_PER_INCH;
    }

    @Override
    public String getName() {
        return "Lift";
    }

    // calculate the current encoder position
    public double getEncoderLocation(double inputTarget) {
        return inputTarget*TICKS_PER_INCH;
    }
    
    // initialize input values
    private void initInputs() {
        IInputManager inputManager = Core.getInputManager();
        // lift preset buttons
        position1Button = (DigitalInput) inputManager.getInput(WSInputs.LIFT_PRESET_1);
        position1Button.addInputListener(this);
        position2Button = (DigitalInput) inputManager.getInput(WSInputs.LIFT_PRESET_2);
        position2Button.addInputListener(this);
        position3Button = (DigitalInput) inputManager.getInput(WSInputs.LIFT_PRESET_3);
        position3Button.addInputListener(this);
        position4Button = (DigitalInput) inputManager.getInput(WSInputs.LIFT_PRESET_4);
        position4Button.addInputListener(this);

        // manual adjustment joysticks
        manualAdjustmentJoystick = (AnalogInput) inputManager.getInput(WSInputs.LIFT_MANUAL);
        manualAdjustmentJoystick.addInputListener(this);

        // override buttons
        overrideButtonModifier = (DigitalInput) inputManager.getInput(WSInputs.WEDGE_SAFETY_2);
        overrideButtonModifier.addInputListener(this);
        pidOverrideButton = (DigitalInput) inputManager.getInput(WSInputs.HATCH_COLLECT);
        pidOverrideButton.addInputListener(this);
        limitSwitchOverrideButton = (DigitalInput) inputManager.getInput(WSInputs.LIFT_LIMIT_SWITCH_OVERRIDE);
        limitSwitchOverrideButton.addInputListener(this);

        // limit switches
        lowerLimitSwitch = (DigitalInput) inputManager.getInput(WSInputs.LIFT_LOWER_LIMIT);
        lowerLimitSwitch.addInputListener(this);
        upperLimitSwitch = (DigitalInput) inputManager.getInput(WSInputs.LIFT_UPPER_LIMIT);
        upperLimitSwitch.addInputListener(this);
    }

    // initialize motor configurations
    private void initOutputs() {
        // setup lift control talon
        liftMaster = new TalonSRX(CANConstants.LIFT_TALON);
        liftMaster.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 0);
        liftMaster.setInverted(INVERTED);
        liftMaster.setSensorPhase(SENSOR_PHASE);
        liftMaster.configNominalOutputForward(0, 0);
        liftMaster.configNominalOutputReverse(0, 0);
        liftMaster.setNeutralMode(NeutralMode.Brake);
        liftMaster.setSelectedSensorPosition(0, 0, -1);

        timer.Start();

        // setup PID values
        // for run mode
        liftMaster.config_kF(runSlot, runK.f, 0);
        liftMaster.config_kP(runSlot, runK.p, 0);
        liftMaster.config_kI(runSlot, runK.i, 0);
        liftMaster.config_kD(runSlot, runK.d, 0);
        // for homing mode
        liftMaster.config_kF(homingSlot, homingK.f, 0);
        liftMaster.config_kP(homingSlot, homingK.p, 0);
        liftMaster.config_kI(homingSlot, homingK.i, 0);
        liftMaster.config_kD(homingSlot, homingK.d, 0);
        // for down mode
        liftMaster.config_kF(downSlot, downK.f, 0);
        liftMaster.config_kP(downSlot, downK.p, 0);
        liftMaster.config_kI(downSlot, downK.i, 0);
        liftMaster.config_kD(downSlot, downK.d, 0);

        // set the default outputs
        setSpeedAndAccel(0, 0);

        // setup victor motor which follows the talon
        liftSlave = new VictorSPX(CANConstants.LIFT_VICTOR);
        liftSlave.setInverted(INVERTED);
        liftSlave.follow(liftMaster);
        liftSlave.setNeutralMode(NeutralMode.Brake);
    }

    // convert speed and acceleration from inches and seconds to ticks and deciseconds
    private void setSpeedAndAccel(double speed, double accel) {
        // inches / second         -> ticks / decisecond
        double speedTicks = speed / 10 * TICKS_PER_INCH;
        // inches / second squared -> ticks / decisecond squared
        double accelTicks = speed / 10 * TICKS_PER_INCH;
        
        // update motor configuration (-1 for no timeout)
        liftMaster.configMotionAcceleration((int) accelTicks, -1);
        liftMaster.configMotionCruiseVelocity((int) speedTicks, -1);
    }
    
    // automatically track lift to a given target level
    public boolean autoLift(int liftLevel) {
        // choose a target
        double target = 0.0;
        if (liftLevel == 1) {
            target = POSITION_1;
        }
        else if (liftLevel == 2) {
            target = POSITION_3;
        }
        else if (liftLevel == 3) {
            target = POSITION_4;
        }

        // set target and update
        target *= TICKS_PER_INCH;
        controlMode = LiftCtrlMode.TRACK;
        update();

        return (Math.abs(Math.abs(liftMaster.getSensorCollection().getQuadraturePosition()) - Math.abs(target)) < getEncoderLocation(2));
    }    
}