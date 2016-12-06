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

import dk.kb.webdanica.core.datamodel.criteria.CriteriaUtils;
import dk.kb.webdanica.core.utils.Constants;
import dk.kb.webdanica.core.utils.TextUtils;

/** 
 * samplecall: 
 * captures = FOREACH captures GENERATE CombinedCombo(
 * url, 
 * date, 
 * text, 
 * links, 
 * hostname, 
 * true, 
 * '/home/test/workflow/wordslist/danishMajorCities_UTF8.txt', 
 * '/home/test/workflow/wordslist/DanishNames_UTF8.txt', 
 * '/home/test/workflow/wordslist/foreninger_lowercased_UTF8.txt', 
 * '/home/test/workflow/wordslist/foreninger_one_word_lowercased_UTF8.txt', 
 * '/home/test/workflow/wordslist/placenamesuffixes_UTF8.txt', 
 * '/home/test/workflow/wordslist/virksomheder_lowercased_UTF8.txt', 
 * '/home/test/workflow/wordslist/virksomheder_one_word_lowercased_UTF8.txt');
 * 
 * 
 *
 * C7b, C7g: danishMajorCities_UTF8.txt (danishMajorCities)
 * C7c, C7d: placenames_UTF8.txt (placenames)
 * C8a: foreninger_lowercased_UTF8.txt (foreninger_lowercased)
 * C8b, C8c: foreninger_one_word_lowercased_UTF8.txt (foreninger_one_word_lowercased)
 * C9b: virksomheder_lowercased_UTF8.txt (virksomheder_lowercased)
 * C9c, C9e: virksomheder_one_word_lowercased_UTF8.txt (virksomheder_one_word_lowercased)
 * C10c: DanishNames_UTF8.txt (DanishNames)
 *				
 * 
 * 
 * CombinedCombo criteria runner.
 * url = tuple[0}
 * timestamp = tuple[1]
 * text= tuple[2]
 * links     = tuple[3]
 * hostname = tuple[4]
 * debugMode = tuple[5] (optional argument), default = false
 * danishMajorCities
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

		String url = (String) input.get(0); // argument #1
		String urlLower = url.toLowerCase();
		String timestamp = (String) input.get(1); // argument #2
		String textNormal = (String) input.get(2);// argument #3
		String text = textNormal.toLowerCase();
		DataBag links = (DataBag)(input.get(3));// argument #4
		String hostname = (String) input.get(4);// argument #5
		boolean debugMode = false;
		if (input.size() > 5) {
			debugMode = (Boolean) input.get(5); // argument #6
		}
		
		boolean useStandardC7BTest = true;
		boolean useStandardC7GTest = true;
		
		boolean useStandardC7CTest = true;
		boolean useStandardC7DTest = true;
		
		boolean useStandardC8ATest = true;
		
		boolean useStandardC8BTest = true;
		boolean useStandardC8CTest = true;

		boolean useStandardC9BTest = true;
		
		boolean useStandardC9CTest = true;
		boolean useStandardC9ETest = true;
		
		boolean useStandardC10CTest = true;
		
		// File 1 - used by tests C7b, C7g
		String cityfilePathAsArg = null;
		if (input.size() > 6) {
			cityfilePathAsArg = (String) input.get(6); // argument #7
		}
		// File 2 - used by tests C10c
		String danishNamesFilePathAsArg = null;
		if (input.size() > 7) {
			danishNamesFilePathAsArg = (String) input.get(7); // argument #8
		}
		// File 3 - used by tests C8a
		String foreningerFilePathAsArg = null;
		if (input.size() > 8) {
			foreningerFilePathAsArg = (String) input.get(8); // argument #9
		}
		// File 4 - used by tests C8b, C8c
		String foreningerOneWordFilePathAsArg = null;
		if (input.size() > 9) {
			foreningerOneWordFilePathAsArg = (String) input.get(9); // argument #10
		}
		// File 5 - used by tests C7c, C7d
		String placeNamesFilePathAsArg = null;
		if (input.size() > 10) {
			placeNamesFilePathAsArg = (String) input.get(10); // argument #11
		}

		// File 6 - used by tests c9b
		String virksomhederFilePathAsArg = null;
		if (input.size() > 11) {
			virksomhederFilePathAsArg = (String) input.get(11); // argument #12
		}
		// File 7 - used by tests C9c, C9e
		String virksomhederOneWordFilePathAsArg = null;
		if (input.size() > 12) {
			virksomhederOneWordFilePathAsArg = (String) input.get(12); // argument #13
		} 
		
		Set<String> tokens = TextUtils.tokenizeText(text);
		Set<String> tokensUncased = TextUtils.tokenizeText(textNormal);
		
		int Cext2 = 0;
		
		StringBuilder errorSb = new StringBuilder();
		
		//calc Cext1 '- Size of web-page'
		int Cext1 = text.length();
		
		/*
		if (cityfilePathAsArg == null) {
			cityfilePath = getCityfilePath(errorSb);
		} else {
			cityfilePath = cityfilePathAsArg;
		}*/
		
		// Validate optional argument 7 - used by C7B and C7G
		File cityFile = null;
		if (cityfilePathAsArg != null) {
			cityFile = new File(cityfilePathAsArg);
			if (cityFile.isFile()) {
				useStandardC7BTest = false;
				useStandardC7GTest = false;
			} else {
				errorSb.append("The given cityFile '" +  cityFile.getAbsolutePath() + "' does not exist. Reverting to old test for C7B and C7G");
			}
		} else {
			errorSb.append("No argument given for the cityFile (optional argument 7). Reverting to old test for C7B and C7G");
		}
		// Validate optional argument 8 - used by test C10C
		File danishNamesFile = null;
		if (danishNamesFilePathAsArg != null) {
			danishNamesFile = new File(danishNamesFilePathAsArg);
			if (danishNamesFile.isFile()) {
				useStandardC10CTest = false;
			} else {
				errorSb.append("The given danishNamesFile '" +  danishNamesFile.getAbsolutePath() + "' does not exist. Reverting to old test for C10C");
			}
		} else {
			errorSb.append("No argument given for the danishNamesFile (optional argument 8). Reverting to old test for C10C");
		}
		// Validate optional argument 9 - used by test C8A
		File foreningerFile = null;
		if (foreningerFilePathAsArg != null) {
			foreningerFile = new File(foreningerFilePathAsArg);
			if (foreningerFile.isFile()) {
				useStandardC8ATest = false;
			} else {
				errorSb.append("The given foreningerFile '" + foreningerFilePathAsArg + "' does not exist. Reverting to old test for C8A");
			}
		} else {
			errorSb.append("No argument given for the foreningerFile (optional argument 9). Reverting to old test for C8A");
		}
		// Validate optional argument 10 - used by C8B and C8C
		File foreningerOneWordFile = null;
		if (foreningerOneWordFilePathAsArg != null) {
			foreningerOneWordFile = new File(foreningerOneWordFilePathAsArg);
			if (foreningerOneWordFile.isFile()) {
				useStandardC8BTest = false;
				useStandardC8CTest = false;
			} else {
				errorSb.append("The given foreningerOneWordFile '" + foreningerOneWordFilePathAsArg + "' does not exist. Reverting to old test for C8b, C8c");
			}
		} else {
			errorSb.append("No argument given for the foreningerOneWordFile (optional argument 10). Reverting to old test for C8b, C8c");
		}
		
		// Validate optional argument 11 -- used by tests C7c, C7d
		File placeNamesFile = null;
		if (placeNamesFilePathAsArg != null) {
			placeNamesFile = new File(placeNamesFilePathAsArg);
			if (placeNamesFile.isFile()) {
				useStandardC7CTest = false;
				useStandardC7DTest = false;
			} else {
				errorSb.append("The given placeNamesFile '" + placeNamesFilePathAsArg + "' does not exist. Reverting to old test for C7C, C7D");
			}
		} else {
			errorSb.append("No argument given for the placeNamesFile (optional argument 11). Reverting to old test for C7C, C7D");
		}
		
		// Validate optional argument 12 - used by tests C89B
		File virksomhederFile = null;
		if (virksomhederFilePathAsArg != null) {
			virksomhederFile = new File(virksomhederFilePathAsArg);
			if (virksomhederFile.isFile()) {
				useStandardC9BTest = false;
			} else {
				errorSb.append("The given virksomhederFile '" + virksomhederFilePathAsArg + "' does not exist. Reverting to old test for C9B");
			}
		} else {
			errorSb.append("No argument given for the virksomhederFile (optional argument 12). Reverting to old test for C9B");
		}
		
		// Validate optional argument 13 - - used by tests C9c, C9e
		File virksomhederOneWordFile = null;
		if (virksomhederOneWordFilePathAsArg != null) {
			virksomhederOneWordFile = new File(virksomhederOneWordFilePathAsArg);
			if (virksomhederOneWordFile.isFile()) {
				useStandardC9CTest = false;
				useStandardC9ETest = false;
			} else {
				errorSb.append("The given virksomhederFile '" + virksomhederOneWordFilePathAsArg + "' does not exist. Reverting to old test for C9C and C9E");
			}
		} else {
			errorSb.append("No argument given for the virksomhederFile (optional argument 12). Reverting to old test for C9C and C9E");
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
				Set<String> C1amatches = C1.computeC1a(links);
				C1amatches.addAll(C1.computeC1(textNormal));
				addResultForCriterie(object, "C1a", C1amatches);
				
				// Calc C2a         'ph. in htm using indicators "mobil +45", "tlf.", "+45", "0045" (input lowercase text)
				Set<String> C2amatches = C2.computeC2a(text);
				addResultForCriterie(object, "C2a", C2amatches);
				// Calc C2b 		'ph in htm using regexp: "(?i)\\b(?:tlf\\.?(nr\\.?)?|telefon|mobil)\\s*(\\+45)?\\s*[1-9]\\d(.?\\d{2}){3}\\b";
				// input: lowercase text
				boolean C2b = C2.computeC2b(text);
				//result.append(", C2b: " + (C2b? "y": "n"));
				object.put("C2b", (C2b? "y": "n"));
				
				// Calc C3a         'æ.ø.å in htm (input lowercase text)
				Set<String> C3amatches = C3.ComputeC3a(text);
				addResultForCriterie(object, "C3a", C3amatches);
				
				// Calc C3b         'ae.oe.aa in htm (input lowercase text)
				Set<String> C3bmatches = C3.ComputeC3b(text);
				addResultForCriterie(object, "C3b", C3bmatches);
				// Calc C3c         'æ.ø.å in url (input url in lowercase)
				Set<String> C3cmatches = C3.ComputeC3c(urlLower);
				addResultForCriterie(object, "C3c", C3cmatches);
				//Calc C3d         'ae.oe.aa in url (input url in lowercase)
				Set<String> C3dmatches = C3.ComputeC3d(urlLower);
				addResultForCriterie(object, "C3d", C3dmatches);
				//Calc C3g		  'look for frequent danish words, with danish letters substituted with ae, oe/o,aa,
				// Input lowercase text tokenized
				Set<String> C3gmatches = C3.computeC3gV5(TextUtils.copyTokens(tokens));
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
					C5amatches = C5.computeC5a(text);
					addResultForCriterie(object, "C5a", C5amatches);
					// Calc C5b - Look for characteristic Norwegian words that is not danish words (input: lowercase text)
					C5bmatches = C5.computeC5b(text);
					addResultForCriterie(object, "C5b", C5bmatches);
				}
				
				//Calc C6a         'freq. danish words (input: lowercase text)
				Set<String> C6amatches = C6.ComputeC6a(text);
				addResultForCriterie(object, "C6a", C6amatches);
				//Calc C6b         'typ. danish words in htm (input: lowercase text)
				Set<String> C6bmatches = C6.ComputeC6b(text);
				addResultForCriterie(object, "C6b", C6bmatches);
					
				// Calc C6c         'typ. words in url (input: lowercase url)
				Set<String> C6cmatches = C6.ComputeC6c(urlLower);
				addResultForCriterie(object, "C6c", C6cmatches);
				// Calc C6d			'frequent danish words: Words.getFrequentDanishWordsNov()
				// Input lowercase text tokenized
				Set<String> C6dmatches = C6.computeC6dV5(TextUtils.copyTokens(tokens));
				addResultForCriterie(object, "C6d", C6dmatches);		
				
				//Calc C7a         'towns in htm (input: lowercase text)
				Set<String> C7amatches = C7.computeC7a(text);
				/*
				if (useStandardC7ATest) {
					C7amatches = C7.computeC7a(text);
				} else {
					C7amatches = C7.computeC7aOnCasedTokens(TextUtils.copyTokens(tokensUncased), cityFile, errorSb);
				}*/
				
				addResultForCriterie(object, "C7a", C7amatches);
				//Calc C7b         'towns in url (input: lowercase url)
				
				Set<String> C7bmatches = null;
				List<Set<String>> cityFileTokenSet = null;
				if (!useStandardC7BTest) {
					cityFileTokenSet = WordsArrayGenerator.getListOfTokens(cityFile,errorSb);
				}
				
				if (useStandardC7BTest || cityFileTokenSet == null) { 
					C7bmatches = C7.computeC7b(urlLower);
				} else {
					C7bmatches = C7.computeC7bAlt(urlLower, cityFileTokenSet);	
				}
				addResultForCriterie(object, "C7b", C7bmatches);
				
				////////////////////////////////////////////////////////////////////////
				//Calc C7c         'town suffixes in htm (input: lowercase text)
				List<Set<String>> placeNamesTokenSet = null; // used by both C7c and C7d
				if (!useStandardC7CTest) {
					placeNamesTokenSet = WordsArrayGenerator.getListOfTokens(placeNamesFile, errorSb);
				}
				Set<String> C7cmatches = null; 
				if (useStandardC7CTest || placeNamesTokenSet == null) {
					C7cmatches = C7.computeC7c(text);
				} else {
					C7cmatches = C7.computeC7cAlt(text, placeNamesTokenSet);
				}
				addResultForCriterie(object, "C7c", C7cmatches);
				//Calc C7d         'town suffixes in url (input: lowercase url)
				Set<String> C7dmatches = null;
				if (useStandardC7DTest || placeNamesTokenSet == null) {
					C7dmatches = C7.computeC7d(urlLower);
				} else {
					C7dmatches = C7.computeC7dAlt(urlLower, placeNamesTokenSet);
				}

				addResultForCriterie(object, "C7d", C7dmatches);
				//Calc C7e         '(København/Danmark) translated to foreign languages in htm 
				Set<String> C7ematches = C7.computeC7e(text);
				addResultForCriterie(object, "C7e", C7ematches);
				//Calc C7f         '(København/Danmark) translated to foreign languages in url (input: lowercase url)
				Set<String> C7fmatches = C7.computeC7f(urlLower);
				addResultForCriterie(object, "C7f", C7fmatches);
				//Calc C7g 			'danish city names (input: al text, tokenized)
				Set<String> C7gmatches = null;
				if (useStandardC7GTest || cityFileTokenSet == null) { 
					C7gmatches = C7.computeC7gV5(TextUtils.copyTokens(tokens));
				} else {
					C7gmatches = C7.computeC7gAlt(text, TextUtils.copyTokens(tokens), cityFileTokenSet);	
				}
				//Set<String> C7gmatches = C7.computeC7gV5(TextUtils.copyTokens(tokens));
				addResultForCriterie(object, "C7g", C7gmatches);
				//Calc C7h			'(København/Danmark) translated to foreign languages in htm (input: text, tokemized) 
				Set<String> C7hmatches = C7.computeC7hV5(TextUtils.copyTokens(tokens));
				addResultForCriterie(object, "C7h", C7hmatches);
				
				////////////////////////////////////////////////////////////////////
				//Calc C8a         'unions in htm (input: al text lowercased)
				List<Set<String>> foreningerFileTokenSet = null;
				Set<String> C8amatches = null;
				if (!useStandardC8ATest) {
					foreningerFileTokenSet = WordsArrayGenerator.getListOfTokens(foreningerFile, errorSb);
				}
				if (useStandardC8ATest || foreningerFileTokenSet == null) {
					C8amatches = C8.computeC8a(text);
				} else {
					C8amatches = C8.computeC8aAlt(text, foreningerFileTokenSet);
				}
				//Set<String> C8amatches = C8.computeC8a(text);
				addResultForCriterie(object, "C8a", C8amatches);
				
				//Calc C8b         'unions in url (input: lowercase url) (WRONGLY computed earlier on text instead of urlLower) 				
				List<Set<String>> foreningerOnewordFileTokenSet = null;
				if (!useStandardC8BTest) {
					foreningerOnewordFileTokenSet = WordsArrayGenerator.getListOfTokens(foreningerOneWordFile, errorSb);
				}
				Set<String> C8bmatches = null;
				if (useStandardC8BTest || foreningerOnewordFileTokenSet == null) {
					C8bmatches = C8.computeC8b(urlLower);
				} else {
					try {
						C8bmatches = C8.computeC8bAlt(urlLower, TextUtils.copyTokens(foreningerOnewordFileTokenSet));
					} catch (Throwable e) {
						errorSb.append("new C8b calculation failed (reverting to standard): " + e);
						C8bmatches = C8.computeC8b(urlLower);
					}
				}
				//Set<String> C8bmatches = C8.computeC8b(urlLower);
				addResultForCriterie(object, "C8b", C8bmatches);
				
				//Calc C8c         'unions in htm (input: al text lowercased, tokenized)
				Set<String> C8cmatches = null;
				if (useStandardC8CTest || foreningerOnewordFileTokenSet == null) {
					C8cmatches = C8.computeC8cV5(TextUtils.copyTokens(tokens));
				} else {
					try {
						C8cmatches = C8.computeC8cAlt(TextUtils.copyTokens(tokens), foreningerOnewordFileTokenSet);
					} catch (Throwable e) {
						errorSb.append("new C8c calculation failed (reverting to standard): " + e);
						C8cmatches =  C8.computeC8cV5(TextUtils.copyTokens(tokens));
					}
				}
				addResultForCriterie(object, "C8c", C8cmatches);
				
				//Calc C9a         'company type Aps etc.
				Set<String> C9amatches = C9.computeC9a(text);
				addResultForCriterie(object, "C9a", C9amatches);
				//Calc C9b         'company names in htm
				// uses virksomheder_lowercased_UTF8.txt
				List<Set<String>> virksomhederFileTokenset = null;
				Set<String> C9bmatches = null;
				if (!useStandardC9BTest) {
					virksomhederFileTokenset = WordsArrayGenerator.getListOfTokens(virksomhederFile, errorSb);
				}
				if (useStandardC9BTest || virksomhederFileTokenset == null) {
					C9bmatches = C9.computeC9b(text);
				} else {
					C9bmatches = C9.computeC9bAlt(text, virksomhederFileTokenset);
				}
				addResultForCriterie(object, "C9b", C9bmatches);
				
				//Calc C9c         'company names in url
				List<Set<String>> virksomhederOneWordFileTokenset = null;
				Set<String> C9cmatches = null;
				if (!useStandardC9CTest) {
					virksomhederOneWordFileTokenset = WordsArrayGenerator.getListOfTokens(virksomhederOneWordFile, errorSb);
				}
				if (useStandardC9CTest || virksomhederOneWordFileTokenset == null) {
					C9cmatches = C9.computeC9c(urlLower);
				} else {
					C9cmatches = C9.computeC9cAlt(urlLower, TextUtils.copyTokens(virksomhederOneWordFileTokenset));
				}
				//Set<String> C9cmatches = C9.computeC9c(urlLower);
				addResultForCriterie(object, "C9c", C9cmatches);
				//Calc C9d         'company cvr
				Set<String> C9dmatches = C9.computeC9d(text);
				addResultForCriterie(object, "C9d", C9dmatches);
				//Calc C9e 		   'search for lowercase company-names (input: lowercased text tokenized,  Output: any matches)
				Set<String> C9ematches = null;
				if (useStandardC9ETest || virksomhederOneWordFileTokenset == null) {
					C9ematches = C9.computeC9eV5(TextUtils.copyTokens(tokens));
				} else {
					C9ematches = C9.computeC9eAlt(TextUtils.copyTokens(tokens), virksomhederOneWordFileTokenset);
				}
				//Set<String> C9ematches = C9.computeC9eV5(TextUtils.copyTokens(tokens));
				addResultForCriterie(object, "C9e", C9ematches);
				//Calc C9f          'search for lowercased cvr-number, input: al text lowercased, output: y/n
				boolean C9f = C9.computeC9f(text);
				//result.append(", C9f: " + (C9f? "y": "n"));
				object.put("C9f", (C9f? "y": "n"));
				
				//Calc C10a         'surname patterns (input: lowercased text, tokenized, output: )
				Set<String> C10amatches = C10.computeC10a(text);
				addResultForCriterie(object, "C10a", C10amatches);
				//Calc C10b         'freq. person names (input: lowercased text, output: any found names in the text)
				Set<String> C10bmatches = C10.computeC10b(text);
				addResultForCriterie(object, "C10b", C10bmatches);
				
				/////////////////////////////////////////////////////////////
				//Calc C10c	
				List<Set<String>> danishNamesTokenSet = null;
				Set<String> C10cmatches = null;
				if (!useStandardC10CTest) { // try to extract tokens 
					danishNamesTokenSet = WordsArrayGenerator.getListOfTokens(danishNamesFile, errorSb);
				}
				if (useStandardC10CTest || danishNamesTokenSet == null) {
					C10cmatches = C10.computeC10cV5(TextUtils.copyTokens(tokens));
				} else {
					C10cmatches = C10.computeC10cV5Alt(text, TextUtils.copyTokens(tokens), danishNamesTokenSet);
				}
				//Set<String> C10cmatches = C10.computeC10cV5(TextUtils.copyTokens(tokens));
				addResultForCriterie(object, "C10c", C10cmatches);     

				/////////////////////////////////////////////////////////////
				// Calc C15a      'The URL belongs to a TLD often used by Danes (The list currently comprises .dk, .no, .se, .de, .eu, .org, .com, .net, .nu, .tv, .info)
				String C15a = C15.computeC15a(hostname);
				object.put("C15a", (C15a==null? "n": "y: " + C15a));
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
			} else {
				object.put("CError", "No errors during analysis");
			}
		}
		return object.toJSONString();
	}

	@SuppressWarnings("unchecked")
	private void addResultForCriterie(JSONObject jo, String criteria, Set<String> matches) {
		String result = null;
		if (matches == null) {
			jo.put(criteria, "Null-resultset computed for criteria " +  criteria);
			return;
		}
		try {
			result = matches.size() + "";
			if (matches.size() > 0) {
				result = matches.size() + " " 
						+ TextUtils.conjoin("#", matches);
			} 
		} catch (Throwable e) {
			jo.put(criteria, "Unable to compute criteria '" + criteria + "': " + e);
			return;
		}
		jo.put(criteria, result);
	}

/*	
	
	private String getCityfilePath(StringBuilder error) {
		try {
			String citynamesFilePath = dk.kb.webdanica.core.utils.Settings.get(WebdanicaSettings.PIG_CITYNAMES_FILEPATH);
			return citynamesFilePath;
		} catch (Throwable e) {
			error.append("Unable to retrieve Cityfilepath: " + e);
		}
		return null;
	}
*/	
}

