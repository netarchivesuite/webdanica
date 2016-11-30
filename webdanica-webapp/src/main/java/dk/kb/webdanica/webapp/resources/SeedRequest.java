package dk.kb.webdanica.webapp.resources;

import dk.kb.webdanica.core.datamodel.Status;
import dk.kb.webdanica.core.datamodel.criteria.CriteriaUtils;

public class SeedRequest {

	private String url;
	private Status newStatus;
	private String pathInfo;

	public SeedRequest(String url, Status newStatus, String pathInfo) {
	    this.url = url;
	    this.newStatus = newStatus;
	    this.pathInfo = pathInfo;
    }

	public Status getNewState() {
		return this.newStatus;
	}
	
	public String getUrl() {
		return this.url;
	}
	
	public String getPathInfo() {
		return this.pathInfo;
	}
	
	//
	// presence of numeric is the request to set the status to this status defined by the numeric value
	// sample pathinfo /seed/aHR0cDovL3Jpc2FnZXIuaW5mby8=/<numeric> 
	
	public static SeedRequest getUrlFromPathinfo(String pathInfo, String pattern) {
		String[] split = pathInfo.split(pattern);
		
		SeedRequest resultKeys = new SeedRequest(null, null, pathInfo);
        if (split.length > 1) {
        	String arguments = split[1];
            String[] argumentParts = arguments.split("/");
            if (argumentParts.length == 2) {
            	Status newStatus = Status.fromOrdinal(Integer.parseInt(argumentParts[1]));
            	resultKeys = new SeedRequest(CriteriaUtils.fromBase64(argumentParts[0]), newStatus, pathInfo);
            	//logger.info("Found Criteriakeys: " + resultKeys);
            } else {
            	resultKeys = new SeedRequest(CriteriaUtils.fromBase64(argumentParts[0]), null, pathInfo);
            }
        }
        return resultKeys;
	}

	
}
