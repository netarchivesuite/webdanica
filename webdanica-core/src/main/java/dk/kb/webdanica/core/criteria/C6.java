package dk.kb.webdanica.core.criteria;

import java.util.Set;

import dk.kb.webdanica.core.utils.TextUtils;

    public class C6 {
        
    	private final static String[] typicalDanishNotions = 
                new String[] {"dansk","danmark", "forening","/dk/", "/da/"};
       
    	public static Set<String> ComputeC6a(String text) {
            return TextUtils.findMatches(text, Words.getFrequentDanishWords());
            
        }
        
        public static Set<String> ComputeC6b(String text) {
            return TextUtils.SearchPattern(
                    text, new String[] {"dansk","danmark", "forening" });
        }    

        public static Set<String> ComputeC6c(String urlLower) {
            return TextUtils.SearchPattern(
                    urlLower, typicalDanishNotions);
        }    
        
        /////////// C6d - diverse varianter /////////////////////////
        
        public static Set<String> ComputeC6d(String text) {
            return TextUtils.SearchWordRegExp(
                    text, Words.getFrequentDanishWordsNov(), false); 
        }

        public static Set<String> ComputeC6dV2(String text) {
            return TextUtils.SearchWordPatterns(text, Words.patternsFrequentDanishWordsNov,false); 
        }    

        public static Set<String> ComputeC6dV3(String text) {
            return TextUtils.SearchWordPatterns(text, Words.patternsFrequentDanishWordsNovNoCase,false); 
        }    

        public static Set<String> computeC6dV5(Set<String> tokens) {
            tokens.retainAll( Words.getFrequentDanishWordsNov());
            return tokens; 
        }    
}
