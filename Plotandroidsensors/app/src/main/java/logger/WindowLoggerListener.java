package logger;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import java.util.concurrent.locks.ReentrantLock;

import rescuedroneapp.rescuedrone.labs.mediatek.plotandroidsensors.DevicePositionChangedListener;
import rescuedroneapp.rescuedrone.labs.mediatek.plotandroidsensors.MainActivity;
import rescuedroneapp.rescuedrone.labs.mediatek.plotandroidsensors.RollingWindowChangesListener;

/**
 * Created by oscar on 05/03/2016.
 */
public class WindowLoggerListener implements RollingWindowChangesListener {

    private Context context;

    private float[][][] snapshot3Windows = null;

    private FileWriter fileWriter;
    private ReentrantLock fileWriterLock;

    public WindowLoggerListener(Context context) {
        this.context = context;
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
    public void newRollingWindowTransformedToRealWorld(float[][][] snapshotAccelGyroMagnetoRealWorldWindows) {
        this.snapshot3Windows = snapshotAccelGyroMagnetoRealWorldWindows;
        FileWriterControllerThread fileWriterControllerThread = new FileWriterControllerThread(context);
        fileWriterControllerThread.start();
    }

    @Override
    public void newRollingWindowDeviceWorldCalculus(float[][] calculusMatrix) {}

    @Override
    public void newRollingWindowRealWorldCalculus(float[][] calculusMatrix) {}

    @Override
    public void newRollingWindowRawData(float[][][] snapshotOfAccelGyroMagnetoInRawWindows) {}


    private class FileWriterControllerThread extends Thread {

        private Context context;

        private FileWriterControllerThread (Context context) {
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
