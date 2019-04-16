package org.wildstang.year2019.auto.programs.Left2056steps;

import org.wildstang.year2019.auto.steps.PathFollowerStep;
import org.wildstang.year2019.auto.steps.CollectHatch;
import org.wildstang.year2019.auto.steps.DeployHatch;
import org.wildstang.year2019.auto.steps.DelayStep;
import org.wildstang.year2019.auto.steps.MotionMagicStraightLine;
import org.wildstang.year2019.auto.steps.BasicStraight;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import org.wildstang.framework.auto.AutoProgram;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class step1 extends AutoProgram {

    @Override
    protected void defineSteps() {
        addStep(new BasicStraight(124));//goes directly to the hatch from level1
    }

    @Override
    public String toString() {
        //give it a name
        return "step1";
    }

}