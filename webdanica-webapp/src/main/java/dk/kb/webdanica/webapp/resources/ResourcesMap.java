package dk.kb.webdanica.webapp.resources;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import dk.kb.webdanica.core.WebdanicaSettings;
import dk.kb.webdanica.core.exceptions.WebdanicaException;
import dk.kb.webdanica.core.utils.Settings;
import dk.kb.webdanica.core.utils.SettingsUtilities;
import dk.netarkivet.common.exceptions.UnknownID;
import dk.kb.webdanica.webapp.Constants;

public class ResourcesMap {

	private final Map<String, ResourceDescription> map;
	private static final String LEFT_ANGLE_BRACKET = "<";
	private static final String RIGHT_ANGLE_BRACKET = ">";
	private static final String LEFT_ANGLE_BRACKET_ENCODED = "%3C";
	private static final String RIGHT_ANGLE_BRACKET_ENCODED = "%3E";
	
	
	private static final Logger logger = Logger.getLogger(ResourcesMap.class.getName());
	private boolean defaultSecuredValue;
	
	public ResourcesMap() {
		String[] paths = new String[]{};
		String[] bools = new String[]{};
		map = new HashMap<String, ResourceDescription>();
		defaultSecuredValue = SettingsUtilities.getBooleanSetting(WebdanicaSettings.WEBAPP_DEFAULT_SECURED_SETTING, Constants.DEFAULT_WEBAPP_DEFAULT_SECURED_SETTING);
		try {
			paths = Settings.getAll(WebdanicaSettings.WEBAPP_RESOURCE_PATH);
			bools = Settings.getAll(WebdanicaSettings.WEBAPP_RESOURCE_SECURED);
		} catch (UnknownID e) {
			logger.warning("No webapp ressources found in the webdanica settings file. "
					 + "Check that your settings file is configured correctly");
			return;
		}
		
		if (paths.length != bools.length) {
			throw new WebdanicaException("mismatch between number of path elements and number of secured elements");
		}
		int elements = paths.length;
	    for (int i=0; i < elements; i++) {
	    	String path = paths[i].trim();
	    	path = path.replace(LEFT_ANGLE_BRACKET_ENCODED, LEFT_ANGLE_BRACKET);
	    	path = path.replace(RIGHT_ANGLE_BRACKET_ENCODED, RIGHT_ANGLE_BRACKET);
	    	
	    	map.put(path, new ResourceDescription(path, Boolean.getBoolean(bools[i])));
	    }
		logger.info("Found " + map.size() + " resources in the settings");   
	}
	
	public ResourceDescription getResourceByPath(String path) {
		ResourceDescription rd = map.get(path);
		if (rd == null) {
			logger.warning("No resource found in settings with path '" + path 
					+ "'. Returning dummy resourcedescription with secured=" + defaultSecuredValue);
			rd = new ResourceDescription("dummypath", defaultSecuredValue);
		}
		return rd;
	}
	
	public Map<String, ResourceDescription> getmap() {
		return this.map;
	}
	
}
