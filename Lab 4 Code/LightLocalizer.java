//Lilly Tong - 260459522
//Ravi Chaganti - 260469339
//October 7, 2013

import lejos.nxt.LCD;
import lejos.nxt.LightSensor;
import lejos.nxt.Motor;
import lejos.nxt.ColorSensor;
import lejos.nxt.Sound;


public class LightLocalizer{
	private Odometer odo;
	private TwoWheeledRobot robot;
	private ColorSensor cs;
    private Navigation nav;
    private static int threshold = 100;//difference between tile color and black line
    private int sensorAve = 0;
    private static final double CS_OFFSET=11; //distance between color sensor and center of rotation
    private static int ROTATION_SPEED = 60;
	public static int FORWARD_SPEED = 60;
	
	
	public LightLocalizer(Odometer odo, ColorSensor cs) {
		this.odo = odo;
		this.robot = odo.getTwoWheeledRobot();
		this.cs = cs;
		this.nav = odo.getNavigation();
		
		// turn on the light
		cs.setFloodlight(true);
	}
	
	public void doLocalization() {
		// drive to location listed in tutorial
		// start rotating and clock all 4 gridlines
		// do trig to compute (0,0) and 0 degrees
		// when done travel to (0,0) and turn to 0 degrees
		
		int counter=0; //initialize counter for black lines, now it's 0.
		
		double [] angle = new double [4]; //create an array to store the 4 angles of the black lines I am going to detect
    
		// Filter the light sensor
		try { Thread.sleep(1500); } catch (InterruptedException e) {}
		
		
		
		//first calculate the color of the tile (not the black line)
	    calibrateSensorAverage();
	    
	 // Rotate and clock the 4 grid lines
        robot.setRotationSpeed(-ROTATION_SPEED);
      //Detect the four black lines
      		while(counter < 4){
      			//displays the infos while it is looping 
      			LCD.drawInt((int)counter,0,5);
      			LCD.drawInt((int)sensorAve,0,6);
      			LCD.drawInt((int)cs.getRawLightValue(),0,7);

      			if (blackLineDetected()){
      				angle[counter]=odo.getTheta();
      				counter++;
      				try {//sleeping to avoid counting the same line twice
      					Thread.sleep(100);
      				} catch (InterruptedException e) {}
      			}
      			
      		}
      		

      		// Stop the robot
      		robot.setSpeeds(0, 0);
            

      		
      		//formula modified from the tutorial slides

      		double thetaX = (angle[3]-angle[1])/2; //last scanned line minus second scanned line divided by 2
      		double thetaY = (angle[2]-angle[0])/2; //third scanned line minus first scanned line divided by 2
      		double newX = -CS_OFFSET*Math.cos(Math.toRadians(thetaY)); //use pythagorean's theorem, -ve sign because I am in the 4th quadrant
      		double newY = -CS_OFFSET*Math.cos(Math.toRadians(thetaX)); //use pythagorean's theorem, -ve sign because I am in the 4th quadrant
      		double newTheta = 180 + thetaX - angle[3]; //theta correction
      		newTheta += odo.getTheta(); //new theta = current theta + theta correction
      		newTheta = Odometer.fixDegAngle(newTheta);

      		odo.setPosition(new double [] {newX, newY, newTheta}, new boolean [] {true, true, true}); //set my new x, y and theta

      		
      		// Travel to (0,0) and turn to 0 degrees when finished
      		//travel to may not be as accurate as the method we are using, so we did it this way:
            
      		nav.turnTo(0); //correct y position 
      		robot.travelDistance(-newY, FORWARD_SPEED);
      		nav.turnTo(90); //correct x position
      		robot.travelDistance(-newX, FORWARD_SPEED);
      		nav.turnTo(0); //turn so to head toward 0 degrees (positive y axis)
      	}
	
      	//calibrates the tile color's average
      	private int calibrateSensorAverage(){
      		int senValue=0;
      		//collects the average of a 5 samples of the tile color
      		for(int i=0;i<5;i++){
      			senValue+=cs.getRawLightValue();
      		}
      		senValue=senValue/5;
      		this.sensorAve = senValue;
      		return senValue;	
      	}
      	
      	// Helper method to detect the black line
      	private boolean blackLineDetected() {
      		return (cs.getRawLightValue() < sensorAve - threshold); //black line is detected if the color is below the tile's color by threshold
		    
		
	}

}
