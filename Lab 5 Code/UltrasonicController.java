//Lilly Tong - 260459522
//Ravi Chaganti - 260469339
public interface UltrasonicController {
	
	public void processUSData(int distance); //public method so can be called from UltrasonicPoller...
	
	public int readUSDistance();
}

//interface is a general category... i.e. ultrasonic controller is a general category, however the actual class implementing this category could be of type PController or BangBangController.... but both Pcontroller and BangbangController should include all the methods existing in the interface, BUT the code in "processUSData" for Pcontroller class may be different from "processUSData" for BangBangcontroller class... but what is important is that they both have the common function "processUSData"
