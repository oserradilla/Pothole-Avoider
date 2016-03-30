package rescuedroneapp.rescuedrone.labs.mediatek.plotandroidsensors;

/**
 * Created by oscar on 30/03/2016.
 */
public class DataToBeAnalysedByAI {

    private float[][][] firstWindowPieceAccelGyroMagnetoRealWorldWindows = null;
    private float[][][] secondWindowPieceAccelGyroMagnetoRealWorldWindows = null;
    private int[] firstWindowPieceSpeedWindow = null;
    private int[] secondWindowPieceSpeedWindow = null;
    private float[][] firstWindowRealWorldCalculus = null;
    private float[][] secondWindowRealWorldCalculus = null;

    public float[][] getSecondWindowRealWorldCalculus() {
        return secondWindowRealWorldCalculus;
    }

    public void setSecondWindowRealWorldCalculus(float[][] secondWindowRealWorldCalculus) {
        this.secondWindowRealWorldCalculus = secondWindowRealWorldCalculus;
    }

    public float[][][] getFirstWindowPieceAccelGyroMagnetoRealWorldWindows() {
        return firstWindowPieceAccelGyroMagnetoRealWorldWindows;
    }

    public void setFirstWindowPieceAccelGyroMagnetoRealWorldWindows(float[][][] firstWindowPieceAccelGyroMagnetoRealWorldWindows) {
        this.firstWindowPieceAccelGyroMagnetoRealWorldWindows = firstWindowPieceAccelGyroMagnetoRealWorldWindows;
    }

    public float[][][] getSecondWindowPieceAccelGyroMagnetoRealWorldWindows() {
        return secondWindowPieceAccelGyroMagnetoRealWorldWindows;
    }

    public void setSecondWindowPieceAccelGyroMagnetoRealWorldWindows(float[][][] secondWindowPieceAccelGyroMagnetoRealWorldWindows) {
        this.secondWindowPieceAccelGyroMagnetoRealWorldWindows = secondWindowPieceAccelGyroMagnetoRealWorldWindows;
    }

    public int[] getFirstWindowPieceSpeedWindow() {
        return firstWindowPieceSpeedWindow;
    }

    public void setFirstWindowPieceSpeedWindow(int[] firstWindowPieceSpeedWindow) {
        this.firstWindowPieceSpeedWindow = firstWindowPieceSpeedWindow;
    }

    public int[] getSecondWindowPieceSpeedWindow() {
        return secondWindowPieceSpeedWindow;
    }

    public void setSecondWindowPieceSpeedWindow(int[] secondWindowPieceSpeedWindow) {
        this.secondWindowPieceSpeedWindow = secondWindowPieceSpeedWindow;
    }

    public float[][] getFirstWindowRealWorldCalculus() {
        return firstWindowRealWorldCalculus;
    }

    public void setFirstWindowRealWorldCalculus(float[][] firstWindowRealWorldCalculus) {
        this.firstWindowRealWorldCalculus = firstWindowRealWorldCalculus;
    }
}
