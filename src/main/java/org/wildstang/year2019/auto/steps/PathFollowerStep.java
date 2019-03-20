package org.wildstang.year2019.auto.steps;

import java.io.File;
import java.io.IOException;

import org.wildstang.framework.auto.steps.AutoStep;
import org.wildstang.framework.core.Core;
import org.wildstang.year2019.robot.WSSubsystems;
import org.wildstang.year2019.subsystems.drive.Drive;
import org.wildstang.year2019.subsystems.drive.Path;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import jaci.pathfinder.PathfinderFRC;
import jaci.pathfinder.Trajectory;

public class PathFollowerStep extends AutoStep {

    private String filePath;
    private Path path;
    private Drive drive;
    private boolean isForwards;

    private boolean m_started = false;

    public PathFollowerStep(String filePath, boolean isForwards) {
        this.filePath = filePath;
        this.isForwards = isForwards;
    }

    @Override
    public void initialize() {
        path = new Path();
        try {
            path.left = PathfinderFRC.getTrajectory(filePath + ".left.pf1.csv");
            path.right = PathfinderFRC.getTrajectory(filePath + ".right.pf1.csv");
        } catch (IOException fileError) {
            for (int i = 0; i < 30; ++i) {
                System.out.println("FAILED TO LOAD PATH!");
                // TODO do something more helpful
            }
        }
        drive = (Drive) Core.getSubsystemManager().getSubsystem(WSSubsystems.DRIVEBASE.getName());
    }

    @Override
    public void update() {
        if (!isFinished()) {
            if (!m_started) {
                drive.setPathFollowingMode();
                drive.setPath(path, isForwards);
                drive.resetEncoders();
                drive.startFollowingPath();
                m_started = true;
            } else {
                if (!drive.isFollowingPath()) {
                    setFinished(true);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "Path Follower";
    }

}
