package dk.kb.webdanica.core.criteria;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import dk.kb.webdanica.core.utils.TextUtils;

public class DanicaRegexps {
    public static final String[] DanishSurnameSuffixes = new String[]{ "sen"};

    public static String danisheMail = "(?i)\\b[A-Z0-9._%+-]+@(?i)[A-Z0-9.-]+\\.DK\\b";
    public static Pattern pDanishMail = Pattern.compile(danisheMail);
    
    // Used in test C9c
    //old:public static String danishCvrRegexp = "(?i)\\bCVR:?\\s+\\d{2}(?:[ ]?\\d{2}){3}\\b";
    public static String danishCvrRegexp = "(?i)\\bcvr(?::|-nr\\.|-nr| nr\\.| nr|-nummer| nummer)?\\s*\\d{2}(?:[ ]?\\d{2}){3}\\b";
    public static Pattern pDanishCvrRegexp = Pattern.compile(danishCvrRegexp);
    public static String danishCvrRegexpNoCase = "\\bcvr(?::|-nr\\.|-nr| nr\\.| nr|-nummer| nummer)?\\s*\\d{2}(?:[ ]?\\d{2}){3}\\b";
    public static Pattern pDanishCvrRegexpNoCase = Pattern.compile(danishCvrRegexpNoCase);

	//String tlfRegExp = "\\b(?:(?:\\+45|0045)[\\s.-]?)?[1-9]\\d([\\s.-]?\\d{2}){3}\\b";
    //String danishTlfRegexp = "(?i)\\b(?:Tlf\\.?|telefon)\\s*(\\+45)?\\s*[1-9]\\d(.?\\d{2}){3}\\b";
    //public static String danishTlfRegexp="\\b(?:(?:\\+45|0045|45)[\\s.-]?)?[1-9]\\d([\\s.-]?\\d{2}){3}\\b";
    public static String danishTlfRegexp = "(?i)\\b(?:tlf\\.?(nr\\.?)?|telefon|mobil)\\s*(\\+45)?\\s*[1-9]\\d(.?\\d{2}){3}\\b";
    public static Pattern pDanishTlfRegexp = Pattern.compile(danishTlfRegexp);
    public static String danishTlfRegexpNoCase = "\\b(?:tlf\\.?(nr\\.?)?|telefon|mobil)\\s*(\\+45)?\\s*[1-9]\\d(.?\\d{2}){3}\\b";
    public static Pattern pDanishTlfRegexpNoCase = Pattern.compile(danishTlfRegexp);
    
    private static Set<String> danishNamesWords = getDanishNamesWords();
    private static Set<String> getDanishNamesWords() {
    	Set<String> words = new HashSet<String>();
    	words.addAll(java.util.Arrays.asList(Words.DanishNames));
    	return words;
    }
    public static String danishNamesRegexp = "(?i)\\b(" + TextUtils.conjoin("|", danishNamesWords) + ")\\b";
    public static Pattern pDanishNamesRegexp = Pattern.compile(danishNamesRegexp);
    
    
    public static String testMailAdress = "svc@kb.dk";
    public static String testPhonenumber = "0045 87 38 38 38"; //JP telefon-number
    public static String testPhonenumber1 = "Tlf. 9132 4720";
    
    
    
    /** Part of characrters for C3a. */
    public static String [] danishCharactersUnicode =  new String[] {
      "\\u00E6",  "\\u00E5", "\\u00C6",
      "\\u00C5","\\u00F8", "\\u00D8"
    };
    
    /** Only danish characters html-encoded, only lowercase. 
     * Part of characrters for C3a. */
    public final static String[] danishCharactersHtmlencoded = new String[] {
            "&oslash;", 
            "&aring;",
            "&aelig;",
            "&#230;","&#248;", "&#229;","&#198;","&#216;","&#197;",
            "&#xe6", "&#xf8","&#xe5","&#xc6","&#xd8","&#xc5",
            "\\u00E6",  "\\u00E5", "\\u00C6",
            "\\u00C5","\\u00F8", "\\u00D8",
            "ø","æ", "å" //should probaly be removed in order to avaiod wrong encoding of æøå
    };

    /** Only danish characters url-encoded, only lowercase. 
     * Used in test C3c. */
    public final static String[] danishCharactersUrlEncoded = new String[] {
       "%f8", "%d8", "%e6", "%e5", "%c6", "%c5",
       
    };
    
    /**
     * @param args
     */
    public static void main(String[] args) {        
    }

}
