package dk.kb.webdanica.core.seeds.filtering;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import dk.netarkivet.common.utils.DomainUtils;


public class InitielFilter {

	public static void main(String[] args) throws IOException {

		// refers to webdanica-core/src/main/resources/outlink-reportfile-final-1460549754730.txt
		File input = new File("src/main/resources/outlink-reportfile-final-1460549754730.txt"); 

		System.out.println(input.getCanonicalPath());
		String WGET_PATH = "/usr/bin/wget"; // TODO read from settings
		File tmpFolder = new File("/tmp"); // TODO read from settings
		File wgetPath = new File(WGET_PATH);
		int delayInSecs=3;
		int tries = 3;
		ResolveRedirects rr = new ResolveRedirects(wgetPath, delayInSecs, tries, tmpFolder);

		BufferedReader br = new BufferedReader(new FileReader(input));
		String line = null;
		int rejects = 0;
		int accepted = 0;
		int redirects = 0;
		while ((line = br.readLine()) != null)  {
			//System.out.println(line);
			if (reject(line)) {
				rejects++;
			} else {
				accepted++;
				String redirected = rr.resolveRedirectedUrl(line);
				if (redirected != null && isProperUrl(redirected)) {
					redirects++;
				}

			}
		}
		System.out.println("rejects: " + rejects);
		System.out.println("accepted: " + accepted);
		System.out.println("redirects: " + redirects);
	}
	
	private static boolean isProperUrl(String redirected) {
		return !reject(redirected);
    }
	public static boolean reject(String seed) {
		
		try {
	        URI url = new URI(seed);
	        String scheme = url.getScheme();
	        if (!isValidScheme(scheme)) {
	        	return true; // BAD scheme
	        }
	        String host = url.getHost();
	        String domainName = DomainUtils.domainNameFromHostname(host);
	        if (domainName == null) {
	        	return true; // Bad domainname (null)
	        }
	        //System.out.println("domain: " +  domainName);
        } catch (URISyntaxException e) {
	        //LOG.
	        e.printStackTrace();
	        return true; // Bad URL
        }
		return false;
	}
	
	private static boolean isValidScheme(String scheme) {
	    //String[] validSchemes = new String[]{"https", "http", "ftp"};
	    Set<String> validSchemesSet = new HashSet<String>();
	    validSchemesSet.add("https");
	    validSchemesSet.add("http");
	    validSchemesSet.add("ftp");
	    return validSchemesSet.contains(scheme);  
    }	
}
