package logger;

import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.net.Uri;

import rescuedroneapp.rescuedrone.labs.mediatek.plotandroidsensors.DevicePositionChangedListener;
import rescuedroneapp.rescuedrone.labs.mediatek.plotandroidsensors.RollingWindowChangesListener;

/**
 * Created by oscar on 18/03/2016.
 */
public class AppNotifications implements RollingWindowChangesListener, DevicePositionChangedListener {

    private Context context;
    private boolean isBeeping;
    private ToneGenerator incidenceToneGenerator;

    public AppNotifications(Context context) {
        this.context = context;
        isBeeping = false;
        incidenceToneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM, 40);
    }

    @Override
    public void newRollingWindowRealWorldCalculus(float[][] calculusMatrix) {
        if (calculusMatrix[2][0] > 6 && calculusMatrix[2][3] < 0.5) {
            if (!isBeeping) {
                incidenceToneGenerator.startTone(ToneGenerator.TONE_DTMF_0, 1000000);
                isBeeping = true;
            }
        } else {
            if (isBeeping) {
                incidenceToneGenerator.stopTone();
                isBeeping = false;
            }
        }
    }

    @Override
    public void onDevicePositionChanged(float[] rotationMatrix) {
        newUserNotification();
    }


    private void newUserNotification() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(context, notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    public void newRollingWindowRawData(float[][][] snapshotOfAccelGyroMagnetoInRawWindows) {

    }

    @Override
    public void newRollingWindowTransformedToRealWorld(float[][][] snapshotAccelGyroMagnetoRealWorldWindows) {

    }

    @Override
    public void newRollingWindowDeviceWorldCalculus(float[][] calculusMatrix) {

    }
}
