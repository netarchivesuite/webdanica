package dk.kb.webdanica.core.criteria;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;

import dk.kb.webdanica.core.datamodel.criteria.CriteriaUtils;
import dk.kb.webdanica.core.utils.Constants;
import dk.kb.webdanica.core.utils.TextUtils;

public class C7a extends EvalFunc<String>{

    @Override
    public String exec(Tuple input) throws IOException {
        if (input == null || input.size() == 0 || input.get(0) == null) {
            return Constants.getCriteriaName(this) + ": " + Constants.NODATA;
        }
        String text = (String) input.get(0);
     // FIXME Should be SearchWord, but this does not work
        Set<String> foundMatches = computeC7a(text);
        
        return (foundMatches.size() > 0? ("C7a: " 
        + TextUtils.conjoin("#", foundMatches)):
            "C7a: emptylist");
    }

    public static Set<String> computeC7a(String text) {
        return TextUtils.SearchPattern(text, 
                Words.danishMajorCities);
    }
    
    public static Set<String> computeC7aOnCasedTokens(Set<String> tokens, File cityFile, StringBuilder error){
    	Set<String> emptyResultset = new HashSet<String>();
    	Set<String> words;
    	String defaultCharset = "UTF-16";
        try {
        	String charset = CriteriaUtils.findCharsetFromName(cityFile.getName());
        	if (charset==null){
        		error.append("Error during computeC7aOnCasedToken: Unable to deduce charset from filename '" +  cityFile.getName() + "'. Assuming default charset '"+ defaultCharset + "'");
        		charset = defaultCharset;
        	}
	        words = WordsArrayGenerator.generateWordSetFromFile(cityFile, charset, "\t", true, false);
	        tokens.retainAll(words);
        } catch (IOException e) {
        	error.append("Error during computeC7aOnCasedToken: " + e);
        	return emptyResultset;
        }
    	
    	return tokens; 
    }
}
