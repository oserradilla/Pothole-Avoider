package rescuedroneapp.rescuedrone.labs.mediatek.plotandroidsensors;

import android.hardware.SensorManager;

import java.util.ArrayList;

/**
 * Created by oscar on 29/02/2016.
 * Detects when the android device is at fixed position, looking at gyroscope's data
 * (if values are similar to 0, this means that the device is not rotating) and accelerometer's data
 * (if values are constant means that there is no acceleration).
 * When detected, it will generate the rotation matrix and notify to the listener specified in the constructor.
 */


public class Calibrator extends Thread implements RollingWindowChanges{

    private final float GYROSCOPE_MEAN_MAX_ROTATION_NOT_MOVING = 0.01f;
    private final float GYROSCOPE_VARIANCE_MAX_ROTATION_NOT_MOVING = 0.001f;

    private final float ACCELEROMETER_VARIANCE_MAX_ROTATION_NOT_MOVING = 0.2f;

    private ArrayList<DevicePositionChangedListener> devicePositionChangedListeners;

    private float[] rotationMatrix = new float[9];

    private long timestampOfLastAnalyses = 0;
    private final int MIN_TIME_BETWEEN_ANALYSES = 1000; //ms

    public Calibrator (ArrayList<DevicePositionChangedListener> devicePositionChangedListeners) {
        this.devicePositionChangedListeners = devicePositionChangedListeners;
    }


    @Override
    public void rollingWindowHasRepresentativelyChanged(float[][][] snapshot3Windows) {
        long currentTimestamp = System.currentTimeMillis();
        if (currentTimestamp - timestampOfLastAnalyses >= MIN_TIME_BETWEEN_ANALYSES) {
            timestampOfLastAnalyses = currentTimestamp;
            CalibratorAnalysisThread calibratorAnalysisThread = new CalibratorAnalysisThread(
                    snapshot3Windows[0], snapshot3Windows[1], snapshot3Windows[2]);
            calibratorAnalysisThread.start();
        }
    }

    @Override
    public void rollingWindowHasCompletelyChanged(float[][][] snapshot3Windows) {

    }

    private class CalibratorAnalysisThread extends Thread {

        private float[][] arrayAccelerometerValues;
        private float[][] arrayGyroscopeValues;
        private float[][] arrayMagnetometerValues;

        public CalibratorAnalysisThread (float[][] arrayAccelerometerValues,
                                         float[][] arrayGyroscopeValues,
                                         float[][] arrayMagnetometerValues) {
            this.arrayAccelerometerValues = arrayAccelerometerValues;
            this.arrayGyroscopeValues = arrayGyroscopeValues;
            this.arrayMagnetometerValues = arrayMagnetometerValues;
        }

        @Override
        public void run() {
            if (arrayAccelerometerValues.length != 0
                    && arrayGyroscopeValues.length != 0
                    && arrayMagnetometerValues.length != 0) {
                if (!isDeviceRotating()) {
                    if (!isDeviceAccelerating()) {
                        if(calulateRotationMatrix()) {
                            for (DevicePositionChangedListener devicePositionChangedListener: devicePositionChangedListeners) {
                                devicePositionChangedListener.onDevicePositionChanged(rotationMatrix);
                            }
                        }
                    }
                }
            }
        }

        private boolean isDeviceRotating() {
            boolean isRotating = true;
            float[] gyroscopeMeans = getMeansVector3(arrayGyroscopeValues);
            if (isVector3InRange(gyroscopeMeans, GYROSCOPE_MEAN_MAX_ROTATION_NOT_MOVING)) {
                float[] gyroscopeVariances = getVariancesVector3(gyroscopeMeans, arrayGyroscopeValues);
                if (isVector3InRange(gyroscopeVariances, GYROSCOPE_VARIANCE_MAX_ROTATION_NOT_MOVING)) {
                    isRotating = false;
                }
            }
            return isRotating;
        }

        // Mean not compared because we cannot know the range in which they will be moving
        private boolean isDeviceAccelerating() {
            boolean isAccelerating = true;
            float[] accelerometerMeans = getMeansVector3(arrayAccelerometerValues);
            float[] accelerometerVariances = getVariancesVector3(accelerometerMeans, arrayAccelerometerValues);
            if (isVector3InRange(accelerometerVariances, ACCELEROMETER_VARIANCE_MAX_ROTATION_NOT_MOVING)) {
                isAccelerating = false;
            }
            return isAccelerating;
        }

        private float[] getMeansVector3(float[][] arrayVector3Values) {
            float xSum = 0.0f;
            float ySum = 0.0f;
            float zSum = 0.0f;
            for(float[] vector3Reading: arrayVector3Values) {
                xSum += vector3Reading[0];
                ySum += vector3Reading[1];
                zSum += vector3Reading[2];
            }
            float[] meansVector3 = new float[3];
            meansVector3[0] = xSum/arrayVector3Values.length;
            meansVector3[1] = ySum/arrayVector3Values.length;
            meansVector3[2] = zSum/arrayVector3Values.length;
            return meansVector3;
        }

        private float[] getVariancesVector3(float[] meansVector3,
                                            float[][] arrayVector3Values) {
            float xiMinusMeanXTwoSquaredSum = 0.0f;
            float yiMinusMeanYTwoSquaredSum = 0.0f;
            float ziMinusMeanZTwoSquaredSum = 0.0f;
            for(float[] vector3Reading: arrayVector3Values) {
                xiMinusMeanXTwoSquaredSum +=
                        Math.pow(vector3Reading[0] - meansVector3[0], 2);
                yiMinusMeanYTwoSquaredSum +=
                        Math.pow(vector3Reading[1] - meansVector3[1], 2);
                ziMinusMeanZTwoSquaredSum +=
                        Math.pow(vector3Reading[2] - meansVector3[2], 2);
            }
            float[] variancesVector3 = new float[3];
            variancesVector3[0] = xiMinusMeanXTwoSquaredSum/arrayVector3Values.length;
            variancesVector3[1] = yiMinusMeanYTwoSquaredSum/arrayVector3Values.length;
            variancesVector3[2] = ziMinusMeanZTwoSquaredSum/arrayVector3Values.length;
            return variancesVector3;
        }


        private boolean isVector3InRange (float[] vector3, float range) {
            boolean isInRange = false;
            if (vector3[0] < range
                    && vector3[0] > -range) {
                if (vector3[1] < range
                        && vector3[1] > -range) {
                    if (vector3[2] < range
                            && vector3[2] > -range) {
                        isInRange = true;
                    }
                }
            }
            return isInRange;
        }

        private boolean calulateRotationMatrix() {
            float[] accelerometerMeans = getMeansVector3(arrayAccelerometerValues);
            float[] magnetometerMeans = getMeansVector3(arrayMagnetometerValues);
            return SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerMeans, magnetometerMeans);
        }
    }
}
