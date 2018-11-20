package org.wildstang.yearly.auto.steps.drivebase;

import org.wildstang.framework.auto.steps.AutoStep;
import org.wildstang.framework.core.Core;
import org.wildstang.yearly.robot.WSSubsystems;
import org.wildstang.yearly.subsystems.DriveBase;

public class StepStopDriveUsingMotionProfile extends AutoStep
{

   private DriveBase driveBase;

   public StepStopDriveUsingMotionProfile()
   {
      
   }

   @Override
   public void initialize()
   {
      driveBase = ((DriveBase) Core.getSubsystemManager().getSubsystem(WSSubsystems.DRIVE_BASE.getName()));
      
      driveBase.stopStraightMoveWithMotionProfile();
      setFinished(true);
   }

   @Override
   public void update()
   {
     
   }

   @Override
   public String toString()
   {
      return "Stop Motion Profile Drive";
   }

}