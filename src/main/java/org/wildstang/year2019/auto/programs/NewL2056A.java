package org.wildstang.year2019.auto.programs;

import org.wildstang.year2019.auto.steps.PathFollowerStep;
import org.wildstang.year2019.auto.steps.CollectHatch;
import org.wildstang.year2019.auto.steps.DeployHatch;
import org.wildstang.year2019.auto.steps.DelayStep;
import org.wildstang.year2019.auto.steps.BasicStraight;
import org.wildstang.year2019.auto.steps.MotionMagicStraightLine;
import org.wildstang.year2019.auto.programs.PathNameConstants;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import org.wildstang.framework.auto.AutoProgram;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class NewL2056A extends AutoProgram {

    @Override
    protected void defineSteps() {


        
        addStep(new PathFollowerStep(PathNameConstants.L20561I,true));

        addStep(new DeployHatch());

        addStep(new PathFollowerStep(PathNameConstants.L20562I,false));

        addStep(new PathFollowerStep(PathNameConstants.L20563I, true));
        
        addStep(new CollectHatch());

        addStep(new PathFollowerStep(PathNameConstants.L20564I, false));

        addStep(new PathFollowerStep(PathNameConstants.L20565I, true));

        addStep(new DeployHatch());
        
        addStep(new PathFollowerStep(PathNameConstants.L20566IA, false));

    }

    @Override
    public String toString() {
        //give it a name
        return "L2056A";
    }

}