//Lilly Tong - 260459522
//Ravi Chaganti - 260469339
//October 7, 2013

import lejos.nxt.LCD;
import lejos.nxt.UltrasonicSensor;
import lejos.util.Timer;
import lejos.util.TimerListener;

public class LCDInfo implements TimerListener{
	public static final int LCD_REFRESH = 100;
	private Odometer odo;
	private Timer lcdTimer;
	private UltrasonicSensor us;
	private USLocalizer usl;
	
	// arrays for displaying data
	private double [] pos;
	
	public LCDInfo(Odometer odo, UltrasonicSensor us, USLocalizer usl) {
		this.odo = odo;
		this.lcdTimer = new Timer(LCD_REFRESH, this);
		this.us = us;
		this.usl = usl;
		
		// initialise the arrays for displaying data
		pos = new double [3];
		
		// start the timer
		lcdTimer.start();
	}
	
	public void timedOut() { 
		odo.getPosition(pos);
		LCD.clear();
		LCD.drawString("X: ", 0, 0);
		LCD.drawString("Y: ", 0, 1);
		LCD.drawString("T: ", 0, 2);
		LCD.drawString("USDist:" + us.getDistance(), 0, 3); //displays raw data read from ultrasonic sensor
		
		LCD.drawInt((int)(pos[0] * 10), 3, 0);
		LCD.drawInt((int)(pos[1] * 10), 3, 1);
		LCD.drawInt((int)pos[2], 3, 2);
		
		try {
			Thread.sleep(50);
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}
}
