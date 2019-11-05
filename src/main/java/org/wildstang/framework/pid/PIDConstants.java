package org.wildstang.framework.pid;

/**
 * Class:       PIDConstants.java
 * Description: Tuple of F, P, I, and D constants, plus the Talon slot number.
 * Notes:       The Talon supports up to 4 slots.
 *              More info is available here: https://phoenix-documentation.readthedocs.io/en/latest/ch16_ClosedLoop.html
 *              More info on PID is available here: https://frc-pdr.readthedocs.io/en/latest/control/pid_control.html
 */
public class PIDConstants {
    // PROPORTIONAL
    // Proportion for the error to control how much the mechanism can move.
    public final double p;
    // INTEGRAL
    // Proportion for the sum of the past error, aka the integral
    public final double i;
    // DERIVATIVE
    // Proportion for the change in error, aka the derivative
    public final double d;
    // FEED-FORWARD
    // Constant to account for the dynamics of the system
    public final double f;

    // Slot to use on the Talon
    public final int slot;

    public PIDConstants(double p, double i, double d, double f, int slot) {
        this.f = f;
        this.p = p;
        this.i = i;
        this.d = d;
        this.slot = slot;
    }
}