package rescuedroneapp.rescuedrone.labs.mediatek.plotandroidsensors;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by oscar on 18/03/2016.
 */
public class RealWorldTransformation implements RollingWindowChangesListener, DevicePositionChangedListener {

    float[] rotationMatrix;
    Lock rotationMatrixLock;
    private ArrayList<RollingWindowChangesListener> rollingWindowChangesListenerListeners;

    public RealWorldTransformation(ArrayList<RollingWindowChangesListener> rollingWindowChangesListenerListeners) {
        rotationMatrix = new float[]{1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f};
        this.rollingWindowChangesListenerListeners = rollingWindowChangesListenerListeners;
        rotationMatrixLock = new ReentrantLock();
    }

    @Override
    public void newRollingWindowRawData(float[][][] snapshotOfAccelGyroMagnetoInRawWindows, int[] snapshotOfSpeedWindow) {
        new newRollingWindowTransformNotifyThread(snapshotOfAccelGyroMagnetoInRawWindows, snapshotOfSpeedWindow).start();
    }

    @Override
    public void onDevicePositionChanged(float[] rotationMatrix) {
        rotationMatrixLock.lock();
        this.rotationMatrix = rotationMatrix;
        rotationMatrixLock.unlock();
    }

    private class newRollingWindowTransformNotifyThread extends Thread {

        private float[][][] snapshotOfAccelGyroMagnetoInRawWindows;
        private int[] snapshotOfSpeedWindow;

        private newRollingWindowTransformNotifyThread(float[][][] snapshotOfAccelGyroMagnetoInRawWindows,
                                                      int[] snapshotOfSpeedWindow) {
            this.snapshotOfAccelGyroMagnetoInRawWindows = snapshotOfAccelGyroMagnetoInRawWindows;
            this.snapshotOfSpeedWindow = snapshotOfSpeedWindow;
        }

        @Override
        public void run() {
            float[][][] newRollingWindowTransformedToRealWorld = transform3WindowsVectorToRealWorld();
            for (RollingWindowChangesListener rollingWindowChangesListenerListener : rollingWindowChangesListenerListeners) {
                rollingWindowChangesListenerListener.newRollingWindowTransformedToRealWorld(
                        newRollingWindowTransformedToRealWorld, snapshotOfSpeedWindow);
            }
        }

        private float[][][] transform3WindowsVectorToRealWorld() {
            int numWindows = 3;
            int numInstancesInWindow = snapshotOfAccelGyroMagnetoInRawWindows[0].length;
            int numVarsPerInstance = 3;
            float[][][] newRollingWindowTransformedToRealWorld =
                    new float[numWindows][numInstancesInWindow][numVarsPerInstance];
            for (int i = 0; i < numWindows; i++) {
                transform3WindowsToRealWorld(snapshotOfAccelGyroMagnetoInRawWindows[i], newRollingWindowTransformedToRealWorld[i]);
            }
            return newRollingWindowTransformedToRealWorld;
        }
    }


    @Override
    public void newRollingWindowTransformedToRealWorld(float[][][] snapshotAccelGyroMagnetoRealWorldWindows, int[] snapshotOfSpeedWindow) {}

    @Override
    public void newRollingWindowDeviceWorldCalculus(float[][] calculusMatrix) {}

    @Override
    public void newRollingWindowRealWorldCalculus(float[][] calculusMatrix) {}

    private void transform3WindowsToRealWorld(float[][] vector3Window, float[][] vector3WindowInRealWorld) {
        int numInstancesInWindow = vector3Window.length;
        rotationMatrixLock.lock();
        for (int j = 0; j < numInstancesInWindow; j++) {
            vector3WindowInRealWorld[j][0] = rotationMatrix[0] * vector3Window[j][0] + rotationMatrix[1] * vector3Window[j][1] + rotationMatrix[2] * vector3Window[j][2];
            vector3WindowInRealWorld[j][1] = rotationMatrix[3] * vector3Window[j][0] + rotationMatrix[4] * vector3Window[j][1] + rotationMatrix[5] * vector3Window[j][2];
            vector3WindowInRealWorld[j][2] = rotationMatrix[6] * vector3Window[j][0] + rotationMatrix[7] * vector3Window[j][1] + rotationMatrix[8] * vector3Window[j][2];
        }
        rotationMatrixLock.unlock();
    }
}