/* Placeholder --- no line detector code yet. This might be unnecessary as line detection might happen on RasPi instead.*/

package org.wildstang.year2019.subsystems.strafeaxis;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.file.*;
import java.io.*;
import java.sql.Driver;

import javax.sound.sampled.Line;

import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.SerialPort.Port;

/*
130  114  96   82   56   40   24   08 0 08   24   40   56   82   96   114  130
NA - 06 - 05 - 04 - 03 - 02 - 01 - 07 - 15 - 14 - 13 - 12 - 11 - 10 - 09 - 08
                                      C
*/

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
  private byte linePosition; 
  public static double AVERAGE_CONSTANSTS[] = new double[16];
  public double average[] = new double[16];
  public double offFromAverage[] = new double[16];
  public boolean ArduinoActive = false; 
  


  public LineDetector() throws NullPointerException {   
    
    try {
      File file = new File("/home/lvuser/log");
      file.createNewFile();
      out = new FileOutputStream(file, false);
      out.write(pass);
      out.write(pass);
      System.out.println("file created") ;
    }
    catch(IOException ex) {
      System.out.println("file not created");
      System.out.println(ex);
    }

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
    }
    catch(NullPointerException e) {

    }
    

    DriverStation ds = DriverStation.getInstance();

    
  }

  
  public void run()  {
    if(arduinoActive) {
      while (true) {
        byte valueRead = arduino.read(1)[0];
        
         //We keep reading until we get a byte with the high bit set. In Java, byte is
         //signed so if the high bit is set the valueRead will be <0.
         
        while (valueRead >= 0) {  
          valueRead = arduino.read(1)[0];
        }
        lineData[0] = (byte) ((int) valueRead + 128);
        for (int i = 1; i < lineData.length; ++i) {
          lineData[i] = arduino.read(1)[0];
        }
      }
    }

    
    
     
      /*
      do {
        //wait
      } while(matchtime != (int)ds.getMatchTime());
      try {
        for(byte data : lineData) {
          out.write(data);
          out.write(10);
        }
        out.write(pass);
        matchtime = (int)ds.getMatchTime();
        pass[1] = (byte)matchtime;
        
      }
      catch(IOException ex) {}
      
      matchtime--;
      */
    
    
  }
  public byte[] getLineSensorData() throws NullPointerException {
    return lineData;
  }

  /*
   * 130 114 96 82 56 40 24 08 0 08 24 40 56 82 96 114 130 NA - 06 - 05 - 04 - 03
   * - 02 - 01 - 07 - 15 - 14 - 13 - 12 - 11 - 10 - 09 - 08 C
   */

   

}