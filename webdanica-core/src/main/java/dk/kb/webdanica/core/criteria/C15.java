package dk.kb.webdanica.core.criteria;

import java.io.IOException;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;

import dk.kb.webdanica.core.utils.Constants;

public class C15 extends EvalFunc<String> {
    
    /** Try to match the following tlds. */
    private static String[] tlds_searched = new String[]{
        ".dk", ".no", ".se", ".de", ".eu", ".org", ".com", ".net", ".nu",".tv",
        ".info"
    };    
    
    /**
     * @param hostname a hostname in lowercase
     * @return null, if hostname does not belong to neighbouring country, 
     * otherwise the tld of the hostname is returned.
     */
    public static String findIfHostBelongsToNeighbouringCountry(String hostname) {
        String res = null;
        for (String tld: tlds_searched) {
            if (hostname.endsWith(tld)) {
                res = tld;
            }
        }
        
        return res;
    }

    @Override
    public String exec(Tuple input) throws IOException {
        if (input == null || input.size() == 0 || input.get(0) == null) {
            return Constants.getCriteriaName(this) + ": " + Constants.NODATA;
        }
        String hostname = (String) input.get(0);
        if (hostname.isEmpty()) {
            return Constants.getCriteriaName(this) + ": " + Constants.NODATA;
        }
        //String tld = findIfHostBelongsToNeighbouringCountry(hostname);
        String tld = findTLD(hostname);
        
        return (tld == null)? "C15: FAILED":"C15: " + tld;
    }

    private String findTLD(String hostname) {
        int tldbegin = hostname.lastIndexOf('.');
        if (tldbegin == -1) {
            return null;
        }
        String tld = hostname.substring(tldbegin, hostname.length());
        return tld;
    }

    public static String computeC15a(String hostname) {
        return findIfHostBelongsToNeighbouringCountry(hostname);      
    }
}
