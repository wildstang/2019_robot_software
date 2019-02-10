package org.wildstang.year2019.auto.steps;

import org.wildstang.framework.auto.steps.AutoStep;
import org.wildstang.framework.core.Core;
import org.wildstang.year2019.robot.WSSubsystems;

import org.wildstang.year2019.subsystems.drive.Drive;

//this example is for Drive, you can also modify it to use ballpath, climbwedge, hatch, lift, strafeaxis

public class ExampleAutoStepDrive extends AutoStep {

    private Drive drive;

    private double rotations;
    private boolean hasStarted = false;
    private double TOLERANCE = 0.1; // FIXME units?

    public ExampleAutoStepDrive(double inches) {
        // Drive subsystem should be responsible for converting inches to rotations.
    }

    public void update() {
        // call what you want the subsystem to do during this step
        // control the drive with drive. whatever you want
        if (!hasStarted) {
            drive.setAutonStraightDrive();
            hasStarted = true;
        } else {
            if (Math.abs((Math.abs(rotations) - (Math.abs(drive.getRightSensorValue() / 4096)))) <= TOLERANCE) {
                drive.setBrakeMode(true);
                setFinished(true);
            }
        }

    }

    public String toString() {
        // put a reasonable name for this step inside the string
        return "";
    }

    public void initialize() {

        drive = (Drive) Core.getSubsystemManager().getSubsystem(WSSubsystems.DRIVEBASE.getName());

        // TODO: is this code the only code using the encoders? If we reset encoders
        // here, will anything else break?
        drive.resetEncoders();
    }

}