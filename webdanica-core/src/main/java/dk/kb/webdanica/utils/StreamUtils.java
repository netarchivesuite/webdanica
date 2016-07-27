package dk.kb.webdanica.utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class StreamUtils {
	public static void writeline(FileOutputStream ftest, String txt) throws FileNotFoundException, IOException {
		byte[] contentInBytes = txt.getBytes();
		ftest.write(contentInBytes);
		ftest.write("\n".getBytes());
		ftest.flush();
	}
}
