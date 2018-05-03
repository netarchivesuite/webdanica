package dk.kb.webdanica.core.datamodel.criteria;

import java.util.ArrayList;
import java.util.List;

public class Language {

    private String code;
    private float confidence;

    public Language(String code, float confidence) {
        this.code = code;
        this.confidence = confidence;
    }
    
    public static List<Language> findLanguages(String languagesFound) {
        List<Language> languages = new ArrayList<Language>();
        // "so: MEDIUM (0.714284)#br: MEDIUM (0.142858)#sl: MEDIUM (0.142857)"
        if (languagesFound.contains("#")) {
            String[] parts = languagesFound.split("#");
            for (String part : parts) {
                Language lang = parseLanguage(part);
                languages.add(lang);
            }
        } else {
            Language lang = parseLanguage(languagesFound);
            languages.add(lang);
        }
        return languages;
    }
    
    /**
     * Parse a String like this: so: MEDIUM (0.714284).
     * @param languageAsString a language in the form above.
     * @return a Language object created from the given languageAsString
     */
    public static Language parseLanguage(String languageAsString) {
        String[] lanparts = languageAsString.split(":");
        String code = lanparts[0];
        String confidencePart = lanparts[1];
        int leftparanthesis = confidencePart.indexOf("(");
        int rightparanthesis = confidencePart.indexOf(")");
        String percentageString = confidencePart.substring(leftparanthesis + 1,
                rightparanthesis);
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
        return ("Language code: " + code + ", confidence = " + confidence);
    }

    public static boolean isLanguage(String languagesFound, String languageCode) {
        List<Language> languages = Language.findLanguages(languagesFound);
        for (Language l : languages) {
            if (l.getCode().equalsIgnoreCase(languageCode)) {
                return true;
            }
        }
        return false;
    } 
}
