package custom_incidences;

public class LibCurve extends LibIncidenceMagnitude{
	boolean isRight;
	public LibCurve(){}
	
	public LibCurve(int incidenceId, double latitude, double longitude, double prevLat,
			double prevLon, float accuracy, int magnitude,boolean isRight) {
		super(incidenceId,latitude, longitude, prevLat, prevLon, accuracy, magnitude);
		this.isRight=isRight;
	}
	public boolean isRight() {
		return isRight;
	}
	public void setRight(boolean isRight) {
		this.isRight = isRight;
	}
}
