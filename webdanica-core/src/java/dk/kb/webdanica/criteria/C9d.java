package dk.kb.webdanica.criteria;

import java.io.IOException;
import java.util.Set;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;

import dk.kb.webdanica.utils.Constants;
import dk.kb.webdanica.utils.TextUtils;


/**
 * Search for a CVR number in the html.
 * C9d: Input is the lowercase text.
 * indicators: "cvr nummer" "cvr-nummer" "cvr-nr"
 */
public class C9d extends EvalFunc<String>{

    
    public static String[] CVRIndicators = new String[]{"cvr", "cvr-nr", 
        "cvr-nummer", "cvr nummer"};
    
    @Override
    public String exec(Tuple input) throws IOException {
        if (input == null || input.size() == 0 || input.get(0) == null) {
            return Constants.getCriteriaName(this) + ": " + Constants.NODATA;
        }
        String text = (String) input.get(0);
        /*
        boolean foundMatch = TextUtils.PatternExists(text, 
                DanicaRegexps.danishCvrRegexp       
         );
        return (foundMatch? "C9d: y": "C9d: n");
        */
        
        Set<String> foundmatches = computeC9d(text);
        return (foundmatches.size() > 0)? "C9d: " + TextUtils.conjoin("#", foundmatches): "C9d: emptylist";  
        }

    public static Set<String> computeC9d(String text) {
        return TextUtils.SearchPattern(text, CVRIndicators);
    }        
    } 
