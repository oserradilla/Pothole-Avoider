package com.oscarsc.potholeavoider.services.tasks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;

import com.oscarsc.potholeavoider.Weather;
import com.oscarsc.potholeavoider.listeners.GpsListener;

public class WebWeather extends Observable{
	private int WEATHERUPDATETIME=10;
	TimerTask weatherTask;
	Weather weather=null;
	public WebWeather(){
		createWeatherTask();
	}
	private void createWeatherTask() {
		weatherTask = new TimerTask() {
			@Override
			public void run() {
				refreshWeather();
				setChanged();//Sets the internal flag that indicates this observable has changed state
				WebWeather.this.notifyObservers(weather);
			}
		};
		Timer timer = new Timer(true);
		timer.scheduleAtFixedRate(weatherTask, 0, WEATHERUPDATETIME * 1000*60);
	}
	private void refreshWeather() {
		try {
			GpsListener.waitLocationNotNull();
			Location currentLocation=GpsListener.getLastLocation();
			double lat=currentLocation.getLatitude();
			double lon=currentLocation.getLongitude();
			loadWeather(lat,lon);
			System.out.println(weather);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void loadWeather(double lat, double lon) {
		String responseString = null;
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response;
			String url=WebServerConnection.getWeatherUrl();
			List<BasicNameValuePair> params = new LinkedList<BasicNameValuePair>();
			params.add(new BasicNameValuePair("lat", String.valueOf(lat)));
			params.add(new BasicNameValuePair("lon", String.valueOf(lon)));
			params.add(new BasicNameValuePair("lang", "es"));
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

			
		} catch (Exception e) {
			//If there is no connection with the server
			responseString=null;
		}
		System.out.println(responseString);
		// We create out JSONObject from the data
		JSONObject jObj;
		try {
			jObj = new JSONObject(responseString);
			weather=new Weather(jObj);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
