package dk.kb.webdanica.core.utils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import dk.kb.webdanica.core.datamodel.URL_REJECT_REASON;
import dk.kb.webdanica.core.seeds.filtering.IgnoredProtocols;
import dk.kb.webdanica.core.utils.UrlInfo;
import dk.netarkivet.common.utils.DomainUtils;

public class UrlUtils {
    
    private static Pattern VALID_IPV4_PATTERN = null;
    private static Pattern VALID_IPV6_PATTERN = null;
    private static final String ipv4Pattern = "(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])";
    private static final String ipv6Pattern = "([0-9a-f]{1,4}:){7}([0-9a-f]){1,4}";

    static {
      try {
        VALID_IPV4_PATTERN = Pattern.compile(ipv4Pattern, Pattern.CASE_INSENSITIVE);
        VALID_IPV6_PATTERN = Pattern.compile(ipv6Pattern, Pattern.CASE_INSENSITIVE);
      } catch (PatternSyntaxException e) {
        //logger.severe("Unable to compile pattern", e);
      }
    }

    
    public static void main (String[] args) {
        String domain="127.0.0.1";
        String domain1="127.0.0.dk";
        String domain2="999.0.0.0";
        System.out.println("isIP: " + isIpAddress(domain));
        System.out.println("isIP: " + isIpAddress(domain1));
        System.out.println("isIP: " + isIpAddress(domain2));
    }
	
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
				return URL_REJECT_REASON.BAD_SCHEME; // bad scheme or scheme == null
			}
			String host = url.getHost();
			if (host == null) {  
				return URL_REJECT_REASON.MISSING_HOST;
			}
			String domainName = DomainUtils.domainNameFromHostname(host);
			if (domainName == null) {
				return URL_REJECT_REASON.MISSING_DOMAIN; 
			}
			if (isIpAddress(domainName)) {
			    return URL_REJECT_REASON.IP_URL;
			}
		} catch (URISyntaxException e) {
			//LOG.
			//e.printStackTrace();
			return URL_REJECT_REASON.BAD_URL; // Bad URL
		}
		return URL_REJECT_REASON.NONE;
	}	
	
	public static boolean ignoredSchema(String url) {
	    boolean ignored=false;
	    return ignored;
	}
	
	
	
	/**
	 * Determine if the given string is a valid IPv4 or IPv6 address.  This method
	 * uses pattern matching to see if the given string could be a valid IP address.
	 * Source: https://stackoverflow.com/questions/15875013/extract-ip-addresses-from-strings-using-regex?rq=1
	 * 
	 * @param ipAddress A string that is to be examined to verify whether or not
	 *  it could be a valid IP address.
	 * @return <code>true</code> if the string is a value that is a valid IP address,
	 *  <code>false</code> otherwise.
	 */
	public static boolean isIpAddress(String ipAddress) {

	    Matcher m1 = UrlUtils.VALID_IPV4_PATTERN.matcher(ipAddress);
	    if (m1.matches()) {
	        return true;
	    }
	    Matcher m2 = UrlUtils.VALID_IPV6_PATTERN.matcher(ipAddress);
	    return m2.matches();
	}

    /**
	 * @param scheme
	 * @return
	 */
	public static boolean isValidScheme(String scheme) {
	    return scheme != null && IgnoredProtocols.schemaMatchesIgnoredProtocol(scheme) == null;  
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
        	// TODO add logging
        }
		
		return new UrlInfo(hostname, domain, tld);
    }

	public static String getHost(String url) {
        URL u = null;
        try {
            u = new URL(url);
        } catch (MalformedURLException e) {
            return "";
        }
        String host = u.getHost();
        // If host cannot be determined, return empty string.
        if ( host == null ) host = "";
        // Ensure i18n hosts are in Unicode format.
        host = java.net.IDN.toUnicode( host, java.net.IDN.ALLOW_UNASSIGNED );
        return host;
    }
	
	/**
	 * Find tld from the given domain by testing 
	 * either if "X." plus the last two parts of the domain or "X." plus the last part of the domain results in a valid domain.
	 * If both is valid, then the two-part tld has precedence. If neither is found, then "N/A" is returned 
	 *  
	 * @param domain a given domain
	 * @return the found tld or "N/A" if non found
	 */
	public static String findTld(String domain) {
	    if (domain == null || domain.isEmpty()) {
	    	return "N/A";
	    }
	    String[] domainParts = domain.split("\\.");
	    int length = domainParts.length;
	    // Try single part tlds
	    String singlePartTld = domainParts[length-1];
	    // Try double part tlds
	    String doublePartTld = domainParts[length-2] + "." + domainParts[length-1];
	    boolean validSinglepart = DomainUtils.isValidDomainName("X." + singlePartTld);
	    boolean validDoublepart = DomainUtils.isValidDomainName("X." + doublePartTld);
	    if (validDoublepart) {
	    	return doublePartTld;
	    } else if (validSinglepart) {
	    	return singlePartTld;
	    }
	    return "N/A";
    }
	
}
