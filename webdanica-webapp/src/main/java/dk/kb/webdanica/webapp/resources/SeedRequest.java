package dk.kb.webdanica.webapp.resources;

import dk.kb.webdanica.core.datamodel.Status;

public class SeedRequest {

	private String url;
	private Status newStatus;

	public SeedRequest(String fromBase64, Status newStatus) {
	    this.url = fromBase64;
	    this.newStatus = newStatus;
    }

	public Status getNewState() {
		return this.newStatus;
	}
	
	public String getUrl() {
		return this.url;
	}
}
