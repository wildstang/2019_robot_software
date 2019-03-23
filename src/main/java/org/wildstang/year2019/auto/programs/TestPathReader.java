package org.wildstang.year2019.auto.programs;

import org.wildstang.year2019.auto.steps.PathFollowerStep;
import org.wildstang.framework.auto.AutoProgram;

public class TestPathReader extends AutoProgram {

    @Override
    protected void defineSteps() {
        addStep(new PathFollowerStep(PathNameConstants.HAB_ROCKET_CLOSE_LEFT, true));
    }

    @Override
    public String toString() {
        //give it a name
        return "TestPathReader";
    }

}