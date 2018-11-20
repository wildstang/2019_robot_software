package org.wildstang.yearly.auto.steps.shooter;

import org.wildstang.framework.auto.steps.AutoStep;
import org.wildstang.framework.core.Core;
import org.wildstang.yearly.robot.WSSubsystems;
import org.wildstang.yearly.subsystems.Intake;

public class StepResetShotToggle extends AutoStep
{
   public StepResetShotToggle()
   {

   }

   @Override
   public void initialize()
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void update()
   {
      // TODO Auto-generated method stub
//      ((DigitalInput)Core.getInputManager().getInput(WSInputs.MAN_BUTTON_8.getName())).setValue(false);
      ((Intake)Core.getSubsystemManager().getSubsystem(WSSubsystems.INTAKE.getName())).shotOverride(false);
      setFinished(true);
   }

   @Override
   public String toString()
   {
      // TODO Auto-generated method stub
      return "Shot reset";
   }

}
