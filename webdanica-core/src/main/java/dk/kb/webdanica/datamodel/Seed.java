package dk.kb.webdanica.datamodel;

public class Seed {
	
	
/*
 Fields in the seeds table:
url text, 
redirected_url text, 
state int,  // see dk.kb.webdanica.datamodel.Status enum class
hostname text, 
status_reason text, // textual explanation behind its state
tld text, // Top level domain for this seed
danica int, // see dk.kb.webdanica.datamodel.DanicaStatus enum class
inserted_time timetamp,	
 */
	private String url;
	private Status state;
	private String redirectedUrl;
	private String hostname;
	private String statusReason;
	private RejectReason rejectedreason;
	private DanicaStatus danicaStatus;
	
	public Seed(String url) {
		this.setUrl(url);
		this.setState(Status.NEW);
	}
	
	public Seed(String url, String redirectedUrl, Status state, String stateReason, String hostname, String tld, DanicaStatus danicastate, long insertedTime, boolean exported) {
		setUrl(url);
		setRedirectedUrl(redirectedUrl);
		setState(state);
		setStatusReason(stateReason);
		setDanicaStatus(danicastate);
		// Ignored insertedTime and exported
	}
	public String getUrl() {
	    return url;
    }

	public void setUrl(String url) {
	    this.url = url;
    }

	public Status getState() {
	    return state;
    }

	public void setState(Status state) {
	    this.state = state;
    }

	public String getRedirectedUrl() {
	    return redirectedUrl;
    }

	public void setRedirectedUrl(String redirectedUrl) {
	    this.redirectedUrl = redirectedUrl;
    }

	public String getHostname() {
	    return hostname;
    }

	public void setHostname(String hostname) {
	    this.hostname = hostname;
    }

	public String getStatusReason() {
	    return statusReason;
    }

	public void setStatusReason(String statusReason) {
	    this.statusReason = statusReason;
    }

	public DanicaStatus getDanicaStatus() {
	    return danicaStatus;
    }

	public void setDanicaStatus(DanicaStatus danicaStatus) {
	    this.danicaStatus = danicaStatus;
    }
	// TODO are these necessary? or is it enough with StatusReason??
/*
	public BlackListReason getBlacklistedreason() {
	    return blacklistedreason;
    }

	public void setBlacklistedreason(BlackListReason blacklistedreason) {
	    this.blacklistedreason = blacklistedreason;
    }
	*/
	
	public String toString() {
		return "Seed '" + url + "' with status " +  state;
	}
	
}