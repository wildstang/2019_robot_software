/* Placeholder --- no line detector code yet. This might be unnecessary as line detection might happen on RasPi instead.*/

package org.wildstang.year2019.subsystems.strafeaxis;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.file.*;
import java.io.*;
import java.sql.Driver;

import javax.sound.sampled.Line;

//import com.sun.tools.classfile.TypeAnnotation.Position;

import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.SerialPort.Port;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/*
130  114  96   82   56   40   24   08 0 08   24   40   56   82   96   114  130
NA - 06 - 05 - 04 - 03 - 02 - 01 - 07 - 15 - 14 - 13 - 12 - 11 - 10 - 09 - 08
                                      C
*/

//

public class LineDetector extends Thread {
  private DriverStation ds;
  private SerialPort arduino;
  private FileOutputStream out;
  private byte pass[] = {10, (byte)150, 58, 9};
  private int matchtime = 150;

  private String filename; 

  private static int[] SENSOR_CONSTANTS = { -130, -114, 96, -82, -56, -40, -24, -8, 0, 8, 24, 40, 56, 82, 96, 114,
      130 };
  private static double TICKS_PER_MM = 17.746;
  boolean arduinoActive;
  // private byte[] lineLocation = new byte[1];
  private byte[] lineData = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
  private int linePosition; 
  public static double AVERAGE_CONSTANSTS[] = new double[16];
  public double average[] = new double[16];
  public double offFromAverage[] = new double[16];
  public boolean ArduinoActive = false; 
  public int running = 0;
  


  public LineDetector() throws NullPointerException {   
      try {
      arduino = new SerialPort(9600, Port.kUSB);
      System.out.println("Connected to kUSB");
      arduinoActive = true;
    } catch (Exception e) {
      System.out.println("Falied to connect to kUSB.  Attempting to connect to kUSB1");
      try {
        arduino = new SerialPort(9600, Port.kUSB1);
        System.out.println("Connected to kUSB1");
        arduinoActive = true;
      } catch (Exception e1) {
        System.out.println("Falied to connect to kUSB1.  Attempting to connect to kUSB2");
        try {
          arduino = new SerialPort(9600, Port.kUSB2);
          System.out.println("Connected to kUSB2");
          arduinoActive = true;
        } catch (Exception e2) {
          System.out.println("Failed to connect");
        }
      }
    }
    try {
      arduino.setReadBufferSize(1);
      run();
    }
    catch(NullPointerException e) {

    }
  }

  
  public void run()  {
    SmartDashboard.putBoolean("Arduino Active",arduinoActive);
    if(arduinoActive) {
      running++;
      SmartDashboard.putNumber("Running", running);
      //while (true) {
        byte valueRead = arduino.read(1)[0];
        if(valueRead < 0) {
          linePosition = Math.abs(valueRead) + 127;  // 256?
        }
        else {
          linePosition = valueRead;
        }
        SmartDashboard.putNumber("Arduino Strafe Target LD",valueRead);
      }
    //}
  }  
  


    public int getLineSensorData() throws NullPointerException {
      return linePosition;
    }
    
     
      
      

  /*
   * 130 114 96 82 56 40 24 08 0 08 24 40 56 82 96 114 130 NA - 06 - 05 - 04 - 03
   * - 02 - 01 - 07 - 15 - 14 - 13 - 12 - 11 - 10 - 09 - 08 C
   */

   

}