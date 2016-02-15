package custom_incidences;

public class LibPothole extends LibIncidenceMagnitude{
	public LibPothole(){}
	
	public LibPothole(int incidenceId, double latitude, double longitude, double prevLat,
			double prevLon, float accuracy, int magnitude) {
		super(incidenceId, latitude, longitude, prevLat, prevLon, accuracy, magnitude);
	}
}
