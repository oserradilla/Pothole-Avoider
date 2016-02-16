package com.oscarsc.potholeavoider.listeners;

import java.util.ArrayList;
import java.util.Observable;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/*There is only one class (this) for listening to GPS locations. This way, we can simulate that we are changing our
 * position only changing this class and notifying the others with Observer/Observable notifications*/
public class GpsListener extends Observable implements
		GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

	private String TAG = this.getClass().getSimpleName();
	private final int GPS_REFRESH_TIME =500;//5*1000 milliseconds = 5 seconds

	private GoogleApiClient googleApiClient =null;
	private LocationRequest locationrequest=null;

	static Object waitNotNullQueue=new Object();
	private Activity activity;
    private static GpsBuffer gpsBuffer=new GpsBuffer();
	public GpsListener(Activity activity) {
		this.activity=activity;
		int resp = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(activity);
		if (resp == ConnectionResult.SUCCESS) {
			turnGPSOn();//GPS is available
            googleApiClient = new GoogleApiClient.Builder(activity)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            googleApiClient.connect();
		} else {
			Toast.makeText(activity, "Google Play Service Error " + resp,
					Toast.LENGTH_LONG).show();
		}
	}
	/*
	 * public void stop() { googleApiClient.removeLocationUpdates(this);
	 * googleApiClient.removeLocationUpdates(mPendingIntent); }
	 */
	
	public void onDestroy() {
		if (googleApiClient != null)
			googleApiClient.disconnect();
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		if (googleApiClient != null && googleApiClient.isConnected()) {
			locationrequest = LocationRequest.create();
			locationrequest.setInterval(GPS_REFRESH_TIME);
			locationrequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,locationrequest, this);
		}
	}

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "onConnectionSuspended");
    }

    @Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.i(TAG, "onConnectionFailed");
	}

// Request code to use when launching the resolution activity
private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;

    public void onConnectionFailed1(ConnectionResult result) {
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else{
            if (result.hasResolution()) {
                try {
                    mResolvingError = true;
                    result.startResolutionForResult(activity, REQUEST_RESOLVE_ERROR);
                } catch (Exception e) {
                    // There was an error with the resolution intent. Try again.
                    googleApiClient.connect();
                }
            } else {
                // Show dialog using GooglePlayServicesUtil.getErrorDialog()
                //showErrorDialog(result.getErrorCode());
                mResolvingError = true;
            }
        }
    }


    @Override
	public void onLocationChanged(Location location) {
		if (location != null) {
			synchronized(waitNotNullQueue){
				waitNotNullQueue.notifyAll();
			}
			//Location currentLocation = location;//Takes current location as the real GPS position
            Location currentLocation = loadNextSimulatedLocation();//Simulates a route ignoring the one taken from the GPS
            gpsBuffer.putLocation(currentLocation);
            Log.i(TAG, "Location changed :" + currentLocation.getLatitude() + ","
					+ currentLocation.getLongitude()+" accuracy: "+currentLocation.getAccuracy());
			setChanged();
			this.notifyObservers(currentLocation);
		}
	}

	public static Location getLastLocation() {
		return gpsBuffer.getLocation(1);
	}

    public Location getLocation(int position){
        return gpsBuffer.getLocation(position);
    }
    public Location getFirstLocation(){
        return gpsBuffer.getFirstLocation();
    }
	/*Distance-unit functions*/
	//Unit: 'M' is statute miles   'K' is kilometers (default)  N' is nautical miles



	public static double distance(Location l1, Location l2, char unit) {
		double lat1=l1 .getLatitude();
		double lon1=l1.getLongitude();
		double lat2=l2.getLatitude();
		double lon2=l2.getLongitude();
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2))
				+ Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
				* Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
        switch(unit){
            case 'K': dist = dist * 1.609344;
                      break;
            case 'm': dist = dist * 1609.344;
                      break;
            case 'N': dist = dist * 0.8684;
                      break;
            default: break;
        }
		return (dist);
	}

	private static double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	private static double rad2deg(double rad) {
		return (rad * 180 / Math.PI);
	}
	//Location functions
	public static String locationToString(Location location){
		return "\nLatitude: "+location.getLatitude() + "\nLongitude: "+location.getLongitude();
	}
	public static String locationToString(double latitude, double longitude){
		return "\nLatitude: "+latitude + "\nLongitude: "+longitude;
	}
	public static void waitLocationNotNull() throws InterruptedException{
		synchronized(waitNotNullQueue){
			while(gpsBuffer.getRealSize()==0)
				waitNotNullQueue.wait();
		}
	}
	//To create a location from latitude and longitude parameters
	public static Location createLocation(double latitude, double longitude) {
		Location location;
		location = new Location("");
		location.setLatitude(latitude);
		location.setLongitude(longitude);
		return location;
	}
	//To notify the user that the GPS is off and the application won't work properly
	public void turnGPSOn()
	{
		final LocationManager manager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

		    if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
		        buildAlertMessageNoGps();
		    }

	} 
	private void buildAlertMessageNoGps() {
	    final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
	    builder.setMessage("This app won't work well with the gps disabled, do you want to enable it?")
	           .setCancelable(false)
	           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	               public void onClick(final DialogInterface dialog, final int id) {
	                   activity.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
	               }
	           })
	           .setNegativeButton("No", new DialogInterface.OnClickListener() {
	               public void onClick(final DialogInterface dialog, final int id) {
	                    dialog.cancel();
	               }
	           });
	    final AlertDialog alert = builder.create();
	    alert.show();
	}
	/*To simulate that we are moving through the map. Every time the gps position is updated twice,
	  the simulated current position will change to the next one in the list */
	//Variables
	int numLoc = 0;
	int countLoc = 0;
	//Functions
	private Location loadNextSimulatedLocation() {
        ArrayList<Location> locations = new ArrayList<Location>();
        locations.add(GpsListener.createLocation(43.064977, -2.494189));
        locations.add(GpsListener.createLocation(43.066709, -2.490627));
        locations.add(GpsListener.createLocation(43.067712, -2.486432));
        locations.add(GpsListener.createLocation(43.069899, -2.478701));
        locations.add(GpsListener.createLocation(43.072913, -2.473262));
        locations.add(GpsListener.createLocation(43.073735, -2.469046));
        locations.add(GpsListener.createLocation(43.075393, -2.464078));
        locations.add(GpsListener.createLocation(43.077053, -2.457577));
        locations.add(GpsListener.createLocation(43.080517, -2.455898));
        locations.add(GpsListener.createLocation(43.082245, -2.452191));
        locations.add(GpsListener.createLocation(43.082940, -2.445819));
        locations.add(GpsListener.createLocation(43.083160, -2.440158));
        locations.add(GpsListener.createLocation(43.063278, -2.506779));
        locations.add(GpsListener.createLocation(43.062676, -2.504692));
        locations.add(GpsListener.createLocation(43.062088, -2.503920));
        locations.add(GpsListener.createLocation(43.061986, -2.501559));
        numLoc = (++numLoc == locations.size()) ? 0 : numLoc;
        return locations.get(numLoc);
    }

    public Location calculatePrevLocation() {
        return gpsBuffer.calculatePrevLocation();
    }

    public boolean areVectorsInRange(Location firstLocation, Location currentLocation, Location prevLocation, Location location, double degreesAngleRange) {
        return gpsBuffer.areVectorsInRange( firstLocation, currentLocation, prevLocation, location, degreesAngleRange);
    }

    public boolean isOnEllipse(Location prevLocation, Location currentLocation, Location incidenceLocation){
        return gpsBuffer.isOnEllipse(prevLocation,currentLocation,incidenceLocation);
    }
}