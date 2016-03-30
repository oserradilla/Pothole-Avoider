package rescuedroneapp.rescuedrone.labs.mediatek.plotandroidsensors;

import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by oscar on 21/03/2016.
 */
public class GatherDataForAI implements RollingWindowChangesListener {

    private float[][][] firstWindowPieceAccelGyroMagnetoRealWorldWindows = null;
    private float[][][] secondWindowPieceAccelGyroMagnetoRealWorldWindows = null;
    private int[] firstWindowPieceSpeedWindow = null;
    private int[] secondWindowPieceSpeedWindow = null;
    private Lock windowReferencesLock;
    private Semaphore waitUntilNewCalculusArrivesSemaphore;
    private Semaphore waitUntilNewRealWorldWindowArrivesSemaphore;

    private float[][] firstWindowRealWorldCalculus = null;
    private float[][] secondWindowRealWorldCalculus = null;
    private Lock realWorldCalculusWindowsLock;

    public GatherDataForAI() {
        waitUntilNewCalculusArrivesSemaphore = new Semaphore(1);
        waitUntilNewRealWorldWindowArrivesSemaphore = new Semaphore(0);
        windowReferencesLock = new ReentrantLock();
        realWorldCalculusWindowsLock = new ReentrantLock();
    }

    @Override
    public void newRollingWindowTransformedToRealWorld(float[][][] snapshotAccelGyroMagnetoRealWorldWindows, int[] snapshotOfSpeedWindow) {
        new newRollingWindowTransformedToRealWorldThread(snapshotAccelGyroMagnetoRealWorldWindows, snapshotOfSpeedWindow).start();
    }

    @Override
    public void newRollingWindowRealWorldCalculus(float[][] calculusMatrix) {
        new newRollingWindowRealWorldCalculusThread(calculusMatrix).start();
    }

    private class newRollingWindowTransformedToRealWorldThread extends Thread {

        float[][][] snapshotAccelGyroMagnetoRealWorldWindows;
        int[] snapshotSpeedWindow;

        private newRollingWindowTransformedToRealWorldThread(float[][][] snapshotAccelGyroMagnetoRealWorldWindows,
                                                             int[] snapshotSpeedWindow) {
            this.snapshotAccelGyroMagnetoRealWorldWindows = snapshotAccelGyroMagnetoRealWorldWindows;
            this.snapshotSpeedWindow = snapshotSpeedWindow;
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
            firstWindowPieceSpeedWindow = secondWindowPieceSpeedWindow;
            secondWindowPieceSpeedWindow = snapshotSpeedWindow;
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
            realWorldCalculusWindowsLock.lock();
            firstWindowRealWorldCalculus = secondWindowRealWorldCalculus;
            secondWindowRealWorldCalculus = calculusMatrix;

            windowReferencesLock.lock();
            if (firstWindowPieceAccelGyroMagnetoRealWorldWindows != null &&
                    firstWindowPieceSpeedWindow != null &&
                    firstWindowRealWorldCalculus != null) {
                DataToBeAnalysedByAI dataToBeAnalysedByAI = new DataToBeAnalysedByAI();
                dataToBeAnalysedByAI.setFirstWindowPieceAccelGyroMagnetoRealWorldWindows(firstWindowPieceAccelGyroMagnetoRealWorldWindows);
                dataToBeAnalysedByAI.setSecondWindowPieceAccelGyroMagnetoRealWorldWindows(secondWindowPieceAccelGyroMagnetoRealWorldWindows);
                dataToBeAnalysedByAI.setFirstWindowPieceSpeedWindow(firstWindowPieceSpeedWindow);
                dataToBeAnalysedByAI.setSecondWindowPieceSpeedWindow(secondWindowPieceSpeedWindow);
                dataToBeAnalysedByAI.setFirstWindowRealWorldCalculus(firstWindowRealWorldCalculus);
                dataToBeAnalysedByAI.setSecondWindowRealWorldCalculus(secondWindowRealWorldCalculus);
                new AIThread(dataToBeAnalysedByAI).start();
            }
            windowReferencesLock.unlock();
            realWorldCalculusWindowsLock.unlock();

            waitUntilNewRealWorldWindowArrivesSemaphore.release();
            waitUntilNewCalculusArrivesSemaphore.release();
        }
    }

    @Override
    public void newRollingWindowRawData(float[][][] snapshotOfAccelGyroMagnetoInRawWindows, int[] snapshotOfSpeedWindow) {}

    @Override
    public void newRollingWindowDeviceWorldCalculus(float[][] calculusMatrix) {}
}
