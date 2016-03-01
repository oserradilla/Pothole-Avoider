package rescuedroneapp.rescuedrone.labs.mediatek.plotandroidsensors;

import java.util.ArrayList;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends Activity implements SensorEventListener,
        OnClickListener {
    private SensorManager sm;
    private float Xaxis;
    private float Yaxis;
    private float Zaxis;
    private Sensor asm;
    private Sensor gyroscope;
    private Sensor magnetometer;
    private float[] measure = new float[3];
    private float[] rMatrix = new float[9];

    private Button btnStart, btnStop, btnUpload;
    private boolean started = false;
    private ArrayList<AccelData> sensorData;
    private LinearLayout layout;
    private View mChart;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        layout = (LinearLayout) findViewById(R.id.chart_container);

        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        asm = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        magnetometer = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        magnetometer.getMinDelay();
        sensorData = new ArrayList();

        btnStart = (Button) findViewById(R.id.btnStart);
        btnStop = (Button) findViewById(R.id.btnStop);
        btnUpload = (Button) findViewById(R.id.btnUpload);
        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        btnUpload.setOnClickListener(this);
        btnStart.setEnabled(true);
        btnStop.setEnabled(false);
        if (sensorData == null || sensorData.size() == 0) {
            btnUpload.setEnabled(false);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        sm.registerListener(this, asm, SensorManager.SENSOR_DELAY_FASTEST);
        sm.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_FASTEST);
        sm.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (started == true) {
            sm.unregisterListener(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        sm.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    float[] mGravity;
    float[] mGyroscope;
    float[] mGeomagnetic;
    float timestamp = 1;

    int ctr = 0;
    boolean rInitialized = false;

    float I[] = new float[9];
    float RotationMatrix[] = new float[9];

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (started) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                mGravity = event.values.clone();
                ctr = ctr+1 >= 100 ? 100 : ctr+1;
            }
            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE)
                mGyroscope = event.values.clone();
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                mGeomagnetic = event.values.clone();
            if (mGravity != null && mGeomagnetic != null && ctr == 100) {
                if (rInitialized) {
                    float transformedVector[] = new float[3];
                    transformedVector[0] = RotationMatrix[0] * mGravity[0] + RotationMatrix[1] * mGravity[1] + RotationMatrix[2] * mGravity[2];
                    transformedVector[1] = RotationMatrix[3] * mGravity[0] + RotationMatrix[4] * mGravity[1] + RotationMatrix[5] * mGravity[2];
                    transformedVector[2] = RotationMatrix[6] * mGravity[0] + RotationMatrix[7] * mGravity[1] + RotationMatrix[8] * mGravity[2];
                    Log.v("Accel", String.valueOf(mGravity[0]) + ","+ String.valueOf(mGravity[1]) + "," + String.valueOf(mGravity[2]));
                    Log.v("Gyro", String.valueOf(mGyroscope[0]) + ","+ String.valueOf(mGyroscope[1]) + "," + String.valueOf(mGyroscope[2]));
                    Log.v("Magne", String.valueOf(mGeomagnetic[0]) + ","+ String.valueOf(mGeomagnetic[1]) + "," + String.valueOf(mGeomagnetic[2]));
                    AccelData data = new AccelData(System.currentTimeMillis(), transformedVector[0],
                            transformedVector[1], transformedVector[2]);
                    sensorData.add(data);
                } else {
                    rInitialized = SensorManager.getRotationMatrix(RotationMatrix, I, mGravity, mGeomagnetic);
                    Toast.makeText(this, "Rotation matrix created", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStart:
                btnStart.setEnabled(false);
                btnStop.setEnabled(true);
                btnUpload.setEnabled(false);
                sensorData = new ArrayList();
                // save prev data if available
                started = true;
                sm.registerListener(this, asm, SensorManager.SENSOR_DELAY_FASTEST);
                sm.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_FASTEST);
                break;
            case R.id.btnStop:
                btnStart.setEnabled(true);
                btnStop.setEnabled(false);
                btnUpload.setEnabled(true);
                started = false;
                sm.unregisterListener(this);
                layout.removeAllViews();
                openChart();

                // show data in chart
                break;
            case R.id.btnUpload:

                break;
            default:
                break;
        }
    }

    private void openChart() {
        if (sensorData != null || sensorData.size() > 0) {
            long t = sensorData.get(0).getTimestamp();
            XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();

            XYSeries xSeries = new XYSeries("X");
            XYSeries ySeries = new XYSeries("Y");
            XYSeries zSeries = new XYSeries("Z");

            for (AccelData data : sensorData) {
                xSeries.add(data.getTimestamp() - t, data.getX());
                ySeries.add(data.getTimestamp() - t, data.getY());
                zSeries.add(data.getTimestamp() - t, data.getZ());
            }

            dataset.addSeries(xSeries);
            dataset.addSeries(ySeries);
            dataset.addSeries(zSeries);

            XYSeriesRenderer xRenderer = new XYSeriesRenderer();
            xRenderer.setColor(Color.RED);
            xRenderer.setPointStyle(PointStyle.CIRCLE);
            xRenderer.setFillPoints(true);
            xRenderer.setLineWidth(1);
            xRenderer.setDisplayChartValues(false);

            XYSeriesRenderer yRenderer = new XYSeriesRenderer();
            yRenderer.setColor(Color.GREEN);
            yRenderer.setPointStyle(PointStyle.CIRCLE);
            yRenderer.setFillPoints(true);
            yRenderer.setLineWidth(1);
            yRenderer.setDisplayChartValues(false);

            XYSeriesRenderer zRenderer = new XYSeriesRenderer();
            zRenderer.setColor(Color.BLUE);
            zRenderer.setPointStyle(PointStyle.CIRCLE);
            zRenderer.setFillPoints(true);
            zRenderer.setLineWidth(1);
            zRenderer.setDisplayChartValues(false);

            XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();
            multiRenderer.setXLabels(0);
            multiRenderer.setLabelsColor(Color.RED);
            multiRenderer.setChartTitle("t vs (x,y,z)");
            multiRenderer.setXTitle("Sensor Data");
            multiRenderer.setYTitle("Values of Acceleration");
            multiRenderer.setZoomButtonsVisible(true);
            for (int i = 0; i < sensorData.size(); i++) {

                multiRenderer.addXTextLabel(i + 1, ""
                        + (sensorData.get(i).getTimestamp() - t));
            }
            for (int i = 0; i < 12; i++) {
                multiRenderer.addYTextLabel(i + 1, ""+i);
            }

            multiRenderer.addSeriesRenderer(xRenderer);
            multiRenderer.addSeriesRenderer(yRenderer);
            multiRenderer.addSeriesRenderer(zRenderer);

            // Getting a reference to LinearLayout of the MainActivity Layout

            // Creating a Line Chart
            mChart = ChartFactory.getLineChartView(getBaseContext(), dataset,
                    multiRenderer);

            // Adding the Line Chart to the LinearLayout
            layout.addView(mChart);

        }
    }
}