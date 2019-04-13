package org.wildstang.year2019.auto.programs;

import org.wildstang.framework.auto.AutoProgram;
import org.wildstang.year2019.auto.steps.ExampleAutoStep;
import org.wildstang.year2019.auto.steps.MotionMagicStraightLine;

public class ExampleAutoProgram extends AutoProgram {

    @Override
    protected void defineSteps() {
        //addStep(new ExampleAutoStep());
        addStep(new MotionMagicStraightLine(50));
    }

    @Override
    public String toString() {
        //give it a name
        return "ExampleAutoProgram";
    }

}