package recurrentneuralnetwork.elman;

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

    public void openNewFile(String fileName) {
        File newTestFile;
        int idTest = 1;
        do {
            newFileName = fileName + String.valueOf(idTest++) + ".txt";
            newTestFile = new File(newFileName);
        } while(newTestFile.exists());
        newTestFile.setReadable(true);
        newTestFile.getParentFile().mkdirs();
        try {
            fileOutputStream = new FileOutputStream(newTestFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    public void setInt(int number) {
        if (!isStartOfLine) {
            newLineToSaveToFile += ",";
        } else {
            isStartOfLine = false;
        }
        newLineToSaveToFile += String.valueOf(number);
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
        newLineToSaveToFile += "\n";
        try {
            fileOutputStream.write(newLineToSaveToFile.getBytes());
            newLineToSaveToFile = "";
            isStartOfLine = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeFile() {
        try {
            if (fileOutputStream != null) {
                fileOutputStream.flush();
                fileOutputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


	public void setLine(String line) {
		newLineToSaveToFile += line;
		newLineToSaveToFile += "\n";
        try {
            fileOutputStream.write(newLineToSaveToFile.getBytes());
            newLineToSaveToFile = "";
            isStartOfLine = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
}