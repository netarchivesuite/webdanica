package dk.kb.webdanica.core.criteria;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.Tuple;

import dk.kb.webdanica.core.utils.UrlUtils;

/**
 * C17: search for .dk links in outlinks data
 * Return Number of dk links found (of total outlinks).
 * 
 * Inddata: AS links:{ tuple(link:[]) }; 
 */
public class C17 {

    public final static String URL_KEY = "url";
    public final static String TEXT_KEY = "text";
    public final static String MAILTO = "mailto";
    public final static String DK_SUFFIX = ".dk";    
    
    
    public static int computeC17(DataBag links) throws ExecException {
        int foundDKoutLinks = 0;
        if (links != null) {
            Iterator<Tuple> i = links.iterator();

            while (i.hasNext()) {
                Tuple o = i.next();
                Map o1 = (Map) o.get(0);
                String url = ((String) o1.get("url")).toLowerCase();
                if (isDkLink(url)) {
                	foundDKoutLinks++;
                }
            }
        }
        return foundDKoutLinks;
    }

    public static boolean isDkLink(String url) {
    	if (!url.startsWith(MAILTO)) {  // Ignoring mail-urls
            String host = UrlUtils.getHost(url);
            if (host != null && !host.isEmpty() && host.endsWith(DK_SUFFIX)) {
               return true;
            } 
    	}
    	return false;
    }
    
    
	public static Set<String> getLinks(DataBag links) throws ExecException{
		Set<String> linkSet = new HashSet<String>();
        if (links != null) {
            Iterator<Tuple> i = links.iterator();
            while (i.hasNext()) {
                Tuple o = i.next();
                Map o1 = (Map) o.get(0);
                String url = (String) o1.get("url");
                linkSet.add(url);
            }
        }
        return linkSet;
    }
	
	// Used to show the C17 links using the CLinks data
	public static Set<String> getDKLinks(Set<String> links) {
		Set<String> linkSet = new TreeSet<String>();
		for (String link: links) {
			if (isDkLink(link.toLowerCase())) {
				linkSet.add(link);
			}
		}
		return linkSet;
	}	
}
