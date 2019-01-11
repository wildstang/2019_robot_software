package org.wildstang.framework.pid;

/** A tuple of P, I, D, F constants */
public class PIDConstants {
    public final double p;
    public final double i;
    public final double d;
    public final double f;

    public PIDConstants(double p, double i, double d, double f) {
        this.p = p;
        this.i = i;
        this.d = d;
        this.f = f;
    }
}
