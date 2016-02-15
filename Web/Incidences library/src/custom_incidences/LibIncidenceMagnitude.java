package custom_incidences;

public abstract class LibIncidenceMagnitude extends LibIncidence{
	int magnitude;
	
	public LibIncidenceMagnitude(){}
	
	public LibIncidenceMagnitude(int incidenceId, double latitude, double longitude,
							double prevLat, double prevLon, float accuracy,int magnitude) {
		super(incidenceId,latitude, longitude, prevLat, prevLon, accuracy);
		this.magnitude=magnitude;
	}

	public int getMagnitude() {
		return magnitude;
	}

	public void setMagnitude(int magnitude) {
		this.magnitude = magnitude;
	}
	
}
