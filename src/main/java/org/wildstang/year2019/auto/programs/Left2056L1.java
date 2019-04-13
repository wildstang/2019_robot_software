package org.wildstang.year2019.auto.programs;

import org.wildstang.year2019.auto.steps.PathFollowerStep;
import org.wildstang.year2019.auto.steps.CollectHatch;
import org.wildstang.year2019.auto.steps.DeployHatch;
import org.wildstang.year2019.auto.steps.DelayStep;
import org.wildstang.year2019.auto.steps.MotionMagicStraightLine;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import org.wildstang.framework.auto.AutoProgram;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Left2056L1 extends AutoProgram {

    @Override
    protected void defineSteps() {


        addStep(new MotionMagicStraightLine(50));

        addStep(new DeployHatch());

        addStep(new PathFollowerStep(PathNameConstants.the2056B,true));

        addStep(new PathFollowerStep(PathNameConstants.the2056C, false));
        
        addStep(new CollectHatch());

        addStep(new PathFollowerStep(PathNameConstants.the2056D, false));

        addStep(new PathFollowerStep(PathNameConstants.the2056E, true));

        addStep(new DeployHatch());
        
        addStep(new PathFollowerStep(PathNameConstants.the2056F, false));

    }

    @Override
    public String toString() {
        //give it a name
        return "RocketLeft";
    }

}