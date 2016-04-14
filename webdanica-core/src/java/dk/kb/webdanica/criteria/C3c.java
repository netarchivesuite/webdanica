package dk.kb.webdanica.criteria;

import java.io.IOException;
import java.util.Set;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;

import dk.kb.webdanica.utils.Constants;
import dk.kb.webdanica.utils.TextUtils;

public class C3c extends EvalFunc<String>{

    @Override
    public String exec(Tuple input) throws IOException {
    
    if (input == null || input.size() == 0 || input.get(0) == null) {
        return Constants.getCriteriaName(this) + ": " + Constants.NODATA;
    }
    String text = (String) input.get(0);
    Set<String> foundMatches = ComputeC3c(text);
    
    String res = (foundMatches.size() != 0)? 
            ("C3c: " + TextUtils.conjoin("#", foundMatches)) : "C3c: emptylist";
    return res;
    
    }

    public static Set<String> ComputeC3c(String urlLower) {
        return TextUtils.SearchPattern(
                urlLower, DanicaRegexps.danishCharactersUrlEncoded);
    }
}
