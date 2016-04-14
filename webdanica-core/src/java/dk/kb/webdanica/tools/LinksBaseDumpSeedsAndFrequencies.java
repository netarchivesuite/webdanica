package dk.kb.webdanica.tools;

import java.io.File;

import dk.kb.webdanica.criteria.LinksBase;

public class LinksBaseDumpSeedsAndFrequencies {
 
    /**
     * @param args inputfile databasedir Must exist previously
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Need a databasedir");
            System.exit(1);
        }
 
        String databasedirAsString = args[0];
         
        File databasedir = new File(databasedirAsString);
        
        if (!databasedir.isDirectory()) {
            System.err.println("The given databasedir '" + databasedir.getAbsolutePath()
                    + "' does not exist or is not a directory.");
            System.exit(1);
        }
        
        LinksBase lb = new LinksBase(databasedir);
        File allSeeds = lb.getAllSeedsAndTheirFrequencies();
        System.out.println("All seeds and frequencies are found in the file: " + allSeeds.getAbsolutePath()); 
        lb.cleanup();
    }
}
