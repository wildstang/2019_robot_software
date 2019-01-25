package org.wildstang.year2019.subsystems.strafeaxis;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import org.wildstang.framework.CoreUtils;
import org.wildstang.framework.CoreUtils.CTREException;
import org.wildstang.framework.core.Core;
import org.wildstang.framework.io.IInputManager;
import org.wildstang.framework.io.Input;
import org.wildstang.framework.io.inputs.AnalogInput;
import org.wildstang.framework.io.inputs.DigitalInput;
import org.wildstang.framework.io.inputs.RemoteAnalogInput;
import org.wildstang.framework.subsystems.Subsystem;
import org.wildstang.framework.timer.WsTimer;
import org.wildstang.year2019.robot.CANConstants;
import org.wildstang.year2019.robot.WSInputs;

/** This subsystem is responsible for lining up hatch panels left-to-right.
 * 
 * There should probably be a PID loop controlling the position of this axis.
 * 
 * Sensors: 
 * <ul>
 * <li> Line detection photocells (handled by LineDetector.java? or RasPi?)
 * <li> Limit switch(es). TODO: left, right or both?
 * <li> Encoder on lead screw Talon.
 * </ul>
 * 
 * Actuators:
 * <ul>
 * <li> Talon controlling lead screw motor.
 * </ul>
 * 
 */

public class StrafeAxis implements Subsystem {

    private static int TIMEOUT = 100;

    /** Line position input --- receive from RasPi */
    private RemoteAnalogInput linePositionInput;
    private DigitalInput leftLimitSwitch;
    private DigitalInput rightLimitSwitch;
    /** Stick operator uses to fine-tune left-right position */
    private AnalogInput fineTuneInput;

    /* Width of the space we have to play in */
    private int leftMaxTravel;
    private int rightMaxTravel;

    private TalonSRX motor;

    /** Accumulator for operator fine-tuning */
    private double fineTuneOffset;

    private WsTimer timer;
    private double lastUpdateTime;
    private double timeBeginHoming;

    /** The axis may be in different modes --- homing while finding limits, tracking while in operation. */
    private enum Mode {
        DISABLED,
        HOMING_LEFT,
        HOMING_RIGHT,
        TRACKING;
        // TODO Overridden state
    }

    private Mode mode;

    @Override
    public void inputUpdate(Input source) {
        if (source == linePositionInput) {
            // Nothing to do; we handle this in update()
        } else if (source == leftLimitSwitch) {
            if (mode == Mode.HOMING_LEFT) {
                homingLeftLimitReached();
            } else {
                // TODO
            }
        } else if (source == rightLimitSwitch) {
            if (mode == Mode.HOMING_RIGHT) {
                homingRightLimitReached();
            } else {
                // TODO
            }
        } else if (source == fineTuneInput) {
            // Nothing to do; we handle this in update()
        }
    }

    @Override
    public void init() {
        initInputs();
        try {
            initMotor();
        } catch (CoreUtils.CTREException e) {
            // FIXME crash
        }
        resetState();
        timer = new WsTimer();
        timer.start();
        mode = Mode.DISABLED;
    }

    @Override
    public void selfTest() {
        // TODO
    }

    @Override
    public void update() {
        double time = timer.get();
        double dT = time - lastUpdateTime;
        lastUpdateTime = time;
        // Clamp the dT to be no more than MAX_UPDATE_DT so that
        // if we glitch and don't update for a while we don't do a big jerk motion
        dT = Math.min(dT, StrafeConstants.MAX_UPDATE_DT);

        if (mode == Mode.TRACKING) {
            fineTuneOffset += fineTuneInput.getValue() * StrafeConstants.FINE_TUNE_MAX_SPEED * dT;
            setTarget(linePositionInput.getValue() + fineTuneOffset);
        }

        if (mode == Mode.HOMING_LEFT || mode == Mode.HOMING_RIGHT) {
            if (time - timeBeginHoming > StrafeConstants.MAX_HOMING_TIME) {
                System.out.println("FAILED TO HOME STRAFE AXIS! DISABLING!");
                // FIXME proper logging
                disable();
            }
        }
    }

    @Override
    public void resetState() {
        fineTuneOffset = 0.0;
    }

    @Override
    public String getName() {
        return "StrafeAxis";
    }

    /** Initiate re-homing the axis. Home left then right. */
    public void beginHomeCycle() {
        mode = Mode.HOMING_LEFT;
        motor.selectProfileSlot(StrafePID.HOMING.slot, 0);
        motor.configMotionAcceleration(StrafeConstants.HOMING_MAX_ACCEL);
        motor.configMotionCruiseVelocity(StrafeConstants.HOMING_MAX_SPEED);
        motor.set(ControlMode.MotionMagic, Double.NEGATIVE_INFINITY);
        timeBeginHoming = timer.get();
    }

    ////////////////////////////////////////
    // Private methods

    private void initInputs() {
        IInputManager inputManager = Core.getInputManager();
        linePositionInput = (RemoteAnalogInput) inputManager.getInput(WSInputs.LINE_POSITION);
        linePositionInput.addInputListener(this);
        leftLimitSwitch = (DigitalInput) inputManager.getInput(WSInputs.STRAFE_LEFT_LIMIT);
        leftLimitSwitch.addInputListener(this);
        rightLimitSwitch = (DigitalInput) inputManager.getInput(WSInputs.STRAFE_RIGHT_LIMIT);
        rightLimitSwitch.addInputListener(this);
        fineTuneInput = (AnalogInput) inputManager.getInput(WSInputs.HATCH_STRAFE);
        fineTuneInput.addInputListener(this);
    }

    private void initMotor() throws CoreUtils.CTREException {
        // FIXME duplicates setup code in drivebase --- factor this out
        motor = new TalonSRX(CANConstants.STRAFE_TALON);

        motor.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, TIMEOUT);

        for (StrafePID pid : StrafePID.values()) {
            motor.config_kF(pid.slot, pid.k.f);
            motor.config_kP(pid.slot, pid.k.p);
            motor.config_kI(pid.slot, pid.k.i);
            motor.config_kD(pid.slot, pid.k.d);
        }

        // Configure output to range from full-forward to full-reverse.
        CoreUtils.checkCTRE(motor.configNominalOutputForward(0, TIMEOUT));
        CoreUtils.checkCTRE(motor.configNominalOutputReverse(0, TIMEOUT));
        CoreUtils.checkCTRE(motor.configPeakOutputForward(+1.0, TIMEOUT));
        CoreUtils.checkCTRE(motor.configPeakOutputReverse(-1.0, TIMEOUT));

        CoreUtils.checkCTRE(motor.configMotionAcceleration(StrafeConstants.HOMING_MAX_ACCEL, TIMEOUT));
        CoreUtils.checkCTRE(motor.configMotionCruiseVelocity(StrafeConstants.HOMING_MAX_SPEED, TIMEOUT));

        motor.setNeutralMode(NeutralMode.Brake);
        motor.setInverted(StrafeConstants.INVERTED);
        motor.setSensorPhase(StrafeConstants.SENSOR_PHASE);

        motor.selectProfileSlot(StrafePID.HOMING.slot, 0);
    }

    /** Called when the homing cycle hits left limit switch. */
    private void homingLeftLimitReached() {
        leftMaxTravel = motor.getSelectedSensorPosition();
        motor.set(ControlMode.MotionMagic, Double.POSITIVE_INFINITY);
    }

    /** Called when the homing cycle hits right limit switch. */
    private void homingRightLimitReached() {
        rightMaxTravel = motor.getSelectedSensorPosition();
        double center = (leftMaxTravel + rightMaxTravel) / 2;
        leftMaxTravel -= center;
        rightMaxTravel -= center;
        motor.setSelectedSensorPosition(rightMaxTravel);

        leftMaxTravel += StrafeConstants.TRAVEL_PADDING_TICKS;
        rightMaxTravel -= StrafeConstants.TRAVEL_PADDING_TICKS;

        mode = Mode.TRACKING;
        motor.selectProfileSlot(StrafePID.TRACKING.slot, 0);
        motor.configMotionAcceleration(StrafeConstants.TRACKING_MAX_ACCEL);
        motor.configMotionCruiseVelocity(StrafeConstants.TRACKING_MAX_SPEED);

        motor.set(ControlMode.MotionMagic, 0);
    }

    /**
     * Set the target position for the strafe axis. Only has an effect when in TRACKING
     * mode --- when we are homing or disabled this does nothing.
     * @param position Inches right of center
     */
    private void setTarget(double position) {
        if (mode == Mode.TRACKING) {
            double targetTicks = position * StrafeConstants.TICKS_PER_INCH;
            double clampedTarget = Math.min(rightMaxTravel, Math.max(targetTicks, leftMaxTravel));
            motor.set(ControlMode.MotionMagic, clampedTarget);
        }
    }

    /**
     * Disable subsystem.
     */
    private void disable() {
        motor.set(ControlMode.PercentOutput, 0);
        mode = Mode.DISABLED;
    }
}