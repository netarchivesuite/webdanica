package dk.kb.webdanica.core.criteria;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pig.EvalFunc;
import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.Tuple;

import dk.kb.webdanica.core.datamodel.criteria.CriteriaUtils;
import dk.kb.webdanica.core.Constants;
import dk.kb.webdanica.core.utils.TextUtils;

/** 
 * C1. Try to match with a danish email address in the text (obsolete)
 * 
 * C1a: search for .dk mailadresse in outlinks data
 * 
 * return: y/n
 */
public class C1 extends EvalFunc<String>{

	public final static String URL_KEY = "url";
    public final static String TEXT_KEY = "text";
    public final static String MAILTO = "mailto";
    public final static String DK_SUFFIX = ".dk";    
	
	
    @Override
    public String exec(Tuple input) throws IOException {
       
        if (input == null || input.size() == 0 || input.get(0) == null) {
            return CriteriaUtils.getCriteriaName(this) + ": " + Constants.NODATA;
        }
        String text = (String) input.get(0);
        Set<String> words = TextUtils.tokenizeText(text);
        for ( String word: words) {
            if (word.matches(DanicaRegexps.danisheMail)) {
                return "C1: y";
            }
        }
        //boolean matches = text.matches(DanicaRegexps.danisheMail);
        //return (matches? "C1: y": "C1: n");
        return "C1: n";
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
    
    // alternative match 
    public static Set<String> computeC1(String text) {
    	 Set<String> foundDKMail = new HashSet<String>();
    	 
    	Pattern p = Pattern.compile(
    			DanicaRegexps.danisheMail, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    	Matcher m = p.matcher(text);
    	while (m.find()) {
    		foundDKMail.add(m.group(0));
    	}
    	return foundDKMail;		 
    }

	public static Set<String> computeC1a(List<String> links) {
		Set<String> foundDKMaillinks = new HashSet<String>();
		if (links == null) {
			return foundDKMaillinks;
		}
		for (String link: links) {
			if (link == null) {
				System.out.println("Found null link");
				continue;
			}
			String url = link.toLowerCase();
            if (url.startsWith(MAILTO)) {
                String mailA = url.substring(MAILTO.length() + 1, url.length());
                if (mailA.matches(DanicaRegexps.danisheMail)) {
                    foundDKMaillinks.add(mailA);
                }
            }
		}
	    return foundDKMaillinks;
    }
}
