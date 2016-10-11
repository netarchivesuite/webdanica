package dk.kb.webdanica.criteria;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;

import dk.kb.webdanica.WebdanicaSettings;
import dk.netarkivet.common.utils.Settings;

public class WordsArrayGenerator {

    private static Set<String> cityNames;

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
        File f10 = new File("/home/svc/devel/webdanica/Bynavne_JEI.txt");
        Set<String> WordSet = generateWordSetFromFile(f10, "UTF-16", "\t", true, false);
        
        for (String s: WordSet){
        	System.out.println(s);
        }
        System.out.println("size of set: " + WordSet.size());
        //runTestsOnFile(f4, frequent150words);       
    }
    
    public static Set<String> generateWordSetFromFile(File f, String charset, String separator, boolean ignoreHeaderline, boolean toLowercase) throws IOException {
        BufferedReader fr = null;
        Set<String> resultSet = new TreeSet<String>();
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
                	String trimmed  = p.trim();
                	if (!trimmed.isEmpty()) {
                		if (toLowercase) {
                			resultSet.add(trimmed.toLowerCase());
                		} else {
                			resultSet.add(trimmed);
                		}
                	}
                }
            }
        } finally {
            IOUtils.closeQuietly(fr);
        }
        return resultSet;
    }

	public synchronized static Set<String> getCityNames() {
	    if (cityNames == null) {
	    	Settings.get(WebdanicaSettings.PIG_CITYNAMES_FILEPATH);
	    }
	    return null;
    }    
}

