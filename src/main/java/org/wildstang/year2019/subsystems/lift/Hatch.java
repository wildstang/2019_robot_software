package org.wildstang.year2019.subsystems.lift;

import org.wildstang.framework.io.Input;
import org.wildstang.framework.io.inputs.DigitalInput;
import org.wildstang.framework.subsystems.Subsystem;
import org.wildstang.framework.core.Core;
import org.wildstang.year2019.robot.WSInputs;
import org.wildstang.year2019.robot.WSOutputs;
import org.wildstang.hardware.crio.outputs.WsSolenoid;
import org.wildstang.hardware.crio.outputs.WsDoubleSolenoid;
import org.wildstang.hardware.crio.outputs.WsDoubleSolenoidState;

import org.wildstang.framework.timer.WsTimer;

public class Hatch implements Subsystem {

    //Timer constants TODO: Measure time during testing
    private static final double DEPLOY_WAIT = 0.25;
    private static final double LOCK_WAIT = 0.15;
    // Local inputs
    private DigitalInput hatchDeploy;
    private DigitalInput hatchCollect;
    private WsTimer timer = new WsTimer();

    // Local outputs
    private WsDoubleSolenoid hatchOut;
    private WsSolenoid hatchLock;

    // Logical variables
    private boolean outPosition;  // true  = Extended
                                  // false = Retracted
    private boolean lockPosition; // true  = Deployed (mechanism can push through hatch panel opening and extends so it cannot
                                  //                      fit back through opening)
                                  // false = Retracted (mechanism can freely move in any direction through hatch panel opening)
    private boolean working;

    //No Longer using
    // private long deployRestartLastMovementTime;
    // private long deployLastMovementTime;
    
    // private long collectRestartLastMovementTime;
    // private long collectLastMovementTime;


    enum commands {
        IDLE, DEPLOY_RESTART, COLLECT_RESTART, DEPLOY, COLLECT;
    }
    private int currentCommand; // 0 = Idle
                                // 1 = Deploy restart
                                // 2 = Collect restart
                                // 3 = Deploy
                                // 4 = Collect

    @Override
    public void inputUpdate(Input source) {
        if (source == hatchDeploy) {
            // if (currentCommand != 0 && hatchDeploy.getValue() == true) {
            //     currentCommand = 1;
            // }

            if (currentCommand == commands.IDLE.ordinal()  && hatchDeploy.getValue() == true) {
                currentCommand = commands.DEPLOY.ordinal();
                
            }
        }
        
        if (source == hatchCollect) {
            // if (currentCommand != 0 && hatchDeploy.getValue() == true) {
            //     currentCommand = 2;
            // }

            if (currentCommand == commands.IDLE.ordinal() && hatchCollect.getValue() == true) {
                currentCommand = commands.COLLECT.ordinal();
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

        hatchOut = (WsDoubleSolenoid) Core.getOutputManager().getOutput(WSOutputs.HATCH_OUT_SOLENOID.getName());
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
        if (currentCommand == commands.DEPLOY.ordinal()) {
            if (!working) {
                working = true;
                outPosition = true;
                if (outPosition) hatchOut.setValue(WsDoubleSolenoidState.FORWARD.ordinal());
                else hatchOut.setValue(WsDoubleSolenoidState.REVERSE.ordinal());

                timer.reset();
                //deployLastMovementTime = System.currentTimeMillis();
            } else if (timer.hasPeriodPassed(DEPLOY_WAIT) && !timer.hasPeriodPassed(2*DEPLOY_WAIT)) {
                lockPosition = false;
                hatchLock.setValue(lockPosition);

            } else if (timer.hasPeriodPassed(2*LOCK_WAIT) && !timer.hasPeriodPassed(3*LOCK_WAIT)) {
                outPosition = false;
                if (outPosition) hatchOut.setValue(WsDoubleSolenoidState.REVERSE.ordinal());
                else hatchOut.setValue(WsDoubleSolenoidState.FORWARD.ordinal());
            } else if (timer.hasPeriodPassed(3*DEPLOY_WAIT)) {
                lockPosition = true;
                hatchLock.setValue(lockPosition);

                working = false;
                //deployLastMovementTime = 0;

                currentCommand = commands.IDLE.ordinal();
            }
        } else if (currentCommand == commands.COLLECT.ordinal()) {
            if (!working) {
                working = true;
                outPosition = true;
                if (outPosition) hatchOut.setValue(WsDoubleSolenoidState.FORWARD.ordinal());
                else hatchOut.setValue(WsDoubleSolenoidState.REVERSE.ordinal());

                timer.reset();
                //collectLastMovementTime = System.currentTimeMillis();
            } else if (timer.hasPeriodPassed(DEPLOY_WAIT)) {
                outPosition = false;
                if (outPosition) hatchOut.setValue(WsDoubleSolenoidState.FORWARD.ordinal());
                else hatchOut.setValue(WsDoubleSolenoidState.REVERSE.ordinal());
                working = false;

                currentCommand = commands.IDLE.ordinal();
            }
        }
    }

    @Override
    public void resetState() {
        // Reset local variables back to default state
        outPosition = false;
        lockPosition = true;
        if (outPosition) hatchOut.setValue(WsDoubleSolenoidState.FORWARD.ordinal());
        else hatchOut.setValue(WsDoubleSolenoidState.REVERSE.ordinal());
        hatchLock.setValue(lockPosition);

        working = false;

        // deployRestartLastMovementTime = 0;
        // deployLastMovementTime = 0;

        // collectRestartLastMovementTime = 0;
        // collectLastMovementTime = 0;

        currentCommand = commands.IDLE.ordinal();
    }

    @Override
    public String getName() {
        return "Hatch";
    }
}