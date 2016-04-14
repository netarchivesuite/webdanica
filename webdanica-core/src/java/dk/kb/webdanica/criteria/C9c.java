package dk.kb.webdanica.criteria;

import java.io.IOException;
import java.util.Set;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;

import dk.kb.webdanica.utils.Constants;
import dk.kb.webdanica.utils.TextUtils;


/**
 * Search for Danish companies in the url.
 * C9c: Input is the lowercase URL.
 */
public class C9c extends EvalFunc<String>{

    @Override
    public String exec(Tuple input) throws IOException {
        if (input == null || input.size() == 0 || input.get(0) == null) {
            return Constants.getCriteriaName(this) + ": " + Constants.NODATA;
        }
        String text = (String) input.get(0);
        Set<String> foundMatches = computeC9c(text);
        
        return (foundMatches.size() > 0? ("C9c: " 
        + TextUtils.conjoin("#", foundMatches)):
            "C9c: emptylist");
    }

    public static Set<String> computeC9c(String urlLower) {
        return TextUtils.SearchPattern(urlLower, 
                Words.virksomheder_lowercased);
    }    
    
}
