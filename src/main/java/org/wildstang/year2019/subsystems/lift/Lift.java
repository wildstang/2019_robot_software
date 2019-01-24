package org.wildstang.year2019.subsystems.lift;

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
        return "Lift";
    }
}