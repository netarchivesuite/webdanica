package dk.kb.webdanica.oldtools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/* functions and structures share by Mysql classes */

public class SeedsLowerCase {

    public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
        /*****************************************/
        /*** Arguments ***************************/
        /*****************************************/
    	String errArgTxt = "Proper args: " + "datadir=<data dir where 'url-dir' (for out-files) exixts> ";
    	
    	/**** args - data-dir ****/
	    String datadirTxt = args[0];
	    if (!datadirTxt.startsWith("datadir=")) {
	        System.err.println("Missing arg datadir setting - got " + datadirTxt);
	        System.err.println(errArgTxt);
	        System.exit(1);
	    }
	    
	    datadirTxt = MysqlX.getStringSetting(datadirTxt);
		//System.out.println("datadirTxt: " + datadirTxt);
	    File dataDir = new File(datadirTxt);
	    if (!dataDir.isDirectory()) {
	        System.err.println("ERROR: The given data-dir '" + dataDir.getAbsolutePath() + "' is not a proper directory or does not exist");
	        System.err.println(errArgTxt);
	        System.exit(1);
	    }
	    
	    File seeds1 = new File(dataDir.getAbsolutePath() + "/" + MysqlX.seeds_dir + "/" + MysqlX.seedfile1);
	    if (!seeds1.isFile()) {
	        System.err.println("ERROR: The given data-dir does not contains file '" + seeds1.getAbsolutePath() + "'");
	        System.err.println(errArgTxt);
	        System.exit(1);
	    }
	    File seeds2 = new File(dataDir.getAbsolutePath() + "/" + MysqlX.seeds_dir + "/" +  MysqlX.seedfile2);
	    if (!seeds2.isFile()) {
	        System.err.println("ERROR: The given data-dir does not contains file '" + seeds2.getAbsolutePath() + "'");
	        System.err.println(errArgTxt);
	        System.exit(1);
	    }
	    File seeds1_lc = new File(dataDir.getAbsolutePath() + "/" + MysqlX.seeds_dir + "/" + MysqlX.seedfile_lc1);
	    seeds1_lc.createNewFile();
	    File seeds2_lc = new File(dataDir.getAbsolutePath() + "/" + MysqlX.seeds_dir + "/" +  MysqlX.seedfile_lc2);
	    seeds2_lc.createNewFile();
	    
        /*****************************************/
        /*** Process   ***************************/
        /*****************************************/
        //make file to write status ,
	    
        System.out.println("Lowercase: " + seeds1.getAbsolutePath());
	    lowercaseFile(seeds1, seeds1_lc);

	    System.out.println("Lowercase: " + seeds2.getAbsolutePath());
	    lowercaseFile(seeds2, seeds2_lc);
    }
    
    public static void lowercaseFile(File srcFile, File lcFile)  throws IOException {
    	FileReader fr = new FileReader(srcFile);
		BufferedReader br = new BufferedReader(fr);        

    	FileWriter fw = new FileWriter(lcFile.getAbsoluteFile());
        BufferedWriter  bw = new BufferedWriter(fw);        

        String line;
        
        //get tablenames inf
        while ((line = br.readLine()) != null) {
            if (!line.isEmpty()) {
                bw.write(line.toLowerCase()); bw.newLine();
            }
        }
    	bw.close();
    	fw.close();
    	br.close();
    	fr.close();
    }
}
