package dk.kb.webdanica.core.criteria;

import java.util.Set;

import dk.kb.webdanica.core.utils.TextUtils;

/**
 * C5a. Look for characteristic danish words assumed not be in a Swedish or Norwegian text.
 * C5b. Look for norwegian words in the text.
 * 
 */
public class C5 {

	public static Set<String> computeC5a(String text) {
        return TextUtils.findMatches(text, FrequentWords.especiallyNormalDanishWords);        
    }
    
	public static Set<String> computeC5b(String text) {
        return TextUtils.findMatches(text, FrequentWords.notDanishWords);
       
    }
    
}
