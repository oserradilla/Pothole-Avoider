package com.oscarsc.potholeavoider.services;

import android.location.Location;

/**
 * Created by oscar on 17/02/2016.
 */
public class MockLocation extends Location {

    private double latitude;
    private double longitude;

    public MockLocation(String provider) {
        super(provider);
    }

    @Override
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @Override
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public double getLatitude() {
        return latitude;
    }

    @Override
    public double getLongitude() {
        return longitude;
    }

    @Override
    public float getAccuracy() {
        return 0f;
    }

    @Override
    public String toString(){
        return "";
    }
}
