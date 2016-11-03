package dk.kb.webdanica.core.criteria;

import java.io.IOException;
import java.util.Set;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;

import dk.kb.webdanica.core.utils.Constants;
import dk.kb.webdanica.core.utils.TextUtils;

public class C8b extends EvalFunc<String>{


    @Override
    public String exec(Tuple input) throws IOException {
        if (input == null || input.size() == 0 || input.get(0) == null) {
            return Constants.getCriteriaName(this) + ": " + Constants.NODATA;
        }
        String text = (String) input.get(0);
        Set<String> foundMatches = computeC8b(text);

        return (foundMatches.size() > 0? ("C8b: " 
                + TextUtils.conjoin("#", foundMatches)):
                "C8b: emptylist");
    }

    public static Set<String> computeC8b(String text) {
        return TextUtils.SearchPattern(text, 
                Words.foreninger_lowercased);
    }    

}
