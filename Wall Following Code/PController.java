//Lilly Tong - 260459522
//Ravi Chaganti - 260469339
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.*;

/*if code doesn't work for gap, try increasing bandcenter, then tilt the angle more toward the front!*/

public class PController implements UltrasonicController {
	
	private final int bandCenter, bandwidth;
	private final int motorStraight = 230, FILTER_OUT = 70;
	private final int wallDist=22;
	private final NXTRegulatedMotor leftMotor = Motor.A, rightMotor = Motor.C;	
	private int distance;
	private int filterControl;
	
	public PController(int bandCenter, int bandwidth) {
		//Default Constructor
		this.bandCenter = bandCenter;
		this.bandwidth = bandwidth;
		leftMotor.setSpeed(motorStraight);
		rightMotor.setSpeed(motorStraight);
		leftMotor.forward();
		rightMotor.forward();
		filterControl = 0;
	}
	
	@Override
	public void processUSData(int distance) {
	
		
		/*GAP FILTER STARTS*/
		if (distance == 255 && filterControl < FILTER_OUT) 
		{
			// bad value, do not set the distance var, however do increment the filter value
			filterControl ++;
			this.distance = 24; //treat it as "within range" case, move cart forward
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
		
		int diff=calcProp(Math.abs(this.distance-wallDist));
		
		if (this.distance < 22) //too close
			
		{
				leftMotor.forward();
				rightMotor.backward(); //for very sharp turn
        	
				leftMotor.setSpeed(motorStraight+diff);
				//NOTE: I CANNOT SET rightMotor.setSpeed(motorStraight-diff); SINCE RIGHT MOTOR IS MOVING BACKWARDS!
			
				if (diff <= 50) //proportional control that is diff-dependent.
				{
					rightMotor.setSpeed(diff);
				}
				
				else //special case for diff>=50
				{
					rightMotor.setSpeed(30);
				}
		}
			
		
		else if (this.distance >= 22 && this.distance < 26) //within range
		{
			leftMotor.forward();
			rightMotor.forward();
			
			leftMotor.setSpeed(motorStraight);
			rightMotor.setSpeed(motorStraight);
		}
		
		else // too far
		{
		
				if (this.distance >=32 && this.distance <= 255) // >=32 is the special case, and 255 is also in this case, out of range, needs a sharp turn
	        	{
	   
	        		
	        		leftMotor.forward();
		        	rightMotor.forward();
		        		
		        	leftMotor.setSpeed(250);
		        	rightMotor.setSpeed(550);
	        	}
	    
		
			
			else //regular proportional control
			{
				leftMotor.forward();
				rightMotor.forward();

				leftMotor.setSpeed(motorStraight-diff);
				rightMotor.setSpeed(motorStraight+diff);
			}
		}
	
	
	}
	
	public int calcProp (int error){ //function for proportional control
		return (int)(1.5*Math.pow(error, 2));
	}
	
	@Override
	public int readUSDistance() {
		return this.distance;
	}

}