package dk.kb.webdanica.webapp.resources;


public class SeedsRequest {

	public static SeedsRequest getUrlFromPathinfo(String pathInfo, String pattern) {
		String[] split = pathInfo.split(pattern);
		SeedsRequest resultKeys = new SeedsRequest(null, null, pathInfo);
		if (split.length > 1) {
        	String arguments = split[1];
            String[] argumentParts = arguments.split("/");
            if (argumentParts.length == 2) {
            	Integer currentState = Integer.parseInt(argumentParts[0]);
            	Integer newStatus = Integer.parseInt(argumentParts[1]);
            	resultKeys = new SeedsRequest(currentState, newStatus, pathInfo);
            } else {
            	Integer currentState = Integer.parseInt(argumentParts[0]);
            	resultKeys = new SeedsRequest(currentState, null, pathInfo);
            }
        }
	    return resultKeys;
    }

	private Integer currentState;
	private Integer newStatus;
	private String pathInfo;
	
	public SeedsRequest(Integer currentState, Integer newStatus, String pathInfo) {
	    this.currentState = currentState;
	    this.newStatus = newStatus;
	    this.pathInfo = pathInfo;
    }
	
	public Integer getNewState() {
		return this.newStatus;
	}
	
	public Integer getCurrentState() {
		return this.currentState;
	}
	
	public String getPathInfo() {
		return this.pathInfo;
	}
	
	public String toString() {
		return "currentState:" + currentState + ", newState:" + newStatus + ",pathinfo:" + pathInfo;
	}
	
	public boolean isChangeStateRequest() {
		return newStatus != null;
	}
	public boolean isAcceptSeedAsDanicaRequest() {
		return (newStatus == SeedRequest.ACCEPT_AS_DANICA_CODE);
	}

	public boolean isRejectSeedAsDanicaRequest() {
		return (newStatus == SeedRequest.REJECT_AS_DANICA_CODE);
	}

	public boolean isRetryAnalysisRequest() {
	    return (newStatus == SeedRequest.RETRY_ANALYSIS_CODE);
    }	
}
