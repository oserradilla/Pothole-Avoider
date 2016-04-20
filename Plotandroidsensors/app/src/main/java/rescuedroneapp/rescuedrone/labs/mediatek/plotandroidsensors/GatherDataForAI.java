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

    private final int LAST_REAL_WORLD_CALCULUS_TO_STORE_MILLISECONDS = 30000;
    private float[][][] windowToStoreRealWorldCalculus = null;
    private Lock realWorldCalculusIdxLock = new ReentrantLock();
    boolean windowRealWorldCalculusHasBeenFilled = false;
    private int realWorldCalculusIdx = 0;
    private int NUM_WINDOWS_TO_STORE_REAL_WORLD_CALCULUS;
    private boolean hasBeenFilled = false;
    private int newWindowPosition = 0;
    private Lock newWindowPositionLock = new ReentrantLock();

    private CurrentIncidenceState currentIncidenceState;

    public GatherDataForAI(int windowFrequency) {
        NUM_WINDOWS_TO_STORE_REAL_WORLD_CALCULUS = (int) Math.ceil(
                (float) LAST_REAL_WORLD_CALCULUS_TO_STORE_MILLISECONDS /windowFrequency);
        //TODO Remove temporal hardcoding of 3 windows (4.5s)
        NUM_WINDOWS_TO_STORE_REAL_WORLD_CALCULUS = 3;
        windowToStoreRealWorldCalculus = new float[NUM_WINDOWS_TO_STORE_REAL_WORLD_CALCULUS][][];
        waitUntilNewCalculusArrivesSemaphore = new Semaphore(1);
        waitUntilNewRealWorldWindowArrivesSemaphore = new Semaphore(0);
        windowReferencesLock = new ReentrantLock();
        realWorldCalculusWindowsLock = new ReentrantLock();
        currentIncidenceState = new CurrentIncidenceState();
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
            boolean passesSVMThreshold = passesSVMThreshold();
            newWindowPositionLock.lock();
            newWindowPosition = (newWindowPosition + 1) % NUM_WINDOWS_TO_STORE_REAL_WORLD_CALCULUS;
            if (!hasBeenFilled) {
                hasBeenFilled = newWindowPosition == 0;
            }

                newWindowPositionLock.unlock();


                realWorldCalculusWindowsLock.lock();
                firstWindowRealWorldCalculus = secondWindowRealWorldCalculus;
                secondWindowRealWorldCalculus = calculusMatrix;

                windowReferencesLock.lock();
                if (firstWindowPieceAccelGyroMagnetoRealWorldWindows != null &&
                        firstWindowPieceSpeedWindow != null &&
                        firstWindowRealWorldCalculus != null) {
                    if (passesSVMThreshold) {
                        DataToBeAnalysedByAI dataToBeAnalysedByAI = new DataToBeAnalysedByAI();
                        dataToBeAnalysedByAI.setFirstWindowPieceAccelGyroMagnetoRealWorldWindows(firstWindowPieceAccelGyroMagnetoRealWorldWindows);
                        dataToBeAnalysedByAI.setSecondWindowPieceAccelGyroMagnetoRealWorldWindows(secondWindowPieceAccelGyroMagnetoRealWorldWindows);
                        dataToBeAnalysedByAI.setFirstWindowPieceSpeedWindow(firstWindowPieceSpeedWindow);
                        dataToBeAnalysedByAI.setSecondWindowPieceSpeedWindow(secondWindowPieceSpeedWindow);
                        dataToBeAnalysedByAI.setFirstWindowRealWorldCalculus(firstWindowRealWorldCalculus);
                        dataToBeAnalysedByAI.setSecondWindowRealWorldCalculus(secondWindowRealWorldCalculus);
                        new AIPotholes(dataToBeAnalysedByAI, currentIncidenceState).start();
                    }
                }

                windowReferencesLock.unlock();

                realWorldCalculusIdxLock.lock();
                windowToStoreRealWorldCalculus[realWorldCalculusIdx] = calculusMatrix;
                realWorldCalculusIdx = (realWorldCalculusIdx + 1) % NUM_WINDOWS_TO_STORE_REAL_WORLD_CALCULUS;
                if (!windowRealWorldCalculusHasBeenFilled) {
                    windowRealWorldCalculusHasBeenFilled = realWorldCalculusIdx == 0;
                }
                if (windowRealWorldCalculusHasBeenFilled) {
                    if (passesSVMThreshold) {
                        float[][][] copyRealWorldCalculusWindow = new float[windowToStoreRealWorldCalculus.length][][];
                        for (int i = 0; i < windowToStoreRealWorldCalculus.length; i++) {
                            copyRealWorldCalculusWindow[i] = windowToStoreRealWorldCalculus[i];
                        }
                        new AICurves(copyRealWorldCalculusWindow, currentIncidenceState).start();
                    }
                }
                realWorldCalculusIdxLock.unlock();

                realWorldCalculusWindowsLock.unlock();

                waitUntilNewRealWorldWindowArrivesSemaphore.release();
                waitUntilNewCalculusArrivesSemaphore.release();

        }

        private boolean passesSVMThreshold() {
            boolean passesSVMThreshold = false;
            float[][] firstCalculusMatrix = firstWindowRealWorldCalculus;
            float[][] secondCalculusMatrix = secondWindowRealWorldCalculus;
            if (firstCalculusMatrix != null && secondCalculusMatrix != null) {
                float avgAccelerometerSVM = (firstCalculusMatrix[2][0] + secondCalculusMatrix[2][0]) / 2;
                float avgGyroscopeSVM = (firstCalculusMatrix[2][3] + secondCalculusMatrix[2][3]) / 2;
                if (avgAccelerometerSVM > 6 && avgGyroscopeSVM < 0.5) {
                    passesSVMThreshold = true;
                }
            }
            return passesSVMThreshold;
        }
    }

    @Override
    public void newRollingWindowRawData(float[][][] snapshotOfAccelGyroMagnetoInRawWindows, int[] snapshotOfSpeedWindow) {}

    @Override
    public void newRollingWindowDeviceWorldCalculus(float[][] calculusMatrix) {}
}
