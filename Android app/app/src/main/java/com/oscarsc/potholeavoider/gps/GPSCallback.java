package com.oscarsc.potholeavoider.gps;

import android.location.Location;

public interface GPSCallback
{
        public abstract void onGPSUpdate(Location location);
}