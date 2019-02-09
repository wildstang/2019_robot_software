package org.wildstang.year2019.Auto.AutoSteps;

import org.wildstang.framework.auto.steps.AutoStep;
import org.wildstang.framework.core.Core;
import org.wildstang.framework.timer.WsTimer;
import org.wildstang.year2019.robot.WSSubsystems;

import org.wildstang.year2019.subsystems.ballpath.Ballpath;


//this example is for Drive, you can also modify it to use ballpath, climbwedge, hatch, lift, strafeaxis

public class DelayStep extends AutoStep{

    WsTimer timer;
    double delay;

    public DelayStep(double delay) {
        this.delay = delay;
    }

    public void update() {
        if (timer.get() > delay) {
            setFinished(true);
        }
    }
    public String toString(){
        //put a reasonable name for this step inside the string
        return "DelayStep";
    }
    public void initialize(){
        timer.start();
    }


}