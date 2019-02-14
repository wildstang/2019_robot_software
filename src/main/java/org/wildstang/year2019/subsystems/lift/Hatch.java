package org.wildstang.year2019.subsystems.lift;

import org.wildstang.framework.io.Input;
import org.wildstang.framework.io.inputs.DigitalInput;
import org.wildstang.framework.subsystems.Subsystem;
import org.wildstang.framework.core.Core;
import org.wildstang.year2019.robot.WSInputs;
import org.wildstang.year2019.robot.WSOutputs;
import org.wildstang.hardware.crio.outputs.WsSolenoid;

import org.wildstang.framework.timer.WsTimer;

public class Hatch implements Subsystem {

    // Local inputs
    private DigitalInput hatchDeploy;
    private DigitalInput hatchCollect;
    private WsTimer timer = new WsTimer();

    // Local outputs
    private WsSolenoid hatchOut;
    private WsSolenoid hatchLock;

    // Logical variables
    private boolean outPosition;  // true  = Extended
                                  // false = Retracted
    private boolean lockPosition; // true  = Deployed (mechanism can push through hatch panel opening and extends so it cannot
                                  //                      fit back through opening)
                                  // false = Retracted (mechanism can freely move in any direction through hatch panel opening)
    private boolean working;
    private long deployRestartLastMovementTime;
    private long deployLastMovementTime;
    
    private long collectRestartLastMovementTime;
    private long collectLastMovementTime;

    // FIXME make this an enum for readability
    private int currentCommand; // 0 = Idle
                                // 1 = Deploy restart
                                // 2 = Collect restart
                                // 3 = Deploy
                                // 4 = Collect

    // Constants
    private final double solenoidDelay = 0.5; // TODO Measure delay during testing

    @Override
    public void inputUpdate(Input source) {
        if (source == hatchDeploy) {
            // if (currentCommand != 0 && hatchDeploy.getValue() == true) {
            //     currentCommand = 1;
            // }

            if (currentCommand == 0 && hatchDeploy.getValue() == true) {
                currentCommand = 3;
            }
        }
        
        if (source == hatchCollect) {
            // if (currentCommand != 0 && hatchDeploy.getValue() == true) {
            //     currentCommand = 2;
            // }

            if (currentCommand == 0 && hatchCollect.getValue() == true) {
                currentCommand = 4;
            }
        }
    }

    @Override
    public void init() {
        // Link digital inputs and outputs to physical controller and robot
        hatchDeploy = (DigitalInput) Core.getInputManager().getInput(WSInputs.HATCH_DEPLOY.getName());
        hatchDeploy.addInputListener(this);

        hatchCollect = (DigitalInput) Core.getInputManager().getInput(WSInputs.HATCH_COLLECT.getName());
        hatchCollect.addInputListener(this);

        hatchOut = (WsSolenoid) Core.getOutputManager().getOutput(WSOutputs.HATCH_OUT_SOLENOID.getName());
        hatchLock = (WsSolenoid) Core.getOutputManager().getOutput(WSOutputs.HATCH_LOCK_SOLENOID.getName());

        timer.start();
        resetState();
    }

    @Override
    public void selfTest() {
    }

    @Override
    public void update() {
        // Restart commands (1 and 2) take priority over regular ones (3 and 4)
        // if (currentCommand == 1) {
        //     if (deployRestartLastMovementTime == 0) {
        //         outPosition = false;
        //         hatchOut.setValue(outPosition);

        //         deployRestartLastMovementTime = System.currentTimeMillis();
        //     } else if (deployRestartLastMovementTime + solenoidDelayMillis >= System.currentTimeMillis()) {
        //         lockPosition = true;
        //         hatchLock.setValue(lockPosition);
        //     } else if (deployRestartLastMovementTime + 2 * solenoidDelayMillis >= System.currentTimeMillis()) {
        //         deployRestartLastMovementTime = 0;

        //         currentCommand = 2;
        //     }
        // } else if (currentCommand == 2) {
        //     if (collectRestartLastMovementTime == 0) {
        //         outPosition = false;
        //         hatchOut.setValue(outPosition);

        //         collectRestartLastMovementTime = System.currentTimeMillis();
        //     } else if (collectRestartLastMovementTime + solenoidDelayMillis >= System.currentTimeMillis()) {
        //         collectRestartLastMovementTime = 0;

        //         currentCommand = 4;
        //     }}
        if (currentCommand == 3) {
            if (!working) {
                working = true;
                outPosition = true;
                hatchOut.setValue(outPosition);

                timer.reset();
                deployLastMovementTime = System.currentTimeMillis();
            } else if (timer.hasPeriodPassed(solenoidDelay) && !timer.hasPeriodPassed(2*solenoidDelay)) {
                lockPosition = false;
                hatchLock.setValue(lockPosition);

            } else if (timer.hasPeriodPassed(2*solenoidDelay) && !timer.hasPeriodPassed(3*solenoidDelay)) {
                outPosition = false;
                hatchOut.setValue(outPosition);
            } else if (timer.hasPeriodPassed(3*solenoidDelay)) {
                lockPosition = true;
                hatchLock.setValue(lockPosition);

                working = false;
                deployLastMovementTime = 0;

                currentCommand = 0;
            }
        } else if (currentCommand == 4) {
            if (!working) {
                working = true;
                outPosition = true;
                hatchOut.setValue(outPosition);

                timer.reset();
                collectLastMovementTime = System.currentTimeMillis();
            } else if (timer.hasPeriodPassed(solenoidDelay)) {
                outPosition = false;
                hatchOut.setValue(outPosition);
                working = false;

                currentCommand = 0;
            }
        }
    }

    @Override
    public void resetState() {
        // Reset local variables back to default state
        outPosition = false;
        lockPosition = true;
        hatchOut.setValue(outPosition);
        hatchLock.setValue(lockPosition);

        working = false;

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