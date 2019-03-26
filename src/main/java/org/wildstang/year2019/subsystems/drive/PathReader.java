package org.wildstang.year2019.subsystems.drive;

import com.ctre.phoenix.motion.TrajectoryPoint;

import jaci.pathfinder.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class PathReader {

    public static Trajectory readTrajectory(File p_path) {
        jaci.pathfinder.Trajectory values = null;
        try {
            values = Pathfinder.readFromCSV(p_path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<TrajectoryPoint> trajPoints = new ArrayList<TrajectoryPoint>();
        double[][] dataPoints = new double[values.length()][];
        Trajectory trajectory = new Trajectory();
        TrajectoryPoint mpPoint = null;
        for (int i = 0; i < values.length(); i++) {
            mpPoint = new TrajectoryPoint();
            dataPoints[i] = new double[3];

            dataPoints[i][0] = (int) values.get(i).dt;
            dataPoints[i][1] = values.get(i).position*6*Math.PI ;
            dataPoints[i][2] = values.get(i).velocity*6*Math.PI;//*18.85;

            
            mpPoint.timeDur = (int) dataPoints[i][0];
            mpPoint.position = dataPoints[i][1];
            mpPoint.velocity = dataPoints[i][2];

            mpPoint.profileSlotSelect0 = 0;

            if (i == 0) {
                mpPoint.zeroPos = true;
            } else {
                mpPoint.zeroPos = false;
            }

            if (i == values.length() - 1) {
                mpPoint.isLastPoint = true;
            } else {
                mpPoint.isLastPoint = false;
            }

            trajPoints.add(mpPoint);
        }

        trajectory.setTalonPoints(trajPoints);
        trajectory.setTrajectoryPoints(dataPoints);

        return trajectory;
    }

}
