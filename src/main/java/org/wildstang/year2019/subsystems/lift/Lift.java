package org.wildstang.year2019.subsystems.lift;

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
import org.wildstang.year2019.robot.CANConstants;
import org.wildstang.year2019.robot.WSInputs;
import org.wildstang.year2019.subsystems.common.Axis;

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
public class Lift extends Axis implements Subsystem {

    private static boolean INVERTED = false;
    private static boolean SENSOR_PHASE = false;
    private static int TIMEOUT = 100;

    // All positions in inches above lower limit
    private static double POSITION_1 = 0.5;
    private static double POSITION_2 = 16.5;
    private static double POSITION_3 = 24.5;
    private static double POSITION_4 = 36.5;

    /** Joystick used by operator to make fine adjustments. Passed into axis init. */
    private AnalogInput manualAdjustmentJoystick;

    private DigitalInput position1Button;
    private DigitalInput position2Button;
    private DigitalInput position3Button;
    private DigitalInput position4Button;

    private TalonSRX liftTalon;

    private AxisConfig config;

    // Logical variables

    @Override
    public void inputUpdate(Input source) {
        super.inputUpdate(source);
        // TODO

        if (source == position1Button) {
            setRoughTarget(POSITION_1);
        } else if (source == position2Button) {
            setRoughTarget(POSITION_2);
        } else if (source == position3Button) {
            setRoughTarget(POSITION_3);
        } else if (source == position4Button) {
            setRoughTarget(POSITION_4);
        }
    }

    @Override
    public void init() {
        initInputs();
        initOutputs();
        resetState();
    }

    private void initInputs() {
        manualAdjustmentJoystick = (AnalogInput) Core.getInputManager().getInput(WSInputs.LIFT_MANUAL);
        manualAdjustmentJoystick.addInputListener(this);
        position1Button = (DigitalInput) Core.getInputManager().getInput(WSInputs.LIFT_PRESET_1);
        position1Button.addInputListener(this);
        position2Button = (DigitalInput) Core.getInputManager().getInput(WSInputs.LIFT_PRESET_2);
        position2Button.addInputListener(this);
        position3Button = (DigitalInput) Core.getInputManager().getInput(WSInputs.LIFT_PRESET_3);
        position3Button.addInputListener(this);
        position4Button = (DigitalInput) Core.getInputManager().getInput(WSInputs.LIFT_PRESET_4);
        position4Button.addInputListener(this);

        config.upperLimitSwitch = (DigitalInput) Core.getInputManager().getInput(WSInputs.LIFT_LOWER_LIMIT);
        config.upperLimitSwitch.addInputListener(this);
        config.upperLimitSwitch = (DigitalInput) Core.getInputManager().getInput(WSInputs.LIFT_UPPER_LIMIT);
        config.upperLimitSwitch.addInputListener(this);
    }

    private void initOutputs() throws CTREException {
        System.out.println("Initializing lift Talon ID " + CANConstants.LIFT_TALON);
        liftTalon = new TalonSRX(CANConstants.LIFT_TALON);
        liftTalon.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, TIMEOUT);
        liftTalon.setInverted(INVERTED);
        liftTalon.setSensorPhase(SENSOR_PHASE);
        CoreUtils.checkCTRE(liftTalon.configNominalOutputForward(0, TIMEOUT));
        CoreUtils.checkCTRE(liftTalon.configNominalOutputReverse(0, TIMEOUT));
        // Peak output is managed by Axis class
        // PID settings are managed by Axis class
        liftTalon.setNeutralMode(NeutralMode.Brake);
    }

    @Override
    public void selfTest() {
    }

    @Override
    public void update() {
        super.update();
    }

    @Override
    public void resetState() {
        // TODO
        super.resetState();
        setRoughTarget(POSITION_1);
    }

    @Override
    public String getName() {
        return "Lift";
    }
}
