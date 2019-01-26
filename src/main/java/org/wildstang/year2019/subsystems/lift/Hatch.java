package org.wildstang.year2019.subsystems.lift;

import org.wildstang.framework.io.Input;
import org.wildstang.framework.io.inputs.DigitalInput;
import org.wildstang.framework.subsystems.Subsystem;
import org.wildstang.framework.core.Core;
import org.wildstang.year2019.robot.WSInputs;
import org.wildstang.year2019.robot.WSOutputs;
import org.wildstang.year2019.robot.Robot;
import org.wildstang.hardware.crio.outputs.WsSolenoid;

public class Hatch implements Subsystem{

    
    
    private DigitalInput hatchDeploy;
    private DigitalInput hatchCollect;

    private WsSolenoid hatchOut;
    private WsSolenoid hatchLock;

    private Boolean outPosition;
    private Boolean lockPosition;

    @Override
    public void inputUpdate(Input source) {
        
        if (source == hatchDeploy) {
            
        }

        if (source == hatchCollect) {
           
        }

    }

    @Override
    public void init() {
        //Links digital inputs and outputs to the physical controller and robot
        hatchDeploy = (DigitalInput) Core.getInputManager().getInput(WSInputs.HATCH_DEPLOY.getName());
        hatchDeploy.addInputListener(this);

        hatchCollect = (DigitalInput) Core.getInputManager().getInput(WSInputs.HATCH_COLLECT.getName());
        hatchCollect.addInputListener(this);

        hatchOut = (WsSolenoid) Core.getOutputManager().getOutput(WSOutputs.HATCH_OUT_SOLENOID.getName());
        hatchLock = (WsSolenoid) Core.getOutputManager().getOutput(WSOutputs.HATCH_LOCK_SOLENOID.getName());

        outPosition=false;
        lockPosition=true;

        resetState();
    }

    @Override
    public void selfTest() {
    }

    @Override
    public void update() {
        
    }

    @Override
    public void resetState() {
        //Reset local variables back to default state
        outPosition=false;
        lockPosition=true;
    }

    @Override
    public String getName() {
        return "Hatch";
    }





}