package org.wildstang.yearly.auto.steps.shooter;

import org.wildstang.framework.auto.steps.AutoStep;
import org.wildstang.framework.core.Core;
import org.wildstang.yearly.robot.WSSubsystems;
import org.wildstang.yearly.subsystems.Intake;

public class StepShoot extends AutoStep
{
   public StepShoot()
   {

   }

   @Override
   public void initialize()
   {
      // TODO Auto-generated method stub
      ((Intake)Core.getSubsystemManager().getSubsystem(WSSubsystems.INTAKE.getName())).setShotOverride(true);
   }

   @Override
   public void update()
   {
      // TODO Auto-generated method stub
//      if(((Shooter)Core.getSubsystemManager().getSubsystem(WSSubsystems.SHOOTER.getName())).doesSpeedMatch())
//      {
//      ((DigitalInput)Core.getInputManager().getInput(WSInputs.MAN_BUTTON_8.getName())).setValue(true);
//      }
      ((Intake)Core.getSubsystemManager().getSubsystem(WSSubsystems.INTAKE.getName())).shotOverride(true);
      setFinished(true);
   }

   @Override
   public String toString()
   {
      // TODO Auto-generated method stub
      return "Shot";
   }

}
