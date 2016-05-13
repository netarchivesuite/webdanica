package dk.kb.webdanica.oldtools;

import java.io.File;

import dk.kb.webdanica.criteria.LinksBase;

public class LinksBaseDumpSeedsNotInOtherBase {
 
    /**
     * @param args inputfile databasedir Must exist previously
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Need two databasedirs: newDb oldDB");
            System.exit(1);
        }
 
        
        String databasedirAsString = args[0];
        String databasedir1AsString = args[1];
         
        File databasedir = new File(databasedirAsString);
        
        File databasedir1 = new File(databasedir1AsString);
        
        if (!databasedir.isDirectory()) {
            System.err.println("The given databasedir '" + databasedir.getAbsolutePath()
                    + "' does not exist or is not a directory.");
            System.exit(1);
        }
        
        if (!databasedir1.isDirectory()) {
            System.err.println("The given databasedir '" + databasedir1.getAbsolutePath()
                    + "' does not exist or is not a directory.");
            System.exit(1);
        }

        LinksBase lb = new LinksBase(databasedir);
        
        LinksBase oldDb = new LinksBase(databasedir1);
        
        
        File allSeeds = lb.getAllSeeds(oldDb);
        System.out.println("All seeds are found in the file: " + allSeeds.getAbsolutePath()); 
        lb.cleanup();
    }
}
