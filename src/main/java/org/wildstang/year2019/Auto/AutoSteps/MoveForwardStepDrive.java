package org.wildstang.year2019.Auto.AutoSteps;

import org.wildstang.framework.auto.steps.AutoStep;
import org.wildstang.framework.core.Core;
import org.wildstang.year2019.robot.WSSubsystems;

import org.wildstang.year2019.subsystems.drive.Drive;

public class MoveForwardStepDrive extends AutoStep
{
    // variables to be used
    private Drive drive;

    // private double rotations;
    // private static final double INCHES_PER_ROTATION = 4 * Math.PI;
    
    private double driveSpeed;
    private boolean driveStarted;
    private double distanceToDrive;

    public MoveForwardStepDrive (double distance, double speed)
    {
        // TODO: Do we need to use rotations?
        distanceToDrive = Math.abs(distance);
        driveSpeed = speed;
    }

    public void initialize()
    {
        //initialize variables and stuff
        drive = (Drive) Core.getSubsystemManager().getSubsystem(WSSubsystems.DRIVEBASE.getName());
        drive.resetEncoders();
    }

    public void update()
    {
        // TODO: Do we need call anything to SmartDashboard?
        // if so.. add it here
        if (!driveStarted)
        {
            drive.setAutonStraightDrive();
            driveStarted = true;
        }
        else
        {
            //setFinished(true); do we need this?
        }
    }   

    public String toString()
    {
        return "MoveForwardStepDrive";
    }
}