package dk.kb.webdanica.core.criteria;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;

import dk.kb.webdanica.core.utils.Constants;
import dk.kb.webdanica.core.utils.TextUtils;


/**
 * Test for match of frequent Danish first names and surnames.
 *
 */
public class C10c_from_b_Nov extends EvalFunc<String>{
          
     @Override
     public String exec(Tuple input) throws IOException {
         if (input == null || input.size() == 0 || input.get(0) == null) {
             return Constants.getCriteriaName(this) + ": " + Constants.NODATA;
         }
         String text = (String) input.get(0);
         Set<String> foundMatches = computeC10c(text);
         
         return (foundMatches.size() > 0? ("C10c: " + TextUtils.conjoin("#", foundMatches)):
             "C10c: emptylist");
     }

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
//        System.out.println("t2: '" + ptestRe.matches(ptestRe, p[0]) );  
        
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
