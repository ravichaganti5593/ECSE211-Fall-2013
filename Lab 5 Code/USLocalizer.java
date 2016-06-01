//Lilly Tong - 260459522
//Ravi Chaganti - 260469339
//October 20, 2013

import lejos.nxt.*;
import lejos.util.Delay;
import java.util.Arrays;

public class USLocalizer {
	public enum LocalizationType { FALLING_EDGE, RISING_EDGE };
	public static double ROTATION_SPEED = 20;
	public static int wallDist = 35;

	private Odometer odometer;
	private Navigation navigation;
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
	private boolean lightLocalization = false; //whether or not I want to use lightLocalization afterwards.
	
	
	public USLocalizer(Odometer odo, UltrasonicSensor us, LocalizationType locType, Navigation navigation) {
		this.odometer = odo;
		this.robot = odo.getTwoWheeledRobot();
		this.us = us;
		this.locType = locType;
		this.navigation = navigation;
		
		// switch off the ultrasonic sensor
		//us.off();
	}
	
	/* WHAT US LOCALIZATION DOES*/
	// begins in the left corner, then sweeps around for walls behind and to the left
	// then computes the orientation and travels roughly to (0,0)
	
	public void doLocalization() {

		
		/*Falling edge*/
		if (locType == LocalizationType.FALLING_EDGE) {
			
			// rotate the robot until it sees no wall
			if (getFilteredData() < 30)
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
				if (getFilteredData() < wallDist - 11 ) //if I see a wall
				{	
					angle1 = odometer.getTheta(); //save angle 1
					CW = false; //set CW to false so I exit this loop
				}
			}
					
			// switch direction and wait until it sees no wall
						
			robot.setSpeeds(0, -20); //rotate in other direction
			while (getFilteredData() < 100) //while I am still too close to the wall
			{
			}
						
			
			while (CCW)
			{
							
				if (getFilteredData() < wallDist + 10) //if I see a wall
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
				if (getFilteredData() > wallDist)
				{
					robot.setSpeeds(0, ROTATION_SPEED);
					while (getFilteredData() > wallDist) //while I am too far from the wall, I rotate toward the wall
					{
					}
					odometer.setTheta(0); //clear theta here
				}
				
				
			
			// keep rotating until the robot no longer sees the wall, then latch the angle
			
				robot.setSpeeds(0, ROTATION_SPEED);
				while (CW)
				{
					if (getFilteredData() > 25) //if I am no longer facing a wall
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
										
					if (getFilteredData() > 25) //if I no longer see a wall
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
		
			if (lightLocalization) //do I implement lightLocalization after this?
			{
				
				/*ROUGHLY TRAVEL TO (0,0)*/
				
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
				
				odometer.setX(x); //set x to my new value measured by the ultrasonic sensor
				odometer.setY(y); //set y to my new value measured by the ultrasonic sensor
			
				
				//travel to (0,0), roughly
				navigation.travelToFixed(0,0); 
			}
			
			else //I turn to positive y axis
			{
				Delay.msDelay(1000);
				navigation.turnTo(0);
				odometer.setX(0); //set x to my new value measured by the ultrasonic sensor
				odometer.setY(0);
			}
			
	}
	
	public int getFilteredData() {
		int[] getDistances = new int[4];

		 // do a ping each time to update the reading from ultrasonic sensor
		for(int i=0;i<4;i++){
			us.ping();

			// wait for the ping to complete
			try { Thread.sleep(50); } catch (InterruptedException e) {}

			// there will be a delay here
			getDistances[i] = us.getDistance();
		}
		
		Arrays.sort(getDistances); //list the array in increasing order the distances read by the sensor
		
		int distance = (getDistances[0]+getDistances[1])/2;// set the medium of the reading to distance
		// Filter the reading greater than 150 
		if (distance > 120)
			{distance = 120;}

		return distance;

	}
	
}
