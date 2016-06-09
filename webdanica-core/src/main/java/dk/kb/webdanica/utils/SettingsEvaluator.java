package dk.kb.webdanica.utils;

import java.io.File;
import java.util.logging.Logger;

/**
 * Simple class for hiding validation of settingsfile.
 * Not included with the dk.kb.webdanica.utils.Settings class to avoid premature Class instantiation of the Settings class.
 */
public class SettingsEvaluator {
	
	/** Logging mechanism. */
    private static final Logger logger = Logger.getLogger(SettingsEvaluator.class.getName());

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
}

