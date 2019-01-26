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

import javax.lang.model.util.ElementScanner6;

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
    private static final double ROLLER_SPEED = 1.0;
    private static final double BACKWARDS_ROLLER_SPEED = -1.0;


    private AnalogInput carriageRollersInput;
    private DigitalInput intakeInput;
    private DigitalInput fullBallpathInput;
    private DigitalInput reverseInput;
    private DigitalInput hopperInput;

    private WsSolenoid hopper_solenoid;
    private WsSolenoid intake_solenoid;

    private VictorSPX intakeVictor;
    private VictorSPX hopperVictor1;
    private VictorSPX hopperVictor2;
    private VictorSPX carriageVictor;

    
    private boolean reverseValue;
    private boolean hopper_position;
    private boolean intake_position;
    private boolean isIntake_motor;
    private boolean isCarriageMotor;
    private boolean isHopper_motor;
    private double CarriageValue;

    /** 
     * TODO: Names set up for each Victor that we are going to need
     * TODO: Add variables that can be updated when buttons are pressed down
     * 
     */

    @Override
    public void inputUpdate(Input source) {
        //Set up 4 buttons
        /**
         * 1 button (INTAKE) to deploy the hopper intake and run the motors (HOLD)
         * 1 button(HOPPER_SOLENOID) to actuate the hopper rollers (HOLD)
         * 1 button (CARRIAGE_ROLLERS) to run carriage rollers (JOYSTICK)
         * 1 button (FULL_BALLPATH) to deploy the hopper intake, run the intake motors, run the hopper rollers, 
         * and the carriage rollers (HOLD)
         * 
         * Update local variables
         */

        if(source == hopperInput)
        {
            if(hopperInput.getValue())
            {
                hopper_position = true;
            }
            
            else
            {
                hopper_position = false;
            }

        }//hopper

        if(source == intakeInput)
        {
            if(intakeInput.getValue())
            {
                intake_position = true;
                isIntake_motor = true;
            }

            else
            {
                intake_position = false;
                isIntake_motor = false;
            }
 
        }//intake

        if(source == carriageRollersInput)
        {
            if(intakeInput.getValue())
            {
                isCarriageMotor = true;
                CarriageValue = carriageRollersInput.getValue();
            }
            
            else
            {
                isCarriageMotor = false;
                CarriageValue = 0.0; 
            } 
        }//carriage rollers

        if(source == fullBallpathInput)
        {
            if(intakeInput.getValue())
            {
                hopper_position = true;
                intake_position = true;
                isIntake_motor = true;
                isCarriageMotor = true;
                isHopper_motor = true;
                CarriageValue = carriageRollersInput.getValue();
            }

            else
            {
                hopper_position = false;
                intake_position = false;
                isIntake_motor = false;
                isCarriageMotor = false;
                isHopper_motor = false;
                CarriageValue = 0.0;
            }//everything
        
        }

        if(source == reverseInput)
        {
            if(intakeInput.getValue())
            {
                reverseValue = true;
                intake_position = true;
            }

            else
            {
                reverseValue = false;
                intake_position = false;
            }
        }


    }

    @Override
    public void init() {

        //Input listeners
        intakeInput = (DigitalInput) Core.getInputManager().getInput(WSInputs.INTAKE.getName());
        intakeInput.addInputListener(this);
        carriageRollersInput = (AnalogInput) Core.getInputManager().getInput(WSInputs.CARRIAGE_ROLLERS);
        carriageRollersInput.addInputListener(this);
        fullBallpathInput = (DigitalInput) Core.getInputManager().getInput(WSInputs.FULL_BALLPATH.getName());
        fullBallpathInput.addInputListener(this);
        hopperInput = (DigitalInput) Core.getInputManager().getInput(WSInputs.HOPPER_SOLENOID.getName());
        hopperInput.addInputListener(this);

        //Solenoids
        hopper_solenoid = (WsSolenoid) Core.getOutputManager().getOutput(WSOutputs.HOPPER_SOLENOID.getName());
        intake_solenoid = (WsSolenoid) Core.getOutputManager().getOutput(WSOutputs.INTAKE_SOLENOID.getName());
        
        //WsVictors
        intakeVictor = new VictorSPX(CANConstants.INTAKE_VICTOR);
        hopperVictor1 = new VictorSPX(CANConstants.HOPPER_VICTOR1);
        hopperVictor2 = new VictorSPX(CANConstants.HOPPER_VICTOR2);
        carriageVictor = new VictorSPX(CANConstants.CARRIAGE_VICTOR);
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
         * - Deploy pistons that put the intake mech into position
         * - Set motors to run at full power
         * 
         * If HOPPER_SOLENOID is pressed then actuate the piston. In when it's false, out 
         * when it's true
         * - Set the value of the solenoid while the button is being pressed
         * 
         * If CARRIAGE_ROLLER is pressed then run the carriage motors
         * - JOYSTICK? Button HOLD?
         * - Button - Set the motors to run at full power
         * - JOYSTICK - Set the value of the 
         * 
         * If EVERYTHING is pressed when deploy the intake, run the intake motors,
         * carriage and hopper rollers
         * 
         */
        hopper_solenoid.setValue(hopper_position);
        intake_solenoid.setValue(intake_position);
        if(isIntake_motor)
        {
            intakeVictor.set(ControlMode.PercentOutput, ROLLER_SPEED);


        }
        if(isCarriageMotor)
        {
            carriageVictor.set(ControlMode.PercentOutput, CarriageValue);
        }
        if(isHopper_motor)
        {
            hopperVictor1.set(ControlMode.PercentOutput, ROLLER_SPEED);
            hopperVictor2.set(ControlMode.PercentOutput, ROLLER_SPEED);

        }
        if(reverseValue)
        {
            hopperVictor1.set(ControlMode.PercentOutput, BACKWARDS_ROLLER_SPEED);
            hopperVictor2.set(ControlMode.PercentOutput, BACKWARDS_ROLLER_SPEED);
            carriageVictor.set(ControlMode.PercentOutput, BACKWARDS_ROLLER_SPEED);
            intakeVictor.set(ControlMode.PercentOutput, BACKWARDS_ROLLER_SPEED);

        }
    
    }

    @Override
    public void resetState() {
        hopper_solenoid.setValue(false);
        intake_solenoid.setValue(false);
        intakeVictor.set(ControlMode.PercentOutput, 0.0);
        carriageVictor.set(ControlMode.PercentOutput, 0.0);
        hopperVictor1.set(ControlMode.PercentOutput, 0.0);
        hopperVictor2.set(ControlMode.PercentOutput, 0.0);

        //Set desired positions for solenoids
    }

    @Override
    public String getName() {
        return "Ballpath";
    }
}