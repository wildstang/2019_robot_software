package org.wildstang.year2019.auto.programs;

import org.wildstang.year2019.auto.steps.PathFollowerStep;
import org.wildstang.year2019.auto.steps.CollectHatch;
import org.wildstang.year2019.auto.steps.DeployHatch;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import org.wildstang.framework.auto.AutoProgram;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class CargoShipLeft extends AutoProgram {

    @Override
    protected void defineSteps() {

        SmartDashboard.putBoolean("Checkpoint 3003 yay", true);

        addStep(new PathFollowerStep(PathNameConstants.HAB_CARGO_FAR_LEFT, true));

        addStep(new DeployHatch());

        addStep(new PathFollowerStep(PathNameConstants.CARGO_FAR_LEFT_INTERIM_HP, false));

        addStep(new PathFollowerStep(PathNameConstants.INTERIM_CARGO_FAR_LEFT_HP, true));
        
        addStep(new CollectHatch());

        addStep(new PathFollowerStep(PathNameConstants.HP_INTERIM_CARGO_CLOSE_LEFT, false));

        addStep(new PathFollowerStep(PathNameConstants.INTERIM_CARGO_CLOSE_LEFT, true));

        addStep(new DeployHatch());
    }

    @Override
    public String toString() {
        //give it a name
        return "CargoShipLeft";
    }

}