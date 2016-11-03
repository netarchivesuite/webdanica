package dk.kb.webdanica.core.criteria;

import java.io.IOException;
import java.util.Set;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;

import dk.kb.webdanica.core.utils.Constants;
import dk.kb.webdanica.core.utils.TextUtils;

public class C3a extends EvalFunc<String>{

    @Override
    public String exec(Tuple input) throws IOException {
    
    if (input == null || input.size() == 0 || input.get(0) == null) {
        return Constants.getCriteriaName(this) + ": " + Constants.NODATA;
    }
    String text = (String) input.get(0);
    Set<String> foundMatches = ComputeC3a(text);
    
    String res = (foundMatches.size() != 0)? 
            ("C3a: " + TextUtils.conjoin("#", foundMatches)) : "C3a: emptylist";
    return res;
    
    }

    
    public static Set<String> ComputeC3a(String text) {
  
        return TextUtils.SearchPattern(
                text, DanicaRegexps.danishCharactersHtmlencoded);
        /* TODO: include uppercase */
    }
    
}
