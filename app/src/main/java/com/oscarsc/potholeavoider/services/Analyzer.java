package com.oscarsc.potholeavoider.services;

import android.content.Context;
import android.util.Log;

import com.oscarsc.potholeavoider.incidences.Curve;
import com.oscarsc.potholeavoider.incidences.Incidence;
import com.oscarsc.potholeavoider.incidences.Pothole;
import com.oscarsc.potholeavoider.incidences.Slope;
import com.oscarsc.potholeavoider.listeners.GpsListener;
import com.oscarsc.potholeavoider.text_to_speech.MyTextToSpeech;

import org.apache.commons.lang3.StringUtils;
//import org.json.JSONObject;
//import changedpath.org.json.JSONObject;

import java.util.ArrayList;
import java.util.Scanner;

public class Analyzer extends Thread {
    private static String previouslyNotAnalyzedData = "";
    IncidenceReporter incidenceReporter;
    MyTextToSpeech tts;
    Context context;
    String incidenceData;
    GpsListener gpsListener;
    //TODO Funcion para comprobar datos de Arduino
    public Analyzer(Context context,MyTextToSpeech tts,String incidenceData, GpsListener gpsListener) {
        super("Analyzer service");
        this.tts=tts;
        this.context=context;
        this.incidenceData=incidenceData;
        this.gpsListener=gpsListener;
        incidenceReporter=new IncidenceReporter();
    }

    public void run() {
        MyTextToSpeech tts=MyTextToSpeech.getInstance();
        if (incidenceData != null) {
            ArrayList<Incidence> incidences = extractIncidences(incidenceData);
            for (Incidence incidence : incidences) {
                if (incidence != null) {
                    if (incidence instanceof Pothole)
                        analyzePothole((Pothole) incidence,tts);
                    else if (incidence instanceof Curve)
                        analyzeCurve((Curve) incidence);
                    else if (incidence instanceof Slope)
                        analyzeSlope((Slope) incidence);
                } else
                    Log.v("Analyzer", "null incidence, not analyzed");
            }
        }
    }

    private ArrayList<Incidence> extractIncidences(String incidenceData) {
        ArrayList<Incidence> incidences = new ArrayList<Incidence>();
        previouslyNotAnalyzedData += incidenceData;
        //Create scanner for reading each incidence
        Scanner incidencesScanner = new Scanner(previouslyNotAnalyzedData);
        incidencesScanner.useDelimiter(";");

        int numCompleted = StringUtils.countMatches(previouslyNotAnalyzedData, ";");
        for (int i = 0; i < numCompleted; i++) {
            //Get new incidence
            String incidenceString = incidencesScanner.next();
            //Create incidence scanner to parse string incidence to incidence object
            Scanner incidenceDataScanner = new Scanner(incidenceString);
            incidenceDataScanner.useDelimiter("_");
            Incidence incidence = parseIncidence(incidenceDataScanner);
            if (incidence != null)
                incidences.add(incidence);
        }
        if (incidencesScanner.hasNext())
            previouslyNotAnalyzedData = incidencesScanner.next();
        else
            previouslyNotAnalyzedData = "";

        return incidences;
    }

    //TODO añadir slope y curve (y modificar protocolo con arduino para que se añada isright y slope(porcentaje))
    private Incidence parseIncidence(Scanner scanner) {
        Incidence incidence = null;
        try {
            GpsListener.waitLocationNotNull();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (scanner.next().equals("i")) {
            int incidenceNumber = -1;//Because it is an incidence with no identifier.
            // This identifier will be given when saved in the database (it is assigned on server)
            String incidenceType = scanner.next();
            int magnitude = scanner.nextInt();
            //Pothole
            if (incidenceType.equals("p")) {
                incidence = new Pothole(incidenceNumber, GpsListener.getLastLocation(), magnitude);
            }
            if (incidenceType.equals("c")) {
                String bool = scanner.next();
                boolean isRight= bool.equals("t");
                incidence = new Curve(incidenceNumber, GpsListener.getLastLocation(), magnitude,isRight);
            }
            if (incidenceType.equals("s")) {
                int slopeValue=scanner.nextInt();
                incidence = new Slope(incidenceNumber, GpsListener.getLastLocation(), magnitude,slopeValue);
            }
        }
        return incidence;
    }

    private void analyzePothole(Pothole pothole,MyTextToSpeech tts) {
        System.out.println("bache detectado");
        tts.speakText("bache detectado");
        try {
            GpsListener.waitLocationNotNull();
            pothole.setLocation(GpsListener.getLastLocation());
            pothole.setPrevLocation(gpsListener.calculatePrevLocation());
            ArrayList<Pothole> potholes=new ArrayList<Pothole>();
            potholes.add(pothole);
            incidenceReporter.reportPotholesToServer(potholes);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void analyzeCurve(Curve curve) {
        ArrayList<Curve> curves=new ArrayList<Curve>();
        curves.add(curve);
        incidenceReporter.reportCurvesToServer(curves);
    }

    private void analyzeSlope(Slope slope) {
        ArrayList<Slope> slopes=new ArrayList<Slope>();
        slopes.add(slope);
        incidenceReporter.reportSlopesToServer(slopes);
    }


}
