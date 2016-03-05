package rescuedroneapp.rescuedrone.labs.mediatek.plotandroidsensors;

import android.util.Log;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by oscar on 02/03/2016.
 */
public class RollingWindow{

    private Sensors sensors;

    private float[][] accelerometerWindow;
    private float[][] gyroscopeWindow;
    private float[][] magnetometerWindow;

    private Lock accelerometerWindowLock = new ReentrantLock();
    private Lock gyroscopeWindowLock = new ReentrantLock();
    private Lock magnetometerWindowLock = new ReentrantLock();

    private boolean hasBeenFilled = false;
    private int newWindowPosition = 0;

    SensorsValuesUpdater sensorsValuesUpdater;
    PeriodicalWindowChangeNotifier periodicalWindowChangeNotifier;

    private int WINDOW_SIZE = -1;

    private ArrayList<RollingWindowChanges> rollingWindowChangesListeners;

    public RollingWindow(Sensors sensors, int sampleFrequency, int windowFrequency,
                         ArrayList<RollingWindowChanges> rollingWindowChangesListeners) {
        this.sensors = sensors;
        WINDOW_SIZE = windowFrequency/sampleFrequency;
        this.rollingWindowChangesListeners = rollingWindowChangesListeners;
        accelerometerWindow = new float[WINDOW_SIZE][3];
        gyroscopeWindow = new float[WINDOW_SIZE][3];
        magnetometerWindow = new float[WINDOW_SIZE][3];

        sensorsValuesUpdater = new SensorsValuesUpdater();
        Timer timerSensorValuesUpdater = new Timer(true);
        timerSensorValuesUpdater.scheduleAtFixedRate(sensorsValuesUpdater, sampleFrequency * 10, sampleFrequency);

        periodicalWindowChangeNotifier = new PeriodicalWindowChangeNotifier();
        Timer timerPeriodicalWindowChangeNotifier = new Timer(true);
        timerPeriodicalWindowChangeNotifier.scheduleAtFixedRate(periodicalWindowChangeNotifier, windowFrequency + sampleFrequency * 10, 100);
    }

    private float[][][] snapshotOf3Windows() {
        float[][][] snapshotWindows = new float[3][][];
        accelerometerWindowLock.lock();
        snapshotWindows[0] = accelerometerWindow.clone();
        accelerometerWindowLock.unlock();

        gyroscopeWindowLock.lock();
        snapshotWindows[1] = gyroscopeWindow.clone();
        gyroscopeWindowLock.unlock();

        magnetometerWindowLock.lock();
        snapshotWindows[2] = magnetometerWindow.clone();
        magnetometerWindowLock.unlock();

        return snapshotWindows;
    }

    private class SensorsValuesUpdater extends TimerTask {

        @Override
        public void run() {
            if (sensors.areCollecting()) {
                accelerometerWindowLock.lock();
                accelerometerWindow[newWindowPosition] = sensors.getLastAccelerometerData();
                accelerometerWindowLock.unlock();

                gyroscopeWindowLock.lock();
                gyroscopeWindow[newWindowPosition] = sensors.getLastGyroscopeData();
                gyroscopeWindowLock.unlock();

                magnetometerWindowLock.lock();
                magnetometerWindow[newWindowPosition] = sensors.getLastMagnetometerData();
                magnetometerWindowLock.unlock();

                newWindowPosition = (newWindowPosition+1)%WINDOW_SIZE;
                if (!hasBeenFilled) {
                    hasBeenFilled = newWindowPosition == 0;
                }
                if (newWindowPosition == 0) {
                    float[][][] completeSnapshotOf3Windows = snapshotOf3Windows();
                    for(RollingWindowChanges rollingWindowChangesListener: rollingWindowChangesListeners) {
                        rollingWindowChangesListener.rollingWindowHasCompletelyChanged(completeSnapshotOf3Windows);
                    }
                }
            }
        }
    }

    private class PeriodicalWindowChangeNotifier extends TimerTask {

        @Override
        public void run() {
            if (sensors.areCollecting()) {
                long startTimestamp = System.currentTimeMillis();
                float[][][] snapshot3Windows = snapshotOf3Windows();
                long endTimestamp = System.currentTimeMillis();
                Log.v("Tiempo tardado", String.valueOf(endTimestamp - startTimestamp));
                for(RollingWindowChanges rollingWindowChangesListener: rollingWindowChangesListeners) {
                    rollingWindowChangesListener.rollingWindowHasRepresentativelyChanged(snapshot3Windows);
                }
            }
        }
    }
}
