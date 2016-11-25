package dk.kb.webdanica.core.tools;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import dk.kb.webdanica.core.criteria.WordsArrayGenerator;

/**
 * Tested on file /home/svc/devel/webdanica/Bynavne_JEI_UTF16.txt
 */
public class CheckListFileFormat {

	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Missing required argument: file"); 
			System.exit(1);
		}
		
		File listfile = new File(args[0]);
		if (!listfile.isFile()) {
			System.err.println("The given file-argument '" + listfile.getAbsolutePath() + "' does not exist or is not a proper file");
			System.exit(1);
		}
		String charset = findCharsetFromName(listfile.getName());
		/*
		if (charset.toUpperCase().equals("UTF16")) {
			charset = "UTF-16";
		}
		 */
		String defaultCharset =  "UTF-16";
		
		List<Set<String>> wordsSet = null;
		List<Set<String>> wordsDefaultSet = null;
		
		try {
			wordsSet = WordsArrayGenerator.generateWordSetFromFile(listfile, charset, "\t", true, false);
			wordsDefaultSet = WordsArrayGenerator.generateWordSetFromFile(listfile, defaultCharset, "\t", true, false);
		} catch (Throwable e) {
			System.err.println("Exception while parsing the file " + listfile.getAbsolutePath() + ": " + e);
			System.exit(1);
		}
		long singleSize = wordsSet.get(0).size();
		long doubleSize = wordsSet.get(1).size(); 
		System.out.println("The file '" +  listfile.getAbsolutePath() + "' contains " + singleSize + " single words, and " +  doubleSize + " double words:");
		System.out.println("The doublewords: ");
		long count = 0;
		for (String word: wordsSet.get(1)) {
			count++;
			System.out.println("word #" +  count + ": '" + word + "'");
		}
		System.out.println("The singlewords: ");
		count = 0;
		for (String word: wordsSet.get(0)) {
			count++;
			System.out.println("word #" +  count + ": '" + word + "'");
		}
		count = 0;
		
		Set<String> AllwordsInwordsSet = new TreeSet<String>();
		AllwordsInwordsSet.addAll(wordsSet.get(0));
		AllwordsInwordsSet.addAll(wordsSet.get(1));
		Set<String> AllwordsInwordsDefaultSet = new TreeSet<String>();
		AllwordsInwordsDefaultSet.addAll(wordsDefaultSet.get(0));
		AllwordsInwordsDefaultSet.addAll(wordsDefaultSet.get(1));
	
		AllwordsInwordsSet.removeAll(AllwordsInwordsDefaultSet);
		
		if (AllwordsInwordsSet.size() != 0) {
			System.out.println("Removing all words also in the set read with UTF-16 charset gives a set of size " + AllwordsInwordsSet.size());
			System.out.println("This means that the file '" +  listfile.getAbsolutePath() + "' is not a '" + defaultCharset + "' file");
		}
	}

	private static String findCharsetFromName(String name) {
	    int index = name.lastIndexOf(".");
	    if (index == -1) {
	    	System.err.println("Unable to deduce the charset from the filename '" + name + "': No suffix found");
	    	System.exit(1);
	    }
	    String udenSuffix = name.substring(0, index);
	    index = udenSuffix.lastIndexOf("_");
	    if (index == -1) {
	    	System.err.println("Unable to deduce the charset from the filenamepart '" + udenSuffix + "': missing correct separator (_)");
	    	System.exit(1);
	    }
	    
	    return udenSuffix.substring(index+1, udenSuffix.length());
    }

}
