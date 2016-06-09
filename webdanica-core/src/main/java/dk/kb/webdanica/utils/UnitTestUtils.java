package dk.kb.webdanica.utils;

import java.io.File;
import java.net.URL;

public class UnitTestUtils {
	 
	/**
	 * Find a test resource for a given path.
	 * @param path the path relative to resources directory eg. <project>/src/test/resources/mypackage/YourFile.csv
	 * @return the file corresponding to the given path 
	 */
	public static File getTestResourceFile(String path) {
		URL url = Thread.currentThread().getContextClassLoader().getResource(path);
		File file = new File(url.getPath());
		return file;
	}
}
