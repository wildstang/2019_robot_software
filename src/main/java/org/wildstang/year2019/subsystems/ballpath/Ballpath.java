package org.wildstang.year2019.subsystems.ballpath;

import org.wildstang.framework.io.Input;
import org.wildstang.framework.subsystems.Subsystem;

/** This subsystem is responsible for handling cargo from entry to exit in the robot.

This subsystem includes the intake, ball hopper and carriage machinery. 

Sensors:
<ul>
<li> Ball presence detector (beam break or something?) in carriage
</ul>

Actuators:
<ul>
<li> Intake roller motor
<li> Intake deploy piston solenoid
<li> Hopper belt drive motor
<li> Hopper belt position piston solenoid
<li> Carriage roller
</ul>

*/
public class Ballpath implements Subsystem {

    @Override
    public void inputUpdate(Input source) {
        // TODO
    }

    @Override
    public void init() {
        // TODO
    }

    @Override
    public void selfTest() {
        // TODO
    }

    @Override
    public void update() {
        // TODO
    }

    @Override
    public void resetState() {
        // TODO
    }

    @Override
    public String getName() {
        return "Ballpath";
    }
}