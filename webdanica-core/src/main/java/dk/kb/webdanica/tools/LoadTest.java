package dk.kb.webdanica.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;


import dk.kb.webdanica.WebdanicaSettings;
import dk.kb.webdanica.datamodel.CriteriaResultsDAO;
import dk.kb.webdanica.datamodel.HarvestDAO;
import dk.kb.webdanica.datamodel.IngestLog;
import dk.kb.webdanica.datamodel.IngestLogDAO;
import dk.kb.webdanica.datamodel.Seed;
import dk.kb.webdanica.datamodel.SeedsDAO;
import dk.kb.webdanica.datamodel.Status;
import dk.kb.webdanica.datamodel.URL_REJECT_REASON;
import dk.kb.webdanica.datamodel.criteria.CriteriaIngest;
import dk.kb.webdanica.datamodel.criteria.SingleCriteriaResult;
import dk.kb.webdanica.datamodel.dao.CassandraDAOFactory;
import dk.kb.webdanica.datamodel.dao.DAOFactory;
import dk.kb.webdanica.datamodel.dao.HBasePhoenixDAOFactory;
import dk.kb.webdanica.interfaces.harvesting.HarvestReport;
import dk.kb.webdanica.utils.SettingsUtilities;
import dk.kb.webdanica.utils.UrlUtils;
import dk.netarkivet.common.utils.FileUtils;
import dk.netarkivet.harvester.datamodel.JobStatus;

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
public class LoadTest {

public static void main(String[] args) throws Exception {
        System.out.println("Starting LoadTest version 0.2 ...");
        if (args.length != 3) {
            System.err.println("Need arguments <seedsfile> <criteriaresultsdir> criteriaresultmultiples");
            System.exit(1);
        }
        File seedsfile = new File(args[0]);
        if (!seedsfile.isFile()){
            System.err.println("The seedsfile located '" + seedsfile.getAbsolutePath() + "' does not exist or is not a proper file");
            System.exit(1);
        }
        
        File criteriaResultsDir = new File(args[1]);
        if (!criteriaResultsDir.isDirectory()){
            System.err.println("The criteriaResultsDir located '" + criteriaResultsDir.getAbsolutePath() + "' does not exist or is not a proper directory");
            System.exit(1);
        }
        
        long criteriaresultmultiples = Long.parseLong(args[2]);
        
        System.out.println("Starting LoadTest at date '" + new Date() + "' using seeds from file '" + seedsfile.getAbsolutePath() + "', sample criteriaresultsdir = '" 
                +  criteriaResultsDir.getAbsolutePath() + "' and criteriaresultmultiples (used in step4) = " + criteriaresultmultiples);  
        
        LoadTest loadseeds = new LoadTest(seedsfile, criteriaResultsDir, criteriaresultmultiples);
        loadseeds.computeStats();    
        
        //System.out.println("Initial-seedscount= " + initialSeedsCount + " - time spent getting that in seconds: " + (millisEnded - millisStarted));
  
               
        loadseeds.writeAcceptLog = true;
        loadseeds.writeRejectLog = true;
        long millisStarted = System.currentTimeMillis();
        IngestLog res = loadseeds.insertSeeds();
        long millisEnded = System.currentTimeMillis();
        System.out.println("Time spent computing the seedsInsertion in secs: " + (millisEnded - millisStarted));
        
        System.out.println(res.getStatistics());
        File acceptLog = loadseeds.getAcceptLog();
        File rejectLog = loadseeds.getRejectLog();
        System.out.println("Acceptlog in file: " + (acceptLog==null?"No log written due to error": acceptLog.getAbsolutePath()));
        System.out.println("Rejectlog in file: " + (rejectLog==null?"No log written due to error": rejectLog.getAbsolutePath()));
       
        loadseeds.testStep3();
        
        //Ingest en million fake kriterie-resultater i criteria_results tabellen
        // Brug criteria_resultsdir som sample-data til at bruge som udgangspunkt 
        loadseeds.testStep4();
        
        
        // 5) Select alle harvests på en bestemt seed (evt. sorteret i tid) - søgning i harvests tabellen
        loadseeds.testStep5();
        //6) Find kriterie-resultater på en bestemt url - - søgning i criteria_results tabellen
        loadseeds.testStep6();
        
        loadseeds.computeStats();
       
        
        
        System.out.println("Finished LoadTest at date '" + new Date() + "' using seeds from file '" + seedsfile.getAbsolutePath() + "'");
        System.exit(0);
    }

    private void testStep3() throws Exception {
 	      System.out.println("TEST3: inserting unique url, and update it to status: Status.ANALYSIS_COMPLETED");
 	      String uniqueSeed = "http://www.netarkivet.dk/stresstest/" + System.currentTimeMillis();
 	      Seed s = new Seed(uniqueSeed);
 	      long millisStarted = System.currentTimeMillis();
 	      SeedsDAO dao = daoFactory.getSeedsDAO();
 	      dao.insertSeed(s);
 	      s.setState(Status.ANALYSIS_COMPLETED);
 	      dao.updateState(s);
 	      // TODO should be able to verify that it has changed
 	      long millisEnded = System.currentTimeMillis(); 
 	     System.out.println("Finished Step3 at " + new Date());
 	    System.out.println("Time spent doing Step3 in secs: " + (millisEnded - millisStarted)); 
 	}
    
    //Ingest en million fake kriterie-resultater i criteria_results tabellen
    // Brug criteria_resultsdir som sample-data til at bruge som udgangspunkt 
    private void testStep4() {
        System.out.println("Starting Step4 at " + new Date());
        File[] resultdirs = criteriaResultsdir.listFiles(new FileFilter() {
            
            @Override
            public boolean accept(File pathname) {
                if (pathname.isDirectory()) {
                    return true;
                }
                return false;
            }
        });
        
        System.out.println("Now ingesting records in criteria_results table at " + new Date() + " based on " + resultdirs.length + " harvest-results");
        long millisStarted = System.currentTimeMillis();
        
        
        // Find sample ingest-files in the criteria-resultsdir  
        HarvestDAO hdao = daoFactory.getHarvestDAO();
        //String seedurlTemplate = "http://netarkivet.dk/";
        //String harvestNameTemplate = "harvest-trial-" + System.currentTimeMillis() + "-";
        boolean successful = true;
        List<String> files = new ArrayList<String>();
        String error = "no errors";
        
        JobStatus finalState = JobStatus.DONE;
        int filecount = 0;
        final long TOTAL = criteriaresultmultiples;
        for (File f: resultdirs) {
           filecount++;
             
            File ingest = new File(f, "part-m-00000.gz");
            System.out.println("Ingesting " + criteriaresultmultiples + " times records based on file " + f.getAbsolutePath());
            for (int count=0; count < TOTAL; count++) {
                long harvestedTime = System.currentTimeMillis();
                String harvestName = harvestNameTemplate + filecount + "/" + count;
                String seedurl = seedurlTemplate + filecount + "/" + count;
                HarvestReport hr = new HarvestReport(harvestName, seedurl, successful, files, error, finalState, harvestedTime);
                try {
                    hdao.insertHarvest(hr);
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                try {
                    CriteriaIngest.processFile(ingest, seedurl, harvestName, true, daoFactory);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        long millisEnded = System.currentTimeMillis(); 
        System.out.println("Finished Step4 at " + new Date());
        System.out.println("Time spent doing Step4 in secs: " + (millisEnded - millisStarted)); 
    }
    
    //5) - søgning i harvests tabellen
    // Select alle harvest navne (LIMIT 100K) -  liste A
    // Select alle harvest på en bestemt seed på en bestemt seed (evt. sorteret i tid) 
    // Find en bestemt harvest, ud fra liste A ovenfor
    
      private void testStep5() throws Exception {
          System.out.println("Starting Step5 at " + new Date());
          long millisStarted = System.currentTimeMillis();
          HarvestDAO hdao = daoFactory.getHarvestDAO();
          System.out.println("Found " + hdao.getCount() + " harvests");
          List<String> harvestNames = hdao.getAllNames();
          System.out.println("Retrieved " + hdao.getCount() + " harvestnames (limit of 100K)");
          String seedurlName = seedurlTemplate + "1/0";
          List<HarvestReport> harvestsFound = hdao.getAllWithSeedurl(seedurlName);
          System.out.println("Retrieved " + harvestsFound.size() + " harvests for seed " + seedurlName);
          if (!harvestNames.isEmpty()) {
              String harvestName = harvestNames.get(0);
              HarvestReport hr = hdao.getHarvest(harvestName);
              System.out.println("Retrieved harvest for seed: " + hr.seed);
          }
          
          long millisEnded = System.currentTimeMillis(); 
          System.out.println("Finished Step5 at " + new Date());
        System.out.println("Time spent doing Step5 in secs: " + (millisEnded - millisStarted));
      }

      //6) Find kriterie-resultater på en bestemt url - søgning i criteria_results tabellen:
      // a. Find en harvest i mængden af harvests og hent dens criterie-resultater
      // b. 
        private void testStep6() throws Exception {
          System.out.println("Starting Step6 at " + new Date());
          long millisStarted = System.currentTimeMillis();
          CriteriaResultsDAO dao = daoFactory.getCriteriaResultsDAO();
          HarvestDAO hdao = daoFactory.getHarvestDAO();
          List<String> harvestNames = hdao.getAllNames();
          String harvestName = harvestNames.get(0);
          List<SingleCriteriaResult> list = dao.getResultsByHarvestname(harvestName);
          System.out.println("Retrieved " + list.size() + " criteria.results for harvest '" + harvestName + "'");
          if (list.size() > 0) {
              SingleCriteriaResult scr = list.get(0);
              String seedUrl = scr.seedurl;
              String url = scr.url;
              List<SingleCriteriaResult> seedUrlResults = dao.getResultsBySeedurl(seedUrl);
              System.out.println("Retrieved " + seedUrlResults.size() + " criteria.results for seedurl '" + seedUrl + "'");
              List<SingleCriteriaResult> urlResults = dao.getResultsByUrl(url);
              System.out.println("Retrieved " + urlResults.size() + " criteria.results for seedurl '" + seedUrl + "'");
          }
          long millisEnded = System.currentTimeMillis(); 
          System.out.println("Finished Step6 at " + new Date());
        System.out.println("Time spent doing Step6 in secs: " + (millisEnded - millisStarted));
    }

    private void computeStats() throws Exception {
 	   SeedsDAO dao = daoFactory.getSeedsDAO();
 	   long millisStarted = System.currentTimeMillis();
       long initialSeedsCount = dao.getSeedsCount(null);
       Map<Integer, Long> mapcount = new HashMap<Integer,Long>();
       for (Status s: Status.values()) {
           mapcount.put(s.ordinal(), dao.getSeedsCount(s));
       }
       HarvestDAO hdao = daoFactory.getHarvestDAO();
       long harvestCount = hdao.getCount();
       CriteriaResultsDAO cdao = daoFactory.getCriteriaResultsDAO(); 
       long totalCritResults = cdao.getCountByHarvest(null); // null meaning = get total count
       
       long millisEnded = System.currentTimeMillis();
       System.out.println("Seeds-stats at '" + new Date() + "':");
       System.out.println("=========================================");
       System.out.println("Total-seeds: " + initialSeedsCount);
       for (Status s: Status.values()) {
           System.out.println("#seeds with status '" + s.name() + "': " + mapcount.get(s.ordinal()));
       }
       System.out.println("Total number of entries in harvests table: " + harvestCount);
       System.out.println("Total number of entries in criteria_results table: " + totalCritResults);
       System.out.println("Time spent computing the stats in secs: " + (millisEnded - millisStarted));
}
 	
    private File seedsfile;
    private boolean writeAcceptLog = false;
    private boolean writeRejectLog = false;
	private File rejectLog = null;
	private File acceptLog = null;
	private List<String> acceptedList = new ArrayList<String>();
    private DAOFactory daoFactory;
    private long totalLines;
    private File criteriaResultsdir;
    private final String seedurlTemplate;
    private final String harvestNameTemplate;
    private final long criteriaresultmultiples;
    
	public LoadTest(File seedsfile, File criteriaResultsDir, long criteriaresultmultiples) {
	   this.seedsfile = seedsfile;
	   this.criteriaResultsdir = criteriaResultsDir;
	   this.criteriaresultmultiples = criteriaresultmultiples;
	   this.totalLines = FileUtils.countLines(seedsfile);
	   this.seedurlTemplate = "http://netarkivet.dk/";
	   this.harvestNameTemplate = "harvest-trial-" + System.currentTimeMillis() + "-"; 
	   
	   
	   final String DEFAULT_DATABASE_SYSTEM = "cassandra";
	   String databaseSystem = SettingsUtilities.getStringSetting(WebdanicaSettings.DATABASE_SYSTEM, DEFAULT_DATABASE_SYSTEM);
       if ("cassandra".equalsIgnoreCase(databaseSystem)) {
           daoFactory = new CassandraDAOFactory();
       } else if ("hbase-phoenix".equalsIgnoreCase(databaseSystem)) {
           daoFactory = new HBasePhoenixDAOFactory();
       }
    }
	
	public long getTotalLines() {
	    return this.totalLines;
	}
	
	/**
	 * @return the ingestLog for the file just processed
	 * @throws Exception 
	 */
	public IngestLog insertSeeds() {
		SeedsDAO dao = daoFactory.getSeedsDAO();
		long fivepercent = (totalLines / 100) * 5;
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
	            String errMsg = "";
	            if (rejectreason == URL_REJECT_REASON.NONE) {
	            	Seed singleSeed = new Seed(trimmedLine);
	            	boolean inserted = false;
	            	boolean isError = false;
	            	try {
	            	    inserted = dao.insertSeed(singleSeed);
	            	} catch (Throwable e) {
	            	    rejectreason = URL_REJECT_REASON.BAD_URL;
	            	    errMsg = "Insertion of url failed: " + e.toString();
	            	}
	            	
	            	if (!inserted && !isError) { // assume duplicate url 
	            		rejectreason = URL_REJECT_REASON.DUPLICATE;
	            		duplicatecount++;
	            	} else if(inserted) { 
	            		insertedcount++;
	            		acceptedList.add(trimmedLine);
	            	}
	            }
	            if (rejectreason != URL_REJECT_REASON.NONE) {
	            	logentries.add(rejectreason + ": " + trimmedLine + " " + errMsg);
	            	rejectedcount++;
	            }
	         
	            logIfpercentageProcessed(linecount, totalLines, fivepercent);
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

	private void logIfpercentageProcessed(long linecount, long totalLines2, long fivepercent) {
        if ((linecount % fivepercent) == 0) {
            int percent = (int) (linecount / fivepercent) * 5;
            System.out.println("Processed " + linecount + " lines of " + totalLines2 + " seeds (" + percent + " %)");
        }
        
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
}
