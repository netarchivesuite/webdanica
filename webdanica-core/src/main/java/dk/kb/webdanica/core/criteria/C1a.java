package dk.kb.webdanica.core.criteria;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.pig.EvalFunc;
import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.Tuple;

import dk.kb.webdanica.core.utils.Constants;

/**
 * C1a: search for .dk mailadresse in outlinks data
 * 
 * return: y/n
 */
public class C1a extends EvalFunc<String>{

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
        Set<String> foundDKMaillinks = computeC1a(links);
        
        return "C1a: found " + foundDKMaillinks.size();
    }
    
    public static Set<String> computeC1a(DataBag links) throws ExecException {
        Set<String> foundDKMaillinks = new HashSet<String>();
        if (links != null) {
            Iterator<Tuple> i = links.iterator();
            while (i.hasNext()) {
                Tuple o = i.next();
                Map o1 = (Map) o.get(0);
                String url = ((String) o1.get("url")).toLowerCase();
                if (url.startsWith(MAILTO)) {
                    // remove mailto.
                    //System.out.println(url);
                    String mailA = url.substring(MAILTO.length() + 1, url.length());
                    if (mailA.matches(DanicaRegexps.danisheMail)) {
                        foundDKMaillinks.add(mailA);
                    }
                }
            }
        }
        return foundDKMaillinks;
       
    }
    
}
