package org.wildstang.yearly.auto.programs;

import org.wildstang.framework.auto.AutoProgram;
import org.wildstang.framework.auto.steps.control.AutoStepDelay;
import org.wildstang.yearly.auto.steps.drivebase.StepDriveDistanceAtSpeed;

public class DriveAtp5 extends AutoProgram
{

   @Override
   protected void defineSteps()
   {
      addStep(new StepDriveDistanceAtSpeed(120, .5, true));
      addStep(new AutoStepDelay(1000));
      addStep(new StepDriveDistanceAtSpeed(120, -.5, true));
   }

   @Override
   public String toString()
   {
      return "Test Drive at .5 speed";
   }

}
