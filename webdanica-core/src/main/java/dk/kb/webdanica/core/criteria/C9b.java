package dk.kb.webdanica.core.criteria;

import java.io.IOException;
import java.util.Set;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;

import dk.kb.webdanica.core.utils.Constants;
import dk.kb.webdanica.core.utils.TextUtils;

public class C9b extends EvalFunc<String>{

    
    @Override
    public String exec(Tuple input) throws IOException {
        if (input == null || input.size() == 0 || input.get(0) == null) {
            return Constants.getCriteriaName(this) + ": " + Constants.NODATA;
        }
        String text = (String) input.get(0);
     // FIXME Should be SearchWord, but this does not work
        Set<String> foundMatches = computeC9b(text);
        
        return (foundMatches.size() > 0? ("C9b: " 
        + TextUtils.conjoin("#", foundMatches)):
            "C9b: emptylist");
    }

    public static Set<String> computeC9b(String text) {
        return TextUtils.SearchPattern(text, 
                Words.virksomheder_lowercased);
    }    
    
    
    
    
    
    
    
    
}
