package org.wildstang.year2019.subsystems.lift;

import org.wildstang.framework.pid.PIDConstants;

/**
 * Class:       LiftPID.java
 * Description: Container of PIDConstants used for the Lift.
 */
public enum LiftPID {
    // Constants in order P, I, D, F, Slot
    HOMING    (new PIDConstants(0.20, 0.0, 7.0, 0, 0)),
    TRACKING  (new PIDConstants(7.00, 0.0, 0.0, 0, 1)),
    DOWNTRACK (new PIDConstants(0.05, 0.0, 7.0, 0, 2));

    public final PIDConstants k;

    LiftPID(PIDConstants pid) {
        this.k = pid;
    }
}