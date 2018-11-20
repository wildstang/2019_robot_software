package org.wildstang.yearly.auto.steps.drivebase;

import org.wildstang.framework.auto.steps.AutoStep;
import org.wildstang.framework.core.Core;
import org.wildstang.yearly.robot.WSSubsystems;
import org.wildstang.yearly.subsystems.DriveBase;

public class StepWaitForDriveMotionProfile extends AutoStep
{

   private DriveBase driveBase;

   public StepWaitForDriveMotionProfile()
   {
      
   }

   @Override
   public void initialize()
   {
      driveBase = ((DriveBase) Core.getSubsystemManager().getSubsystem(WSSubsystems.DRIVE_BASE.getName()));
   }

   @Override
   public void update()
   {
      double distanceRemaining = driveBase.getDistanceRemaining();
      double velocity = driveBase.getVelocity();
      if ((distanceRemaining < 0.01) &&
          (distanceRemaining > -0.01))  {
          setFinished(true);
      } 
      if ((distanceRemaining < 12.0) &&
          (distanceRemaining > -12.0) &&
              (velocity < 0.10) &&
              (velocity > -0.10))  {
          setFinished(true); 
      }
   }

   @Override
   public String toString()
   {
      return "Stop Motion Profile Drive";
   }

}