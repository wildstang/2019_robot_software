package org.wildstang.year2019.subsystems.strafeaxis;

import org.wildstang.framework.pid.PIDConstants;

/**
 * Class:       StrafePID.java
 * Description: Container of PIDConstants used for StrafeAxis.
 */
public enum StrafePID {
    // Constants in order P, I, D, F, Slot
    HOMING   (new PIDConstants(0.1, 0, 0.001, 0, 0)),
    TRACKING (new PIDConstants(0.1, 0, 0.001, 0, 1));

    public final PIDConstants k;

    StrafePID(PIDConstants pid) {
        this.k = pid;
    }
}