package dk.kb.webdanica.core.criteria;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.tika.langdetect.OptimaizeLangDetector;
import org.apache.tika.language.detect.LanguageDetector;
import org.apache.tika.language.detect.LanguageResult;



/** 
 * A program to calculate criteria c4a, c4b.
 * returns a list containing (C4a,C4b) as Strings. 
 *
 */
public class C4 {
	    
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

