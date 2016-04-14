package dk.kb.webdanica.criteria;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;

import dk.kb.webdanica.utils.Constants;
import dk.kb.webdanica.utils.TextUtils;

public class C7g_from_a_Nov extends EvalFunc<String>{

    @Override
    public String exec(Tuple input) throws IOException {
        if (input == null || input.size() == 0 || input.get(0) == null) {
            return Constants.getCriteriaName(this) + ": " + Constants.NODATA;
        }
        String text = (String) input.get(0);
     // FIXME Should be SearchWord, but this does not work
        Set<String> foundMatches = computeC7g(text);
        
        return (foundMatches.size() > 0? ("C7g: " 
        + TextUtils.conjoin("#", foundMatches)):
            "C7g: emptylist");
    }

    public static Set<String> computeC7g(String text) {
        return TextUtils.SearchWordRegExp(text, Words.danishMajorCitiesNov,false); 
    }    

    public static Set<String> computeC7gV2(String text) {
        return TextUtils.SearchWordPatterns(text, Words.patternsdanishMajorCitiesNov,false); 
    }    
    
    public static Set<String> computeC7gV3(String text) {
        return TextUtils.SearchWordPatterns(text, Words.patternsdanishMajorCitiesNovNoCase,false); 
    }    

    public static Set<String> computeC7gV5(Set<String> tokens) {
        List<String> words = Arrays.asList(Words.danishMajorCitiesNov);
        tokens.retainAll(words);
        return tokens; 
    }    

}
