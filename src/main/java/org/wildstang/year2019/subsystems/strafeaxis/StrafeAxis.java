package org.wildstang.year2019.subsystems.strafeaxis;

import org.wildstang.framework.io.Input;
import org.wildstang.framework.subsystems.Subsystem;

/** This subsystem is responsible for lining up hatch panels left-to-right.
 * 
 * There should probably be a PID loop controlling the position of this axis.
 * 
 * Sensors: 
 * <ul>
 * <li> Line detection photocells (handled by LineDetector.java? or RasPi?)
 * <li> Limit switch(es). TODO: left, right or both?
 * <li> Encoder on lead screw Talon.
 * </ul>
 * 
 * Actuators:
 * <ul>
 * <li> Talon controlling lead screw motor.
 * </ul>
 * 
 */

public class StrafeAxis implements Subsystem {

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
        return "StrafeAxis";
    }
}