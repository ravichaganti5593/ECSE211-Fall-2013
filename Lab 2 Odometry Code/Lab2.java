/*
 * Lab2.java
 */
import lejos.nxt.*;

public class Lab2 {
	

	private static final double leftRadius = 2.2; // radius of left wheel in cm
	private static final double rightRadius= 2.2; // radius of right wheel in cm
	private static final double width = 16.1; //width between the two wheels
	private static final SensorPort colorPort = SensorPort.S1; //where color sensor is plugged in, in this case, 1

	
	public static void main(String[] args) {
		int buttonChoice;

		// some objects that need to be instantiated
		Odometer odometer = new Odometer(leftRadius, rightRadius, width);
		ColorSensor colorsensor = new ColorSensor(colorPort); //sets up instance of ColorSensor
		OdometryDisplay odometryDisplay = new OdometryDisplay(odometer, colorsensor);
		OdometryCorrection odometryCorrection = new OdometryCorrection(odometer, colorsensor);

		do {
			// clear the display
			LCD.clear();

			// ask the user whether the motors should drive in a square or float
			LCD.drawString("< Left | Right >", 0, 0);
			LCD.drawString("       |        ", 0, 1);
			LCD.drawString(" Float | Drive  ", 0, 2);
			LCD.drawString("motors | in a   ", 0, 3);
			LCD.drawString("       | square ", 0, 4);

			buttonChoice = Button.waitForAnyPress();
		} while (buttonChoice != Button.ID_LEFT
				&& buttonChoice != Button.ID_RIGHT);

		if (buttonChoice == Button.ID_LEFT) {
			for (NXTRegulatedMotor motor : new NXTRegulatedMotor[] { Motor.A, Motor.B, Motor.C }) {
				motor.forward();
				motor.flt();
			}

			// start only the odometer and the odometry display
			odometer.start();
			odometryDisplay.start();
		} else {
			// start the odometer, the odometry display and (possibly) the
			// odometry correction
			odometer.start();
			odometryDisplay.start();
			odometryCorrection.start();

			// spawn a new Thread to avoid SquareDriver.drive() from blocking
			(new Thread() {
				public void run() {
					SquareDriver.drive(Motor.A, Motor.B, leftRadius, rightRadius, width);
				}
			}).start();
		}
		
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}
}