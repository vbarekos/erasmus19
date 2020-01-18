// Erasmus+ 2019-2020
// 2o Gymnasio Samou, GREECE
// Christmas programming challenge: EV3 Colour Sorter of National flags' coloured bricks
// GROUP: TEAM_3

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class finalBELT {

	// Unregulated EV3 Large Motor for moving the belt
	private static EV3LargeRegulatedMotor beltMotor;

	// Create a motor object to control the Medium Motor that is used as a servo
	private static EV3MediumRegulatedMotor servoMotor;

	// Colour Sensor for reading colours
	private static EV3ColorSensor ColorSensor;

	// Touch Sensor for stopping the movement of the Categorizer at the left
	// border end
	private static EV3TouchSensor touchSensor;

	public static void main(String[] args) {
		// Create the instances
		beltMotor = new EV3LargeRegulatedMotor(MotorPort.A);
		beltMotor.resetTachoCount();
		beltMotor.setSpeed(1700);

		// create a motor object to control the motor we are using as a servo.
		servoMotor = new EV3MediumRegulatedMotor(MotorPort.C);
		servoMotor.resetTachoCount();

		Port s4 = LocalEV3.get().getPort("S4");
		ColorSensor = new EV3ColorSensor(s4);

		SampleProvider ColorProvider;
		float[] ColorSample;

		// Colour sensor in mode of reading ID colours from the bricks
		ColorProvider = ColorSensor.getColorIDMode();
		ColorSample = new float[ColorProvider.sampleSize()];

		// Our touch sensor used to stop the conveyor belt at the left end side
		touchSensor = new EV3TouchSensor(SensorPort.S1);
		SensorMode touch = touchSensor.getTouchMode();
		float[] sample = new float[touch.sampleSize()];

		int angle = 0;

		// Start with plunger in LOWER position (no brick extracted yet)
		servoMotor.rotateTo(0, true);
		Delay.msDelay(300);

		while (Button.ENTER.isUp()) {
			// Check if working!!!
			angle = servoMotor.getTachoCount();
			fixError(angle);

			// Check the state of the Touch Sensor
			touch.fetchSample(sample, 0);

			// Takes 1 colour reading from the Colour Sensor
			ColorProvider.fetchSample(ColorSample, 0);
			System.out.println(ColorSample[0]);
			Delay.msDelay(300);

			// Blue brick, move to Blue's position
			if (ColorSample[0] == 2.0) {

				// Move Conveyor Belt to the Blue Area position
				moveTo(100, 800);

				// Extract 1 Brick
				extractBrick();

				// Move Conveyor Belt to the initial top left side
				do {
					// Keep going backward until it touches our Touch Sensor
					// to be positioned at left side.
					beltMotor.backward();
					touch.fetchSample(sample, 0);
					// System.out.println(sample[0]);
				} while (sample[0] == 0.0);

				beltMotor.stop();
				Delay.msDelay(500);
			}

			// White brick, move to White's position
			if (ColorSample[0] == 6.0) {
				// Move Conveyor Belt to the White Area position
				moveTo(300, 1000);

				// Extract 1 Brick
				extractBrick();

				// Move Conveyor Belt to the initial top left side
				do {
					beltMotor.backward();
					touch.fetchSample(sample, 0);
					// System.out.println(sample[0]);
				} while (sample[0] == 0.0);

				beltMotor.stop();
				Delay.msDelay(500);

			}

			// No brick, do nothing
			if (ColorSample[0] == 7.0) {
				beltMotor.stop();
				Delay.msDelay(1000);
			}

			// Colour Code
			// -1 is Nothing Detected
			// 2 is BLUE
			// 6 is WHITE
			// 0 is RED
			// 3 is YELLOW
			// 7 is BLACK

			// All other colours case
			if ((ColorSample[0] == -1) || (ColorSample[0] == 0)
					|| (ColorSample[0] == 3.0) || (ColorSample[0] == 1.0)) {
				// Move Conveyor Belt to the AllOthers Area position
				moveTo(500, 1000);

				// Extract 1 Brick
				extractBrick();

				// Move Conveyor Belt to the initial top left side
				do {
					beltMotor.backward();
					touch.fetchSample(sample, 0);
					// System.out.println(sample[0]);
				} while (sample[0] == 0.0);

				beltMotor.stop();
				Delay.msDelay(500);
			}
		}

		// free up resources
		FreeResources();
	}

	private static void fixError(int angle) {
		if (angle >= 10) {
			servoMotor.rotate(-10);
			angle -= 10;
		}
	}

	// Extracts 1 brick out of the categorizer
	private static void extractBrick() {
		// plunger in Upper then Lower position
		servoMotor.rotateTo(-90, true);
		Delay.msDelay(300);
		servoMotor.rotateTo(0, true);
		Delay.msDelay(300);
	}

	// Move Conveyor Belt to position for dropping the brick to the bin
	private static void moveTo(int pos, int delay) {
		beltMotor.rotateTo(pos, true);
		Delay.msDelay(delay);
	}

	private static void FreeResources() {
		// Free up motor-sensor resources
		ColorSensor.close();
		touchSensor.close();
		beltMotor.stop();
		servoMotor.stop();
		beltMotor.close();
		servoMotor.close();
	}
}
