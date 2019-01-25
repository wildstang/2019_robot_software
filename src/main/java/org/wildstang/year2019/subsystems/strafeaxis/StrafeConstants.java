package org.wildstang.year2019.subsystems.strafeaxis;

public class StrafeConstants {
    /** # of rotations of encoder in one inch of axis travel */
    public static final double REVS_PER_INCH = 1.5;
    /** Number of encoder ticks in one revolution */
    public static final double TICKS_PER_REV = 4096;
    /** # of ticks in one surface inch of wheel movement */
    public static final double TICKS_PER_INCH = TICKS_PER_REV * REVS_PER_INCH;

    // Motion magic constants
    public static final int TRACKING_MAX_SPEED = (int)(10.0 * TICKS_PER_INCH); // 10.0 inches per decisecond (100 in/s)
    public static final int TRACKING_MAX_ACCEL = (int)(TRACKING_MAX_SPEED / 2.0); // Stop to full speed in .2 sec
    public static final int HOMING_MAX_SPEED = (int)(0.2 * TICKS_PER_INCH); // .2 in/ds (2 in/s)
    public static final int HOMING_MAX_ACCEL = (int)(HOMING_MAX_SPEED / 10.0); // Stop to full speed in 1 sec

    /** Amount to take off the ends of the allowable travel distance to allow for error and overshoot */
    public static final double TRAVEL_PADDING_TICKS = 0.5 * TICKS_PER_INCH; // .5 inches

    public static final boolean INVERTED = false;
    public static final boolean SENSOR_PHASE = false;

    /** The maximum speed that the operator can command the axis to move with their fine-tuning control 
     * Unlike the TALON parameters, this is in inches per SECOND, not inches per decisecond
    */
    public static final double FINE_TUNE_MAX_SPEED = 1 * TICKS_PER_INCH; // 1.0 in/s

    /**
     * The maximum delta-T used in update. If update() is not called for a period due to
     * a fault of some kind, we don't want to misbehave by multiplying by an insane value on
     * resumption.
     */
    public static final double MAX_UPDATE_DT = 0.05;

    /**
     * The maximum time that we're willing to run a homing cycle. If homing takes longer than this,
     * something has gone wrong, so we disable the axis.
     */
    public static final double MAX_HOMING_TIME = 30;
}
