package org.wildstang.year2019.subsystems.drive;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

import org.wildstang.year2019.robot.WSInputs;
import org.wildstang.framework.CoreUtils;
import org.wildstang.framework.core.Core;
import org.wildstang.framework.io.Input;
import org.wildstang.framework.io.inputs.AnalogInput;
import org.wildstang.framework.io.inputs.DigitalInput;
import org.wildstang.framework.logger.StateTracker;
import org.wildstang.year2017.subsystems.drive.DriveState;
import org.wildstang.framework.subsystems.Subsystem;
import org.wildstang.year2019.robot.CANConstants;
import org.wildstang.year2017.robot.RobotTemplate;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.SensorCollection;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.TalonSRXConfiguration;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/** This subsystem controls the drive wheels so that the robot can move
 * around.
 * 
 * TODO: factor input management and drive logic into separate classes somehow.
 * 
 * TODO: Factor the helper classes into core framework */
public class Drive implements Subsystem {

    int logCounter1 = 0;
    int logCounter2 = 0;
    int logCounter3 = 0;

    /** Status frame period controls how frequently the TalonSRX reports back with
     * its sensor status over the CANBus. This constant is in milliseconds. */
    //private static final int STATUS_FRAME_PERIOD = 10;
    //private static final double NEUTRAL_DEADBAND = 0.001;
    private static final int TIMEOUT = 100; // milliseconds

    // Parameterizing over left and right makes motor setup code DRYer
    private static final int LEFT = 0;
    private static final int RIGHT = 1;
    private static final int[] SIDES = {LEFT, RIGHT};
    private static final String[] SIDE_NAMES = {"left", "right"};
    private static final int[] MASTER_IDS = {CANConstants.LEFT_DRIVE_TALON,
                                             CANConstants.RIGHT_DRIVE_TALON};
    private static final int[][] FOLLOWER_IDS = {CANConstants.LEFT_DRIVE_VICTORS,
                                                 CANConstants.RIGHT_DRIVE_VICTORS};
    private int pathNum = 1;
    private static final String DRIVER_STATES_FILENAME = "/home/lvuser/drive_state_";
    /** Left and right Talon master controllers */
    private TalonSRX[] masters = new TalonSRX[2];
    /** Left and right pairs of Victor follower controllers */
    private VictorSPX[][] followers = new VictorSPX[2][2];

    /** Input to steer robot */
    private AnalogInput headingInput;
    /** Input to control forward-backward movement */
    private AnalogInput throttleInput;
    
    /** Button to control base lock mode */
    private DigitalInput baseLockInput;
    /** Button to control quick turn mode */
    private DigitalInput quickTurnInput;
    /** Button to control anti-turbo mode */
    private DigitalInput antiTurboInput;

    /** Keeps track of what kind of drive we're doing (e.g. cheesy drive vs path vs magic) */
    private DriveType driveMode;

    /** Counter used to space out updates in update() method */
    private int updateCounter;

    /** The highest speed either side of the drive has achieved */
    private double maxSpeedAchieved;

    /** This PathFollower helper activates when we're in path mode to follow paths */
    private PathFollower pathFollower;

    /** The Cheesy helper calculates the cheesy drive strategy */
    private CheesyDriveHelper cheesyHelper = new CheesyDriveHelper();

    // While this is really a temporary variable, declared here to prevent
    // constant stack allocation.
    // TODO: Determine whether this is premature optimization.
    private DriveSignal driveSignal;
    
    /** Drive State */
    private LinkedList<DriveState> driveStates = new LinkedList<DriveState>();

    //////////////////////////////////////////////////////
    // Commanded values
    /** The throttle value currently being commanded. */
    private double commandThrottle;
    /** The heading value currently being commanded. */
    private double commandHeading;
    /** True iff quick-turn is currently commanded. */
    private boolean commandQuickTurn;
    /** True iff raw mode is currently commanded. */
    private boolean commandRawMode = false;
    /** True iff antiturbo is currently commanded. */
    private boolean commandAntiTurbo = false;

    ///////////////////////////////////////////////////////////
    // PUBLIC METHODS

    public Drive() {
        /* Nothing to do in constructor. */
    }

    @Override
    public void init() {
        // TODO: set up logging DONE
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
        try {
            initMotorControllers();
        } catch (CoreUtils.CTREException e) {
            System.out.println("Failed to init drive motor controllers: " + e);
            // FIXME CRASH
        }

        initInputs();

        resetState();
    }

    @Override
    public void resetState() {
        // Encoders start at zero.
        resetEncoders();
        // Default drive mode is open-loop cheesy drive.
        setBrakeMode(false);
        setOpenLoopDrive();
        // We start at no throttle or steering.
        setThrottle(0);
        setHeading(0);
        
        // All features start disabled.
        commandQuickTurn = false;
        commandRawMode = false;
        commandAntiTurbo = false;

        maxSpeedAchieved = 0;
    }
    @Override
    public void inputUpdate(Input source) {

        if (source == throttleInput) {
            if (logCounter1 % 1000 == 0)
                System.out.println("Drive::inputUpdate::throttleInput: " + throttleInput.getValue());
            logCounter1++;
            
            setThrottle(throttleInput.getValue());
        } else if (source == headingInput) {
            if (logCounter2 % 1000 == 0)
                System.out.println("Drive::inputUpdate::headingInput: " + headingInput.getValue());
            logCounter2++;

            setHeading(headingInput.getValue());
        }
        // TODO: Do we want to make quickturn automatic?
        else if (source == quickTurnInput) {
            commandQuickTurn = quickTurnInput.getValue();
        } else if (source == antiTurboInput) {
            commandAntiTurbo = antiTurboInput.getValue();
        } else if (source == baseLockInput) {
            // TODO: determine if raw mode is necessary. If so, rename, since it's only used
            // in base lock mode. Merge raw and base lock? And then factor this block out of
            // inputUpdate.
            commandRawMode = baseLockInput.getValue();

            // Determine drive state override
            if (commandRawMode) {
                setFullBrakeMode();
            } else {
                setOpenLoopDrive();
                setHeading(0);
                setThrottle(0);
            }
        }
    }

    @Override
    public void selfTest() {
        // DO NOT IMPLEMENT
        // TODO WHY NOT?
    }

    @Override
    public void update() {
        
        // Update dashboard with statistics on motor performance
        // TODO: Is this redundant with logging machinery? (Not redundant?)
        if (updateCounter % 10 == 0) {
            for (int side : SIDES) {
                TalonSRX master = masters[side];
                double output = master.getMotorOutputPercent();
                SmartDashboard.putNumber(SIDE_NAMES[side] + " output", output);
                double speed = master.getSelectedSensorVelocity();
                SmartDashboard.putNumber(SIDE_NAMES[side] + " speed", speed);
                double error = master.getClosedLoopError();
                SmartDashboard.putNumber(SIDE_NAMES[side] + " error", error);
                double target = master.getClosedLoopTarget();
                SmartDashboard.putNumber(SIDE_NAMES[side] + " target", target);
            }
            
            SmartDashboard.putNumber("commandThrottle", commandThrottle);
            SmartDashboard.putNumber("commandHeading", commandHeading);
            SmartDashboard.putString("Drive mode", driveMode.name());
        }



        switch (driveMode) {
        case PATH:
            /* FIXME re-enable this
            collectDriveState(); */
        break;

        
        case CHEESY:
            double effectiveThrottle = commandThrottle;
            if (commandAntiTurbo) {
                effectiveThrottle = commandThrottle * DriveConstants.ANTI_TURBO_FACTOR;
            }

            if (logCounter3 % 1000 == 0) {
            System.out.println("Drive::update::commandAntiTurbo::effectiveThrottle: " + effectiveThrottle);
            System.out.println("Drive::update::commandAntiTurbo::commandHeading: " + commandHeading);
            }
            logCounter3++;
            SmartDashboard.putBoolean("Quick Turn", commandQuickTurn);
            driveSignal = cheesyHelper.cheesyDrive(effectiveThrottle, commandHeading, commandQuickTurn);

            SmartDashboard.putNumber("driveSignal.left", driveSignal.leftMotor);
            SmartDashboard.putNumber("driveSignal.right", driveSignal.rightMotor);
            
            setMotorSpeeds(driveSignal);

            //  FIXME re-enable this (FIXED)
            if (RobotTemplate.LOG_STATE) {
                maxSpeedAchieved = Math.max(maxSpeedAchieved,
                        Math.max(Math.abs(masters[LEFT].getSelectedSensorVelocity()),
                                Math.abs(masters[RIGHT].getSelectedSensorVelocity())));

                SmartDashboard.putNumber("Max Encoder Speed", maxSpeedAchieved);
            }
        break;
        case FULL_BRAKE:
        break;
        case MAGIC:
        break;
        case RAW:
        default:
            // Raw is default
            driveSignal = new DriveSignal(commandThrottle, commandThrottle);
        break;
        }
        SensorCollection leftEncoder = masters[LEFT].getSensorCollection();
        SmartDashboard.putNumber("Left Encoder", leftEncoder.getQuadraturePosition());
        SensorCollection rightEncoder = masters[RIGHT].getSensorCollection();
        SmartDashboard.putNumber("Right Encoder", rightEncoder.getQuadraturePosition());

        if (RobotTemplate.LOG_STATE) {
            StateTracker tracker = Core.getStateTracker();
            tracker.addState("Drive heading", "Drive", commandHeading);
            tracker.addState("Drive throttle", "Drive", commandThrottle);
            // tracker.addState("Vision distance", "Drive", m_visionDistance); # enable when vision is done
            // tracker.addState("Vision correction", "Drive", m_visionXCorrection); # enable when vision is done

            String[] sides = {"Left ", "Right "};
            for (int i = 0; i < masters.length; i++) {
                TalonSRX master = masters[i];
                VictorSPX[] follower = followers[i];
                String side = sides[i];
                tracker.addState(side + "output", "Drive", master.getMotorOutputPercent());
                tracker.addState(side + "speed (RPM)", "Drive", master.getSelectedSensorVelocity());
                tracker.addState(side + "1 voltage", "Drive", master.getMotorOutputVoltage());
                //tracker.addState(side + "2 voltage", "Drive", followers.getMotorOutputVoltage());
                tracker.addState(side + "1 current", "Drive", master.getOutputCurrent());
                //tracker.addState(side + "2 current", "Drive", followers.getOutputCurrent());
            }
        }
        updateCounter += 1;
    }

    @Override
    public String getName() {
        return "Drive Base";
    }

    /** Reset drive encoders back to zero */
    public void resetEncoders() {
        for (TalonSRX master : masters) {
            // FIXME CHECK ERROR CODE
            master.getSensorCollection().setQuadraturePosition(0, 10);
        }
    }

    /** Set brake mode when in neutral for all drive motors: true to brake, false to coast */
    public void setBrakeMode(boolean brake) {
        NeutralMode mode = brake? NeutralMode.Brake : NeutralMode.Coast;
        for (int side : SIDES) {
            masters[side].setNeutralMode(mode);
            for (VictorSPX follower : followers[side]) {
                follower.setNeutralMode(mode);
            }
        }
    }
    
    /** Active stop --- stop drive motion immediately. */
    public void setFullBrakeMode() {
        stopPathFollowing();

        // Set talons to hold their current position
        if (driveMode != DriveType.FULL_BRAKE) {
            // Set up Talons to hold their current position as close as possible
            for (TalonSRX master : masters) {
                master.selectProfileSlot(DrivePID.BASE_LOCK.slot, 0);
                master.set(ControlMode.Position, master.getSelectedSensorPosition());
            }

            driveMode = DriveType.FULL_BRAKE;
            setBrakeMode(true);
        }
    }

    /** Begin to follow the given path. */
    public void setPath(Path p_path) {
        if (pathFollower != null) {
            if (pathFollower.isActive()) {
                throw new IllegalStateException("One path is already active!");
            }
        }

        pathFollower = new PathFollower(p_path, masters[LEFT], masters[RIGHT]);
    }

    public PathFollower getPathFollower() {
        return pathFollower;
    }

    public void startFollowingPath() {
        if (pathFollower == null) {
            throw new IllegalStateException("No path set");
        }

        if (pathFollower.isActive()) {
            throw new IllegalStateException("Path is already active");
        }
    }


    /** Stop following and clean up path. FIXED? */
    public void pathCleanup() {
        if (pathFollower != null) {
            pathFollower.stop();
            pathFollower = null;
            // FIXME fix the next two lines FIXED
            writeDriveStatesToFile(DRIVER_STATES_FILENAME + pathNum + ".txt");
        }
    }
    
    public void writeDriveStatesToFile(String fileName) {
        BufferedWriter bw = null;
        FileWriter fw = null;

        try {
            fw = new FileWriter(fileName);
            bw = new BufferedWriter(fw);
            for (DriveState ds : driveStates) {
                bw.write(ds.toString());
            }
            System.out.println("Done");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if (bw != null) {
                bw.close();
            }
        }

        catch (IOException ex) {
            ex.printStackTrace();
        }
        driveStates.clear();

    }

    /** Stop following this path. 
     * 
     * FIXME this is weirdly redundant with pathCleanup --- something is wrong 
     * The code IS redundant with pathCleanup, do we remove this whole method or do we remove the
     * "pathFollower.stop();" from "abortFollowingPath()"?
     */
    public void abortFollowingPath() {
        if (pathFollower != null) {
            pathFollower.stop();
        }
    }

    // TODO copy in the rest of path functionality Fixed?

    /** Switch to cheesy drive. */
    public void setOpenLoopDrive() {
        stopPathFollowing();

        driveMode = DriveType.CHEESY;

        setBrakeMode(false);

        // Zero motor output
        for (TalonSRX master : masters) {
            master.set(ControlMode.PercentOutput, 0);
        }
    }

    public void setHeading(double heading) {
        this.commandHeading = heading;
    }

    public void setThrottle(double throttle) {
        this.commandThrottle = throttle;
    }

    public void setQuickTurn(boolean quickTurn) {
        this.commandQuickTurn = false;
    }
    

    /////////////////////////////////////////////////////////
    // PRIVATE METHODS

    /** Set up our input members and subscribe to inputUpdate events */
    private void initInputs() {
        // Set and subscribe to inputs
        headingInput = (AnalogInput) Core.getInputManager().getInput(WSInputs.DRIVE_HEADING);
        headingInput.addInputListener(this);
        throttleInput = (AnalogInput) Core.getInputManager().getInput(WSInputs.DRIVE_THROTTLE);
        throttleInput.addInputListener(this);
        
        quickTurnInput = (DigitalInput) Core.getInputManager()
                .getInput(WSInputs.QUICK_TURN.getName());
        quickTurnInput.addInputListener(this);

        antiTurboInput = (DigitalInput) Core.getInputManager()
                .getInput(WSInputs.ANTITURBO.getName());
        antiTurboInput.addInputListener(this);

        baseLockInput = (DigitalInput) Core.getInputManager()
                .getInput(WSInputs.BASE_LOCK.getName());
        baseLockInput.addInputListener(this);
    }

    /** Initialize all drive base motor controllers. */
    private void initMotorControllers() throws CoreUtils.CTREException {
        for (int side : SIDES) {
            masters[side] = new TalonSRX(MASTER_IDS[side]);

            initMaster(side, masters[side]);

            for (int i = 0; i < FOLLOWER_IDS[side].length; ++i) {
                followers[side][i] = new VictorSPX(FOLLOWER_IDS[side][i]);
                initFollower(side, followers[side][i]);
            }
        }
    }

    /** Initialize this master controller.
     *
     * <p>See https://github.com/CrossTheRoadElec/Phoenix-Examples-Languages/ for
     * examples on how to set up a Talon.
     *
     * @param side Which side (LEFT or RIGHT) this is master for
     * @param master The WSTalonSRX object to set up */
    private void initMaster(int side, TalonSRX master) throws CoreUtils.CTREException {
        System.out.println("Initializing TalonSRX master ID " + MASTER_IDS[side]);

        // The Talon SRX should be directly connected to an encoder
        master.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, TIMEOUT);

        if (side == LEFT) {
            master.setInverted(DriveConstants.LEFT_DRIVE_INVERTED);
            master.setSensorPhase(DriveConstants.LEFT_DRIVE_SENSOR_PHASE);
        } else {
            master.setInverted(DriveConstants.RIGHT_DRIVE_INVERTED);
            master.setSensorPhase(DriveConstants.RIGHT_DRIVE_SENSOR_PHASE);
        }

        // Configure output to range from full-forward to full-reverse.
        CoreUtils.checkCTRE(master.configNominalOutputForward(0, TIMEOUT));
        CoreUtils.checkCTRE(master.configNominalOutputReverse(0, TIMEOUT));
        CoreUtils.checkCTRE(master.configPeakOutputForward(+1.0, TIMEOUT));
        CoreUtils.checkCTRE(master.configPeakOutputReverse(-1.0, TIMEOUT));

        // Load all the PID settings.
        for (DrivePID pid : DrivePID.values()) {
            master.config_kF(pid.slot, pid.k.f);
            master.config_kP(pid.slot, pid.k.p);
            master.config_kI(pid.slot, pid.k.i);
            master.config_kD(pid.slot, pid.k.d);
        }
        
        // Special case for base lock: we widen the deadband
        CoreUtils.checkCTRE(master.configAllowableClosedloopError(DrivePID.BASE_LOCK.slot,
                DriveConstants.BRAKE_MODE_ALLOWABLE_ERROR));
        
        // Coast is a reasonable default neutral mode. TODO: is it really?
        master.setNeutralMode(NeutralMode.Coast);

        TalonSRXConfiguration master_config = new TalonSRXConfiguration();
        master.getAllConfigs(master_config, TIMEOUT);
        System.out.print(master_config.toString("drive talon " + SIDE_NAMES[side]));
    }

    private void initFollower(int side, VictorSPX follower) {
        TalonSRX master = masters[side];
        if (side == LEFT) {
            follower.setInverted(DriveConstants.LEFT_DRIVE_INVERTED);
        } else {
            follower.setInverted(DriveConstants.RIGHT_DRIVE_INVERTED);
        }
        follower.follow(master);
        // TODO should neutral mode on followers ever change?
        follower.setNeutralMode(NeutralMode.Coast);
    }

    private void stopPathFollowing() {
        if (driveMode == DriveType.PATH) {
            abortFollowingPath();
            pathCleanup();
        }
    }

    private void setMotorSpeeds(DriveSignal speeds) {
        masters[LEFT].set(ControlMode.PercentOutput, speeds.leftMotor);
        masters[RIGHT].set(ControlMode.PercentOutput, speeds.rightMotor);

    }
}
