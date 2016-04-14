package dk.kb.webdanica.criteria;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import org.apache.pig.EvalFunc;
import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.Tuple;

import dk.kb.webdanica.utils.Constants;

/**
 * C17: search for .dk links in outlinks data
 * Return Number of dk links found (of total outlinks).
 * 
 * Inddata: AS links:{ tuple(link:[]) }; 
 */
public class C17 extends EvalFunc<String>{

    public final static String URL_KEY = "url";
    public final static String TEXT_KEY = "text";
    public final static String MAILTO = "mailto";
    public final static String DK_SUFFIX = ".dk";    
    
    
 
    @Override
    public String exec(Tuple input) throws IOException {
        if (input == null|| input.size() == 0 || input.get(0) == null) {
            return Constants.getCriteriaName(this) + ": " + Constants.NODATA;
        } 
        
        DataBag links = (DataBag)(input.get(0));
        int foundDKOutlinks = computeC17(links);
        
              
        return "C17: found " + foundDKOutlinks;
    }
    
    private static String getHost(String urlLower) {
        URL u = null;
        try {
            u = new URL(urlLower);
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

    public static int computeC17(DataBag links) throws ExecException {
        int foundDKoutLinks = 0;
        if (links != null) {
            Iterator<Tuple> i = links.iterator();

            while (i.hasNext()) {
                Tuple o = i.next();
                Map o1 = (Map) o.get(0);
                String url = ((String) o1.get("url")).toLowerCase();
                if (url.startsWith(MAILTO)) {
                    //System.out.println("Ignoring mail-url: " + url);
                } else {
                    String host = getHost(url);
                    if (!host.isEmpty()) {
                        if (host.endsWith(DK_SUFFIX)) {
                            foundDKoutLinks++;
                        }
                    } else {
                        //System.out.println("Ignoring problematic-url: " + url);
                    }

                }
            }}
        return foundDKoutLinks;
    }

}
