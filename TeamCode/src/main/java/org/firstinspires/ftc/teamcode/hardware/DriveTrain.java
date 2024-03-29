package org.firstinspires.ftc.teamcode.hardware;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.teamcode.common.Settings;
import org.firstinspires.ftc.teamcode.common.CommonLogic;

//@Disabled
public class DriveTrain extends BaseHardware {

    //for truning this is the tolerance of trun in degrees
    public static final int DRIVETRAIN_GyroHeadingTol = 3;
    public static final int DRIVETRAIN_ticsPerRev = Settings.REV_HD_20_MOTOR_TICKS_PER_REV;
    public static final double DRIVETRAIN_wheelDistPerRev = 4 * 3.14159;

    public static final double DRIVETRAIN_gearRatio = 20.0 / 20.0;
    public static final double DRIVETRAIN_ticsPerInch = DRIVETRAIN_ticsPerRev / DRIVETRAIN_wheelDistPerRev / DRIVETRAIN_gearRatio;
    public static final double DRIVETRAIN_DriveTolerInches = .25;
    public static final double DRIVETRAIN_NORMALSPEED = .75;
    public static final double DRIVEIVETRAIN_TURBOSPEED = 1.0;
    public static final double DRTRAIN_SLOWSPEED = .25;
    public static final double DRIVETRAIN_TURNSPEED = .4;

    // naj set constant for Gyro KP for driving straight
    public static final double chassis_KPGyroStraight = 0.02;
    private static final String TAGChassis = "8492-Chassis";
    public CommonGyro subGyro = new CommonGyro();
    // Declare OpMode members.
    private ElapsedTime runtime = new ElapsedTime();
    private int initCounter = 0;
    //current mode of operation for Chassis
    private Mode drivetrain_mode_Current = Mode.UNKNOWN;
    private boolean cmdComplete = true;
    private int cmdStartTime_mS = 0;
    private DcMotor LDM1 = null;
    private DcMotor LDM2 = null;
    private DcMotor RDM1 = null;
    private DcMotor RDM2 = null;
    private double LDM1Power = 0.0;
    private double LDM2Power = 0.0;
    private double RDM1Power = 0.0;
    private double RDM2Power = 0.0;
    private int TargetHeadingDeg = 0;
    private double TargetDistanceInches = 0.0;
    private double maxPower = 1.0;
    private double minPower = -1.0;


    //*********************************************************************************************
    /*
     * Code to run ONCE when the driver hits INIT
     */
    @Override
    public void init() {

        RDM1 = hardwareMap.dcMotor.get("RDM1");
        LDM1 = hardwareMap.dcMotor.get("LDM1");
        LDM2 = hardwareMap.dcMotor.get("LDM2");
        RDM2 = hardwareMap.dcMotor.get("RDM2");

        if (LDM1 == null) {
            telemetry.log().add("LDM1 is null...");
        }
        if (LDM2 == null) {
            telemetry.log().add("LDM2 is null...");
        }
        if (RDM1 == null) {
            telemetry.log().add("RDM1 is null...");
        }
        if (RDM2 == null) {
            telemetry.log().add("RDM2 is null...");
        }


        LDM1.setDirection(DcMotor.Direction.FORWARD);
        LDM2.setDirection(DcMotor.Direction.FORWARD);
        RDM1.setDirection(DcMotor.Direction.FORWARD);
        RDM2.setDirection(DcMotor.Direction.FORWARD);

        LDM1.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        LDM2.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        RDM1.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        RDM2.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        setMotorMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        LDM1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        LDM2.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        RDM1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        RDM2.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        subGyro.telemetry = telemetry;
        subGyro.hardwareMap = hardwareMap;
        subGyro.init();

        telemetry.addData("Chassis_Test", "Initialized");
        drivetrain_mode_Current = Mode.STOPPED;
        runtime.reset();

    }

    //*********************************************************************************************
    /*
     * Code to run REPEATEDLY after the driver hits INIT, but before they hit PLAY
     */
    @Override
    public void init_loop() {

        if (runtime.milliseconds() > 1000) {
            initCounter = initCounter + 1;
            telemetry.addData("Robot init time: ", initCounter);
            telemetry.update();
            runtime.reset();
        }
        subGyro.init_loop();
    }

    //*********************************************************************************************
    private void setMotorMode(DcMotor.RunMode newMode) {

        LDM1.setMode(newMode);
        RDM1.setMode(newMode);
        LDM2.setMode(newMode);
        RDM2.setMode(newMode);
    }

    //*********************************************************************************************
    public void setMotorMode_RUN_WITHOUT_ENCODER() {
        setMotorMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
    }

    //*********************************************************************************************
    public void DriveMotorEncoderReset() {

        LDM1.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        RDM1.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        LDM2.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        RDM2.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        setMotorMode_RUN_WITHOUT_ENCODER();
    }

    //*********************************************************************************************
    /*
     * Code to run ONCE when the driver hits PLAY
     */
    @Override
    public void start() {
        runtime.reset();
        subGyro.start();
    }

    //*********************************************************************************************
    /*
     * Code to run REPEATEDLY after the driver hits PLAY but before they hit STOP
     */
    @Override
    public void loop() {
        subGyro.loop();

        switch (drivetrain_mode_Current) {

            case STOPPED:
                doStopped();
                break;

            case DRIVE_BY_GYRO:
                doDrive();
                break;

            case TURN_BY_GYRO:
                doTurn();
                break;

            case TELEOP:
                // doTeleop was put directly into cmdTeleop
                //doTeleop();
                break;

            default:
                break;


        }
        // Show the elapsed game time and wheel power.
        // telemetry.addData("Status", "RUN Time: " + runtime.toString());
        telemetry.update();
         RobotLog.aa(TAGChassis,"Stage: "+ drivetrain_mode_Current );
        //RobotLog.aa(TAGChassis, "Runtime: " + runtime.seconds());
        //double inchesTraveled = Math.abs(getEncoderInches());
        //RobotLog.aa(TAGChassis, "loop targetinches: " + Math.abs(TargetDistanceInches - Chassis_DriveTolerInches));
        //RobotLog.aa(TAGChassis, "inchesTraveled: " + inchesTraveled);

    }

    //*********************************************************************************************
    public void doTeleop() {
        drivetrain_mode_Current = Mode.TELEOP;
        //Cap the power limit for the wheels
       double LDM1P = CommonLogic.CapValue(LDM1Power,
                minPower, maxPower);

        //Cap the power limit for the wheels
        double LDM2P = CommonLogic.CapValue(LDM2Power,
                minPower, maxPower);

        double RDM1P = CommonLogic.CapValue(RDM1Power,
                minPower, maxPower);

        double RDM2P = CommonLogic.CapValue(RDM2Power,
                minPower, maxPower);




        LDM1.setPower(LDM1P);
        RDM1.setPower(RDM1P);
        LDM2.setPower(LDM2P);
        RDM2.setPower(RDM2P);
        RobotLog.aa(TAGChassis, "doTeleop: LDM1Power =" + LDM1P + " RDM1Power =" + RDM1P +
                        " LDM2Power =" + LDM2P + " RDM2Power =" + RDM2P);
    }
    //*********************************************************************************************
    private void doStopped() {
        // This is different because here we are only stopping the drive wheels not ending the
        // program.
        RobotLog.aa(TAGChassis, "doStop:");

        // only make the trip out to the motors once.
        // on repeat trips don't bother with the trip out to the hardware
        if ((Math.abs(LDM1Power) + Math.abs(LDM2Power) + Math.abs(RDM1Power) + Math.abs(RDM2Power)) != 0.0) {
            LDM1Power = 0.0;
            LDM2Power = 0.0;
            RDM1Power = 0.0;
            RDM2Power = 0.0;

            TargetDistanceInches = 0.0;
            LDM1.setPower(LDM1Power);
            LDM2.setPower(LDM2Power);
            RDM1.setPower(RDM1Power);
            RDM2.setPower(RDM2Power);
            drivetrain_mode_Current = Mode.STOPPED;
        }
    }
    //*********************************************************************************************
    private void doDrive() {
/*
        // insert adjustments to drive straight using gyro
        RobotLog.aa(TAGChassis, "curr heading: " + subGyro.gyroNormalize(subGyro.getGyroHeading()));
        RobotLog.aa(TAGChassis, "Target: " + TargetHeadingDeg);

        double delta = -subGyro.deltaHeading(subGyro.gyroNormalize(subGyro.getGyroHeading()), TargetHeadingDeg);
        //double leftPower = TargetMotorPowerLeft - (delta * chassis_KPGyroStraight);
        //double rightPower = TargetMotorPowerRight + (delta * chassis_KPGyroStraight);


        leftPower = CommonLogic.CapValue(leftPower,
                Settings.REV_MIN_POWER, Settings.REV_MAX_POWER);

        rightPower = CommonLogic.CapValue(rightPower,
                Settings.REV_MIN_POWER, Settings.REV_MAX_POWER);

        RobotLog.aa(TAGChassis, "delta: " + delta);
        RobotLog.aa(TAGChassis, "leftpower: " + leftPower + " right " + rightPower);
        RobotLog.aa(TAGChassis, "Target Inches: " + Math.abs(TargetDistanceInches - DRIVETRAIN_DriveTolerInches));


        LDM1.setPower(leftPower);
        LDM2.setPower(leftPower);
        RDM1.setPower(rightPower);
        RDM2.setPower(rightPower);

        //check if we've gone far enough, if so stop and mark task complete
        double inchesTraveled = Math.abs(getEncoderInches());
        RobotLog.aa(TAGChassis, "Inches Traveled: " + inchesTraveled);
        if (inchesTraveled >= Math.abs(TargetDistanceInches - DRIVETRAIN_DriveTolerInches)) {
            RobotLog.aa(TAGChassis, "Target Inches: " + Math.abs(TargetDistanceInches - DRIVETRAIN_DriveTolerInches));
            RobotLog.aa(TAGChassis, "Inches Traveled: " + inchesTraveled);
            cmdComplete = true;
            doStopped();
        }


 */
    }    // doDrive()

    //*********************************************************************************************
    private void doTurn() {
        /*
         *   executes the logic of a single scan of turning the robot to a new heading
         */

        int currHeading = subGyro.gyroNormalize(subGyro.getGyroHeading());
        RobotLog.aa(TAGChassis, "Turn currHeading: " + currHeading + " target: " + TargetHeadingDeg);
        RobotLog.aa(TAGChassis, "Runtime: " + runtime.seconds());

        if (subGyro.gyroInTol(currHeading, TargetHeadingDeg, DRIVETRAIN_GyroHeadingTol)) {
            RobotLog.aa(TAGChassis, "Complete currHeading: " + currHeading);
            //We are there stop
            cmdComplete = true;
            drivetrain_mode_Current = Mode.STOPPED;
            doStopped();
        }
    }

    //*********************************************************************************************
    // create method to return complete boolean
    public boolean getcmdComplete() {
        return (cmdComplete);
    }

    //*********************************************************************************************
    // create command to be called from auton to drive straight
    public void cmdDriveByGyro(double DrivePower, int headingDeg, double targetDistanceInches) {
/*
        cmdComplete = false;
        if (drivetrain_mode_Current != Mode.DRIVE_BY_GYRO) {
            drivetrain_mode_Current = Mode.DRIVE_BY_GYRO;
        }
        TargetHeadingDeg = headingDeg;
        RobotLog.aa(TAGChassis, "cmdDriveByGyro: " + DrivePower);
        TargetMotorPowerLeft = DrivePower;
        TargetMotorPowerRight = DrivePower;
        TargetDistanceInches = targetDistanceInches;
        DriveMotorEncoderReset();
        drivetrain_mode_Current = Mode.DRIVE_BY_GYRO;
        //doDrive();

 */
    }

    //*********************************************************************************************
    public void cmdTurnByGyro(double L_Speed, double R_Speed, int headingDeg) {

        //can only be called one time per movement of the chassis
        drivetrain_mode_Current = Mode.TURN_BY_GYRO;
        TargetHeadingDeg = headingDeg;
        RobotLog.aa(TAGChassis, "cmdTurnByGyro target: " + TargetHeadingDeg);

        double lspeed = CommonLogic.CapValue(L_Speed,Settings.REV_MIN_POWER, Settings.REV_MAX_POWER);
        double rspeed = CommonLogic.CapValue(R_Speed, Settings.REV_MIN_POWER,Settings.REV_MAX_POWER);

        LDM1.setPower(lspeed);
        LDM2.setPower(lspeed);
        RDM1.setPower(rspeed);
        RDM2.setPower(rspeed);
        cmdComplete = false;
        runtime.reset();
        doTurn();
    }

    public void cmdTeleOp(double Left_Y, double Left_X, double Right_X) {
        cmdComplete = false;
        drivetrain_mode_Current = Mode.TELEOP;
       double Direction = Math.atan2(Left_X, Left_Y);
       double Magnitude = Math.sqrt( Left_X * Left_X + Left_Y * Left_Y );
       double Turn = Right_X;
        //The front-right and back-left wheel should be set to sin(angle−1/4π) * magnitude.
        //The front-left and back-right wheel should be set to sin(angle+1/4π) * magnitude.
        // https://seamonsters-2605.github.io/archive/mecanum/
       double Plus = Math.sin(Direction+(1/4 * Math.PI)) * Magnitude;
       double Minus = Math.sin(Direction-(1/4 * Math.PI)) * Magnitude;
        RobotLog.aa(TAGChassis, " Plus: " + Plus + " Minus: " + Minus + " Magnitude: " + Magnitude
        + " Direction: " + Direction);
       if (Math.abs(Plus) >= Math.abs(Minus)){
           LDM1Power = (Plus + Turn) / Math.abs(Plus);
           //RDM1Power = (-Minus + Turn) / Math.abs(Plus);
          LDM2Power = (Minus + Turn) / Math.abs(Plus);
           //RDM2Power = (-Plus + Turn) / Math.abs(Plus);
       }
       else {
           LDM1Power = (Plus + Turn) / Math.abs(Minus);
           //RDM1Power = (-Minus + Turn) / Math.abs(Minus);
           LDM2Power = (Minus + Turn) / Math.abs(Minus);
           //RDM2Power = (-Plus + Turn) / Math.abs(Minus);
       }

        RobotLog.aa(TAGChassis, "LDM1Power: " + LDM1Power +" LDM2Power: " + LDM2Power
                + " RDM1Power: " + RDM1Power +" RDM2Power: " + RDM2Power);
        RobotLog.aa(TAGChassis, "Left_X: " + Left_X +" Left_Y: " + Left_Y
                + " Right_X: " + Right_X );





        doTeleop();
    }

    //*********************************************************************************************

    //*********************************************************************************************
    public double getEncoderInches() {
        // create method to get inches driven in auton
        // read the values from the encoders
        // LDM1.getCurrentPosition()
        // convert that to inches
        // by dividing by ticksPerInch

        // average the distance traveled by each wheel to determine the distance travled by the
        // robot
        int totalitics = Math.abs(LDM1.getCurrentPosition()) +
                Math.abs(LDM2.getCurrentPosition()) +
                Math.abs(RDM1.getCurrentPosition()) +
                Math.abs(RDM2.getCurrentPosition());
        double averagetics = totalitics / 4;
        double inches = averagetics / DRIVETRAIN_ticsPerInch;

        return inches;
    }

    //*********************************************************************************************
    /*
     * Code to run ONCE after the driver hits STOP
     */
    @Override
    public void stop() {
        //only called at the end of the program and is meant to stop the motors and set them to Zero
        LDM1.setPower(0.0);
        LDM2.setPower(0.0);
        RDM1.setPower(0.0);
        RDM2.setPower(0.0);
        drivetrain_mode_Current = Mode.STOPPED;
        subGyro.stop();
    }

    //*********************************************************************************************
    public void setMaxPower(double newMax) {

        this.maxPower = Math.abs(newMax);
        this.minPower = Math.abs(newMax)* -1;
    }

    public enum Mode {
        STOPPED,
        DRIVE_BY_GYRO,
        TURN_BY_GYRO,
        TELEOP,
        UNKNOWN
    }
    //*********************************************************************************************
}
