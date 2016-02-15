package custom_incidences;

public class LibSlope extends LibIncidenceMagnitude {
	double endLat;
	double endLon;
	int slope;

	public LibSlope(){}
	
	public LibSlope(int incidenceId, double latitude, double longitude, double prevLat,
			double prevLon, float accuracy, int magnitude, double endLat,
			double endLon, int slope) {
		super(incidenceId, latitude, longitude, prevLat, prevLon, accuracy, magnitude);
		this.endLat=endLat;
		this.endLon=endLon;
		this.slope=slope;
	}

	public double getEndLat() {
		return endLat;
	}

	public void setEndLat(double endLat) {
		this.endLat = endLat;
	}

	public double getEndLon() {
		return endLon;
	}

	public void setEndLon(double endLon) {
		this.endLon = endLon;
	}

	public int getSlope() {
		return slope;
	}

	public void setSlope(int maxSlope) {
		this.slope = maxSlope;
	}
}
