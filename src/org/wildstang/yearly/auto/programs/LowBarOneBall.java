package org.wildstang.yearly.auto.programs;


import org.wildstang.framework.auto.AutoProgram;
import org.wildstang.framework.auto.steps.AutoParallelStepGroup;
import org.wildstang.framework.auto.steps.control.AutoStepDelay;
import org.wildstang.yearly.auto.steps.drivebase.StepQuickTurn;
import org.wildstang.yearly.auto.steps.drivebase.StepResetEncoders;
import org.wildstang.yearly.auto.steps.drivebase.StepSetShifter;
import org.wildstang.yearly.auto.steps.drivebase.StepStartDriveUsingMotionProfile;
import org.wildstang.yearly.auto.steps.drivebase.StepStopDriveUsingMotionProfile;
import org.wildstang.yearly.auto.steps.drivebase.StepVisionAdjustment;
import org.wildstang.yearly.auto.steps.drivebase.StepWaitForDriveMotionProfile;
import org.wildstang.yearly.auto.steps.intake.StepResetIntakeToggle;
import org.wildstang.yearly.auto.steps.intake.StepSetIntakeState;
import org.wildstang.yearly.auto.steps.shooter.StepResetShooterPositionToggle;
import org.wildstang.yearly.auto.steps.shooter.StepResetShotToggle;
import org.wildstang.yearly.auto.steps.shooter.StepRunFlywheel;
import org.wildstang.yearly.auto.steps.shooter.StepSetShooterPosition;
import org.wildstang.yearly.auto.steps.shooter.StepShoot;
import org.wildstang.yearly.subsystems.Shooter;

public class LowBarOneBall extends AutoProgram
{

   @Override
   protected void defineSteps()
   {
      AutoParallelStepGroup findGoal = new AutoParallelStepGroup();
      // TODO Auto-generated method stub
      addStep(new StepSetShifter(false));
      addStep(new StepSetShooterPosition(false));
      addStep(new StepSetIntakeState(true));
      addStep(new StepStartDriveUsingMotionProfile(160, 0.0));
      addStep(new StepWaitForDriveMotionProfile());
      addStep(new StepStopDriveUsingMotionProfile());
      addStep(new StepResetShooterPositionToggle());
      addStep(new StepSetShooterPosition(true));
      addStep(new StepResetShooterPositionToggle());
      addStep(new StepResetIntakeToggle());
      addStep(new StepSetIntakeState(false));
      addStep(new StepResetIntakeToggle());
      addStep(new AutoStepDelay(1500));
      addStep(new StepQuickTurn(55));
      addStep(new StepResetEncoders());
      addStep(new StepStartDriveUsingMotionProfile(40, 0.0));
      addStep(new StepWaitForDriveMotionProfile());
      addStep(new StepStopDriveUsingMotionProfile());
//      addStep(new StepTurnForTime(-.6, 1500));
      addStep(new AutoStepDelay(1000));
//      addStep(new StepRunFlywheel(Shooter.FLYWHEEL_SPEED_MEDIUM));
//      addStep(new AutoStepDelay(2000));
//      findGoal.addStep(new AutoStepDelay(2000));
      addStep(new StepVisionAdjustment());
      addStep(new StepRunFlywheel(Shooter.FLYWHEEL_SPEED_LOW));
      addStep(new AutoStepDelay(4000));
//      addStep(findGoal);
      addStep(new StepShoot());
      addStep(new AutoStepDelay(3000));
      addStep(new StepResetShotToggle());
      addStep(new StepRunFlywheel(Shooter.FLYWHEEL_SPEED_ZERO));
   }

   @Override
   public String toString()
   {
      // TODO Auto-generated method stub
      return "Low Bar 1 Ball";
   }

}
