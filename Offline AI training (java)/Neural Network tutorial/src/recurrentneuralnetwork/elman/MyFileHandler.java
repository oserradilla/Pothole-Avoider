package recurrentneuralnetwork.elman;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MyFileHandler {
	
	private Logger log;
	
	public MyFileHandler(Logger log) {
		this.log = log;
	}
	
	public float[][] readAllInputFromDirectory(String dirName, int[] inputDataLengthVector, int numColumns) throws IOException {
		int linesOfFiles = getNumLinesFromFilesOfFolder(dirName);
		float[][] inputFromCsvFile = new float[linesOfFiles][numColumns];
		int inputFromCsvFileIdx = 0;
		final File folder = new File(dirName);
		for (final File fileEntry : folder.listFiles()) {
	        if (fileEntry.isFile()) {
	        	inputFromCsvFileIdx = readInputsFromCsvFile(fileEntry.getAbsolutePath(),
	        			numColumns, inputFromCsvFile, inputFromCsvFileIdx);
	        }
	    }
		inputDataLengthVector[0] = inputFromCsvFileIdx;
		return inputFromCsvFile;
	}
	
	private int getNumLinesFromFilesOfFolder(String dirName) throws IOException {
		final File folder = new File(dirName);
		int numLines = 0;
	    for (final File fileEntry : folder.listFiles()) {
	        if (fileEntry.isFile()) {
	        	numLines += countNumLinesOfFile(fileEntry.getAbsolutePath());
	        }
	    }
	    return numLines;
	}
	
	private int readInputsFromCsvFile(String csvFile, int numColumns, 
			float[][] fileBuffer, int startPosition) {
	
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		int endPosition = startPosition;
		try {

			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {

			        // use comma as separator
				String[] splittedLine = line.split(cvsSplitBy);
				if(splittedLine.length == numColumns) {
					for(int i=0; i<numColumns; i++) {
						fileBuffer[endPosition][i] = Float.valueOf(splittedLine[i]);
					}
					endPosition++;
				}
			}

		} catch (FileNotFoundException e) {
			log.log( Level.SEVERE, e.toString(), e );
		} catch (IOException e) {
			log.log( Level.SEVERE, e.toString(), e );
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					log.log( Level.SEVERE, e.toString(), e );
				}
			}
		}
		return endPosition;
	}
	
	private int countNumLinesOfFile(String fileName) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		int lines = 0;
		while (reader.readLine() != null) lines++;
		reader.close();
		return lines;
	}

}
