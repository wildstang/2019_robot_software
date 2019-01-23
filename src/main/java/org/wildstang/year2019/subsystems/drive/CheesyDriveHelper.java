package org.wildstang.year2019.subsystems.drive;

/**
 * Helper class to implement "Cheesy Drive". "Cheesy Drive" simply means that
 * the "turning" stick controls the curvature of the robot's path rather than
 * its rate of heading change. This helps make the robot more controllable at
 * high speeds. Also handles the robot's quick turn functionality - "quick turn"
 * overrides constant-curvature turning for turn-in-place maneuvers.
 * 
 * TODO: add angular FPID here to deal with the issues currently handled with the
 * quickstop logic.
 */
public class CheesyDriveHelper {

    /** Constant on mQuickStopAccumulator low-pass filter */
    private static final double kAlpha = 0.1;

    /** Steering deadband as fraction of unity */
    private static final double kWheelDeadband = 0.02;
    /** Throttle deadband as fraction of unity */
    public static final double kThrottleDeadband = 0.02;

    /** Steering sensitivity during cheesy driving (non-quickturn) */
    private static final double kTurnSensitivity = 1.6;
    /**
     * Throttle above which we do not accumulate into mQuickStopAccumulator
     * TODO: figure out why this makes sense
     */
    private static final double kMaxThrottleForQuickStop = 0.2;

    /**
     * Tracks how much extra turning momentum we've built up; when we leave
     * quick-turn we penalize turning by this much to help slow down.
     * TODO replace this with PID on robot angular position.
     */
    private double mQuickStopAccumulator;

    private DriveSignal mSignal = new DriveSignal(0, 0);

    /**
     * Find appropriate wheel outputs for a cheesy move at this speed.
     * 
     * @param throttle  Commanded throttle from driver
     * @param wheel     Commanded steering from driver (right is positive)
     * @param quickTurn true iff driver commands quick turn
     * @return Correct wheel motor outputs. DO NOT MODIFY; CALLER DOES NOT OWN
     *         RETURN VALUE.
     */
    public DriveSignal cheesyDrive(double throttle, double wheel, boolean isQuickTurn) {

        wheel = handleDeadband(wheel, kWheelDeadband);
        throttle = handleDeadband(throttle, kThrottleDeadband);

        /*
         * overPower controls the extent to which we compensate for saturation in turns.
         * When the driver commands a turn while driving full throttle, we want motors
         * on one side to go over 100%. At overPower of zero, we simply limit the fast
         * side, getting as close as possible to the commanded throttle. At overPower of
         * unity, we reduce throttle on the slow side so that we get the full commanded
         * turn rate at the expense of throttle. Values in between cause behaviors in
         * between.
         */
        double overPower;

        double angularPower;

        if (isQuickTurn) {
            if (Math.abs(throttle) < kMaxThrottleForQuickStop) {
                mQuickStopAccumulator = (1 - kAlpha) * mQuickStopAccumulator + kAlpha * Util.limit(wheel, 1.0) * 2;
            }
            overPower = 1.0;
            angularPower = wheel;
        } else {
            overPower = 0.0;
            angularPower = Math.abs(throttle) * wheel * kTurnSensitivity - mQuickStopAccumulator;
            if (mQuickStopAccumulator > 1) {
                mQuickStopAccumulator -= 1;
            } else if (mQuickStopAccumulator < -1) {
                mQuickStopAccumulator += 1;
            } else {
                mQuickStopAccumulator = 0.0;
            }
        }

        double rightPwm = throttle - angularPower;
        double leftPwm = throttle + angularPower;
        if (leftPwm > 1.0) {
            rightPwm -= overPower * (leftPwm - 1.0);
            leftPwm = 1.0;
        } else if (rightPwm > 1.0) {
            leftPwm -= overPower * (rightPwm - 1.0);
            rightPwm = 1.0;
        } else if (leftPwm < -1.0) {
            rightPwm += overPower * (-1.0 - leftPwm);
            leftPwm = -1.0;
        } else if (rightPwm < -1.0) {
            leftPwm += overPower * (-1.0 - rightPwm);
            rightPwm = -1.0;
        }
        mSignal.rightMotor = rightPwm;
        mSignal.leftMotor = leftPwm;

        return mSignal;
    }

    public double handleDeadband(double val, double deadband) {
        return (Math.abs(val) > Math.abs(deadband)) ? val : 0.0;
    }
}
