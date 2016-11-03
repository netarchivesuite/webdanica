package dk.kb.webdanica.core.criteria;

import java.io.IOException;
import java.util.Set;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;

import dk.kb.webdanica.core.utils.Constants;
import dk.kb.webdanica.core.utils.TextUtils;

public class C7c extends EvalFunc<String>{

    @Override
    public String exec(Tuple input) throws IOException {
        if (input == null || input.size() == 0 || input.get(0) == null) {
            return Constants.getCriteriaName(this) + ": " + Constants.NODATA;
        }
        String text = (String) input.get(0);
     // FIXME Should be SearchWordSuffixPatterns, but this does not work
        Set<String> foundMatches = computeC7c(text);
        
        return (foundMatches.size() > 0? ("C7c: " 
        + TextUtils.conjoin("#", foundMatches)):
            "C7c: emptylist");
    }

    public static Set<String> computeC7c(String text) {
        return TextUtils.SearchPattern(text, Words.placenamesuffixes);
    }    

}
