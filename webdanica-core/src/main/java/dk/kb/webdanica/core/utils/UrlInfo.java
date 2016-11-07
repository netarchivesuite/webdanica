package dk.kb.webdanica.core.utils;

/**
 * Helper class for containing hostname, domain, and tld for a single url.
 */
public class UrlInfo {

	private String hostname;
	private String domain;
	private String tld;
	
	/**
	 * Constructor for the UrlInfo class.
	 * @param hostname 
	 * @param domain
	 * @param tld
	 */
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
