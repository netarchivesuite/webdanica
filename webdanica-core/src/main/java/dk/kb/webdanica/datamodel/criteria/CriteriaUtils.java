package dk.kb.webdanica.datamodel.criteria;

import java.io.File;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dk.kb.webdanica.criteria.Words;
import dk.kb.webdanica.utils.TextUtils;

public class CriteriaUtils {
	public static String findC9eval(String c9b, String c9e)  {
		String s = "";
		if (c9b!=null && (!c9b.isEmpty() && !c9b.startsWith("0"))) {
	        String[] comps9b = c9b.substring(1).trim().split(",");
	        Set<String> compsLst9b = new HashSet<String>();
	        compsLst9b.addAll(java.util.Arrays.asList(comps9b));
	        List<String> words2 = Arrays.asList(Words.virksomheder_lowercased_2_words_Nov2);
	        compsLst9b.retainAll(words2);
	        Set<String>  compsLst9e = new HashSet<String>();
	    	if (c9e!=null && (!c9e.isEmpty() && !c9e.startsWith("0"))) {
	            String[] comps9e = c9e.substring(1).trim().split(",");
	            compsLst9e.addAll(java.util.Arrays.asList(comps9e));
	            List<String> words1 = Arrays.asList(Words.virksomheder_lowercased_1_word_Nov3);
	            compsLst9e.retainAll(words1);
	    	}
	    	compsLst9e.addAll(compsLst9b);
	    	s = compsLst9e.size() + " " + TextUtils.conjoin("#", compsLst9e);
		}
		if (s.isEmpty() && !c9e.isEmpty()) s=c9e;
		return s;
	}

	public static String findC8cval(String c8a, String c8c)  {
		String s = "";
		if (c8a!=null && (!c8a.isEmpty() && !c8a.startsWith("0"))) {
	        String[] comps8a = c8a.substring(1).trim().split(",");
	        Set<String> compsLst8a = new HashSet<String>();
	        compsLst8a.addAll(java.util.Arrays.asList(comps8a));
	        List<String> words2 = Arrays.asList(Words.foreninger_lowercased_2_words_Nov2);
	        compsLst8a.retainAll(words2);
	        Set<String>  compsLst8c = new HashSet<String>();
	    	if (c8c!=null && (!c8c.isEmpty() && !c8c.startsWith("0"))) {
	            String[] comps8c = c8c.substring(1).trim().split(",");
	            compsLst8c.addAll(java.util.Arrays.asList(comps8c));
	            List<String> words1 = Arrays.asList(Words.foreninger_lowercased_1_word_Nov2);
	            compsLst8c.retainAll(words1);
	    	}
	    	compsLst8c.addAll(compsLst8a);
	    	s = compsLst8c.size() + " " + TextUtils.conjoin("#", compsLst8c);
		}
		if (s.isEmpty() && !c8c.isEmpty()) s=c8c;
		return s;
	}

	public static String findC10cval(String c10c)  {
		String s = "";
		if (c10c!=null && (!c10c.isEmpty())) {
			if (!c10c.startsWith("0")) {
	            Set<String> tokens = TextUtils.tokenizeText(c10c.substring(1).trim());
	            List<String> words = Arrays.asList(Words.DanishNamesNov3);
	            tokens.retainAll(words);
	            s = tokens.size() + " " + TextUtils.conjoin("#", tokens);
			} else {
				s=c10c;
			}
	    }
		return s;
	}

	public static String find8bVal(String url) {
	    Set<String> foundMatches = computeC8b(url);
	    String val = (foundMatches.size() > 0 
			    		? (foundMatches.size() + " " + TextUtils.conjoin("#", foundMatches))
			    		: "0");
	    return val;
	}

	/*public static String findNew3Val(String previous) {
		String val = "";
		if (previous.startsWith("0")) {
			val = "0";
		} else {
	        Set<String> foundMatches = computeNewC3x(previous);
	        val = (foundMatches.size() > 0 
	   		    		? (foundMatches.size() + " " + TextUtils.conjoin("#", foundMatches))
	   		    		: "0");
		}
	    return val;
	}*/

	public static String findNew3ValToken(String previous) {
		String val = "";
		if (previous.startsWith("0")) {
			val = "0";
		} else {
	        Set<String> tokens = TextUtils.tokenizeText(previous);
	        Set<String> foundMatches = new HashSet<String>();
	        for (String word: tokens) {
	        	if(TextUtils.findMatches(word, Words.frequentwordsWithDanishLettersCodedNew).size()>0) {
	                foundMatches.add(word);
	            }
	        }
	    	val = (foundMatches.size() > 0 
			    		? (foundMatches.size() + " " + TextUtils.conjoin("#", foundMatches))
			    		: "0");
		}
	    return val;
	}

	public static String findTLD(String url) {
	    String[] parts0 = url.split(":");
	    // Check http://www.medicasur.com.mx:8090/ - becomes .mx
	    
	    if (parts0.length <2) {
	        //System.out.println("Error --- no ':' in url " + url); 
	        return "";
	    }
	    
	    String[] parts1 = parts0[1].split("/");
	    String[] parts2 = parts0[1].split( "\\\\");
	    String[] parts = (parts1.length > parts2.length ? parts1 : parts2);
	    
	    if (parts.length == 0) {
	        //System.out.println("Error --- no parts in url " + url); 
	        return "";
	    }
	    
	    int i = -1;
	    boolean found = false;
	    boolean stop = false;
	    while (!stop && !found) {
	    	i++;
	    	found = !parts[i].isEmpty();
	    	stop = (i==parts.length-1);	
	    }
	    
	    if (stop && !found) {
	    	//System.out.println("stopped --- : " + url); 
	        return "";
	    }
	    
	    String s = parts[i];
	    
	    int tldbegin = s.lastIndexOf('.');
	    if (tldbegin == -1) {
	        //System.out.println("No TLD found: " + url );
	        return "";
	    }
	    String tld = s.substring(tldbegin+1, s.length());
	    
	    return tld;
	}

	public static Set<String> computeC8b(String text) {
	    return TextUtils.SearchPattern(text, 
	            Words.foreninger_lowercased);
	}    

	/*public   static Set<String> computeNewC3x(String text) {
	    return TextUtils.SearchPattern(text, 
	            Words.frequentwordsWithDanishLettersCodedNew);
	} */   

	public static boolean getBoleanSetting(String string) {
	    String[] parts = string.split("=");
	    if (parts[1].equalsIgnoreCase("true")) {
	        return true;
	    }
	    return false;
	}

	public static String getStringSetting(String string) {
	    String[] parts = string.split("=");
	    if (parts.length>1) return parts[1];
	    else return "";
	}

	public static File checkDir(String dirname) {
	    File statDir = new File(dirname);
	    if (!statDir.isDirectory()) {
	        System.err.println("ERROR: Cannot find dir'" + statDir.getAbsolutePath() + "' as a proper directory");
	        System.exit(1);
	    }
	    return statDir;
	}

	public static boolean isNumeric(String s) {
	 	return s.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+");
	}	
	
	public static String getStringSequence(Collection<String> strSet, String delim) {
		String s = "";
	    for (String str: strSet) {
	    	s = (s.isEmpty() ? str : s + delim + str);
	    }
		return s;
	}

	public static String getIntegerSequence(Collection<Integer> intSet, String delim) {
		String s = "";
	    for (int i: intSet) {
	    	s = (s.isEmpty() ? ""+i : s + delim + i);
	    }
		return s;
	}

	public static String getBooleanSequence(Collection<Boolean> boolSet, String delim) {
		String s = "";
	    for (Boolean b: boolSet) {
	    	s = (s.isEmpty() ? "" : s + delim) + (b?"true":"false");
	    }
		return s;
	}

	
	
}
