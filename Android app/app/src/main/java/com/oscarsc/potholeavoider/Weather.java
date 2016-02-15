package com.oscarsc.potholeavoider;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class Weather implements Serializable{
	private static final long serialVersionUID = 1L;
	
	float temp;
	int humidity;
	int windSpeed;
	float windDirection;
	String main;
	String description;
	String icon;
    long sunrise;
    long sunset;
	public Weather(float temp, int humidity, int windSpeed, int windDirection,
			String main, String description, String icon, long sunrise, long sunset){
		this.temp=temp;
		this.humidity=humidity;
		this.windSpeed=windSpeed;
		this.windDirection=windDirection;
		this.main=main;
		this.description=description;
		this.icon=icon;
        this.sunrise=sunrise;
        this.sunset=sunset;
	}
	public Weather(JSONObject jObj) throws JSONException{
		JSONObject obj=jObj.getJSONArray("weather").getJSONObject(0);
		main=obj.getString("main");
		description=obj.getString("description");
		icon=obj.getString("icon");
		obj=jObj.getJSONObject("main");
		humidity=obj.getInt("humidity");
		temp=(float) obj.getDouble("temp");
		obj=jObj.getJSONObject("wind");
		windSpeed=(int) obj.getDouble("speed");
		windDirection=(float) obj.getDouble("deg");
        obj=jObj.getJSONObject("sys");
        sunrise=obj.getLong("sunrise");
        sunset=obj.getLong("sunset");
	}
	public int getHumidity() {
		return humidity;
	}
	public void setHumidity(int humidity) {
		this.humidity = humidity;
	}
	public int getWindSpeed() {
		return windSpeed;
	}
	public void setWindSpeed(int windSpeed) {
		this.windSpeed = windSpeed;
	}
	public float getWindDirection() {
		return windDirection;
	}
	public void setWindDirection(int windDirection) {
		this.windDirection = windDirection;
	}
	public float getTempKelvin(){
		return temp;
	}
	public float getTempCelsius(){
		return temp-272.15f;
	}
	@Override
	public String toString(){
		return "Current weather: "+main+
				"\ndescription: "+description+
				"\ntemperature: "+temp+
				"\nhumidity: "+humidity+
				"\nwind speed: "+windSpeed+
				"\nwind direction"+windDirection+
				"\nicon: "+icon+
                "\nsunrise: "+sunrise+
                "\nsunset: "+sunset;
	}
	public String getImageId() {
	    String uri = "drawable/"+"img_"+icon;
	    return uri;
	}
	public String getDescription(){
		return description;
	}
}
