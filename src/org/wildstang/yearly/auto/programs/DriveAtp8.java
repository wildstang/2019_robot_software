package org.wildstang.yearly.auto.programs;

import org.wildstang.framework.auto.AutoProgram;
import org.wildstang.framework.auto.steps.control.AutoStepDelay;
import org.wildstang.yearly.auto.steps.drivebase.StepDriveDistanceAtSpeed;

public class DriveAtp8 extends AutoProgram
{

   @Override
   protected void defineSteps()
   {
      addStep(new StepDriveDistanceAtSpeed(120, .8, true));
      addStep(new AutoStepDelay(1000));
      addStep(new StepDriveDistanceAtSpeed(120, -.8, true));
   }

   @Override
   public String toString()
   {
      return "Test Drive at .8 speed";
   }

}
