package org.wildstang.year2019.robot;

public final class CANConstants {
    public static final int[] LEFT_DRIVE_TALONS = {2, 3};
    public static final int[] RIGHT_DRIVE_TALONS = {4, 13};

    // TESTING: Some Victors removed to emulate 6-wheel drive
    public static final int LEFT_DRIVE_VICTOR = 1;
    public static final int RIGHT_DRIVE_VICTOR = 5;

    // TODO put in correct IDs
    public static final int STRAFE_TALON = 12;//this is a talon right?
    public static final int INTAKE_VICTOR = 13;
    public static final int CARRIAGE_VICTOR = 14;
    public static final int HOPPER_VICTOR1 = 15;
    public static final int HOPPER_VICTOR2 = 11;
    public static final int LIFT_TALON = 10;
    public static final int LIFT_VICTOR = 9;
}
