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

    /** TODO: remove this */
    //private static final int TIMEOUT = 0;

    private boolean rubberControl = false; 
    private int offFromCenter; 
    private int CENTER = 100; //needs to set manually once axis is created
    private static int RUBBER_FLEX = 200;
    
    /** # of rotations of encoder in one inch of axis travel */
    private static final double REVS_PER_INCH = 10; 
    /** Number of encoder ticks in one revolution */
    private static final double TICKS_PER_REV = 1024; 
    /** # of ticks in one inch of axis movement */
    private static final double TICKS_PER_INCH = TICKS_PER_REV * REVS_PER_INCH;

    /** The maximum speed the operator can command to move in fine-tuning */
    private static final double MANUAL_SPEED = 2; // in/s
    private static final double TRACKING_MAX_SPEED = 20; // in/s
    private static final double TRACKING_MAX_ACCEL = 100; // in/s^2
    private static final double HOMING_MAX_SPEED = 2; // in/s
    private static final double HOMING_MAX_ACCEL = 2; // in/s^2

    private static final double LEFT_STOP_POS = -6;
    private static final double LEFT_MAX_TRAVEL = -5;
    private static final double RIGHT_MAX_TRAVEL = 5;

    private static final double AXIS_IN_RANGE_THRESHOLD = TICKS_PER_INCH * 0.5;

    /** Line position input --- receive from RasPi */
    private RemoteAnalogInput linePositionInput;

    private TalonSRX motor;

    public LineDetector arduino = new LineDetector();

    /** The axis configuration we pass up to the axis initialization */
    private AxisConfig axisConfig = new AxisConfig();

    @Override
    public void inputUpdate(Input source) {
        if (axisConfig.pidOverrideButton.getValue()) {
            //beginHoming(arduino.getLinePosition());
        }
        if (isHoming && !axisConfig.pidOverrideButton.getValue()) {
            //finishHoming();
        }
        if (source == linePositionInput) {
            setRoughTarget(linePositionInput.getValue());
        }
        //System.out.println("test");
           
    }

    @Override
    public void init() {        
        initInputs();
        initOutputs();
        initAxis();
        resetState();
        CENTER = motor.getSelectedSensorPosition();                
    }
    
    @Override
    public void selfTest() {
        // TODO
    }

    @Override
    public void update() {
        double manualMotorSpeed = axisConfig.manualAdjustmentJoystick.getValue();  
        if (axisConfig.lowerLimitSwitch.getValue() && manualMotorSpeed > 0) {
            manualMotorSpeed = 0;
        }
        else if (axisConfig.upperLimitSwitch.getValue() && manualMotorSpeed < 0) {
            manualMotorSpeed = 0;
        }
        if (manualMotorSpeed > 0.1 || manualMotorSpeed < -0.1) {
            motor.set(ControlMode.PercentOutput, manualMotorSpeed);
        }   
        arduino.getLinePosition();

        if(rubberControl) { //rubberControl bool currently has no control to enable, must change bool to true in code. 
            if(manualMotorSpeed > 0.1 || manualMotorSpeed < -0.1) {
                motor.set(ControlMode.PercentOutput, axisConfig.manualAdjustmentJoystick.getValue());
            }
            else {
                if(motor.getSelectedSensorPosition() > CENTER + RUBBER_FLEX) {
                    motor.set(ControlMode.PercentOutput, -0.75);
                }
                if(motor.getSelectedSensorPosition() < CENTER - RUBBER_FLEX) {
                    motor.set(ControlMode.PercentOutput, 0.75);
                }
                else{
                    motor.set(ControlMode.PercentOutput, 0);
                }   
            }
        }

        SmartDashboard.putBoolean("Upper limit switch", axisConfig.upperLimitSwitch.getValue());
        SmartDashboard.putBoolean("Lower limit switch", axisConfig.upperLimitSwitch.getValue());
        SmartDashboard.putNumber("Strafe Encoder Value", motor.getSensorCollection().getQuadraturePosition()); 
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
        linePositionInput = (RemoteAnalogInput) inputManager.getInput(WSInputs.LINE_POSITION);
        linePositionInput.addInputListener(this);

    }

    private void initOutputs() {
        motor = new TalonSRX(CANConstants.STRAFE_TALON);
        motor.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, -1);
        motor.configNominalOutputForward(0, -1);
        motor.configNominalOutputReverse(0, -1);
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

    private void initMotor() {
        while(!axisConfig.lowerLimitSwitch.getValue()) {
            motor.set(ControlMode.PercentOutput, 0.75);
        }
        motor.setSelectedSensorPosition(0);
        while(!axisConfig.upperLimitSwitch.getValue()) {
            motor.set(ControlMode.PercentOutput, -0.75);
        }
        CENTER = motor.getSelectedSensorPosition() / 2;
    }
}
