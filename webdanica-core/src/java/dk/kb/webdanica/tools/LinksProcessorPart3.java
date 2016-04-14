package dk.kb.webdanica.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.IOUtils;

import dk.netarkivet.common.utils.DomainUtils;

/**
 * Inddata 1: Katalog over filer med seeds fra et tld (.com, .uk) 
 * Inddata 2: katalog til at lægge seedlister for hvert domæne
 * 
 * For hvert tld, laves der filen "seedlist-" + TLD + ".txt" 
 */
public class LinksProcessorPart3 {

    /**
     * @param args Fil med liste af links udtaget af Berkeley DB JE database.
     * (hvor alle fejlURLER og garbage er filtreret fra)
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        File inputDir = new File(args[0]);
        if (!inputDir.isDirectory()) {
            System.err.println("Directory '" +  inputDir.getAbsolutePath() 
                    + "' does not exist");
            System.exit(1);
        }
        File outputDir = new File(args[1]);
        if (!outputDir.isDirectory()) {
            System.err.println("Directory '" +  outputDir.getAbsolutePath() 
                    + "' does not exist");
            System.exit(1);
        }
        
        for (File inputFile: inputDir.listFiles()) {
            System.out.println("Processing file " + inputFile);
        
        BufferedReader fr = new BufferedReader(new FileReader(inputFile));        
        String line;
        /*
        File skippedUrls = new File("skipped_urls.txt");
        Writer skippedUrlsWriter = makeWriter(skippedUrls);
        File garbageUrls = new File("garbage_urls.txt");
        Writer garbageWriter = makeWriter(garbageUrls);
        File urlCandidates = new File("candidate_urls.txt");
        Writer urlcandidateWriter = makeWriter(urlCandidates);
        */
        long linecount=0L;
        String trimmedLine = null;
        while ((line = fr.readLine()) != null) {
            trimmedLine = line.trim();
            if (!trimmedLine.isEmpty()) {
                linecount++;
                String domain = findDomain(trimmedLine);
                writeToSeedList(outputDir, trimmedLine, domain);
            }
        }
        
        System.out.println("linecount: " + linecount);
        //System.out.println("Last line: " + trimmedLine);
        IOUtils.closeQuietly(fr);
        }
    }

   
    private static void writeToSeedList(File outdir, String trimmedLine, String domain) throws Exception {
        if (trimmedLine.contains("translate.google")) {
            File urlCandidates = new File(outdir, "translate.google.urls.txt");
            Writer urlcandidateWriter = makeWriter(urlCandidates);
            urlcandidateWriter.write(trimmedLine + "\n");
            urlcandidateWriter.close();
        } else {
        //tld.replaceAll(".", "_");
        String[] tldParts = domain.split("\\.");
        String tld=null;
        int length = tldParts.length;
        if (length > 1) {
            tld = tldParts[length-2] + "_" + tldParts[length-1];
        } else if (length > 0) {
            tld = tldParts[length-1];
        } else {
            throw new Exception("Unable to split on '.' on domain '" + domain + "'");
        }
        File out = new File(outdir, "tld-" + tld + "-seeds.txt"); 
        Writer outWriter = makeWriter(out);
        outWriter.write(trimmedLine + "\n");
        outWriter.close();
        }
    }


    private static String findDomain(String url) {
        //System.out.println("urlsize: " + url.length());
        URL theURL;
        try {
            theURL = new URL(url);
        } catch (MalformedURLException e) {
           return null;
        }
        String host = theURL.getHost();
        return DomainUtils.domainNameFromHostname(host);
    }
    
    public static Writer makeWriter(File outputfile) throws IOException {
        BufferedWriter out = null;
        FileWriter fstream = new FileWriter(outputfile, true); //true tells to append data.
        out = new BufferedWriter(fstream);
        return out;       
    }
    
    

}
