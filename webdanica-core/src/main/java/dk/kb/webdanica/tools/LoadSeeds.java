package dk.kb.webdanica.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import dk.kb.webdanica.datamodel.IngestLog;
import dk.kb.webdanica.datamodel.IngestLogDAO;
import dk.kb.webdanica.datamodel.Seed;
import dk.kb.webdanica.datamodel.SeedDAO;
import dk.kb.webdanica.datamodel.URL_REJECT_REASON;
import dk.kb.webdanica.utils.UrlUtils;

/**
 * Program to load seeds into the webdanica database.
 * Policy: 
 *  - Seeds already present in the database is ignored
 *  - seeds that are not proper URLs are not loaded into the database
 *  - seeds that have extensions matching any of the suffixes in our ignored suffixes are also skipped
 *  
 *  TODO Program is called with -Dwebdanica.settings.file=/full/path/to/webdanica_settings_file
 *  
 *  TESTED with webdanica-core/src/main/resources/outlink-reportfile-final-1460549754730.txt
 *  TESTED with webdanica-core/src/main/resources/outlinksWithAnnotations.txt
 *  TESTED with webdanica-core/src/main/resources/webdanica-seeds.table
 */
public class LoadSeeds {

	private File seedsfile;

	public LoadSeeds(File seedsfile) {
	   this.seedsfile = seedsfile;
    }

	public static void main(String[] args) {
		
		if (args.length != 1) {
			System.err.println("Need seedsfile as argument");
			System.exit(1);
		}
		File seedsfile = new File(args[0]);
		if (!seedsfile.isFile()){
			System.err.println("The seedsfile located '" + seedsfile.getAbsolutePath() + "' does not exist or is not a proper file");
			System.exit(1);
		}
		LoadSeeds loadseeds = new LoadSeeds(seedsfile);
		IngestLog res = loadseeds.processSeeds();
		System.out.println(res);
	}
	/**
	 * @return the ingestLog for the file just processed
	 */
	public IngestLog processSeeds() {
		SeedDAO dao = SeedDAO.getInstance();
		String line;
        long linecount=0L;
        long insertedcount=0L;
        long rejectedcount=0L;
        long duplicatecount=0L;
        String trimmedLine = null;
        BufferedReader fr = null;
        List<String> logentries = new ArrayList<String>();
        try {
        	fr = new BufferedReader(new FileReader(seedsfile));
	        while ((line = fr.readLine()) != null) {
	            trimmedLine = line.trim();
	            
	            trimmedLine = removeAnnotationsIfNecessary(trimmedLine);
	         
	            linecount++;
	            URL_REJECT_REASON rejectreason = UrlUtils.isRejectableURL(trimmedLine);
	            if (rejectreason == URL_REJECT_REASON.NONE) {
	            	Seed singleSeed = new Seed(trimmedLine);
	            	boolean inserted = dao.insertSeed(singleSeed);
	            	if (!inserted) { // assume duplicate url 
	            		rejectreason = URL_REJECT_REASON.DUPLICATE;
	            		duplicatecount++;
	            	} else {
	            		insertedcount++;
	            	}
	            }
	            if (rejectreason != URL_REJECT_REASON.NONE) {
	            	logentries.add(rejectreason + ": " + trimmedLine);
	            	rejectedcount++;
	            }
	            
	        }
        } catch (Throwable e) {
	        e.printStackTrace();
        } finally {
        	IOUtils.closeQuietly(fr);
        	dao.close();
        }
	
	    IngestLog logresult = logIngestStats(logentries, linecount, insertedcount, rejectedcount, duplicatecount); 
	    return logresult;
	}

	private String removeAnnotationsIfNecessary(String trimmedLine) {
	    String[] trimmedParts = trimmedLine.split(" ");
	    if (trimmedParts.length > 1) {
	    	return trimmedParts[0];
	    } else {
	    	return trimmedLine;
	    }
    }

	private IngestLog logIngestStats(List<String> logentries, long linecount, long insertedcount, long rejectedcount, long duplicatecount) {
			IngestLogDAO dao = IngestLogDAO.getInstance();
			IngestLog log = new IngestLog(logentries, seedsfile.getName(), linecount, insertedcount, rejectedcount, duplicatecount);
			dao.insertLog(log);
			dao.close();
			return log;
		}
}
