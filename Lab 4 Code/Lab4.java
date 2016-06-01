//Lilly Tong - 260459522
//Ravi Chaganti - 260469339
//October 7, 2013

import lejos.nxt.*;
import lejos.util.Delay;

public class Lab4 {

	public static void main(String[] args) {
		// setup the odometer, display, and ultrasonic and color sensor etc.
		TwoWheeledRobot patBot = new TwoWheeledRobot(Motor.A, Motor.B);
		Odometer odo = new Odometer(patBot, true);
		UltrasonicSensor us = new UltrasonicSensor(SensorPort.S1);
		USLocalizer usl = new USLocalizer(odo, us, USLocalizer.LocalizationType.RISING_EDGE);
		LCDInfo lcd = new LCDInfo(odo, us, usl);
		ColorSensor cs = new ColorSensor(SensorPort.S2);
		Navigation navigation = new Navigation(odo);
	
		Button.waitForAnyPress();
		
		
		// perform the ultrasonic localization
		usl.doLocalization(); 
		
		Delay.msDelay(1000);
		
		
		//ROUGH CURRENT COORDINATE DETECTION: WHERE AM I RIGHT NOW? WHAT IS MY X AND Y POSITION? I NEED TO GO TO (0,0)
		
		
		double x,y; //create two variables, x and y to store my current position coordinates
		
		navigation.turnTo(270); //turn robot to LEFT wall to use ultrasonic sensor to read my x distance from the wall
		// do a ping
		us.ping();
				
		// wait for the ping to complete
		try { Thread.sleep(50); } catch (InterruptedException e) {}
		
		x = -30.96 + us.getDistance(); //I have to add -30.96 to my x value read by the sensor because I am in the 4th quadrant
		
		Delay.msDelay(1000); //wait for a bit
		
		navigation.turnTo(180); //turn robot to BACK wall to use ultrasonic sensor to read my y distance from the wall.
		// do a ping
		us.ping();
								
		// wait for the ping to complete
		try { Thread.sleep(50); } catch (InterruptedException e) {}
		y = -30.96 + us.getDistance(); //I have to add -30.96 to my y value read by the sensor because I am in the 4th quadrant
		
		odo.setX(x); //set x to my new value measured by the ultrasonic sensor
		odo.setY(y); //set y to my new value measured by the ultrasonic sensor
	
		
		//travel to (0,0), roughly
		navigation.travelTo(0,0); 
		
		
		// perform the light sensor localization
		LightLocalizer lsl = new LightLocalizer(odo, cs);
		lsl.doLocalization();			
		
		
		Button.waitForAnyPress();
		
		
	}

}
