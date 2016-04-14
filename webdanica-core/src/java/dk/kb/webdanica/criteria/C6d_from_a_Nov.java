package dk.kb.webdanica.criteria;

import java.io.IOException;
import java.util.Set;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;

import dk.kb.webdanica.utils.Constants;
import dk.kb.webdanica.utils.TextUtils;

    public class C6d_from_a_Nov extends EvalFunc<String>{
        
        @Override
        public String exec(Tuple input) throws IOException {
            if (input == null || input.size() == 0 || input.get(0) == null) {
                return Constants.getCriteriaName(this) + ": " + Constants.NODATA;
            }
            String text = (String) input.get(0);
            Set<String> foundMatches = ComputeC6d(text);
            
            return (foundMatches.size() > 0? ("C6d: " + foundMatches.size() + " hits:" 
            + TextUtils.conjoin("#", foundMatches)):
                "C6d: emptylist");
        }

        public static Set<String> ComputeC6d(String text) {
            return TextUtils.SearchWordRegExp(
                    text, Words.getFrequentDanishWordsNov(), false);
            
        }

        public static Set<String> ComputeC6dV2(String text) {
            return TextUtils.SearchWordPatterns(text, Words.patternsFrequentDanishWordsNov,false); 
        }    

        public static Set<String> ComputeC6dV3(String text) {
            return TextUtils.SearchWordPatterns(text, Words.patternsFrequentDanishWordsNovNoCase,false); 
        }    

        public static Set<String> computeC6dV5(Set<String> tokens) {
            tokens.retainAll( Words.getFrequentDanishWordsNov());
            return tokens; 
        }    

    }    
