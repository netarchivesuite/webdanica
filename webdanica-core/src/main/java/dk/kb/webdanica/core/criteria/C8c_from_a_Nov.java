package dk.kb.webdanica.core.criteria;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;

import dk.kb.webdanica.core.utils.Constants;
import dk.kb.webdanica.core.utils.TextUtils;

public class C8c_from_a_Nov extends EvalFunc<String>{

    @Override
    public String exec(Tuple input) throws IOException {
        if (input == null || input.size() == 0 || input.get(0) == null) {
            return Constants.getCriteriaName(this) + ": " + Constants.NODATA;
        }
        String text = (String) input.get(0);
        Set<String> foundMatches = computeC8c(text);
        
        return (foundMatches.size() > 0? ("C8c: " + TextUtils.conjoin("#", foundMatches)):
            "C8c: emptylist");
    }

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
