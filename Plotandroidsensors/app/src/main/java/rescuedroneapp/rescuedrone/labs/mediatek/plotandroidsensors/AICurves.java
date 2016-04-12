package rescuedroneapp.rescuedrone.labs.mediatek.plotandroidsensors;

import notifications.MyTextToSpeech;

/**
 * Created by oscar on 01/04/2016.
 */
public class AICurves extends Thread {

    private final float GYRO_Z_MEAN_THRESHOLD = 0.2f;
    private float[][][] realWorldCalculusWindow;

    public AICurves(float[][][] realWorldCalculusWindow) {
        this.realWorldCalculusWindow = realWorldCalculusWindow;
    }

    @Override
    public void run() {
        int curveClass = passesGyroZMeanThreshold();
        if(curveClass != 0) {
            String curveClassString = curveClass > 0? "derecha" : "izquierda";
            MyTextToSpeech tts = MyTextToSpeech.getInstance();
            tts.speakText("Curva " + curveClassString);
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
