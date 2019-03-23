package org.wildstang.year2019.auto.programs;

import org.wildstang.year2019.auto.steps.PathFollowerStep;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import org.wildstang.framework.auto.AutoProgram;

public class TestPathReader extends AutoProgram {

    @Override
    protected void defineSteps() {
        SmartDashboard.putBoolean("Checkpoint 3003 yay", true);

        addStep(new PathFollowerStep(PathNameConstants.HAB_ROCKET_CLOSE_LEFT, true));
    }

    @Override
    public String toString() {
        //give it a name
        return "TestPathReader";
    }

}