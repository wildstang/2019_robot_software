package org.wildstang.year2019.auto.steps;

import org.wildstang.framework.auto.steps.AutoStep;
import org.wildstang.framework.core.Core;
import org.wildstang.year2019.robot.WSSubsystems;
import org.wildstang.year2019.subsystems.drive.InclassDrive;

/**
 * Class:       InclassAutoProgram.java
 * Description: This is the template class for an in-class project on Nov 17 2019, a follow up to InclassDrive.java.
 *              Please see InclassAutoProgram.java for a complete description of this week's assigment.
 */
public class InclassAutoStep extends AutoStep {

    private InclassDrive driveSys;

    public InclassAutoStep() {
        // constructor, receive any step configuration parameters here
    }

    @Override
    public void initialize() {
        driveSys = (InclassDrive) Core.getSubsystemManager().getSubsystem(WSSubsystems.DRIVEBASE.getName());

        // initial setup for drive subsystem
    }

    @Override
    public void update() {
        // change the state of the step and the drive subsystem
    }

    @Override
    public String toString() {
        return "In-Class Drive Auto Step";
    }

}