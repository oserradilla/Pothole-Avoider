package com.oscarsc.potholeavoider.incidences;

import android.location.Location;

import com.oscarsc.potholeavoider.R;
import com.oscarsc.potholeavoider.listeners.GpsListener;

import custom_incidences.LibCurve;

public class Curve extends Incidence{

	private static final long serialVersionUID = 1L;
	
	public transient final static int descriptionId = R.string.curveDescription;
	public transient final static int rightImageId = R.drawable.curve_right_img;
	public transient final static int leftImageId = R.drawable.curve_left_img;

	private LibCurve curve;
	
	public Curve(LibCurve curve){
		this.curve=curve;
	}
	public Curve(int id,Location location, int magnitude, boolean isRight) {
		// TODO Substitue 0 0 and -1 by previous location (lat and lon)
		curve = new LibCurve(-id,location.getLatitude(), location.getLongitude(), 0,
				0, location.getAccuracy(), magnitude, isRight);
	}

	@Override
	public String toString() {
		return "Dangerous" + ((curve.isRight()) ? "right" : "left")
				+ " curve of magnitude " + curve.getMagnitude() + " in coordinates: "
				+ locationToString();
	}

	@Override
	public String voiceSpanish(int meters) {
		return "Curva pronunciada hacia la "
				+ ((curve.isRight()) ? "derecha" : "izquierda"); /*+ " de magnitud "
				+ curve.getMagnitude() + " a " + meters + " metros";*/
	}

	@Override
	public String voiceEnglish(int meters) {
		return "Dangerous curve to " + ((curve.isRight()) ? "right" : "left")
				+ " of magnitude " + curve.getMagnitude() + " to " + meters + " meters";
	}

	@Override
	public int getImage() {
		return (curve.isRight()) ? rightImageId : leftImageId;
	}
	@Override
	public String locationToString() {
		return GpsListener.locationToString(curve.getLatitude(),curve.getLongitude());
	}
	@Override
	public int getMagnitude() {
		return curve.getMagnitude();
	}
	@Override
	public Location getLocation() {
		Location location=new Location(" ");
		location.setLatitude(curve.getLatitude());
		location.setLongitude(curve.getLongitude());
		return location;
	}

    @Override
    public int getId() {
        return curve.getIncidenceId();
    }

    @Override
    public void setLocation(Location location) {
        curve.setLatitude(location.getLatitude());
        curve.setLongitude(location.getLongitude());
    }

    @Override
    public void setPrevLocation(Location prevLocation) {
        curve.setPrevLat(prevLocation.getLatitude());
        curve.setPrevLon(prevLocation.getLongitude());
    }

    @Override
    public Location getPrevLocation() {
        Location prevLocation=new Location("prevLocation");
        prevLocation.setLatitude(curve.getPrevLat());
        prevLocation.setLongitude(curve.getPrevLon());
        return prevLocation;
    }

    public LibCurve getInternalCurve(){
        return curve;
    }
}
