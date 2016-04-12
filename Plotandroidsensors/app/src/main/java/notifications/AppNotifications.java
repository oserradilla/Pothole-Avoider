package notifications;

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
    private static ToneGenerator incidenceToneGenerator;

    public AppNotifications(Context context) {
        this.context = context;
        isBeeping = false;
        incidenceToneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM, 60);
    }

    @Override
    public void newRollingWindowRealWorldCalculus(float[][] calculusMatrix) {

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

    public static void beep() {
        incidenceToneGenerator.startTone(ToneGenerator.TONE_DTMF_0, 1000);
    }


    @Override
    public void newRollingWindowRawData(float[][][] snapshotOfAccelGyroMagnetoInRawWindows, int[] snapshotOfSpeedWindow) {

    }

    @Override
    public void newRollingWindowTransformedToRealWorld(float[][][] snapshotAccelGyroMagnetoRealWorldWindows, int[] snapshotOfSpeedWindow) {

    }

    @Override
    public void newRollingWindowDeviceWorldCalculus(float[][] calculusMatrix) {

    }
}
