import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.*;

public class BangBangController implements UltrasonicController{
	private final int bandCenter, bandwidth;
	private final int motorLow, motorHigh;
	private final int motorStraight = 150, FILTER_OUT = 65;
	private final NXTRegulatedMotor leftMotor = Motor.A, rightMotor = Motor.B;
	private int distance;
	private int filterControl;
	private boolean ON = false;
	
	public BangBangController(int bandCenter, int bandwidth, int motorLow, int motorHigh) {
		//Default Constructor
		this.bandCenter = bandCenter;
		this.bandwidth = bandwidth;
		this.motorLow = motorLow;
		this.motorHigh = motorHigh;
		/*
		leftMotor.setSpeed(motorStraight);
		rightMotor.setSpeed(motorStraight);
		leftMotor.forward();
		rightMotor.forward();
		filterControl = 0;
		*/
	}
	
	@Override
	public void processUSData(int distance) {
		this.distance = distance; //actual distance to the wall at this point
		if (ON)
		{
			// TODO: process a movement based on the us distance passed in (BANG-BANG style)
			
			/* GAP FILTER STARTS */
				if (distance == 255 && filterControl < FILTER_OUT) 
				{
						// bad value, do not set the distance var, however do increment the filter value
						filterControl ++;
						this.distance = 23; //treat it as "within range case" case, move cart forward
				} 
				else if (distance == 255)
				{
						// true 255, therefore set distance to 255
						this.distance = distance;
				} 
				else 
				{
						// distance went below 255, therefore reset everything.
						filterControl = 0;
						this.distance = distance;
				}
			/*GAP FILTER ENDS*/ 
				
	        if (this.distance <= 15) //too close
	        {	
	        	leftMotor.forward();
	        	rightMotor.backward(); //sharp turn
	        	
	        	leftMotor.setSpeed(motorHigh);
	        	rightMotor.setSpeed(motorLow);
	        }
			
	        else if (this.distance > 15 && this.distance <= 18 ) //just right
	        {
	        	leftMotor.forward();
	        	rightMotor.forward();
	        	
	        	leftMotor.setSpeed(motorStraight); //previously 350, now 200
	        	rightMotor.setSpeed(motorStraight);
	        }
	        
	        else //too far
	        {
	       
	        		
	        		leftMotor.forward();
		        	rightMotor.forward();
		        		
		        	leftMotor.setSpeed(motorLow);
		        	rightMotor.setSpeed(motorHigh);
	        	
	    
	        }
		}
	}
	
	public void turnON()
	{
		ON = true;
	}
	
	public void turnOFF()
	{
		ON = false;
	}

	@Override
	public int readUSDistance() {
		return this.distance;
	}
}
