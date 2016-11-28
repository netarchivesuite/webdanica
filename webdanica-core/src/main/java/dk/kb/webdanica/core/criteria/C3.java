package dk.kb.webdanica.core.criteria;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import dk.kb.webdanica.core.utils.TextUtils;

public class C3 {
    
    public static Set<String> ComputeC3a(String text) {
        return TextUtils.SearchPattern(
                text, DanicaRegexps.danishCharactersHtmlencoded);
        /* TODO: include uppercase */
    }
    
    //C3b search transformed frequent danish words
    // with danish characters, where ø is o/oe
    public static Set<String> ComputeC3b(String text) {
        return TextUtils.SearchPattern(
            text, FrequentWords.frequentwordsWithDanishLettersCoded);
    }
    
    //c3c: look urlencodede danishcaracters in the url
    public static Set<String> ComputeC3c(String urlLower) {
        return TextUtils.SearchPattern(
                urlLower, DanicaRegexps.danishCharactersUrlEncoded);
    }
    
    public static Set<String> ComputeC3d(String urlLower) {
		return TextUtils.SearchPattern(
				urlLower, FrequentWords.frequentwordsWithDanishLettersCoded);
	}
    
    
    //C3g - diverse varianter 
    public static Set<String> computeC3g(String text) {
        return TextUtils.SearchWordRegExp(
            text, FrequentWords.frequentwordsWithDanishLettersCodedNov, false);
    }
  
    public static Set<String> computeC3gV2(String text) {
        return TextUtils.SearchWordPatterns(text, FrequentWords.patternsFrequentwordsWithDanishLettersCodedNov,false); 
    }    

    public static Set<String> computeC3gV3(String text) {
        return TextUtils.SearchWordPatterns(text, FrequentWords.patternsFrequentwordsWithDanishLettersCodedNov,false); 
    }    
    
    public static Set<String> computeC3gV5(Set<String> tokens) {
        List<String> words = Arrays.asList(FrequentWords.frequentwordsWithDanishLettersCodedNov);
        tokens.retainAll(words);
        return tokens; 
} 
    
    
}
