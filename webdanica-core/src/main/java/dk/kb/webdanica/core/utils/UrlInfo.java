package dk.kb.webdanica.core.utils;

public class UrlInfo {

	private String hostname;
	private String domain;
	private String tld;

	public UrlInfo(String hostname, String domain, String tld) {
		this.hostname = hostname;
		this.domain = domain;
		this.tld = tld;
    }

	public String getHostname() {
	    return hostname;
    }
	
	public String getDomain() {
		return domain;
	}

	public String getTld() {
		return tld;
	}
	
}
