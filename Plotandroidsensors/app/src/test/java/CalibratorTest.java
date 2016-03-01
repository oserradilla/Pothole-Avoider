import org.junit.Test;

import java.util.ArrayList;

import rescuedroneapp.rescuedrone.labs.mediatek.plotandroidsensors.Calibrator;
import rescuedroneapp.rescuedrone.labs.mediatek.plotandroidsensors.DevicePositionChangedListener;

/**
 * Created by oscar on 01/03/2016.
 */

public class CalibratorTest implements DevicePositionChangedListener {

    float[] rotationMatrix = null;

    // Must plot the following error:
    // Exception in thread "Thread-0" java.lang.RuntimeException: Method getRotationMatrix in android.hardware.SensorManager not mocked.
    @Test
    public void testCalibrationWhenNotMoving() throws InterruptedException {
        ArrayList<float[]> accelerometerList = new ArrayList<>();
        ArrayList<float[]> gyroscopeList = new ArrayList<>();
        ArrayList<float[]> magnetometerList = new ArrayList<>();

        accelerometerList.add(new float[]{-2.0286255f,3.1937256f,9.036133f});
        accelerometerList.add(new float[]{-2.03154f,3.173584f,9.054291f});
        accelerometerList.add(new float[]{-2.0280762f,3.1838531f,9.077698f});

        gyroscopeList.add(new float[]{-0.0010986328f,-0.0048828125f,-0.0048065186f});
        gyroscopeList.add(new float[]{-0.00033569336f,-0.0038604736f,-0.0052948f});
        gyroscopeList.add(new float[]{-0.00033569336f,-0.0051879883f,-0.0052337646f});

        magnetometerList.add(new float[]{-29.580688f,-21.691895f,-43.933105f});
        magnetometerList.add(new float[]{-29.0802f, -20.851135f, -44.421387f});
        magnetometerList.add(new float[]{-28.746033f, -22.19696f, -43.118286f});

        Calibrator calibrator = new Calibrator(accelerometerList, gyroscopeList, magnetometerList, this);
        calibrator.start();
        Thread.sleep(500);
    }

    @Override
    public void onDevicePositionChanged(float[] rotationMatrix) {
        this.rotationMatrix = rotationMatrix.clone();
    }
}
