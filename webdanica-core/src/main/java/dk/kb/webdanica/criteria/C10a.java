package dk.kb.webdanica.criteria;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;

import dk.kb.webdanica.utils.Constants;
import dk.kb.webdanica.utils.TextUtils;

/**
 * C10a: Looks for typical patterns in Danish surnames.
 * Assumes input is lowercase
 * TODO Does not find "sen" names containing Danish characters.
 */
public class C10a extends EvalFunc<String>{
 
    
    
   public static void main(String[] args) {
       String text = "Karen Sørensen";
               String text2 = "Søren Carlsen";
               
       Set<String> foundMatches = TextUtils.SearchWordSuffixPatterns(text, DanicaRegexps.DanishSurnameSuffixes);
       System.out.println(foundMatches.size());
       
       foundMatches = findMatches(text + "\n" + text2);
       String res = (foundMatches.size() != 0)? 
               ("C10a: " + TextUtils.conjoin("#", foundMatches)) : "C10a: emptylist";
       System.out.println(res);
       
   }
    
    
    @Override
    public String exec(Tuple input) throws IOException {
        if (input == null || input.size() == 0 || input.get(0) == null) {
            return Constants.getCriteriaName(this) + ": " + Constants.NODATA;
        }
        
        String text = (String) input.get(0);
        
        Set<String> foundMatches = findMatches(text);
        // Does not work.
        //Set<String> foundMatches = TextUtils.SearchWordSuffixPatterns(text, DanicaRegexps.DanishSurnameSuffixes);
        
        String res = (foundMatches.size() != 0)? 
                ("C10a: " + TextUtils.conjoin("#", foundMatches)) : "C10a: emptylist";
        return res;
    }
    
    public static Set<String> findMatches(String text) {
        Set<String> tokens = TextUtils.tokenizeText(text);
        Set<String> foundMatches = new HashSet<String>();
        String regexp = "[\\u00E6\\u00E5\\u00C6\\u00C5\\u00F8\\u00D8\\w]+" + "sen";
        //String regexp = ".*\\b[\\w\\u00E6\\u00E5\\u00C6\\u00C5\\u00F8\\u00D8]+" + "sen" + "\\b.*";
        Set<String> badwords = Words.getBadSenWords();
        for (String word: tokens) {
            //System.out.println(word);
            if (word.matches(regexp) && !badwords.contains(word)) {
                foundMatches.add(word);
            }
        }
        return foundMatches;
    }


    public static Set<String> computeC10a(String text) {
        return findMatches(text);
    }
}