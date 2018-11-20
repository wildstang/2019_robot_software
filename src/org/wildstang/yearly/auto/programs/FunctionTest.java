package org.wildstang.yearly.auto.programs;

import org.wildstang.framework.auto.AutoProgram;
import org.wildstang.framework.auto.steps.control.AutoStepDelay;
import org.wildstang.yearly.auto.steps.drivebase.StepStartDriveUsingMotionProfile;
import org.wildstang.yearly.auto.steps.drivebase.StepStopDriveUsingMotionProfile;
import org.wildstang.yearly.auto.steps.drivebase.StepWaitForDriveMotionProfile;
import org.wildstang.yearly.auto.steps.intake.StepIntake;
import org.wildstang.yearly.auto.steps.shooter.StepResetShooterPositionToggle;
import org.wildstang.yearly.auto.steps.shooter.StepSetShooterPosition;

public class FunctionTest extends AutoProgram
{

   @Override
   protected void defineSteps()
   {
      // TODO Auto-generated method stub
//      addStep(new StepSetIntakeState(false));
//      addStep(new AutoStepDelay(1000));
//      addStep(new StepIntake(0));
      
      addStep(new StepIntake(1));
      addStep(new AutoStepDelay(1000));
      addStep(new StepIntake(0));
      
      addStep(new StepSetShooterPosition(true));
      addStep(new StepResetShooterPositionToggle());
      addStep(new AutoStepDelay(1000));
      addStep(new StepSetShooterPosition(false));
      addStep(new StepResetShooterPositionToggle());
      
      addStep(new StepStartDriveUsingMotionProfile(25, 0));
      addStep(new StepWaitForDriveMotionProfile()); 
      addStep(new StepStopDriveUsingMotionProfile());
   }

   @Override
   public String toString()
   {
      // TODO Auto-generated method stub
      return "FunctionTest";
   }

}
