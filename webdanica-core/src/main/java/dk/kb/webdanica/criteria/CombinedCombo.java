package dk.kb.webdanica.criteria;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.pig.EvalFunc;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.Tuple;
import org.apache.tika.language.detect.LanguageDetector;
import org.apache.tika.language.detect.LanguageResult;
import org.apache.tika.langdetect.OptimaizeLangDetector;

import dk.kb.webdanica.utils.Constants;
import dk.kb.webdanica.utils.TextUtils;


/** 
 * CombinedCombo criteria runner.
 * url = tuple[0}
 * timestamp = tuple[1]
 * text= tuple[2]
 * links     = tuple[3]
 * hostname = tuple[4]
 * 
 * 
 * Note: C16a removed, as it is not relevant for production use, but only in the Research project
 * 
 * String result produced by CombinedCombo.exec:
 * 
 * URL, Cext1: sizeOfInput, Cext3=timestamp  
 * followed by comma-separated results for (if Cext1 > 0):
 * Cext2 - Include Asian Symbols test
 * if Cext2 < 200 (normal text):
 * C4a
 * C4b (new Criteria)
 * C5a (provided C4a is "da" or "no")
 * C5b (provided C4a is "da" or "no")
 * C3a
 * C6a
 * C6b
 * C3b
 * C3c
 * C3d
 * C6c
 * Uncommented conditions before the remainder of the tests:
 * //                if (C4a.equalsIgnoreCase("da") && C5amatches.size() > 0 && C5bmatches.size() == 0 && C3amatches.size() > 0 
 * //                    &&  C6amatches.size() > 0 && C6bmatches.size() > 0 && Cext1 > 200) {  
 * //            if (Cext1 > 200) {
 * 
 * C1a
 * C2a
 * 
 * C7a
 * C7b
 * C7c
 * C7d
 * C7e
 * C7f
 * 
 * C8a
 * C8b
 * 
 * C9a
 * C9b
 * C9c
 * C9d
 * 
 * C10a
 * C10b
 * 
 * C15a 'Url belongs to a tld often used by danes:  ".dk", ".no", ".se", ".de", ".eu", ".org", ".com", ".net", ".nu",".tv",
        ".info" 
 * C17a 'Refers to .dk web pages
 * 
 * C2b
 * C3g
 * C6d
 * C7g
 * C7h
 * C8c
 * C9e
 * C9f
 * C10c
 * 
 */
public class CombinedCombo extends EvalFunc<String> {
	@Override
	public String exec(Tuple input) throws IOException {
		if (input == null || input.size() == 0 || input.get(0) == null) {
			return Constants.getCriteriaName(this) + ": " + Constants.NODATA;
		}

		String result = "";
		String url = (String) input.get(0);
		String urlLower = url.toLowerCase();
		String timestamp = (String) input.get(1);
		String text = (String) input.get(2);
		DataBag links = (DataBag)(input.get(3));
		String hostname = (String) input.get(4);
		Set<String> tokens = TextUtils.tokenizeText(text);
		int Cext2 = 0;
		//System.out.println("TUPLE-length: " + input.size());

		//calc Cext1 '- Size of web-page'
		int Cext1 =  text.length();
		result += url + ", Cext1:" + Cext1;
		result += ", Cext3:" + timestamp;
		if (Cext1 > 0) {  
			//Calc Cext2        'Cext2 - Include Asian Symbols
			int bytesLength = text.getBytes().length; 
			Float percent = new Float(bytesLength*100) / new Float(text.length());
			Cext2 = percent.intValue();
			result += ", Cext2:" + Cext2;

			if (Cext2 < 200) {
				// Assume non-asian text if below 200 %

				//String C4a = new LanguageIdentifier(text).getLanguage(); Use deprecated code
				LanguageDetector ld = new OptimaizeLangDetector();
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
				
				for (LanguageResult r: results) {
					C4a += r.getLanguage();
				}
				
				result += ", C4a: " + C4a;
				result += ", C4b: " + C4b; // New result
				Set<String> C5amatches = new HashSet<String>();
				Set<String> C5bmatches = new HashSet<String>();
				if (C4a.equalsIgnoreCase("da") || C4a.equalsIgnoreCase("no")) {
					C5amatches = C5a.computeC5a(text);
					C5bmatches = C5b.computeC5b(text);
					result += addResultForCriterie("C5a", C5amatches); 
					result += addResultForCriterie("C5b", C5bmatches);
				}
				/*
                Calc C3a         'æ.ø.å in htm
                Calc C6a         'freq. words
                Calc C6b         'typ. words in htm
				 */
				Set<String> C3amatches = C3a.ComputeC3a(text);
				result += addResultForCriterie("C3a", C3amatches);

				Set<String> C6amatches = C6a.ComputeC6a(text);
				result += addResultForCriterie("C6a", C6amatches);
				Set<String> C6bmatches = C6b.ComputeC6b(text);
				result += addResultForCriterie("C6b", C6bmatches);

				/*
                Calc C3b         'ae.oe.aa in htm
                Calc C3c         'æ.ø.å in url
                Calc C3d         'ae.oe.aa in url

                Calc C6c         'typ. words in url 
				 */

				Set<String> C3bmatches = C3b.ComputeC3b(text);
				result += addResultForCriterie("C3b", C3bmatches);
				Set<String> C3cmatches = C3c.ComputeC3c(urlLower);
				result += addResultForCriterie("C3c", C3cmatches);
				Set<String> C3dmatches = ComputeC3d(urlLower);
				result += addResultForCriterie("C3d", C3dmatches);

				Set<String> C6cmatches = C6c.ComputeC6c(urlLower);
				result += addResultForCriterie("C6c", C6cmatches);

				//                if (C4a.equalsIgnoreCase("da") && C5amatches.size() > 0 && C5bmatches.size() == 0 && C3amatches.size() > 0 
				//                        &&  C6amatches.size() > 0 && C6bmatches.size() > 0 && Cext1 > 200) {  
				//                if (Cext1 > 200) {

				Set<String> C1amatches = C1a.computeC1a(links);
				result += addResultForCriterie("C1a", C1amatches);

				Set<String> C2amatches = C2.computeC2a(text);
				result += addResultForCriterie("C2a", C2amatches);
				Set<String> C7amatches = C7a.computeC7a(text);
				result += addResultForCriterie("C7a", C7amatches);
				Set<String> C7bmatches = C7b.computeC7b(urlLower);
				result += addResultForCriterie("C7b", C7bmatches);
				Set<String> C7cmatches = C7c.computeC7c(text);
				result += addResultForCriterie("C7c", C7cmatches);
				Set<String> C7dmatches = C7d.computeC7d(urlLower);
				result += addResultForCriterie("C7d", C7dmatches);
				Set<String> C7ematches = C7e.computeC7e(text);
				result += addResultForCriterie("C7e", C7ematches);
				Set<String> C7fmatches = C7f.computeC7f(urlLower);
				result += addResultForCriterie("C7f", C7fmatches);

				Set<String> C8amatches = C8a.computeC8a(text);
				result += addResultForCriterie("C8a", C8amatches);
				Set<String> C8bmatches = C8b.computeC8b(text);
				result += addResultForCriterie("C8b", C8bmatches);

				Set<String> C9amatches = C9a.computeC9a(text);
				result += addResultForCriterie("C9a", C9amatches);
				Set<String> C9bmatches = C9b.computeC9b(text);
				result += addResultForCriterie("C9b", C9bmatches);
				Set<String> C9cmatches = C9c.computeC9c(urlLower);
				result += addResultForCriterie("C9c", C9cmatches);
				Set<String> C9dmatches = C9d.computeC9d(text);
				result += addResultForCriterie("C9d", C9dmatches);

				Set<String> C10amatches = C10a.computeC10a(text);
				result += addResultForCriterie("C10a", C10amatches);
				Set<String> C10bmatches = C10b.computeC10b(text);
				result += addResultForCriterie("C10b", C10bmatches);

				/*
                    Calc C15a      'The URL belongs to a TLD often used by Danes,
                    Calc C16a      'The web pages is referred to by a number of web pages
                    Calc C17a      'Refers to .dk web pages
				 */
				boolean C15a = C15.computeC15a(hostname);
				result += ", C15a: " + (C15a? "y": "n");
				int c16a = C16.computeC16(url);
				result += ", C16a: " + c16a;
				int c17a = C17.computeC17(links);
				result += ", C17a: " + c17a;




				/*
				 * IF (C4a=da) AND (C5a>0)  AND (C5b=0) AND (C3a>0) AND (C6a>0) AND (C6b>0) AND (Cext1>200)  THEN 'Great chance that it is Danish and iSize of web-page >200 chars
				 * 
				 */

				//Calc C1a         '@ in link

				/*
                Calc C2a         'ph. in htm
                Calc C7a         'towns in htm
                Calc C7b         'towns in url
                Calc C7c         'town suffixes in htm
                Calc C7d         'town suffixes in url
                Calc C7e         'names in foreign in hm
                Calc C7f         'names in foreign in url
                Calc C8a         'unions in htm
                Calc C8b         'unions in url
                Calc C9a         'company type Aps etc.
                Calc C9b         'company names in htm
                Calc C9c         'company names in url
                Calc C9d         'company cvr
                Calc C10a         'surname patterns
                Calc C10b         'freq. person names
                Calc C15a      'The URL belongs to a TLD often used by Danes,
                Calc C16a      'The web pages is referred to by a number of web pages
                Calc C17a      'Refers to .dk web pages
				 */    


				boolean C2b = C2b_from_a_Nov.computeC2b(text);
				result += ", C2b: " + (C2b? "y": "n");
				Set<String> c3tokens = copyTokens(tokens);
				Set<String> C3gmatches = C3g_fom_b_Nov.computeC3gV5(c3tokens);
				result += addResultForCriterie("C3g", C3gmatches);
				Set<String> c6tokens = copyTokens(tokens);
				Set<String> C6dmatches = C6d_from_a_Nov.computeC6dV5(c6tokens);
				result += addResultForCriterie("C6d", C6dmatches);
				Set<String> c7gtokens = copyTokens(tokens);
				Set<String> C7gmatches = C7g_from_a_Nov.computeC7gV5(c7gtokens);
				result += addResultForCriterie("C7g", C7gmatches);
				Set<String> c7htokens = copyTokens(tokens);
				Set<String> C7hmatches = C7h_from_e_Nov.computeC7hV5(c7htokens);
				result += addResultForCriterie("C7h", C7hmatches);
				Set<String> c8tokens = copyTokens(tokens);
				Set<String> C8cmatches = C8c_from_a_Nov.computeC8cV5(c8tokens);
				result += addResultForCriterie("C8c", C8cmatches);
				Set<String> c9tokens = copyTokens(tokens);
				Set<String> C9ematches = C9e_from_b_Nov.computeC9eV5(c9tokens);
				result += addResultForCriterie("C9e", C9ematches);
				boolean C9f = C9f_from_d_Nov.computeC9f(text);
				result += ", C9f: " + (C9f? "y": "n");
				Set<String> c10tokens = copyTokens(tokens);
				Set<String> C10cmatches = C10c_from_b_Nov.computeC10cV5(c10tokens);
				result += addResultForCriterie("C10c", C10cmatches);     


			}
		}
		return result;
	}

	private String addResultForCriterie(String criteria, Set<String> matches) {
		return ", " + criteria + ": " + matches.size() + " " 
				+ TextUtils.conjoin("#", matches);
	}

	public static Set<String> ComputeC3d(String urlLower) {
		return TextUtils.SearchPattern(
				urlLower, Words.frequentwordsWithDanishLettersCoded);
	}

	private Set<String> copyTokens(Set<String> tokens) {
		Set<String> res = new HashSet<String>();
		res.addAll(tokens);
		return res;
	}
    
}

