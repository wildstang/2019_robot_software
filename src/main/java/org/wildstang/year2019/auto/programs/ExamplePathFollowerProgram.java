package org.wildstang.year2019.auto.programs;

import org.wildstang.framework.auto.AutoProgram;
import org.wildstang.year2019.auto.steps.PathFollowerStep;

public class ExamplePathFollowerProgram extends AutoProgram {

    @Override
    protected void defineSteps() {
        addStep(new PathFollowerStep("/home/lvuser/paths/2019/output/example"));
    }

    @Override
    public String toString() {
        //give it a name
        return "Example path-follower";
    }

}