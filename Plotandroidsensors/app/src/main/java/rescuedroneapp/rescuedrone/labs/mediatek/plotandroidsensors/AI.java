package rescuedroneapp.rescuedrone.labs.mediatek.plotandroidsensors;

import android.util.Log;

/**
 * Created by oscar on 02/03/2016.
 */
public class AI implements RollingWindowChanges{

    @Override
    public void rollingWindowHasRepresentativelyChanged(float[][][] snapshot3Windows) {
        Log.v("copied","copied");
    }
}
