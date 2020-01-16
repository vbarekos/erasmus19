// Erasmus+ 2019-2020
// 2o Gymnasio Samou, GREECE
// Christmas programming challenge: EV3 Colour Sorter of National flags' coloured bricks
// GROUP: TEAM_1

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

	// Motor object for the hitting lever, 
	// controls the Medium Motor used as servo
	private static EV3MediumRegulatedMotor servoMotor;

	// Colour Sensor for reading colours
	private static EV3ColorSensor ColorSensor;

	public static void main(String[] args) {
		// Create the motor instances
		beltMotor = new UnregulatedMotor(MotorPort.C);
		servoMotor = new EV3MediumRegulatedMotor(MotorPort.D);
		
		Port s4 = LocalEV3.get().getPort("S4");
		ColorSensor = new EV3ColorSensor(s4);

		SampleProvider ColorProvider;
		float[] ColorSample;

		// Colour sensor in mode of reading ID colours from the bricks
		ColorProvider = ColorSensor.getColorIDMode();
		ColorSample = new float[ColorProvider.sampleSize()];

		// Array of size 8 to store 8 colour readings
		// We take 8 readings for each block to be more confident on our colour
		// decision
		float[] ColorSamples = new float[8];
		int i, angle = 0;

		while (Button.ENTER.isUp()) {

			// ServoMotor object assumes it is at zero angle position when
			// created, so
			// robot must be in it's starting configuration when starts.
			// Reposition hitting lever's servoMotor to 0 degrees when it
			// offsets by errors (Correcting big errors)
			if (angle >= 10) {
				servoMotor.rotate(-10);
				angle -= 10;
			}

			// Start moving forward the belt at constant low power
			driveBelt(25);

			// Take 1 colour sample reading with the Colour Sensor
			ColorProvider.fetchSample(ColorSample, 0);
			ColorSamples[0] = ColorSample[0];
			System.out.println(ColorSample[0]);
			Delay.msDelay(100);

			// Begin the colour examination procedure only if a colored brick is
			// detected
			// it excludes detection of values:
			// colorID -1 by the sensor that is "nothing detected"
			// colorID 7.0 by the sensor that is BROWN colour (senses the empty
			// conveyor belt)
			// finalBELT colour)
			if ((ColorSamples[0] != -1) && (ColorSamples[0] != 7.0)) {

				// Print the detected colour on ev3 brick screen
				// Colour codes: 2 is Blue, 6 is White, 3 Green, 4 Yellow, 5 Red
				System.out.println(ColorSample[0]);

				// Take Some Colour readings and store them in the array
				// Takes 7 more colour readings
				for (i = 1; i < ColorSamples.length; i++) {
					ColorProvider.fetchSample(ColorSample, 0);
					ColorSamples[i] = ColorSample[0];
					// fast reading samples
					Delay.msDelay(20);
				}

				if (isAllEqual(ColorSamples)) {
					// Make a sound when a coloured brick is read (8 matching
					// reading samples)
					Sound.beepSequence();

					// demonstrate rotation to target angle without wait
					servoMotor.resetTachoCount();

					// BLUE bricks hit to the LEFT side
					if (ColorSample[0] == 2f) {
						SortingLever(-1);
					} else if (ColorSample[0] == 6f) {
						// WHITE bricks hit to the RIGHT side
						SortingLever(1);
					} else {
						// For ALL OTHER colours let them fall
						SortingLever(0);
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

	// Used to move the lever that hits and sorts the bricks
	// Direction flag: -1 is LEFT, 1 is RIGHT, 0 out of the way
	private static void SortingLever(int direction) {

		// Move Lever to hit bricks to the LEFT side
		if (direction == -1) {
			servoMotor.rotateTo(30, true);
			Delay.msDelay(500);
			servoMotor.rotateTo(0, true);
		}

		// Move Lever to hit bricks to the RIGHT side
		if (direction == 1) {
			servoMotor.rotateTo(-30, true);
			Delay.msDelay(500);
			servoMotor.rotateTo(0, true);
		}

		// Move Lever at far end side so that no brick is hit
		if (direction == 0) {
			servoMotor.rotateTo(50, true);
			Delay.msDelay(1200);
			servoMotor.rotateTo(0, true);
		}
	}

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
