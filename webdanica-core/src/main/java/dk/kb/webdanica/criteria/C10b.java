package dk.kb.webdanica.criteria;

import java.io.IOException;
import java.util.Set;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;

import dk.kb.webdanica.utils.Constants;
import dk.kb.webdanica.utils.TextUtils;


/**
 * Test for match of frequent Danish first names and surnames.
 *
 */
public class C10b extends EvalFunc<String>{
          
     @Override
     public String exec(Tuple input) throws IOException {
         if (input == null || input.size() == 0 || input.get(0) == null) {
             return Constants.getCriteriaName(this) + ": " + Constants.NODATA;
         }
         String text = (String) input.get(0);
      // FIXME Should be SearchWord, but this does not work
         Set<String> foundMatches = computeC10b(text);
         
         return (foundMatches.size() > 0? ("C10b: " + TextUtils.conjoin("#", foundMatches)):
             "C10b: emptylist");
     }

    public static Set<String> computeC10b(String text) {
        return TextUtils.SearchPattern(text, Words.DanishNames);
    }    
   
}
