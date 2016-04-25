package dk.kb.webdanica.criteria;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;

import dk.kb.webdanica.utils.Constants;
import dk.kb.webdanica.utils.TextUtils;

public class C9e_from_b_Nov extends EvalFunc<String>{

    
    @Override
    public String exec(Tuple input) throws IOException {
        if (input == null || input.size() == 0 || input.get(0) == null) {
            return Constants.getCriteriaName(this) + ": " + Constants.NODATA;
        }
        String text = (String) input.get(0);
        Set<String> foundMatches = computeC9e(text);
        
        return (foundMatches.size() > 0? ("C9e: " 
        + TextUtils.conjoin("#", foundMatches)):
            "C9e: emptylist");
    }

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
}
