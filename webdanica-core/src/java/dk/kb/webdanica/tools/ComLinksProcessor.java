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
 * Inddata 1: Fil med liste af COM links udtaget af Berkeley DB JE database.
     * (hvor alle fejlURLER og garbage er filtreret fra)
 * Inddata 2: katalog til at lægge seedlister for com. og skippede seedlister fra 
 * facebook.com/sharer
 */
public class ComLinksProcessor {

    /**
     * @param args Fil med liste af links udtaget af Berkeley DB JE database.
     * (hvor alle fejlURLER og garbage er filtreret fra)
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        File inputFile = new File(args[0]);
        File outputDir = new File(args[1]);
        if (!outputDir.isDirectory()) {
            System.err.println("Directory '" +  outputDir.getAbsolutePath() 
                    + "' does not exist");
            System.exit(1);
        }
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
        System.out.println("Last line: " + trimmedLine);
        IOUtils.closeQuietly(fr);   
    }

   
    private static void writeToSeedList(File outdir, String trimmedLine, String domain) throws Exception {
        String[] tldParts = domain.split("\\.");
        String tld=null;
        if (tldParts.length > 0) {
            tld = tldParts[tldParts.length-1];
        } else {
            throw new Exception("Unable to split on '.' on domain '" + domain + "'");
        }
        File out = null;
        if (trimmedLine.contains("facebook.com/sharer")) {
            out = new File(outdir, "facebook.com_sharer_seeds.txt");    
        } else {
            out = new File(outdir, "tld-" + tld + "-seeds.txt");
        }
        Writer outWriter = makeWriter(out);
        outWriter.write(trimmedLine + "\n");
        outWriter.close();
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
