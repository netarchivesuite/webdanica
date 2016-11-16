package dk.kb.webdanica.core.criteria;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;
import org.apache.tika.langdetect.OptimaizeLangDetector;
import org.apache.tika.language.LanguageIdentifier;
import org.apache.tika.language.detect.LanguageDetector;
import org.apache.tika.language.detect.LanguageResult;

import dk.kb.webdanica.core.datamodel.criteria.Language;
import dk.kb.webdanica.core.datamodel.criteria.SingleCriteriaResult;
import dk.kb.webdanica.core.utils.Constants;


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
        
        /**
         * Language detection using the OptimaizeLangDetector
         * @param text
         * @return
         * @throws IOException
         */
        public static List<String> computeNewC4(String text) throws IOException{
        	List<String> returnList = new ArrayList<String>();
        	LanguageDetector ld = new OptimaizeLangDetector();
			ld.loadModels();
			ld.setMixedLanguages(true);
			ld.setShortText(true);
			List<LanguageResult> results= ld.detectAll(text);
			String C4a = "";
			String C4b = ""; // contains all found languages with probability scores
			if (results.size() == 0) {
				C4a = "0"; // represents no language found
				C4b = "0";
			} else if (results.size() == 1) {
				C4a = results.get(0).getLanguage();
				C4b = results.get(0).toString();
			} else {
				// Set C4a to the language with the highest probability score
				float highscore = 0.0f;
				C4b = StringUtils.join(results, "#");
				for (LanguageResult r: results) {
					if (r.getRawScore() > highscore){
						C4a = r.getLanguage();
						highscore = r.getRawScore();
					}
				}
			}
			
			returnList.add(C4a);
			returnList.add(C4b);
			return returnList;
        }
}

