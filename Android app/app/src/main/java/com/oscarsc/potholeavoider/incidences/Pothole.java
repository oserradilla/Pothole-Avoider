package com.oscarsc.potholeavoider.incidences;

import android.location.Location;

import com.oscarsc.potholeavoider.R;
import com.oscarsc.potholeavoider.listeners.GpsListener;

import custom_incidences.LibPothole;

public class Pothole extends Incidence{

	private static final long serialVersionUID = 1L;
	public final static int descriptionId = R.string.potholeDescription;
	public final static int imageId = R.drawable.pothole_img;
	LibPothole pothole;
	public Pothole(LibPothole pothole){
		this.pothole=pothole;
	}
	public Pothole(int id,Location location,int magnitude) {
		pothole=new LibPothole(id,location.getLatitude(), location.getLongitude(), 0,
				0, location.getAccuracy(), magnitude);
	}
	@Override
	public String toString() {
		return "Pothole of magnitude "+pothole.getMagnitude()+" in coordinates: \n"+locationToString();
	}
	@Override
	public String voiceSpanish(int meters) {
		return "Bache peligroso a "+meters+" metros";//de magnitud "+pothole.getMagnitude()+" a "+meters+" metros";
	}
	@Override
	public String voiceEnglish(int meters) {
		return "Pothole of magnitude "+pothole.getMagnitude()+" to "+meters+" meters";
	}
	@Override
	public int getImage() {
		return imageId;
	}
	@Override
	public String locationToString() {
		return GpsListener.locationToString(pothole.getLatitude(),pothole.getLongitude());
	}
	@Override
	public int getMagnitude() {
		return pothole.getMagnitude();
	}
	@Override
	public Location getLocation() {
		Location location=new Location(" ");
		location.setLatitude(pothole.getLatitude());
		location.setLongitude(pothole.getLongitude());
		return location;
	}
    @Override
    public int getId() {
        return pothole.getIncidenceId();
    }

    @Override
    public void setLocation(Location location) {
        pothole.setLatitude(location.getLatitude());
        pothole.setLongitude(location.getLongitude());
    }

    @Override
    public void setPrevLocation(Location prevLocation) {
        pothole.setPrevLat(prevLocation.getLatitude());
        pothole.setPrevLon(prevLocation.getLongitude());
    }

    @Override
    public Location getPrevLocation() {
        Location prevLocation=new Location("prevLocation");
        prevLocation.setLatitude(pothole.getPrevLat());
        prevLocation.setLongitude(pothole.getPrevLon());
        return prevLocation;
    }


    public LibPothole getInternalPothole(){
        return pothole;
    }
}
