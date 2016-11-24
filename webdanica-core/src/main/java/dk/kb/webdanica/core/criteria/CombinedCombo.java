package dk.kb.webdanica.core.criteria;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.Tuple;
import org.json.simple.JSONObject;

import dk.kb.webdanica.core.WebdanicaSettings;
import dk.kb.webdanica.core.datamodel.criteria.CriteriaUtils;
import dk.kb.webdanica.core.utils.Constants;
import dk.kb.webdanica.core.utils.TextUtils;

/** 
 * CombinedCombo criteria runner.
 * url = tuple[0}
 * timestamp = tuple[1]
 * text= tuple[2]
 * links     = tuple[3]
 * hostname = tuple[4]
 * debugMode = tuple[5] (optional argument), default = false
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
	@SuppressWarnings("unchecked")
    @Override
	public String exec(Tuple input) throws IOException {
		if (input == null || input.size() == 0 || input.get(0) == null) {
			return Constants.getCriteriaName(this) + ": " + Constants.NODATA;
		}

		String url = (String) input.get(0);
		String urlLower = url.toLowerCase();
		String timestamp = (String) input.get(1);
		String textNormal = (String) input.get(2);
		String text = textNormal.toLowerCase();
		DataBag links = (DataBag)(input.get(3));
		String hostname = (String) input.get(4);
		boolean debugMode = false;
		if (input.size() > 5) {
			debugMode = (Boolean) input.get(5);
		}
		String cityfilePathAsArg = null;
		if (input.size() == 7) {
			cityfilePathAsArg = (String) input.get(6); 
		}
		
		Set<String> tokens = TextUtils.tokenizeText(text);
		Set<String> tokensUncased = TextUtils.tokenizeText(textNormal);
		
		int Cext2 = 0;
		
		StringBuilder errorSb = new StringBuilder();
		
		//calc Cext1 '- Size of web-page'
		int Cext1 = text.length();
		String cityfilePath = null;
		if (cityfilePathAsArg == null) {
			cityfilePath = getCityfilePath(errorSb);
		} else {
			cityfilePath = cityfilePathAsArg;
		}
		File cityFile = null;
		boolean useStandardC7ATest = true;
		if (cityfilePath != null) {
			cityFile = new File(cityfilePath);
			if (cityFile.isFile()) {
				useStandardC7ATest = false;
			} else {
				errorSb.append("The given file '" +  cityFile.getAbsolutePath() + "' does not exist. Reverting to old test");
			}
		}
		JSONObject object = new JSONObject();
		object.put("url", CriteriaUtils.toBase64(url));
		object.put("Cext1", Cext1 + "");
		object.put("Cext3", timestamp);

		//result.append(url + ", Cext1:" + Cext1);
		//result.append(", Cext3:" + timestamp);
		
        if (Cext1 > 0) {  
			//Calc Cext2        'Cext2 - Include Asian Symbols
			int bytesLength = text.getBytes().length; 
			Float percent = new Float(bytesLength*100) / new Float(text.length());
			Cext2 = percent.intValue();
			object.put("Cext2", "" + Cext2);
			if (Cext2 < 200) { // Assume non-asian text if Cext2 is < below 200 %
				
				//Calc C1a         '@ in links (looks for danish mail-addresses matching this regex: "(?i)\\b[A-Z0-9._%+-]+@(?i)[A-Z0-9.-]+\\.DK\\b";
				//input: outlinks found
				Set<String> C1amatches = C1a.computeC1a(links);
				addResultForCriterie(object, "C1a", C1amatches);
				
				// Calc C2a         'ph. in htm using indicators "mobil +45", "tlf.", "+45", "0045" (input lowercase text)
				Set<String> C2amatches = C2.computeC2a(text);
				addResultForCriterie(object, "C2a", C2amatches);
				// Calc C2b 		'ph in htm using regexp: "(?i)\\b(?:tlf\\.?(nr\\.?)?|telefon|mobil)\\s*(\\+45)?\\s*[1-9]\\d(.?\\d{2}){3}\\b";
				// input: lowercase text
				boolean C2b = C2b_from_a_Nov.computeC2b(text);
				//result.append(", C2b: " + (C2b? "y": "n"));
				object.put("C2b", (C2b? "y": "n"));
				
				// Calc C3a         'æ.ø.å in htm (input lowercase text)
				Set<String> C3amatches = C3a.ComputeC3a(text);
				addResultForCriterie(object, "C3a", C3amatches);
				
				// Calc C3b         'ae.oe.aa in htm (input lowercase text)
				Set<String> C3bmatches = C3b.ComputeC3b(text);
				addResultForCriterie(object, "C3b", C3bmatches);
				// Calc C3c         'æ.ø.å in url (input url in lowercase)
				Set<String> C3cmatches = C3c.ComputeC3c(urlLower);
				addResultForCriterie(object, "C3c", C3cmatches);
				//Calc C3d         'ae.oe.aa in url (input url in lowercase)
				Set<String> C3dmatches = ComputeC3d(urlLower);
				addResultForCriterie(object, "C3d", C3dmatches);
				//Calc C3g		  'look for frequent danish words, with danish letters substituted with ae, oe/o,aa,
				// Input lowercase text tokenized
				Set<String> C3gmatches = C3g_fom_b_Nov.computeC3gV5(TextUtils.copyTokens(tokens));
				addResultForCriterie(object, "C3g", C3gmatches);
			
				// Calc C4a and C4b - detect the language(s) of the text
				List<String> languageTestResults = C4.computeNewC4(text);
				String C4a = languageTestResults.get(0);
				String C4b = languageTestResults.get(1);
				//result.append(", C4a: " + C4a);
				object.put("C4a", C4a);
				//result.append(", C4b: " + C4b); // New result
				object.put("C4b", C4b);
				Set<String> C5amatches = new HashSet<String>();
				Set<String> C5bmatches = new HashSet<String>();
				if (C4a.equalsIgnoreCase("da") || C4a.equalsIgnoreCase("no")) {
					// Calc C5a - Look for characteristic danish words (input: lowercase text)
					C5amatches = C5a.computeC5a(text);
					addResultForCriterie(object, "C5a", C5amatches);
					// Calc C5b - Look for characteristic Norwegian words that is not danish words (input: lowercase text)
					C5bmatches = C5b.computeC5b(text);
					addResultForCriterie(object, "C5b", C5bmatches);
				}
				
				//Calc C6a         'freq. danish words (input: lowercase text)
				Set<String> C6amatches = C6a.ComputeC6a(text);
				addResultForCriterie(object, "C6a", C6amatches);
				//Calc C6b         'typ. danish words in htm (input: lowercase text)
				Set<String> C6bmatches = C6b.ComputeC6b(text);
				addResultForCriterie(object, "C6b", C6bmatches);
					
				// Calc C6c         'typ. words in url (input: lowercase url)
				Set<String> C6cmatches = C6c.ComputeC6c(urlLower);
				addResultForCriterie(object, "C6c", C6cmatches);
				// Calc C6d			'frequent danish words: Words.getFrequentDanishWordsNov()
				// Input lowercase text tokenized
				Set<String> C6dmatches = C6d_from_a_Nov.computeC6dV5(TextUtils.copyTokens(tokens));
				addResultForCriterie(object, "C6d", C6dmatches);
				
				
				//Calc C7a         'towns in htm (input: lowercase tezt)
				Set<String> C7amatches = null;
				if (useStandardC7ATest) {
					C7amatches = C7a.computeC7a(text);
				} else {
					C7amatches = C7a.computeC7aOnCasedTokens(TextUtils.copyTokens(tokensUncased), cityFile, errorSb);
				}
				
				addResultForCriterie(object, "C7a", C7amatches);
				//Calc C7b         'towns in url (input: lowercase url)
				Set<String> C7bmatches = C7b.computeC7b(urlLower);
				addResultForCriterie(object, "C7b", C7bmatches);
				//Calc C7c         'town suffixes in htm (input: lowercase text)
				Set<String> C7cmatches = C7c.computeC7c(text);
				addResultForCriterie(object, "C7c", C7cmatches);
				//Calc C7d         'town suffixes in url (input: lowercase url)
				Set<String> C7dmatches = C7d.computeC7d(urlLower);
				addResultForCriterie(object, "C7d", C7dmatches);
				//Calc C7e         '(København/Danmark) translated to foreign languages in htm 
				Set<String> C7ematches = C7e.computeC7e(text);
				addResultForCriterie(object, "C7e", C7ematches);
				//Calc C7f         '(København/Danmark) translated to foreign languages in url (input: lowercase url)
				Set<String> C7fmatches = C7f.computeC7f(urlLower);
				addResultForCriterie(object, "C7f", C7fmatches);
				//Calc C7g 			'danish city names (input: al text, tokenized)
				Set<String> C7gmatches = C7g_from_a_Nov.computeC7gV5(TextUtils.copyTokens(tokens));
				addResultForCriterie(object, "C7g", C7gmatches);
				//Calc C7h			'(København/Danmark) translated to foreign languages in htm (input: text, tokemized) 
				Set<String> C7hmatches = C7h_from_e_Nov.computeC7hV5(TextUtils.copyTokens(tokens));
				addResultForCriterie(object, "C7h", C7hmatches);
				
				//Calc C8a         'unions in htm (input: al text lowercased)
				Set<String> C8amatches = C8a.computeC8a(text);
				addResultForCriterie(object, "C8a", C8amatches);
				//Calc C8b         'unions in url (input: lowercase url) (WRONGLY computed earlier on text instead of urlLower) 
				Set<String> C8bmatches = C8b.computeC8b(urlLower);
				addResultForCriterie(object, "C8b", C8bmatches);
				//Calc C8c         'unions in htm (input: al text lowercased, tokenized)
				Set<String> C8cmatches = C8c_from_a_Nov.computeC8cV5(TextUtils.copyTokens(tokens));
				addResultForCriterie(object, "C8c", C8cmatches);
				
				
				//Calc C9a         'company type Aps etc.
				Set<String> C9amatches = C9a.computeC9a(text);
				addResultForCriterie(object, "C9a", C9amatches);
				//Calc C9b         'company names in htm
				Set<String> C9bmatches = C9b.computeC9b(text);
				addResultForCriterie(object, "C9b", C9bmatches);
				//Calc C9c         'company names in url
				Set<String> C9cmatches = C9c.computeC9c(urlLower);
				addResultForCriterie(object, "C9c", C9cmatches);
				//Calc C9d         'company cvr
				Set<String> C9dmatches = C9d.computeC9d(text);
				addResultForCriterie(object, "C9d", C9dmatches);
				//Calc C9e 		   'search for lowercase company-names (input: lowercased text tokenized,  Output: any matches) 
				Set<String> C9ematches = C9e_from_b_Nov.computeC9eV5(TextUtils.copyTokens(tokens));
				addResultForCriterie(object, "C9e", C9ematches);
				//Calc C9f          'search for lowercased cvr-number, input: al text lowercased, output: y/n
				boolean C9f = C9f_from_d_Nov.computeC9f(text);
				//result.append(", C9f: " + (C9f? "y": "n"));
				object.put("C9f", (C9f? "y": "n"));
				
				//Calc C10a         'surname patterns (input: lowercased text, tokenized, output: )
				Set<String> C10amatches = C10a.computeC10a(text);
				addResultForCriterie(object, "C10a", C10amatches);
				//Calc C10b         'freq. person names (input: lowercased text, output: any found names in the text)
				Set<String> C10bmatches = C10b.computeC10b(text);
				addResultForCriterie(object, "C10b", C10bmatches);
				//Calc C10c		
				Set<String> C10cmatches = C10c_from_b_Nov.computeC10cV5(TextUtils.copyTokens(tokens));
				addResultForCriterie(object, "C10c", C10cmatches);     

				// Calc C15a      'The URL belongs to a TLD often used by Danes (The list currently comprises .dk, .no, .se, .de, .eu, .org, .com, .net, .nu, .tv, .info)
				boolean C15a = C15.computeC15a(hostname);
				object.put("C15a", (C15a? "y": "n"));
				//Calc C17a      'The outlinks of the page refers to .dk web pages
				int c17a = C17.computeC17(links);
				object.put("C17a", c17a + "");
				if (debugMode) {
					String ctext = CriteriaUtils.toBase64(text);
					if (ctext == null) {
						errorSb.append("Error: Unable to convert text of size " + text.length() 
								+ " to base64");
					} 
					object.put("CText", ctext + "");
					String clinks = TextUtils.conjoin("##", C17.getLinks(links));
					object.put("CLinks", CriteriaUtils.toBase64(clinks));
				}
			}
			if (!errorSb.toString().isEmpty()) {
				object.put("CError", errorSb.toString());
			}
		}
		return object.toJSONString();
	}

	@SuppressWarnings("unchecked")
    private void addResultForCriterie(JSONObject jo, String criteria, Set<String> matches) {
		String result = matches.size() + "";
		if (matches.size() > 0) {
			result = matches.size() + " " 
					+ TextUtils.conjoin("#", matches);
		}
		jo.put(criteria, result);
	}

	public static Set<String> ComputeC3d(String urlLower) {
		return TextUtils.SearchPattern(
				urlLower, Words.frequentwordsWithDanishLettersCoded);
	}
	
	private String getCityfilePath(StringBuilder error) {
		try {
			String citynamesFilePath = dk.kb.webdanica.core.utils.Settings.get(WebdanicaSettings.PIG_CITYNAMES_FILEPATH);
			return citynamesFilePath;
		} catch (Throwable e) {
			error.append("Unable to retrieve Cityfilepath: " + e);
		}
		return null;
	}
}

