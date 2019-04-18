package org.wildstang.year2019.auto.programs.Left2056steps;

import org.wildstang.year2019.auto.steps.PathFollowerStep;
import org.wildstang.year2019.auto.steps.CollectHatch;
import org.wildstang.year2019.auto.steps.DeployHatch;
import org.wildstang.year2019.auto.steps.DelayStep;
import org.wildstang.year2019.auto.steps.MotionMagicStraightLine;
import org.wildstang.year2019.auto.programs.PathNameConstants;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import org.wildstang.framework.auto.AutoProgram;


import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class step3 extends AutoProgram {

    @Override
    protected void defineSteps() {
        //addStep(new PathFollowerStep(PathNameConstants.the2056C,true));
        addStep(new PathFollowerStep(PathNameConstants.LCS3I,true));
    }

    @Override
    public String toString() {
        //give it a name
        return "step3";
    }

}