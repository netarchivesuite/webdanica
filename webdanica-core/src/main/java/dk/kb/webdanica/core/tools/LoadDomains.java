package dk.kb.webdanica.core.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class LoadDomains {
	
	private final File domainfile;

	public LoadDomains(File domainfile) {
		this.domainfile = domainfile;
		
		BufferedReader fr = null;
        List<String> logentries = new ArrayList<String>();
        String line = null;
        String trimmedLine = null;
        try {
        	fr = new BufferedReader(new FileReader(domainfile));
	        while ((line = fr.readLine()) != null) {
	            trimmedLine = line.trim();
	            
	        }
        } catch (Throwable e) {
        	
        }
    }

	public static void main(String[] args) {
	if (args.length != 1) {
        System.err.println("Need domainfile as argument");
        System.exit(1);
    }
    File seedsfile = new File(args[0]);
    if (!seedsfile.isFile()){
        System.err.println("The domainfile located '" + seedsfile.getAbsolutePath() + "' does not exist or is not a proper file");
        System.exit(1);
    }
    
    System.out.println("Processing domains from file '" + seedsfile.getAbsolutePath() + "'");
    
    System.out.println();
    LoadDomains loadseeds = new LoadDomains(seedsfile);
	}
}
