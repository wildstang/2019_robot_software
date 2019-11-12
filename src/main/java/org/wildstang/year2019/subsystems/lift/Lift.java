package org.wildstang.year2019.subsystems.lift;


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
import org.wildstang.year2019.subsystems.common.Axis;
import org.wildstang.year2019.subsystems.lift.LiftPID;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * Class:       Lift.java
 * Inputs:      7 buttons, 2 limit switches, and 1 joystick axis
 * Outputs:     2 motors, a master and a slave
 * Description: This is the first implementation of the lift subsystem.
 *              The lift veritical raises and lowers hatches which are picked up.
 *              It is continuous, not staged, so PID constants don't need to be updated as it moves.
 *              There are no breakes, springs cancel out the weight of the lift.
 *              The lift consists of 2 motors controlled together, which can be manual controlled with a joystick,
 *              or automatically move to positions by button press.
 *              This class only directly oversees the position, the Axis parent class actually controls the PID.
 */
public class Lift extends Axis implements Subsystem {

    private static final boolean INVERTED = false;
    private static final boolean SENSOR_PHASE = true;

    // stopping positions in inches above the bottom limit switch
    private static double POSITION_1 = 0.0;     // low goal
    private static double POSITION_2 = 8.0;     // cargo goal - cargo only
    private static double POSITION_3 = 2.4228;  // mid goal
    private static double POSITION_4 = 6.0;     // high goal

    // encoder rotations per inch of vertical travel
    private static final double REVS_PER_INCH = 5.092;     
    // encoder ticks per rotation of the encoder
    private static final double TICKS_PER_REV = 1024; 
    // computation of number of encoder ticks per inch of vertical travel
    private static final double TICKS_PER_INCH = TICKS_PER_REV * REVS_PER_INCH;

    // max speeds and accelerations in inches per second
    private static final double MANUAL_SPEED       = 2; // manual control speed
    private static final double TRACKING_MAX_SPEED = 1; // tracking speed
    private static final double TRACKING_MAX_ACCEL = 1; // tracking acceleration
    private static final double HOMING_MAX_SPEED   = 1; // homing speed
    private static final double HOMING_MAX_ACCEL   = 1; // homing acceleration

    // lift limitations
    private static final double BOTTOM_STOP_POS = -.5;
    private static final double BOTTOM_MAX_TRAVEL = 0;
    private static final double TOP_MAX_TRAVEL = 40;

    // buttons to home in on a specific button
    private DigitalInput position1Button;
    private DigitalInput position2Button;
    private DigitalInput position3Button;
    private DigitalInput position4Button;

    // lift control motors (sync'd)
    private TalonSRX liftMaster;
    private VictorSPX liftSlave;

    @Override
    public void inputUpdate(Input source) {
        super.inputUpdate(source);
        
        // set the target based on which button is pressed
        if (source == position1Button &&
            position1Button.getValue()) {
            setRoughTarget(POSITION_1);
        }
        else if (source == position2Button &&
                 position2Button.getValue()) {
            setRoughTarget(POSITION_2);
        }
        else if (source == position3Button &&
                 position3Button.getValue()) {
            setRoughTarget(POSITION_3);
        }
        else if (source == position4Button &&
                 position4Button.getValue()) {
            setRoughTarget(POSITION_4);
        }
    }

    @Override
    public void init() {
        // initialize everything in their private functions
        initInputs();
        initOutputs();
        initAxis();
        resetState();
    }

    @Override
    public void selfTest() { }

    @Override
    public void update() {
        // update Smart Dashboard, parent Axis class will manage everything else
        SmartDashboard.putNumber("Lift Encoder Value", liftMaster.getSensorCollection().getQuadraturePosition());
        super.update();
    }

    @Override
    public void resetState() {
        // reset parent, then return to bottom (position 1)
        super.resetState();
        setRoughTarget(POSITION_1);
    }

    @Override
    public String getName() {
        return "Lift";
    }

    /**
     * Private initialization functions
     */

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
    }

    // initialize motors
    private void initOutputs() {
        // initialize lift master Talon
        // PID and peak output are managed by the Axis class
        liftMaster = new TalonSRX(CANConstants.LIFT_TALON);
        liftMaster.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 0);
        liftMaster.setInverted(INVERTED);
        liftMaster.setSensorPhase(SENSOR_PHASE);
        liftMaster.configNominalOutputForward(0, 0);
        liftMaster.configNominalOutputReverse(0, 0);

        // initialize lift slave Victor
        liftSlave = new VictorSPX(CANConstants.LIFT_VICTOR);
        liftSlave.setInverted(INVERTED);
        liftSlave.follow(motor);
        liftSlave.setNeutralMode(NeutralMode.Brake);
    }

    // initialize the axis
    private void initAxis() {
        IInputManager inputManager = Core.getInputManager();
        AxisConfig axisConfig = new AxisConfig();

        // initialize limit switches
        axisConfig.lowerLimitSwitch = (DigitalInput) inputManager.getInput(WSInputs.LIFT_LOWER_LIMIT);
        axisConfig.lowerLimitSwitch.addInputListener(this);
        axisConfig.upperLimitSwitch = (DigitalInput) inputManager.getInput(WSInputs.LIFT_UPPER_LIMIT);
        axisConfig.upperLimitSwitch.addInputListener(this);

        // initialize manual control joystick
        axisConfig.manualAdjustmentJoystick = (AnalogInput) inputManager.getInput(WSInputs.LIFT_MANUAL);
        axisConfig.manualAdjustmentJoystick.addInputListener(this);

        // initialize override buttons
        axisConfig.overrideButtonModifier = (DigitalInput) inputManager.getInput(WSInputs.WEDGE_SAFETY_2);
        axisConfig.overrideButtonModifier.addInputListener(this);
        axisConfig.pidOverrideButton = (DigitalInput) inputManager.getInput(WSInputs.HATCH_COLLECT);
        axisConfig.pidOverrideButton.addInputListener(this);
        axisConfig.limitSwitchOverrideButton = (DigitalInput) inputManager.getInput(WSInputs.LIFT_LIMIT_SWITCH_OVERRIDE);
        axisConfig.limitSwitchOverrideButton.addInputListener(this);

        // pass master lift motor to axis
        axisConfig.motor = liftMaster;

        // set constant values
        axisConfig.ticksPerInch = TICKS_PER_INCH;
        axisConfig.runAcceleration = TRACKING_MAX_ACCEL;
        axisConfig.runSpeed = TRACKING_MAX_SPEED;
        axisConfig.homingAcceleration = HOMING_MAX_ACCEL;
        axisConfig.homingSpeed = HOMING_MAX_SPEED;
        axisConfig.manualSpeed = MANUAL_SPEED;
        axisConfig.minTravel = BOTTOM_MAX_TRAVEL;
        axisConfig.maxTravel = TOP_MAX_TRAVEL;
        axisConfig.lowerLimitPosition = BOTTOM_STOP_POS;

        // setup PID
        axisConfig.runSlot = LiftPID.TRACKING.k.slot;
        axisConfig.runK = LiftPID.TRACKING.k;
        axisConfig.homingSlot = LiftPID.HOMING.k.slot;
        axisConfig.homingK = LiftPID.HOMING.k;

        // pass axis to parent
        initAxis(axisConfig);
    }
}
