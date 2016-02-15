package com.oscarsc.potholeavoider.services;

import com.oscarsc.potholeavoider.incidences.Curve;
import com.oscarsc.potholeavoider.incidences.Pothole;
import com.oscarsc.potholeavoider.incidences.Slope;
import com.oscarsc.potholeavoider.services.tasks.WebServerConnection;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

import common_resources.CommonConstants;
import custom_incidences.LibCurve;
import custom_incidences.LibPothole;
import custom_incidences.LibSlope;
import custom_parsers.IncidenceJsonParser;

/**
 * Created by oscar on 2/6/15.
 */
public class IncidenceReporter {

    public void reportIncidencesToServer(ArrayList<Pothole> potholes,
                                          ArrayList<Curve> curves, ArrayList<Slope> slopes) {
        HttpClient client = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); //Timeout Limit
        HttpResponse response;
        try {
            HttpPost post = new HttpPost(WebServerConnection.getIncidencesReportPath());
            ArrayList<LibPothole> travellingPotholes=new ArrayList<LibPothole>();
            for(Pothole pothole: potholes){
                travellingPotholes.add(pothole.getInternalPothole());
            }
            ArrayList<LibCurve> travellingCurves=new ArrayList<LibCurve>();
            for(Curve curve: curves){
                travellingCurves.add(curve.getInternalCurve());
            }
            ArrayList<LibSlope> travellingSlopes=new ArrayList<LibSlope>();
            for(Slope slope: slopes){
                travellingSlopes.add(slope.getInternalSlope());
            }

            IncidenceJsonParser jsonParser=new IncidenceJsonParser();
            String jsonString;
            jsonString=jsonParser.serializeIncidenceArrayList(travellingPotholes,travellingCurves,travellingSlopes);
            System.out.print(jsonString);

            StringEntity se = new StringEntity(jsonString);
            se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            post.setEntity(se);
            response = client.execute(post);
                    /*Checking response */
            if (response != null) {
                InputStream in = response.getEntity().getContent(); //Get the data in the entity
                Scanner scanner=new Scanner(in);
                String inserted=scanner.nextLine();
                if(inserted.equals(CommonConstants.OK_REPORTING))
                    System.out.println("Potholes reported correctly");
                else
                    System.out.println("Potholes not reported: Error");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reportPotholesToServer(ArrayList<Pothole> potholes) {
        HttpClient client = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); //Timeout Limit
        HttpResponse response;
        try {
            HttpPost post = new HttpPost(WebServerConnection.getPotholesReportPath());
            ArrayList<LibPothole> travellingPotholes=new ArrayList<LibPothole>();
            for(Pothole pothole: potholes){
                travellingPotholes.add(pothole.getInternalPothole());
            }

            IncidenceJsonParser jsonParser=new IncidenceJsonParser();
            String jsonString;
            jsonString=jsonParser.serializePotholeArrayList(travellingPotholes);
            System.out.print(jsonString);

            StringEntity se = new StringEntity(jsonString);
            se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            post.setEntity(se);
            response = client.execute(post);
                    /*Checking response */
            if (response != null) {
                InputStream in = response.getEntity().getContent(); //Get the data in the entity
                Scanner scanner=new Scanner(in);
                String inserted=scanner.nextLine();
                if(inserted.equals(CommonConstants.OK_REPORTING))
                    System.out.println("Potholes reported correctly");
                else
                    System.out.print("Potholes not reported: Error");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reportCurvesToServer(ArrayList<Curve> curves) {
        HttpClient client = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); //Timeout Limit
        HttpResponse response;
        try {
            HttpPost post = new HttpPost(WebServerConnection.getCurvesReportPath());
            ArrayList<LibCurve> travellingCurves=new ArrayList<LibCurve>();
            for(Curve curve: curves){
                travellingCurves.add(curve.getInternalCurve());
            }

            IncidenceJsonParser jsonParser=new IncidenceJsonParser();
            String jsonString;
            jsonString=jsonParser.serializeCurveArrayList(travellingCurves);
            System.out.print(jsonString);

            StringEntity se = new StringEntity(jsonString);
            se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            post.setEntity(se);
            response = client.execute(post);
                    /*Checking response */
            if (response != null) {
                InputStream in = response.getEntity().getContent(); //Get the data in the entity
                Scanner scanner=new Scanner(in);
                String inserted=scanner.nextLine();
                if(inserted.equals(CommonConstants.OK_REPORTING))
                    System.out.println("Curves reported correctly");
                else
                    System.out.print("Curves not reported: Error");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reportSlopesToServer(ArrayList<Slope> slopes) {
        HttpClient client = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); //Timeout Limit
        HttpResponse response;
        try {
            HttpPost post = new HttpPost(WebServerConnection.getSlopesReportPath());
            ArrayList<LibSlope> travellingSlopes=new ArrayList<LibSlope>();
            for(Slope slope: slopes){
                travellingSlopes.add(slope.getInternalSlope());
            }

            IncidenceJsonParser jsonParser=new IncidenceJsonParser();
            String jsonString;
            jsonString=jsonParser.serializeSlopeArrayList(travellingSlopes);
            System.out.print(jsonString);

            StringEntity se = new StringEntity(jsonString);
            se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            post.setEntity(se);
            response = client.execute(post);
                    /*Checking response */
            if (response != null) {
                InputStream in = response.getEntity().getContent(); //Get the data in the entity
                Scanner scanner=new Scanner(in);
                String inserted=scanner.nextLine();
                if(inserted.equals(CommonConstants.OK_REPORTING))
                    System.out.println("Slopes reported correctly");
                else
                    System.out.print("Slopes not reported: Error");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
