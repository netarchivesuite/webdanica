package dk.kb.webdanica.core.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import dk.kb.webdanica.core.datamodel.URL_REJECT_REASON;
import dk.kb.webdanica.core.utils.UrlInfo;
import dk.netarkivet.common.utils.DomainUtils;

public class UrlUtils {
	
	/** Check, if the seed is OK or should be rejected.
	 * 
	 * @param seed a url to test
	 * @return a reason for rejection (URL_REJECT_REASON.NONE means seed is OK)
	 */
	public static URL_REJECT_REASON isRejectableURL(String seed) {
		try {
			URI url = new URI(seed);
			String scheme = url.getScheme();
			if (!isValidScheme(scheme)) {
				return URL_REJECT_REASON.BAD_SCHEME; // wrong scheme and scheme == null
			}
			String host = url.getHost();
			if (host == null) { // TODO: Or a different reason: MISSING_HOST or just BAD_URL 
				return URL_REJECT_REASON.MISSING_DOMAIN;
			}
			String domainName = DomainUtils.domainNameFromHostname(host);
			if (domainName == null) {
				return URL_REJECT_REASON.MISSING_DOMAIN; 
			}
		} catch (URISyntaxException e) {
			//LOG.
			//e.printStackTrace();
			return URL_REJECT_REASON.BAD_URL; // Bad URL
		}
		return URL_REJECT_REASON.NONE;
	}	
	
	
	/**
	 * @param scheme
	 * @return
	 * TODO read from WebdanicaSettings
	 */
	public static boolean isValidScheme(String scheme) {
	    //String[] validSchemes = new String[]{"https", "http", "ftp"};
	    Set<String> validSchemesSet = new HashSet<String>();
	    validSchemesSet.add("https");
	    validSchemesSet.add("http");
	    validSchemesSet.add("ftp");
	    return scheme != null && validSchemesSet.contains(scheme.toLowerCase());  
    }


	public static UrlInfo getInfo(String url) {
		String hostname = "N/A";
		String domain = "N/A";
		String tld = "N/A";
		try {
	        URI Url = new URI(url);
	        hostname = Url.getHost();
	        domain = DomainUtils.domainNameFromHostname(hostname);
	        tld = findTld(domain);
        } catch (Throwable e) {
	       // TODO log this exception
        }
		
		return new UrlInfo(hostname, domain, tld);
    }


	public static String findTld(String domain) {
	    if (domain == null || domain.isEmpty()) {
	    	return "N/A";
	    }
	    String[] domainParts = domain.split(".");
	    int length = domainParts.length;
	    // Try single dot tld
	    String singlePartTld = domainParts[length-1];
	    String doublePartTld = domainParts[length-2] + "." + domainParts[length-1];
	    if (DomainUtils.isValidDomainName("X." + singlePartTld)) {
	    	return singlePartTld;
	    } else if (DomainUtils.isValidDomainName("X." + doublePartTld)) {
	    	return doublePartTld;
	    }
	    return "N/A";
    }
	
}
