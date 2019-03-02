/* Placeholder --- no line detector code yet. This might be unnecessary as line detection might happen on RasPi instead.*/

package org.wildstang.year2019.subsystems.strafeaxis;
import javax.sound.sampled.Line;

import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.SerialPort.Port;

/*
130  114  96   82   56   40   24   08 0 08   24   40   56   82   96   114  130
NA - 06 - 05 - 04 - 03 - 02 - 01 - 07 - 15 - 14 - 13 - 12 - 11 - 10 - 09 - 08
                                      C
*/


public class LineDetector {
    private SerialPort arduino;
    private static int[] SENSOR_CONSTANTS = {-130, -114, 96, -82, -56, -40, -24, -8, 0, 8, 24, 40, 56, 82, 96, 114, 130};
    private static double TICKS_PER_MM = 17.746;
    boolean arduinoActive; 
    //private byte[] lineLocation = new byte[1]; 
    private byte[] lineLocation = new byte[16]; 
    
    public LineDetector() {

        try {
            //arduino = new SerialPort(9600, Port.kUSB);  
            System.out.println("Connected to kUSB");
          }
          catch(Exception e) {
            System.out.println("Falied to connect to kUSB.  Attempting to connect to kUSB1");
            try {
              //arduino = new SerialPort(9600, Port.kUSB1);  
              System.out.println("Connected to kUSB1");
            }
            catch(Exception e1) {
              System.out.println("Falied to connect to kUSB1.  Attempting to connect to kUSB2");
              try {
                //arduino = new SerialPort(9600, Port.kUSB2);  
                System.out.println("Connected to kUSB2");                
              } 
              catch(Exception e2) {
                System.out.println("Failed to connect");                
              }
            }
          }
          arduino.setReadBufferSize(1);
    }

    public byte [] getLinePosition() {
        byte valueRead = arduino.read(1)[0];
        /* We keep reading until we get a byte with the high bit set. In Java, byte is signed
        so if the high bit is set the valueRead will be <0. */
        while (valueRead >= 0) {
            valueRead = arduino.read(1)[0];
        }
        lineLocation[0] = (byte)((int)valueRead + 128);
        for (int i = 1; i < lineLocation.length; ++i) {
            lineLocation[i] = arduino.read(1)[0];
        }
        //System.out.println(lineLocation[0]);
        //return SENSOR_CONSTANTS[lineLocation[0]] * TICKS_PER_MM;
        return lineLocation;                
    }
    /*
    130  114  96   82   56   40   24   08 0 08   24   40   56   82   96   114  130
    NA - 06 - 05 - 04 - 03 - 02 - 01 - 07 - 15 - 14 - 13 - 12 - 11 - 10 - 09 - 08
                                          C
    */

}