// Erasmus+ 2019-2020
// 2o Gymnasio Samou, GREECE
// Christmas programming challenge: EV3 Colour Sorter of National flags' coloured bricks
// GROUP: TEAM_2

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.motor.UnregulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class NewBelt {

	// Unregulated EV3 Large Motor for moving the belt
	private static UnregulatedMotor beltMotor;

	// Create a motor object to control the Medium Motor that is used as a servo
	private static EV3MediumRegulatedMotor servoMotor;

	// Colour Sensor for reading colours
	private static EV3ColorSensor ColorSensor;

	public static void main(String[] args) {
		// Create the instances
		beltMotor = new UnregulatedMotor(MotorPort.B);
		servoMotor = new EV3MediumRegulatedMotor(MotorPort.C);

		Port s2 = LocalEV3.get().getPort("S2");
		ColorSensor = new EV3ColorSensor(s2);

		SampleProvider ColorProvider;
		float[] ColorSample;

		// Colour sensor in mode of reading ID colours from the bricks
		ColorProvider = ColorSensor.getColorIDMode();
		ColorSample = new float[ColorProvider.sampleSize()];

		// Array of size 5 to store 5 colour readings
		// We take 5 readings for each block to be more confident on our colour
		// decision
		float[] ColorSamples = new float[5];
		int i, angle = 0;

		// Whole program keeps running until the user presses the Enter button
		// of the EV3 Smart Brick
		while (Button.ENTER.isUp()) {

			// ServoMotor object assumes it is at zero angle position when
			// created, so
			// robot must be in it's starting configuration when starts.
			// Reposition hitting lever's servoMotor to 0 degrees when it
			// offsets by errors (Correcting big errors)
			if (angle >= 20) {
				servoMotor.rotate(-20);
				angle -= 20;
			}

			// Start moving forward the belt at constant low power
			driveBelt(17);

			// Take 1 colour sample reading with the Colour Sensor
			ColorProvider.fetchSample(ColorSample, 0);
			ColorSamples[0] = ColorSample[0];
			Delay.msDelay(100);

			// Begin the colour examination procedure only if a colored
			// brick is detected,other ie it excludes detection of values:
			// colorID -1 by the sensor that is "nothing detected"
			// colorID 7.0 by the sensor that is BROWN colour
			// colorID 1.0 by the sensor that is BLACK colour (our conveyor belt
			// colour)
			if ((ColorSamples[0] != -1) && (ColorSamples[0] != 7.0)
					&& (ColorSamples[0] != 1.0)) {

				// Print out the detected colour ID code
				// Colour codes: 2 is Blue, 6 is White, 3 Green, 4 Yellow, 5 Red
				System.out.println(ColorSample[0]);

				// Take 4 more Colour sample readings and store them in the
				// array ColorSamples
				for (i = 1; i < ColorSamples.length; i++) {
					ColorProvider.fetchSample(ColorSample, 0);
					ColorSamples[i] = ColorSample[0];
					// fast reading samples
					Delay.msDelay(20);
				}

				if (isAllEqual(ColorSamples)) {
					// Make a sound when a coloured brick is read (5 matching
					// reading samples)
					Sound.beepSequence();

					// demonstrate rotate to target angle without wait
					servoMotor.resetTachoCount();

					// Blue bricks hit to the left side
					if (ColorSample[0] == 2f) {
						hitBrick(-1);
					}

					// White bricks hit to the right side
					if (ColorSample[0] == 6f) {
						hitBrick(1);
					}
				}

			}

		}
		// free up motor-sensor resources
		FreeResources();

	}

	// Used to drive the conveyor belt at a constant speed (feeding the Lego
	// bricks)
	private static void driveBelt(int power) {
		beltMotor.setPower(power);
		beltMotor.forward();
	}

	// Used to move the lever that hits and sorts the bricks (direction flag: -1
	// is LEFT, 1 is RIGHT)
	// This lever has to move very fast and with large acceleration to hit hard
	// the bricks
	private static void hitBrick(int direction) {
		// Sets desired motor speed, in degrees per second. The maximum reliably
		// velocity is 100 x battery voltage
		servoMotor.setSpeed(1700);

		// Sets the acceleration rate of this motor in degrees/sec/sec. The
		// default value is 6000
		servoMotor.setAcceleration(36000);

		// BLUE bricks hit to the LEFT side
		if (direction == -1) {
			servoMotor.rotateTo(90, true);
			Delay.msDelay(2000);
			servoMotor.rotateTo(0, true);
			Delay.msDelay(500);
		}

		// WHITE bricks hit to the RIGHT side
		if (direction == 1) {
			servoMotor.rotateTo(-90, true);
			Delay.msDelay(2000);
			servoMotor.rotateTo(0, true);
			Delay.msDelay(500);
		}

	}

	// Examine if all 5 readings for each block are equal, meaning that we
	// decided with
	// confidence for the colour of the block read.
	public static boolean isAllEqual(float[] a) {
		for (int i = 1; i < a.length; i++) {
			if (a[0] != a[i]) {
				return false;
			}
		}
		return true;
	}

	private static void FreeResources() {
		// Free up motor-sensor resources
		beltMotor.stop();
		beltMotor.close();
		servoMotor.stop();
		servoMotor.close();
		ColorSensor.close();
	}

}
