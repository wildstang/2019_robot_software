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
    private byte pass[] = { 10, (byte) 150, 58, 9 };
    private int matchtime = 150;

    private String filename;

    private static int[] SENSOR_CONSTANTS =
        {-130, -114, 96, -82, -56, -40, -24, -8, 0, 8, 24, 40, 56, 82, 96, 114, 130};
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

    private double valuesFromArduino[] = new double[16];
    private int linePositionFromArduino;

    public LineDetector() {
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
        } catch (NullPointerException e) {
            System.out.println("Threw nullptr in LineDetector init!");
        }
    }

    public void run() {
        SmartDashboard.putBoolean("Arduino Active", arduinoActive);
        while (arduinoActive) {
            running++;
            SmartDashboard.putNumber("Running", running);
            readLinePositionFromArduino();
            SmartDashboard.putNumber("Arduino Strafe Target LD", linePosition);
        }
    }

    private void readLinePositionFromArduino() {
        byte byteRead = arduino.read(1)[0];
        while (byteRead != -1) {
            byteRead = arduino.read(1)[0];
        }
        for (int i = 0; i < 16; ++i) {
            if (byteRead == -1) {
                System.out.println("Communications glitch with Arduino");
            }
            int valueRead = makeUnsigned(byteRead);
            valuesFromArduino[i] = valueRead;
            byteRead = arduino.read(1)[0];
        }
        linePositionFromArduino = makeUnsigned(byteRead);

        SmartDashboard.putNumberArray("Light Sensor Values", valuesFromArduino);
        SmartDashboard.putNumber("Arduino line position", linePositionFromArduino);
    }

    public int getLineSensorData() throws NullPointerException {
        return linePosition;
    }

    public static int makeUnsigned(byte byteRead) {
        return (byteRead + 0x100) % 0x100;
    }

    /*
     * 130 114 96 82 56 40 24 08 0 08 24 40 56 82 96 114 130 NA - 06 - 05 - 04 - 03
     * - 02 - 01 - 07 - 15 - 14 - 13 - 12 - 11 - 10 - 09 - 08 C
     */

}
