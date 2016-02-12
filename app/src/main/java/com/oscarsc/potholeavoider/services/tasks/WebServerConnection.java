package com.oscarsc.potholeavoider.services.tasks;

public class WebServerConnection {
	//Global
	public static String HTTP="http://";
	//Pothole avoider system variables
    //Raspberry's Settings
	public static String INCIDENCES_ADDRESS="www.potholeavoider.es";//Servers IP or Domain address
	public static String INCIDENCES_PORT="80";
	public static String INCIDENCES_GLOBAL_PATH="/pha_rpi/web-service";
    //My laptops settings
    /*public static String INCIDENCES_ADDRESS="192.168.0.16";
    public static String INCIDENCES_PORT="8080";
    public static String INCIDENCES_GLOBAL_PATH="/potholeAvoiderWebService/web-service";*/

    public static String READ_INCIDENCES_PATH="/near-incidences?";
    public static String REPORT_INCIDENCES_PATH="/report-incidences";
    public static String REPORT_POTHOLES_PATH="/report-potholes";
    public static String REPORT_CURVES_PATH="/report-curves";
    public static String REPORT_SLOPES_PATH="/report-slopes";
	//Weather variables
	public static String WEATHER_ADDRESS="api.openweathermap.org";//Servers IP or Domain address
	public static String WEATHER_PATH="/data/2.5/weather?";
	//Functions
	public static String getIncidencesUrl(){
		return HTTP+INCIDENCES_ADDRESS+":"+INCIDENCES_PORT+INCIDENCES_GLOBAL_PATH+READ_INCIDENCES_PATH;
	}
	public static String getWeatherUrl(){
		return HTTP+WEATHER_ADDRESS+WEATHER_PATH;
	}
    public static String getIncidencesReportPath(){
        return HTTP+INCIDENCES_ADDRESS+":"+INCIDENCES_PORT+INCIDENCES_GLOBAL_PATH+REPORT_INCIDENCES_PATH;
    }
    public static String getPotholesReportPath(){
        return HTTP+INCIDENCES_ADDRESS+":"+INCIDENCES_PORT+INCIDENCES_GLOBAL_PATH+REPORT_POTHOLES_PATH;
    }
    public static String getCurvesReportPath(){
        return HTTP+INCIDENCES_ADDRESS+":"+INCIDENCES_PORT+INCIDENCES_GLOBAL_PATH+REPORT_CURVES_PATH;
    }
    public static String getSlopesReportPath(){
        return HTTP+INCIDENCES_ADDRESS+":"+INCIDENCES_PORT+INCIDENCES_GLOBAL_PATH+REPORT_SLOPES_PATH;
    }

}
