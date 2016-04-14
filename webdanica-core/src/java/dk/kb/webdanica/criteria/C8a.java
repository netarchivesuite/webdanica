package dk.kb.webdanica.criteria;

import java.io.IOException;
import java.util.Set;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;

import dk.kb.webdanica.utils.Constants;
import dk.kb.webdanica.utils.TextUtils;

public class C8a extends EvalFunc<String>{

    @Override
    public String exec(Tuple input) throws IOException {
        if (input == null || input.size() == 0 || input.get(0) == null) {
            return Constants.getCriteriaName(this) + ": " + Constants.NODATA;
        }
        String text = (String) input.get(0);
     // FIXME Should be SearchWord, but this does not work
        /**
        Set<String> foundMatches = TextUtils.SearchWord(text, 
                Words.foreninger_lowercased);
        */
        
        Set<String> foundMatches = computeC8a(text);
        
        return (foundMatches.size() > 0? ("C8a: " + TextUtils.conjoin("#", foundMatches)):
            "C8a: emptylist");
    }

    public static Set<String> computeC8a(String text) {
        return TextUtils.SearchPattern(text, 
                Words.foreninger_lowercased);
    }    
}
