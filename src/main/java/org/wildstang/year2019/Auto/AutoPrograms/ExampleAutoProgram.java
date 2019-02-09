package org.wildstang.year2019.Auto.AutoPrograms;

import org.wildstang.framework.auto.AutoProgram;
import org.wildstang.year2019.Auto.AutoSteps.ExampleAutoStep;

public class ExampleAutoProgram extends AutoProgram {

    @Override
    protected void defineSteps() {
        addStep(new ExampleAutoStep());
    }

    @Override
    public String toString() {
        //give it a name
        return "";
    }

}