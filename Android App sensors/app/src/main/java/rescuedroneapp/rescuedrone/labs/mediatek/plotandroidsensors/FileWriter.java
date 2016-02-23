package rescuedroneapp.rescuedrone.labs.mediatek.plotandroidsensors;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by oscar on 23/02/2016.
 */
public class FileWriter {
    private boolean isStartOfLine = true;
    private String stringToSave = "";

    private Context context;

    public FileWriter(Context context) {
        this.context = context;
    }

    public void restart() {
        stringToSave = "";
        isStartOfLine = true;
    }

    public void setFloat(float number) {
        if (!isStartOfLine) {
            stringToSave += ",";
        } else {
            isStartOfLine = false;
        }
        stringToSave += String.valueOf(number);
    }

    public void nextLine() {
        stringToSave += "\n";
        isStartOfLine = true;
    }

    public void writeToFile() {
        try {
            String baseFolder;
// check if external storage is available
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                baseFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getCanonicalPath();
            }
// revert to using internal storage (not sure if there's an equivalent to the above)
            else {
                baseFolder = context.getFilesDir().getAbsolutePath();
            }
            String newFileName = "";
            File newTestFile;
            int idTest = 1;
            do {
                newFileName = "test" + String.valueOf(idTest++) + ".txt";
                newTestFile = new File(baseFolder + File.separator + "sensors"
                        + File.separator + newFileName);
            } while(newTestFile.exists());
            newTestFile.setReadable(true);
            newTestFile.getParentFile().mkdirs();
            FileOutputStream fos = new FileOutputStream(newTestFile);
            fos.write(stringToSave.getBytes());
            fos.flush();
            fos.close();
            Toast.makeText(context, "File " + newFileName + " created correctly", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "File error writing at file", Toast.LENGTH_LONG);
        }
    }
}