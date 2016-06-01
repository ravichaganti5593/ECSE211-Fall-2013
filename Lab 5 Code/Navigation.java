//Lilly Tong - 260459522
//Ravi Chaganti - 260469339
import lejos.nxt.*;
import lejos.util.Delay;

public class Navigation extends Thread{
	
	/*class constants*/
	private double leftRadius;
	private double rightRadius;
	private double width;
	private static final int FORWARD_SPEED = 70;
	private static final int ROTATE_SPEED = 100;
	private final NXTRegulatedMotor leftMotor = Motor.A;
	private final NXTRegulatedMotor rightMotor = Motor.B;
	private final NXTRegulatedMotor usSensor = Motor.C;
	
	/*class variables*/
	private Odometer odometer;
	private UltrasonicSensor us;
	private boolean isNavigating;
	private double x; //value passed by Lab 3 and set through setter, where i want to go or my destination x coordinate
	private double y; //value passed by Lab 3 and set through setter, where i want to go or my destination y coordinate
	private static double dX; 
	private static double dY;
	private static double distance;
	private static double thetaHeading;

	
	
	/*Object Detection variables*/
	private static boolean obstacleAhead = false;
	private boolean performColorID = false;
	private int counter = 0;
	private Object lock;
	private static boolean pickedUp = false;
	private boolean goToDestination = false;
	
	public Navigation(Odometer odometer, UltrasonicSensor us, double leftRadius, double rightRadius, double width)
	{
		this.odometer = odometer;
		this.us = us;
		this.leftRadius = leftRadius;
		this.rightRadius = rightRadius;
		this.width = width;
		lock = new Object();
	}
	
	//THREAD STARTS//
	public void run()
	{
		
		usSensor.rotateTo(-100);
		
		double[] x={0, 0, 0, 80.96};
		double[] y={45, 105, 165, 182.88};
		double objectXDistance, objectYDistance;
		int threshold;
		
		objectXDistance = 0;
		objectYDistance = 0;
	
		
		for (int i=0; i<4; i++) //for the four coordinates points I should travel to in this lab
		{
			/*START: CHECK AHEAD: IS THERE OBSTACLE ALONG THIS VERTICAL PATH BEFORE I TRAVEL TO x[i], y[i]*/
			if ( i!=0 && !pickedUp && !goToDestination) //assume first coordinate third coordinates are clear
			{	
					usSensor.rotateTo(0); //rotate ultrasonic sensor to the front and check if there is anything
					if(i!=3)
					{
						threshold=70; //if I am not travelling toward the last x[i], y[i], the largest distance the obstacle can be from me is 70
					}
					else 
					{
						threshold=30; //if I am travelling toward the last x[i], y[i], the largest distance the obstacle can be at is at 30
					}
					
					if(usDistance()<threshold) //if indeed something is discovered . IF PROBLEMS ARISE HERE ADD A FILTER
					{
						objectYDistance = odometer.getY()+usDistance()-10; //set Y coordinate that I should travel to current distance + distance from current to obstacle - 10
						obstacleAhead = true; //set obstacleAhead to true, this variable controls what happens next
					}
					usSensor.rotateTo(-100); //rotate ultrasonic sensor back to the side position
				
			}
			/*STOP: CHECK AHEAD: IS THERE OBSTACLE ALONG THIS VERTICAL PATH*/
			
			isNavigating = true;
			while (isNavigating) //isNavigating can be set to false inside travelTo() if destination is reached. 
			{
				if(pickedUp)
				{
					travelToFixed(80, 185); //travel to destination directly
				}
				
				else if (obstacleAhead)
				{
					travelTo(objectXDistance, objectYDistance);	 //only travel up to the distance at which obstacle is detected
				}
				else
				{
					if (odometer.getY() > y[i]) //if my current y is greater than the y coordinate I am supposed to go to, go to the next (x,y)
					{
						i++;
					}
					usSensor.rotateTo(-100); //rotate ultrasonic sensor to the side to detect any objects in the open field
					travelTo(x[i], y[i]); //this function is being called in a loop because it is inside isNavigating
				}
				
			}
			
			if (obstacleAhead) //if there is indeed an obstacle ahead of the path I am travelling along
			{
				usSensor.rotateTo(0); //at this point I should be right in front of obstacle, So I turn my sensor towards the front
				
				//5a. If it is a foam
				if (ObjectDetection.getRawRatio() >= 1) //this function returns true if a foam is detected
				{
					pickedUp = true; //local variable inside navigation that tells navigation object has been picked up
		
				}
				
				//5b. if it is a block
				else 
				{
					/***CHANGE BANG BANG BANDWIDTH!!!!!! since right sensor***/
					
					//the following is a fixed path I travel to avoid the block in front of me
					leftMotor.forward();
					rightMotor.forward();
					
					leftMotor.setSpeed(ROTATE_SPEED);
					rightMotor.setSpeed(ROTATE_SPEED);
	
					leftMotor.rotate(convertAngle(leftRadius, width, 90.0), true); //turn robot 90 degrees clockwise
					rightMotor.rotate(-convertAngle(rightRadius, width, 90.0), false);
					
					leftMotor.setSpeed(200);
					rightMotor.setSpeed(200);
					
					leftMotor.rotate(convertDistance(leftRadius, 30), true); //travel forward 20
					rightMotor.rotate(convertDistance(rightRadius, 30), false); 
					
					leftMotor.setSpeed(ROTATE_SPEED);
					rightMotor.setSpeed(ROTATE_SPEED);
	
					leftMotor.rotate(-convertAngle(leftRadius, width, 90.0), true); //turn robot 90 degrees anticlockwise
					rightMotor.rotate(convertAngle(rightRadius, width, 90.0), false);
					
					leftMotor.setSpeed(200);
					rightMotor.setSpeed(200);
					
					leftMotor.rotate(convertDistance(leftRadius, 45), true);  //travel forward 20
					rightMotor.rotate(convertDistance(rightRadius, 45), false); 
					
					leftMotor.setSpeed(ROTATE_SPEED);
					rightMotor.setSpeed(ROTATE_SPEED);
	
					leftMotor.rotate(-convertAngle(leftRadius, width, 90.0), true); //turn robot 90 degrees anticlockwise
					rightMotor.rotate(convertAngle(rightRadius, width, 90.0), false);
					
					leftMotor.setSpeed(200);
					rightMotor.setSpeed(200);
					
					leftMotor.rotate(convertDistance(leftRadius, 30), true);  //travel forward 20
					rightMotor.rotate(convertDistance(rightRadius, 30), false); 
					
					turnTo(0); //turn robot to 0 degrees
					
					if(odometer.getY() >= y[i+1]) //if my current Y exceeds the Y I am supposed to travel to, I travel to the next set of (x,y)
					{
						i++;
					}
				}
				
				obstacleAhead = false; //now I'm past the obstacle, set this to false
			}
		}
		
	}
	//THREAD ENDS//
	
	public void travelTo(double x, double y) //this function should be constantly called along the path of reaching your destination
	{	
		
		/* BEGIN: OBJECT CHECK*/
		
		scanForObjects(); 

		/* END: OBJECT CHECK*/
		
		//everything below is unchanged from Lab3, it's just your regular travelTo
		
		dX = x - odometer.getX();
		dY = y - odometer.getY();
		
		/* BEGIN: DESTINATION REACHED*/
		if ((Math.abs(dX) <= 0.5) && (Math.abs(dY) <= 0.5)) //position error threshold
		{
			isNavigating = false; //set isNavigating to false so I will exit the while loop in the run() method and terminate thread for this round.
			leftMotor.stop();
			rightMotor.stop();
		}
		/* END: DESTINATION REACHED*/
		
		else //will only execute if I have not yet reached my destination
		{
			distance = Math.sqrt(Math.pow(dX, 2) + Math.pow(dY, 2)); //how far I should travel to get to destination
		
			/* BEGIN: CASES OF WHICH ANGLE I SHOULD HEAD IN BY QUADRANT */
		
			if (dY > 0)
			{
				if (dX != 0)
				{
					thetaHeading = Math.atan(dX/dY)*(180/Math.PI); //rotate to COMPLEMENTARY ANGLE!
				}
			
				else // dX = 0
				{
				thetaHeading = 0; //else I travel along positive Y axis
				}
			}
		
			else if (dY == 0)
			{
				if (dX > 0)
				{	
					thetaHeading = 90; //destination is to the right of me
				}
			
				else
				{
					thetaHeading = -90; //destination is to the left of me
				}
			}
		
			else if (dY < 0)
			{
				if (dX < 0)
				{
					thetaHeading = Math.atan(dX/dY)*(180/Math.PI) - 180;
				}
			
				else if (dX > 0)
				{
					thetaHeading = Math.atan(dX/dY)*(180/Math.PI) + 180;
				}
			
				else 
				{
					thetaHeading = 180; //else I travel along negative Y axis
				}
			}
		
			/* END: CASES OF WHICH ANGLE I SHOULD HEAD IN BY QUADRANT */
			
			/*	 BEGIN: calls turnTo if my angle is too far off from destination */
			if (Math.abs(odometer.getTheta() - thetaHeading) >= 0.7) //angle threshold: 1 degree off from where i should be heading
			{
				turnTo(thetaHeading); //I only call turnTo if my error is above the angle error threshold.
			}
			/* END: calls turnTo if my angle is too far off from destination*/
			
			/* BEGIN: move robot forward closer toward the destination*/
				
			leftMotor.setSpeed(FORWARD_SPEED);
			rightMotor.setSpeed(FORWARD_SPEED);
		

			leftMotor.forward();
			rightMotor.forward();
			
			/* END: move robot forward closer toward the destination */
		

			try {
				Thread.sleep(100); //sleep 100ms so I don't constantly spam the robot and tells it to move forward.
			} catch (InterruptedException e) {
			}
		
		}
	
	
	}

	
	public void travelToFixed(double x, double y) //this function is for travelling a fixed distance with no adjustments along the way
	{// USE THE FUNCTIONS setForwardSpeed and setRotationalSpeed from TwoWheeledRobot!
			dX = x - odometer.getX();
			dY = y - odometer.getY();
			
			distance = Math.sqrt(Math.pow(dX, 2) + Math.pow(dY, 2)); //how far I should travel to get to destination
			
			/* BEGIN: CASES OF WHICH ANGLE I SHOULD HEAD IN BY QUADRANT */
			
			if (dY > 0)
			{
				if (dX != 0)
				{
					thetaHeading = Math.atan(dX/dY)*(180/Math.PI); //rotate to COMPLEMENTARY ANGLE!
				}
				
				else // dX = 0
				{
				thetaHeading = 0; //else I travel along positive Y axis
				}
			}
			
			else if (dY == 0)
			{
				if (dX > 0) //if positive x
				{	
					thetaHeading = 90; //destination is to the right of me
				}
				
				else
				{
					thetaHeading = -90; //destination is to the left of me
				}
			}
			
			else if (dY < 0) //case where y I am heading towards is negative
			{
				if (dX < 0) //subcase: x I am heading toward is also negative
				{
					thetaHeading = Math.atan(dX/dY)*(180/Math.PI) - 180; //minus correction, which is 180
				}
				
				else if (dX > 0) //subcase: x I am heading toward is positive
				{
					thetaHeading = Math.atan(dX/dY)*(180/Math.PI) + 180; //plus correction, which is 180
				}
				
				else 
				{
					thetaHeading = 180; //else I travel along negative Y axis
				}
			}
			
			/* END: CASES OF WHICH ANGLE I SHOULD HEAD IN BY QUADRANT */
			
			turnTo(thetaHeading); //call turnTo function to calculate which angle I should head toward
			
			leftMotor.setSpeed(200); //set forward speed
			rightMotor.setSpeed(200); //set forward speed
				
			Motor.A.rotate(convertDistance(leftRadius, distance), true); //convert distance to angles I should travel
			Motor.B.rotate(convertDistance(rightRadius, distance), false); //convert distance to angles distance I should travel
	}
	
	public void turnTo(double theta)
	{
		double thetaCurrent = odometer.getTheta();
		double thetaTurn = theta - thetaCurrent; //this is like theta = theta + (0 - thetaCurrent), first return to zero, THEN i turn to the absolute position theta based on the board's coordinate system
		
		if (thetaTurn > 180) //not minimum turn
		{
			thetaTurn = thetaTurn - 360; //the angle is too big, so -360 to get minimum turn
		}
		
		else if (thetaTurn < -180)
		{
			thetaTurn = thetaTurn + 360; //the angle is too small, so +360 to get minimum turn
		}
		
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		
		leftMotor.rotate(convertAngle(leftRadius, width, thetaTurn), true);
		rightMotor.rotate(-convertAngle(rightRadius, width, thetaTurn), false);
	}
	
	public boolean isNavigating()
	{
		return isNavigating;
	}
	
	public int usDistance() //gets distance read by ultrasonic sensor
	{
		us.ping();
		
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {	}
		
		return us.getDistance();
		
	}
	
	public static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	public static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
	
///////////////////////////////////////////////LAB 5 METHODS/////////////////////////////////////////////////////
	
	public void scanForObjects() //this is called when I am travelling along my path
	{
		if (usDistance() < 70) //if ultrasonic distance is less than 80, I know there is an object in the open field
		{
			counter++; //filter counter
			
			if (counter == 3) //below only executes if counter has gone up to 3
			{
				counter = 0; //reset counter
				objectEncountered(us.getDistance()); //call objectEncountered, pass the distance at which it was detected at
			}
			
		}
	}
	
	public void objectEncountered(int usDistance)
	{
		double currentY; //record my current Y
		
		/*START: MOVE CLOSE TO THE OBJECT*/
		leftMotor.forward(); //set both wheel to forward
		rightMotor.forward();
		
		leftMotor.rotate(convertDistance(leftRadius, 15), true); //travel 10cm forward due to offset of sensors
		rightMotor.rotate(convertDistance(rightRadius, 15), false); 
		
		currentY = odometer.getY(); //record current y
		
		leftMotor.rotate(convertAngle(leftRadius, width, 90), true); //rotate the entire robot 90 degrees clockwise
		rightMotor.rotate(-convertAngle(rightRadius, width, 90), false);
		
		usSensor.rotateTo(0); //rotate the sensor so that it's pointing ahead
		
		leftMotor.setSpeed(200); //set forward speed to 150
		rightMotor.setSpeed(200);
		
		leftMotor.forward(); //move forward
		rightMotor.forward();
		
		leftMotor.rotate(convertDistance(leftRadius, usDistance-15), true); //travel a fixed distance toward the object detected. Distance travelled: ultrasonic distance - 20
		rightMotor.rotate(convertDistance(rightRadius, usDistance-15), false);
		/*END: MOVE CLOSE TO THE OBJECT*/
		
		/*OBJECT LOCALIZATION BEGINS: I'M NEAR THE OBJECT AND I WANT TO FIND IT*/
		
		turnTo(20); //turn robot to roughly 20 degrees to begin localization
		
		leftMotor.setSpeed(50);
		rightMotor.setSpeed(50);
		
		leftMotor.rotate(convertAngle(leftRadius, width, 150), true); //rotate the entire robot 150 degrees clockwise
		rightMotor.rotate(-convertAngle(rightRadius, width, 150), true);
		
		boolean checkObjectType = true;
		
		while (checkObjectType) //rotate and continuously check the raw color ratio
		{
			if (ObjectDetection.getRawRatio() <= 0.987) //if it goes less than 0.987, that means it is a block
			{
				Sound.buzz(); //block
				ObjectDetection.setObjectType(0);
				checkObjectType = false;
			}
			
			else if (odometer.getTheta() >= 80) //if after turning 80 degrees it still didn't buzz, it means it must be a foam
			{
				Sound.beep(); //foam
				ObjectDetection.setObjectType(1);
				checkObjectType = false;
			}
		}
		
		leftMotor.stop(); //stop, I have found the closest point to the object. But there is still some distance left.
		rightMotor.stop(); //stop, I have found the closest point to the object. But there is still some distance left.
		
		
		
		//5a. If it is a foam
		if (ObjectDetection.getObjectInt() == 1) //this function returns true if a foam is detected
		{
			//ObjectDetection.pickedUp(); //call this function so ObjectDetection knows a foam has been picked up
			
			/***START OF MOTION CONTROL: OBJECT PICKUP***/
			
			usSensor.rotateTo(-100); //rotate sensor to side position
			leftMotor.rotate(convertDistance(leftRadius, 10), true); //travel forward to object
			rightMotor.rotate(convertDistance(rightRadius, 10), false); 
			
			usSensor.rotateTo(10); //LOCK the square
			

			
			/***END OF MOTION CONTROL: OBJECT PICKUP***/
			
			pickedUp = true; 
		}
		
		//5b. If it is a block
		else if (ObjectDetection.getObjectInt() == 0) //if block
		{
			
			leftMotor.setSpeed(200);
			rightMotor.setSpeed(200);
			
			leftMotor.rotate(-convertDistance(leftRadius, 10), true); //retreat a certain distance so I don't crash into anything
			rightMotor.rotate(-convertDistance(rightRadius, 10), false); //retreat a certain distance so I don't crash into anything
			
			travelToFixed(0, currentY+30); //travel back to the original path, except now I increase Y by 15 to avoid scanning the same obstacle again.
			
		}

		
	}
	

}
