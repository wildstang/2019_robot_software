package org.wildstang.year2019.Auto.AutoSteps;

import org.wildstang.framework.auto.steps.AutoStep;
import org.wildstang.framework.core.Core;
import org.wildstang.year2019.robot.WSSubsystems;

import org.wildstang.year2019.subsystems.drive.Drive;


//this example is for Drive, you can also modify it to use ballpath, climbwedge, hatch, lift, strafeaxis

public class ExampleAutoStepDrive extends AutoStep{

    private Drive drive;

    private double rotations;
    private boolean hasStarted=false;
    private static final double INCHES_PER_ROTATION = 4*Math.PI;
    private static final double TOLERANCE=1/INCHES_PER_ROTATION;

    public ExampleAutoStepDrive(double inches){
        rotations = inches*INCHES_PER_ROTATION;
    }

    public void update(){
        //call what you want the subsystem to do during this step
        // control the drive with drive. whatever you want
        if(!hasStarted){
            drive.setAutonStraightDrive();
            hasStarted=true;
        }else{
            if (Math.abs((Math.abs(rotations)-(Math.abs(drive.getRightSensorValue()/4096))))<=TOLERANCE){
                drive.setBrakeMode(true);
                setFinished(true);
            }
        }
        

    }
    public String toString(){
        //put a reasonable name for this step inside the string
        return "";
    }
    public void initialize(){

        drive = (Drive) Core.getSubsystemManager().getSubsystem(WSSubsystems.DRIVEBASE.getName());
        drive.resetEncoders();
    }


}