package recurrentneuralnetwork.elman;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by oscar on 23/02/2016.
 */
public class FileWriter {
    private boolean isStartOfLine = true;
    private String newLineToSaveToFile = "";

    private String newFileName = "";
    private OutputStream fileOutputStream = null;

	private Logger log;
	
	public FileWriter(Logger log) {
		this.log = log;
	}
    
	public static int getTestNumber(String fileName) {
		File newTestFile;
        int idTest = 0;
        String newFileName;
        do {
        	idTest++;
            newFileName = fileName + String.valueOf(idTest) + ".log";
            newTestFile = new File(newFileName);
        } while(newTestFile.exists());
        return idTest;
	}
	
    public void openNewFile(String fileName) {
        File newTestFile;
        newTestFile = new File(fileName+".txt");
        newTestFile.setReadable(true);
        newTestFile.getParentFile().mkdirs();
        try {
            fileOutputStream = new FileOutputStream(newTestFile);
        } catch (FileNotFoundException e) {
        	log.log( Level.SEVERE, e.toString(), e );
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
        	log.log( Level.SEVERE, e.toString(), e );
        }
    }

    public void closeFile() {
        try {
            if (fileOutputStream != null) {
                fileOutputStream.flush();
                fileOutputStream.close();
            }
        } catch (Exception e) {
        	log.log( Level.SEVERE, e.toString(), e );
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
        	log.log( Level.SEVERE, e.toString(), e );
        }
	}
}