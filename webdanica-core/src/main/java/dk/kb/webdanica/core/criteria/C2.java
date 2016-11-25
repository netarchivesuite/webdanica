package dk.kb.webdanica.core.criteria;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.kb.webdanica.core.utils.TextUtils;

/**
 * Match danish telephone-numbers prefixed by one of 
 * +45 0045 followed by eight digits. 
 * Test-method: 
 *  See if any of these danish telnumberIndicators are found in the text 
 */
public class C2 {
    
    public static String[] telnumberIndicators = new String[]{"mobil +45", "tlf.", "+45", "0045"};
    
    // C2a. Look for the telnumberIndicators
    public static Set<String> computeC2a(String text) {
        return TextUtils.SearchPattern(text, telnumberIndicators);
    }
    
    // C2b. Try to find a Danish telefonnumber in the text based on a regular expression
    // version 1
    public static boolean computeC2b(String text) {
        Pattern pDanishTlfRegexp = Pattern.compile(DanicaRegexps.danishTlfRegexp);
        Matcher m = pDanishTlfRegexp.matcher(text);
    	return m.matches();
    }
    // C2b. Try to find a Danish telefonnumber in the text based on a regular expression
    // version 2
    public static boolean computeC2bV2(String text) {
        Matcher m = DanicaRegexps.pDanishTlfRegexp.matcher(text);
    	return m.matches();
    }    
 
    // C2b. Try to find a Danish telefonnumber in the text based on a regular expression
    // version 3    
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
