//Lilly Tong - 260459522
//Ravi Chaganti - 260469339
import lejos.nxt.*;
import lejos.util.Delay;

public class Lab3 {
	
	private static final SensorPort usPort = SensorPort.S1;
	private static final double leftRadius = 2.19; // radius of left wheel in cm
	private static final double rightRadius= 2.19; // radius of right wheel in cm
	private static final double width = 16.1; //width between the two wheels
	private static final int bandCenter = 30, bandWidth = 3;
	private static final int motorLow = 100, motorHigh = 300;
			
	public static void main(String[] args) {
		
		int buttonChoice; 
		
		//Set up ultrasonic sensor, odometer, bangbang, and odometer display
		UltrasonicSensor usSensor = new UltrasonicSensor(usPort);
		Odometer odometer = new Odometer(leftRadius, rightRadius, width);
		BangBangController bangbang = new BangBangController(bandCenter, bandWidth, motorLow, motorHigh);
		UltrasonicPoller usPoller = new UltrasonicPoller(usSensor, bangbang);
		Navigation navigation = new Navigation(odometer,usPoller, bangbang);
		OdometryDisplay odometryDisplay = new OdometryDisplay(odometer, navigation, bangbang);
		
		do {
			// clear the display
			LCD.clear();

			// ask the user whether the motors should drive in a square or float
			LCD.drawString("< Left | Right >", 0, 0);
			LCD.drawString("       |        ", 0, 1);
			LCD.drawString("Demo 1 | Demo 2 ", 0, 2);
			LCD.drawString("       |        ", 0, 3);
			LCD.drawString("       |        ", 0, 4);

			buttonChoice = Button.waitForAnyPress();
		} while (buttonChoice != Button.ID_LEFT
				&& buttonChoice != Button.ID_RIGHT);

		if (buttonChoice == Button.ID_LEFT) //Demo 1
		{
		
			
			// start the odometer, the odometry display and (possibly) the
			// odometry correction
			
			
			/*DEMO PART 1 STARTS*/
			
			navigation.setDemo(1);
			odometer.start();
			odometryDisplay.start();
			navigation.start();
			
			Button.waitForAnyPress();
			
			
			/*DEMO PART 1 ENDS*/
			
			
		} 
		
		else //demo 2
		{ 
			
			/* DEMO 2 STARTS */
			
			navigation.setDemo(2);
			odometer.start();
			odometryDisplay.start();
			usPoller.start();
			navigation.start();
			
			Button.waitForAnyPress();
			
			/*DEMO 2 ENDS*/
			
		}
		
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
		
	}
	

}
