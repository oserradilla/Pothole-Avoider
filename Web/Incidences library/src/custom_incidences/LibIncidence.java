package custom_incidences;

public abstract class LibIncidence {
	int incidenceId;
	double latitude;
	double longitude;
	double prevLat;
	double prevLon;
	float accuracy;
	
	public LibIncidence(){}
	
	public LibIncidence(int incidenceId,double latitude, double longitude, double prevLat, double prevLon,	float accuracy){
		this.latitude=latitude;
		this.longitude=longitude;
		this.prevLat=prevLat;
		this.prevLon=prevLon;
		this.accuracy=accuracy;
	}
	public int getIncidenceId(){
		return incidenceId;
	}
	public void setIncidenceId(int incidenceId){
		this.incidenceId=incidenceId;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public double getPrevLat() {
		return prevLat;
	}
	public void setPrevLat(double prevLat) {
		this.prevLat = prevLat;
	}
	public double getPrevLon() {
		return prevLon;
	}
	public void setPrevLon(double prevLon) {
		this.prevLon = prevLon;
	}
	public float getAccuracy() {
		return accuracy;
	}
	public void setAccuracy(float accuracy) {
		this.accuracy = accuracy;
	}
}
