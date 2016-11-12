package dk.kb.webdanica.core.datamodel.criteria;

import java.util.ArrayList;
import java.util.List;

public class Language {
	
	public static void main (String[] args) {
		String testLanguages = "so: MEDIUM (0.714284)#br: MEDIUM (0.142858)#sl: MEDIUM (0.142857)";
		List<Language> languages = findLanguages(testLanguages);
		for (Language l: languages) {
			System.out.println(l);
		}
	}
	
	private String code;
	private float confidence;

	public Language(String code, float confidence) {
		this.code=code;
		this.confidence = confidence;
	}
	
	public static List<Language> findLanguages(String languagesFound) {
	    List<Language> languages = new ArrayList<Language>();
	    //"so: MEDIUM (0.714284)#br: MEDIUM (0.142858)#sl: MEDIUM (0.142857)"
	    if (languagesFound.contains("#")) {
	    	String[] parts = languagesFound.split("#");
	    	for (String part: parts) {
	    		Language l = parseLanguage(part);
	    		languages.add(l);
	    	}
	    }
	    return languages;
    }

	public static Language parseLanguage(String part) {
		String[] lanparts = part.split(":"); 
		String code = lanparts[0];
		String confidencePart = lanparts[1];
		int leftparanthesis = confidencePart.indexOf("(");
		int rightparanthesis = confidencePart.indexOf(")");
		String percentageString = confidencePart.substring(leftparanthesis +1, rightparanthesis);
		Float percentage = Float.valueOf(percentageString);
	    return new Language(code, percentage);
    }

	public float getConfidence() {
	    return confidence;
    }

	public String getCode() {
	    return code;
    }
	
	public String toString() {
		return ("Language code: " + code + ", confidence = " + confidence );
	}
	
}
