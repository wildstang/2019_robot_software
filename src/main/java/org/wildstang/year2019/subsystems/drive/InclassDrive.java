package org.wildstang.year2019.subsystems.drive;

import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import org.wildstang.framework.core.Core;
import org.wildstang.framework.io.Input;
import org.wildstang.framework.io.inputs.AnalogInput;
import org.wildstang.framework.io.inputs.DigitalInput;
import org.wildstang.framework.subsystems.Subsystem;
import org.wildstang.year2019.robot.CANConstants;
import org.wildstang.year2019.robot.WSInputs;

/**
 * Class:       InclassDrive.java
 * Inputs:      2 joysticks and 1 button
 * Outputs:     4 talons
 * Description: This is the template class for an in-class project on Nov 11 2019.
 *              Assumes a robot with a simple 4 wheel drive, 2 on each side, with a Talon to control each wheel.
 * 
 *              You, the programmer, are given the 2 joysticks to work with plus one button for additional feature
 *              control. With these you need to create various drive systems to control the robot. Do not add any
 *              additional inputs or outputs. Create a new class using this template for parts 1, 3, and 5.
 * 
 *              There are X parts to this project. Try them one at a time:
 *              1:  Build a tank drive system where the Y-Axis of each joystick controls the speed of the wheels
 *                  on the matching side of the robot.
 *              2:  Implement an anti-turbo button that runs the robot at quarter speed for increased precision.
 *              3:  Build a halo drive system where the Left Y-Axis controls the speed of the robot and the Right
 *                  X-Axis controls the direction the robot turns (proportion of the speed left-right).
 *              4:  Implement quick turn functionality where the robot will turn faster when a button is pressed
 *                  or the right joystick reaches -1/1 (turn faster by running one side in reverse).
 *              5:  Build a holonomic drive system controlled by arcade drive where there is an angled wheel on each
 *                  corner of the robot that is controled with a single joystick by following the direction it is pointing.
 * 
 *              Note: You aren't expected to complete the whole project in one class, nor does it need to be completed
 *              and turned in. If you do not complete the project today, feel free to work on it in your free time,
 *              we may or may not come back to it in a future class.
 */
public class InclassDrive implements Subsystem {

    // inputs
    AnalogInput leftXAxis;
    AnalogInput leftYAxis;
    AnalogInput rightXAxis;
    AnalogInput rightYAxis;
    DigitalInput featureButton;

    // outputs
    TalonSRX leftFrontDrive;
    TalonSRX rightFrontDrive;
    TalonSRX leftRearDrive;
    TalonSRX rightRearDrive;

    @Override
    public void resetState() {
        // set the default state of any variables you create here
    }

    @Override
    public void inputUpdate(Input source) {
        // respond to changes for the various inputs here
    }

    @Override
    public void update() {
        // update the outputs here
    }

    @Override
    public void init() {
        // initialize inputs
        featureButton = (DigitalInput) Core.getInputManager().getInput(WSInputs.ANTITURBO);
        featureButton.addInputListener(this);

        leftXAxis = (AnalogInput) Core.getInputManager().getInput(WSInputs.DRIVE_HEADING);
        leftXAxis.addInputListener(this);

        leftYAxis = (AnalogInput) Core.getInputManager().getInput(WSInputs.DRIVE_THROTTLE);
        leftYAxis.addInputListener(this);

        rightXAxis = (AnalogInput) Core.getInputManager().getInput(WSInputs.DRIVE_HEADING);
        rightXAxis.addInputListener(this);

        rightYAxis = (AnalogInput) Core.getInputManager().getInput(WSInputs.DRIVE_THROTTLE);
        rightYAxis.addInputListener(this);

        // initialize outputs
        leftFrontDrive = new TalonSRX(CANConstants.LEFT_DRIVE_TALON);

        rightFrontDrive = new TalonSRX(CANConstants.RIGHT_DRIVE_TALON);
        rightFrontDrive.setInverted(true);

        leftRearDrive = new TalonSRX(CANConstants.LEFT_DRIVE_TALON);

        rightRearDrive = new TalonSRX(CANConstants.RIGHT_DRIVE_TALON);
        rightRearDrive.setInverted(true);

        resetState();
    }

    @Override public String getName() { return "Inclass Drive Assignment"; }
    @Override public void  selfTest() { }
}