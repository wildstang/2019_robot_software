/* Placeholder --- no line detector code yet. This might be unnecessary as line detection might happen on RasPi instead.*/

package org.wildstang.year2019.subsystems.strafeaxis;

import javax.sound.sampled.Line;

import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.SerialPort.Port;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class LineDetector {
    private SerialPort arduino; ;
    private byte[] lineLocation = new byte[1]; 
    
    public LineDetector() {
        try {
            arduino = new SerialPort(9600, Port.kUSB);  
            System.out.println("Connected to kUSB");
          }
          catch(Exception e) {
            System.out.println("Failed to connect to kUSB.  Attempting to connect to kUSB1");
            try {
              arduino = new SerialPort(9600, Port.kUSB1);  
              System.out.println("Connected to kUSB1");
            }
            catch(Exception e1) {
              System.out.println("Falied to connect to kUSB1.  Attempting to connect to kUSB2");
              try {
                arduino = new SerialPort(9600, Port.kUSB2);  
                System.out.println("Connected to kUSB2");
              }
              catch(Exception e2) {
                System.out.println("Failed to connect");
              }
            }
          }
          arduino.setReadBufferSize(1);
    }

    public double getLinePosition() {
        lineLocation = arduino.read(1);
        System.out.println(lineLocation[0]);
        SmartDashboard.putNumber("Line position", lineLocation[0]);
        return lineLocation[0];
    }
}