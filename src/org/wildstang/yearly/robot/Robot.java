package org.wildstang.yearly.robot;
/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.wildstang.framework.auto.AutoManager;
import org.wildstang.framework.core.Core;
import org.wildstang.framework.logger.StateLogger;
import org.wildstang.framework.timer.ProfilingTimer;
import org.wildstang.hardware.crio.RoboRIOInputFactory;
import org.wildstang.hardware.crio.RoboRIOOutputFactory;
import org.wildstang.yearly.auto.programs.CornerShot;
import org.wildstang.yearly.auto.programs.CrossingDefense;
import org.wildstang.yearly.auto.programs.DriveAtp1;
import org.wildstang.yearly.auto.programs.DriveAtp10;
import org.wildstang.yearly.auto.programs.DriveAtp2;
import org.wildstang.yearly.auto.programs.DriveAtp3;
import org.wildstang.yearly.auto.programs.DriveAtp4;
import org.wildstang.yearly.auto.programs.DriveAtp5;
import org.wildstang.yearly.auto.programs.DriveAtp6;
import org.wildstang.yearly.auto.programs.DriveAtp7;
import org.wildstang.yearly.auto.programs.DriveAtp8;
import org.wildstang.yearly.auto.programs.DriveAtp9;
import org.wildstang.yearly.auto.programs.FunctionTest;
import org.wildstang.yearly.auto.programs.LowBarOneBall;
import org.wildstang.yearly.auto.programs.VisionTest;
import org.wildstang.yearly.subsystems.DriveBase;
import org.wildstang.yearly.subsystems.Intake;
import org.wildstang.yearly.subsystems.Shooter;

//import com.ni.vision.NIVision.Image;

import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

//import edu.wpi.first.wpilibj.Watchdog;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot
{

   private static long lastCycleTime = 0;
   private static int session;
   //private static Image frame;
   private StateLogger m_stateLogger = null;
   private Core m_core = null;
   private static Logger s_log = Logger.getLogger(Robot.class.getName());
   
   private boolean exceptionThrown = false;
   
   private boolean firstRun = true;
   private boolean AutoFirstRun = true;

   static boolean teleopPerodicCalled = false;
   
   CameraServer server;

   private void startloggingState()
   {
      Writer outputWriter = null;

      outputWriter = getFileWriter();
//       outputWriter = getNetworkWriter("10.1.11.12", 17654);

      m_stateLogger.setWriter(outputWriter);
      m_stateLogger.start();
      Thread t = new Thread(m_stateLogger);
      t.start();
   }

   private Writer getNetworkWriter(String ipAddress, int port)
   {
      BufferedWriter output = null;

      try
      {
         Socket socket = new Socket(ipAddress, port);
         output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
      }
      catch (UnknownHostException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      catch (IOException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

      return output;
   }

   private FileWriter getFileWriter()
   {
      FileWriter output = null;

      try
      {
    	  File outputFile;
    	  String osname = System.getProperty("os.name");
          if (osname.startsWith("Windows"))
          {
        	  outputFile = new File("./../../log.txt");
          }
          else if (osname.startsWith("Mac"))
          {
        	  outputFile = new File ("./../../log.txt");
          }
          else
          {
        	  outputFile = new File("/home/lvuser/log.txt");
          }
          if (outputFile.exists())
         {
            outputFile.delete();
         }
         outputFile.createNewFile();
         output = new FileWriter(outputFile);
      }
      catch (IOException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

      return output;
   }

   public void testInit()
   {

   }

   /**
    * This function is run when the robot is first started up and should be used
    * for any initialization code.
    */
   public void robotInit()
   {
      startupTimer.startTimingSection();

      m_core = new Core(RoboRIOInputFactory.class, RoboRIOOutputFactory.class);
      m_stateLogger = new StateLogger(Core.getStateTracker());

      // Load the config
      loadConfig();

      // Create application systems
      m_core.createInputs(WSInputs.values());
      // m_core.createInputs(SwerveInputs.values());
      m_core.createOutputs(WSOutputs.values());

      // 1. Add subsystems
      m_core.createSubsystems(WSSubsystems.values());

//      startloggingState();

      // 2. Add Auto programs
//      AutoManager.getInstance().addProgram(new OneBallMoatRampart());
//      AutoManager.getInstance().addProgram(new MotionProfileTest());
//      AutoManager.getInstance().addProgram(new OneBallMoatRampart());
//      AutoManager.getInstance().addProgram(new HarpoonAuto());
//      AutoManager.getInstance().addProgram(new TwoBall());
      AutoManager.getInstance().addProgram(new CrossingDefense());
//      AutoManager.getInstance().addProgram(new SpyShotCross());
//      AutoManager.getInstance().addProgram(new CornerAvoid());
      AutoManager.getInstance().addProgram(new LowBarOneBall());
//      AutoManager.getInstance().addProgram(new LowBarLowGoal());
      AutoManager.getInstance().addProgram(new VisionTest());
      AutoManager.getInstance().addProgram(new CornerShot());
      AutoManager.getInstance().addProgram(new FunctionTest());
      AutoManager.getInstance().addProgram(new DriveAtp1());
      AutoManager.getInstance().addProgram(new DriveAtp2());
      AutoManager.getInstance().addProgram(new DriveAtp3());
      AutoManager.getInstance().addProgram(new DriveAtp4());
      AutoManager.getInstance().addProgram(new DriveAtp5());
      AutoManager.getInstance().addProgram(new DriveAtp6());
      AutoManager.getInstance().addProgram(new DriveAtp7());
      AutoManager.getInstance().addProgram(new DriveAtp8());
      AutoManager.getInstance().addProgram(new DriveAtp9());
      AutoManager.getInstance().addProgram(new DriveAtp10());
      
      s_log.logp(Level.ALL, this.getClass().getName(), "robotInit", "Startup Completed");
      startupTimer.endTimingSection();
      
      //server = CameraServer.getInstance();
      //server.setQuality(15);
      //server.setSize(2);
      //server.startAutomaticCapture("cam0");

   }

   private void loadConfig()
   {
	   File configFile;
	   String osname = System.getProperty("os.name");
       if (osname.startsWith("Windows"))
       {
    	   configFile = new File("./Config/ws_config.txt");
       }
       else if (osname.startsWith("Mac"))
       {
    	   configFile = new File("./Config/ws_config.txt");
       }
       else
       {
    	   configFile = new File("/ws_config.txt");
       }

      BufferedReader reader = null;

      try
      {
         reader = new BufferedReader(new FileReader(configFile));
         Core.getConfigManager().loadConfig(reader);

      }
      catch (FileNotFoundException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

      if (reader != null)
      {
         try
         {
            reader.close();
         }
         catch (IOException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }
   }

   ProfilingTimer durationTimer = new ProfilingTimer("Periodic method duration", 50);
   ProfilingTimer periodTimer = new ProfilingTimer("Periodic method period", 50);
   ProfilingTimer startupTimer = new ProfilingTimer("Startup duration", 1);
   ProfilingTimer initTimer = new ProfilingTimer("Init duration", 1);

   public void disabledInit()
   {
      initTimer.startTimingSection();
      AutoManager.getInstance().clear();

      loadConfig();

      Core.getSubsystemManager().init();

      initTimer.endTimingSection();
      s_log.logp(Level.ALL, this.getClass().getName(), "disabledInit", "Disabled Init Complete");

   }

   public void disabledPeriodic()
   {
      // If we are finished with teleop, finish and close the log file
      ((DriveBase) Core.getSubsystemManager().getSubsystem(WSSubsystems.DRIVE_BASE.getName())).stopStraightMoveWithMotionProfile();
      if (teleopPerodicCalled)
      {
         m_stateLogger.stop();
      }
      AutoFirstRun = true;
      firstRun = true;
   }

   public void autonomousInit()
   {
      Core.getSubsystemManager().init();

      m_core.setAutoManager(AutoManager.getInstance());
      AutoManager.getInstance().startCurrentProgram();
   }

   /**
    * This function is called periodically during autonomous
    */
   public void autonomousPeriodic()
   {
      // Update all inputs, outputs and subsystems

      m_core.executeUpdate();

      if (AutoFirstRun)
      {
         ((DriveBase) Core.getSubsystemManager().getSubsystem(WSSubsystems.DRIVE_BASE.getName())).resetLeftEncoder();
         ((DriveBase) Core.getSubsystemManager().getSubsystem(WSSubsystems.DRIVE_BASE.getName())).resetRightEncoder();
         ((DriveBase) Core.getSubsystemManager().getSubsystem(WSSubsystems.DRIVE_BASE.getName())).setSuperDriveOverride(true);
         AutoFirstRun = false;
      }
   }

   public void teleopInit()
   {
      // Remove the AutoManager from the Core
      m_core.setAutoManager(null);

      Core.getSubsystemManager().init();
      
      DriveBase driveBase = ((DriveBase) Core.getSubsystemManager().getSubsystem(WSSubsystems.DRIVE_BASE.getName()));
      
      driveBase.stopStraightMoveWithMotionProfile();

      periodTimer.startTimingSection();
   }
   
   /**
    * This function is called periodically during operator control
    */
   public void teleopPeriodic()
   {
      if (firstRun)
      {
         ((Shooter) Core.getSubsystemManager().getSubsystem(WSSubsystems.SHOOTER.getName())).shooterOverride(false);
         ((Intake) Core.getSubsystemManager().getSubsystem(WSSubsystems.INTAKE.getName())).setIntakeOverrideOn(false);
         ((Intake) Core.getSubsystemManager().getSubsystem(WSSubsystems.INTAKE.getName())).setShotOverride(false);
         ((DriveBase) Core.getSubsystemManager().getSubsystem(WSSubsystems.DRIVE_BASE.getName())).resetLeftEncoder();
         ((DriveBase) Core.getSubsystemManager().getSubsystem(WSSubsystems.DRIVE_BASE.getName())).resetRightEncoder();
         ((DriveBase) Core.getSubsystemManager().getSubsystem(WSSubsystems.DRIVE_BASE.getName())).setSuperDriveOverride(false);
         ((DriveBase) Core.getSubsystemManager().getSubsystem(WSSubsystems.DRIVE_BASE.getName())).stopStraightMoveWithMotionProfile();
         ((DriveBase) Core.getSubsystemManager().getSubsystem(WSSubsystems.DRIVE_BASE.getName())).setLeftDrive(0);
         firstRun = false;
      }

      try{
      teleopPerodicCalled = true;

      long cycleStartTime = System.currentTimeMillis();
      // System.out.println("Cycle separation time: " + (cycleStartTime -
      // lastCycleTime));

      // Update all inputs, outputs and subsystems
      m_core.executeUpdate();

      /*
       * try { NIVision.IMAQdxGrab(session, frame, 1);
       * CameraServer.getInstance().setImage(frame); } catch(Exception e){}
       */

      long cycleEndTime = System.currentTimeMillis();
      long cycleLength = cycleEndTime - cycleStartTime;
      // System.out.println("Cycle time: " + cycleLength);
      lastCycleTime = cycleEndTime;
      // Watchdog.getInstance().feed();
      }
      catch(Throwable e)
      {
         SmartDashboard.putString("Exception thrown", e.toString());
         exceptionThrown = true;
         throw e;
      }
      finally
      {
         SmartDashboard.putBoolean("ExceptionThrown", exceptionThrown);
      }
   }

   /**
    * This function is called periodically during test mode
    */
   public void testPeriodic()
   {
      // Watchdog.getInstance().feed();
   }
}
