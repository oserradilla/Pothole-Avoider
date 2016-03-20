package rescuedroneapp.rescuedrone.labs.mediatek.plotandroidsensors;

import java.util.ArrayList;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import logger.AppNotifications;
import logger.WindowLoggerListener;

public class MainActivity extends Activity implements
        OnClickListener {

    private float Xaxis;
    private float Yaxis;
    private float Zaxis;

    private float[] measure = new float[3];
    private float[] rMatrix = new float[9];

    private Button btnStart, btnStop, btnUpload;
    private boolean started = false;
    private ArrayList<AccelData> sensorData;
    private LinearLayout layout;
    private View mChart;

    private Sensors sensors;
    private RollingWindow rollingWindow;
    private WindowLoggerListener windowLogger;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        layout = (LinearLayout) findViewById(R.id.chart_container);

        sensorData = new ArrayList();

        sensors = new Sensors(this);

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
        sensors.startCollectingData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (started == true) {
            sensors.stopCollectingData();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensors.stopCollectingData();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStart:
                int windowFrequency = 2000;
                int sampleFrequency = 5;
                int hasRepresentativelyChanged = 100;
                btnStart.setEnabled(false);
                btnStop.setEnabled(true);
                btnUpload.setEnabled(false);
                sensorData = new ArrayList();
                sensors.startCollectingData();
                windowLogger = new WindowLoggerListener(this);
                AI ai = new AI(sampleFrequency,windowFrequency,hasRepresentativelyChanged);
                ArrayList<DevicePositionChangedListener> devicePositionChangedListeners = new ArrayList<>();
                AppNotifications appNotifications = new AppNotifications(this);
                devicePositionChangedListeners.add(appNotifications);
                Calibrator calibrator = new Calibrator(windowFrequency, devicePositionChangedListeners);
                calibrator.start();
                ArrayList<RollingWindowChangesListener> rollingWindowChangesListenerListeners = new ArrayList<>();
                rollingWindowChangesListenerListeners.add(calibrator);
                rollingWindowChangesListenerListeners.add(windowLogger);
                Preprocessing preprocessing = new Preprocessing(rollingWindowChangesListenerListeners);
                rollingWindowChangesListenerListeners.add(preprocessing);
                rollingWindow = new RollingWindow(sensors,sampleFrequency,windowFrequency,hasRepresentativelyChanged, rollingWindowChangesListenerListeners);
                RealWorldTransformation realWorldTransformation = new RealWorldTransformation(rollingWindowChangesListenerListeners);
                rollingWindowChangesListenerListeners.add(realWorldTransformation);
                devicePositionChangedListeners.add(realWorldTransformation);
                // save prev data if available
                started = true;
                break;
            case R.id.btnStop:
                btnStart.setEnabled(true);
                btnStop.setEnabled(false);
                btnUpload.setEnabled(true);
                started = false;
                sensors.stopCollectingData();
                windowLogger.endLogging();
                /*layout.removeAllViews();
                openChart();*/
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

    public void showToast(final String toast)
    {
        runOnUiThread(new Runnable() {
            public void run()
            {
                Toast.makeText(MainActivity.this, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }
}