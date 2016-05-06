package com.oscarsc.potholeavoider.logger;

import android.content.Context;

import com.oscarsc.potholeavoider.artificial_intelligence.RollingWindowChangesListener;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by oscar on 05/03/2016.
 */
public class WindowLoggerListener implements RollingWindowChangesListener {

    private FileWriter fileWriter;
    private ReentrantLock fileWriterLock;

    public WindowLoggerListener(Context context) {
        fileWriter = new FileWriter(context);
        fileWriter.openNewFile();
        fileWriterLock = new ReentrantLock();
    }

    public void endLogging() {
        fileWriterLock.lock();
        fileWriter.closeFile();
        fileWriterLock.unlock();
    }

    @Override
    public void newRollingWindowTransformedToRealWorld(float[][][] snapshotAccelGyroMagnetoRealWorldWindows, int[] snapshotOfSpeedWindow) {
        FileWriterControllerThread fileWriterControllerThread = new FileWriterControllerThread(
                snapshotAccelGyroMagnetoRealWorldWindows, snapshotOfSpeedWindow);
        fileWriterControllerThread.start();
    }

    @Override
    public void newRollingWindowDeviceWorldCalculus(float[][] calculusMatrix) {}

    @Override
    public void newRollingWindowRealWorldCalculus(float[][] calculusMatrix) {}

    @Override
    public void newRollingWindowRawData(float[][][] snapshotOfAccelGyroMagnetoInRawWindows, int[] snapshotOfSpeedWindow) {}


    private class FileWriterControllerThread extends Thread {
        private float[][][] snapshotAccelGyroMagnetoRealWorldWindows = null;
        private int[] snapshotOfSpeedWindow = null;

        private FileWriterControllerThread (float[][][] snapshotAccelGyroMagnetoRealWorldWindows,
                                            int[] snapshotOfSpeedWindow) {
            this.snapshotAccelGyroMagnetoRealWorldWindows = snapshotAccelGyroMagnetoRealWorldWindows;
            this.snapshotOfSpeedWindow = snapshotOfSpeedWindow;
        }

        @Override
        public void run() {
            fileWriterLock.lock();
            if (fileWriter != null) {
                saveAccelerometerGyroscopeAndSpeedWindowsToFile(
                        snapshotAccelGyroMagnetoRealWorldWindows[0], snapshotAccelGyroMagnetoRealWorldWindows[1], snapshotOfSpeedWindow);
            }
            fileWriterLock.unlock();
        }

        private void saveAccelerometerGyroscopeAndSpeedWindowsToFile(
                float[][] accelerometerWindowInRealWorld, float[][] gyroscopeWindowInRealWorld,
                int[] speedWindow) {
            int lengthToSave = accelerometerWindowInRealWorld.length > gyroscopeWindowInRealWorld.length ?
                    gyroscopeWindowInRealWorld.length : accelerometerWindowInRealWorld.length;
            for(int i=0; i < lengthToSave; i++) {
                fileWriter.setFloat(accelerometerWindowInRealWorld[i][0]);
                fileWriter.setFloat(accelerometerWindowInRealWorld[i][1]);
                fileWriter.setFloat(accelerometerWindowInRealWorld[i][2]);
                fileWriter.setFloat(gyroscopeWindowInRealWorld[i][0]);
                fileWriter.setFloat(gyroscopeWindowInRealWorld[i][1]);
                fileWriter.setFloat(gyroscopeWindowInRealWorld[i][2]);
                fileWriter.setInt(speedWindow[i]);
                fileWriter.nextLine();
            }
        }
    }
}
