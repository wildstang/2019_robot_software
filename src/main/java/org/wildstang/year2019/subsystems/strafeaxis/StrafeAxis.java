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


    private boolean rubberControl = true; 
    private int offFromCenter; 
    private int CENTER = 100; //needs to set manually once axis is created
    private static int RUBBER_FLEX = 30;
    
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

    public boolean isLimitSwitchOverridden;
    public boolean overrideButtonValue;

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
        
        if (source == axisConfig.overrideButtonModifier) {
            overrideButtonValue = axisConfig.overrideButtonModifier.getValue();
        } else if (source == axisConfig.limitSwitchOverrideButton) {
            if (axisConfig.limitSwitchOverrideButton.getValue() == true && axisConfig.overrideButtonValue) {
                isLimitSwitchOverridden = !isLimitSwitchOverridden;
            }
        } else if (source == axisConfig.lowerLimitSwitch) {
            SmartDashboard.putBoolean("Left Limit Switch", axisConfig.lowerLimitSwitch.getValue());
        } else if (source == axisConfig.upperLimitSwitch) {
            SmartDashboard.putBoolean("Right Limit Switch", axisConfig.upperLimitSwitch.getValue());
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
        ///super.update();

         //double time = timer.get();
         double time = super.timer.GetTimeInSec();//timertesting
         double dT = time - super.lastUpdateTime;
         lastUpdateTime = time;
         // Clamp the dT to be no more than MAX_UPDATE_DT so that
         // if we glitch and don't update for a while we don't do a big jerk motion
         if (dT > super.MAX_UPDATE_DT) {
             System.out.println("WARNING: MAX_UPDATE_DT exceeded in Axis");
             dT = super.MAX_UPDATE_DT;
         }
                 
        double manualMotorSpeed = axisConfig.manualAdjustmentJoystick.getValue();  ///Positives and negitives may need to be reversed
        if (!isLimitSwitchOverridden) {
            if (axisConfig.lowerLimitSwitch.getValue() && manualMotorSpeed > 0) {
                manualMotorSpeed = 0;
            }
            else if (axisConfig.upperLimitSwitch.getValue() && manualMotorSpeed < 0) {
                manualMotorSpeed = 0;
            }
        }
        if (manualMotorSpeed > 0.1 || manualMotorSpeed < -0.1) {
            motor.set(ControlMode.PercentOutput, manualMotorSpeed);
        }
        SmartDashboard.putNumber("StrafeAxis Motor Speed", manualMotorSpeed);
        SmartDashboard.putBoolean("Lower Limit Switch", axisConfig.lowerLimitSwitch.getValue());
        SmartDashboard.putBoolean("Upper Limit Switch", axisConfig.upperLimitSwitch.getValue());
        SmartDashboard.putNumber("StrafeAxis Encoder Value", motor.getSensorCollection().getQuadraturePosition());

        arduino.getLinePosition();

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
        motor.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 0);
        motor.configNominalOutputForward(0, 0);
        motor.configNominalOutputReverse(0, 0);
        // peak output managed by axis
        // speed and accel managed by axis
        motor.setInverted(INVERTED);
        motor.setSensorPhase(SENSOR_PHASE);
        motor.overrideLimitSwitchesEnable(false);
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

    }
}
