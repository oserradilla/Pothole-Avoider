package com.oscarsc.potholeavoider.artificial_intelligence;

import android.content.Context;

import java.util.ArrayList;

import com.oscarsc.potholeavoider.gps.PreciseGPSListener;
import com.oscarsc.potholeavoider.gps.SpeedLastValue;
import com.oscarsc.potholeavoider.incidences.Incidence;
import com.oscarsc.potholeavoider.listeners.GpsListener;
import com.oscarsc.potholeavoider.logger.WindowLoggerListener;
import com.oscarsc.potholeavoider.notifications.AppNotifications;
import com.oscarsc.potholeavoider.services.IncidenceCommunicator;
import com.oscarsc.potholeavoider.services.IncidenceReporter;

public class ArtificialIntelligence extends Thread{

    private boolean started = false;
    private ArrayList<AccelData> sensorData;

    private Sensors sensors;
    private RollingWindow rollingWindow;
    private WindowLoggerListener windowLogger;

    private SpeedLastValue speedLastValue = null;
    private PreciseGPSListener gpsListener = null;

    private Context context;
    private IncidenceReporter incidenceReporter;

    public ArtificialIntelligence(Context context) {
        this.context = context;

        sensorData = new ArrayList();
        sensors = new Sensors(context);
        speedLastValue = new SpeedLastValue();
        gpsListener = new PreciseGPSListener(context, speedLastValue);
        incidenceReporter=new IncidenceReporter();
    }

    public void startCollecting() {
        int windowFrequency = 1500;
        int sampleFrequency = sensors.getMinSampleFrequencyMilliseconds();
        sensorData = new ArrayList();
        sensors.startCollectingData();

        windowLogger = new WindowLoggerListener(context);
        ArrayList<DevicePositionChangedListener> devicePositionChangedListeners = new ArrayList<>();
        AppNotifications appNotifications = new AppNotifications(context);
        devicePositionChangedListeners.add(appNotifications);
        Calibrator calibrator = new Calibrator(windowFrequency, devicePositionChangedListeners);
        calibrator.start();
        ArrayList<RollingWindowChangesListener> rollingWindowChangesListenerListeners = new ArrayList<>();
        rollingWindowChangesListenerListeners.add(appNotifications);
        rollingWindowChangesListenerListeners.add(calibrator);
        rollingWindowChangesListenerListeners.add(windowLogger);
        Preprocessing preprocessing = new Preprocessing(rollingWindowChangesListenerListeners);
        rollingWindowChangesListenerListeners.add(preprocessing);
        rollingWindow = new RollingWindow(sensors, speedLastValue, sampleFrequency,windowFrequency, rollingWindowChangesListenerListeners);
        RealWorldTransformation realWorldTransformation = new RealWorldTransformation(rollingWindowChangesListenerListeners);
        rollingWindowChangesListenerListeners.add(realWorldTransformation);
        devicePositionChangedListeners.add(realWorldTransformation);
        GatherDataForAI gatherDataForAI = new GatherDataForAI(windowFrequency, incidenceReporter);
        rollingWindowChangesListenerListeners.add(gatherDataForAI);
        started = true;
    }

    public void stopCollecting() {
        started = false;
        sensors.stopCollectingData();
        windowLogger.endLogging();
    }

    @Override
    public void run() {
        try {
            GpsListener.waitLocationNotNull();
            startCollecting();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void onDestroy() {
        if (started == true) {
            sensors.stopCollectingData();
        }
        if (gpsListener != null) {
            gpsListener.stopGPS();
        }
    }
}