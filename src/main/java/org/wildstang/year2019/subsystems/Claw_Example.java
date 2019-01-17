package org.wildstang.year2019.subsystems;

import org.wildstang.year2019.robot.WSInputs;
import org.wildstang.framework.CoreUtils;
import org.wildstang.framework.core.Core;
import org.wildstang.framework.io.Input;
import org.wildstang.framework.io.inputs.AnalogInput;
import org.wildstang.framework.io.inputs.DigitalInput;
import org.wildstang.framework.subsystems.Subsystem;
import org.wildstang.year2016.robot.WSOutputs;
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

public class Claw_Example implements Subsystem{

    private DigitalInput button;
    private boolean position;
    private WsSolenoid solenoid;

    public void init(){
        //registers and creates all sensors and other items
        button = (DigitalInput) Core.getInputManager().getInput(WSInputs.ANTITURBO.getName());
        button.addInputListener(this);
        solenoid = (WsSolenoid) Core.getOutputManager().getOutput(WSOutputs.ARMS.getName());
        resetState();
    }
    public void update(){
        //called every time the core framework updates (~.02 seconds)
        solenoid.setValue(position);
    }
    public void inputUpdate(Input signal){
        //used to read local sensors and put them into variables
        if (signal==button){
            position=button.getValue();
        }
    }
    public void selfTest(){
        //used for testing
    }
    public void resetState(){
        //resets to the default state for all variables
        position=false;
    }
    public String getName(){
        return "Claw_Example";
    }



}