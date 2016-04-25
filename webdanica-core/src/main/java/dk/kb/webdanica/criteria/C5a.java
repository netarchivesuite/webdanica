package dk.kb.webdanica.criteria;

import java.io.IOException;
import java.util.Set;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;

import dk.kb.webdanica.utils.Constants;
import dk.kb.webdanica.utils.TextUtils;

public class C5a extends EvalFunc<String>{

    @Override
    public String exec(Tuple input) throws IOException {
    
    if (input == null || input.size() == 0 || input.get(0) == null) {
        return Constants.getCriteriaName(this) + ": " + Constants.NODATA;
    }
    String text = (String) input.get(0);
    //System.out.println("the text is: "+ text);
    // Should work, but doesn't
    //Set<String> foundMatches = TextUtils.SearchWordPattern(
    //        text, Words.especiallyNormalDanishWords)
    Set<String> foundMatches = computeC5a(text);
    
    String res = (foundMatches.size() != 0)? 
    ("C5a: " + TextUtils.conjoin("#", foundMatches)) : "C5a: emptylist";
    return res;
    
    }
    
    public static Set<String> computeC5a(String text) {
        return TextUtils.findMatches(text, Words.especiallyNormalDanishWords);        
    }
    
    
}
