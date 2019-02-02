package org.wildstang.year2019.subsystems.lift;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.TalonSRXConfiguration;

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

    //private DigitalInput AxisConfig.upperLimitSwitch; (AxisConfig.AxisConfig.upperLimitSwitch)
    //private DigitalInput AxisConfig.upperLimitSwitch; (AxisConfig.AxisConfig.upperLimitSwitch)

    // Local outputs
    private TalonSRX liftTalon;

    // Logical variables

    private int currentPosition;
    //private int desiredPosition; (replaced by Axis.roughTarget)


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
        } else if (source == AxisConfig.upperLimitSwitch) {
            // TODO
        } else if (source == AxisConfig.upperLimitSwitch) {
            // TODO
        }
    }

    @Override
    public void init() {
        currentPosition = 0;
        setRoughTarget(0.0);

        initInputs();

        try {
            initOutputs();
        } catch (CTREException e) {
            System.out.println("Failed to init lift talon: " + e);
        }
    }

    private void initInputs() {
        // FIXME Get proper names for each input (temporary position shown)
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

        // FIXME ENUM THIS
        AxisConfig.upperLimitSwitch = (DigitalInput) Core.getInputManager().getInput("Lift Lower Limit Switch");
        AxisConfig.upperLimitSwitch.addInputListener(this);
        AxisConfig.upperLimitSwitch = (DigitalInput) Core.getInputManager().getInput("Lift Upper Limit Switch");
        AxisConfig.upperLimitSwitch.addInputListener(this);
        lowerLimitSwitch = (DigitalInput) Core.getInputManager().getInput("Lift Lower Limit Switch");
        lowerLimitSwitch.addInputListener(this);
        upperLimitSwitch = (DigitalInput) Core.getInputManager().getInput("Lift Upper Limit Switch");
        upperLimitSwitch.addInputListener(this);
    }

    private void initOutputs() throws CTREException {
        // FIXME Change CAN ID to appropriate one; move to CANConstants
        System.out.println("Initializing TalonSRX master ID 0");

        liftTalon = new TalonSRX(CANConstants.LIFT_TALON);

        // FIXME Below is a rough copy from Drive; tailoring to Lift's specific requirements may be required
        liftTalon.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, TIMEOUT);
        liftTalon.setInverted(INVERTED);
        liftTalon.setSensorPhase(SENSOR_PHASE);

        CoreUtils.checkCTRE(liftTalon.configNominalOutputForward(0, TIMEOUT));
        CoreUtils.checkCTRE(liftTalon.configNominalOutputReverse(0, TIMEOUT));
        CoreUtils.checkCTRE(liftTalon.configPeakOutputForward(+1.0, TIMEOUT));
        CoreUtils.checkCTRE(liftTalon.configPeakOutputReverse(-1.0, TIMEOUT));
        
        // FIXME Find appropriate PID constants
        liftTalon.config_kF(0, 0.55);
        liftTalon.config_kP(0, 0.8);
        liftTalon.config_kI(0, 0.001);
        liftTalon.config_kD(0, 10.0);

        liftTalon.setNeutralMode(NeutralMode.Brake);

        // FIXME real logging here
        TalonSRXConfiguration liftTalonConfig = new TalonSRXConfiguration();
        liftTalon.getAllConfigs(liftTalonConfig, TIMEOUT);
        System.out.print(liftTalonConfig.toString("Lift Talon 0"));
    }

    @Override
    public void selfTest() {
        // TODO
    }

    @Override
    public void update() {
        super.update();
    }

    @Override
    public void resetState() {
        // TODO
        super.resetState();

        currentPosition = 0;

        setRoughTarget(0.0);
    }

    @Override
    public String getName() {
        return "Lift";
    }
}
