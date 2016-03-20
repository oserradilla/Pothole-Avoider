package logger;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import rescuedroneapp.rescuedrone.labs.mediatek.plotandroidsensors.DevicePositionChangedListener;

/**
 * Created by oscar on 18/03/2016.
 */
public class AppNotifications implements DevicePositionChangedListener {

    private Context context;

    public AppNotifications(Context context) {
        this.context = context;
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
}
