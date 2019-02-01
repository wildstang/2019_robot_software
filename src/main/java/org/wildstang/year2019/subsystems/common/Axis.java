package org.wildstang.year2019.subsystems.common;

import com.ctre.phoenix.motorcontrol.IMotorController;
import com.ctre.phoenix.motorcontrol.NeutralMode;

import org.wildstang.framework.io.Input;
import org.wildstang.framework.subsystems.Subsystem;
import org.wildstang.framework.timer.WsTimer;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.DigitalInput;

/**
 * This is a base class for controlled axes. This year it's the base for the lift and the strafe axes.
 * 
 * PID-controlled axes like these have some traits in common. They have limit switches and need to react
 * to them. They need a fine-tune input separate from their coarse commanded inputs. This encapsulates that.
 * 
 */
public abstract class Axis implements Subsystem {

    /**
     * The update() method takes into account time since update() was last called. If a
     * glitch causes a long pause, however, we don't want to suddenly make a huge position update and
     * run off the rails (potentially literally). So we cap that time at some sane value.
     */
    private static final double MAX_UPDATE_DT = .01;

    // Local inputs
    private double manualAdjustment;
    private double roughTarget;

    private WsTimer timer;
    private double lastUpdateTime;




    protected static class AxisConfig {
        // TODO: refactor CAN motors into output so this can just be an output
        /**
         * The motor used to control the axis. Subclass must configure the motor and hand it
         * off to us. That includes setting up 
         */
        private IMotorController motor;
    
        /** The number of motor encoder ticks in one inch of axis travel. */
        private double ticksPerInch;
        /** The maximum motor acceleration during run */
        private double runAcceleration;
        /** The maximum motor speed during run */
        private double runSpeed;
        /** The maximum motor acceleration during homing */
        private double homingAcceleration;
        /** The maximum motor speed during homing */
        private double homingSpeed;

        /** The maximum speed of the axis in fine-tuning. */
        private double manualSpeed;

        /** This input is used by the manipulator controller to fine-tune the axis position. */
        private AnalogInput manualAdjustmentJoystick;

        public static DigitalInput lowerLimitSwitch; 

        public static DigitalInput upperLimitSwitch; 
    }

    private AxisConfig axisConfig;

    public void update() {
        double time = timer.get();
        double dT = time - lastUpdateTime;
        lastUpdateTime = time;
        // Clamp the dT to be no more than MAX_UPDATE_DT so that
        // if we glitch and don't update for a while we don't do a big jerk motion
        if (dT > MAX_UPDATE_DT) {
            // FIXME real logging
            System.out.println("WARNING: MAX_UPDATE_DT exceeded in Axis");
            dT = MAX_UPDATE_DT;
        }

        manualAdjustment += axisConfig.manualAdjustmentJoystick.getValue() * axisConfig.manualSpeed * dT;
        setTarget(roughTarget + manualAdjustment);
    }

    public void inputUpdate(Input source) {
        if (source == axisConfig.manualAdjustmentJoystick) {
            // Handled in update, nothin to do
        }
    }

    public void resetState() {
        manualAdjustment = 0;
    }

    /**
     * Set the rough position (e.g. preset position, or vision-detected position). Actual axis
     * position will also take into account the manual adjustment.
     * @param position Position to travel to in inches
     */
    protected void setRoughTarget(double target) {
        roughTarget = target;
    }

    /**
     * Initialize the axis with relevant settings
     */
    protected void initAxis(AxisConfig config) {
        this.axisConfig = config;
        timer.start();
    }

    /**
     * Set the exact target of axis motion. For private use to move the axis.
     */
    private void setTarget(double target) {
        //target = 
    }
}