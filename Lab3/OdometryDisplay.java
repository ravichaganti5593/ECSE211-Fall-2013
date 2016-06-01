/*
 * OdometryDisplay.java
 */
import lejos.nxt.*;

public class OdometryDisplay extends Thread {
	private Odometer odometer;
	private Navigation navigation;
	private UltrasonicController cont;

	// constructor
	public OdometryDisplay(Odometer odometer, Navigation navigation, UltrasonicController cont) {
		this.odometer = odometer;
		this.navigation = navigation;
		this.cont = cont;
	}

	// run method (required for Thread)
	public void run() {
		
		double[] position = new double[3];
		
		while (true) 
		{
				LCD.clear();
				LCD.drawString("X:              ", 0, 0);
				LCD.drawString("Y:              ", 0, 1);
				LCD.drawString("T:              ", 0, 2);
				LCD.drawString("US Distance: " + cont.readUSDistance(), 0, 3 );
				
				// get the odometry information
				odometer.getPosition(position, new boolean[] { true, true, true });
	
				// display odometry information
				for (int i = 0; i < 3; i++) {
				LCD.drawString(formattedDoubleToString(position[i], 2), 3, i);
				}
				
				
				try {
					Thread.sleep(200);
				} catch (Exception e) {
					System.out.println("Error: " + e.getMessage());
				}
		}
		
	}
	
	private static String formattedDoubleToString(double x, int places) {
		String result = "";
		String stack = "";
		long t;
		
		// put in a minus sign as needed
		if (x < 0.0)
			result += "-";
		
		// put in a leading 0
		if (-1.0 < x && x < 1.0)
			result += "0";
		else {
			t = (long)x;
			if (t < 0)
				t = -t;
			
			while (t > 0) {
				stack = Long.toString(t % 10) + stack;
				t /= 10;
			}
			
			result += stack;
		}
		
		// put the decimal, if needed
		if (places > 0) {
			result += ".";
		
			// put the appropriate number of decimals
			for (int i = 0; i < places; i++) {
				x = Math.abs(x);
				x = x - Math.floor(x);
				x *= 10.0;
				result += Long.toString((long)x);
			}
		}
		
		return result;
	}

}
