package org.wildstang.year2019.subsystems.climbwedge;

import org.wildstang.framework.io.Input;
import org.wildstang.framework.io.inputs.DigitalInput;
import org.wildstang.framework.subsystems.Subsystem;
import org.wildstang.framework.core.Core;
import org.wildstang.year2019.robot.WSInputs;
import org.wildstang.year2019.robot.WSOutputs;
import org.wildstang.hardware.crio.outputs.WsSolenoid;
import org.wildstang.framework.timer.WsTimer;

/**
 * Class:       ClimbWedge.java
 * Inputs:      2 buttons
 * Outputs:     1 single solenoid
 * Description: The climbing wedge subsystem is used to wedge the robot up to climb better.
 *              The wedge is made of a single solenoid, which is activated when both buttons are pressed.
 *              It is relaxed when a set amount of time has elapsed.
 */
public class ClimbWedge implements Subsystem {
    
    // inputs
    private DigitalInput wedgeButton1;
    private DigitalInput wedgeButton2;

    // outputs
    private WsSolenoid deployWedge;

    // states
    private boolean wedgeButton1Status;
    private boolean wedgeButton2Status;
    private boolean deployWedgeStatus;
    private boolean timerStatus;

    // time wait before relaxing
    private WsTimer timer = new WsTimer();
    private final double WEDGE_RELAX_DELAY = 0.5;

    @Override
    public void init() {
        // setup the buttons
        wedgeButton1 = (DigitalInput) Core.getInputManager().getInput(WSInputs.WEDGE_SAFETY_1.getName());
        wedgeButton1.addInputListener(this);

        wedgeButton2 = (DigitalInput) Core.getInputManager().getInput(WSInputs.WEDGE_SAFETY_2.getName());
        wedgeButton2.addInputListener(this);

        // setup the solenoid
        deployWedge = (WsSolenoid) Core.getOutputManager().getOutput(WSOutputs.WEDGE_SOLENOID.getName());

        resetState();
    }

    @Override
    public void selfTest() {
    }

    @Override
    public void update() {
        // check if both buttons assigned to wedge have been pressed down
        if (wedgeButton1Status && wedgeButton2Status) {
            deployWedgeStatus = true;
            if (timerStatus) {
                // relax the wedge if the timer has passed
                if (timer.hasPeriodPassed(WEDGE_RELAX_DELAY)) {
                    deployWedgeStatus = false;
                }
            }
            else {
                // start the timer if it is not
                timer.reset();
                timer.start();
                timerStatus = true;
            }
        }
        else {
            deployWedgeStatus = false;
            timer.stop();
            timerStatus = false;
        }
        
        deployWedge.setValue(deployWedgeStatus);
    }

    @Override
    public void inputUpdate(Input source) {
        if (source == wedgeButton1) {
            wedgeButton1Status = wedgeButton1.getValue();
        }

        if (source == wedgeButton2) {
            wedgeButton2Status = wedgeButton2.getValue();
        }
    }

    @Override
    public void resetState() {
        wedgeButton1Status = false;
        wedgeButton2Status = false;
        deployWedgeStatus = false;
        timerStatus = false;
    }

    @Override
    public String getName() {
        return "ClimbWedge";
    }
}