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
	private BlackListReason blacklistedreason;
	private DanicaStatus danicaStatus;
	
	public Seed(String url) {
		this.setUrl(url);
		this.setState(Status.NEW);
	}
	/*
	url text PRIMARY KEY,
    danica int,
    exported boolean,
    hostname text,
    inserted_time timestamp,
    redirected_url text,
    status int,
    status_reason text,
    tld text
*/
	
	
	/*
	public Seed(String url, Status state, String redirectedUrl, String hostname, DanicaStatus danicastate,  )
	*/
	

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

	public BlackListReason getBlacklistedreason() {
	    return blacklistedreason;
    }

	public void setBlacklistedreason(BlackListReason blacklistedreason) {
	    this.blacklistedreason = blacklistedreason;
    }
	
}