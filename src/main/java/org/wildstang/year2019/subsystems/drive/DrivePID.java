package org.wildstang.year2019.subsystems.drive;

import org.wildstang.framework.pid.PIDConstants;

/**
 * Class:       DrivePID.java
 * Description: Container of PIDConstants used for the Drive system.
 */
public enum DrivePID {
    PATH      (new PIDConstants(1.0, 0.000, 0.00, 0.60, 0)),
    BASE_LOCK (new PIDConstants(0.8, 0.001, 10.0, 0.00, 1)),
    MM_QUICK  (new PIDConstants(0.8, 0.001, 10.0, 0.55, 2)),
    MM_DRIVE  (new PIDConstants(0.2, 0.001, 2.00, 0.00, 3));

    public final PIDConstants k;

    DrivePID(PIDConstants pid) {
        this.k = pid;
    }
}