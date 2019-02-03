package org.wildstang.year2019.subsystems.lift;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.TalonSRXConfiguration;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;

import org.wildstang.framework.io.inputs.AnalogInput;
import org.wildstang.framework.CoreUtils;
import org.wildstang.framework.CoreUtils.CTREException;
import org.wildstang.framework.core.Core;
import org.wildstang.framework.io.inputs.DigitalInput;
import org.wildstang.framework.io.Input;
import org.wildstang.framework.subsystems.Subsystem;
import org.wildstang.year2019.robot.WSInputs;

/** This subsystem goes up and down and puts hatches on holes.
 * 
 * Because this year's lift is continuous and not staged, the PID constants do
 * not need to change when the lift moves up and down.
 * 
 * This lift has no brake. There will be springs canceling out the weight of
 * the lift, making PID control alone sufficient.
 * 
 * Because the hatch injection mechanism and the lift are somewhat coupled, 
 * this one subsystem is responsible for both. Hatch-specific code goes in 
 * Hatch.java?
 * 
 * Sensors: 
 * <ul>
 * <li> Limit switch(es). TODO: top, bottom or both?
 * <li> Encoder on lift Talon.
 * <li> pneumatic pressure sensor.
 * </ul>
 * 
 * Actuators:
 * <ul>
 * <li> Talon driving lift.
 * <li> Piston solenoids for hatch mechanism TODO detail here.
 * </ul>
 * 
 */
public class Lift implements Subsystem {

    // Local inputs
    private AnalogInput manualAdjustmentJoystick;

    private DigitalInput position1Button;
    private DigitalInput position2Button;
    private DigitalInput position3Button;
    private DigitalInput position4Button;

    private DigitalInput lowerLimitSwitch;
    private DigitalInput upperLimitSwitch;

    // Local outputs
    private TalonSRX leftLift;
    private VictorSPX rightLift;

    // Logical variables
    private int currentPosition;
    private int desiredPosition;
    private boolean movingToPosition;

    // Constants
    private final int[] positionEncoderTickLocations = {0, 0, 0, 0}; // TODO Determine locations
    private final int encoderTickDifferenceThreshold = 20;
    private final double joystickDeadband = 0.05;

    @Override
    public void inputUpdate(Input source) {
        if (source == manualAdjustmentJoystick) {
            desiredPosition = 0;
        } else if (source == position1Button) {
            desiredPosition = 1;
        } else if (source == position2Button) {
            desiredPosition = 2;
        } else if (source == position3Button) {
            desiredPosition = 3;
        } else if (source == position4Button) {
            desiredPosition = 4;
        }
    }

    @Override
    public void init() {
        currentPosition = 0;
        desiredPosition = 0;
        movingToPosition = false;

        initInputs();

        try {
            initOutputs();
        } catch (CTREException e) {
            System.out.println("Failed to init lift talon: " + e);
        }
    }

    private void initInputs() {
        manualAdjustmentJoystick = (AnalogInput) Core.getInputManager().getInput(WSInputs.LIFT_MANUAL);
        manualAdjustmentJoystick.addInputListener(this);

        position1Button = (DigitalInput) Core.getInputManager().getInput(WSInputs.LIFT_PRESENT_UP);
        position1Button.addInputListener(this);
        position2Button = (DigitalInput) Core.getInputManager().getInput(WSInputs.LIFT_PRESENT_DOWN);
        position2Button.addInputListener(this);
        position3Button = (DigitalInput) Core.getInputManager().getInput(WSInputs.LIFT_PRESENT_RIGHT);
        position3Button.addInputListener(this);
        position4Button = (DigitalInput) Core.getInputManager().getInput(WSInputs.LIFT_PRESENT_LEFT);
        position4Button.addInputListener(this);

        lowerLimitSwitch = (DigitalInput) Core.getInputManager().getInput(WSInputs.LIFT_LOWER_LIMIT);
        lowerLimitSwitch.addInputListener(this);
        upperLimitSwitch = (DigitalInput) Core.getInputManager().getInput(WSInputs.LIFT_UPPER_LIMIT);
        upperLimitSwitch.addInputListener(this);
    }

    private void initOutputs() throws CTREException {
        System.out.println("Initializing TalonSRX master ID 7");

        leftLift = new TalonSRX(7);

        // TODO Below is a rough copy from Drive; tailoring to Lift's specific requirements may be required
        leftLift.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 100);
        leftLift.setInverted(false);
        leftLift.setSensorPhase(true);

        CoreUtils.checkCTRE(leftLift.configNominalOutputForward(0, 100));
        CoreUtils.checkCTRE(leftLift.configNominalOutputReverse(0, 100));
        CoreUtils.checkCTRE(leftLift.configPeakOutputForward(+1.0, 100));
        CoreUtils.checkCTRE(leftLift.configPeakOutputReverse(-1.0, 100));
        
        leftLift.config_kF(0, 0.0);
        leftLift.config_kP(0, 0.2);
        leftLift.config_kI(0, 0.0);
        leftLift.config_kD(0, 5.0);

        leftLift.setNeutralMode(NeutralMode.Coast);

        TalonSRXConfiguration liftTalonConfig = new TalonSRXConfiguration();
        leftLift.getAllConfigs(liftTalonConfig, 100);
        System.out.print(liftTalonConfig.toString("Lift Talon 7"));

        System.out.println("Initializing VictorSPX follower ID 8");

        rightLift = new VictorSPX(8);

        rightLift.follow(leftLift);
    }

    @Override
    public void selfTest() {
    }

    @Override
    public void update() {
        // Movement should occur under any of the following conditions (in order of control priority):
        //   1. Manipulator uses joystick to manually adjust lift height
        //   2. Lift's current preset position differs from desired one (manipulator presses a lift preset button)
        //   3. Manipulator hasn't finished movement (failover in case manipulator requests to go back to current position)
        if (manualAdjustmentJoystick.getValue() < -joystickDeadband || manualAdjustmentJoystick.getValue() > joystickDeadband) {
            if (lowerLimitSwitch.getValue() == true && manualAdjustmentJoystick.getValue() < 0.0) {
                leftLift.set(ControlMode.PercentOutput, 0.0);
            } else if (upperLimitSwitch.getValue() == true && manualAdjustmentJoystick.getValue() > 0.0) {
                leftLift.set(ControlMode.PercentOutput, 0.0);
            } else {
                leftLift.set(ControlMode.PercentOutput, manualAdjustmentJoystick.getValue());
            }
        } else if (currentPosition != desiredPosition || movingToPosition) {
            if (findMinEncoderTickDiffernece(leftLift.getSelectedSensorPosition(0)) < encoderTickDifferenceThreshold) {
                movingToPosition = false;
                currentPosition = desiredPosition;
                leftLift.selectProfileSlot(LiftPID.TRACKING.slot, 0);
            } else {
                movingToPosition = true;
                leftLift.selectProfileSlot(LiftPID.HOMING.slot, 0);

                if (desiredPosition == 1) {
                    leftLift.set(ControlMode.Position, positionEncoderTickLocations[0]);
                } else if (desiredPosition == 2) {
                    leftLift.set(ControlMode.Position, positionEncoderTickLocations[1]);
                } else if (desiredPosition == 3) {
                    leftLift.set(ControlMode.Position, positionEncoderTickLocations[2]);
                } else if (desiredPosition == 4) {
                    leftLift.set(ControlMode.Position, positionEncoderTickLocations[3]);
                }
            }
        }
    }

    @Override
    public void resetState() {
        currentPosition = 0;
        desiredPosition = 0;
        movingToPosition = false;
    }

    @Override
    public String getName() {
        return "Lift";
    }

    private int findMinEncoderTickDiffernece(int encoderTick) {
        int[] encoderTickDifferences = new int[4];
        for (int i = 0; i < encoderTickDifferences.length; i++) {
            encoderTickDifferences[i] = Math.abs(encoderTick - positionEncoderTickLocations[i]);
        }

        int least = encoderTickDifferences[0];
        for (int i = 0; i < encoderTickDifferences.length; i++) {
            if (encoderTickDifferences[i] < least) {
                least = encoderTickDifferences[i];
            }
        }

        return least;
    }
}