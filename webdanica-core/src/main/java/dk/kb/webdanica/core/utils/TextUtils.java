package dk.kb.webdanica.core.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.kb.webdanica.core.criteria.Words;

public class TextUtils {

    
    public static Set<String> SearchWord(String text, String[] words) {
        Set<String> matches = new HashSet<String>();
        for (String word: words) {
            if (text.matches(toRegexp(word))) {
                matches.add(word);
            }
        }
        return matches;
    }

    public static Set<String> SearchWordRegExp(String text, Collection<String> words, boolean withCount) {
        Set<String> res = new HashSet<String>();
        for (String word: words) {
            String testRe = "(?i)\\b(?:" + word + ")\\b";
            Pattern ptestRe = Pattern.compile(testRe);
        	if (ptestRe.matcher(text).find()) {
                if (withCount) {
	                int cnt = 0; 
		            String[] p = ptestRe.split(text);
		            String s = text.substring(text.length()-word.length());
		            cnt = p.length - (s.matches(testRe)?0:1);
		            if (cnt>0) {
		            	res.add(cnt + ";" + word);
		            }
	            } else {
	            	res.add(word);
	            }
            }
        }
        return res;
    }

    public static Set<String> SearchWordRegExp(String text, String[] words, boolean withCount) {
        Set<String> res = new HashSet<String>();
        for (String word: words) {
            String testRe = "(?i)\\b(?:" + word + ")\\b";
            Pattern ptestRe = Pattern.compile(testRe);
        	if (ptestRe.matcher(text).find()) {
                if (withCount) {
	                int cnt = 0; 
		            String[] p = ptestRe.split(text);
		            cnt = p.length - (p[p.length-1].matches(testRe)?0:1);
		            if (cnt>0) {
		            	res.add(cnt + ";" + word);
		            }
	            } else {
	            	res.add(word);
	            }
            }
        }
        return res;
    }

    public static Set<String> SearchWordPatterns(String text, Set<Words.WordPattern> wpts, boolean withCount) {
        Set<String> res = new HashSet<String>();
        for (Words.WordPattern wp: wpts) {
        	if (wp.p.matcher(text).find()) {
        		if (withCount) {
	                int cnt = 0; 
		            String[] pl = wp.p.split(text);
		            cnt = pl.length - (wp.p.matcher(pl[pl.length-1]).find()?0:1);
		            res.add(cnt + ";" + wp.w);
	            } else {
	            	res.add(wp.w);
	            }
            }
        }
        return res;
    }

    private static String toRegexp(String word) {
      //String res = "\\s*\\b" + word + "\\b\\s*";
      //before November: String res = "\\s+" + word + "\\s+";
    	String res = ".*(\\b)(" + word + ")(\\b).*" ;
        
      //System.out.println("PATTERN = '" + res + "' ");
      return res;
      //return " " + word + " ";
    }
    
    public static <T> String conjoin(String sep, Collection<T> objects
            ) {
        if (objects == null) {
            return null;
        }
        StringBuilder res = new StringBuilder();
        for (T o : objects) {
            if (res.length() != 0) {
                res.append(sep);
            }
            res.append(o);
        }
        return res.toString();
    }

    public static Set<String> SearchPattern(String text,
            String[] patterns) {
        Set<String> matches = new HashSet<String>();
        for (String pattern: patterns) {
            if (text.contains(pattern)) {
                matches.add(pattern);
            }
        }
        return matches;
    }
    
    public static Set<String> SearchWordSuffixPatterns(String text,
            String[] patterns) {
        Set<String> matches = new HashSet<String>();
        for (String pattern: patterns) {
            
            if (text.matches(".*\\b[\\w\\u00E6\\u00E5\\u00C6\\u00C5\\u00F8\\u00D8]+" + pattern + "\\b.*")) {
                matches.add(pattern);
            }
        }
        return matches;
    }

    public static Set<String> SearchWord(String text, List<String> words) {
        Set<String> matches = new HashSet<String>();
        for (String word: words) {
            //System.out.println("Trying to match - " + word);
            if (text.matches(toRegexp(word))) {
                matches.add(word);
            }
        }
        //System.out.println("matches found: " + matches.size());
        return matches;

    }

    public static boolean PatternExists(String text, String pattern) {
        return text.matches(pattern);
    }
    
    public static Set<String> SearchPattern(String text,
            Collection<String> patterns) {
        Set<String> matches = new HashSet<String>();
        for (String pattern: patterns) {
            if (text.contains(pattern)) {
                matches.add(pattern);
            }
            /*if (text.matches(".*" + pattern + ".*")) {
                matches.add(pattern);
            }*/
        }
        return matches;
    }

    public static Set<String> SearchWordPattern(String text,
            String[] especiallyNormalDanishWords) {
        Set<String> matches = new HashSet<String>();
        for (String pattern: especiallyNormalDanishWords) {
            Pattern p = Pattern.compile("\\b" +  pattern + "\\b", Pattern.DOTALL);
            Matcher m = p.matcher(text);
            if (m.matches()) {
                matches.add(pattern);
            }
        }
        return matches;
    }
    
    public static Set<String> tokenizeText(String text) {
        String words[] = text.split(" ");
        Set<String> tokens = new HashSet<String>(); 
        for (String word: words) {
            String wordTrimmed = word.trim(); 
            if (wordTrimmed.endsWith(",")) {
                //System.out.println("Token before: '" + wordTrimmed + "'");
                wordTrimmed = wordTrimmed.substring(0, wordTrimmed.length() - 1);
                //System.out.println("Token after: '" + wordTrimmed + "'");
            }
            if (wordTrimmed.endsWith(".")) {
                wordTrimmed = wordTrimmed.substring(0, wordTrimmed.length() - 1);
            }
            if (wordTrimmed.length() > 0) {
                tokens.add(wordTrimmed);
            }
        }
        return tokens;
    }
    
    public static Set<String> tokenizeUrl(String text) {	
        String words[] = text.split("/");
        
        Set<String> tokens = new HashSet<String>(); 
        for (String word: words) {
            String wordTrimmed = word.trim(); 
            if (wordTrimmed.endsWith(",")) {
                //System.out.println("Token before: '" + wordTrimmed + "'");
                wordTrimmed = wordTrimmed.substring(0, wordTrimmed.length() - 1);
                //System.out.println("Token after: '" + wordTrimmed + "'");
            }
            if (wordTrimmed.endsWith(".")) {
                wordTrimmed = wordTrimmed.substring(0, wordTrimmed.length() - 1);
            }
            if (wordTrimmed.length() > 0) {
                tokens.add(wordTrimmed);
            }
        }
        return tokens;
    }
    
    public static Set<String> findMatches(String text,
            String[] wordsToFind) {
        List<String> words = Arrays.asList(wordsToFind);
        return findMatches(text, words);
    }
    
    public static Set<String> findMatches(String text,
            Collection<String> wordsToFind) {
        Set<String> tokens = TextUtils.tokenizeText(text);
        tokens.retainAll(wordsToFind);
        return tokens;
    }  
}
