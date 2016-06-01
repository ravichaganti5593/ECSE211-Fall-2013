//Lilly Tong - 260459522
//Ravi Chaganti - 260469339
import lejos.nxt.*;
import lejos.util.Delay;

public class Navigation extends Thread{
	
	/*class constants*/
	private static final double leftRadius = 2.19;
	private static final double rightRadius = 2.19;
	private static final double width = 16.1; 
	private static final int FORWARD_SPEED = 200;
	private static final int ROTATE_SPEED = 100;
	private final NXTRegulatedMotor leftMotor = Motor.A;
	private final NXTRegulatedMotor rightMotor = Motor.B;
	private final NXTRegulatedMotor usSensor = Motor.C;
	private double[] x1 = {60.96, 30.48, 30.48, 60.96}; //x coordinates that robot must travel to; for demo 1
	private double[] y1 = {30.48, 30.48, 60.96, 0.0}; //y coordinates that robot must travel to; for demo 1
	private double[] x2 = {0.0, 60.96}; //x coordinates that robot must travel to; for demo 2
	private double[] y2 = {60.96, 0.0}; //y coordinates that robot must travel to; for demo 2
	
	/*class variables*/
	private Odometer odometer;
	private UltrasonicPoller usPoller;
	private BangBangController bangbang;
	private boolean isNavigating;
	private double x; //value passed by Lab 3 and set through setter, where i want to go or my destination x coordinate
	private double y; //value passed by Lab 3 and set through setter, where i want to go or my destination y coordinate
	private static double dX; 
	private static double dY;
	private static double distance;
	private static double thetaHeading;
	private static int demo;
	private static boolean obstacleAhead;
	
	public Navigation(Odometer odometer, UltrasonicPoller usPoller, BangBangController bangbang)
	{
		this.odometer = odometer;
		this.usPoller = usPoller;
		this.bangbang = bangbang;
	}
	
	//THREAD STARTS//
	public void run()
	{
		
		//DEMO PART 1 //
		if (demo == 1) //checks which demo
		{
			for (int i=0; i<4; i++)
			{	
				isNavigating = true;
				while (isNavigating) //isNavigating can be set to false inside travelTo() if destination is reached. 
				{
					travelTo(x1[i],y1[i]);
				}
			}
		}
		
		else if (demo == 2) //checks which demo
		{
			for (int i=0; i<2; i++)
			{	
				isNavigating = true;
				if (i == 1)
				{
					obstacleAhead = true;
				}
				while (isNavigating) //isNavigating can be set to false inside travelTo() if destination is reached. 
				{
					travelTo(x2[i],y2[i]);
				}
			}
			
		}
		
	}
	//THREAD ENDS//
	
	public void travelTo(double x, double y) //this function should be constantly called along the path of reaching your destination
	{	
		/* BEGIN: OBSTACLE AVOIDANCE*/
		if (obstacleAhead) //true if I am in demo2, and travelling to (60,0), and not yet past the obstacle
		{
				if (usDistance() <= 10) //turn on wall follower bang bang style if I am getting close to the obstacle
				{
					while (obstacleAhead)
					{
					Sound.beep();
					leftMotor.setSpeed(ROTATE_SPEED);
					rightMotor.setSpeed(ROTATE_SPEED);

					leftMotor.rotate(convertAngle(leftRadius, width, 90.0), true);	//wall encountered: rotate robot 90 degrees
					rightMotor.rotate(-convertAngle(rightRadius, width, 90.0), false); //wall encountered: rotate robot 90 degrees
					usSensor.rotateTo(-90, false); //rotate sensor to counter-clockwise so as to face the wall
					
					bangbang.turnON();
					try {
						Thread.sleep(8500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} //give the robot approximately 10 seconds to navigate past the obstacle
					
					bangbang.turnOFF();
					obstacleAhead = false; //now I went past the obstacle I don't have to enter this if statement again and bang bang won't be turned on.
					}
				}
		}
		/* END: OBSTACLE AVOIDANCE*/
		
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
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		
		}
		
		//Motor.A.rotate(convertDistance(leftRadius, distance), true);
		//Motor.B.rotate(convertDistance(rightRadius, distance), false);
	
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
		return bangbang.readUSDistance(); 
	}
	
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
	
	public void setDemo(int demoNumber)
	{
		demo = demoNumber;
	}
	

}
