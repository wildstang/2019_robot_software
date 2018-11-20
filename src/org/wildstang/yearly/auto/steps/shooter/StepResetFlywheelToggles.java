package org.wildstang.yearly.auto.steps.shooter;

import org.wildstang.framework.auto.steps.AutoStep;
import org.wildstang.framework.core.Core;
import org.wildstang.framework.io.inputs.DigitalInput;
import org.wildstang.yearly.robot.WSInputs;

public class StepResetFlywheelToggles extends AutoStep
{

   public StepResetFlywheelToggles()
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
         ((DigitalInput)Core.getInputManager().getInput(WSInputs.MAN_BUTTON_3.getName())).setValue(false);
         ((DigitalInput)Core.getInputManager().getInput(WSInputs.MAN_BUTTON_4.getName())).setValue(false);
         setFinished(true);
   }

   @Override
   public String toString()
   {
      // TODO Auto-generated method stub
      return "Reset flywheel toggles";
   }

}
