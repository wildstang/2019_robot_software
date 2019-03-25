package org.wildstang.year2019.auto.programs;

import org.wildstang.year2019.auto.steps.PathFollowerStep;
import org.wildstang.framework.auto.AutoProgram;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class TestPathReader extends AutoProgram {

    @Override
    protected void defineSteps() {
        
        addStep(new PathFollowerStep(PathNameConstants.EXAMPLE + "habRocketCloseLeft", true));
        //addStep(new PathFollowerStep(PathNameConstants.EXAMPLE + "rocketCloseInterimLeft", false));
        //addStep(new PathFollowerStep(PathNameConstants.EXAMPLE + ""))
    }

    @Override
    public String toString() {
        //give it a name
        return "TestPathReader";
    }

}