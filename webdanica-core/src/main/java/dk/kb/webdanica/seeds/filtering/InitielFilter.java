package dk.kb.webdanica.seeds.filtering;

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
		/*
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream is = classloader.getResourceAsStream("outlink-reportfile-final-1460549754730.txt");
		classloader.g
		*/
		File input = new File("/home/svc/devel/webdanica/webdanica-core/src/resources/outlink-reportfile-final-1460549754730.txt");
				
				
		BufferedReader br = new BufferedReader(new FileReader(input));
		String line = null;
		int rejects = 0;
		int accepted = 0;
		while ((line = br.readLine()) != null)  {
			//System.out.println(line);
			if (reject(line)) {
				rejects++;
			} else {
				accepted++;
			}
		}
		System.out.println("rejects: " + rejects);
		System.out.println("accepted: " + accepted);
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
