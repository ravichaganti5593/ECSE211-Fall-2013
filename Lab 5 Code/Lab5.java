//Lilly Tong - 260459522
//Ravi Chaganti - 260469339
import lejos.nxt.*;
import lejos.util.Delay;

public class Lab5 {

	private static double leftRadius = 2.21;
	private static double rightRadius = 2.21;
	private static double width = 16.1;
	private static final int bandCenter = 30, bandWidth = 3;
	private static final int motorLow = 100, motorHigh = 300;
	
	/*DEMO 1*/
	private static int USdistance;
	
	public static void main(String[] args) {
		
		int buttonChoice;
		
		// setup everything: odometer, display, and ultrasonic and color sensor etc.
		TwoWheeledRobot patBot = new TwoWheeledRobot(Motor.A, Motor.B, width, leftRadius, rightRadius);
		Odometer odo = new Odometer(patBot, true); //once instance is given, automatically starts timer 
		UltrasonicSensor us = new UltrasonicSensor(SensorPort.S1);
		BangBangController bangbang = new BangBangController(bandCenter, bandWidth, motorLow, motorHigh);
		UltrasonicPoller usPoller = new UltrasonicPoller(us, bangbang);
		Navigation navigation = new Navigation(odo, us, leftRadius, rightRadius, width);
		USLocalizer usl = new USLocalizer(odo, us, USLocalizer.LocalizationType.FALLING_EDGE, navigation);
		ColorSensor cs = new ColorSensor(SensorPort.S2);
		ObjectDetection od = new ObjectDetection(us, odo, cs, patBot, navigation);
		LCDInfo lcd = new LCDInfo(odo, us, usl, cs, od); //once instance is given, automatically starts	
		
		
		do {
			// clear the display
			LCD.clear();

			// ask the user whether they want demo 1 or demo 2
			LCD.drawString("< Left | Right >", 0, 0);
			LCD.drawString("       |        ", 0, 1);
			LCD.drawString("Demo 1 | Demo 2 ", 0, 2);
			LCD.drawString("       |        ", 0, 3);
			LCD.drawString("       |        ", 0, 4);

			buttonChoice = Button.waitForAnyPress();
		} while (buttonChoice != Button.ID_LEFT
				&& buttonChoice != Button.ID_RIGHT);

		/*DEMO 1: OBJECT IDENTIFICATION*/
		if (buttonChoice == Button.ID_LEFT) 
		{
			lcd.getLCDTimer().start();
			
			while (true)
			{
			
			od.colorIdentification();
			
			Button.waitForAnyPress();
			}
			
			
		} 
		
		/*DEMO 2: THE REAL DEAL*/
		else 
		{ 
			lcd.getLCDTimer().start();
			usPoller.start(); //start ultrasonic poller
			
	
			usl.doLocalization(); //do ultrasonic localization
			navigation.start();
			
			
			Button.waitForAnyPress();
		}
		
		
		
	}

}
