package dk.kb.webdanica.core.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.io.FileUtils;

public class UnitTestUtils {
	 
	/**
	 * Find a test resource for a given path.
	 * @param path the path relative to resources directory eg. path is mypackage/YourFile.csv if file is <project>/src/test/resources/mypackage/YourFile.csv
	 * @return the file corresponding to the given path or null if not found 
	 */
	public static File getTestResourceFile(String path) {
		URL url = Thread.currentThread().getContextClassLoader().getResource(path);
		if (url != null) {
			File file = new File(url.getPath());
			return file;
		} 
		return null;
	}
	
	public static String getTestResourceFileAsString(String path) throws IOException {
		File file = getTestResourceFile(path);
		if (file != null && file.isFile()) {
			return FileUtils.readFileToString(file);
		} else {
			return null;
		}
	}
	
	public static void setEnv(Map<String, String> newenv) {
		try {
			Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
			Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
			theEnvironmentField.setAccessible(true);
			Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
			env.putAll(newenv);
			Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
			theCaseInsensitiveEnvironmentField.setAccessible(true);
			Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
			cienv.putAll(newenv);
		} catch (NoSuchFieldException e) {
			try {
				Class[] classes = Collections.class.getDeclaredClasses();
				Map<String, String> env = System.getenv();
				for(Class cl : classes) {
					if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
						Field field = cl.getDeclaredField("m");
						field.setAccessible(true);
						Object obj = field.get(env);
						Map<String, String> map = (Map<String, String>) obj;
						map.clear();
						map.putAll(newenv);
					}
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		} 
	}

}
