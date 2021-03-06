package org.firstinspires.ftc.teamcode.repeaters;

import android.graphics.Bitmap;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import com.qualcomm.robotcore.hardware.DcMotorSimple;

import com.qualcomm.robotcore.util.ElapsedTime;

import for_camera_opmodes.LinearOpModeCamera;


import static java.lang.Math.abs;

/**
 * Created by MichaelL on 4/18/17.
 */

@Autonomous(name="Repeaters AutoBlueBeacon", group="Repeaters")
public class RepeatersAutoBlueBeacon extends LinearOpModeCamera{
    private static final double     DRIVE_SPEED             = 0.4;
    /* Declare OpMode members. */
    private RepeatersHardware robot = new RepeatersHardware();   // Use a Pushbot's hardware
    private ElapsedTime runtime = new ElapsedTime();
    private double initialLightIntensity = 0.0;
    private static final int ds2 = 2;
    private static final double     WHITE_THRESHOLD = 0.03;
    private static final double COLOR_DIFF_THRESHOLD = 5000000;

    public boolean red = false;

    public enum Color {
        RED, BLUE, UNSURE
    }

    @Override
    public void runOpMode() {

        /*
         * Initialize the drive system variables.
         * The init() method of the hardware class does all the work here
         */
        robot.init(hardwareMap);


        robot.rightlightSensor.enableLed(true);
        sleep(500);

        initialLightIntensity = robot.rightlightSensor.getLightDetected();

        // Send telemetry message to signify robot waiting;
        telemetry.addData("Status", "Ready to run");    //
        telemetry.update();

        // Wait for game start (driver press PLAY)
        waitForStart();
        startCamera();

        // Step 1:  Drive forward
        robot.rightMotor.setPower(1);
        robot.leftMotor.setPower(1);
        runtime.reset();
        while (opModeIsActive() && (runtime.seconds() < 1.45
        )) {
            telemetry.addData("Path", "Leg 1: %2.5f S Elapsed", runtime.seconds());
            telemetry.update();
        }
        // Step 2:  use rightmotor rotate robot left (30deg?) align with playingfieldwall
        robot.rightMotor.setPower(1);
        robot.leftMotor.setPower(0);
        runtime.reset();
        while (opModeIsActive() && (runtime.seconds() < 0.24)) {
            telemetry.addData("Path", "Leg 2: %2.5f S Elapsed", runtime.seconds());
            telemetry.update();
        }

        // Step 3:  Drive forward to search for white line and press beacon
        findWhiteLine(6);
        robot.leftMotor.setPower(-0.05);
        robot.rightMotor.setPower(-0.05);
        while (opModeIsActive() && (runtime.seconds() < 0.1)) {
            telemetry.addData("Path", "Leg 5: %2.5f S Elapsed", runtime.seconds());
            telemetry.update();
        }
        pushButton();

        // Step 4: Driver forward again -repeat
        findWhiteLine(6);
        pushButton();

        // Step 5: move to position for rotation
        robot.leftMotor.setPower(1);
        robot.rightMotor.setPower(1);
        while (opModeIsActive() && (runtime.seconds() < 0.5)) {
            telemetry.addData("Path", "Leg 5: %2.5f S Elapsed", runtime.seconds());
            telemetry.update();
        }
        // Step 6: rotate (45deg?) to shoot particle
        robot.leftMotor.setPower(1);
        robot.rightMotor.setPower(-1);
        while (opModeIsActive() && (runtime.seconds() < 0.3)) {
            telemetry.addData("Path", "Leg 6: %2.5f S Elapsed", runtime.seconds());
            telemetry.update();
        }
        // Step 7: move forward(backward) to shoot
        robot.leftMotor.setPower(-1);
        robot.rightMotor.setPower(-1);
        while (opModeIsActive() && (runtime.seconds() < 0.3)) {
            telemetry.addData("Path", "Leg 7: %2.5f S Elapsed", runtime.seconds());
            telemetry.update();
        }
        // Step 8: shoot particle
        robot.leftMotor.setPower(0);
        robot.rightMotor.setPower(0);
        robot.flickerMotor.setPower(-1);
        while (opModeIsActive() && (runtime.seconds() < 1)) {
            telemetry.addData("Path", "Leg 8: %2.5f S Elapsed", runtime.seconds());
            telemetry.update();
        }
    }
    private void findWhiteLine(double timeout) {

        runtime.reset();

        sleep(500);
        robot.move(red ? -DRIVE_SPEED * 0.25 : DRIVE_SPEED * 0.25, red ? -DRIVE_SPEED * 0.25 : DRIVE_SPEED * 0.25);
        while (opModeIsActive() && (robot.rightlightSensor.getLightDetected() < initialLightIntensity + WHITE_THRESHOLD) && (runtime.seconds() < timeout)) {
            // Display the light level while we are looking for the line
            telemetry.addData("Light Level: ",  robot.rightlightSensor.getLightDetected());
            telemetry.update();
        }
        for (DcMotorSimple motor: robot.allMotors) {
            motor.setPower(0);
        }
    }
        /*/robot.leftMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        robot.rightMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        while (opModeIsActive() && (robot.lightSensor.getLightDetected() < initialLightIntensity + WHITE_THRESHOLD) && (runtime.seconds() < timeout)) {
            // Display the light level while we are looking for the line
            telemetry.addData("Light Level: ",  robot.lightSensor.getLightDetected());
            telemetry.update();
        }
        /*/
    private void pushButton() {

            ramSequence();
            Color color = getColor();

            while ((color == Color.UNSURE || ((color == Color.RED) ^ red)) && opModeIsActive()) {
                sleep(500);
                ramSequence();
                sleep(1000);
                color = getColor();
            }
        }

    private void ramSequence() {
        performActionWithDuration(() -> {
            robot.autobeaconServo.setPower(1);
        }, 1.5, "Ram");

        performActionWithDuration(() -> {
            robot.autobeaconServo.setPower(-1);
        }, 1.5, "Unram");

        robot.autobeaconServo.setPower(0.05);

    }

    private Color getColor() {
        boolean finished = false;
        Color detectedColor = Color.UNSURE;

        while (opModeIsActive() && !finished) {
            if (imageReady()) { // only do this if an image has been returned from the camera
                int redValue = 0;
                int blueValue = 0;

                // get image, rotated so (0,0) is in the bottom left of the preview window
                Bitmap rgbImage;
                rgbImage = convertYuvImageToRgb(yuvImage, width, height, ds2);

                // 480 x 640
                for (int x = 0; x < rgbImage.getWidth(); x++) {
                    for (int y = 0; y < rgbImage.getHeight(); y++) {
                        int pixel = rgbImage.getPixel(x, y);
                        redValue += red(pixel);
                        blueValue += blue(pixel);
                    }
                }

                if (abs(blueValue - redValue) > COLOR_DIFF_THRESHOLD) {
                    detectedColor = blueValue > redValue ? Color.BLUE : Color.RED;
                }
                finished = true;
                telemetry.addData("B/R", blueValue + " " + redValue);
                telemetry.addData("Color:", detectedColor);
                telemetry.update();
            } else {
                telemetry.addData("Color:", "Getting");
                telemetry.update();
            }
        }
        return detectedColor;
    }
        interface RobotAction {
        void performAction();
        }

        private void performActionWithDuration(RobotAction action, double duration, String description) {
        action.performAction();
        runtime.reset();
        while (opModeIsActive() && (runtime.seconds() < duration)) {
            telemetry.addData(description, "%2.5f S Elapsed", runtime.seconds());
            telemetry.update();
        }
        for (DcMotorSimple motor: robot.allMotors) {
            motor.setPower(0);
        }
    }
}

