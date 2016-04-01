package rescuedroneapp.rescuedrone.labs.mediatek.plotandroidsensors;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import gps.SpeedLastValue;

/**
 * Created by oscar on 02/03/2016.
 */
public class RollingWindow{

    private Sensors sensors;
    private SpeedLastValue speedLastValue;

    private float[][] accelerometerWindow;
    private float[][] gyroscopeWindow;
    private float[][] magnetometerWindow;
    private int[] speedWindow;

    private Lock accelerometerWindowLock = new ReentrantLock();
    private Lock gyroscopeWindowLock = new ReentrantLock();
    private Lock magnetometerWindowLock = new ReentrantLock();
    private Lock speedWindowLock = new ReentrantLock();
    private Lock newWindowPositionLock = new ReentrantLock();

    private boolean hasBeenFilled = false;
    private int newWindowPosition = 0;

    SensorsValuesUpdater sensorsValuesUpdater;

    private int WINDOW_SIZE = -1;

    private ArrayList<RollingWindowChangesListener> rollingWindowChangesListenerListeners;

    public RollingWindow(Sensors sensors, SpeedLastValue speedLastValue, int sampleFrequency, int windowFrequency,
                         ArrayList<RollingWindowChangesListener> rollingWindowChangesListenerListeners) {
        this.sensors = sensors;
        this.speedLastValue = speedLastValue;
        WINDOW_SIZE = windowFrequency/sampleFrequency;
        this.rollingWindowChangesListenerListeners = rollingWindowChangesListenerListeners;
        accelerometerWindow = new float[WINDOW_SIZE][3];
        gyroscopeWindow = new float[WINDOW_SIZE][3];
        magnetometerWindow = new float[WINDOW_SIZE][3];
        speedWindow = new int[WINDOW_SIZE];
        sensorsValuesUpdater = new SensorsValuesUpdater();
        Timer timerSensorValuesUpdater = new Timer(true);
        timerSensorValuesUpdater.scheduleAtFixedRate(sensorsValuesUpdater, sampleFrequency * 10, sampleFrequency);
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

    private int[] snapshotOfSpeedWindow() {
        int[] snapshotOfSpeedWindow;
        speedWindowLock.lock();
        snapshotOfSpeedWindow = speedWindow.clone();
        speedWindowLock.unlock();
        return snapshotOfSpeedWindow;
    }

    private class SensorsValuesUpdater extends TimerTask {

        @Override
        public void run() {
            if (sensors.areCollecting()) {
                newWindowPositionLock.lock();
                accelerometerWindowLock.lock();
                accelerometerWindow[newWindowPosition] = sensors.getLastAccelerometerData();
                accelerometerWindowLock.unlock();

                gyroscopeWindowLock.lock();
                gyroscopeWindow[newWindowPosition] = sensors.getLastGyroscopeData();
                gyroscopeWindowLock.unlock();

                magnetometerWindowLock.lock();
                magnetometerWindow[newWindowPosition] = sensors.getLastMagnetometerData();
                magnetometerWindowLock.unlock();

                speedWindowLock.lock();
                speedWindow[newWindowPosition] = speedLastValue.getLastSpeed();
                speedWindowLock.unlock();

                newWindowPosition = (newWindowPosition+1)%WINDOW_SIZE;
                if (!hasBeenFilled) {
                    hasBeenFilled = newWindowPosition == 0;
                }
                if (newWindowPosition == 0)
                    new RollingWindowChangesNotifier().start();
                newWindowPositionLock.unlock();
            }
        }

        private class RollingWindowChangesNotifier extends Thread {

            @Override
            public void run() {
                newWindowPositionLock.lock();
                float[][][] snapshotOf3Windows = snapshotOf3Windows();
                int[] snapshotOfSpeedWindow = snapshotOfSpeedWindow();
                newWindowPositionLock.unlock();
                for(RollingWindowChangesListener rollingWindowChangesListenerListener : rollingWindowChangesListenerListeners) {
                    rollingWindowChangesListenerListener.newRollingWindowRawData(snapshotOf3Windows, snapshotOfSpeedWindow);
                }
            }
        }
    }
}
