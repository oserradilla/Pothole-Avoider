package rescuedroneapp.rescuedrone.labs.mediatek.plotandroidsensors;

/**
 * Created by oscar on 02/03/2016.
 */
public interface RollingWindowChangesListener {
    void newRollingWindowRawData(float[][][] snapshotOfAccelGyroMagnetoInRawWindows);
    void newRollingWindowTransformedToRealWorld(float[][][] snapshotAccelGyroMagnetoRealWorldWindows);

    /* Matrix format:
                        AccelX|AccelY|AccelZ|GyroX|GyroY|GyroZ|MagnetoX|MagnetoY|MagnetoZ
            mean
            variance
            svm
            */
    void newRollingWindowDeviceWorldCalculus(float[][] calculusMatrix);
    void newRollingWindowRealWorldCalculus(float[][] calculusMatrix);
}
