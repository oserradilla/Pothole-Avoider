package rescuedroneapp.rescuedrone.labs.mediatek.plotandroidsensors;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by oscar on 20/04/2016.
 */
public class CurrentIncidenceState implements IncidenceDetectedListener {

    private AtomicBoolean curveDetected = new AtomicBoolean(false);

    @Override
    public boolean hasCurveBeenDetected() {
        return curveDetected.get();
    }

    public void curveDetected(boolean isCurveDetected) {
        curveDetected.set(isCurveDetected);
    }
}
