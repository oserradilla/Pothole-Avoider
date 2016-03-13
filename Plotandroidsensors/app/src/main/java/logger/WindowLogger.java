package logger;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import java.util.concurrent.locks.ReentrantLock;

import rescuedroneapp.rescuedrone.labs.mediatek.plotandroidsensors.DevicePositionChangedListener;
import rescuedroneapp.rescuedrone.labs.mediatek.plotandroidsensors.MainActivity;
import rescuedroneapp.rescuedrone.labs.mediatek.plotandroidsensors.RollingWindowChanges;

/**
 * Created by oscar on 05/03/2016.
 */
public class WindowLogger implements RollingWindowChanges, DevicePositionChangedListener {

    private Context context;

    private ReentrantLock rotationMatrixLock;

    private float[] rotationMatrix = null;
    private float[][][] snapshot3Windows = null;

    private FileWriter fileWriter;
    private ReentrantLock fileWriterLock;

    public WindowLogger(Context context) {
        this.context = context;
        rotationMatrixLock = new ReentrantLock();
        fileWriter = new FileWriter(context);
        fileWriter.openNewFile();
        fileWriterLock = new ReentrantLock();
    }

    @Override
    public void rollingWindowHasRepresentativelyChanged(float[][][] snapshot3Windows) {
    }

    @Override
    public void rollingWindowHasCompletelyChanged(float[][][] snapshot3Windows) {
        float[] rotationMatrixCopy = null;
        rotationMatrixLock.lock();
        if (rotationMatrix != null) {
            rotationMatrixCopy = rotationMatrix.clone();
        }
        rotationMatrixLock.unlock();
        //if (rotationMatrixCopy != null) {
            this.snapshot3Windows = snapshot3Windows;
            FileWriterControllerThread fileWriterControllerThread = new FileWriterControllerThread(context, rotationMatrixCopy);
            fileWriterControllerThread.start();
        //}
    }

    @Override
    public void onDevicePositionChanged(float[] rotationMatrix) {
        rotationMatrixLock.lock();
        if (this.rotationMatrix == null) {
            newUserNotification();
        }
        this.rotationMatrix = rotationMatrix;
        rotationMatrixLock.unlock();
    }

    private void newUserNotification() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(context, notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void endLogging() {
        fileWriterLock.lock();
        fileWriter.closeFile();
        fileWriterLock.unlock();
    }



    private class FileWriterControllerThread extends Thread {

        private float[] rotationMatrix;
        private Context context;

        private FileWriterControllerThread (Context context, float[] rotationMatrix) {
            this.rotationMatrix = rotationMatrix;
            this.context = context;
        }

        @Override
        public void run() {
            long startTimestamp = System.currentTimeMillis();
            //float[][] accelerometerWindowInRealWorld = getVector3WindowInRealWorld(snapshot3Windows[0],rotationMatrix);
            //float[][] gyroscopeWindowInRealWorld = getVector3WindowInRealWorld(snapshot3Windows[1],rotationMatrix);
            fileWriterLock.lock();
            if (fileWriter != null) {
                saveAccelerometerAndGyroscopeWindowsToFile(
                        snapshot3Windows[0], snapshot3Windows[1]);
            }
            fileWriterLock.unlock();
            long endTimestamp = System.currentTimeMillis();
            long difference = endTimestamp - startTimestamp;
            ((MainActivity) context).showToast(String.valueOf(difference));
        }

        private float[][] getVector3WindowInRealWorld(float[][] vector3Window, float[] rotationMatrix) {
            float[][] vector3WindowInRealWorld = new float[vector3Window.length][vector3Window[0].length];
            for (int i=0; i < vector3Window.length; i++) {
                vector3WindowInRealWorld[i][0] = rotationMatrix[0] * vector3Window[i][0] + rotationMatrix[1] * vector3Window[i][1] + rotationMatrix[2] * vector3Window[i][2];
                vector3WindowInRealWorld[i][1] = rotationMatrix[3] * vector3Window[i][0] + rotationMatrix[4] * vector3Window[i][1] + rotationMatrix[5] * vector3Window[i][2];
                vector3WindowInRealWorld[i][2] = rotationMatrix[6] * vector3Window[i][0] + rotationMatrix[7] * vector3Window[i][1] + rotationMatrix[8] * vector3Window[i][2];
            }
            return vector3WindowInRealWorld;
        }

        private void saveAccelerometerAndGyroscopeWindowsToFile(
                float[][] accelerometerWindowInRealWorld, float[][] gyroscopeWindowInRealWorld) {
            int lengthToSave = accelerometerWindowInRealWorld.length > gyroscopeWindowInRealWorld.length ?
                    gyroscopeWindowInRealWorld.length : accelerometerWindowInRealWorld.length;
            for(int i=0; i < lengthToSave; i++) {
                fileWriter.setFloat(accelerometerWindowInRealWorld[i][0]);
                fileWriter.setFloat(accelerometerWindowInRealWorld[i][1]);
                fileWriter.setFloat(accelerometerWindowInRealWorld[i][2]);
                fileWriter.setFloat(gyroscopeWindowInRealWorld[i][0]);
                fileWriter.setFloat(gyroscopeWindowInRealWorld[i][1]);
                fileWriter.setFloat(gyroscopeWindowInRealWorld[i][2]);
                fileWriter.nextLine();
            }
        }
    }
}
