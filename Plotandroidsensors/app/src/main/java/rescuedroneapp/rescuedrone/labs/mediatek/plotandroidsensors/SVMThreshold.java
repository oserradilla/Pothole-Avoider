package rescuedroneapp.rescuedrone.labs.mediatek.plotandroidsensors;

/**
 * Created by oscar on 21/03/2016.
 */
public class SVMThreshold implements RollingWindowChangesListener {

    @Override
    public void newRollingWindowRealWorldCalculus(float[][] calculusMatrix) {
        if (calculusMatrix[2][0] > 6 && calculusMatrix[2][3] < 0.5) {

        }
    }

    @Override
    public void newRollingWindowRawData(float[][][] snapshotOfAccelGyroMagnetoInRawWindows) {}

    @Override
    public void newRollingWindowTransformedToRealWorld(float[][][] snapshotAccelGyroMagnetoRealWorldWindows) {}

    @Override
    public void newRollingWindowDeviceWorldCalculus(float[][] calculusMatrix) {}
}
