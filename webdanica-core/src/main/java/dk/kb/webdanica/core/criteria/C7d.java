package dk.kb.webdanica.core.criteria;

import java.io.IOException;
import java.util.Set;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;

import dk.kb.webdanica.core.utils.Constants;
import dk.kb.webdanica.core.utils.TextUtils;

public class C7d extends EvalFunc<String>{

       
    @Override
    public String exec(Tuple input) throws IOException {
        if (input == null || input.size() == 0 || input.get(0) == null) {
            return Constants.getCriteriaName(this) + ": " + Constants.NODATA;
        }
        String text = (String) input.get(0);
        Set<String> foundMatches = computeC7d(text);
        
        return (foundMatches.size() > 0? ("C7d: " 
        + TextUtils.conjoin("#", foundMatches)):
            "C7d: emptylist");
    }

    public static Set<String> computeC7d(String urlLower) {
        return  TextUtils.SearchPattern(urlLower, 
                Words.placenamesuffixes);
    }
    
}
