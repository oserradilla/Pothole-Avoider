package com.oscarsc.potholeavoider.gps;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.widget.Toast;

/**
 * Created by oscar on 30/03/2016.
 */
public class PreciseGPSListener implements GPSCallback{

    private Context context;

    private GPSManager gpsManager = null;
    private SpeedLastValue speedLastValue;
    Boolean isGPSEnabled=false;
    LocationManager locationManager;
    private ProgressDialog dialog;
    private boolean gpsInitialized = false;

    public PreciseGPSListener(Context context, SpeedLastValue speedLastValue) {
        this.context = context;
        this.speedLastValue = speedLastValue;
        locationManager = (LocationManager)context.getSystemService(context.LOCATION_SERVICE);
        gpsManager = new GPSManager(context);
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(isGPSEnabled)
        {
            gpsManager.startListening(context);
            gpsManager.setGPSCallback(this);
        }
        else
        {
            gpsManager.showSettingsAlert();
        }
        dialog = ProgressDialog.show(context, "",
                "Looking for GPS. Please wait...", true);
    }

    Toast toast = null;

    @Override
    public void onGPSUpdate(Location location)
    {
        float speed = location.getSpeed();
        int lastSpeedKmph = Math.round(speed * 3.6f);
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(context, String.valueOf(lastSpeedKmph), Toast.LENGTH_SHORT);
        toast.show();
        speedLastValue.setLastSpeed(lastSpeedKmph);
        if (!gpsInitialized) {
            dialog.dismiss();
            gpsInitialized = true;
        }
    }

    public void stopGPS() {
        gpsManager.stopListening();
        gpsManager.setGPSCallback(null);
        gpsManager = null;
    }
}
