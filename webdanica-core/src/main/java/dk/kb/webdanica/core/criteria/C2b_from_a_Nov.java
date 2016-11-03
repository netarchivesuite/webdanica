package dk.kb.webdanica.core.criteria;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;

import dk.kb.webdanica.core.utils.Constants;

/**
 * Match danish telephone-numbers prefixed by one of 
 * +45 0045 followed by eight digits. 
 * Test-method: 
 *  See if any of these danish telnumberIndicators are found in the text 
 */
public class C2b_from_a_Nov extends EvalFunc<String> {
    
    public static String[] telnumberIndicators = new String[]{"mobil +45", "tlf.", "+45", "0045"};
    
    @Override
    public String exec(Tuple input) throws IOException {
        if (input == null || input.size() == 0 || input.get(0) == null) {
            return Constants.getCriteriaName(this) + ": " + Constants.NODATA;
        }
        return (computeC2b((String) input.get(0))? "C2b:y" : "C2b:n");  
    }
    
    public static boolean computeC2b(String text) {
        Pattern pDanishTlfRegexp = Pattern.compile(DanicaRegexps.danishTlfRegexp);
        Matcher m = pDanishTlfRegexp.matcher(text);
    	return m.matches();
    }

    public static boolean computeC2bV2(String text) {
        Matcher m = DanicaRegexps.pDanishTlfRegexp.matcher(text);
    	return m.matches();
    }    
    
    public static boolean computeC2bV3(String text) {
        Matcher m = DanicaRegexps.pDanishTlfRegexpNoCase.matcher(text);
    	return m.matches();
    }    
    
    /*public static void main(String[] args) {
    	System.out.println("DanicaRegexps " + DanicaRegexps.danishTlfRegexp);
    	String text = "dfsf<s";
        System.out.println("t: '" + text + "': " + DanicaRegexps.pDanishTlfRegexp.matcher(text).matches());  
    	text = "tlf. +45 40 43 11 00";
        System.out.println("t: '" + text + "': " + DanicaRegexps.pDanishTlfRegexp.matcher(text).matches());  
    }*/
}
