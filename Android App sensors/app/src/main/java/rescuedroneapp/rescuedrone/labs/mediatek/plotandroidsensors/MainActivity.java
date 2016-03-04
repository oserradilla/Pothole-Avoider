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

public class MainActivity extends Activity implements SensorEventListener,
        OnClickListener {
    private SensorManager sm;
    private float Xaxis;
    private float Yaxis;
    private float Zaxis;
    private Sensor asm;
    private Sensor gsm;
    private float[] measure = new float[3];
    private float[] rMatrix = new float[9];

    private Button btnStart, btnStop, btnUpload;
    private boolean started = false;
    private ArrayList<AccelData> sensorData;
    private LinearLayout layout;
    private View mChart;

    private FileWriter fileWriter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        layout = (LinearLayout) findViewById(R.id.chart_container);

        fileWriter = new FileWriter(this);

        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        asm = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gsm = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

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
        sm.registerListener(this, gsm, SensorManager.SENSOR_DELAY_FASTEST);
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
    float[] mGeomagnetic;
    float timestamp = 1;
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (started) {
            Sensor source = event.sensor;

            //  if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {

            //    return;
            //}
            //else {

            if (source.getType() == Sensor.TYPE_ACCELEROMETER) {


                Zaxis = event.values[2];
                Yaxis = event.values[1];
                Xaxis = event.values[0];
                lowFilter();
                Log.v("Accelerometer", String.valueOf(measure[0]) + "-" +
                        String.valueOf(measure[1]) + "-" +
                        String.valueOf(measure[2]));
            } else if (source.getType() == Sensor.TYPE_GYROSCOPE) {

                float conv = 1.0f / 1000000000.0f;
                float err = 0.1f;
                float[] rvector = new float[4];
                if (timestamp != 0) {
                    final float dt = (event.timestamp - timestamp) * conv;

                    Xaxis = event.values[0];
                    Yaxis = event.values[1];
                    Zaxis = event.values[2];

                    float angs = (float) Math.sqrt(Xaxis * Xaxis + Yaxis * Yaxis + Zaxis * Zaxis);

                    if (angs > err) {
                        Xaxis /= angs;
                        Yaxis /= angs;
                        Zaxis /= angs;
                    }
                    float squarezeta = angs * dt;
                    float sinsquarezeta = (float) Math.sin(squarezeta);
                    float cossquarezeta = (float) Math.cos(squarezeta);
                    rvector[0] = sinsquarezeta * Xaxis;
                    rvector[1] = sinsquarezeta * Yaxis;
                    rvector[2] = sinsquarezeta * Zaxis;
                    rvector[3] = cossquarezeta;

                }
                timestamp = event.timestamp;
                SensorManager.getRotationMatrixFromVector(rMatrix, rvector);
                // Log.v("Rotation vector", String.valueOf(rvector[0]) + "-" +
                //       String.valueOf(rvector[1]) + "-" +
                //     String.valueOf(rvector[2]) + "-" +
                //   String.valueOf(rvector[3]));
                /*AccelData data = new AccelData(System.currentTimeMillis(), rvector[0], rvector[1], rvector[2]);
                sensorData.add(data);*/
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStart:
                fileWriter.openNewFile();
                btnStart.setEnabled(false);
                btnStop.setEnabled(true);
                btnUpload.setEnabled(false);
                sensorData = new ArrayList();
                // save prev data if available
                started = true;
                sm.registerListener(this, asm, SensorManager.SENSOR_DELAY_FASTEST);
                sm.registerListener(this, gsm, SensorManager.SENSOR_DELAY_FASTEST);
                break;
            case R.id.btnStop:
                btnStart.setEnabled(true);
                btnStop.setEnabled(false);
                btnUpload.setEnabled(true);
                started = false;
                sm.unregisterListener(this);
                layout.removeAllViews();
                openChart();
                fileWriter.closeFile();
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
//    }

    public void lowFilter() {
        final float alpha = 0.05f;
        float[] gravity = new float[3];

        gravity[0] =  (alpha * gravity[0] + (1 - alpha) * Xaxis);
        gravity[1] =  (alpha * gravity[1] + (1 - alpha) * Yaxis);
        gravity[2] =  (alpha * gravity[2] + (1 - alpha) * Zaxis);

        measure[0] = Xaxis - gravity[0];
        measure[1] = Yaxis - gravity[1];
        measure[2] = Zaxis - gravity[2];
        AccelData data = new AccelData(System.currentTimeMillis(), Xaxis, Yaxis, Zaxis);
        sensorData.add(data);
        fileWriter.setFloat(Xaxis);
        fileWriter.setFloat(Yaxis);
        fileWriter.setFloat(Zaxis);
        fileWriter.nextLine();
    }
}