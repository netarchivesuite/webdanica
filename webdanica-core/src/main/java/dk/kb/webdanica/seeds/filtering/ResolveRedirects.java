package dk.kb.webdanica.seeds.filtering;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Class that resolves redirects by calling the 'wget' program.
 * This identifies destinations of URL-shorteners, and links to non-dk material
 * hidden inside dk-urls, eg.: 
 * http://go.eniro.dk/lg/ni/cat-7728/https:/www.cia.gov/library/publications/the-world-factbook/
 *
 * Use a tmpfolder to store the files: -P /var/cache/foobar/ 
 * 
 * TODO Ensure that the wget program exists before calling the program   
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
}
