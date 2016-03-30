package rescuedroneapp.rescuedrone.labs.mediatek.plotandroidsensors;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import gps.GPSListener;
import gps.SpeedLastValue;
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

    private SpeedLastValue speedLastValue = null;
    private GPSListener gpsListener = null;

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
        speedLastValue = new SpeedLastValue();
        gpsListener = new GPSListener(this, speedLastValue);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensors.startCollectingData();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (started == true) {
            sensors.stopCollectingData();
        }
        if (gpsListener != null) {
            gpsListener.stopGPS();
        }
        super.onDestroy();
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
                ArrayList<DevicePositionChangedListener> devicePositionChangedListeners = new ArrayList<>();
                AppNotifications appNotifications = new AppNotifications(this);
                devicePositionChangedListeners.add(appNotifications);
                Calibrator calibrator = new Calibrator(windowFrequency, devicePositionChangedListeners);
                calibrator.start();
                ArrayList<RollingWindowChangesListener> rollingWindowChangesListenerListeners = new ArrayList<>();
                rollingWindowChangesListenerListeners.add(appNotifications);
                rollingWindowChangesListenerListeners.add(calibrator);
                rollingWindowChangesListenerListeners.add(windowLogger);
                Preprocessing preprocessing = new Preprocessing(rollingWindowChangesListenerListeners);
                rollingWindowChangesListenerListeners.add(preprocessing);
                rollingWindow = new RollingWindow(sensors, speedLastValue, sampleFrequency,windowFrequency,hasRepresentativelyChanged, rollingWindowChangesListenerListeners);
                RealWorldTransformation realWorldTransformation = new RealWorldTransformation(rollingWindowChangesListenerListeners);
                rollingWindowChangesListenerListeners.add(realWorldTransformation);
                devicePositionChangedListeners.add(realWorldTransformation);
                GatherDataForAI gatherDataForAI = new GatherDataForAI();
                rollingWindowChangesListenerListeners.add(gatherDataForAI);
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