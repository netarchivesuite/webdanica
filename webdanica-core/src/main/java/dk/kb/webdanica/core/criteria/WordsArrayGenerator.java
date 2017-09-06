package dk.kb.webdanica.core.criteria;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;

import dk.kb.webdanica.core.datamodel.criteria.CriteriaUtils;

public class WordsArrayGenerator {

	/**
     * Program for generating String arrays from files. Assumes one entry pr line. 
     * @param args Not used
     * @throws IOException If file not found
     */
    public static void main(String[] args) throws IOException {
        File f = new File("korpus/frekvens150-utf8.txt");
        File f1 = new File("korpus/frekvens250sub-uden-htmlencoding.txt");
        File f2 = new File("korpus/frekvens250adj-uden-htmlencoding.txt");
        File f3 = new File("korpus/frekvens250verb-uden-htmlencoding.txt");
        File f4 = new File("korpus/testfile.txt");
        File f5 = new File("korpus/stednavneefterled-utf8.txt");
        File f6 = new File("korpus/Danske_Byer_utf8.txt");
        File f7 = new File("korpus/Danske_foreninger_utf8.txt");
        File f8 = new File("korpus/Danske_Virksomheder_utf8.txt");
        File f9 = new File("korpus/names_utf8.txt");
        File f10 = new File("/home/svc/devel/webdanica/Bynavne_JEI_UTF16.txt"); 
        List<Set<String>> WordSet = generateWordSetFromFile(f10, "UTF-16", "\t", true, false);
        Set<String> singleSet = WordSet.get(0);
        Set<String> doubleSet = WordSet.get(1);
        System.out.println("single words found: ");
        for (String s: singleSet){
        	System.out.println(s);
        }
        System.out.println("Double words found: ");
        for (String s: doubleSet){
        	System.out.println(s);
        }
        System.out.println("size of Singleset: " + singleSet.size());
        System.out.println("size of doubleSet: " + doubleSet.size());
        //runTestsOnFile(f4, frequent150words);       
    }
    
    public static List<Set<String>> generateWordSetFromFile(File f, String charset, String separator, boolean ignoreHeaderline, boolean toLowercase) throws IOException {
        BufferedReader fr = null;
        List<Set<String>> results = new ArrayList<Set<String>>();
        Set<String> resultSetSingle = new TreeSet<String>();
        Set<String> resultSetDouble = new TreeSet<String>();
        boolean seenFirstLine = false;
        try {
        	InputStream is = new FileInputStream(f);
            fr = new BufferedReader(new InputStreamReader(is, charset));
            String line;
            while ((line = fr.readLine()) != null) {
            	String trimmedLine = line.trim();
            	if (!seenFirstLine) {
            		seenFirstLine = true;
            		if (ignoreHeaderline) {
            			continue;
            		}
            	}
            	String[] parts = trimmedLine.split(separator);
                for (String p: parts){
                	String word = p.trim();
                	if (!word.isEmpty()) {
                		if (toLowercase) {
                			word = word.toLowerCase();
                		}
                		if (word.contains(" ")) {
                    		resultSetDouble.add(word);
                    	} else {
                    		resultSetSingle.add(word);
                    	}
                	}
                }
            }
        } finally {
            IOUtils.closeQuietly(fr);
        }
        results.add(resultSetSingle);
        results.add(resultSetDouble);
        return results;
    }
    
    public static List<Set<String>> getListOfTokens(File externalFile, StringBuilder error){
    	List<Set<String>> words;
    	String defaultCharset = "UTF-16";
        try {
        	String charset = CriteriaUtils.findCharsetFromName(externalFile.getName());
        	if (charset==null){
        		error.append("Error during getListOfTokens: Unable to deduce charset from filename '" + externalFile.getName() + "'. Assuming default charset '"+ defaultCharset + "'");
        		charset = defaultCharset;
        	}
	        words = WordsArrayGenerator.generateWordSetFromFile(externalFile, charset, "\t", true, false);
        } catch (IOException e) {
        	error.append("Error during getListOfTokens: " + e);
        	return null;
        }
    	return words; 
    }
    
    
    
    
}

