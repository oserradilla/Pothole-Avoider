package com.oscarsc.potholeavoider.incidences;

import java.io.Serializable;

import android.location.Location;

import com.oscarsc.potholeavoider.notifications.MyTextToSpeech;

public abstract class Incidence implements Serializable{
    boolean isVisited=false;

	private static final long serialVersionUID = 1L;
	@Override
	public abstract String toString();
	
	public abstract String voiceSpanish(int meters);
	
	public abstract String voiceEnglish(int meters);
	
	public String voiceSelectedLanguage(int meters){
		if(MyTextToSpeech.language.equals("english"))
			return voiceEnglish(meters);
		else
			return voiceSpanish(meters);
	}
	
	public abstract int getImage();
	
	public abstract String locationToString();
	
	public abstract int getMagnitude();

	public abstract Location getLocation();

    public abstract int getId();

    public abstract void setLocation(Location location);

    public abstract void setPrevLocation(Location location);

    public abstract Location getPrevLocation();

    public boolean isVisited() {
        return isVisited;
    }

    public void setVisited(boolean isVisited) {
        this.isVisited = isVisited;
    }
}
