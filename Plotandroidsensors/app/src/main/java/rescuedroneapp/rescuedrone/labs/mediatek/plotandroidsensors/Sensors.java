package rescuedroneapp.rescuedrone.labs.mediatek.plotandroidsensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by oscar on 02/03/2016.
 */
public class Sensors implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private Sensor magnetometer;

    private float[] lastAccelerometerData;
    private float[] lastGyroscopeData;
    private float[] lastMagnetometerData;

    private Lock lastAccelerometerDataLock = new ReentrantLock();
    private Lock lastGyroscopeDataLock = new ReentrantLock();
    private Lock lastMagnetometerDataLock = new ReentrantLock();

    private boolean collecting = false;

    public Sensors(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        //magnetometer.getMinDelay();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            lastAccelerometerDataLock.lock();
            lastAccelerometerData = event.values.clone();
            lastAccelerometerDataLock.unlock();
        }
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            lastGyroscopeDataLock.lock();
            lastGyroscopeData = event.values.clone();
            lastGyroscopeDataLock.unlock();
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            lastMagnetometerDataLock.lock();
            lastMagnetometerData = event.values.clone();
            lastMagnetometerDataLock.unlock();
        }
    }

    public void startCollectingData() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_FASTEST);
        collecting = true;
    }

    public void stopCollectingData() {
        collecting = false;
        sensorManager.unregisterListener(this);
    }

    public float[] getLastAccelerometerData() {
        lastAccelerometerDataLock.lock();
        float[] accelerometerData = lastAccelerometerData.clone();
        lastAccelerometerDataLock.unlock();
        return accelerometerData;
    }

    public float[] getLastGyroscopeData() {
        lastGyroscopeDataLock.lock();
        float[] gyroscopeData = lastGyroscopeData.clone();
        lastGyroscopeDataLock.unlock();
        return gyroscopeData;
    }

    public float[] getLastMagnetometerData() {
        lastMagnetometerDataLock.lock();
        float[] magnetometerData = lastMagnetometerData.clone();
        lastMagnetometerDataLock.unlock();
        return magnetometerData;
    }

    public boolean areCollecting(){
        return collecting;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
