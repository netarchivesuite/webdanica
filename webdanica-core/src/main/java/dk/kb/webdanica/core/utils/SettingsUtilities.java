package dk.kb.webdanica.core.utils;

import java.io.File;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Utility class for settingsfile utilities.
 */
public class SettingsUtilities {
	
	/** Logging mechanism. */
    private static final Logger logger = Logger.getLogger(SettingsUtilities.class.getName());

    /**
     * Tests if the given settingsFile is a valid SimpleXMl settingsfile.
     * @param settingsFile a given SimpleXml based settings-file.
     * @param verbose if true, write the stack-trace in the logfile.
     * @return true, if the settingsFile is valid, else false;
     */
    public static boolean isValidSimpleXmlSettingsFile(File settingsFile, boolean verbose) {
    	try {
    		new SimpleXml(settingsFile);
    	} catch(Throwable e) {
    		if (verbose) {
    			logger.warning(e.toString());
    		}
    		return false;
    	}
    	return true;
    }
    
    /**
     * Tests if the given settingsFile is a valid SimpleXMl settingsfile.
     * @param settingsFile a given SimpleXml based settings-file.
     * @return true, if the settingsFile is valid, else false;
     */
	public static boolean isValidSimpleXmlSettingsFile(File settingsFile) {
		return isValidSimpleXmlSettingsFile(settingsFile, false);
	}
	
	public static String getStringSetting(String settingsName, String default_string_value) {
    	String returnValue = default_string_value;
	    if (Settings.hasKey(settingsName)) {
	    	String settingsValue = Settings.get(settingsName);  
	    	if (settingsValue == null || settingsValue.isEmpty()) {
	    		logger.warning("Using default value '" + default_string_value + "' for setting '" + settingsName + "', as the value in the settings is null or empty");
	    	} else {
	    		returnValue = settingsValue;
	    	}
	    } else {
	    	logger.warning("The setting '" + settingsName + "' is not defined in the settingsfile. Using the default value: " + default_string_value);
	    }
	    return returnValue;
    }
    

	public static int getIntegerSetting(String settingsName, int default_int_value) {
	 	int returnValue = default_int_value;
	    if (Settings.hasKey(settingsName)) {
	    	String settingsValueAsString = Settings.get(settingsName);  
	    	if (settingsValueAsString == null || settingsValueAsString.isEmpty()) {
	    		logger.warning("Using default value '" + default_int_value + "' for setting '" + settingsName + "', as the value in the settings is null or empty");
	    		int intValue;
	    		try {
	            	intValue = Integer.parseInt(settingsValueAsString);
	            	returnValue = intValue;
	            } catch (NumberFormatException e) {
	            	logger.warning("Using default value '" + default_int_value + "' for setting '" + settingsName + "', as the value '" + settingsValueAsString 
	            			+ "'  in the settings is not a valid integer");
	            }
	    	} else {
	    		returnValue = default_int_value;
	    	}
	    } else {
	    	logger.warning("The setting '" + settingsName + "' is not defined in the settingsfile. Using the default value: " + default_int_value);
	    }
	    return returnValue;
    }

	/**
	 * test if property file is defined by the given propertyKey. If not call
	 * System.exit(1);
	 * @param propertyKey a key for a property
	 */
	public static void testPropertyFile(String propertyKey){
		String setting = System.getProperty(propertyKey);
		if (setting == null) {
			System.err.println("Required java property '" + propertyKey + "' is undefined");
			System.exit(1);
		}
		File settingsFile = new File(setting);
	
		if (!settingsFile.exists()) {
			System.err.println("The settings file defined by property '" + propertyKey + "' does not exist: " 
					+ settingsFile.getAbsolutePath() + "' does not exist");
			System.exit(1);
		}
	}

	public static void verifyClassOrExit(String dbdriver) {
		try {
			Class.forName(dbdriver);
		} catch (ClassNotFoundException e) {
			System.out.println("Required class '" + dbdriver + "' not found in classpath");
			System.out.println("Program terminated");
			System.exit(1);
		}
    }

	public static void verifyWebdanicaSettings(Set<String> requiredSettings) {
	    boolean exit = false;
	    for (String key: requiredSettings){
	    	if (!Settings.hasKey(key)) {
	    		exit = true;
	    		System.err.println("Missing setting '" + key + "' in settingsfile");
	    	}
	    }
	    if (exit) {
	    	System.err.println("Exiting program prematurely because of missing settings");
	    	System.exit(1);
	    }
	    
    }
	
}

