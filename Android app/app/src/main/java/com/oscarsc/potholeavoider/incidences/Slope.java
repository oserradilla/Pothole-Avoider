package com.oscarsc.potholeavoider.incidences;

import com.oscarsc.potholeavoider.R;
import com.oscarsc.potholeavoider.listeners.GpsListener;

import android.location.Location;

import custom_incidences.LibSlope;

public class Slope extends Incidence{
	private static final long serialVersionUID = 1L;
    public transient final static int descriptionId = R.string.slopeDescription;
	public final static int imageUpId = R.drawable.slope_up_img;
	public final static int imageDownId = R.drawable.slope_down_img;
	LibSlope slope;
	public Slope(LibSlope slope){
		this.slope=slope;
	}
	public Slope(int id,Location location, int magnitude,int slopeValue) {
        Location endLocation=new Location("");
        endLocation.setLatitude(0);
        endLocation.setLongitude(0);
		slope=new LibSlope(id,location.getLatitude(), location.getLongitude(), 0,
				0, location.getAccuracy(), magnitude,endLocation.getLatitude(),endLocation.getLongitude(),slopeValue);
	}
	@Override
	public String toString() {
		return "Slope of magnitude "+slope.getMagnitude()+" in coordinates: \n"+locationToString()+"\nSlope: "+
			String.valueOf(slope.getSlope());
	}
	@Override
	public String voiceSpanish(int meters) {
        return "Pendiente peligrosa con un desnivel del "+String.valueOf(slope.getSlope())+" porcien";
		/*return "Cuesta de magnitud "+slope.getMagnitude()+" a "+meters+" metros"+". Con una pendiente del "+
				String.valueOf(slope.getSlope())+" porcien";*/
	}
	@Override
	public String voiceEnglish(int meters) {
		return "Slope of magnitude "+slope.getMagnitude()+" to "+meters+" meters"+". With a slope of "+
				String.valueOf(slope.getSlope())+" percent";
	}
	@Override
	public int getImage() {
		if(slope.getSlope()<0)
			return imageDownId;
		return imageUpId;
	}
	@Override
	public String locationToString() {
		return GpsListener.locationToString(slope.getLatitude(),slope.getLongitude());
	}
	@Override
	public int getMagnitude() {
		return slope.getMagnitude();
	}
	@Override
	public Location getLocation() {
		Location location=new Location(" ");
		location.setLatitude(slope.getLatitude());
		location.setLongitude(slope.getLongitude());
		return location;
	}
    public int getSlope(){
        return slope.getSlope();
    }

    @Override
    public void setLocation(Location location) {
        slope.setLatitude(location.getLatitude());
        slope.setLongitude(location.getLongitude());
    }

    @Override
    public void setPrevLocation(Location prevLocation) {
        slope.setPrevLat(prevLocation.getLatitude());
        slope.setPrevLon(prevLocation.getLongitude());
    }

    @Override
    public Location getPrevLocation() {
        Location prevLocation=new Location("prevLocation");
        prevLocation.setLatitude(slope.getPrevLat());
        prevLocation.setLongitude(slope.getPrevLon());
        return prevLocation;
    }


    public LibSlope getInternalSlope(){
        return slope;
    }
    @Override
    public int getId() {
        return slope.getIncidenceId();
    }
}
