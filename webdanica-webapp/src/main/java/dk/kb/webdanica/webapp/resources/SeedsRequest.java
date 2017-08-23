package dk.kb.webdanica.webapp.resources;

/**
 * Parses requests like
 * "/seeds/$state/$newstate/"
 * "/seeds/$state/"
 * "/seeds/-1/-1/$domain/" - show all seeds from a domain (all states)
 * "/seeds/$state/-1/$domain/" - show all seeds from a domain with a given state
 *
 */
public class SeedsRequest {

    public static final Integer NEWSTATE_DUMMY_VALUE = -1; 
    
	public static SeedsRequest getUrlFromPathinfo(String pathInfo, String pattern) {
		String[] split = pathInfo.split(pattern);
		SeedsRequest resultKeys = new SeedsRequest(null, null, null, pathInfo);
		if (split.length > 1) {
        	String arguments = split[1];
            String[] argumentParts = arguments.split("/");
            if (argumentParts.length == 3) {
                Integer currentState = Integer.parseInt(argumentParts[0]);
                Integer newStatus = Integer.parseInt(argumentParts[1]);
                String domain = argumentParts[2];
                resultKeys = new SeedsRequest(currentState, newStatus, domain, pathInfo);
            } else if (argumentParts.length == 2) {
                Integer currentState = Integer.parseInt(argumentParts[0]);
            	Integer newStatus = Integer.parseInt(argumentParts[1]);
            	resultKeys = new SeedsRequest(currentState, newStatus, null, pathInfo);
            } else if (argumentParts.length == 1) {
            	Integer currentState = Integer.parseInt(argumentParts[0]);
            	resultKeys = new SeedsRequest(currentState, null, null, pathInfo);
            }
        }
	    return resultKeys;
    }

	private Integer currentState;
	private Integer newStatus;
	private String pathInfo;
    private String domain;
	
	public SeedsRequest(Integer currentState, Integer newStatus, String domain, String pathInfo) {
	    this.currentState = currentState;
	    this.newStatus = newStatus;
	    this.domain = domain;
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
		return newStatus != null && newStatus != NEWSTATE_DUMMY_VALUE;
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
	
	public boolean isShowSeedsFromDomainRequest() {
	    if (NEWSTATE_DUMMY_VALUE.equals(newStatus)) {
	        return true;
	    }
	    return false;
	}
	
	public String getDomain() {
	    return this.domain;
	}

    public boolean isShowSeedsFromDomainWithStateRequest() {
        if (NEWSTATE_DUMMY_VALUE.equals(newStatus) && !NEWSTATE_DUMMY_VALUE.equals(currentState)) {
            return true;
        }
        return false;
    }
	
}
