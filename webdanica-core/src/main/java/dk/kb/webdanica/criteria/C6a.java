package dk.kb.webdanica.criteria;

import java.io.IOException;
import java.util.Set;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;

import dk.kb.webdanica.utils.Constants;
import dk.kb.webdanica.utils.TextUtils;

    public class C6a extends EvalFunc<String>{
        
        @Override
        public String exec(Tuple input) throws IOException {
            if (input == null || input.size() == 0 || input.get(0) == null) {
                return Constants.getCriteriaName(this) + ": " + Constants.NODATA;
            }
            String text = (String) input.get(0);
            // FIXME Should be SearchWord, but this does not work
            
            Set<String> foundMatches = ComputeC6a(text);
            
            return (foundMatches.size() > 0? ("C6a: " + foundMatches.size() + " hits:" 
            + TextUtils.conjoin("#", foundMatches)):
                "C6a: emptylist");
        }

        public static Set<String> ComputeC6a(String text) {
            return TextUtils.findMatches(text, Words.getFrequentDanishWords());
            
        }
        
}
