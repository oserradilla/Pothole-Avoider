package rescuedroneapp.rescuedrone.labs.mediatek.plotandroidsensors;

/**
 * Created by oscar on 01/04/2016.
 */
public class AICurves extends Thread {

    private float[][][] realWorldCalculusWindow;

    public AICurves(float[][][] realWorldCalculusWindow) {
        this.realWorldCalculusWindow = realWorldCalculusWindow;
    }
}
