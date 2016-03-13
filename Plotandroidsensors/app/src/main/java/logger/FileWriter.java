package logger;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by oscar on 23/02/2016.
 */
public class FileWriter {
    private boolean isStartOfLine = true;
    private String newLineToSaveToFile = "";

    private String newFileName = "";
    private OutputStream fileOutputStream = null;

    private Context context;
    private int lines = 0;

    public FileWriter(Context context) {
        this.context = context;
    }

    public void openNewFile() {
        String baseFolder = "";
        // check if external storage is available
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            try {
                baseFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getCanonicalPath();
            } catch (IOException e) {
                Toast.makeText(context, "Error creating file", Toast.LENGTH_LONG).show();
            }
        }
        // revert to using internal storage (not sure if there's an equivalent to the above)
        else {
            baseFolder = context.getFilesDir().getAbsolutePath();
        }
        File newTestFile;
        int idTest = 1;
        do {
            newFileName = "test" + String.valueOf(idTest++) + ".txt";
            newTestFile = new File(baseFolder + File.separator + "sensors"
                    + File.separator + newFileName);
        } while(newTestFile.exists());
        newTestFile.setReadable(true);
        newTestFile.getParentFile().mkdirs();
        try {
            fileOutputStream = new FileOutputStream(newTestFile);
        } catch (FileNotFoundException e) {
            Toast.makeText(context, "File not found", Toast.LENGTH_LONG).show();
        }
    }

    public void setFloat(float number) {
        if (!isStartOfLine) {
            newLineToSaveToFile += ",";
        } else {
            isStartOfLine = false;
        }
        newLineToSaveToFile += String.valueOf(number);
    }

    public void nextLine() {
        lines++;
        newLineToSaveToFile += "\n";
        try {
            fileOutputStream.write(newLineToSaveToFile.getBytes());
            newLineToSaveToFile = "";
            isStartOfLine = true;
        } catch (IOException e) {
            Toast.makeText(context, "Error writing to file", Toast.LENGTH_LONG).show();
        }
    }

    public void closeFile() {
        try {
            if (fileOutputStream != null) {
                fileOutputStream.flush();
                fileOutputStream.close();
                //Toast.makeText(context, "Test saved at " + newFileName , Toast.LENGTH_LONG).show();
                Toast.makeText(context, "Number of lines sent to outputstream: " + String.valueOf(lines) , Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error closing file", Toast.LENGTH_LONG).show();
        }
    }
}