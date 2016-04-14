package dk.kb.webdanica.criteria;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

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
        
        generateWordArraysFromFile(f7);
        //runTestsOnFile(f4, frequent150words);
        
        
        
    }
    
    public static void generateWordArraysFromFile(File f) throws IOException {
        BufferedReader fr = null;    
        try {
            fr = new BufferedReader(new FileReader(f));
            String line;
            int linesread = 0;
            while ((line = fr.readLine()) != null) {
                linesread++;
                String trimmed = (line.trim()).toLowerCase();
                System.out.print("\"" + trimmed + "\", ");
                if (linesread == 10) {
                    System.out.println();
                    linesread = 0;
                }
            }
            System.out.println();
        } finally {
            IOUtils.closeQuietly(fr);
        }
    }
}

