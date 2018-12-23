package org.wildstang.year2017.subsystems;

import java.io.*;
import java.sql.DriverManager;
import java.util.LinkedList;

import org.wildstang.framework.core.Core;
import org.wildstang.framework.io.Input;
import org.wildstang.framework.io.inputs.AnalogInput;
import org.wildstang.framework.io.inputs.DigitalInput;
import org.wildstang.framework.subsystems.Subsystem;
import org.wildstang.hardware.crio.outputs.WsSolenoid;
import org.wildstang.year2017.robot.*;
import org.wildstang.year2017.subsystems.drive.*;

import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
//import com.ctre.phoenix.motorcontrol.can.TalonSRX.StatusFrameRate;
//import com.ctre.phoenix.motorcontrol.can.TalonSRX.TalonControlMode;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.PIDOutput;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Drive implements Subsystem
{
   // Constants
   private static final double ROBOT_WIDTH_INCHES = 30;
   private static final double WHEEL_DIAMETER_INCHES = 4;
   public static final double ENCODER_CPR = 4096;
   private static final double TICKS_TO_INCHES = WHEEL_DIAMETER_INCHES * Math.PI / ENCODER_CPR; //.0009817146
   private static final double RADIANS = Math.PI / 180;
   private final double CORRECTION_HEADING_LEVEL = 1.2;
   private static final String DRIVER_STATES_FILENAME = "/home/lvuser/drive_state_";
   private int pathNum = 1;
   private static final double ANTI_TURBO_FACTOR = 0.5;
   

   // Hold a reference to the input test for fast equality test during
   // inputUpdate()
   private AnalogInput m_headingInput;
   private AnalogInput m_throttleInput;
   private WsSolenoid m_shifterSolenoid;
   private DigitalInput m_baseLockInput;
   private DigitalInput m_shifterInput;
   private DigitalInput m_quickTurnInput;
   private DigitalInput m_autoDropInput;
   private DigitalInput m_antiTurboInput;

   // Talons for output
   private TalonSRX m_leftMaster;
   private TalonSRX m_rightMaster;
   private TalonSRX m_leftFollower;
   private TalonSRX m_rightFollower;

   private CheesyDriveHelper m_cheesyHelper = new CheesyDriveHelper();

   // Drive state information
   private DriveState absoluteDriveState = new DriveState(0, 0, 0, 0, 0, 0, 0);
   private LinkedList<DriveState> driveStates = new LinkedList<DriveState>();

   // ALL variables below here are state that needs to be reset in resetState()
   private boolean m_gearDropFinished = false;
   
   // Values from inputs
   private double m_throttleValue;
   private double m_headingValue;
   private boolean m_quickTurn;

   private boolean m_shifterCurrent = false;
   private boolean m_shifterPrev = false;
   private boolean m_highGear = false;

   private boolean m_rawModeCurrent = false;
   private boolean m_rawModePrev = false;
   private boolean m_rawMode = false;

   private boolean m_autoDropCurrent = false;
   private boolean m_autoDropPrev = false;
   private boolean m_autoDropMode = false;

   private boolean m_antiTurbo = false;
   
   private DriveType m_driveMode = DriveType.CHEESY;
   private PathFollower m_pathFollower;

   double maxSpeed = 0;
   private boolean m_brakeMode = true;

   // While this is really a temporary variable, declared here to prevent
   // constant stack allocation
   private DriveSignal m_driveSignal;

   double m_visionXCorrection;
   double m_visionDistance;

   
   @Override
   public void init()
   {
      // Add any additional items to track in the logger
      if (true)//RobotTemplate.LOG_STATE)
      {
         Core.getStateTracker().addIOInfo("Left speed (RPM)", "Drive", "Input", null);
         Core.getStateTracker().addIOInfo("Right speed (RPM)", "Drive", "Input", null);
         Core.getStateTracker().addIOInfo("Left output", "Drive", "Input", null);
         Core.getStateTracker().addIOInfo("Right output", "Drive", "Input", null);
         Core.getStateTracker().addIOInfo("Left 1 current", "Drive", "Input", null);
         Core.getStateTracker().addIOInfo("Left 2 current", "Drive", "Input", null);
         Core.getStateTracker().addIOInfo("Right 1 current", "Drive", "Input", null);
         Core.getStateTracker().addIOInfo("Right 2 current", "Drive", "Input", null);
         Core.getStateTracker().addIOInfo("Left 1 voltage", "Drive", "Input", null);
         Core.getStateTracker().addIOInfo("Left 2 voltage", "Drive", "Input", null);
         Core.getStateTracker().addIOInfo("Right 1 voltage", "Drive", "Input", null);
         Core.getStateTracker().addIOInfo("Right 2 voltage", "Drive", "Input", null);
         Core.getStateTracker().addIOInfo("Drive heading", "Drive", "Input", null);
         Core.getStateTracker().addIOInfo("Drive throttle", "Drive", "Input", null);
         Core.getStateTracker().addIOInfo("Vision distance", "Drive", "Input", null);
         Core.getStateTracker().addIOInfo("Vision correction", "Drive", "Input", null);
      }
      
      // Drive
      m_headingInput = (AnalogInput) Core.getInputManager().getInput(WSInputs.DRV_HEADING.getName());
      m_headingInput.addInputListener(this);

      m_throttleInput = (AnalogInput) Core.getInputManager().getInput(WSInputs.DRV_THROTTLE.getName());
      m_throttleInput.addInputListener(this);

      m_shifterInput = (DigitalInput) Core.getInputManager().getInput(WSInputs.SHIFT.getName());
      m_shifterInput.addInputListener(this);
      
      m_quickTurnInput = (DigitalInput) Core.getInputManager().getInput(WSInputs.QUICK_TURN.getName());
      m_quickTurnInput.addInputListener(this);

      m_autoDropInput = (DigitalInput) Core.getInputManager().getInput(WSInputs.AUTO_GEAR_DROP.getName());
      m_autoDropInput.addInputListener(this);

      m_antiTurboInput = (DigitalInput) Core.getInputManager().getInput(WSInputs.ANTITURBO.getName());
      m_antiTurboInput.addInputListener(this);

      m_baseLockInput = (DigitalInput) Core.getInputManager().getInput(WSInputs.BASE_LOCK.getName());
      m_baseLockInput.addInputListener(this);

      m_shifterSolenoid = (WsSolenoid) Core.getOutputManager().getOutput(WSOutputs.SHIFTER.getName());

      initDriveTalons();
      
      resetState();
   }

   
   @Override
   public void resetState()
   {
      resetEncoders();
      setBrakeMode(false);
      setHighGear(true);
      setOpenLoopDrive();
      m_gearDropFinished = false;
      setThrottle(0);
      setHeading(0);
      m_quickTurn = false;

      m_shifterCurrent = false;
      m_shifterPrev = false;

      m_rawModeCurrent = false;
      m_rawModePrev = false;
      m_rawMode = false;

      m_autoDropCurrent = false;
      m_autoDropPrev = false;
      m_autoDropMode = false;
      
      m_antiTurbo = false;

      maxSpeed = 0;
   }



   public void initDriveTalons()
   {
      m_leftMaster = new TalonSRX(CANConstants.LEFT_MASTER_TALON_ID);
      m_leftFollower = new TalonSRX(CANConstants.LEFT_FOLLOWER_TALON_ID);
      m_rightMaster = new TalonSRX(CANConstants.RIGHT_MASTER_TALON_ID);
      m_rightFollower = new TalonSRX(CANConstants.RIGHT_FOLLOWER_TALON_ID);

      // Start in open loop mode
      m_leftMaster.set(ControlMode.PercentOutput, 0);
      m_leftFollower.set(ControlMode.Follower, CANConstants.LEFT_MASTER_TALON_ID);

      m_rightMaster.set(ControlMode.PercentOutput, 0);
      m_rightFollower.set(ControlMode.Follower, CANConstants.RIGHT_MASTER_TALON_ID);

      /* Unclear what the new WPI analog to these lines is. -Zack
      m_leftMaster.configNominalOutputVoltage(0.0, 0.0);
      m_leftMaster.configPeakOutputVoltage(+12.0f, -12.0f);

      m_rightMaster.configNominalOutputVoltage(0.0, 0.0);
      m_rightMaster.configPeakOutputVoltage(+12.0f, -12.0f); */

      setBrakeMode(false);

      // TODO: Enable when encoders are mounted

      // Set up the encoders
      m_leftMaster.setFeedbackDevice(TalonSRX.FeedbackDevice.CtreMagEncoder_Relative);
      m_leftMaster.setStatusFrameRateMs(StatusFrameRate.Feedback, 10);
      m_leftMaster.reverseSensor(true);
      if (m_leftMaster.isSensorPresent(TalonSRX.FeedbackDevice.CtreMagEncoder_Relative) != TalonSRX.FeedbackDeviceStatus.FeedbackStatusPresent)
      {
         SmartDashboard.putBoolean("LeftEncPresent", false);
      }
      else
      {
         SmartDashboard.putBoolean("LeftEncPresent", true);
      }

      m_rightMaster.setFeedbackDevice(FeedbackDevice.CTRE_MagEncoder_Relative);
      m_rightMaster.setStatusFrameRateMs(StatusFrameRate.Feedback, 10);
      if (m_rightMaster.isSensorPresent(FeedbackDevice.CTRE_MagEncoder_Relative) != TalonSRX.FeedbackDeviceStatus.FeedbackStatusPresent)
      {
         SmartDashboard.putBoolean("RightEncPresent", false);
      }
      else
      {
         SmartDashboard.putBoolean("RightEncPresent", true);
      }

      m_leftMaster.setStatusFrameRateMs(StatusFrameRate.Feedback, 10);
      m_rightMaster.setStatusFrameRateMs(StatusFrameRate.Feedback, 10);

      // Load PID profiles
      // Path following profile
      m_leftMaster.setPID(DriveConstants.PATH_P_GAIN, DriveConstants.PATH_I_GAIN, DriveConstants.PATH_D_GAIN, DriveConstants.PATH_F_GAIN, 0, 0, DriveConstants.PATH_PROFILE_SLOT);
      m_rightMaster.setPID(DriveConstants.PATH_P_GAIN, DriveConstants.PATH_I_GAIN, DriveConstants.PATH_D_GAIN, DriveConstants.PATH_F_GAIN, 0, 0, DriveConstants.PATH_PROFILE_SLOT);

      m_leftMaster.setPID(DriveConstants.MM_QUICK_P_GAIN, DriveConstants.MM_QUICK_I_GAIN, DriveConstants.MM_QUICK_D_GAIN, DriveConstants.MM_QUICK_F_GAIN, 0, 0, DriveConstants.BASE_LOCK_PROFILE_SLOT);
      m_rightMaster.setPID(DriveConstants.MM_QUICK_P_GAIN, DriveConstants.MM_QUICK_I_GAIN, DriveConstants.MM_QUICK_D_GAIN, DriveConstants.MM_QUICK_F_GAIN, 0, 0, DriveConstants.BASE_LOCK_PROFILE_SLOT);
      
   }

   @Override
   public void inputUpdate(Input p_source)
   {

      if (p_source == m_throttleInput)
      {
         m_throttleValue = m_throttleInput.getValue();
         
//         m_quickTurn = m_cheesyHelper.handleDeadband(m_throttleValue, CheesyDriveHelper.kThrottleDeadband) == 0.0;
      }
      else if (p_source == m_headingInput)
      {
         m_headingValue = m_headingInput.getValue();
      }
      else if (p_source == m_shifterInput)
      {
         m_shifterCurrent = m_shifterInput.getValue();
         // Check and toggle shifter state
         toggleShifter();
      }
      else if (p_source == m_autoDropInput)
      {
         m_autoDropCurrent = m_autoDropInput.getValue();
         // Check and toggle auto gear drop state
         toggleAutoDrop();
         if (m_autoDropMode)
         {
            setAutoGearMode();
            SmartDashboard.putBoolean("Auto gear mode", true);
         }
         else
         {
            exitAutoGearMode();
            setOpenLoopDrive();
            setHeading(0);
            setThrottle(0);
            SmartDashboard.putBoolean("Auto gear mode", false);
         }
      }
      // TODO: Do we want to make quickturn automatic?
      else if (p_source == m_quickTurnInput)
      {
         m_quickTurn = m_quickTurnInput.getValue();
      }
      else if (p_source == m_antiTurboInput)
      {
         m_antiTurbo = m_antiTurboInput.getValue();
      }
      else if (p_source == m_baseLockInput)
      {
         m_rawModeCurrent = m_baseLockInput.getValue();

         // Determine drive state override
         if (m_rawModeCurrent)
         {
            setFullBrakeMode();
         }
         else
         {
            setOpenLoopDrive();
            setHeading(0);
            setThrottle(0);
         }
      }
   }

   @Override
   public void selfTest()
   {
      // DO NOT IMPLEMENT
   }

   @Override
   public void update()
   {

      // Set shifter output before driving
      // NOTE: The state of m_highGear needs to be set prior to update being
      // called. This is either in inputUpdate() (for teleop)
      // or by an auto program by calling setHighGear()
      if (!m_highGear)
      {
         m_shifterSolenoid.setValue(true);
      }
      else
      {
         m_shifterSolenoid.setValue(false);
      }

      SmartDashboard.putBoolean("High Gear", m_highGear);
      SmartDashboard.putNumber("throttleValue", m_throttleValue);
      SmartDashboard.putNumber("heading value", m_headingValue);
      SmartDashboard.putString("Drive mode", m_driveMode.name());
      
      switch (m_driveMode)
      {
         case PATH:
            collectDriveState();
            break;

         case CHEESY:
            double effectiveThrottle = m_throttleValue;
            if (m_antiTurbo)
            {
               effectiveThrottle = m_throttleValue * ANTI_TURBO_FACTOR;
            }
            
            m_driveSignal = m_cheesyHelper.cheesyDrive(effectiveThrottle, m_headingValue, m_quickTurn);
            setMotorSpeeds(m_driveSignal);
            
            if (RobotTemplate.LOG_STATE)
            {
               double tempMax = Math.max(Math.abs(m_leftMaster.getSpeed()), Math.abs(m_rightMaster.getSpeed()));
               if (tempMax > maxSpeed)
               {
                  maxSpeed = tempMax;
               }
                
               SmartDashboard.putNumber("Max Encoder Speed", maxSpeed);
            }
            break;
         case FULL_BRAKE:
            break;
         case AUTO_GEAR_DROP:
            autoGear();
            break;
         case MAGIC:
            break;
         case RAW:
         default:
            // Raw is default
            m_driveSignal = new DriveSignal(m_throttleValue, m_throttleValue);
            break;
      }
      
      SmartDashboard.putNumber("Left Encoder", m_leftMaster.getEncPosition());
      SmartDashboard.putNumber("Right Encoder", m_rightMaster.getEncPosition());
      
      
      if (RobotTemplate.LOG_STATE)
      {
         Core.getStateTracker().addState("Drive heading", "Drive", m_headingValue);
         Core.getStateTracker().addState("Drive throttle", "Drive", m_throttleValue);
         Core.getStateTracker().addState("Vision distance", "Drive", m_visionDistance);
         Core.getStateTracker().addState("Vision correction", "Drive", m_visionXCorrection);
         
         Core.getStateTracker().addState("Left output", "Drive", m_leftMaster.get());
         Core.getStateTracker().addState("Right output", "Drive", m_rightMaster.get());

         Core.getStateTracker().addState("Left speed (RPM)", "Drive", m_leftMaster.getSpeed());
         Core.getStateTracker().addState("Right speed (RPM)", "Drive", m_rightMaster.getSpeed());
   
         Core.getStateTracker().addState("Left 1 voltage", "Drive", m_leftMaster.getOutputVoltage());
         Core.getStateTracker().addState("Left 2 voltage", "Drive", m_leftFollower.getOutputVoltage());
         Core.getStateTracker().addState("Right 1 voltage", "Drive", m_rightMaster.getOutputVoltage());
         Core.getStateTracker().addState("Right 2 voltage", "Drive", m_rightFollower.getOutputVoltage());

         Core.getStateTracker().addState("Left 1 current", "Drive", m_leftMaster.getOutputCurrent());
         Core.getStateTracker().addState("Left 2 current", "Drive", m_leftFollower.getOutputCurrent());
         Core.getStateTracker().addState("Right 1 current", "Drive", m_rightMaster.getOutputCurrent());
         Core.getStateTracker().addState("Right 2 current", "Drive", m_rightFollower.getOutputCurrent());
      }
   }

   public void setAntiTurbo(boolean p_antiTurbo)
   {
      m_antiTurbo = p_antiTurbo;
   }
   
   private void toggleShifter()
   {
      if (m_shifterCurrent && !m_shifterPrev)
      {
         m_highGear = !m_highGear;
      }
      m_shifterPrev = m_shifterCurrent;
      
      // TODO: Remove this
      maxSpeed = 0; // Easy way to reset max speed.
   }

   private void toggleAutoDrop()
   {
      if (m_autoDropCurrent && !m_autoDropPrev)
      {
         m_autoDropMode = !m_autoDropMode;
      }
      m_autoDropPrev = m_autoDropCurrent;
   }

   private void collectDriveState()
   {
      // Calculate all changes in DriveState
      double deltaLeftTicks = m_leftMaster.getEncPosition() - absoluteDriveState.getDeltaLeftEncoderTicks();
      double deltaRightTicks = m_rightMaster.getEncPosition() - absoluteDriveState.getDeltaRightEncoderTicks();
      double deltaHeading = 0 - absoluteDriveState.getHeadingAngle(); // CHANGE
      double deltaTime = System.currentTimeMillis() - absoluteDriveState.getDeltaTime();

      
      /****** CONVERT TICKS TO TURN RADIUS AND CIRCLE ******/
      long startTime = System.nanoTime();

      double deltaLeftInches = deltaLeftTicks * TICKS_TO_INCHES;
      double deltaRightInches = deltaRightTicks * TICKS_TO_INCHES;
      double deltaTheta;
      double straightLineInches = 0;
      
      if (deltaLeftTicks != deltaRightTicks)
      {
         deltaTheta = (Math.atan2((deltaRightInches - deltaLeftInches), ROBOT_WIDTH_INCHES) / RADIANS);
      }
      else
      {
         straightLineInches = deltaLeftInches;
         deltaTheta = 0;
      }

      double c;
      double rLong;

      if (deltaTheta < 0)
      {
         c = Math.abs((deltaRightTicks * ROBOT_WIDTH_INCHES) / (deltaLeftTicks - deltaRightTicks));
      }
      else if (deltaTheta > 0)
      {
         c = Math.abs((deltaLeftTicks * ROBOT_WIDTH_INCHES) / (deltaRightTicks - deltaLeftTicks));
      }
      else
      {
         c = (double) Integer.MAX_VALUE;
      }

      rLong = c + ROBOT_WIDTH_INCHES; // Will probably use later, this is the larger turn radius.

      // System.out.println("Time Elapsed: " + (System.nanoTime() - startTime));
      /*********************************/
      
      // Add the DriveState to the list
      driveStates.add(new DriveState(deltaTime, deltaRightTicks, deltaLeftTicks, deltaHeading, straightLineInches, c, deltaTheta));

      // reset the absolute DriveState for the next cycle
      absoluteDriveState.setDeltaTime(absoluteDriveState.getDeltaTime() + deltaTime);
      absoluteDriveState.setDeltaRightEncoderTicks(absoluteDriveState.getDeltaRightEncoderTicks() + deltaRightTicks);
      absoluteDriveState.setDeltaLeftEncoderTicks(absoluteDriveState.getDeltaLeftEncoderTicks() + deltaLeftTicks);
      absoluteDriveState.setHeading(absoluteDriveState.getHeadingAngle() + deltaHeading);
   }

   private void autoGear()
   {
      m_visionXCorrection = RobotTemplate.getVisionServer().getXCorrectionLevel();
      m_visionDistance = RobotTemplate.getVisionServer().getDistance();

      setHeading(m_visionXCorrection * CORRECTION_HEADING_LEVEL);

      if (m_visionDistance < 36)
      {
         setThrottle(.2);
      }
      else
      {
         setThrottle(.35);
      }

      if (m_visionDistance < 10)
      {
         setThrottle(0);
         m_gearDropFinished = true;
      }
      
      m_driveSignal = m_cheesyHelper.cheesyDrive(m_throttleValue, m_headingValue, m_quickTurn);
      setMotorSpeeds(m_driveSignal);
   }
   
   public boolean isGearDropFinished()
   {
      return m_gearDropFinished;
   }

   private void calculateRawMode()
   {
      if (m_rawModeCurrent && !m_rawModePrev)
      {
         m_rawMode = !m_rawMode;
      }
      m_rawModePrev = m_rawModeCurrent;
      if (m_rawMode)
      {
         setRawDrive();
      }
   }

   public void setHighGear(boolean p_high)
   {
      m_highGear = p_high;
   }

   public void setBrakeMode(boolean p_brakeOn)
   {
      if (m_brakeMode != p_brakeOn)
      {
         m_leftMaster.enableBrakeMode(p_brakeOn);
         m_leftFollower.enableBrakeMode(p_brakeOn);
         m_rightMaster.enableBrakeMode(p_brakeOn);
         m_rightFollower.enableBrakeMode(p_brakeOn);
         m_brakeMode = p_brakeOn;
      }

   }
   
   public void setMotionMagicMode(boolean p_quickTurn, double f_gain)
   {
      // Stop following any current path
      stopPathFollowing();

      // Set talons to hold their current position
      if (m_driveMode != DriveType.MAGIC)
      {
         // Set up Talons for the Motion Magic mode

         m_leftMaster.setProfile(DriveConstants.BASE_LOCK_PROFILE_SLOT);
         m_leftMaster.changeControlMode(TalonControlMode.MotionMagic);

         m_rightMaster.setProfile(DriveConstants.BASE_LOCK_PROFILE_SLOT);
         m_rightMaster.changeControlMode(TalonControlMode.MotionMagic);

//         m_leftMaster.setPID(DriveConstants.MM_QUICK_P_GAIN, DriveConstants.MM_QUICK_I_GAIN, DriveConstants.MM_QUICK_D_GAIN, f_gain, 0, 0, DriveConstants.BASE_LOCK_PROFILE_SLOT);
//         m_rightMaster.setPID(DriveConstants.MM_QUICK_P_GAIN, DriveConstants.MM_QUICK_I_GAIN, DriveConstants.MM_QUICK_D_GAIN, f_gain, 0, 0, DriveConstants.BASE_LOCK_PROFILE_SLOT);
         if (p_quickTurn)
         {
            m_leftMaster.setMotionMagicAcceleration(350);  // RPM
            m_leftMaster.setMotionMagicCruiseVelocity(350);  // RPM
            m_leftMaster.setPID(DriveConstants.MM_QUICK_P_GAIN, DriveConstants.MM_QUICK_I_GAIN, DriveConstants.MM_QUICK_D_GAIN, DriveConstants.MM_QUICK_F_GAIN, 0, 0, DriveConstants.BASE_LOCK_PROFILE_SLOT);

            m_rightMaster.setMotionMagicAcceleration(350);  // RPM
            m_rightMaster.setMotionMagicCruiseVelocity(350);  // RPM
            m_rightMaster.setPID(DriveConstants.MM_QUICK_P_GAIN, DriveConstants.MM_QUICK_I_GAIN, DriveConstants.MM_QUICK_D_GAIN, DriveConstants.MM_QUICK_F_GAIN, 0, 0, DriveConstants.BASE_LOCK_PROFILE_SLOT);
         }
         else
         {
            m_leftMaster.setMotionMagicAcceleration(900);  // RPM
            m_leftMaster.setMotionMagicCruiseVelocity(800);  // RPM
            m_leftMaster.setPID(DriveConstants.MM_DRIVE_P_GAIN, DriveConstants.MM_DRIVE_I_GAIN, DriveConstants.MM_DRIVE_D_GAIN, DriveConstants.MM_DRIVE_F_GAIN, 0, 0, DriveConstants.BASE_LOCK_PROFILE_SLOT);

            m_rightMaster.setMotionMagicAcceleration(900);  // RPM
            m_rightMaster.setMotionMagicCruiseVelocity(800);  // RPM
            m_rightMaster.setPID(DriveConstants.MM_DRIVE_P_GAIN, DriveConstants.MM_DRIVE_I_GAIN, DriveConstants.MM_DRIVE_D_GAIN, DriveConstants.MM_DRIVE_F_GAIN, 0, 0, DriveConstants.BASE_LOCK_PROFILE_SLOT);
         }

         resetEncoders();
         
         m_driveMode = DriveType.MAGIC;

         setBrakeMode(true);
      }
   }

   public double getLeftSensorValue()
   {
      return m_leftMaster.getEncPosition();
   }
   
   public double getRightSensorValue()
   {
      return m_rightMaster.getEncPosition();
   }
   
   public void setMotionMagicTargetAbsolute(double p_leftTarget, double p_rightTarget)
   {
      m_leftMaster.set(p_leftTarget);
      m_rightMaster.set(p_rightTarget);
   }

   public void setMotionMagicTargetDelta(double p_leftDelta, double p_rightDelta)
   {
      m_leftMaster.set(m_leftMaster.getEncPosition() + p_leftDelta);
      m_rightMaster.set(m_rightMaster.getEncPosition() + p_rightDelta);
   }

   private void stopPathFollowing()
   {
      if (m_driveMode == DriveType.PATH)
      {
         abortFollowingPath();
         pathCleanup();
      }
   }

   public void setMotorSpeeds(DriveSignal p_signal)
   {
      // Set left and right speeds
      m_leftMaster.set(p_signal.leftMotor);
      m_rightMaster.set(p_signal.rightMotor);
   }

   public void setAutoGearMode()
   {
      stopPathFollowing();

      m_driveMode = DriveType.AUTO_GEAR_DROP;
      RobotTemplate.getVisionServer().startVideoLogging();

      setHighGear(true);
      m_gearDropFinished = false;
      
      // Reconfigure motor controllers
      m_leftMaster.changeControlMode(TalonControlMode.PercentVbus);
      m_rightMaster.changeControlMode(TalonControlMode.PercentVbus);
   }
   
   public void exitAutoGearMode()
   {
      if (RobotTemplate.getVisionServer() != null)
      {
         RobotTemplate.getVisionServer().stopVideoLogging();
      }
   }
   
   public void setPathFollowingMode()
   {

      m_driveMode = DriveType.PATH;

      // Configure motor controller modes for path following
      m_leftMaster.changeControlMode(TalonControlMode.MotionProfile);
      m_leftMaster.setProfile(0);

      m_rightMaster.changeControlMode(TalonControlMode.MotionProfile);
      m_rightMaster.setProfile(0);

      // Go as fast as possible
//      setHighGear(true);

      // Use brake mode to stop quickly at end of path, since Talons will put
      // output to neutral
      setBrakeMode(true);
   }
   
   public void resetEncoders()
   {
      m_leftMaster.setEncPosition(0);
      m_rightMaster.setEncPosition(0);
   }

   public void setOpenLoopDrive()
   {
      stopPathFollowing();

      m_driveMode = DriveType.CHEESY;

      m_gearDropFinished = true;
      
      setBrakeMode(false);
      
      // Reconfigure motor controllers
      m_leftMaster.changeControlMode(TalonControlMode.PercentVbus);
      m_rightMaster.changeControlMode(TalonControlMode.PercentVbus);
   }

   public void setRawDrive()
   {
      setOpenLoopDrive();

      m_driveMode = DriveType.RAW;

   }

   public void setFullBrakeMode()
   {
      stopPathFollowing();

      // Set talons to hold their current position
      if (m_driveMode != DriveType.FULL_BRAKE)
      {
         // Set up Talons to hold their current position as close as possible

         m_leftMaster.setProfile(DriveConstants.BASE_LOCK_PROFILE_SLOT);
         m_leftMaster.changeControlMode(TalonSRX.TalonControlMode.Position);
         m_leftMaster.setAllowableClosedLoopErr(DriveConstants.BRAKE_MODE_ALLOWABLE_ERROR);
         m_leftMaster.set(m_leftMaster.getPosition());

         m_rightMaster.setProfile(DriveConstants.BASE_LOCK_PROFILE_SLOT);
         m_rightMaster.changeControlMode(TalonSRX.TalonControlMode.Position);
         m_rightMaster.setAllowableClosedLoopErr(DriveConstants.BRAKE_MODE_ALLOWABLE_ERROR);
         m_rightMaster.set(m_rightMaster.getPosition());

         
         m_leftMaster.setPID(DriveConstants.BASE_LOCK_P_GAIN, DriveConstants.BASE_LOCK_I_GAIN, DriveConstants.BASE_LOCK_D_GAIN, DriveConstants.BASE_LOCK_F_GAIN, 0, 0, DriveConstants.BASE_LOCK_PROFILE_SLOT);
         m_rightMaster.setPID(DriveConstants.BASE_LOCK_P_GAIN, DriveConstants.BASE_LOCK_I_GAIN, DriveConstants.BASE_LOCK_D_GAIN, DriveConstants.BASE_LOCK_F_GAIN, 0, 0, DriveConstants.BASE_LOCK_PROFILE_SLOT);

         m_driveMode = DriveType.FULL_BRAKE;

         setBrakeMode(true);
      }
      setHighGear(false);
   }

   public void setPath(Path p_path)
   {
      if (m_pathFollower != null)
      {
         if (m_pathFollower.isActive())
         {
            throw new IllegalStateException("One path is already active!");
         }
      }

      m_pathFollower = new PathFollower(p_path, m_leftMaster, m_rightMaster);
   }

   public PathFollower getPathFollower()
   {
      return m_pathFollower;
   }

   public void startFollowingPath()
   {
      if (m_pathFollower == null)
      {
         throw new IllegalStateException("No path set");
      }

      if (m_pathFollower.isActive())
      {
         throw new IllegalStateException("Path is already active");
      }

      m_pathFollower.start();
   }

   public void abortFollowingPath()
   {
      if (m_pathFollower != null)
      {
         m_pathFollower.stop();
      }
   }

   public void pathCleanup()
   {
      if (m_pathFollower != null)
      {
         m_pathFollower.stop();
         m_pathFollower = null;
         writeDriveStatesToFile(DRIVER_STATES_FILENAME + pathNum++ + ".txt");
      }
   }

   @Override
   public String getName()
   {
      return "Drive Base";
   }
   

   public void writeDriveStatesToFile(String fileName)
   {
      BufferedWriter bw = null;
      FileWriter fw = null;

      try
      {
         fw = new FileWriter(fileName);
         bw = new BufferedWriter(fw);
         for (DriveState ds : driveStates)
         {
            bw.write(ds.toString());
         }
         System.out.println("Done");
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }

      try
      {
         if (bw != null)
         {
            bw.close();
         }
      }
      
      //373x45=>31 ft, 1 inch x 3 ft 9 inches
      // We got 23 ft 9 inches x 11 ft 4 inches
      // Our calculations were 7 ft 4 inches too short in the x direction, but
      // we were 7 ft 7 inches too long in the y direction
      catch (IOException ex)
      {
         ex.printStackTrace();
      }
      driveStates.clear();
      
   }

   public void setHeading(double newHeading)
   {
      m_headingValue = newHeading;
   }

   public void setThrottle(double newThrottle)
   {
      m_throttleValue = newThrottle;
   }
   
   
   
   public void setQuickTurn(boolean p_quickTurn)
   {
      m_quickTurn = p_quickTurn;
   }



   /**
    * Returns distance traveled since encoders were set to zero, in inches.
    * @return
    */
   public int getEncoderDistanceInches()
   {
      long leftTick = Math.abs(m_leftMaster.getEncPosition());
      long rightTick = Math.abs(m_rightMaster.getEncPosition());

      return (int) (((leftTick + rightTick) / 2) * TICKS_TO_INCHES);
   }



   
   
   
}
