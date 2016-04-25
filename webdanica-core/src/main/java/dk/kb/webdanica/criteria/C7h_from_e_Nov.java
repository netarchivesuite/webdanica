package dk.kb.webdanica.criteria;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;

import dk.kb.webdanica.utils.Constants;
import dk.kb.webdanica.utils.TextUtils;

public class C7h_from_e_Nov extends EvalFunc<String>{
    @Override
    public String exec(Tuple input) throws IOException {
        if (input == null || input.size() == 0 || input.get(0) == null) {
            return Constants.getCriteriaName(this) + ": " + Constants.NODATA;
        }
        String text = (String) input.get(0);
        Set<String> foundMatches = computeC7h(text);
        
        return (foundMatches.size() > 0? ("C7h: " 
        + TextUtils.conjoin("#", foundMatches)):
            "C7h: emptylist");
    }

    public static Set<String> computeC7h(String text) {
        return TextUtils.SearchWordRegExp(text, Words.CapitalCountryTranslatedNov,true); 
    }

    public static Set<String> computeC7hV2(String text) {
        return TextUtils.SearchWordPatterns(text, Words.patternsCapitalCountryTranslatedNov,false); 
    }    

    public static Set<String> computeC7hV3(String text) {
        return TextUtils.SearchWordPatterns(text, Words.patternsCapitalCountryTranslatedNovNoCase,false); 
    }    

    public static Set<String> computeC7hV5(Set<String> tokens) {
        List<String> words = Arrays.asList(Words.CapitalCountryTranslatedNov);
        tokens.retainAll(words);
        return tokens; 
    }    
}
