package com.oscarsc.potholeavoider.services.tasks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import custom_incidences.LibCurve;
import custom_incidences.LibIncidence;
import custom_incidences.LibPothole;
import custom_incidences.LibSlope;
import custom_parsers.IncidenceJsonParser;
import android.location.Location;

import com.oscarsc.potholeavoider.incidences.Curve;
import com.oscarsc.potholeavoider.incidences.Incidence;
import com.oscarsc.potholeavoider.incidences.Pothole;
import com.oscarsc.potholeavoider.incidences.Slope;
import com.oscarsc.potholeavoider.listeners.GpsListener;
import com.oscarsc.potholeavoider.services.IncidenceCommunicator;

@SuppressWarnings("unused")
public class WebIncidences extends Observable{
	private TimerTask incidenceUpdaterTask;
	private ArrayList<Incidence> nextIncidences;
	public void updateIncidencesTask(int codeIncidences) {
		incidenceUpdaterTask = new ParameterTimerTask(codeIncidences);
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(incidenceUpdaterTask, 0,
				IncidenceCommunicator.NEXTINCIDENCESUPDATETIME * 1000);
	}
	private void refreshIncidences(int inc) {
        nextIncidences=new ArrayList<Incidence>();
		try {
			GpsListener.waitLocationNotNull();
			Location currentLocation=GpsListener.getLastLocation();
			double lat=currentLocation.getLatitude();
			double lon=currentLocation.getLongitude();
			nextIncidences.addAll(nearIncidences(inc,lat, lon, IncidenceCommunicator.LOADNEXTINCIDENCESRATIOKILOMETERS));
			for(Incidence i:nextIncidences){
				System.out.println(i);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private ArrayList<Incidence> nearIncidences(int inc,double lat,double lon,float dis) throws ClientProtocolException, IOException{
		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response;
		String responseString = null;
		String url=WebServerConnection.getIncidencesUrl();
		List<BasicNameValuePair> params = new LinkedList<BasicNameValuePair>();
        params.add(new BasicNameValuePair("inc", String.valueOf(inc)));
		params.add(new BasicNameValuePair("lat", String.valueOf(lat)));
		params.add(new BasicNameValuePair("lon", String.valueOf(lon)));
		params.add(new BasicNameValuePair("dis", String.valueOf(dis)));
		String paramString = URLEncodedUtils.format(params, "utf-8");
		url+=paramString;
		response = httpclient.execute(new HttpGet(url));
		StatusLine statusLine = response.getStatusLine();
		if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			response.getEntity().writeTo(out);
			out.close();
			responseString = out.toString();
		} else {
			// Closes the connection.
			response.getEntity().getContent().close();
			throw new IOException(statusLine.getReasonPhrase());
		}
		IncidenceJsonParser parser=new IncidenceJsonParser();
		ArrayList<ArrayList<LibIncidence>> arrayIncidences = parser.deserializeIncidenceArrayList(responseString);
		ArrayList<LibIncidence> potholes=arrayIncidences.get(0);
		ArrayList<LibIncidence> curves=arrayIncidences.get(1);
		ArrayList<LibIncidence> slopes=arrayIncidences.get(2);
		ArrayList<Incidence> incidences=new ArrayList<Incidence>();
		for(LibIncidence pothole:potholes){
			Pothole loadedPothole=new Pothole((LibPothole) pothole);
			incidences.add(loadedPothole);
		}
		for(LibIncidence curve:curves){
			Curve loadedCurve=new Curve((LibCurve) curve);
			incidences.add(loadedCurve);
		}
		for(LibIncidence slope:slopes){
			Slope loadedSlope=new Slope((LibSlope) slope);
			incidences.add(loadedSlope);
		}
		return incidences;
	}
	/*Loads simulated incidences (needs simulated GPS route) to test the application works
	  and see how it works*///Mondragon
	private synchronized ArrayList<Incidence> loadNextIncidences() {
		ArrayList<Incidence> incidences = new ArrayList<Incidence>();
		Location location=GpsListener.createLocation(43.061088, -2.51300);
		Incidence incidence=new Slope(1,location, -1, -1);
		incidences.add(incidence);
		location = GpsListener.createLocation(43.069182, -2.480096);
		incidence = new Pothole(1,location, 0);
		incidences.add(incidence);
		location = GpsListener.createLocation(43.078770, -2.461327);
		incidence = new Curve(1,location, 0, true);
		incidences.add(incidence);
		location = GpsListener.createLocation(43.069800, -2.477327);
		incidence = new Curve(2,location, 0, false);
		incidences.add(incidence);
		return incidences;
	}
	//Elgoibar, Durango
	/*private synchronized ArrayList<Incidence> chargeNextIncidences() {
		ArrayList<Incidence> incidences = new ArrayList<Incidence>();
		Location location=GpsListener.createLocation(43.180279, -2.471126);//Eibar
		Incidence incidence=new Slope(location, -1, location, -1);
		incidences.add(incidence);
		location = GpsListener.createLocation(43.180154, -2.472885);//Eibar2
		incidence = new Pothole(location, 0);
		incidences.add(incidence);
		location = GpsListener.createLocation(43.178975, -2.632183);//Durango
		incidence = new Curve(location, 0, true);
		incidences.add(incidence);
		location = GpsListener.createLocation(43.185265, -2.661087);//Durango2
		incidence = new Curve(location, 0, false);
		incidences.add(incidence);
		return incidences;
	}*/


    private class ParameterTimerTask extends TimerTask {

        private final int serial;
        ParameterTimerTask (int serial){
            this.serial = serial;
        }
        public void run() {
            refreshIncidences(serial);//Load incidences from web service and database
            //nextIncidences = loadNextIncidences();//Uses simulated data (there's no connection with the server)
            setChanged();//Sets the internal flag that indicates this observable has changed state
            WebIncidences.this.notifyObservers(nextIncidences);
        }
    }
}
