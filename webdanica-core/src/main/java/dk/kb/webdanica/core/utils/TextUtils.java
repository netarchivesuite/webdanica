package dk.kb.webdanica.core.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.kb.webdanica.core.criteria.WordPattern;
import dk.netarkivet.common.webinterface.HTMLUtils;

public class TextUtils {

    public static Set<String> SearchWord(String text, String[] words) {
        Set<String> matches = new HashSet<String>();
        for (String word : words) {
            if (text.matches(toRegexp(word))) {
                matches.add(word);
            }
        }
        return matches;
    }

    public static Set<String> SearchWordRegExp(String text,
            Collection<String> words, boolean withCount) {
        Set<String> res = new HashSet<String>();
        for (String word : words) {
            String testRe = "(?i)\\b(?:" + word + ")\\b";
            Pattern ptestRe = Pattern.compile(testRe);
            if (ptestRe.matcher(text).find()) {
                if (withCount) {
                    int cnt = 0;
                    String[] p = ptestRe.split(text);
                    String s = text.substring(text.length() - word.length());
                    cnt = p.length - (s.matches(testRe) ? 0 : 1);
                    if (cnt > 0) {
                        res.add(cnt + ";" + word);
                    }
                } else {
                    res.add(word);
                }
            }
        }
        return res;
    }

    public static Set<String> SearchWordRegExp(String text, String[] words,
            boolean withCount) {
        Set<String> res = new HashSet<String>();
        for (String word : words) {
            String testRe = "(?i)\\b(?:" + word + ")\\b";
            Pattern ptestRe = Pattern.compile(testRe);
            if (ptestRe.matcher(text).find()) {
                if (withCount) {
                    int cnt = 0;
                    String[] p = ptestRe.split(text);
                    cnt = p.length - (p[p.length - 1].matches(testRe) ? 0 : 1);
                    if (cnt > 0) {
                        res.add(cnt + ";" + word);
                    }
                } else {
                    res.add(word);
                }
            }
        }
        return res;
    }

    public static Set<String> SearchWordPatterns(String text,
            Set<WordPattern> wpts, boolean withCount) {
        Set<String> res = new HashSet<String>();
        for (WordPattern wp : wpts) {
            if (wp.p.matcher(text).find()) {
                if (withCount) {
                    int cnt = 0;
                    String[] pl = wp.p.split(text);
                    cnt = pl.length
                            - (wp.p.matcher(pl[pl.length - 1]).find() ? 0 : 1);
                    res.add(cnt + ";" + wp.w);
                } else {
                    res.add(wp.w);
                }
            }
        }
        return res;
    }

    private static String toRegexp(String word) {
        // String res = "\\s*\\b" + word + "\\b\\s*";
        // before November: String res = "\\s+" + word + "\\s+";
        String res = ".*(\\b)(" + word + ")(\\b).*";

        // System.out.println("PATTERN = '" + res + "' ");
        return res;
        // return " " + word + " ";
    }

    public static <T> String conjoin(String sep, Collection<T> objects) {
        if (objects == null) {
            return null;
        }
        StringBuilder res = new StringBuilder();
        for (T o : objects) {
            if (res.length() != 0) {
                res.append(sep);
            }
            res.append(o);
        }
        return res.toString();
    }

    public static Set<String> SearchPattern(String text, String[] patterns) {
        Set<String> matches = new HashSet<String>();
        for (String pattern : patterns) {
            if (text.contains(pattern)) {
                matches.add(pattern);
            }
        }
        return matches;
    }

    public static Set<String> SearchPattern(String text,
            Collection<String> patterns) {
        Set<String> matches = new HashSet<String>();
        for (String pattern : patterns) {
            if (text.contains(pattern)) {
                matches.add(pattern);
            }
            /*
             * if (text.matches(".*" + pattern + ".*")) { matches.add(pattern);
             * }
             */
        }
        return matches;
    }

    public static Set<String> SearchWordSuffixPatterns(String text,
            String[] patterns) {
        Set<String> matches = new HashSet<String>();
        for (String pattern : patterns) {

            if (text.matches(".*\\b[\\w\\u00E6\\u00E5\\u00C6\\u00C5\\u00F8\\u00D8]+"
                    + pattern + "\\b.*")) {
                matches.add(pattern);
            }
        }
        return matches;
    }

    public static Set<String> SearchWord(String text, List<String> words) {
        Set<String> matches = new HashSet<String>();
        for (String word : words) {
            // System.out.println("Trying to match - " + word);
            if (text.matches(toRegexp(word))) {
                matches.add(word);
            }
        }
        // System.out.println("matches found: " + matches.size());
        return matches;

    }

    public static boolean PatternExists(String text, String pattern) {
        return text.matches(pattern);
    }

    public static Set<String> SearchWordPattern(String text,
            String[] especiallyNormalDanishWords) {
        Set<String> matches = new HashSet<String>();
        for (String pattern : especiallyNormalDanishWords) {
            Pattern p = Pattern
                    .compile("\\b" + pattern + "\\b", Pattern.DOTALL);
            Matcher m = p.matcher(text);
            if (m.matches()) {
                matches.add(pattern);
            }
        }
        return matches;
    }

    public static Set<String> tokenizeText(String text) {
        String[] words = text.split(" ");
        Set<String> tokens = new HashSet<String>();
        for (String word : words) {
            String wordTrimmed = word.trim();
            if (wordTrimmed.endsWith(",")) {
                // System.out.println("Token before: '" + wordTrimmed + "'");
                wordTrimmed = wordTrimmed
                        .substring(0, wordTrimmed.length() - 1);
                // System.out.println("Token after: '" + wordTrimmed + "'");
            }
            if (wordTrimmed.endsWith(".")) {
                wordTrimmed = wordTrimmed
                        .substring(0, wordTrimmed.length() - 1);
            }
            if (wordTrimmed.length() > 0) {
                tokens.add(wordTrimmed);
            }
        }
        return tokens;
    }

    public static Set<String> tokenizeUrl(String url, boolean cased) {
        // remove the protocol, if possible to identify it. This information is
        // meaningless
        final String protocolMarker = "://";
        if (url.contains(protocolMarker)) {
            url = url.substring(url.indexOf(protocolMarker) + 3, url.length());
        }

        // If the url is url-encoded i.e. contains "%" in the url then
        // url-decode it before processing
        String urldecoded = url;
        if (url.contains("%")) {
            urldecoded = HTMLUtils.decode(url);
        }
        Set<String> tokens = new HashSet<String>();
        String[] splitSlash = urldecoded.split("/");
        for (String s : splitSlash) {
            String sTrimmed = s.trim();
            if (sTrimmed.isEmpty()) {
                continue;
            }
            if (sTrimmed.contains(" ")) {
                Collections.addAll(tokens, sTrimmed.split(" "));
            } else {
                tokens.add(sTrimmed);
            }
        }
        Set<String> tokens1 = new HashSet<String>();
        for (String s : tokens) {
            if (s.contains(".")) {
                Collections.addAll(tokens1, s.split("\\."));
            } else {
                tokens1.add(s);
            }
        }
        /*
         * for (String word: words) { String wordTrimmed = word.trim(); if
         * (wordTrimmed.endsWith(",")) { //System.out.println("Token before: '"
         * + wordTrimmed + "'"); wordTrimmed = wordTrimmed.substring(0,
         * wordTrimmed.length() - 1); //System.out.println("Token after: '" +
         * wordTrimmed + "'"); } if (wordTrimmed.endsWith(".")) { wordTrimmed =
         * wordTrimmed.substring(0, wordTrimmed.length() - 1); } if
         * (wordTrimmed.length() > 0) { tokens.add(wordTrimmed); } }
         */
        return tokens1;
    }

    public static Set<String> findMatches(String text, String[] wordsToFind) {
        List<String> words = Arrays.asList(wordsToFind);
        return findMatches(text, words);
    }

    public static Set<String> findMatches(String text,
            Collection<String> wordsToFind) {
        Set<String> tokens = TextUtils.tokenizeText(text);
        tokens.retainAll(wordsToFind);
        return tokens;
    }

    public static Set<String> copyTokens(Set<String> tokens) {
        Set<String> res = new HashSet<String>();
        res.addAll(tokens);
        return res;
    }

    public static List<Set<String>> copyTokens(List<Set<String>> fileTokenSet) {
        List<Set<String>> resultList = new ArrayList<Set<String>>();
        for (Set<String> set : fileTokenSet) {
            Set<String> res = new HashSet<String>();
            res.addAll(set);
            resultList.add(res);
        }
        return resultList;
    }

    public static String findHarvestNameInStatusReason(String statusReason) {
        final String SPLITTER = "harvestname";
        String harvestName = null;
        boolean isTypeTwo = statusReason.contains("'");
        // System.out.println(isTypeTwo);
        if (!statusReason.contains(SPLITTER)) {
            // System.out.println("Can't find harvestname in field statusreason. marker has changed: '"
            // + statusReason + "'");
            return null;
        } else {
            String[] statusReasonParts = statusReason.split(SPLITTER);
            if (statusReasonParts.length > 1) {
                harvestName = statusReasonParts[1].trim();
                if (!isTypeTwo) {
                    String[] harvestNameparts = harvestName.split(" ");
                    if (harvestNameparts.length == 2) {
                        return harvestNameparts[1];
                    } else {
                        return null;
                    }
                } else {
                    return harvestName.substring(harvestName.indexOf("'") + 1,
                            harvestName.lastIndexOf("'"));
                }
            } else {
                return null;
            }
        }
    }
}
