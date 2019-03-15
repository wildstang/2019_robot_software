package org.wildstang.year2019.auto.programs;

import org.wildstang.framework.auto.AutoProgram;
import org.wildstang.year2019.auto.steps.PathFollowerStep;

public class ExamplePathFollow extends AutoProgram {

    @Override
    protected void defineSteps() {
        addStep(new PathFollowerStep("FIXME WHAT"));
    }

    @Override
    public String toString() {
        //give it a name
        return "Example path following auto program";
    }

}