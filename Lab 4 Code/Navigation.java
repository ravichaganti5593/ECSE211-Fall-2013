//Lilly Tong - 260459522
//Ravi Chaganti - 260469339
//October 7, 2013

import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.UltrasonicSensor;


public class Navigation {
	// put your navigation code here 

	private TwoWheeledRobot robot;
	
	/*class constants*/
	private static final double leftRadius = 2.15;
	private static final double rightRadius = 2.15;
	private static final double width = 16.1; 
	private static final int FORWARD_SPEED = 200;
	private static final int ROTATE_SPEED = 100;
	private final NXTRegulatedMotor leftMotor = Motor.A;
	private final NXTRegulatedMotor rightMotor = Motor.B;
	private final NXTRegulatedMotor usSensor = Motor.C;
	
	/*class variables*/
	private Odometer odometer;
	private boolean isNavigating;
	private boolean pastObstacle = false;
	private static double dX; //SET TO PRIVATE LATER!!! IF NOT USED FOR ODOMETRY DISPLAY.
	private static double dY;
	private static double distance;
	private static double thetaHeading;
	
	public Navigation(Odometer odo) {
		this.odometer = odo;
		this.robot = odo.getTwoWheeledRobot();
	}
	
	public void travelTo(double x, double y) {
		// USE THE FUNCTIONS setForwardSpeed and setRotationalSpeed from TwoWheeledRobot!
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
		
		leftMotor.setSpeed(FORWARD_SPEED); //set forward speed
		rightMotor.setSpeed(FORWARD_SPEED); //set forward speed
			
		Motor.A.rotate(convertDistance(leftRadius, distance), true); //convert distance to angles I should travel
		Motor.B.rotate(convertDistance(rightRadius, distance), false); //convert distance to angles distance I should travel
			
	}
		
	
	public void turnTo(double theta) {
		// USE THE FUNCTIONS setForwardSpeed and setRotationalSpeed from TwoWheeledRobot!
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
	
	private static int convertDistance(double radius, double distance) { // calculates how much distance I should travel
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
}
