/* Placeholder --- no line detector code yet. This might be unnecessary as line detection might happen on RasPi instead.*/

package org.wildstang.year2019.subsystems.strafeaxis;
import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.SerialPort.Port;

public class LineDetector {
    private SerialPort arduino = new SerialPort(9600, Port.kUSB);
    private byte[] lineLocation = new byte[1]; 
    
    public double getLinePosition() {
        lineLocation = arduino.read(0);
        return lineLocation[0];
    }
}