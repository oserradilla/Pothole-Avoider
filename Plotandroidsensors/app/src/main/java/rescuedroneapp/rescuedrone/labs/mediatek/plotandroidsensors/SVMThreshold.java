package rescuedroneapp.rescuedrone.labs.mediatek.plotandroidsensors;

import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by oscar on 21/03/2016.
 */
public class SVMThreshold implements RollingWindowChangesListener {

    private float[][][] firstWindowPieceAccelGyroMagnetoRealWorldWindows = null;
    private float[][][] secondWindowPieceAccelGyroMagnetoRealWorldWindows = null;
    private Lock windowReferencesLock;
    private Semaphore waitUntilNewCalculusArrivesSemaphore;
    private Semaphore waitUntilNewRealWorldWindowArrivesSemaphore;

    public SVMThreshold() {
        waitUntilNewCalculusArrivesSemaphore = new Semaphore(1);
        waitUntilNewRealWorldWindowArrivesSemaphore = new Semaphore(0);
        windowReferencesLock = new ReentrantLock();
    }

    @Override
    public void newRollingWindowTransformedToRealWorld(float[][][] snapshotAccelGyroMagnetoRealWorldWindows) {
        new newRollingWindowTransformedToRealWorldThread(snapshotAccelGyroMagnetoRealWorldWindows).start();
    }

    @Override
    public void newRollingWindowRealWorldCalculus(float[][] calculusMatrix) {
        new newRollingWindowRealWorldCalculusThread(calculusMatrix).start();
    }

    private class newRollingWindowTransformedToRealWorldThread extends Thread {

        float[][][] snapshotAccelGyroMagnetoRealWorldWindows;

        private newRollingWindowTransformedToRealWorldThread(float[][][] snapshotAccelGyroMagnetoRealWorldWindows) {
            this.snapshotAccelGyroMagnetoRealWorldWindows = snapshotAccelGyroMagnetoRealWorldWindows;
        }

        @Override
        public void run() {
            try {
                waitUntilNewCalculusArrivesSemaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            windowReferencesLock.lock();
            firstWindowPieceAccelGyroMagnetoRealWorldWindows = secondWindowPieceAccelGyroMagnetoRealWorldWindows;
            secondWindowPieceAccelGyroMagnetoRealWorldWindows = snapshotAccelGyroMagnetoRealWorldWindows;
            windowReferencesLock.unlock();
            waitUntilNewRealWorldWindowArrivesSemaphore.release();
        }
    }

    private class newRollingWindowRealWorldCalculusThread extends Thread {

        private float[][] calculusMatrix;

        private newRollingWindowRealWorldCalculusThread(float[][] calculusMatrix) {
            this.calculusMatrix = calculusMatrix;
        }

        @Override
        public void run() {
            try {
                waitUntilNewRealWorldWindowArrivesSemaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (calculusMatrix[2][0] > 6 && calculusMatrix[2][3] < 0.5) {
                windowReferencesLock.lock();
                if (firstWindowPieceAccelGyroMagnetoRealWorldWindows != null) {
                    new EnergyThreshold(firstWindowPieceAccelGyroMagnetoRealWorldWindows,
                            secondWindowPieceAccelGyroMagnetoRealWorldWindows).start();
                }
                windowReferencesLock.unlock();
            }
            waitUntilNewCalculusArrivesSemaphore.release();
        }
    }

    @Override
    public void newRollingWindowRawData(float[][][] snapshotOfAccelGyroMagnetoInRawWindows) {}

    @Override
    public void newRollingWindowDeviceWorldCalculus(float[][] calculusMatrix) {}
}
