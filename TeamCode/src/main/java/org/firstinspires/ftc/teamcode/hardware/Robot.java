package org.firstinspires.ftc.teamcode.hardware;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class Robot extends BaseHardware {
    public DriveTrain driveTrain = new DriveTrain();


    @Override
    public void init() {
        // Must set Hardware Map and telemetry before calling init
        driveTrain.hardwareMap = this.hardwareMap;
        driveTrain.telemetry = this.telemetry;
        driveTrain.init();

    }

    @Override
    public void init_loop() {
        driveTrain.init_loop();

    }

    @Override
    public void start() {
        driveTrain.start();

    }

    @Override
    public void loop() {
        driveTrain.loop();


    }



    @Override
    public void stop() {
        driveTrain.stop();

    }


}
