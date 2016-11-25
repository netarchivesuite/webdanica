package dk.kb.webdanica.core.criteria;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.kb.webdanica.core.utils.TextUtils;

/**
 * C9a. Search for "a/s" and "aps" in the text.
 * C9b. Search for Danish companies in the text
 * C9c. Search for Danish companies in the url.
 * C9d: Search for a CVR number in the text. Input is the lowercase text.
 * 	using indicators: "cvr nummer" "cvr-nummer" "cvr-nr"
 * C9e: search for lowercase company-names
 * C9f:Search for a CVR nummer using a regexp. currently using: 
 * 	DanicaRegexps.danishCvrRegexp

 */
public class C9 {
    
	 public static String[] CVRIndicators = new String[]{"cvr", "cvr-nr", 
	        "cvr-nummer", "cvr nummer"};
	
    // C9a. search for "a/s" and "aps" in the text.
    public static Set<String> computeC9a(String text) {
        return TextUtils.findMatches(text, Words.aktieselskabNames);
    } 
    
    public static Set<String> computeC9b(String text) {
        return TextUtils.SearchPattern(text, 
                Words.virksomheder_lowercased);
    }
    /**
     * Search for Danish companies in the url.
     * C9c: Input is the lowercase URL.
     * @return the companies matched if any
     */
    public static Set<String> computeC9c(String urlLower) {
        return TextUtils.SearchPattern(urlLower,
        		Words.virksomheder_one_word_lowercased
                //Words.virksomheder_lowercased
        );
    }  
    
    public static Set<String> computeC9d(String text) {
        return TextUtils.SearchPattern(text, CVRIndicators);
    }
    
    ////////////// C9e - diverse varianter
    public static Set<String> computeC9e(String text) {
        return TextUtils.SearchWordRegExp(text, Words.virksomheder_lowercased,false); 
    }    
    
    public static Set<String> computeC9eV2(String text) {
        return TextUtils.SearchWordPatterns(text, Words.patternsVirksomheder_lowercased,false); 
    }    
    
    public static Set<String> computeC9eV3(String text) {
        return TextUtils.SearchWordPatterns(text, Words.patternsVirksomheder_lowercasedNoCase,false); 
    }    

    public static Set<String> computeC9eV5(Set<String> tokens) {
        List<String> words = Arrays.asList(Words.virksomheder_lowercased);
        tokens.retainAll(words);
        return tokens; 
    }    
    
    //////////////C9f - diverse varianter
    public static boolean computeC9f(String text) {
        Pattern pDanishCvrRegexp = Pattern.compile(DanicaRegexps.danishCvrRegexp);
        Matcher m = pDanishCvrRegexp.matcher(text);
    	return m.matches();
    }

    public static boolean computeC9fV2(String text) {
        Matcher m = DanicaRegexps.pDanishCvrRegexp.matcher(text);
    	return m.matches();
    }    

    public static boolean computeC9fV3(String text) {
        Matcher m = DanicaRegexps.pDanishCvrRegexpNoCase.matcher(text);
    	return m.matches();
    }    
    
}


