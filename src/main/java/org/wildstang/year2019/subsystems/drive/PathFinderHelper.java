package org.wildstang.year2019.subsystems.drive;

import jaci.pathfinder.Pathfinder;
import jaci.pathfinder.Trajectory;
import jaci.pathfinder.followers.EncoderFollower;

import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.kauailabs.navx.frc.AHRS;

import org.wildstang.year2019.subsystems.drive.DriveConstants;

import edu.wpi.first.wpilibj.Notifier;

/**
 * This class implements the path finding logic described at
 * 
 * https://wpilib.screenstepslive.com/s/currentCS/m/84338/l/1021631-integrating-path-following-into-a-robot-program
 * 
 * PathFinderHelper needs access to the motor encoders and the gyro, so the
 * Drive must pass in those when it creates the PathFinderHelper.
 * 
 * The drive will call a method on this class to get a DriveSignal describing
 * what it should set the motors to (like CheesyDriveHelper works). We still
 * need a reference to the TalonSRXs for the motors because they have the
 * encoders on them.
 */
class PathFinderHelper {

    private AHRS gyro;
    private Path path;
    private Drive drive;
    /** Reference to left motor for encoder reading */
    private TalonSRX leftMotor;
    /** Reference to right motor for encoder reading */
    private TalonSRX rightMotor;
    /**
     * Follower for left trajectory --- includes motor control logic for left motor
     */
    private EncoderFollower leftFollower;
    /**
     * Follower for right trajectory --- includes motor control logic for right
     * motor
     */
    private EncoderFollower rightFollower;
    /**
     * This notifier calls our pathUpdate() method periodically. TODO: what controls
     * the interval?
     */
    private Notifier followerNotifier;
    /** If this is false, we follow the path backwards. */
    private boolean isForwards;

    /**
     * Create PathFinderHelper with the given gyro and motors.
     * 
     * @param gyro       Gyro to use for navigation.
     * @param leftMotor  left motor. WE DO NOT ISSUE COMMANDS TO THIS MOTOR. It is
     *                   ONLY for reading the encoder.
     * @param rightMotor same as leftMotor, but on the right.
     * @param Path       to follow.
     */
    PathFinderHelper(Drive drive, AHRS gyro, TalonSRX leftMotor, TalonSRX rightMotor, Path path, boolean isForwards) {
        this.gyro = gyro;
        this.drive = drive;
        this.leftMotor = leftMotor;
        this.rightMotor = rightMotor;
        this.leftFollower = new EncoderFollower(path.left);
        this.rightFollower = new EncoderFollower(path.right);
        this.followerNotifier = new Notifier(this::pathUpdate);
        this.path = path;
        this.isForwards = isForwards;
    }

    void pathUpdate() {
        double leftOutput;
        double rightOutput;
        if (!isActive()) {
            stop();
            drive.abortFollowingPath();
        } else {
            if (isForwards) {
                leftOutput = leftFollower.calculate(leftMotor.getSelectedSensorPosition());
                rightOutput = rightFollower.calculate(leftMotor.getSelectedSensorPosition());
            } else {
                leftOutput = -rightFollower.calculate(-leftMotor.getSelectedSensorPosition());
                rightOutput = -rightFollower.calculate(-rightMotor.getSelectedSensorPosition());
            }
            // TODO use gyro?

            drive.helperSetDriveSignal(new DriveSignal(leftOutput, rightOutput));
        }
    }

    /** Return true IFF we're currently following a path. */
    boolean isActive() {
        return !leftFollower.isFinished() && !rightFollower.isFinished();
    }

    /** Abort following the path immediately. */
    void stop() {
        followerNotifier.stop();
    }
}