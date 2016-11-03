package dk.kb.webdanica.core.criteria;

import java.io.IOException;
import java.util.Set;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;

import dk.kb.webdanica.core.utils.Constants;
import dk.kb.webdanica.core.utils.TextUtils;

/**
 * Match danish telephone-numbers prefixed by one of 
 * +45 0045 followed by eight digits. 
 * Test-method: 
 *  See if any of these danish telnumberIndicators are found in the text 
 *  TODO Still experimental
 */
public class C2 extends EvalFunc<String> {
    
    public static String[] telnumberIndicators = new String[]{"mobil +45", "tlf.", "+45", "0045"};
    
    @Override
    public String exec(Tuple input) throws IOException {
        if (input == null || input.size() == 0 || input.get(0) == null) {
            return Constants.getCriteriaName(this) + ": " + Constants.NODATA;
        }
        String text = (String) input.get(0);
        Set<String> matches = computeC2a(text);
        
        return (matches.size() > 0)? "C2: " 
            + TextUtils.conjoin("#", matches): "C2: emptylist";  

        //boolean matches = text.matches(DanicaRegexps.danishTlfRegexp);
        //return (matches? "C2: y": "C2: n");  
    }

    public static Set<String> computeC2a(String text) {
        return TextUtils.SearchPattern(text, telnumberIndicators);
    }
}
