package rescuedroneapp.rescuedrone.labs.mediatek.plotandroidsensors;

import android.util.Log;

import notifications.AppNotifications;
import notifications.MyTextToSpeech;

/**
 * Created by oscar on 02/03/2016.
 */
public class AIPotholes extends Thread {

    private DataToBeAnalysedByAI dataToBeAnalysedByAI;

    public AIPotholes(DataToBeAnalysedByAI dataToBeAnalysedByAI) {
        this.dataToBeAnalysedByAI = dataToBeAnalysedByAI;
    }

    @Override
    public void run() {
            if(passesEnergyThreshold()) {
                MyTextToSpeech tts = MyTextToSpeech.getInstance();
                tts.speakText("Bache");
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
        if (energyDeviation > 20) {
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
