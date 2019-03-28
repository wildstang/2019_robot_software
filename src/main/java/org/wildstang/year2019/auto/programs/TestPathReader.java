package org.wildstang.year2019.auto.programs;

import org.wildstang.year2019.auto.steps.PathFollowerStep;
import org.wildstang.year2019.auto.steps.CollectHatch;
import org.wildstang.year2019.auto.steps.DeployHatch;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import org.wildstang.framework.auto.AutoProgram;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class TestPathReader extends AutoProgram {

    @Override
    protected void defineSteps() {

        SmartDashboard.putBoolean("Checkpoint 3003 yay", true);

        addStep(new CollectHatch());
        addStep(new DeployHatch());
        //addStep(new PathFollowerStep(PathNameConstants.TEST_ARC, true));
        //addStep(new PathFollowerStep(PathNameConstants.TEST_ARC, false));
    }

    @Override
    public String toString() {
        //give it a name
        return "TestPathReader";
    }

}