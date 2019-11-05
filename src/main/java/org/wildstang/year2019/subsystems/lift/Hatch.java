package org.wildstang.year2019.subsystems.lift;

import org.wildstang.framework.io.Input;
import org.wildstang.framework.io.inputs.DigitalInput;
import org.wildstang.framework.subsystems.Subsystem;
import org.wildstang.framework.core.Core;
import org.wildstang.year2019.robot.WSInputs;
import org.wildstang.year2019.robot.WSOutputs;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import org.wildstang.hardware.crio.outputs.WsSolenoid;

import org.wildstang.framework.timer.WsTimer;

/**
 * Class:       Hatch.java
 * Inputs:      2 buttons
 * Outputs:     2 solenoids
 * Description: Controls the hatch-mech which is used to pickup and deliver hatches.
 *              Comprised of 2 solenoids, one which locks/unlocks the hatch-mech, and another which deploys/retracts it.
 *              Each solenoid motion is actived when the appropriate button is pressed.
 */
public class Hatch implements Subsystem {
    // timer constants
    private static final double DEPLOY_WAIT = 0.15;
    private static final double RETRACT_WAIT = 0.1;
    private WsTimer timer = new WsTimer();

    // inputs
    private DigitalInput extendButton;
    private DigitalInput collectButton;

    // outputs
    private WsSolenoid extendSolenoid;
    private WsSolenoid lockSolenoid;

    // states
    private boolean extended;
    private boolean locked;
    private boolean collect;

    // enum/list of command options
    enum HatchCommand {
        // 0,      1,       2,        3
        IDLE, DEPLOY, COLLECT, COLLECT2;
    }
    private HatchCommand currentCommand;

    @Override
    public void inputUpdate(Input source) {
        if (source == extendButton) {
            if (currentCommand == HatchCommand.IDLE && extendButton.getValue()) {
                currentCommand = HatchCommand.DEPLOY;   
            }
        }
        
        if(source == collectButton) {
            if (collectButton.getValue() && !collect) {
                locked = !locked;
            } 
            collect = collectButton.getValue();
        }
    }

    @Override
    public void init() {
        // initialize buttons
        extendButton = (DigitalInput) Core.getInputManager().getInput(WSInputs.HATCH_DEPLOY.getName());
        extendButton.addInputListener(this);

        collectButton = (DigitalInput) Core.getInputManager().getInput(WSInputs.HATCH_COLLECT.getName());
        collectButton.addInputListener(this);

        // initialize solenoids
        extendSolenoid = (WsSolenoid) Core.getOutputManager().getOutput(WSOutputs.HATCH_OUT_SOLENOID.getName());
        lockSolenoid = (WsSolenoid) Core.getOutputManager().getOutput(WSOutputs.HATCH_LOCK_SOLENOID.getName());

        timer.start();
        resetState();
    }

    @Override
    public void selfTest() { }

    @Override
    public void update() {
        if (currentCommand == HatchCommand.DEPLOY) {
            if (!extended) {
                extended = true;
                extendSolenoid.setValue(extended);

                timer.reset();
            }
            else if (timer.hasPeriodPassed(DEPLOY_WAIT) && !timer.hasPeriodPassed(RETRACT_WAIT + DEPLOY_WAIT)) {
                locked = true;
                lockSolenoid.setValue(locked);
            }
            else if (timer.hasPeriodPassed(DEPLOY_WAIT + RETRACT_WAIT) && !timer.hasPeriodPassed(2*DEPLOY_WAIT + RETRACT_WAIT)) {
                extended = false;
                extendSolenoid.setValue(extended);
                currentCommand = HatchCommand.IDLE;
            }
        } 
        lockSolenoid.setValue(locked);
        
        // update SmartDashboard
        SmartDashboard.putBoolean("Hatch Out", extendSolenoid.getValue());
        SmartDashboard.putBoolean("Hatch Lock", lockSolenoid.getValue());
    }

    @Override
    public void resetState() {
        // reset local variables back to default state
        extended = false;
        locked = false;
        extendSolenoid.setValue(extended);
        lockSolenoid.setValue(locked);

        collect = false;

        currentCommand = HatchCommand.IDLE;
    }

    @Override
    public String getName() {
        return "Hatch";
    }

    // used by autonomous to deploy the hatch
    public boolean deployAuto() {
        locked = true;
        update();
        
        return currentCommand == HatchCommand.IDLE;
    }

    // used by autonomous to collect hatches
    public boolean collectAuto() {
        locked = false;
        update();

        return currentCommand == HatchCommand.IDLE;
    }
}