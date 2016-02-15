package com.oscarsc.potholeavoider.services;

import java.util.Observable;
import java.util.Observer;

import android.os.Bundle;
import android.os.Message;

import com.oscarsc.potholeavoider.MainActivityHandler;
import com.oscarsc.potholeavoider.Weather;
import com.oscarsc.potholeavoider.services.tasks.WebWeather;

public class WeatherCommunicator implements Observer{
	MainActivityHandler mainHandler;
	Weather weather;
	public WeatherCommunicator(MainActivityHandler mainHandler){
		this.mainHandler=mainHandler;
		WebWeather webWeather=new WebWeather();
		webWeather.addObserver(this);
	}
	private void updateScreen() {
		Bundle bundle = new Bundle();
		bundle.putSerializable("weather", weather);
		Message message = Message.obtain(mainHandler);// To recycle message objects
		message.setData(bundle);
		mainHandler.sendMessage(message);
	}
	@Override
	public void update(Observable observable, Object data) {
		if(data!=null){
			if(data instanceof Weather){
				weather=(Weather) data;
				updateScreen();
			}
		}
	}

}
