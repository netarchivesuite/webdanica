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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import dk.netarkivet.common.utils.DomainUtils;

/**
 * Inddata 1: Fil med liste af links udtaget af Berkeley DB JE database
 * Inddata 2: katalog til at lægge seedlister for hvert domæne
 * 
 * For hvert tld, laves der filen "seedlist-" + TLD + ".txt" 
 */
public class LinksProcessor {

    /**
     * @param args Fil med liste af links udtaget af Berkeley DB JE database.
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        File inputFile = new File(args[0]);
        BufferedReader fr = new BufferedReader(new FileReader(inputFile));
        Map<String,Set<String>> domainsMap = new HashMap<String, Set<String>>();
        //domainsMap.put("skipped_urls", new HashSet<String>());
        //domainsMap.put("garbage_urls", new HashSet<String>());
        String line;
        
        File skippedUrls = new File("skipped_urls-14-11-2014.txt");
        Writer skippedUrlsWriter = makeWriter(skippedUrls);
        File garbageUrls = new File("garbage_urls-14-11-2014.txt");
        Writer garbageWriter = makeWriter(garbageUrls);
        File urlCandidates = new File("candidate_urls-14-11-2014.txt");
        Writer urlcandidateWriter = makeWriter(urlCandidates);
        long linecount=0L;
        String trimmedLine = null;
        while ((line = fr.readLine()) != null) {
            trimmedLine = line.trim();
            if (!trimmedLine.isEmpty()) {
                linecount++;
                if (skipLine(trimmedLine)) {
                    //
                    //Set<String> skipped = domainsMap.get("skipped_urls");
                    //skipped.add(trimmedLine);
                    skippedUrlsWriter.write(trimmedLine + "\n");
                } else { 
                    String domain = findDomain(trimmedLine);
                    if (domain == null) {
                        garbageWriter.write(trimmedLine + "\n");                        
                    } else {
                        urlcandidateWriter.write(trimmedLine + "\n");
                        /*if (!domainsMap.containsKey(domain)) {
                            domainsMap.put(domain, new HashSet<String>());
                        }
                        Set<String> domainUrls = domainsMap.get(domain);
                        domainUrls.add(trimmedLine);
                        */
                }    
                }
            } else {
                //System.out.println("Skipping line '" + line + "'");
            }
        }
        
        for (String key: domainsMap.keySet()) {
          String domain = key;
          domain.replaceAll("\\.", "_");
          Set<String> urls = domainsMap.get(key);
          System.out.println("domain '" + key + ": " + urls.size());
        }
        
        System.out.println("linecount: " + linecount);
        System.out.println("Last line: " + trimmedLine);
        IOUtils.closeQuietly(fr);   
    }

    private static boolean skipLine(String trimmedLine) {
        String[] ignoredExts = new String[]{
                ".jpg", ".avi", ".waw", ".gif",
                ".bmp", ".doc", ".docx", ".dot", 
                ".eps", ".exe", ".jp2", ".jpe",".jpeg",
                ".mdb", ".mov", ".mp3", ".mp4", ".mpeg",
                ".odt",
                ".pdd",
                ".pdf",
                ".pict", 
                ".png", ".psd", ".rar", ".raw", 
                ".rtf", ".swf", 
                ".tif",
                ".tiff",
                ".wps", ".xls" };

          for (String ext: ignoredExts) {
            if (trimmedLine.endsWith(ext)) {
                return true;
            }
        }
        return false;
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
