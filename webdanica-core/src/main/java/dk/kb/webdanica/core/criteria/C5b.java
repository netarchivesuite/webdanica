package dk.kb.webdanica.core.criteria;

import java.io.IOException;
import java.util.Set;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;

import dk.kb.webdanica.core.utils.Constants;
import dk.kb.webdanica.core.utils.TextUtils;

/**
 * C5b. Look for norwegian words in the text.
 *
 */
public class C5b extends EvalFunc<String>{


    @Override
    public String exec(Tuple input) throws IOException {
    
    if (input == null || input.size() == 0 || input.get(0) == null) {
        return Constants.getCriteriaName(this) + ": " + Constants.NODATA;
    }
    
    String text = (String) input.get(0);
    // Does not work
    //Set<String> foundMatches = TextUtils.SearchWord(
    //        text, Words.notDanishWords);
    Set<String> foundMatches = computeC5b(text); 
    String res = (foundMatches.size() != 0)? 
            ("C5b: " + TextUtils.conjoin("#", foundMatches)) : "C5b: emptylist"; 
    return res;
    }
    
    public static Set<String> computeC5b(String text) {
        return TextUtils.findMatches(text, Words.notDanishWords);
       
    }
    
}
