package rescuedroneapp.rescuedrone.labs.mediatek.plotandroidsensors;

import android.hardware.SensorManager;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by oscar on 29/02/2016.
 * Detects when the android device is at fixed position, looking at gyroscope's data
 * (if values are similar to 0, this means that the device is not rotating) and accelerometer's data
 * (if values are constant means that there is no acceleration).
 * When detected, it will generate the rotation matrix and notify to the listener specified in the constructor.
 */


public class Calibrator extends Thread implements RollingWindowChangesListener {

    private ArrayList<DevicePositionChangedListener> devicePositionChangedListeners;

    private final int MIN_WINDOW_SIZE_TO_CALIBRATE = 10000;

    private float[] lastAccelzMeansRealWorld = null;
    Lock lastAccelzMeansRealWorldLock;
    private int sizeOfWindowSizeToCalibrate;
    private int idxOfWindowSizeToCalibrate = 0;
    private Lock idxOfWindowSizeToCalibrateLock;
    private boolean windowSizeToCalibrateIsInitialized;
    private float[][] lastCalculusDeviceWorld;
    Lock lastCalculusDeviceWorldLock;
    Semaphore newRollingWindowDeviceWorldCalculusSemaphore;
    Semaphore newRollingWindowRealWorldCalculusSemaphore;
    private Boolean deviceWorldCalculusArrived = false;
    private Boolean realWorldCalculusArrived = false;
    private Semaphore waitUntilBothCalculusArrivedSemaphore;

    public Calibrator (int windowFrequency, ArrayList<DevicePositionChangedListener> devicePositionChangedListeners) {
        this.devicePositionChangedListeners = devicePositionChangedListeners;
        sizeOfWindowSizeToCalibrate = MIN_WINDOW_SIZE_TO_CALIBRATE / windowFrequency;
        sizeOfWindowSizeToCalibrate = sizeOfWindowSizeToCalibrate < (float) MIN_WINDOW_SIZE_TO_CALIBRATE/windowFrequency ?
                sizeOfWindowSizeToCalibrate + 1 : sizeOfWindowSizeToCalibrate;
        lastAccelzMeansRealWorld = new float[sizeOfWindowSizeToCalibrate];
        lastCalculusDeviceWorld = new float[sizeOfWindowSizeToCalibrate][6];
        windowSizeToCalibrateIsInitialized = sizeOfWindowSizeToCalibrate == 1;
        lastCalculusDeviceWorldLock = new ReentrantLock();
        lastAccelzMeansRealWorldLock = new ReentrantLock();
        idxOfWindowSizeToCalibrateLock = new ReentrantLock();
        newRollingWindowDeviceWorldCalculusSemaphore = new Semaphore(1);
        newRollingWindowRealWorldCalculusSemaphore = new Semaphore(1);
        waitUntilBothCalculusArrivedSemaphore = new Semaphore(0);
    }

    @Override
    public void newRollingWindowDeviceWorldCalculus(float[][] calculusMatrix) {
        try {
            newRollingWindowDeviceWorldCalculusSemaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        lastCalculusDeviceWorldLock.lock();
        idxOfWindowSizeToCalibrateLock.lock();
        lastCalculusDeviceWorld[idxOfWindowSizeToCalibrate][0]=calculusMatrix[0][0];
        lastCalculusDeviceWorld[idxOfWindowSizeToCalibrate][1]=calculusMatrix[0][1];
        lastCalculusDeviceWorld[idxOfWindowSizeToCalibrate][2]=calculusMatrix[0][2];
        lastCalculusDeviceWorld[idxOfWindowSizeToCalibrate][3]=calculusMatrix[0][6];
        lastCalculusDeviceWorld[idxOfWindowSizeToCalibrate][4]=calculusMatrix[0][7];
        lastCalculusDeviceWorld[idxOfWindowSizeToCalibrate][5]=calculusMatrix[0][8];
        idxOfWindowSizeToCalibrateLock.unlock();
        lastCalculusDeviceWorldLock.unlock();
        synchronized (deviceWorldCalculusArrived) {
            synchronized (realWorldCalculusArrived){
                if (realWorldCalculusArrived) {
                    realWorldCalculusArrived = false;
                    deviceWorldCalculusArrived = false;
                    waitUntilBothCalculusArrivedSemaphore.release();
                } else {
                    deviceWorldCalculusArrived = true;
                }
            }
        }
    }

    @Override
    public void newRollingWindowRealWorldCalculus(float[][] calculusMatrix) {
        try {
            newRollingWindowRealWorldCalculusSemaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        lastAccelzMeansRealWorldLock.lock();
        idxOfWindowSizeToCalibrateLock.lock();
        lastAccelzMeansRealWorld[idxOfWindowSizeToCalibrate]=calculusMatrix[0][2];
        idxOfWindowSizeToCalibrateLock.unlock();
        lastAccelzMeansRealWorldLock.unlock();
        synchronized (deviceWorldCalculusArrived) {
            synchronized (realWorldCalculusArrived){
                if (deviceWorldCalculusArrived) {
                    deviceWorldCalculusArrived = false;
                    realWorldCalculusArrived = false;
                    waitUntilBothCalculusArrivedSemaphore.release();
                } else {
                    realWorldCalculusArrived = true;
                }
            }
        }
    }

    // To skip the first window arrived after recalculation of the rotation matrix (because there will be some data belonging to the previous rotation matrix so should be skipped
    private boolean firstWindowSkipped = false;

    @Override
    public void run() {
        while (true) {
            try {
                waitUntilBothCalculusArrivedSemaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            idxOfWindowSizeToCalibrateLock.lock();
            idxOfWindowSizeToCalibrate = (idxOfWindowSizeToCalibrate+1) % sizeOfWindowSizeToCalibrate;
            if (windowSizeToCalibrateIsInitialized) {
                if (deviceHasToBeCalibrated()) {
                    float[] rotationMatrix = calulateRotationMatrix();
                    if (rotationMatrix != null) {
                        for (DevicePositionChangedListener devicePositionChangedListener : devicePositionChangedListeners) {
                            devicePositionChangedListener.onDevicePositionChanged(rotationMatrix);
                        }
                        idxOfWindowSizeToCalibrate = 0;
                        windowSizeToCalibrateIsInitialized = false;
                        firstWindowSkipped = false;
                    }
                }
            } else {
                if (!firstWindowSkipped) {
                    firstWindowSkipped = true;
                    idxOfWindowSizeToCalibrate = idxOfWindowSizeToCalibrate-1 < 0 ? 0 : idxOfWindowSizeToCalibrate-1;
                } else {
                    windowSizeToCalibrateIsInitialized = idxOfWindowSizeToCalibrate == 0;
                }
            }
            idxOfWindowSizeToCalibrateLock.unlock();
            newRollingWindowDeviceWorldCalculusSemaphore.release();
            newRollingWindowRealWorldCalculusSemaphore.release();
        }
    }

    private float[] calulateRotationMatrix() {
        float[][] accelerometerMagnetometerMeans = getAccelerometerMagnetometerMeans();
        float[] rotationMatrix = new float[9];
        boolean isCorrect = SensorManager.getRotationMatrix(rotationMatrix, null,
                accelerometerMagnetometerMeans[0], accelerometerMagnetometerMeans[1]);
        if (!isCorrect) {
            rotationMatrix = null;
        }
        return rotationMatrix;
    }

    private float[][] getAccelerometerMagnetometerMeans() {
        float[][] accelerometerMagnetometerMeans = new float[][]{
                {0.0f, 0.0f, 0.0f},
                {0.0f, 0.0f, 0.0f}
        };
        lastCalculusDeviceWorldLock.lock();
        for(int i = 0; i < sizeOfWindowSizeToCalibrate; i++) {
            accelerometerMagnetometerMeans[0][0] += lastCalculusDeviceWorld[i][0];
            accelerometerMagnetometerMeans[0][1] += lastCalculusDeviceWorld[i][1];
            accelerometerMagnetometerMeans[0][2] += lastCalculusDeviceWorld[i][2];
            accelerometerMagnetometerMeans[1][0] += lastCalculusDeviceWorld[i][3];
            accelerometerMagnetometerMeans[1][1] += lastCalculusDeviceWorld[i][4];
            accelerometerMagnetometerMeans[1][2] += lastCalculusDeviceWorld[i][5];
        }
        lastCalculusDeviceWorldLock.unlock();
        accelerometerMagnetometerMeans[0][0] /= sizeOfWindowSizeToCalibrate;
        accelerometerMagnetometerMeans[0][1] /= sizeOfWindowSizeToCalibrate;
        accelerometerMagnetometerMeans[0][2] /= sizeOfWindowSizeToCalibrate;
        accelerometerMagnetometerMeans[1][0] /= sizeOfWindowSizeToCalibrate;
        accelerometerMagnetometerMeans[1][1] /= sizeOfWindowSizeToCalibrate;
        accelerometerMagnetometerMeans[1][2] /= sizeOfWindowSizeToCalibrate;
        return accelerometerMagnetometerMeans;
    }

    private boolean deviceHasToBeCalibrated() {
        float sumOfLastAccelzMeans = 0;
        lastAccelzMeansRealWorldLock.lock();
        for(int i = 0; i < sizeOfWindowSizeToCalibrate; i++) {
            sumOfLastAccelzMeans += lastAccelzMeansRealWorld[i];
        }
        float meanOfLastAccelzMeans = sumOfLastAccelzMeans / sizeOfWindowSizeToCalibrate;
        lastAccelzMeansRealWorldLock.unlock();
        boolean needsCalibration = meanOfLastAccelzMeans < 9.3f || meanOfLastAccelzMeans > 10.3f;
        return needsCalibration;
    }

    @Override
    public void newRollingWindowRawData(float[][][] snapshotOfAccelGyroMagnetoInRawWindows) {}

    @Override
    public void newRollingWindowTransformedToRealWorld(float[][][] snapshotAccelGyroMagnetoRealWorldWindows) {}

}
