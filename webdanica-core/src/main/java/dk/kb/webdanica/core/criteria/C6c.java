package dk.kb.webdanica.core.criteria;

import java.io.IOException;
import java.util.Set;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;

import dk.kb.webdanica.core.utils.Constants;
import dk.kb.webdanica.core.utils.TextUtils;

public class C6c extends EvalFunc<String>{

    private final static String[] typicalDanishNotions = 
             new String[] {"dansk","danmark", "forening","/dk/", "/da/"};
    
    
    @Override
    public String exec(Tuple input) throws IOException {
    
    if (input == null || input.size() == 0 || input.get(0) == null) {
        return Constants.getCriteriaName(this) + ": " + Constants.NODATA;
    }
    String text = (String)input.get(0);
    Set<String> foundMatches = ComputeC6c(text);
    
    String res = (foundMatches.size() != 0)? 
            ("C6c: " + TextUtils.conjoin("#", foundMatches)) : "C6c: emptylist";
    return res;
    
    }


    public static Set<String> ComputeC6c(String urlLower) {
        return TextUtils.SearchPattern(
                urlLower, typicalDanishNotions);
    }    
    

}
