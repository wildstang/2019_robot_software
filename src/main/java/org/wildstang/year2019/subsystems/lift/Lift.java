package org.wildstang.year2019.subsystems.lift;

import com.ctre.phoenix.motorcontrol.ControlMode;
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
    private TalonSRX liftTalon;

    // Logical variables
    private int currentPosition;
    private int desiredPosition;
    

    @Override
    public void inputUpdate(Input source) {
        // TODO

        if (source == manualAdjustmentJoystick) {
            desiredPosition = 0;

            // TODO
        } else if (source == position1Button) {
            desiredPosition = 1;
        } else if (source == position2Button) {
            desiredPosition = 2;
        } else if (source == position3Button) {
            desiredPosition = 3;
        } else if (source == position4Button) {
            desiredPosition = 4;
        } else if (source == lowerLimitSwitch) {
            // TODO
        } else if (source == upperLimitSwitch) {
            // TODO
        }
    }

    @Override
    public void init() {
        // TODO

        currentPosition = 0;
        desiredPosition = 0;

        initInputs();

        try {
            initOutputs();
        } catch (CTREException e) {
            System.out.println("Failed to init lift talon: " + e);
        }
    }

    private void initInputs() {
        // FIXME Get proper names for each input (temporary position shown)
        manualAdjustmentJoystick = (AnalogInput) Core.getInputManager().getInput("Right Vertical Joystick");
        manualAdjustmentJoystick.addInputListener(this);

        position1Button = (DigitalInput) Core.getInputManager().getInput("Lift Position 1");
        position1Button.addInputListener(this);
        position2Button = (DigitalInput) Core.getInputManager().getInput("Lift Position 2");
        position2Button.addInputListener(this);
        position3Button = (DigitalInput) Core.getInputManager().getInput("Lift Position 3");
        position3Button.addInputListener(this);
        position4Button = (DigitalInput) Core.getInputManager().getInput("Lift Position 4");
        position4Button.addInputListener(this);

        lowerLimitSwitch = (DigitalInput) Core.getInputManager().getInput("Lift Lower Limit Switch");
        lowerLimitSwitch.addInputListener(this);
        upperLimitSwitch = (DigitalInput) Core.getInputManager().getInput("Lift Upper Limit Switch");
        upperLimitSwitch.addInputListener(this);
    }

    private void initOutputs() throws CTREException {
        // FIXME Change CAN ID to appropriate one
        System.out.println("Initializing TalonSRX master ID 0");

        liftTalon = new TalonSRX(0);

        // FIXME Below is a rough copy from Drive; tailoring to Lift's specific requirements may be required
        liftTalon.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 100);
        liftTalon.setInverted(false);
        liftTalon.setSensorPhase(true);

        CoreUtils.checkCTRE(liftTalon.configNominalOutputForward(0, 100));
        CoreUtils.checkCTRE(liftTalon.configNominalOutputReverse(0, 100));
        CoreUtils.checkCTRE(liftTalon.configPeakOutputForward(+1.0, 100));
        CoreUtils.checkCTRE(liftTalon.configPeakOutputReverse(-1.0, 100));
        
        // FIXME Find appropriate PID constants
        liftTalon.config_kF(0, 0.55);
        liftTalon.config_kP(0, 0.8);
        liftTalon.config_kI(0, 0.001);
        liftTalon.config_kD(0, 10.0);

        liftTalon.setNeutralMode(NeutralMode.Coast);

        TalonSRXConfiguration liftTalonConfig = new TalonSRXConfiguration();
        liftTalon.getAllConfigs(liftTalonConfig, 100);
        System.out.print(liftTalonConfig.toString("Lift Talon 0"));
    }

    @Override
    public void selfTest() {
        // TODO
    }

    @Override
    public void update() {
        // TODO

        if (manualAdjustmentJoystick.getValue() < -0.05 || manualAdjustmentJoystick.getValue() > 0.05) {
            liftTalon.set(ControlMode.PercentOutput, manualAdjustmentJoystick.getValue());
        } else if (currentPosition != desiredPosition) { // If current position differs from desired one, movement is required
            // TODO
        }
    }

    @Override
    public void resetState() {
        // TODO

        currentPosition = 0;
        desiredPosition = 0;
    }

    @Override
    public String getName() {
        return "Lift";
    }
}