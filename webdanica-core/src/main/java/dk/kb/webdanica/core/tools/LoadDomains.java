package dk.kb.webdanica.core.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Tool for ingesting domains into the webdanica system.
 * Usage java LoadDomains domainfile [--accept] 
 * 
 * If --accept is used, the domains are marked as danica
 * Otherwise their state is Unknown
 * 
 * In any case, if the domain is already in the domains table, the domain is unchanged
 */
public class LoadDomains {
	
	private final File domainfile;

	public LoadDomains(File domainfile, boolean acceptAsDanica) {
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
	if (args.length < 1) {
        System.err.println("Need domainfile as argument.");
        PrintUsage();
        System.exit(1);
    } else if (args.length > 2) {
    	System.err.println("Too many arguments. Only two accepted.");
        PrintUsage();
        System.exit(1);
    }
	
    File seedsfile = new File(args[0]);
    if (!seedsfile.isFile()){
        System.err.println("The domainfile located '" + seedsfile.getAbsolutePath() + "' does not exist or is not a proper file");
        System.exit(1);
    }
    boolean acceptAsDanica = false;
    if (args.length == 2){
    	if (args[1].equalsIgnoreCase("--accept")) {
    		acceptAsDanica = true;
    	} else {
    		System.err.println("Unknown argument '" + args[1] + "' ignored.");
    	}
    }
    
    System.out.println("Processing domains from file '" + seedsfile.getAbsolutePath() + "'. AcceptAsDanica= " +  acceptAsDanica);
    
    System.out.println();
    LoadDomains loadseeds = new LoadDomains(seedsfile, acceptAsDanica);
	}

	private static void PrintUsage() {
		System.err.println("Usage: java LoadDomains domainsfile [--accept]");
	    
    }
}
