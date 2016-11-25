package dk.kb.webdanica.webapp.resources;

import java.util.Date;
import java.util.logging.Logger;

import com.antiaction.common.templateengine.TemplatePlaceHolder;

import dk.kb.webdanica.core.datamodel.Status;
import dk.kb.webdanica.core.datamodel.criteria.CriteriaUtils;

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
	
	//
	// presence of numeric is the request to set the status to this status defined by the numeric value
	// sample pathinfo /seed/aHR0cDovL3Jpc2FnZXIuaW5mby8=/<numeric> 
	
	public static SeedRequest getUrlFromPathinfo(String pathInfo, String pattern) {
		String[] split = pathInfo.split(pattern);
		
		SeedRequest resultKeys = null;
        if (split.length > 1) {
        	String arguments = split[1];
            String[] argumentParts = arguments.split("/");
            if (argumentParts.length == 2) {
            	Status newStatus = Status.fromOrdinal(Integer.parseInt(argumentParts[1]));
            	resultKeys = new SeedRequest(CriteriaUtils.fromBase64(argumentParts[0]), newStatus);
            	//logger.info("Found Criteriakeys: " + resultKeys);
            } else {
            	resultKeys = new SeedRequest(CriteriaUtils.fromBase64(argumentParts[0]), null);
            }
        }
        return resultKeys;
	}

	public static String printDate(Long insertedTime) {
	    if (insertedTime == null || insertedTime == 0L) {
	    	return insertedTime + "";
	    } else {
	    	return new Date(insertedTime).toString();
	    }
	    
    }
	
	
	
	
}
