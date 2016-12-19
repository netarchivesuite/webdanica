package dk.kb.webdanica.core.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;

import dk.kb.webdanica.core.datamodel.DanicaStatus;
import dk.kb.webdanica.core.datamodel.Domain;
import dk.kb.webdanica.core.datamodel.IngestLog;
import dk.kb.webdanica.core.datamodel.Seed;
import dk.kb.webdanica.core.datamodel.Status;
import dk.kb.webdanica.core.datamodel.URL_REJECT_REASON;
import dk.kb.webdanica.core.datamodel.dao.DAOFactory;
import dk.kb.webdanica.core.datamodel.dao.DomainsDAO;
import dk.kb.webdanica.core.datamodel.dao.IngestLogDAO;
import dk.kb.webdanica.core.datamodel.dao.SeedsDAO;
import dk.kb.webdanica.core.utils.DatabaseUtils;
import dk.kb.webdanica.core.utils.UrlUtils;

/**
 * Program to load seeds into the webdanica database.
 * Policy: 
 *  - Seeds already present in the database is ignored
 *  - seeds that are not proper URLs are not loaded into the database
 *  - seeds that have extensions matching any of the suffixes in our ignored suffixes are also skipped
 *  
 *  Remember to call program with -Dwebdanica.settings.file=$LOADSEEDS_HOME/webdanica_settings_file
 *  and -Ddk.netarkivet.settings.file=$LOADSEEDS_HOME/settings_NAS_Webdanica.xml
 *  and -Dlogback.configurationFile=$LOADSEEDS_HOME/silent_logback.xml 
 *  
 *  TESTED with webdanica-core/src/main/resources/outlink-reportfile-final-1460549754730.txt
 *  TESTED with webdanica-core/src/main/resources/outlinksWithAnnotations.txt
 *  TESTED with webdanica-core/src/main/resources/webdanica-seeds.table
 */
public class LoadSeeds {

public static final String ACCEPT_ARGUMENT	= "--accepted";
	
public static void main(String[] args) throws Exception {
		boolean acceptSeedsAsDanica = false;
        if (args.length < 1 || args.length > 2) {
        	System.err.println("Wrong number of arguments. One or two is needed. Given was " + args.length + " arguments");
            System.err.println("Correct usage: java LoadSeeds seedsfile [--accepted]");
            System.err.println("Exiting program");
            System.exit(1);
        }
        File seedsfile = new File(args[0]);
        if (!seedsfile.isFile()){
            System.err.println("The seedsfile located '" + seedsfile.getAbsolutePath() + "' does not exist or is not a proper file");
            System.err.println("Exiting program");
            System.exit(1);
        }
        if (args.length == 2) {
        	if (args[1].equalsIgnoreCase(ACCEPT_ARGUMENT)) {
        		acceptSeedsAsDanica = true;
        	} else {
        		System.err.println("The second argument '" + args[1] + "' is unknown. Don't know what to do. Exiting program");
                System.exit(1);
        	}
        }
        
        System.out.println("Processing seeds from file '" + seedsfile.getAbsolutePath() + "'");
        if (acceptSeedsAsDanica) {
        	System.out.println("Ingesting all seeds as danica!");
        }
        
        System.out.println();
        LoadSeeds loadseeds = new LoadSeeds(seedsfile, acceptSeedsAsDanica);
        loadseeds.writeAcceptLog = true;
        loadseeds.writeRejectLog = true;
        loadseeds.writeUpdateLog = true;
        
        IngestLog res = loadseeds.processSeeds();
        System.out.println(res.getStatistics());
        File acceptLog = loadseeds.getAcceptLog();
        File rejectLog = loadseeds.getRejectLog();
        File updateLog = loadseeds.getUpdateLog();
        System.out.println("Acceptlog in file: " + (acceptLog==null?"No log written due to error": acceptLog.getAbsolutePath()));
        System.out.println("Rejectlog in file: " + (rejectLog==null?"No log written due to error": rejectLog.getAbsolutePath())); 
        System.out.println("Updatelog in file: " + (updateLog==null?"No log written due to error": updateLog.getAbsolutePath()));
        
    }
 	
	private File seedsfile;
    private boolean writeAcceptLog = false;
    private boolean writeRejectLog = false;
    private boolean writeUpdateLog = false;
	private File rejectLog = null;
	private File acceptLog = null;
	private File updateLog = null;
	private List<String> acceptedList = new ArrayList<String>();
    private DAOFactory daoFactory;
	private boolean ingestAsDanica;
	
	public LoadSeeds(File seedsfile, boolean acceptSeedsAsDanica) {
	   this.seedsfile = seedsfile;
	   this.daoFactory = DatabaseUtils.getDao();
	   this.ingestAsDanica = acceptSeedsAsDanica;
    }
	
	/**
	 * @return the ingestLog for the file just processed
	 * @throws Exception 
	 */
	public IngestLog processSeeds() {
		SeedsDAO dao = daoFactory.getSeedsDAO();
		DomainsDAO ddao = daoFactory.getDomainsDAO();
		String line;
        long linecount=0L;
        long insertedcount=0L;
        long rejectedcount=0L;
        long duplicatecount=0L;
        String trimmedLine = null;
        String url = null;
        BufferedReader fr = null;
        List<String> logentries = new ArrayList<String>();
        List<String> updatelogentries = new ArrayList<String>();
        List<String> domainLogentries = new ArrayList<String>();
        try {
        	fr = new BufferedReader(new FileReader(seedsfile));
	        while ((line = fr.readLine()) != null) {
	            trimmedLine = line.trim();
	            
	            trimmedLine = removeAnnotationsIfNecessary(trimmedLine);
	            url = trimmedLine;
	            linecount++;
	            URL_REJECT_REASON rejectreason = UrlUtils.isRejectableURL(url);
	            String errMsg = "";
	            if (rejectreason == URL_REJECT_REASON.NONE) {
	            	Seed singleSeed = new Seed(url);
	            	if (ingestAsDanica) {
	            		singleSeed.setDanicaStatus(DanicaStatus.YES);
	            		singleSeed.setDanicaStatusReason("Known by curators to be danica");
	            		singleSeed.setStatus(Status.DONE);
	            		singleSeed.setStatusReason("Set to Status done at ingest to prevent further processing of this url");
	            	}
	            	boolean inserted = false;
	            	boolean isError = false;
	            	try {
	            	    inserted = dao.insertSeed(singleSeed);
	            	    
	            	    if (inserted) {
	            	    	String domainName = singleSeed.getDomain();
	            	    	if (!ddao.existsDomain(domainName)) {
	            	    		Domain newdomain = Domain.createNewUndecidedDomain(domainName);
	            	    		boolean insertedDomain = ddao.insertDomain(newdomain);
	            	    		if (!insertedDomain) {
	            	    			domainLogentries.add("Failed to add domain '" + domainName + "' to domains table, the domain of seed '" + url + "'");
	            	    		} else {
	            	    			domainLogentries.add("Added domain '" + domainName + "' to domains table, the domain of seed '" + url + "'");
	            	    		}
	            	    	}
	            	    }
	            	} catch (Throwable e) {
	            	    rejectreason = URL_REJECT_REASON.BAD_URL;
	            	    errMsg = "Insertion of url failed: " + e.toString();
	            	}
	            	
	            	if (!inserted && !isError) { // assume duplicate url
	            		if (ingestAsDanica) {
	            			// update state of seed if not already in Status.DONE 
	            			Seed oldSeed = dao.getSeed(url);
	            			if (oldSeed == null) {
	            				// Should not happen
	            				rejectreason = URL_REJECT_REASON.UNKNOWN;
	            				errMsg = "The url '" + url + "' should have been in database. But no record was found";
	            			} else {
	            				if (oldSeed.getDanicaStatus().equals(DanicaStatus.YES)) {
	            					updatelogentries.add("The seed '" + url + "' is already in the database with DanicaStatus.YES and status '" + oldSeed.getStatus() + "'");
	            				} else {
	            					updatelogentries.add("The seed '" + url + "' is already in the database with DanicaStatus=" + oldSeed.getDanicaStatus() + ", and status '" +
	            							oldSeed.getStatus() + "'. Changing to DanicaStatus.YES and status.DONE");
	            					oldSeed.setDanicaStatus(DanicaStatus.YES);
	            					oldSeed.setDanicaStatusReason("Known by curators to be danica");
	            					oldSeed.setStatus(Status.DONE);
	            					oldSeed.setStatusReason("Set to Status done at ingest to prevent further processing of this url");
	            					dao.updateSeed(oldSeed);
	            				}
	            			}
	            		} else {
	            			rejectreason = URL_REJECT_REASON.DUPLICATE;
	            			duplicatecount++;
	            		}
	            	} else if(inserted) { 
	            		insertedcount++;
	            		acceptedList.add(url);
	            	}
	            }
	            
	            if (rejectreason != URL_REJECT_REASON.NONE) {
	            	logentries.add(rejectreason + ": " + url + " " + errMsg);
	            	rejectedcount++;
	            }
	            
	        }
	        
	        // add the updatedLog to logentries
	        for (String logUpdated: updatelogentries) {
	        	logentries.add("UPDATED: " + logUpdated);
	        }
	        
	        // add the domains to logentries
	        for (String logUpdated: domainLogentries) {
	        	logentries.add("DOMAINS: " + logUpdated);
	        }
	        
	        // write accept-log
	        if (writeAcceptLog) {
	        	acceptLog = new File(seedsfile.getParentFile(), seedsfile.getName() + ".accepted.txt");
	        	int count=0;
	        	while (acceptLog.exists()) {
	        		acceptLog = new File(seedsfile.getParentFile(), seedsfile.getName() + ".accepted.txt" + "." + count);
	        		count++;
	        	} 
	        	PrintWriter acceptWriter = new PrintWriter(new BufferedWriter(new FileWriter(acceptLog)));
	        	String acceptHeader = "Acceptlog for file '" + seedsfile.getAbsolutePath() + "' ingested at '" 
	        			+ new Date() + "'";
	        	String stats = "total lines: " + linecount + ", accepted = " + insertedcount + ", rejected=" + rejectedcount 
	        			+ " (of which " + duplicatecount + " duplicates";
	        	acceptWriter.println(acceptHeader);
	        	acceptWriter.println(stats);
	        	if (!acceptedList.isEmpty()) {
	        		acceptWriter.println("The " + insertedcount + " accepted :");
	        		for (String acc: acceptedList) {
	        			acceptWriter.println(acc);
	        		} 
	        	} else {
	        		acceptWriter.println("None were accepted!");
	        	}
	        	
	        	acceptWriter.close();
	        }
	        
	        // write reject-log
	        if (writeRejectLog) {
	        	rejectLog = new File(seedsfile.getParentFile(), seedsfile.getName() + ".rejected.txt");
	        	int count=0;
	        	while (rejectLog.exists()) {
	        		rejectLog = new File(seedsfile.getParentFile(), seedsfile.getName() + ".rejected.txt" + "." + count);
	        		count++;
	        	}
	        	PrintWriter rejectWriter = new PrintWriter(new BufferedWriter(new FileWriter(rejectLog)));
	        	String rejectHeader = "Rejectlog for file '" + seedsfile.getAbsolutePath() + "' ingested at '" 
	        			+ new Date() + "'";
	        	String stats = "total lines: " + linecount + ", accepted = " + insertedcount + ", rejected=" + rejectedcount 
	        			+ " (of which " + duplicatecount + " duplicates";
	        	rejectWriter.println(rejectHeader);
	        	rejectWriter.println(stats);
	        	rejectWriter.println("Rejected seeds:");
	        	for (String rej: logentries) {
	        		rejectWriter.println(rej);
	        	}
	        	rejectWriter.close();
	        }
	        
	     // write update-log
	        if (writeUpdateLog) {
	        	updateLog = new File(seedsfile.getParentFile(), seedsfile.getName() + ".updated.txt");
	        	int count=0;
	        	while (updateLog.exists()) {
	        		updateLog = new File(seedsfile.getParentFile(), seedsfile.getName() + ".updated.txt" + "." + count);
	        		count++;
	        	}
	        	PrintWriter updatedWriter = new PrintWriter(new BufferedWriter(new FileWriter(updateLog)));
	        	String updatedHeader = "Update and domain Log for file '" + seedsfile.getAbsolutePath() + "' ingested at '" 
	        			+ new Date() + "'";
	        	updatedWriter.println(updatedHeader);
	        	updatedWriter.println();
	        	if (!updatelogentries.isEmpty()) {
	        		updatedWriter.println("Update - entries:");
	        		for (String rej: updatelogentries) {
	        			updatedWriter.println(rej);
	        		}
	        	}
	        	if (!domainLogentries.isEmpty()) {
	        		updatedWriter.println("domain-log - entries:");
	        		for (String rej: domainLogentries) {
	        			updatedWriter.println(rej);
	        		}
	        	}
	        	updatedWriter.close();
	        }
	          
	        
	        
        } catch (Throwable e) {
	        e.printStackTrace();
        } finally {
        	IOUtils.closeQuietly(fr);
        	dao.close();
        }
        
	    IngestLog logresult = null;
        try {
            logresult = logIngestStats(logentries, linecount, insertedcount, rejectedcount, duplicatecount);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
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

	private IngestLog logIngestStats(List<String> logentries, long linecount, long insertedcount, long rejectedcount, long duplicatecount) throws Exception {
		IngestLogDAO dao = null;
		try {
			dao = daoFactory.getIngestLogDAO();
			IngestLog log = new IngestLog(logentries, seedsfile.getName(), linecount, insertedcount, rejectedcount, duplicatecount);
			dao.insertLog(log);
			return log;
		} finally {
			dao.close();
		}
	}
	
	File getRejectLog() {
		return this.rejectLog;
	}
	
	File getAcceptLog() {
		return this.acceptLog;
	}
	
	File getUpdateLog() {
		return this.updateLog;
	}
}
