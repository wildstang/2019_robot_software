package org.wildstang.year2019.auto.programs;

import org.wildstang.year2019.auto.steps.PathFollowerStep;
import org.wildstang.year2019.auto.steps.CollectHatch;
import org.wildstang.year2019.auto.steps.DeployHatch;
import org.wildstang.year2019.auto.steps.DelayStep;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import org.wildstang.framework.auto.AutoProgram;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class RocketLeft extends AutoProgram {

    @Override
    protected void defineSteps() {

        SmartDashboard.putBoolean("Checkpoint 3003 yay", true);

        addStep(new PathFollowerStep(PathNameConstants.HAB_2_START, true));
        addStep(new PathFollowerStep(PathNameConstants.HAB_ROCKET_CLOSE_LEFT, true));

        addStep(new DeployHatch());

        addStep(new PathFollowerStep(PathNameConstants.ROCKET_CLOSE_LEFT_INTERIM_HP, false));

        addStep(new PathFollowerStep(PathNameConstants.INTERIM_ROCKET_CLOSE_LEFT_HP, true));
        
        addStep(new CollectHatch());

        addStep(new PathFollowerStep(PathNameConstants.HP_INTERIM_BACK_ROCKET_LEFT, false));

        addStep(new PathFollowerStep(PathNameConstants.INTERIM_HP_BACK_ROCKET_LEFT, true));

        addStep(new DeployHatch());

        //addStep(new PathFollowerStep(PathNameConstants.INTERIM_HP_BACK_ROCKET_LEFT,false));
    }

    @Override
    public String toString() {
        //give it a name
        return "RocketLeft";
    }

}