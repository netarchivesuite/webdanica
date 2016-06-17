package dk.kb.webdanica.seeds.filtering;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import dk.kb.webdanica.datamodel.WgetSettings;
import dk.kb.webdanica.exceptions.WebdanicaException;

/**
 * Class that resolves redirects by calling the 'wget' program.
 * This identifies destinations of URL-shorteners, and links to non-dk material
 * hidden inside dk-urls, eg.: 
 * http://go.eniro.dk/lg/ni/cat-7728/https:/www.cia.gov/library/publications/the-world-factbook/
 *
 * Uses a tmpfolder to store the temporary files using the argument -P /var/cache/foobar/ 
 * 
 */
public class ResolveRedirects {

	/**
	 * TEST program to try it out on different urls
	 * @param args no args currently used
	 */
	public static void main( String[] args ) {
		String testUrl = "http://t.co/LDWqmtDM"; // should refer to https://wiki.ubuntu.com/UbuntuOpenWeek
		String WGET_PATH = "/usr/bin/wget"; // TODO read from settings
		File tmpFolder = new File("/tmp"); // TODO read from settings
		File wgetPath = new File(WGET_PATH);
		int delayInSecs=3;
		int tries = 3;
		// change this to unittest
		ResolveRedirects rr = new ResolveRedirects(wgetPath, delayInSecs, tries, tmpFolder);
		String result = rr.resolveRedirectedUrl(testUrl);
		System.out.println(result);
	}

    private File wgetPath;
	private int delayInSecs;
	private File tmpFolder;
	private int tries;
    
    public ResolveRedirects(File wgetPath, int delayInSecs, int tries, File tmpFolder ) {
    	this.wgetPath = wgetPath;
    	this.delayInSecs = delayInSecs;
    	this.tmpFolder = tmpFolder;	
    	this.tries = tries;
    	if (!wgetPath.canExecute()) {
    		throw new WebdanicaException("The wget program at location '" + wgetPath.getAbsolutePath() 
    				+ "' is either an incorrect path or not executable");
    	}
    }
 
    public ResolveRedirects(WgetSettings wgetSettings) {
	    this.wgetPath = wgetSettings.getPath();
	    this.delayInSecs = wgetSettings.getDelayInSecs();
    	this.tmpFolder = wgetSettings.getTmpFolder();
    	this.tries = wgetSettings.getTries();
    	if (!wgetPath.canExecute()) {
    		throw new WebdanicaException("The wget program at location '" + wgetPath.getAbsolutePath() 
    				+ "' is either an incorrect path or not executable");
    	}
    }

	public String resolveRedirectedUrl(String url) {
        try {
            String cmdString = wgetPath.getAbsolutePath() + " -P " + tmpFolder.getAbsolutePath() 
            		+ " -S --tries=" + tries + " --wait=" + delayInSecs  + " " + url + " 2>&1"; 
            Runtime rt = Runtime.getRuntime();
            Process  p = rt.exec(cmdString);
            p.waitFor();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String s;
            boolean foundLocation = false;
            String location = null;
            while ((s = r.readLine())!= null) {
                if (s.contains("Location")){
                    foundLocation = true;
                    location = s.split("Location:")[1];
                }
            }
            r.close();
            if (foundLocation) {
                String trimmed = location.trim();
                return trimmed;
            } else {
                return null;
            }
        } catch ( IOException ioe ) { 
            ioe.printStackTrace(); 
        }  catch ( InterruptedException ie ) { 
            ie.printStackTrace(); 
        }
        return null;
    }
    /**
     *  https://en.wikipedia.org/wiki/URL_shortening#Registering_a_short_URL:
     *   bit.ly (Bitly)
     *   goo.gl (Google)
     *	 ow.ly (Hootsuite)
     *	 t.co (Twitter)
     *	 TinyURL (Gilby) = > tinurl.com
     *	 Tr.im (Gravity4)
     *  
     * @param url the given url
     * @return true, if matches any of the possible redirect regexps
     */
    public static boolean isPossibleUrlredirect(String url) {
    	String[] redirectRegexps = new String[] {"/http", // embedded urls  visible in the url 
    			"redir.aspx", // markers that this is redirected (could be others)
    			"http://bit.ly", "http://goo.gl",
    			"http://ow.ly", "http://t.co", "http://tinyurl.com", "http://tr.im"
    			};
    	for (String redString: redirectRegexps) {
    		if (url.matches(redString)) {
    			// TODO log this
    			return true;
    		}
    	}

    	return false;
    }
}
