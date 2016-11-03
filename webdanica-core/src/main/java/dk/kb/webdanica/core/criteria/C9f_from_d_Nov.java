package dk.kb.webdanica.core.criteria;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;

import dk.kb.webdanica.core.utils.Constants;

/**
 * Search for a CVR number in the html.
 * C9d: Input is the lowercase text.
 * indicators: "cvr nummer" "cvr-nummer" "cvr-nr"
 */
public class C9f_from_d_Nov extends EvalFunc<String>{
    @Override
    public String exec(Tuple input) throws IOException {
        if (input == null || input.size() == 0 || input.get(0) == null) {
            return Constants.getCriteriaName(this) + ": " + Constants.NODATA;
        }
        return (computeC9f((String) input.get(0))? "C9f:y" : "C9f:n");  
    }
    public static boolean computeC9f(String text) {
        Pattern pDanishCvrRegexp = Pattern.compile(DanicaRegexps.danishCvrRegexp);
        Matcher m = pDanishCvrRegexp.matcher(text);
    	return m.matches();
    }

    public static boolean computeC9fV2(String text) {
        Matcher m = DanicaRegexps.pDanishCvrRegexp.matcher(text);
    	return m.matches();
    }    

    public static boolean computeC9fV3(String text) {
        Matcher m = DanicaRegexps.pDanishCvrRegexpNoCase.matcher(text);
    	return m.matches();
    }    
}

