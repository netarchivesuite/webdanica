package dk.kb.webdanica.core.criteria;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dk.kb.webdanica.core.datamodel.criteria.CriteriaUtils;
import dk.kb.webdanica.core.utils.TextUtils;

public class C7 {


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
	        words = WordsArrayGenerator.generateWordSetFromFile(cityFile, charset, "\t", true, false).get(0);
	        tokens.retainAll(words);
        } catch (IOException e) {
        	error.append("Error during computeC7aOnCasedToken: " + e);
        	return emptyResultset;
        }
    	
    	return tokens; 
    }
    
    public static Set<String> computeC7b(String urlLower) {
        return TextUtils.SearchPattern(urlLower, Words.danishMajorCities);
    }    
    
    public static Set<String> computeC7c(String text) {
        return TextUtils.SearchPattern(text, Words.placenames);
    }    
    
    public static Set<String> computeC7cAlt(String text,
            List<Set<String>> placeNamesTokenSet) {
		Set<String> resultSet = new HashSet<String>();
		for (Set<String> set: placeNamesTokenSet) {
			resultSet.addAll(TextUtils.SearchPattern(text,set));
		}
	    return resultSet;
    }
    
    public static Set<String> computeC7d(String urlLower) {
        return  TextUtils.SearchPattern(urlLower, 
                Words.placenames);
    }
    
    public static Set<String> computeC7e(String text) {
        return  TextUtils.SearchPattern(text,  Words.CapitalCountryTranslated);
    }
    
    public static Set<String> computeC7f(String urlLower) {
        return TextUtils.SearchPattern(urlLower, 
                Words.CapitalCountryTranslated);
    }

    //////////////////C7g - diverse varianter ////////////////////////////
    
    public static Set<String> computeC7g(String text) {
        return TextUtils.SearchWordRegExp(text, Words.danishMajorCitiesNov,false); 
    }    

    public static Set<String> computeC7gV2(String text) {
        return TextUtils.SearchWordPatterns(text, Words.patternsdanishMajorCitiesNov,false); 
    }    
    
    public static Set<String> computeC7gV3(String text) {
        return TextUtils.SearchWordPatterns(text, Words.patternsdanishMajorCitiesNovNoCase,false); 
    }    

    public static Set<String> computeC7gV5(Set<String> tokens) {
        List<String> words = Arrays.asList(Words.danishmajorcities);
        tokens.retainAll(words);
        return tokens; 
    }    
    //////////////////C7h - diverse varianter ////////////////////////////
    
    public static Set<String> computeC7h(String text) {
        return TextUtils.SearchWordRegExp(text, Words.CapitalCountryTranslatedNov,true); 
    }

    public static Set<String> computeC7hV2(String text) {
        return TextUtils.SearchWordPatterns(text, Words.patternsCapitalCountryTranslatedNov,false); 
    }    

    public static Set<String> computeC7hV3(String text) {
        return TextUtils.SearchWordPatterns(text, Words.patternsCapitalCountryTranslatedNovNoCase,false); 
    }    

    public static Set<String> computeC7hV5(Set<String> tokens) {
        //List<String> words = Arrays.asList(Words.CapitalCountryTranslatedNov);
        List<String> words = Arrays.asList(Words.CapitalCountryTranslated);
        tokens.retainAll(words);
        return tokens; 
    }

	public static Set<String> computeC7bAlt(String urlLower,
            List<Set<String>> cityFileTokenSet) {
		return TextUtils.SearchPattern(urlLower, cityFileTokenSet.get(0)); 
    }

	public static Set<String> computeC7gAlt(String text, Set<String> copyTokens,
            List<Set<String>> cityFileTokenSet) {
	    	Set<String> resultSet = new HashSet<String>();
	    Set<String> singleWords = cityFileTokenSet.get(0);
	    Set<String> doubleWords = cityFileTokenSet.get(1);
	    for (String doubleWord: doubleWords) {
	    	if (text.contains(doubleWord)) {
	    		resultSet.add(doubleWord);
	    	}
	    }
	    copyTokens.retainAll(singleWords);
	    resultSet.addAll(copyTokens);
	    return resultSet;
    }

	
	public static Set<String> computeC7dAlt(String urlLower,
            List<Set<String>> placeNamesTokenSet) {
		Set<String> resultSet = new HashSet<String>();
		for (Set<String> set: placeNamesTokenSet) {
			resultSet.addAll(TextUtils.SearchPattern(urlLower,set));
		}
	    return resultSet;
    }       
}
