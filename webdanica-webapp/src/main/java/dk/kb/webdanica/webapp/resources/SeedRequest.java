package dk.kb.webdanica.webapp.resources;

import dk.kb.webdanica.core.datamodel.criteria.CriteriaUtils;

public class SeedRequest {	
	
	private String url;
	private Integer newStatus;
	private String pathInfo;
	public static final int ACCEPT_AS_DANICA_CODE = 100;
	public static final int REJECT_AS_DANICA_CODE = 101;
	public static final int RETRY_ANALYSIS_CODE = 102;
	
	public SeedRequest(String url, Integer newStatus, String pathInfo) {
	    this.url = url;
	    this.newStatus = newStatus;
	    this.pathInfo = pathInfo;
    }

	public Integer getNewState() {
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
            	Integer newStatus = Integer.parseInt(argumentParts[1]);
            	resultKeys = new SeedRequest(CriteriaUtils.fromBase64(argumentParts[0]), newStatus, pathInfo);
            } else {
            	resultKeys = new SeedRequest(CriteriaUtils.fromBase64(argumentParts[0]), null, pathInfo);
            }
        }
        return resultKeys;
	}
	
	public boolean isAcceptSeedAsDanicaRequest() {
		return (newStatus == ACCEPT_AS_DANICA_CODE);
	}

	public boolean isRejectSeedAsDanicaRequest() {
		return (newStatus == REJECT_AS_DANICA_CODE);
	}

	public boolean isRetryAnalysisRequest() {
	    return (newStatus == RETRY_ANALYSIS_CODE);
    }
}
