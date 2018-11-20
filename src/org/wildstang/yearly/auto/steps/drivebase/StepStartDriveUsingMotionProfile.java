package org.wildstang.yearly.auto.steps.drivebase;

import org.wildstang.framework.auto.steps.AutoStep;
import org.wildstang.framework.core.Core;
import org.wildstang.yearly.robot.WSSubsystems;
import org.wildstang.yearly.subsystems.DriveBase;

public class StepStartDriveUsingMotionProfile extends AutoStep
{

   

   private static final long MILLIS_TO_REVERSE = 200;

   private double distance;
   private double speed;
   private boolean hasReachedTarget = false;
   private long timeWhenTargetReached;
   private boolean shouldHardStop;

   private DriveBase driveBase;

   public StepStartDriveUsingMotionProfile(double distanceInInches, double speed)
   {
      this.distance = distanceInInches;
      this.speed = Math.abs(speed);
      
   }

   @Override
   public void initialize()
   {
      driveBase = ((DriveBase) Core.getSubsystemManager().getSubsystem(WSSubsystems.DRIVE_BASE.getName()));
      driveBase.resetLeftEncoder();
      driveBase.resetRightEncoder();
      //((WsMotionProfileControl)Core.getInputManager().getInput(WSInputs.MOTION_PROFILE_CONTROL.getName())).setProfileEnabled(true);

      driveBase.startStraightMoveWithMotionProfile(this.distance, this.speed);
      setFinished(true);
   }

   @Override
   public void update()
   {
     
   }

   @Override
   public String toString()
   {
      return "Start Motion Profile Drive";
   }

}