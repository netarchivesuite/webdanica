package dk.kb.webdanica.webapp.resources;

import java.util.Date;
import java.util.logging.Logger;

import com.antiaction.common.templateengine.TemplatePlaceHolder;

public class ResourceUtils {
	/**
	 * 
	 * @param somePlace
	 * @param placeholdername
	 * @param value
	 * @param template
	 * @param logger
	 */
	public static void insertText(TemplatePlaceHolder somePlace, String placeholdername, String value, String template, Logger logger) {
	if (somePlace != null) {
        somePlace.setText(value);
     } else {
     	logger.warning("No placeholder '" + placeholdername + "' found in template '" + template + "'" );
     }
	}
	
	public static String printDate(Long insertedTime) {
	    if (insertedTime == null || insertedTime == 0L) {
	    	return insertedTime + "";
	    } else {
	    	return new Date(insertedTime).toString();
	    }
	    
    }
	
	
	
	
}
