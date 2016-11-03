package dk.kb.webdanica.core.criteria;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;

import dk.kb.webdanica.core.utils.Constants;
import dk.kb.webdanica.core.utils.TextUtils;

//C3b search transformed frequent danish words
// with danish characters, where ø is o/oe
public class C3g_fom_b_Nov extends EvalFunc<String>{

    @Override
    public String exec(Tuple input) throws IOException {
    
    if (input == null || input.size() == 0 || input.get(0) == null) {
        return Constants.getCriteriaName(this) + ": " + Constants.NODATA;
    }
    String text = (String) input.get(0);
    Set<String> foundMatches = computeC3g(text);
    String res = (foundMatches.size() != 0)? 
            ("C3g: " + TextUtils.conjoin("#", foundMatches)) : "C3g: emptylist";
    return res;
    
    }

    public static Set<String> computeC3g(String text) {
        return TextUtils.SearchWordRegExp(
            text, Words.frequentwordsWithDanishLettersCodedNov, false);
    }

    public static Set<String> computeC3gV2(String text) {
        return TextUtils.SearchWordPatterns(text, Words.patternsFrequentwordsWithDanishLettersCodedNov,false); 
    }    

    public static Set<String> computeC3gV3(String text) {
        return TextUtils.SearchWordPatterns(text, Words.patternsFrequentwordsWithDanishLettersCodedNov,false); 
    }    
    
    public static Set<String> computeC3gV5(Set<String> tokens) {
        List<String> words = Arrays.asList(Words.frequentwordsWithDanishLettersCodedNov);
        tokens.retainAll(words);
        return tokens; 
    }    
}
