package dk.kb.webdanica.core.criteria;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import dk.kb.webdanica.core.utils.TextUtils;

public class C8 {

	/**
     * C8a
     */
    public static Set<String> computeC8a(String text) {
    	// TODO Should probably be SearchWord as shown below, but this does not work
        /**
        return TextUtils.SearchWord(text, 
                Words.foreninger_lowercased);
        **/        
        return TextUtils.SearchPattern(text, 
                Words.foreninger_lowercased);
    }
    /**
     * C8b
     */ 
    public static Set<String> computeC8b(String url) {
    	return TextUtils.SearchPattern(url, 
    			Words.foreninger_lowercased);
    }     
    //////////////// C8C - diverse varianter ///////////////////
    public static Set<String> computeC8c(String text) {
        return TextUtils.SearchWordRegExp(text, Words.foreninger_lowercased,false); 
    }    

    public static Set<String> computeC8cV2(String text) {
        return TextUtils.SearchWordPatterns(text, Words.patternsForeninger_lowercased,false); 
    }    

    public static Set<String> computeC8cV3(String text) {
        return TextUtils.SearchWordPatterns(text, Words.patternsForeninger_lowercasedNoCase,false); 
    }    

    public static Set<String> computeC8cV5(Set<String> tokens) {
        List<String> words = Arrays.asList(Words.foreninger_lowercased);
        tokens.retainAll(words);
        return tokens; 
    }    
}
