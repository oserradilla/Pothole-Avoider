package com.oscarsc.potholeavoider.artificial_intelligence;

import android.util.Log;

import com.oscarsc.potholeavoider.incidences.Pothole;
import com.oscarsc.potholeavoider.listeners.GpsListener;
import com.oscarsc.potholeavoider.notifications.MyTextToSpeech;
import com.oscarsc.potholeavoider.services.IncidenceReporter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by oscar on 02/03/2016.
 */
public class AIPotholes extends Thread {

    private DataToBeAnalysedByAI dataToBeAnalysedByAI;

    private CurrentIncidenceState currentIncidenceState;
    private IncidenceReporter incidenceReporter;

    public AIPotholes(DataToBeAnalysedByAI dataToBeAnalysedByAI,
                      CurrentIncidenceState currentIncidenceState,
                      IncidenceReporter incidenceReporter) {
        this.dataToBeAnalysedByAI = dataToBeAnalysedByAI;
        this.currentIncidenceState = currentIncidenceState;
        this.incidenceReporter = incidenceReporter;
    }

    @Override
    public void run() {
            if(passesEnergyThreshold()) {
                Timer timer = new Timer();
                timer.schedule(new AskIfIncidenceHasBeenDetected(currentIncidenceState), 6750);
            }
    }

    private class AskIfIncidenceHasBeenDetected extends TimerTask {

        private IncidenceDetectedListener incidenceDetectedListener;

        public AskIfIncidenceHasBeenDetected(IncidenceDetectedListener incidenceDetectedListener) {
            this.incidenceDetectedListener = incidenceDetectedListener;
        }

        @Override
        public void run() {
            if(!incidenceDetectedListener.hasCurveBeenDetected()) {
                MyTextToSpeech tts = MyTextToSpeech.getInstance();
                tts.speakText("Bache");
                // TODO calculate magnitude
                int magnitude = -1;
                //IncidenceId not used. ID assigned in server to be stored so any value is valid here
                Pothole detectedPothole = new Pothole(-1, GpsListener.getLastLocation(), magnitude);
                ArrayList<Pothole> detectedPotholes = new ArrayList<Pothole>();
                detectedPotholes.add(detectedPothole);
                incidenceReporter.reportPotholesToServer(detectedPotholes);
            }
        }
    }

    private boolean passesEnergyThreshold() {
        boolean passesEnergyThreshold = false;
        float[][][] firstWindowPieceAccelGyroMagnetoRealWorldWindows =
                dataToBeAnalysedByAI.getFirstWindowPieceAccelGyroMagnetoRealWorldWindows();
        float[][][] secondWindowPieceAccelGyroMagnetoRealWorldWindows =
                dataToBeAnalysedByAI.getSecondWindowPieceAccelGyroMagnetoRealWorldWindows();
        EnergyThreshold energyThreshold = new EnergyThreshold(
                firstWindowPieceAccelGyroMagnetoRealWorldWindows, secondWindowPieceAccelGyroMagnetoRealWorldWindows);
        float energyDeviation = energyThreshold.calculateEnergyDeviation();
        if (energyDeviation > 30) {
            passesEnergyThreshold = true;
        }
        return passesEnergyThreshold;
    }

    private class EnergyThreshold{

        private float[][][] firstWindowPieceAccelGyroMagnetoRealWorldWindows = null;
        private float[][][] secondWindowPieceAccelGyroMagnetoRealWorldWindows = null;

        public EnergyThreshold (float[][][] firstWindowPieceAccelGyroMagnetoRealWorldWindows ,
                                float[][][] secondWindowPieceAccelGyroMagnetoRealWorldWindows) {
            this.firstWindowPieceAccelGyroMagnetoRealWorldWindows = firstWindowPieceAccelGyroMagnetoRealWorldWindows;
            this.secondWindowPieceAccelGyroMagnetoRealWorldWindows = secondWindowPieceAccelGyroMagnetoRealWorldWindows;
        }

        public float calculateEnergyDeviation (){
            float[] completeAccelWindow = getCompleteWindow(firstWindowPieceAccelGyroMagnetoRealWorldWindows[0],
                    secondWindowPieceAccelGyroMagnetoRealWorldWindows[0]);
            ContinuousFilter filter = new ContinuousFilter(5, 200, ContinuousFilter.PassType.Highpass, 1);
            float[] filteredCompleteAccelWindow = new float[completeAccelWindow.length-50];
            for (int i = 0; i < completeAccelWindow.length; i++) {
                filter.Update(completeAccelWindow[i]);
                if (i > 49) {
                    filteredCompleteAccelWindow[i-50] = filter.getValue();
                }
            }
            float[] squareOfTotalAccelZRealWorld = getSquareOfTotalAccelZRealWorld(filteredCompleteAccelWindow);
            float mean = mean(squareOfTotalAccelZRealWorld);
            float max = max(squareOfTotalAccelZRealWorld);
            Log.v("MEAN,MAX", String.valueOf(mean) + ";" + String.valueOf(max));
            float energyDeviation = max/mean;
            return energyDeviation;
        }

        private float[] getCompleteWindow(float[][] firstAccelRealWorld, float[][] secondAccelRealWorld) {
            float[] completeWindow = new float[firstAccelRealWorld.length+secondAccelRealWorld.length];
            for (int i=0; i<firstAccelRealWorld.length; i++) {
                completeWindow[i] = firstAccelRealWorld[i][2];
            }
            for (int i=0; i<secondAccelRealWorld.length; i++) {
                completeWindow[i+firstAccelRealWorld.length] = secondAccelRealWorld[i][2];
            }
            return completeWindow;
        }

        private float[] getSquareOfTotalAccelZRealWorld(float[] completeAccelWindow) {
            float[] squareOfTotalAccelZRealWorld = new float[completeAccelWindow.length];
            for (int i=0; i<completeAccelWindow.length; i++) {
                squareOfTotalAccelZRealWorld[i] = (float) Math.pow(completeAccelWindow[i], 2);
            }
            return squareOfTotalAccelZRealWorld;
        }

        private float mean(float[] floatArray) {
            float accomulation = 0;
            for(int i=0; i<floatArray.length; i++) {
                accomulation += floatArray[i];
            }
            return accomulation/floatArray.length;
        }

        private float max(float[] floatArray) {
            float max = Float.MIN_VALUE;
            for(int i=0; i<floatArray.length; i++) {
                if (max < floatArray[i]) {
                    max = floatArray[i];
                }
            }
            return max;
        }
    }
}
