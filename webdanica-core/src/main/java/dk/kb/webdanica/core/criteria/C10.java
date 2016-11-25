package dk.kb.webdanica.core.criteria;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dk.kb.webdanica.core.utils.TextUtils;

/**
 * C10a: Looks for typical patterns in Danish surnames.
 * Assumes input is lowercase
 * TODO Does not find "sen" names containing Danish characters.
 * 
 * 
 * 
 */
public class C10 {
   
   public static void main(String[] args) {
       String text = "Karen Sørensen Hansen Jespersen";
       String text2 = "Søren Carlsen " + text;
       
       String regexp = "\\p{Upper}.*sen";
       Set<String> foundMatches = TextUtils.SearchWordSuffixPatterns(text, DanicaRegexps.DanishSurnameSuffixes);
       System.out.println(foundMatches.size());
       
       String sampleText = text + "\n" + text2;
       String lowercased = sampleText.toLowerCase();
       
       foundMatches = findMatches(lowercased);
       System.out.println("matches in lowercased: " + foundMatches.size());
       String res = (foundMatches.size() != 0)? 
               ("C10a: " + TextUtils.conjoin("#", foundMatches)) : "C10a: emptylist";
       System.out.println(res);
       foundMatches = findMatches(sampleText);
       System.out.println("matches in non-lowercased: " + foundMatches.size());
       res = (foundMatches.size() != 0)? 
               ("C10a: " + TextUtils.conjoin("#", foundMatches)) : "C10a: emptylist";
       for (String s: foundMatches) {
    	   System.out.println("String '"+ s  + "' matches regexp: " + s.matches(regexp));
       }
               
       System.out.println(res);
       
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

    /**
     * Compute C10a
     *  Looks for typical patterns in Danish surnames.
     *
     */
    public static Set<String> computeC10a(String text) {
    	return findMatches(text);
    }

    /**
     * Compute C10b
     * Test for match of frequent Danish first names and surnames.
     *
     */
    public static Set<String> computeC10b(String text) {
    	return TextUtils.SearchPattern(text, Words.DanishNames);
    }    

    /**
     * C10c
     * Various test for match of frequent Danish first names and surnames.
     * 
     */
    
    
   public static Set<String> computeC10c(String text) {
       return TextUtils.SearchWordRegExp(text, Words.DanishNamesNov,false); 
   }    

   public static Set<String> computeC10cV2(String text) {
       return TextUtils.SearchWordPatterns(text, Words.patternsDanishNamesNov,false); 
   }    

   public static Set<String> computeC10cV3(String text) {
       return TextUtils.SearchWordPatterns(text, Words.patternsDanishNamesNovNoCase,false); 
   }    
  
   public static Set<String> computeC10cV5(Set<String> tokens) {
       List<String> words = Arrays.asList(Words.DanishNamesNov);
       tokens.retainAll(words);
       return tokens; 
   }    
   
  /* public static void main(String[] args) {
   	String text = "";
   	Set<String> f = computeC10c(text);
   	System.out.println("C10c V1 in '" + text + "': " + (f.size() > 0? ("C10c: " + TextUtils.conjoin("#", f)): "C10c: emptylist"));
   	f = computeC10cV2(text);
   	System.out.println("C10c V2 in '" + text + "': " + (f.size() > 0? ("C10c: " + TextUtils.conjoin("#", f)): "C10c: emptylist"));
   	f = computeC10cV3(text);
   	System.out.println("C10c V3 in '" + text + "': " + (f.size() > 0? ("C10c: " + TextUtils.conjoin("#", f)): "C10c: emptylist"));

   	text = "dfsf<s";
   	f = computeC10c(text);
   	System.out.println("C10c V1 in '" + text + "': " + (f.size() > 0? ("C10c: " + TextUtils.conjoin("#", f)): "C10c: emptylist"));
   	f = computeC10cV2(text);
   	System.out.println("C10c V2 in '" + text + "': " + (f.size() > 0? ("C10c: " + TextUtils.conjoin("#", f)): "C10c: emptylist"));
   	f = computeC10cV3(text);
   	System.out.println("C10c V3 in '" + text + "': " + (f.size() > 0? ("C10c: " + TextUtils.conjoin("#", f)): "C10c: emptylist"));

   	text = "pedersen, andersen, pedersen";
   	f = computeC10c(text);
   	System.out.println("C10c V1 in '" + text + "': " + (f.size() > 0? ("C10c: " + TextUtils.conjoin("#", f)): "C10c: emptylist"));
   	f = computeC10cV2(text);
   	System.out.println("C10c V2 in '" + text + "': " + (f.size() > 0? ("C10c: " + TextUtils.conjoin("#", f)): "C10c: emptylist"));
   	f = computeC10cV3(text);
   	System.out.println("C10c V3 in '" + text + "': " + (f.size() > 0? ("C10c: " + TextUtils.conjoin("#", f)): "C10c: emptylist"));
   	
   	text = "fdsfds pedersen, andersen, pedersen fasfasdf";
   	f = computeC10c(text);
   	System.out.println("C10c V1 in '" + text + "': " + (f.size() > 0? ("C10c: " + TextUtils.conjoin("#", f)): "C10c: emptylist"));
   	f = computeC10cV2(text);
   	System.out.println("C10c V2 in '" + text + "': " + (f.size() > 0? ("C10c: " + TextUtils.conjoin("#", f)): "C10c: emptylist"));
   	f = computeC10cV3(text);
   	System.out.println("C10c V3 in '" + text + "': " + (f.size() > 0? ("C10c: " + TextUtils.conjoin("#", f)): "C10c: emptylist"));
   	
   	text = "sdasf pedersen, andersen, pedersen";
   	f = computeC10c(text);
   	System.out.println("C10c V1 in '" + text + "': " + (f.size() > 0? ("C10c: " + TextUtils.conjoin("#", f)): "C10c: emptylist"));
   	f = computeC10cV2(text);
   	System.out.println("C10c V2 in '" + text + "': " + (f.size() > 0? ("C10c: " + TextUtils.conjoin("#", f)): "C10c: emptylist"));
   	f = computeC10cV3(text);
   	System.out.println("C10c V3 in '" + text + "': " + (f.size() > 0? ("C10c: " + TextUtils.conjoin("#", f)): "C10c: emptylist"));
   	
   	text = "pedersenfrawtrea";
   	f = computeC10c(text);
   	System.out.println("C10c V1 in '" + text + "': " + (f.size() > 0? ("C10c: " + TextUtils.conjoin("#", f)): "C10c: emptylist"));
   	f = computeC10cV2(text);
   	System.out.println("C10c V2 in '" + text + "': " + (f.size() > 0? ("C10c: " + TextUtils.conjoin("#", f)): "C10c: emptylist"));
   	f = computeC10cV3(text);
   	System.out.println("C10c V3 in '" + text + "': " + (f.size() > 0? ("C10c: " + TextUtils.conjoin("#", f)): "C10c: emptylist"));
   	
   	text = "Pedersen, Andersen";
   	f = computeC10c(text);
   	System.out.println("C10c V1 in '" + text + "': " + (f.size() > 0? ("C10c: " + TextUtils.conjoin("#", f)): "C10c: emptylist"));
   	f = computeC10cV2(text);
   	System.out.println("C10c V2 in '" + text + "': " + (f.size() > 0? ("C10c: " + TextUtils.conjoin("#", f)): "C10c: emptylist"));
   	f = computeC10cV3(text);
   	System.out.println("C10c V3 in '" + text + "': " + (f.size() > 0? ("C10c: " + TextUtils.conjoin("#", f)): "C10c: emptylist"));
   	
/*    	System.out.println("Nye ");
   	System.out.println("DanicaRegexps.pDanishNamesRegexp " + DanicaRegexps.danishNamesRegexp);
   	
   	text = "tlf. +45  fsdfsd  tlf fasfsd";
   	String w = "tlf";
       String testRe = "(?i)\\b(?:" + w + ")\\b";
       Pattern ptestRe = Pattern.compile(testRe);
       String[] p = ptestRe.split(text);
       String s = text.substring(text.length()-w.length());
       
       System.out.println("t: '" + text + "'");  
       System.out.println("t.split.len: " + p.length );  
       System.out.println("matches: '" + s.matches(testRe) + "'");  
//       System.out.println("t2: '" + ptestRe.matches(ptestRe, p[0]) );  
       
       /*Pattern p = Pattern.compile("\\d\\d\\d");
       Matcher m = p.matcher("a123b");
       System.out.println(m.find());
       System.out.println(m.matches());
       
       /*Pattern p = Pattern.compile("\\d\\d\\d");
       Matcher m = p.matcher("a123b");
       System.out.println(m.find());
       System.out.println(m.matches());
       
       //String testRe2 = "(?i)\\b(?:tlf)\\b";
       //Pattern ptestRe2 = Pattern.compile(testRe2);
       //Matcher m = ptestRe.matcher(text);
       //System.out.println(text + " --- " + m.find());
   } */

    
}
