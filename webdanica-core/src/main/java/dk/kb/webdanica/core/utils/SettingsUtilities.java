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
	    		logger.info("Using value '" + settingsValue + "' for setting '" + settingsName + "'.");
	    		returnValue = settingsValue;
	    	}
	    } else {
	    	logger.warning("The setting '" + settingsName + "' is not defined in the settingsfile. Using the default value: '" + default_string_value + "'");
	    }
	    return returnValue;
    }
    
	public static int getIntegerSetting(String settingsName, int default_int_value) {
	 	int returnValue = default_int_value;
	    if (Settings.hasKey(settingsName)) {
	    	String settingsValueAsString = Settings.get(settingsName);  
	    	if (settingsValueAsString == null || settingsValueAsString.isEmpty()) {
	    		logger.warning("Using default value '" + default_int_value + "' for setting '" + settingsName + "', as the value in the settings is null or empty");
	    	} else { // Try to parse the settingsValueAsString as a valid Integer
	    		int intValue;
	    		try {
	            	intValue = Integer.parseInt(settingsValueAsString);
	            	returnValue = intValue;
	            	logger.info("Using value '" + returnValue + "' for setting '" + settingsName + "'.");
	            } catch (NumberFormatException e) {
	            	logger.warning("Using default value '" + default_int_value + "' for setting '" + settingsName + "', as the value '" + settingsValueAsString 
	            			+ "'  in the settings is not a valid integer");
	            }
	    	}
	    } else {
	    	logger.warning("The setting '" + settingsName + "' is not defined in the settingsfile. Using the default value: '" + default_int_value + "'");
	    }
	    return returnValue;
    }

	public static boolean getBooleanSetting(String settingsName, boolean default_bool_value) {
		boolean returnValue = default_bool_value;
	    if (Settings.hasKey(settingsName)) {
	    	String settingsValueAsString = Settings.get(settingsName);  
	    	if (settingsValueAsString == null || settingsValueAsString.isEmpty()) {
	    		logger.warning("Using default value '" + default_bool_value + "' for setting '" + settingsName + "', as the value in the settings is null or empty");
	    	} else {
	    		boolean boolValue = Boolean.parseBoolean(settingsValueAsString);
	            returnValue = boolValue;
	            logger.info("Using value '" + returnValue + "' for setting '" + settingsName + "'.");
	    	}
	    } else {
	    	logger.warning("The setting '" + settingsName + "' is not defined in the settingsfile. Using the default value: '" + default_bool_value + "'");
	    }
	    return returnValue;
    }
	
	/**
	 * test if property file is defined by the given propertyKey. If not call
	 * System.exit(1);
	 * @param propertyKey a key for a property
	 */
	public static boolean testPropertyFile(String propertyKey, boolean exitIfCheckFails){
		String setting = System.getProperty(propertyKey);
		if (setting == null) {
			if (exitIfCheckFails) {
				System.err.println("Required java property '" + propertyKey + "' is undefined");
				System.exit(1);
			} else {
				logger.warning("Required java property '" + propertyKey + "' is undefined");
				return false;
			}
		}
		File settingsFile = new File(setting);
	
		if (!settingsFile.exists()) {
			String errMsg = "The settings file defined by property '" + propertyKey + "' does not exist: " 
					+ settingsFile.getAbsolutePath() + "' does not exist";
			if (exitIfCheckFails) {
				System.err.println(errMsg);
				System.exit(1);
			} else {
				logger.warning(errMsg);
				return false;
			}
		}
		return true;
	}

	public static boolean verifyClass(String dbdriver, boolean exitIfcheckFails) {
		try {
			Class.forName(dbdriver);
		} catch (ClassNotFoundException e) {
			if (exitIfcheckFails) {
				System.out.println("Required class '" + dbdriver + "' not found in classpath");
				System.out.println("Program terminated");
				System.exit(1);
			}
			return false;
		}
		return true;
    }

	public static boolean verifyWebdanicaSettings(Set<String> requiredSettings, boolean exitIfCheckFails) {
	    boolean exit = false;
	    for (String key: requiredSettings){
	    	if (!Settings.hasKey(key)) {
	    		exit = true;
	    		System.err.println("Missing setting '" + key + "' in settingsfile");
	    	}
	    }
	    if (exit && exitIfCheckFails) {
	    	System.err.println("Exiting program prematurely because of missing settings");
	    	System.exit(1);
	    } else if (exit) {
	    	return false;
	    }
	    return true;
    }
	
}

