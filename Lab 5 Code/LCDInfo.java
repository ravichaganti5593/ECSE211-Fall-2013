//Lilly Tong - 260459522
//Ravi Chaganti - 260469339
//October 20, 2013

import lejos.nxt.LCD;

import lejos.nxt.*;
import lejos.nxt.UltrasonicSensor;
import lejos.util.Timer;
import lejos.util.TimerListener;

public class LCDInfo implements TimerListener{ //TIME LISTENER LINE 1
	public static final int LCD_REFRESH = 100; //TIME LISTENER LINE 2
	private Odometer odo;
	private Timer lcdTimer; //TIME LISTENER LINE 3
	private UltrasonicSensor us;
	private USLocalizer usl;
	private ColorSensor cs;
	private ObjectDetection od;
	private String[] objectType;
	
	// arrays for displaying data
	private double [] pos;
	
	public LCDInfo(Odometer odo, UltrasonicSensor us, USLocalizer usl, ColorSensor cs, ObjectDetection od) {
		this.odo = odo;
		this.lcdTimer = new Timer(LCD_REFRESH, this); //TIME LISTENER LINE 4
		this.us = us;
		this.usl = usl;
		this.cs = cs;
		this.od = od;
		
		// initialise the arrays for displaying data
		pos = new double [3];
		
		// start the timer
		//lcdTimer.start(); //TIME LISTENER LINE 5
	}
	
	public void timedOut() { //TIME LISTENER LINE 6
		
		objectType = od.getObjectType(); //get what kind of object is detected
		
		odo.getPosition(pos);
		LCD.clear();
		LCD.drawString("X: ", 0, 0);
		LCD.drawString("Y: ", 0, 1);
		LCD.drawString("T: ", 0, 2);
		LCD.drawString("USDist: " + us.getDistance(), 0, 3); //displays raw data read from ultrasonic sensor
		LCD.drawString("Color: " + od.getBRRatio(), 0, 4);
		LCD.drawString("Object: " + objectType[0], 0, 5);

		
		LCD.drawInt((int)(pos[0] * 10), 3, 0);
		LCD.drawInt((int)(pos[1] * 10), 3, 1);
		LCD.drawInt((int)pos[2], 3, 2);
		
		try {
			Thread.sleep(50);
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}
	
	public Timer getLCDTimer()
	{
		return this.lcdTimer;
	}
}
