package org.wildstang.year2019.subsystems.ballpath;

import org.wildstang.framework.io.Input;
import org.wildstang.framework.subsystems.Subsystem;
import org.wildstang.year2019.robot.WSInputs;
import org.wildstang.framework.CoreUtils;
import org.wildstang.framework.core.Core;
import org.wildstang.framework.io.inputs.AnalogInput;
import org.wildstang.framework.io.inputs.DigitalInput;
import org.wildstang.year2019.robot.WSOutputs;
import org.wildstang.year2019.robot.CANConstants;
import org.wildstang.year2019.robot.Robot;
import org.wildstang.hardware.crio.outputs.WsSolenoid;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.SensorCollection;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.TalonSRXConfiguration;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/** This subsystem is responsible for handling cargo from entry to exit in the robot.

This subsystem includes the intake, ball hopper, and carriage machinery. 

Sensors:
<ul>
<li> Ball presence detector (beam break or something?) in carriage
</ul>

Actuators:
<ul>
<li> Intake roller motor
<li> Intake deploy piston solenoid
<li> Hopper belt drive motor
<li> Hopper belt position piston solenoid
<li> Carriage roller
</ul>

*/
public class Ballpath implements Subsystem {

    private DigitalInput intake;
    private DigitalInput carriage_rollers;
    private DigitalInput everything;
    private DigitalInput hopper_button;
    private WsSolenoid hopper_solenoid;
    private boolean position;

    @Override
    public void inputUpdate(Input source) {
        //Set up 4 buttons
        /**
         * 1 button (INTAKE) to deploy the hopper intake and run the motors (Hold)
         * 1 button(HOPPER_SOLENOID) to actuate the hopper rollers (Hold)
         * 1 button (CARRIAGE_ROLLERS) to run carriage rollers (Hold)
         * 1 button (EVERYTHING) to deploy the hopper intake, run the intake motors, run the hopper rollers, 
         * and the carriage rollers (Hold)
         * 
         * Update local variables
         */

        if(source == hopper_button)
        {
            if(hopper_button.getValue())
            {
                position = true;
            }
        }//hopper
        if(source == intake)
        {

        }//intake
        if(source == carriage_rollers)
        {
            
        }//carriage rollers
        if(source == everything)
        {
            
        }//everything


    }

    @Override
    public void init() {

        intake = (DigitalInput) Core.getInputManager().getInput(WSInputs.INTAKE.getName());
        intake.addInputListener(this);
        carriage_rollers = (DigitalInput) Core.getInputManager().getInput(WSInputs.CARRIAGE_ROLLER.getName());
        carriage_rollers.addInputListener(this);
        everything = (DigitalInput) Core.getInputManager().getInput(WSInputs.EVERYTHING.getName());
        everything.addInputListener(this);
        hopper_button = (DigitalInput) Core.getInputManager().getInput(WSInputs.HOPPER_SOLENOID.getName());
        hopper_button.addInputListener(this);
        hopper_solenoid = (WsSolenoid) Core.getOutputManager().getOutput(WSOutputs.HOPPER_SOLENOID_OUTPUT.getName());
        
        resetState();
    }

    @Override
    public void selfTest() {
        // TODO
    }

    @Override
    public void update() {
        /**
         * If INTAKE is pressed down then deploy the intake and set the intake motors to
         * run
         * 
         * If HOPPER_SOLENOID is pressed then actuate the piston. In when it's false, out 
         * when it's true
         * 
         * If CARRIAGE_ROLLER is pressed then run the carriage motors
         * 
         * If EVERYTHING is pressed when deploy the intake, run the intake motors,
         * carriage and hopper rollers
         * 
         */
        hopper_solenoid.setValue(position);
    }

    @Override
    public void resetState() {
        //Set desired positions for solenoids
    }

    @Override
    public String getName() {
        return "Ballpath";
    }
}