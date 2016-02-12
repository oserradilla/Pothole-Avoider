package com.oscarsc.potholeavoider.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ListIterator;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.TimerTask;

import android.app.Activity;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import com.oscarsc.potholeavoider.MainActivityHandler;
import com.oscarsc.potholeavoider.incidences.Incidence;
import com.oscarsc.potholeavoider.incidences.IncidenceDistance;
import com.oscarsc.potholeavoider.listeners.GpsListener;
import com.oscarsc.potholeavoider.services.tasks.WebIncidences;
import com.oscarsc.potholeavoider.text_to_speech.MyTextToSpeech;

public class IncidenceCommunicator implements Observer {
    private MyTextToSpeech tts;
    private MainActivityHandler mainHandler;
    private ArrayList<Incidence> nextIncidences = new ArrayList<Incidence>();
    private TimerTask incidenceUpdaterTask;
    public final static int NEXTINCIDENCESUPDATETIME = 10;// 90seconds = 1.5minutes
    public final static float LOADNEXTINCIDENCESRATIOKILOMETERS = 1000.0f;// 10Km
    public final static double PROXIMITYWARNINGDISTANCEKILOMETERS = 1.0;// 1Km
    public final static double ANGLE_DEGREES_VECTORS_ROAD_DIRECTION = 20;

    private static boolean potholesEnabled = true;
    private static boolean curvesEnabled = true;
    private static boolean slopesEnabled = true;
    private static boolean pavementEnabled = true;

    WebIncidences webIncidences;

    ArrayList<IncidenceDistance> notifiedIncidences = new ArrayList<IncidenceDistance>();

    GpsListener gpsListener;

    public IncidenceCommunicator(Activity activity, MainActivityHandler mainHandler,
                                 MyTextToSpeech myTts, GpsListener gpsListener) {
        tts = myTts;
        this.gpsListener = gpsListener;
        this.mainHandler = mainHandler;
        webIncidences = new WebIncidences();
        webIncidences.addObserver(this);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        Set<String> stringSet = preferences.getStringSet("incidence_key", null);
        updateSelectedIncidences(stringSet);
    }

    private synchronized ArrayList<IncidenceDistance> analyzeNearIncidences(
            Location currentLocation) {
        ArrayList<IncidenceDistance> nearIncidences = new ArrayList<IncidenceDistance>();
        ListIterator<Incidence> listIterator = nextIncidences.listIterator();
        while(listIterator.hasNext()) {
            Incidence incidence = listIterator.next();
            if (incidence != null) {
                double distance = GpsListener.distance(currentLocation,
                        incidence.getLocation(), 'K');
                if (distance < PROXIMITYWARNINGDISTANCEKILOMETERS) {
                    if (!incidence.isVisited()) {
                        double roundedDistance = roundDistance(distance);
                        Location firstLocation = gpsListener.getFirstLocation();
                        Location prevIncidenceLocation = incidence.getPrevLocation();
                        Location incidenceLocation = incidence.getLocation();
                        boolean areVectorsInRange = gpsListener.areVectorsInRange(firstLocation, currentLocation,
                                prevIncidenceLocation, incidenceLocation, ANGLE_DEGREES_VECTORS_ROAD_DIRECTION);
                        boolean isVisitedInLastLocationChange = gpsListener.isOnEllipse(gpsListener.getLocation(2),
                                currentLocation, incidenceLocation);
                        if(!isVisitedInLastLocationChange) {
                            if (areVectorsInRange)
                                nearIncidences.add(new IncidenceDistance(incidence, roundedDistance, 0));
                        }else{
                            incidence.setVisited(true);
                            listIterator.set(incidence);
                        }
                    }
                } else{
                    incidence.setVisited(false);
                    listIterator.set(incidence);
                }
            }
        }
        Collections.sort(nearIncidences);
        IncidenceDistance id;
        for (int i = 1; i <= 2; i++) {
            if (i > nearIncidences.size()) {
                id = new IncidenceDistance(null, 0, -i);
                nearIncidences.add(i - 1, id);
            } else {
                id = nearIncidences.get(i - 1);
                id.setPosition(i);
                nearIncidences.set(i - 1, id);
            }
        }
        return nearIncidences;
    }

    private double roundDistance(double distance) {
        int intDistance = (int) (distance * 1000);
        int toRound = intDistance % 100;
        intDistance -= toRound;
        if (toRound >= 25) {
            intDistance += (toRound < 75) ? 50 : 100;
        }
        distance = (double) intDistance / 1000;
        return distance;
    }

    private void showNextIncidences(ArrayList<IncidenceDistance> nextIncidences) {
        boolean isFirst = true;
        for (IncidenceDistance id : nextIncidences) {
            if (isFirst) {
                if (id.getIncidence() != null) {
                    Log.v("Next Incidence", id.toString());
                    if (saveIncidence(id))
                        tts.speakText(id.speakIncidence());
                    isFirst = false;
                } else
                    removeSavedIncidences();
            }
            updateScreen(id);
        }
    }

    private void updateScreen(IncidenceDistance id) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("incidenceDistance", id);
        Message message = Message.obtain(mainHandler);// To recycle message objects
        message.setData(bundle);
        mainHandler.sendMessage(message);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void update(Observable observable, Object data) {
        if (data != null) {
            if (data instanceof Location) {
                Location location = (Location) data;
                ArrayList<IncidenceDistance> nearIncidences = analyzeNearIncidences(location);
                showNextIncidences(nearIncidences);
            }
            if (data instanceof ArrayList<?>) {
                nextIncidences = (ArrayList<Incidence>) data;
            }
            if (data instanceof Set<?>) {
                updateSelectedIncidences((Set<String>) data);
            }
        }
    }

    private void updateSelectedIncidences(Set<String> stringSet) {
        if (stringSet != null) {
            potholesEnabled = stringSet.contains("pothole");
            curvesEnabled = stringSet.contains("curve");
            slopesEnabled = stringSet.contains("slope");
            pavementEnabled = stringSet.contains("pavement");
        }
        webIncidences.updateIncidencesTask(getIncidenceCode());
    }

    private int getIncidenceCode() {
        int code = 0;
        ArrayList<Integer> integerIncidences = new ArrayList<Integer>();
        integerIncidences.add((potholesEnabled) ? 1 : 0);
        integerIncidences.add((curvesEnabled) ? 1 : 0);
        integerIncidences.add((slopesEnabled) ? 1 : 0);
        int bitNumber = 1;
        for (Integer integerIncidence : integerIncidences) {
            code += bitNumber * integerIncidence;
            bitNumber = bitNumber * 2;
        }
        return code;
    }

    public void removeSavedIncidences() {
        notifiedIncidences.clear();
    }

    public boolean saveIncidence(IncidenceDistance id) {
        if (!notifiedIncidences.contains(id)) {
            notifiedIncidences.add(id);
            return true;
        }
        return false;
    }
}
