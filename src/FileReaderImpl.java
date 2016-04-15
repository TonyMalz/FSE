

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * The implementation of a file reader 
 * @author Johannes Gareis
 * 
 */
public class FileReaderImpl {

	public FileReaderImpl() {

	}

	/**
	 * Function to read a file into an array of strings, each line representing
	 * one entry in the array
	 * 
	 * @param path
	 *            the path pointing to the file to be read in
	 * @return an string array with a string for each line
	 */
	public ArrayList<String> readFileToStringList(String path) {
		ArrayList<String> nodesAsStrings = new ArrayList<String>();
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		try {
			fileReader = new FileReader(path);
			bufferedReader = new BufferedReader(fileReader);

			String line;
			while ((line = bufferedReader.readLine()) != null) {
				nodesAsStrings.add(line);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fileReader != null) {
				try {
					fileReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return nodesAsStrings;
	}

}
