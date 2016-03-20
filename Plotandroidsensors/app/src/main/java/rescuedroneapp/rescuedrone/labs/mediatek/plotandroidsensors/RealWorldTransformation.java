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
    public void newRollingWindowRawData(float[][][] snapshotOfAccelGyroMagnetoInRawWindows) {
        new newRollingWindowTransformNotifyThread(snapshotOfAccelGyroMagnetoInRawWindows).start();
    }

    @Override
    public void newRollingWindowDeviceWorldCalculus(float[][] calculusMatrix) {
        new newRollingWindowCalculusTransformNotifyThread(calculusMatrix).start();
    }

    @Override
    public void onDevicePositionChanged(float[] rotationMatrix) {
        rotationMatrixLock.lock();
        this.rotationMatrix = rotationMatrix;
        rotationMatrixLock.unlock();
    }

    private class newRollingWindowTransformNotifyThread extends Thread {

        private float[][][] snapshotOfAccelGyroMagnetoInRawWindows;

        private newRollingWindowTransformNotifyThread(float[][][] snapshotOfAccelGyroMagnetoInRawWindows) {
            this.snapshotOfAccelGyroMagnetoInRawWindows = snapshotOfAccelGyroMagnetoInRawWindows;
        }

        @Override
        public void run() {
            float[][][] newRollingWindowTransformedToRealWorld = transform3WindowsVectorToRealWorld();
            for (RollingWindowChangesListener rollingWindowChangesListenerListener : rollingWindowChangesListenerListeners) {
                rollingWindowChangesListenerListener.newRollingWindowTransformedToRealWorld(newRollingWindowTransformedToRealWorld);
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

    private class newRollingWindowCalculusTransformNotifyThread extends Thread {

        private float[][] calculusMatrix;

        private newRollingWindowCalculusTransformNotifyThread(float[][] calculusMatrix) {
            this.calculusMatrix = calculusMatrix;
        }

        @Override
        public void run() {
            float[][] newRollingWindowCalculusTransformedToRealWorld = new float[calculusMatrix.length][calculusMatrix[0].length];
            transform3WindowsToRealWorld(calculusMatrix, newRollingWindowCalculusTransformedToRealWorld);
            for (RollingWindowChangesListener rollingWindowChangesListenerListener : rollingWindowChangesListenerListeners) {
                rollingWindowChangesListenerListener.newRollingWindowRealWorldCalculus(newRollingWindowCalculusTransformedToRealWorld);
            }
        }
    }


    @Override
    public void newRollingWindowTransformedToRealWorld(float[][][] snapshotAccelGyroMagnetoRealWorldWindows) {}

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