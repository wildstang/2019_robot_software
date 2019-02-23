package org.wildstang.year2019.subsystems.drive;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import com.ctre.phoenix.motion.TrajectoryPoint;

public class PathReader {

    public static Trajectory readTrajectory(File p_path) {
        Trajectory trajectory = new Trajectory();

        // Open the file
        BufferedReader reader = null;
        Iterable<CSVRecord> records = null;
        try {
            reader = new BufferedReader(new FileReader(p_path));
            records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(reader);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ArrayList<double[]> dataPoints = new ArrayList<double[]>();
        TrajectoryPoint mpPoint = null;
        ArrayList<TrajectoryPoint> trajPoints = new ArrayList<TrajectoryPoint>();

        if (records != null) {
            for (CSVRecord record : records) {
                double position_inches = Double.parseDouble(record.get("position"));
                double velocity_inches = Double.parseDouble(record.get("velocity"));
                double interval_seconds = Double.parseDouble(record.get("dt"));

                // unit conversion
                // TODO verify that this is necessary
                double rotations = position_inches * DriveConstants.TICKS_PER_INCH;
                double velocity = velocity_inches * DriveConstants.TICKS_PER_INCH / 10;
                double interval = interval_seconds * 1000;

                double dataPoint[] = new double[3];
                dataPoint[0] = rotations;
                dataPoint[1] = velocity;
                dataPoint[2] = interval;
                dataPoints.add(new double[3]);

                // Create a TrajectoryPoint for the Talon - do this while reading the file
                mpPoint = new TrajectoryPoint();
                mpPoint.position = rotations;
                mpPoint.velocity = velocity;
                mpPoint.timeDur = (int)(interval * 1000);
                mpPoint.profileSlotSelect0 = DrivePID.PATH.slot;
                mpPoint.zeroPos = false;

                mpPoint.isLastPoint = false;

                trajPoints.add(mpPoint);
            }
            // Make sure the first and last points are marked as such
            if (trajPoints.size() > 0) {
                trajPoints.get(0).zeroPos = true;
                trajPoints.get(dataPoints.size() - 1).isLastPoint = true;
            } else {
                System.out.println("Somehow loaded empty trajectory!");
            }
        }

        trajectory.setTrajectoryPoints((double[][])dataPoints.toArray());
        trajectory.setTalonPoints(trajPoints);

        return trajectory;
    }
}
