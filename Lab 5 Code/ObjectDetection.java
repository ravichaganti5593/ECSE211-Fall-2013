//Lilly Tong - 260459522
//Ravi Chaganti - 260469339
import java.util.Arrays;

import lejos.nxt.*;
import lejos.nxt.ColorSensor.Color;
import lejos.util.Delay;
import lejos.util.Timer;
import lejos.util.TimerListener;

public class ObjectDetection implements TimerListener {
	
	public static final int PERIOD = 25; //period at which obstacle detection collects data
	private static final int ROTATE_SPEED = 20;
	private Timer odTimer; //create a timer
	
	private NXTRegulatedMotor leftMotor = Motor.A;
	private NXTRegulatedMotor rightMotor = Motor.B;
	private NXTRegulatedMotor usMotor = Motor.C;
	private Navigation navigation;
	private UltrasonicSensor us;
	private Odometer odometer;
	private TwoWheeledRobot robot;
	private static ColorSensor cs;
	private int distance;
	private static int objectType; //Type of object detected: 0 is for block, 1 is for foam, -1 is for none
	private static double BRRatio;
	
	private Object lock;
	private static boolean pickedUp = false;
	private static boolean shouldPickUp = false;
	
	public ObjectDetection(UltrasonicSensor us, Odometer odometer, ColorSensor cs, TwoWheeledRobot robot, Navigation navigation)
	{
		this.odTimer = new Timer(PERIOD, this); //set up timer
		this.us = us;
		this.odometer = odometer;
		this.cs = cs;
		this.robot = robot;
		this.navigation = navigation;
		this.objectType = -1; //default value for type of object detected: no object detected, since "-1" is for none
		BRRatio = 0;
		lock = new Object();
		
		//start the timer, if there is a timedOut function
	}
	
	public void timedOut()
	{
		//void method
	}
	
	public static double getRawRatio() //return raw ratio between blue and red
	{
		double[] red = new double[3]; //take 3 samples of red value
		double[] blue = new double[3]; //take 3 samples of blue value
		
		for (int i=0; i<3; i++)
		{
			Color color = cs.getRawColor(); //get raw color
			red[i]=color.getRed(); //record red value
			blue[i]=color.getBlue(); //record blue value

		}
		
		double redAvg = (red[0]+red[1]+red[2])/3.0; //take average of 3 values
		double blueAvg = (blue[0]+blue[1]+blue[2])/3.0; //take average of 3 values
		BRRatio = blueAvg/redAvg; //calculate blue to red ratio
		
		//if < 0.99, you know it's a block!!!
	
		return BRRatio;
	}
	
	public void colorIdentification()
	{
		double[] red = new double[3];
		double[] blue = new double[3];
		double[] green = new double[3];
		
		for (int i=0; i<3; i++)
		{
			Color color = cs.getRawColor();
			red[i]=color.getRed();
			blue[i]=color.getBlue();

		}
		
		double redAvg = (red[0]+red[1]+red[2])/3.0;
		double blueAvg = (blue[0]+blue[1]+blue[2])/3.0;
		BRRatio = blueAvg/redAvg;
		
		if ( BRRatio > 1) //if the ratio of blue to red is more than 1
		{
			Sound.beep(); //foam
			objectType = 1;
		}
		
		else 
		{
			Sound.buzz(); //block
			objectType = 0;
		}
			
	}
	
	public double getBRRatio()
	{
		return BRRatio;
	}
	
	public static int getObjectInt()
	{
		return objectType; //returns in integer what kind of object was detected
	}
	
	private int getFilteredData()
	{	
		us.ping();
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
		}
		
		int distance = us.getDistance();
		
		if (distance > 80) //for objects outside my radius of sweep
		{
			distance = 80;
		}
		
		return distance;
	}
	
	public static void setObjectType(int type)
	{
		objectType = type;
	}
	public String[] getObjectType()
	{
		String[] typeOfObject=new String[1]; //create a String array of size 1 to store what type of object is detected
		
		if(objectType == 0) 
		{
			typeOfObject[0]="Block";
		}
		
		else if(objectType == 1)
		{
			typeOfObject[0]="Foam";
		}
		
		else
		{
			typeOfObject[0]="None";
		}
		
		return typeOfObject; //return the entire String array, but remember to retrieve the value, one must retrieve this array's 0th index.
	}
	
}
