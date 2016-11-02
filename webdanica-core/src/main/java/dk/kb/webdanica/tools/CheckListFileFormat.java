package dk.kb.webdanica.tools;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import dk.kb.webdanica.criteria.WordsArrayGenerator;

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
		
		Set<String> words = new HashSet<String>();
		try {
			words = WordsArrayGenerator.generateWordSetFromFile(listfile, charset, "\t", true, false);   
		} catch (Throwable e) {
			System.err.println("Exception while parsing the file " + listfile.getAbsolutePath() + ": " + e);
			System.exit(1);
		}
		System.out.println("The file '" +  listfile.getAbsolutePath() + "' contains " + words.size() + " words:");
		long count = 0;
		for (String word: words) {
			count++;
			System.out.println("word #" +  count + ": '" + word + "'");
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
