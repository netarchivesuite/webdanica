package dk.kb.webdanica.tools.seeds;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;


/**
 * 
 * Take a directory with seed-files and merge them into larger seedlists
 * suffix = "-seeds.txt"
 * The rest inbetween is just a counter
 */
public class PartitionSeeds {

    /**
     * @param args A directory with seed-files, a size of seedlists, and a prefix
     * @throws IOException 
     * 
     */
    public static void main(String[] args) throws IOException {
       if (args.length != 3) {
           System.err.println("Missing args: dir seedlistsize prefix");
           System.exit(1);
       }
       File dir = new File(args[0]);
       if (!dir.isDirectory()) {
           System.err.println("The given filepath '" + dir.getAbsolutePath() + "' does not point to a directory");
           System.exit(1);
       }
       int size = Integer.parseInt(args[1]);
       
       
       String prefix = args[2];
       String suffix = "-seeds.txt";
       int count=0;
       int filecount=0;
       
       File[] filelist = dir.listFiles();
       File outSeedsFile = new File(prefix + filecount + suffix);
       Writer outWriter = makeWriter(outSeedsFile);
               
       for (File f: filelist) {
           if (f.isDirectory()) {
               System.err.println("Ignoring subdirectory '" + f.getAbsolutePath() + "'");   
           } else {
               BufferedReader fr = new BufferedReader(new FileReader(f));        
               String line;
               while ((line = fr.readLine()) != null) {
                   count++;
                   if (count > size) {
                       filecount++;
                       outWriter.close();
                       outSeedsFile = new File(prefix + filecount + suffix);
                       outWriter = makeWriter(outSeedsFile);
                       count=0;
                   }
                   outWriter.write(line + "\n");
                   
               }
               fr.close();
           }
       }
    }

    
    public static Writer makeWriter(File outputfile) throws IOException {
        BufferedWriter out = null;
        FileWriter fstream = new FileWriter(outputfile, true); //true tells to append data.
        out = new BufferedWriter(fstream);
        return out;       
    }
    
    
    
    
    

}
