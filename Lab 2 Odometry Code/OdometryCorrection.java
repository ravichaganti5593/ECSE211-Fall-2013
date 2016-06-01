/* 
 * OdometryCorrection.java
 */

import lejos.nxt.*;

public class OdometryCorrection extends Thread {
	private static final long CORRECTION_PERIOD = 10;
	private static final double offset = 12.5; //the offset is the distance between color sensor and center of robot in cm;
	private static final int blackThreshold = 400;  //below this value would be a black line
	private double angle;
	private double x;
	private double y;
	private Odometer odometer;
	private ColorSensor colorsensor;
	private int colorValue; //stores color detected by sensor in here
	

	// constructor
	public OdometryCorrection(Odometer odometer, ColorSensor colorsensor) {
		this.odometer = odometer;
		this.colorsensor = colorsensor;
		angle = 0;
		x = 0;
		y = 0;
	}

	// run method (required for Thread)
	public void run() {
		long correctionStart, correctionEnd;

		while (true) {
			correctionStart = System.currentTimeMillis();
			/*CORRECTION STARTS*/
		
			colorValue = colorsensor.getRawLightValue();
			
			angle = Math.abs(odometer.getTheta());
			x = odometer.getX();
			y = odometer.getY();
		
			if (colorValue <= blackThreshold) //if it is a black line
			{
			
				if (angle <= 1) 
				{
					if (y>10 && y<31)
					{
						odometer.setY(15+offset);
						Sound.beep();
					}
					
					else if (y>45 && y<65)
					{
						odometer.setY(45+offset);
						Sound.beep();
					}
				}
				
				else if (angle >= 89 && angle <=91)
				{
					if (x>10 && x<31)
					{
						odometer.setX(15+offset);
						Sound.beep();
					}
					
					else if (x>50 && x<65)
					{
						odometer.setX(45+offset);
						Sound.beep();
					}
				}
				else if (angle >= 179 && angle <= 181)
				{
					if (y>20 && y<42)
					{
						odometer.setY(45-offset);
						Sound.beep();
					}
					
					else if (y<15)
					{
						odometer.setY(15-offset);
						Sound.beep();
					}
				}
				
				else if (angle >= 269 && angle <= 271)
				{
					if (x>20 && x<42)
					{
						odometer.setX(45-offset);
						Sound.beep();
					}
					
					else if (x<15)
					{
						odometer.setX(15-offset);
						Sound.beep();
					}
				}
			}
			
			
			/*CORRECTION ENDS*/
			// this ensure the odometry correction occurs only once every period
			correctionEnd = System.currentTimeMillis();
			if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
				try {
					Thread.sleep(CORRECTION_PERIOD
							- (correctionEnd - correctionStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometry correction will be
					// interrupted by another thread
				}
			}
		}
	}
}