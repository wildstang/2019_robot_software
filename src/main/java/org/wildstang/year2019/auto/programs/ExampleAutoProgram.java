package org.wildstang.year2019.auto.programs;

import org.wildstang.framework.auto.AutoProgram;
import org.wildstang.year2019.auto.steps.ExampleAutoStep;

public class ExampleAutoProgram extends AutoProgram {

    @Override
    protected void defineSteps() {
        addStep(new ExampleAutoStep());
    }

    @Override
    public String toString() {
        //give it a name
        return "ExampleAutoProgram";
    }

}