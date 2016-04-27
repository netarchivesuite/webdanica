package dk.kb.webdanica.seeds.filtering;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Class that resolves redirects by calling the 'wget' program.
 * This identifies destinations of URL-shorteneners, and links to non-dk material
 * hidden inside dk-urls, eg.: 
 * http://go.eniro.dk/lg/ni/cat-7728/https:/www.cia.gov/library/publications/the-world-factbook/
 *
 *
 * TODO: Use a tmpfolder to store the files:
 *  -P /var/cache/foobar/  
 *
 */
public class ResolveRedirects {

    public static void main( String[] args ) {
            String testUrl = "http://t.co/LDWqmtDM";
            System.out.println(getNewLocation(testUrl, 3));
            File wgetPath = new File("/usr/bin/wget");
            //System.out.println(wgetPath.canExecute());
    }
    
    public static String getNewLocation(String url, int delayInSecs) {
        try {
            String cmdString = "/usr/bin/wget -S --tries=3 --wait=" + delayInSecs  + " " + url + " 2>&1"; 
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


