package org.wildstang.yearly.auto.programs;

import org.wildstang.framework.auto.AutoProgram;
import org.wildstang.yearly.auto.steps.drivebase.StepStartDriveUsingMotionProfile;
import org.wildstang.yearly.auto.steps.drivebase.StepStopDriveUsingMotionProfile;
import org.wildstang.yearly.auto.steps.drivebase.StepWaitForDriveMotionProfile;

public class MotionProfileTest extends AutoProgram
{
   private double speed;
   private double distance;
   private double heading;

   @Override
   protected void defineSteps()
   {
      distance = 20;
      heading = 0;
      addStep(new StepStartDriveUsingMotionProfile(distance, heading));
      addStep(new StepWaitForDriveMotionProfile()); 
      addStep(new StepStopDriveUsingMotionProfile());
   }

   @Override
   public String toString()
   {
      // TODO Auto-generated method stub
      return "Test Motion Profile";
   }

}