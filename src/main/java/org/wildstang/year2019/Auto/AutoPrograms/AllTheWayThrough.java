package org.wildstang.year2019.Auto.AutoPrograms;

import org.wildstang.year2019.robot.WSSubsystems;
import org.wildstang.framework.auto.AutoProgram;
import org.wildstang.framework.core.Core;
import org.wildstang.year2019.Auto.AutoSteps.DelayStep;
import org.wildstang.year2019.Auto.AutoSteps.EnableIntakeStep;
import org.wildstang.year2019.Auto.AutoSteps.EnableWholePathStep;
import org.wildstang.year2019.Auto.AutoSteps.ExampleAutoStep;
import org.wildstang.year2019.subsystems.ballpath.Ballpath;

public class AllTheWayThrough extends AutoProgram {

    @Override
    protected void defineSteps() {
        addStep(new EnableIntakeStep(true));
        addStep(new DelayStep(2.0));
        addStep(new EnableIntakeStep(false));
        addStep(new EnableWholePathStep(true));
        addStep(new DelayStep(7.0));
        addStep(new EnableWholePathStep(false));
    }

    @Override
    public String toString() {
        //give it a name
        return "";
    }

}