package org.wildstang.year2019.subsystems.lift;

import org.wildstang.framework.io.Input;
import org.wildstang.framework.io.inputs.DigitalInput;
import org.wildstang.framework.subsystems.Subsystem;
import org.wildstang.framework.core.Core;
import org.wildstang.year2019.robot.WSInputs;
import org.wildstang.year2019.robot.WSOutputs;
import org.wildstang.year2019.robot.Robot;
import org.wildstang.hardware.crio.outputs.WsSolenoid;

public class Hatch implements Subsystem {

    // Input variables
    private DigitalInput hatchDeploy;
    private DigitalInput hatchCollect;

    // Output variables
    private WsSolenoid hatchOut;
    private WsSolenoid hatchLock;

    // Logical variables
    private boolean outPosition;  // true  = Extended
                                  // false = Retracted
    private boolean lockPosition; // true  = Deployed (mechanism can push through hatch panel opening and extends so it cannot
                                  //                      fit back through opening)
                                  // false = Retracted (mechanism can freely move in any direction through hatch panel opening)

    private long deployRestartLastMovementTime;
    private long deployLastMovementTime;
    
    private long collectRestartLastMovementTime;
    private long collectLastMovementTime;

    private int currentCommand; // 0 = Idle
                                // 1 = Deploy restart
                                // 2 = Collect restart
                                // 3 = Deploy
                                // 4 = Collect

    @Override
    public void inputUpdate(Input source) {
        if (source == hatchDeploy) {
            if (currentCommand != 0 && hatchDeploy.getValue() == true) {
                currentCommand = 1;
            }

            if (currentCommand == 0 && hatchDeploy.getValue() == true) {
                currentCommand = 3;
            }
        }
        
        if (source == hatchCollect) {
            if (currentCommand != 0 && hatchDeploy.getValue() == true) {
                currentCommand = 2;
            }

            if (currentCommand == 0 && hatchDeploy.getValue() == true) {
                currentCommand = 4;
            }
        }
    }

    @Override
    public void init() {
        // Links digital inputs and outputs to the physical controller and robot
        hatchDeploy = (DigitalInput) Core.getInputManager().getInput(WSInputs.HATCH_DEPLOY.getName());
        hatchDeploy.addInputListener(this);

        hatchCollect = (DigitalInput) Core.getInputManager().getInput(WSInputs.HATCH_COLLECT.getName());
        hatchCollect.addInputListener(this);

        hatchOut = (WsSolenoid) Core.getOutputManager().getOutput(WSOutputs.HATCH_OUT_SOLENOID.getName());
        hatchLock = (WsSolenoid) Core.getOutputManager().getOutput(WSOutputs.HATCH_LOCK_SOLENOID.getName());

        resetState();
    }

    @Override
    public void selfTest() {
    }

    @Override
    public void update() {
        if (currentCommand == 1) {
            if (deployRestartLastMovementTime == 0) {
                outPosition = false;
                hatchOut.setValue(outPosition);

                deployRestartLastMovementTime = System.currentTimeMillis();
            } else if (deployRestartLastMovementTime + 500 >= System.currentTimeMillis()) {
                lockPosition = true;
                hatchLock.setValue(lockPosition);
            } else if (deployRestartLastMovementTime + 1000 >= System.currentTimeMillis()) {
                currentCommand = 2;

                deployRestartLastMovementTime = 0;
            }
        } else if (currentCommand == 2) {
            if (collectRestartLastMovementTime == 0) {
                outPosition = false;
                hatchOut.setValue(outPosition);

                collectRestartLastMovementTime = System.currentTimeMillis();
            } else if (collectRestartLastMovementTime + 500 >= System.currentTimeMillis()) {
                deployRestart = false;
                collectRestart = true;
                currentCommand = 4;

                collectRestartLastMovementTime = 0;
            }
        } else if (currentCommand == 3) {
            if (deployLastMovementTime == 0) {
                outPosition = true;
                hatchOut.setValue(outPosition);

                deployLastMovementTime = System.currentTimeMillis();
            } else if (deployLastMovementTime + 500 >= System.currentTimeMillis()) {
                lockPosition = false;
                hatchLock.setValue(lockPosition);
            } else if (deployLastMovementTime + 1000 >= System.currentTimeMillis()) {
                outPosition = false;
                hatchOut.setValue(outPosition);
            } else if (deployLastMovementTime + 1500 >= System.currentTimeMillis()) {
                lockPosition = true;
                hatchLock.setValue(lockPosition);

                deployLastMovementTime = 0;

                currentCommand = 0;
            }
        } else if (currentCommand == 4) {
            if (collectLastMovementTime == 0) {
                outPosition = true;
                hatchOut.setValue(outPosition);

                collectLastMovementTime = System.currentTimeMillis();
            } else if (collectLastMovementTime + 500 >= System.currentTimeMillis()) {
                outPosition = false;
                hatchOut.setValue(outPosition);

                collectLastMovementTime = 0;

                currentCommand = 0;
            }
        }
    }

    @Override
    public void resetState() {
        // Reset local variables back to default state
        deployRestart = false;
        collectRestart = false;

        outPosition = false;
        lockPosition = true;

        deployRestartLastMovementTime = 0;
        deployLastMovementTime = 0;

        collectRestartLastMovementTime = 0;
        collectLastMovementTime = 0;

        currentCommand = 0;
    }

    @Override
    public String getName() {
        return "Hatch";
    }
}