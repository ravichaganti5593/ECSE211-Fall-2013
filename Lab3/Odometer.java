/*
 * Odometer.java
 */

//NOTE to self: you can only RETRIEVE int and FEED int into lejos nxt functions, but you can still perform math on doubles!

import lejos.nxt.*;

public class Odometer extends Thread {
	
	/*CLASS CONSTANTS*/
	
	private static final long ODOMETER_PERIOD = 25; //odometer update period in ms
	private static double leftRadius; // radius of left wheel in cm
	private static double rightRadius; // radius of right wheel in cm
	private static double width; //width between the two wheels

	
	/*CLASS VARIABLES*/
	private final NXTRegulatedMotor leftMotor = Motor.A, rightMotor = Motor.B;
	private double x, y, theta; //robot position
	private double dC, dTR; //average change in arclength, change in ROTATIONAL THETA
	private double lastLeftTacho; //previous left tacho count in rads
	private double lastRightTacho; //previous right tacho count in rads
	private double currentLeftTacho; //current left tacho count in rads
	private double currentRightTacho; //current right tacho count in rads
	private double dLeftTacho; //change in tacho count between consecutive measurements
	private double dRightTacho; //change in tacho count between consecutive measurements

	// lock object for mutual exclusion
	private Object lock;

	/*CONSTRUCTOR*/
	public Odometer(double leftRadius, double rightRadius, double width) {
		
		this.leftRadius = leftRadius;
		this.rightRadius = rightRadius;
		this.width = width;
		x = 0.0;
		y = 0.0;
		theta = 0.0;
		lock = new Object();
		currentLeftTacho = 0;
		currentRightTacho = 0;
	}

	// run method (required for Thread)
	public void run() {
		long updateStart, updateEnd;

		while (true) {
			updateStart = System.currentTimeMillis();
			
			/*ODOMETER STARTS*/
			
			lastLeftTacho = currentLeftTacho; //store the value of currentLeftTacho into lastLeftTacho before I update my currentLeftTacho
			lastRightTacho = currentRightTacho; //store the value of currentRightTacho into lastRightTacho before I update my currentRightTacho
			
			currentLeftTacho = Math.PI/180*leftMotor.getTachoCount();//getTachoCount returns answer in degrees, so convert it to radians first
			currentRightTacho = Math.PI/180*rightMotor.getTachoCount();//*degToRad/100; //getTachoCount returns answer in degrees, so convert it to radians first
			
			dLeftTacho = currentLeftTacho - lastLeftTacho; //change in tacho count during this period
			dRightTacho = currentRightTacho - lastRightTacho; //change in tacho count during this period, negative when it is turning clockwise
			
			dC = (dLeftTacho * leftRadius + dRightTacho * rightRadius)/2; //arclength
			dTR = (dLeftTacho * leftRadius - dRightTacho * rightRadius)/width; //-ve means turning to the left, +ve means turning to the right
			
			/*ODOMETER ENDS*/

			synchronized (lock) {
				// don't use the variables x, y, or theta anywhere but here!
				//theta = -0.7376;
				
				/*UPDATE OF VALUES BEGIN*/

				theta = theta + dTR*180/(Math.PI); //in degrees
				x = x + dC*Math.sin(theta*Math.PI/180); //must convert theta to radians
				y = y + dC*Math.cos(theta*Math.PI/180); //must convert theta to radians
				
				/*UPDATE OF VALUES END*/
				
				
			}

			// this ensures that the odometer only runs once every period
			updateEnd = System.currentTimeMillis();
			if (updateEnd - updateStart < ODOMETER_PERIOD) { //if this loop finishes executing but still some time left in odometer period, go to sleep until this loop is done.
				try {
					Thread.sleep(ODOMETER_PERIOD - (updateEnd - updateStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometer will be interrupted by
					// another thread
				}
			}
		}
	}

	// accessors
	public void getPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				position[0] = x;
			if (update[1])
				position[1] = y;
			if (update[2])
				position[2] = theta;
		}
	}

	public double getX() {
		double result;

		synchronized (lock) {
			result = x;
		}

		return result;
	}

	public double getY() {
		double result;

		synchronized (lock) {
			result = y;
		}

		return result;
	}

	public double getTheta() {
		double result;

		synchronized (lock) {
			result = theta;
		}

		return result;
	}

	// mutators
	public void setPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				x = position[0];
			if (update[1])
				y = position[1];
			if (update[2])
				theta = position[2];
		}
	}

	public void setX(double x) {
		synchronized (lock) {
			this.x = x;
		}
	}

	public void setY(double y) {
		synchronized (lock) {
			this.y = y;
		}
	}

	public void setTheta(double theta) {
		synchronized (lock) {
			this.theta = theta;
		}
	}
}