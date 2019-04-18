package org.wildstang.year2019.auto.programs.Left2056steps;

import org.wildstang.year2019.auto.steps.PathFollowerStep;
import org.wildstang.year2019.auto.steps.CollectHatch;
import org.wildstang.year2019.auto.steps.DeployHatch;
import org.wildstang.year2019.auto.steps.DelayStep;
import org.wildstang.year2019.auto.steps.MotionMagicStraightLine;
import org.wildstang.year2019.auto.programs.PathNameConstants;
import org.wildstang.year2019.auto.steps.BasicStraight;
import org.wildstang.year2019.auto.steps.DelayStep;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import org.wildstang.framework.auto.AutoProgram;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class step1 extends AutoProgram {

    @Override
    protected void defineSteps() {
        //addStep(new BasicStraight(124));//goes directly to the hatch from level1
        //addStep(new BasicStraight(120));
        //addStep(new DelayStep(0.5));
        //addStep(new PathFollowerStep(PathNameConstants.TEST_LINE,true));
        addStep(new PathFollowerStep(PathNameConstants.HAB_CARGO_FAR_LEFT,true));
        addStep(new DeployHatch());
        addStep(new PathFollowerStep(PathNameConstants.CARGO_FAR_LEFT_INTERIM_HP,false));
        addStep(new PathFollowerStep(PathNameConstants.INTERIM_CARGO_FAR_LEFT_HP,true));
        addStep(new CollectHatch());
        addStep(new PathFollowerStep(PathNameConstants.the2056D,false));
        addStep(new PathFollowerStep(PathNameConstants.the2056E,true));
        addStep(new DeployHatch());
    
    }

    @Override
    public String toString() {
        //give it a name
        return "step1";
    }

}