package rescuedroneapp.rescuedrone.labs.mediatek.plotandroidsensors;

import android.util.Log;

/**
 * Created by oscar on 02/03/2016.
 */
public class AI{

    private int sampleFrequency;
    private int windowFrequency;
    private int represtativeChangeFrequency;

    public AI(int sampleFrequency, int windowFrequency, int represtativeChangeFrequency) {
        this.sampleFrequency = sampleFrequency;
        this.windowFrequency = windowFrequency;
        this.represtativeChangeFrequency = represtativeChangeFrequency;
    }

    //@Override
    public void rollingWindowHasRepresentativelyChanged(float[][][] snapshot3Windows, int newWindowPosition) {
        int startPosition = newWindowPosition-represtativeChangeFrequency;
        int endPosition = newWindowPosition -1;
        float diffSVMAccelerometer = diffSVM(snapshot3Windows[0], startPosition, endPosition);
        float diffSVMGyroscope = diffSVM(snapshot3Windows[1], startPosition, endPosition);
        //if ()
    }

    private float diffSVM (float[][] window, int startPosition, int endPosition) {
        float maxSVMValue = -1;
        float minSVMValue = Float.MAX_VALUE;
        float currentSVMValue;
        for (int i = startPosition; i <= endPosition; i++) {
            currentSVMValue = (float) Math.sqrt(Math.pow(window[i][0],2) +
                    Math.pow(window[i][1],2) + Math.pow(window[i][2],2));
            if (currentSVMValue > maxSVMValue)
                maxSVMValue = currentSVMValue;
            if (currentSVMValue < minSVMValue)
                minSVMValue = currentSVMValue;
        }
        float diffMaxMinSVM = maxSVMValue - minSVMValue < 0 ? 0 : maxSVMValue - minSVMValue;
        return diffMaxMinSVM;
    }
}
