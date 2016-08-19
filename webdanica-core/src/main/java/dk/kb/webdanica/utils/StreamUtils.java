package dk.kb.webdanica.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

public class StreamUtils {
	public static void writeline(FileOutputStream ftest, String txt) throws FileNotFoundException, IOException {
		byte[] contentInBytes = txt.getBytes();
		ftest.write(contentInBytes);
		ftest.write("\n".getBytes());
		ftest.flush();
	}
	
	public static BufferedReader getBufferedReader(File ingestFile) throws IOException {
		BufferedReader br = null;
		if (isGzippedFile(ingestFile)) {
			br = new BufferedReader(new InputStreamReader(
			        new GZIPInputStream(new FileInputStream(ingestFile))));
		} else {
			 br = new BufferedReader(new FileReader(ingestFile)); 
		}
		
	    return br;
    }

	private static boolean isGzippedFile(File ingestFile) {
	    return ingestFile.getName().endsWith(".gz"); 
    }

	
	
	
}
