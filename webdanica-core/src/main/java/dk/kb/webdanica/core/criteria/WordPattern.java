package dk.kb.webdanica.core.criteria;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class WordPattern {

	public Pattern p;
	public String w;

	public WordPattern(Pattern ip,String iw) {
		this.p = ip;
		this.w = iw;
	}

	public static Set<WordPattern> getCompiledPatterns(String[] words) {
		Set<WordPattern> res = new HashSet<WordPattern>();
		for (String word: words) {
			if (!word.trim().isEmpty()) {
				String re = "(?i)\\b(?:" + word + ")\\b";
				Pattern pRe = Pattern.compile(re);
				WordPattern wp = new WordPattern(pRe,word);
				res.add(wp);
			}
		}
		return res;
	}

	public static Set<WordPattern> getCompiledPatterns(Set<String> words) {
		Set<WordPattern> res = new HashSet<WordPattern>();
		for (String word: words) {
			if (!word.trim().isEmpty()) {
				String re = "(?i)\\b(?:" + word + ")\\b";
				Pattern pRe = Pattern.compile(re);
				WordPattern wp = new WordPattern(pRe,word);
				res.add(wp);
			}
		}
		return res;
	}

	public static Set<WordPattern> getCompiledPatternsNoCase(String[] words) {
		Set<WordPattern> res = new HashSet<WordPattern>();
		for (String word: words) {
			if (!word.trim().isEmpty()) {
				String re = "\\b(?:" + word + ")\\b";
				Pattern pRe = Pattern.compile(re);
				WordPattern wp = new WordPattern(pRe,word);
				res.add(wp);
			}
		}
		return res;
	}

	public static Set<WordPattern> getCompiledPatternsNoCase(Set<String> words) {
		Set<WordPattern> res = new HashSet<WordPattern>();
		for (String word: words) {
			if (!word.trim().isEmpty()) {
				String re = "\\b(?:" + word + ")\\b";
				Pattern pRe = Pattern.compile(re);
				WordPattern wp = new WordPattern(pRe,word);
				res.add(wp);
			}
		}
		return res;
	}
}
