package org.wildstang.year2019.robot;

import org.wildstang.framework.core.Inputs;
import org.wildstang.framework.hardware.InputConfig;
import org.wildstang.framework.hardware.WsRemoteAnalogInputConfig;
import org.wildstang.framework.io.inputs.InputType;
import org.wildstang.hardware.JoystickConstants;
import org.wildstang.hardware.crio.inputs.WSInputType;
import org.wildstang.hardware.crio.inputs.config.WsAnalogGyroConfig;
import org.wildstang.hardware.crio.inputs.config.WsDigitalInputConfig;
import org.wildstang.hardware.crio.inputs.config.WsJSButtonInputConfig;
import org.wildstang.hardware.crio.inputs.config.WsJSJoystickInputConfig;

/**
 * Class:       WSInputs.java
 * Description: Defines inputs for the robot.
 */
public enum WSInputs implements Inputs {
    //***************************************************************
    //      Driver and Manipulator Controller Button Locations
    //***************************************************************
    //
    //    +-------------------------------------------------------+
    //  +  +---------+              [TOP]              +---------+  +       
    //  |  |    6    |                                 |    7    |  |       
    //  |  +---------+                                 +---------+  |       
    //  |      			                                           |   
    //  |  +---------+                                 +---------+  |       
    //  |  |    4    |                                 |    5    |  |
    //  +  +---------+                                 +---------+  +
    //    +-------------------------------------------------------+
    //  
    //    +-------------------------------------------------------+
    //   /    +--+                 [FRONT]                         \
    //  +     |YU|                                         (3)      +       
    //  |  +--+  +--+        +----+       +----+                    | 
    //  |  |XL    XR|        |  8 |  (X)  |  9 |       (0)     (2)  |       
    //  |  +--+  +--+        +----+       +----+                    | 
    //  |     |YD|                                         (1)      |       
    //  |     +--+     +--+          (X)          +--+              |
    //  |             /    \                     /    \             |
    //  |            |  10  |                   |  11  |            |
    //  |             \    /                     \    /             |
    //  +              +--+                       +--+              +
    //   \                                                         /
    //    \            +-----------------------------+            /
    //     \          /                               \          /
    //      \        /                                 \        /
    //       \      /                                   \      /
    //        +----+                                     +----+
    
    // ********************************
    // Driver Enums
    // ********************************

    // ---------------------------------
    // Driver Joysticks
    // ---------------------------------
    DRIVE_THROTTLE("Throttle", WSInputType.JS_JOYSTICK, 
            new WsJSJoystickInputConfig(0, JoystickConstants.LEFT_JOYSTICK_Y), true),
            
    DRIVE_HEADING("Heading", WSInputType.JS_JOYSTICK, 
            new WsJSJoystickInputConfig(0, JoystickConstants.RIGHT_JOYSTICK_Y), true),

    // ---------------------------------
    // Driver Buttons
    // ---------------------------------
    ANTITURBO("Antiturbo", WSInputType.JS_BUTTON, 
            new WsJSButtonInputConfig(0, 4), false),
    
    SHIFT("Driver Shift", WSInputType.JS_BUTTON, 
            new WsJSButtonInputConfig(0, 2), false),
    
    QUICK_TURN("Quick Turn", WSInputType.JS_JOYSTICK, 
            new WsJSJoystickInputConfig(0, JoystickConstants.RIGHT_TRIGGER), false),
    
    BASE_LOCK("Base lock", WSInputType.JS_BUTTON, 
            new WsJSButtonInputConfig(0, 3), false),
    
    BUMPER("BUMPER", WSInputType.JS_BUTTON, 
            new WsJSButtonInputConfig(0, 1), false),

    AUTO_E_STOP("Auto E-Stop", WSInputType.JS_BUTTON, 
            new WsJSButtonInputConfig(0, 0), false),

    // ---------------------------------
    // Manipulator Joysticks
    // ---------------------------------
    LIFT_MANUAL("Lift Manual Up/Down", WSInputType.JS_JOYSTICK,
            new WsJSJoystickInputConfig(1, 1), true),
            
    //OPEN JOYSTICK("name", WSInputType.JS_JOYSTICK,
    //        new WsJSJoystickInputConfig(1, JoystickConstants.LEFT_JOYSTICK_X), true),

    //OPEN_JOYSTICK("Lift Manual Up/Down", WSInputType.JS_JOYSTICK,
    //        new WsJSJoystickInputConfig(1, JoystickConstants.RIGHT_JOYSTICK_Y), true),
            
    STRAFE_MANUAL("Hatch Strafe", WSInputType.JS_JOYSTICK,
            new WsJSJoystickInputConfig(1, 4), true),
            
    // ---------------------------------
    // Manipulator DPAD Buttons
    // ---------------------------------
    LIFT_PRESET_1("Lift Preset 1", WSInputType.JS_DPAD_BUTTON,
            new WsJSButtonInputConfig(1, JoystickConstants.DPAD_Y_DOWN), false),
    LIFT_PRESET_2("Lift Preset 2", WSInputType.JS_DPAD_BUTTON,
            new WsJSButtonInputConfig(1, JoystickConstants.DPAD_X_LEFT), false),
    LIFT_PRESET_3("Lift Preset 3", WSInputType.JS_DPAD_BUTTON,
            new WsJSButtonInputConfig(1, JoystickConstants.DPAD_X_RIGHT), false),
    LIFT_PRESET_4("Lift Preset 4", WSInputType.JS_DPAD_BUTTON,
            new WsJSButtonInputConfig(1, JoystickConstants.DPAD_Y_UP), false), 

    // ---------------------------------
    // Manipulator Buttons
    // ---------------------------------
    INTAKE("Intake", WSInputType.JS_BUTTON, 
            new WsJSButtonInputConfig(1, 2), false),
            
    REVERSE_BUTTON("Reverse", WSInputType.JS_BUTTON, 
            new WsJSButtonInputConfig(1, 0), false),
            
    HOPPER_SOLENOID("Hopper solenoids", WSInputType.JS_BUTTON, 
            new WsJSButtonInputConfig(1, 1), false),
            
    FULL_BALLPATH("Full Ballpath", WSInputType.JS_BUTTON, 
            new WsJSButtonInputConfig(1, 3), false),
            
    HATCH_COLLECT("Hatch Collect", WSInputType.JS_BUTTON, 
            new WsJSButtonInputConfig(1, 4), false),
    HATCH_DEPLOY("Hatch Deploy", WSInputType.JS_BUTTON, 
            new WsJSButtonInputConfig(1, 5), false),
            
    CARRIAGE_ROLLERS("Carriage Rollers", WSInputType.JS_JOYSTICK, 
            new WsJSJoystickInputConfig(1, 2), false),
            
    AUTOMATIC_STRAFE_SWITCH("Button to override strafe", WSInputType.JS_JOYSTICK, 
            new WsJSJoystickInputConfig(1, 3), false),
            
    WEDGE_SAFETY_1("First Wedge Safety", WSInputType.JS_BUTTON, 
            new WsJSButtonInputConfig(1, 6), false),
    WEDGE_SAFETY_2("Second Wedge Safety", WSInputType.JS_BUTTON, 
            new WsJSButtonInputConfig(1, 9), false),
            
    LIFT_LIMIT_SWITCH_OVERRIDE("Lift Limit Switch Override", WSInputType.JS_BUTTON, 
            new WsJSButtonInputConfig(1, 10), false),
    STRAFE_LIMIT_SWITCH_OVERRIDE("Strafe Limit Switch Override", WSInputType.JS_BUTTON, 
            new WsJSButtonInputConfig(1, 11), false),

    // ********************************
    // Digital IOs
    // ********************************
    STRAFE_LEFT_LIMIT("Strafe left limit", WSInputType.SWITCH, 
            new WsDigitalInputConfig(1, false), false),
    STRAFE_RIGHT_LIMIT("Strafe right limit", WSInputType.SWITCH, 
            new WsDigitalInputConfig(2, false), false),

    LIFT_LOWER_LIMIT("Lift Lower Limit", WSInputType.SWITCH, 
            new WsDigitalInputConfig(3, true), false),
    LIFT_UPPER_LIMIT("Lift Upper Limit", WSInputType.SWITCH, 
            new WsDigitalInputConfig(4, true), false),

    CARRIAGE_SENSOR_A("Carriage Sensor A",WSInputType.SWITCH, 
            new WsDigitalInputConfig(5,true),false),
    CARRIAGE_SENSOR_B("Carriage Sensor B",WSInputType.SWITCH, 
            new WsDigitalInputConfig(6,true),false),

    // -------------------------------
    // Networked sensors
    // -------------------------------
    /** Message from the RasPi telling us where the line is */
    LINE_POSITION("Line position", WSInputType.REMOTE_ANALOG, 
            new WsRemoteAnalogInputConfig("line"), false),

    // ********************************
    // Others ...
    // ********************************
    GYRO("Gyro", WSInputType.ANALOG_GYRO, 
            new WsAnalogGyroConfig(0, true), false),

    // IMU("IMU", WSInputType.COMPASS, new WsI2CInputConfig(I2C.Port.kMXP, 0x20),
    // true);

    VISION_FRAMES_PROCESSED("nFramesProcessed", WSInputType.REMOTE_ANALOG, 
            new WsRemoteAnalogInputConfig("vision"), false);
    
    private final String m_name;
    private final InputType m_type;

    private InputConfig m_config = null;

    private boolean m_trackingState;

    private static boolean isLogging = true;

    WSInputs(String p_name, InputType p_type, InputConfig p_config, boolean p_trackingState) {
        m_name = p_name;
        m_type = p_type;
        m_config = p_config;
        m_trackingState = p_trackingState;
    }

    @Override
    public String getName() {
        return m_name;
    }

    @Override
    public InputType getType() {
        return m_type;
    }

    public InputConfig getConfig() {
        return m_config;
    }

    public boolean isTrackingState() {
        return m_trackingState;
    }

    public static boolean getLogging() {
        return isLogging;
    }
}