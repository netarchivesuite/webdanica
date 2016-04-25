package dk.kb.webdanica.criteria;

import java.io.IOException;
import java.util.Set;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;

import dk.kb.webdanica.utils.Constants;
import dk.kb.webdanica.utils.TextUtils;

//C3b search transformed frequent danish words
// with danish characters, where ø is o/oe
public class C3b extends EvalFunc<String>{

    @Override
    public String exec(Tuple input) throws IOException {
    
    if (input == null || input.size() == 0 || input.get(0) == null) {
        return Constants.getCriteriaName(this) + ": " + Constants.NODATA;
    }
    String text = (String) input.get(0);
    /**Set<String> foundMatches = TextUtils.SearchWord(
            text, Words.frequentwordsWithDanishLettersCoded);
    */
    Set<String> foundMatches = TextUtils.SearchPattern(
            text, Words.frequentwordsWithDanishLettersCoded);
    String res = (foundMatches.size() != 0)? 
            ("C3b: " + TextUtils.conjoin("#", foundMatches)) : "C3b: emptylist";
    return res;
    
    }

    public static Set<String> ComputeC3b(String text) {
        return TextUtils.SearchPattern(
            text, Words.frequentwordsWithDanishLettersCoded);
    }

}
