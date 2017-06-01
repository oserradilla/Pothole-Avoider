package com.oscarsc.potholeavoider.artificial_intelligence;


import com.oscarsc.potholeavoider.incidences.Curve;
import com.oscarsc.potholeavoider.listeners.GpsListener;
import com.oscarsc.potholeavoider.notifications.MyTextToSpeech;
import com.oscarsc.potholeavoider.services.IncidenceReporter;

import java.util.ArrayList;

/**
 * Created by oscar on 01/04/2016.
 */
public class AICurves extends Thread {

    private final float GYRO_Z_MEAN_THRESHOLD = 0.2f;
    private float[][][] realWorldCalculusWindow;

    private CurrentIncidenceState currentIncidenceState;
    private IncidenceReporter incidenceReporter;

    public AICurves(float[][][] realWorldCalculusWindow, CurrentIncidenceState currentIncidenceState,
                    IncidenceReporter incidenceReporter) {
        this.realWorldCalculusWindow = realWorldCalculusWindow;
        this.currentIncidenceState = currentIncidenceState;
        this.incidenceReporter = incidenceReporter;
    }

    @Override
    public void run() {
        int curveClass = passesGyroZMeanThreshold();
        if(curveClass != 0) {
            currentIncidenceState.curveDetected(true);
            boolean isRight= curveClass > 0;
            String curveClassString = isRight? "derecha" : "izquierda";
            MyTextToSpeech tts = MyTextToSpeech.getInstance();
            tts.speakText("Curva " + curveClassString);
            // TODO calculate magnitude
            int magnitude = -1;
            //IncidenceId not used. ID assigned in server to be stored so any value is valid here
            Curve detectedCurve = new Curve(-1, GpsListener.getLastLocation(), magnitude,isRight);
            ArrayList<Curve> detectedCurves = new ArrayList<Curve>();
            detectedCurves.add(detectedCurve);
            incidenceReporter.reportCurvesToServer(detectedCurves);
        } else {
            currentIncidenceState.curveDetected(false);
        }
    }

    // Return 0 if no curves. Megative if curveLeft and positive y curveRight
    private int passesGyroZMeanThreshold() {
        int curveClass = 0;
        float sumGyroZ = 0.0f;
        for(int i=0; i<realWorldCalculusWindow.length; i++) {
            sumGyroZ += realWorldCalculusWindow[i][0][5];
        }
        float meanGyroZ = sumGyroZ/realWorldCalculusWindow.length;
        boolean passesGyroZMeanThreshold = meanGyroZ > GYRO_Z_MEAN_THRESHOLD ||
                meanGyroZ < -GYRO_Z_MEAN_THRESHOLD;
        if (passesGyroZMeanThreshold)
            curveClass = (int) (-meanGyroZ*10);
        return curveClass;
    }
}
