package dk.kb.webdanica.criteria;

import java.io.IOException;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;
import org.apache.tika.language.LanguageIdentifier;

import dk.kb.webdanica.utils.Constants;


/** 
 * The TIKA languageIdentifier as a PIG UDF.
 * Answer C4: TRUE if language is 'da' or 'no', else FALSE
 * Currently, the language is just returned
 *
 */
public class C4 extends EvalFunc<String> {
        @Override
        public String exec(Tuple input) throws IOException {
            if (input == null || input.size() == 0 || input.get(0) == null) {
                return Constants.getCriteriaName(this) + ": " + Constants.NODATA;
            }
            String result;
            String text = (String) input.get(0);
            //String language = new LanguageIdentifier(text).getLanguage();
            LanguageIdentifier li = new LanguageIdentifier(text);
            String language = li.getLanguage();
            boolean certain = li.isReasonablyCertain();
            result = "C4: " + language;
            if (language.equalsIgnoreCase("da") || language.equalsIgnoreCase("no")) {
                result += ", " + C5a.computeC5a(text);
                result += ", " + C5b.computeC5b(text);
            }
            return result;
            //boolean matchesNorwegianAndDanish = language.equalsIgnoreCase("da") 
            //        || language.equalsIgnoreCase("no");
            //return (matchesNorwegianAndDanish? "C4: TRUE": "C4: FALSE");
        }
}
