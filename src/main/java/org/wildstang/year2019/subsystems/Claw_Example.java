package org.wildstang.year2019.subsystems;

import org.wildstang.year2019.robot.WSInputs;
import org.wildstang.year2019.robot.WSOutputs;
import org.wildstang.framework.core.Core;
import org.wildstang.framework.io.Input;
import org.wildstang.framework.io.inputs.DigitalInput;
import org.wildstang.framework.subsystems.Subsystem;
import org.wildstang.hardware.crio.outputs.WsSolenoid;

/**
 * Class:       Claw_Example.java
 * Inputs:      2 toggle buttons
 * Outputs:     2 single solenoids
 * Description: This is an example subsystem that represents a generic claw.
 *              The claw is comprised of 2 solenoids which are opened with one button and closed with another.
 */
public class Claw_Example implements Subsystem {

    // inputs
    private DigitalInput openButton;
    private DigitalInput closeButton;

    // outputs
    private WsSolenoid solenoidA;
    private WsSolenoid solenoidB;

    // states
    private boolean isClawOpen;

    // initializes the subsystem
    public void init() {
        // register buttons with arbitrary button names, since this is a test
        openButton = (DigitalInput) Core.getInputManager().getInput(WSInputs.ANTITURBO.getName());
        openButton.addInputListener(this);
        closeButton = (DigitalInput) Core.getInputManager().getInput(WSInputs.SHIFT.getName());
        closeButton.addInputListener(this);

        // register solenoids with arbitrary output names, since this is a test
        solenoidA = (WsSolenoid) Core.getOutputManager().getOutput(WSOutputs.WEDGE_SOLENOID.getName());
        solenoidB = (WsSolenoid) Core.getOutputManager().getOutput(WSOutputs.HOPPER_SOLENOID.getName());

        resetState();
    }

    // update the subsystem everytime the framework updates (every ~0.02 seconds)
    public void update() {
        solenoidA.setValue(isClawOpen);
        solenoidB.setValue(isClawOpen);
    }

    // respond to input updates
    public void inputUpdate(Input signal) {
        // check to see which input was updated
        if (signal == openButton) {
            // open the claw if the open button is pressed
            if (openButton.getValue()) {
                isClawOpen = true;
            }
        }

        if (signal == closeButton) {
            // close the claw if the close button is pressed
            if (closeButton.getValue()) {
                isClawOpen = false;
            }
        }
    }

    // used for testing
    public void selfTest() {}

    // resets all variables to the default state
    public void resetState() {
        isClawOpen = false;
    }

    // returns the unique name of the example
    public String getName() {
        return "Claw_Example";
    }
}