package dk.kb.webdanica.criteria;

import java.io.IOException;
import java.util.Set;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;

import dk.kb.webdanica.utils.Constants;
import dk.kb.webdanica.utils.TextUtils;

/**
 * C9a. search for "a/s" and "aps" in the text.
 * 
 */
public class C9a extends EvalFunc<String>{

    @Override
    public String exec(Tuple input) throws IOException {
        if (input == null || input.size() == 0 || input.get(0) == null) {
            return Constants.getCriteriaName(this) + ": " + Constants.NODATA;
        }
        String text = (String) input.get(0);
        // WordSearch does not work
        /* 
         Set<String> foundMatches = TextUtils.SearchWord(text, 
                Words.aktieselskabNames );
        */        
        
        Set<String> foundMatches = computeC9a(text);
        
        return (foundMatches.size() > 0? ("C9a: " 
        + TextUtils.conjoin("#", foundMatches)):
            "C9a: emptylist");
    }

    public static Set<String> computeC9a(String text) {
        return TextUtils.findMatches(text, Words.aktieselskabNames);
    }    
}
