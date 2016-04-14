package dk.kb.webdanica.criteria;

import java.io.IOException;
import java.util.Set;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;


import dk.kb.webdanica.utils.TextUtils;
import dk.kb.webdanica.utils.Constants;;

/** 
 * C1. Try to match with a danish email address.
 *
 */
public class C1 extends EvalFunc<String>{

    @Override
    public String exec(Tuple input) throws IOException {
       
        if (input == null || input.size() == 0 || input.get(0) == null) {
            return Constants.getCriteriaName(this) + ": " + Constants.NODATA;
        }
        String text = (String) input.get(0);
        Set<String> words = TextUtils.tokenizeText(text);
        for ( String word: words) {
            if (word.matches(DanicaRegexps.danisheMail)) {
                return "C1: y";
            }
        }
        //boolean matches = text.matches(DanicaRegexps.danisheMail);
        //return (matches? "C1: y": "C1: n");
        return "C1: n";
    }

}
