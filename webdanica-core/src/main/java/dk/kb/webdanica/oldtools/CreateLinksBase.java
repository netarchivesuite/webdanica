package dk.kb.webdanica.oldtools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.apache.commons.io.IOUtils;
import dk.kb.webdanica.criteria.LinksBase;

public class CreateLinksBase {
 
    /**
     * @param args inputfile databasedir (is created if it does not exist)
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Missing either the datafile to ingest or the databasedir");
            System.exit(1);
        }
        //testdata: "/home/svc/resultater/output.extraction.arc-27-02-2014");
        String datafileAsString = args[0];
        String databasedirAsString = args[1];
        File input = new File(datafileAsString);        
        File databasedir = new File(databasedirAsString);
        if (!input.isFile()) {
            System.err.println("The given datafile '" +  input.getAbsolutePath()
                    + "' does not exist or is not a proper file");
            System.exit(1);
        }
        if (!databasedir.isDirectory()) {
            System.err.println("The given databasedir '" + databasedir.getAbsolutePath()
                    + "' does not exist or is not a directory.");
            System.exit(1);
        }
        
        LinksBase lb = new LinksBase(databasedir);
        ingestFile(input, lb);
        lb.cleanup();
    }

    public static void ingestFile(File inputfile, LinksBase database) throws Exception {
        BufferedReader fr = new BufferedReader(new FileReader(inputfile));
        String line;
        while ((line = fr.readLine()) != null) {
            String trimmedLine = line.trim();
            if (!trimmedLine.isEmpty() && !trimmedLine.startsWith("Url skipped")) {
                //System.out.println("Handling line " + line);
                if (!database.hasUrl(trimmedLine)) {
                    //System.out.println("Inserting url '" + trimmedLine + "'");                
                    database.put(trimmedLine, Long.valueOf(1L));
                } else {
                    Long currentFrequency = database.getFrequency(trimmedLine);
                    currentFrequency++;
                    database.put(trimmedLine, currentFrequency);
                    //System.out.println("Frequency for url '" + trimmedLine + "' is now " +
                    //currentFrequency.toString());
                }
            } else {
                //System.out.println("Skipping line '" + line + "'");
            }
        }
        IOUtils.closeQuietly(fr);   
    }    
}
