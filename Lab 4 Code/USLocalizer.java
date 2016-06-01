//Lilly Tong - 260459522
//Ravi Chaganti - 260469339
//October 7, 2013

import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.UltrasonicSensor;
import lejos.nxt.*;

public class USLocalizer {
	public enum LocalizationType { FALLING_EDGE, RISING_EDGE };
	public static double ROTATION_SPEED = 15;
	public static int wallDist = 30;

	private Odometer odometer;
	private TwoWheeledRobot robot;
	private UltrasonicSensor us;
	private LocalizationType locType;
	private final NXTRegulatedMotor leftMotor = Motor.A;
	private final NXTRegulatedMotor rightMotor = Motor.B;
	
	public int lastValidDistance; //saves my last valid distance so to filter out bad values and set them to the last valid value.
	private double angle1; //my first angle detected. in Rising edge it's the left wall in falling edge it's the back wall's angle.
	private double angle2; //my second angle detected. in rising edge it's the back wall in falling edge it's the left wall's angle.
	private double thetaCorrected; //my ACTUAL theta the odometer should display.
	private boolean CW=true; //rotating clockwise
	private boolean CCW=true; //rotating counterclockwise
	
	public USLocalizer(Odometer odo, UltrasonicSensor us, LocalizationType locType) {
		this.odometer = odo;
		this.robot = odo.getTwoWheeledRobot();
		this.us = us;
		this.locType = locType;
		
		// switch off the ultrasonic sensor
		//us.off();
	}
	
	public void doLocalization() {

		
		/*Falling edge*/
		if (locType == LocalizationType.FALLING_EDGE) {
			
			// rotate the robot until it sees no wall
			if (getFilteredData() < wallDist)
			{
				robot.setSpeeds(0, ROTATION_SPEED);
				while (getFilteredData() < 100) //while robot is still facing close to the wall
				{
				}
				odometer.setTheta(0); //clear theta here
			}
					
			// keep rotating until the robot sees a wall, then latch the angle
			
			robot.setSpeeds(0, ROTATION_SPEED);
			while (CW)
			{
				if (getFilteredData() < wallDist) //if I see a wall
				{	
					angle1 = odometer.getTheta(); //save angle 1
					CW = false; //set CW to false so I exit this loop
				}
			}
					
			// switch direction and wait until it sees no wall
						
			robot.setSpeeds(0, -ROTATION_SPEED); //rotate in other direction
			while (getFilteredData() < 100) //while I am still too close to the wall
			{
			}
						
			
			while (CCW)
			{
							
				if (getFilteredData() < wallDist) //if I see a wall
				{
					angle2 = odometer.getTheta(); //save angle 2
					CCW = false; //set CCW to false so I exit the loop
					leftMotor.stop(); //stop rotating
					rightMotor.stop();
				}
			}
			
			
			thetaCorrected = 225 - (angle1-angle2)/2; //calculate what the odometer should display at this point
			odometer.setTheta(thetaCorrected);
						

			//odo.setPosition(new double [] {0.0, 0.0, 0.0}, new boolean [] {true, true, true});
			
		} 
		
		
		/*Rising edge*/
			else { 
			
				//rotate so robot is facing the wall
				if (getFilteredData() > 20)
				{
					robot.setSpeeds(0, ROTATION_SPEED);
					while (getFilteredData() > 20) //while I am too far from the wall, I rotate toward the wall
					{
					}
					odometer.setTheta(0); //clear theta here
				}
				
				
			
			// keep rotating until the robot no longer sees the wall, then latch the angle
			
				robot.setSpeeds(0, ROTATION_SPEED);
				while (CW)
				{
					if (getFilteredData() > wallDist) //if I am no longer facing a wall
					{
						angle1 = odometer.getTheta(); //save angle 1
						CW = false; //set CW to false so I exit this loop
					}
				}
								
				// switch direction and wait until it sees the wall
									
				robot.setSpeeds(0, -ROTATION_SPEED);
				while (getFilteredData() > wallDist) //while I am still too far from the wall, I rotate until I see a wall
				{
				}
									
				// keep rotating until the robot no longer sees a wall, then latch the angle
		
				while (CCW) //while rotating counterclockwise
				{
										
					if (getFilteredData() > wallDist) //if I no longer see a wall
					{
						angle2 = odometer.getTheta(); //save angle 2
						CCW = false; //set CCW to false so I exit the loop
						leftMotor.stop(); //stop motors
						rightMotor.stop();
					}
				}
				
				if (angle2 == 0)
				{
					angle2=360;//since formula only works with 360 and not 0, if my angle2 is 0 I have to make it 360.
				}
				
				thetaCorrected = 45 + (angle2-angle1)/2; //calculate what the odometer should display at this point
									
				odometer.setTheta(thetaCorrected);
			
		}
	}
	
	private int getFilteredData() { //my filter
		int distance;
		 
		// do a ping
		us.ping();
		
		// wait for the ping to complete
		try { Thread.sleep(50); } catch (InterruptedException e) {}
		
		// there will be a delay here
		distance = us.getDistance();
	
		
		if (distance > 120) //any distance larger than 150 is invalid and erroneous and should be ignored.
		{
			distance = lastValidDistance; //set distance to last valid distance
		}
		
		lastValidDistance = distance; //lastValidDistance takes on the value of distance if distance is less than 150 (valid)
		
		return distance;
	}

}
