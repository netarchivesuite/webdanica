package dk.kb.webdanica.core.tools;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dk.kb.webdanica.core.WebdanicaSettings;
import dk.kb.webdanica.core.datamodel.IngestLog;
import dk.kb.webdanica.core.datamodel.Seed;
import dk.kb.webdanica.core.datamodel.Status;
import dk.kb.webdanica.core.datamodel.URL_REJECT_REASON;
import dk.kb.webdanica.core.datamodel.criteria.CriteriaIngest;
import dk.kb.webdanica.core.datamodel.criteria.ProcessResult;
import dk.kb.webdanica.core.datamodel.criteria.SingleCriteriaResult;
import dk.kb.webdanica.core.datamodel.dao.CassandraDAOFactory;
import dk.kb.webdanica.core.datamodel.dao.CriteriaResultsDAO;
import dk.kb.webdanica.core.datamodel.dao.DAOFactory;
import dk.kb.webdanica.core.datamodel.dao.HBasePhoenixDAOFactory;
import dk.kb.webdanica.core.datamodel.dao.HarvestDAO;
import dk.kb.webdanica.core.datamodel.dao.IngestLogDAO;
import dk.kb.webdanica.core.datamodel.dao.SeedsDAO;
import dk.kb.webdanica.core.interfaces.harvesting.SingleSeedHarvest;
import dk.kb.webdanica.core.utils.DatabaseUtils;
import dk.kb.webdanica.core.utils.SettingsUtilities;
import dk.kb.webdanica.core.utils.UrlUtils;
import dk.netarkivet.harvester.datamodel.JobStatus;

/**
 * Program to load test the webdanica database.
 *  
 *  Remember to call program with -Dwebdanica.settings.file=$LOADSEEDS_HOME/webdanica_settings_file
 *  and -Ddk.netarkivet.settings.file=$LOADSEEDS_HOME/settings_NAS_Webdanica.xml
 *  and -Dlogback.configurationFile=$LOADSEEDS_HOME/silent_logback.xml 
 *  
 */  
public class LoadTest {

public static void main(String[] args) throws Exception {
        System.out.println("Starting LoadTest version 0.3 ...");
        if (args.length != 3) {
            System.err.println("Need arguments <numberofseedstoingest> <criteriaresultsdir> criteriaresultmultiples");
            System.exit(1);
        }
        long numberofseedstoingest = 0;
        try {
            numberofseedstoingest = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            System.err.println("The given numberofseedstoingest argument '" + args[0]+ "' cannot be parsed as number ");
            System.exit(1);
        }
          
        File criteriaResultsDir = new File(args[1]);
        if (!criteriaResultsDir.isDirectory()){
            System.err.println("The criteriaResultsDir located '" + criteriaResultsDir.getAbsolutePath() + "' does not exist or is not a proper directory");
            System.exit(1);
        }
        long criteriaresultmultiples = 0;
        try {
            criteriaresultmultiples = Long.parseLong(args[2]);
        } catch (NumberFormatException e) {
            System.err.println("The given criteriaresultmultiples argument '" + args[0]+ "' cannot be parsed as number ");
            System.exit(1);
        }
        long starttime = System.currentTimeMillis();
        
        System.out.println("Starting LoadTest at date '" + new Date() + "' with numberofseedstoingest=" + numberofseedstoingest + ", sample criteriaresultsdir = '" 
                +  criteriaResultsDir.getAbsolutePath() + "' and criteriaresultmultiples (used in step4) = " + criteriaresultmultiples);  
        
        LoadTest loadseeds = new LoadTest(numberofseedstoingest, criteriaResultsDir, criteriaresultmultiples);
        System.out.println();
        System.out.println("database-stats before test starts:");
        System.out.println();
        loadseeds.computeStats();    
        System.out.println();
        System.out.println("Starting inserting " +  loadseeds.getTotalLines() + " seeds");
        long millisStarted = System.currentTimeMillis();
        IngestLog res = loadseeds.insertSeeds();
        long millisEnded = System.currentTimeMillis();
        System.out.println("Time spent computing the seedsInsertion in secs: " + (millisEnded - millisStarted)/1000);
        
        System.out.println(res.getStatistics());
        System.out.println();
        /*
        File acceptLog = loadseeds.getAcceptLog();
        File rejectLog = loadseeds.getRejectLog();
        System.out.println("Acceptlog in file: " + (acceptLog==null?"No log written due to error": acceptLog.getAbsolutePath()));
        System.out.println("Rejectlog in file: " + (rejectLog==null?"No log written due to error": rejectLog.getAbsolutePath()));
        */
  
        loadseeds.testStep3();
        
        //Ingest en million fake kriterie-resultater i criteria_results tabellen
        // Brug criteria_resultsdir som sample-data til at bruge som udgangspunkt 
        loadseeds.testStep4();
        System.out.println();
              
        
        // 5) Select alle harvests på en bestemt seed (evt. sorteret i tid) - søgning i harvests tabellen
        loadseeds.testStep5();
        System.out.println();
        
        //6) Find kriterie-resultater på en bestemt url - - søgning i criteria_results tabellen
        loadseeds.testStep6();
        System.out.println();
        System.out.println("database-stats after test is finished:");
        System.out.println();
        
        loadseeds.computeStats();      
        long endtime = System.currentTimeMillis();
        System.out.println("Finished LoadTest at date '" + new Date() + "' with numberofseedstoingest=" + numberofseedstoingest + ", sample criteriaresultsdir = '" 
                +  criteriaResultsDir.getAbsolutePath() + "' and criteriaresultmultiples (used in step4) = " + criteriaresultmultiples);
        long totaltimespent = endtime - starttime;
        long totaltimetspentMinutes = totaltimespent / 1000 / 60; 
        System.out.println("=========== Total time spent (minutes):  " + totaltimetspentMinutes); 
        
        System.exit(0);
    }

    private void testStep3() throws Exception {
 	      System.out.println("TEST3: inserting unique url, and update it to status: Status.ANALYSIS_COMPLETED");
 	      String uniqueSeed = "http://www.netarkivet.dk/stresstest/" + System.currentTimeMillis();
 	      Seed s = new Seed(uniqueSeed);
 	      long millisStarted = System.currentTimeMillis();
 	      SeedsDAO dao = daoFactory.getSeedsDAO();
 	      dao.insertSeed(s);
 	      s.setStatus(Status.ANALYSIS_COMPLETED);
 	      dao.updateSeed(s);
 	      // TODO should be able to verify that it has changed
 	      long millisEnded = System.currentTimeMillis(); 
 	     System.out.println("Finished Step3 at " + new Date());
 	     long timeusedInMillis = (millisEnded - millisStarted);
 	    System.out.println("Time spent doing Step3 in millisecs: " + timeusedInMillis + "(secs: " +  timeusedInMillis/1000 + ")");
 	    System.out.println();
 	    
 	}
    
    //Ingest en million fake kriterie-resultater i criteria_results tabellen
    // Brug criteria_resultsdir som sample-data til at bruge som udgangspunkt 
    private void testStep4() throws Exception {
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
        CriteriaResultsDAO cdao = daoFactory.getCriteriaResultsDAO();
        //String seedurlTemplate = "http://netarkivet.dk/";
        //String harvestNameTemplate = "harvest-trial-" + System.currentTimeMillis() + "-";
        boolean successful = true;
        List<String> files = new ArrayList<String>();
        String error = "no errors";
        long harvestsInserted = 0;
        long critResultsInserted = 0;
        JobStatus finalState = JobStatus.DONE;
        int filecount = 0;
        final long TOTAL = criteriaresultmultiples;
        for (File f: resultdirs) {
           filecount++;
             
            File ingest = new File(f, "part-m-00000.gz");
            System.out.println("Part " + filecount + "/" + resultdirs.length + ": Starting ingest of " + criteriaresultmultiples + " times records based on file '" + f.getAbsolutePath() + "' at " + new Date());
            ProcessResult pr = CriteriaIngest.processFile(ingest, seedurlTemplate, harvestNameTemplate, false, daoFactory);
            List<SingleCriteriaResult> critResults = pr.results;
            System.out.println(".. using the " + critResults.size() + " record(s) in " + ingest.getAbsolutePath() + " as template");
            for (int count=0; count < TOTAL; count++) {
                long harvestedTime = System.currentTimeMillis();
                String harvestName = harvestNameTemplate + filecount + "/" + count;
                String seedurl = seedurlTemplate + filecount + "/" + count;
                SingleSeedHarvest hr = new SingleSeedHarvest(harvestName, seedurl, successful, files, error, finalState, harvestedTime, null); // FIXME
                try {
                    hdao.insertHarvest(hr);
                    harvestsInserted++;
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                for (SingleCriteriaResult cresult: critResults) {
                    cresult.harvestName = harvestName;
                    cresult.seedurl = seedurl;
                    cdao.insertRecord(cresult);
                    critResultsInserted++;
                }
            }
        }
        long millisEnded = System.currentTimeMillis(); 
        System.out.println("Finished Step4 at " + new Date());
        System.out.println("Inserted " + harvestsInserted + " harvests in database");
        System.out.println("Inserted " + critResultsInserted + " criteriaresults in database");
        System.out.println("Total time spent doing Step4 in minutes: " + ((millisEnded - millisStarted)/60000)); 
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
          List<SingleSeedHarvest> harvestsFound = hdao.getAllWithSeedurl(seedurlName);
          System.out.println("Retrieved " + harvestsFound.size() + " harvests for seed " + seedurlName);
          if (!harvestNames.isEmpty()) {
              String harvestName = harvestNames.get(0);
              SingleSeedHarvest hr = hdao.getHarvest(harvestName);
              System.out.println("Retrieved harvest for seed: " + hr.getSeed());
          }
          
          long millisEnded = System.currentTimeMillis(); 
          System.out.println("Finished Step5 at " + new Date());
        System.out.println("Time spent doing Step5 in secs: " + (millisEnded - millisStarted)/1000);
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
              System.out.println("Retrieved " + seedUrlResults.size() + " criteria.results for seedurl '" + seedUrl + "' and harvestname '" +  scr.harvestName + "'");
              List<SingleCriteriaResult> urlResults = dao.getResultsByUrl(url);
              System.out.println("Retrieved " + urlResults.size() + " criteria.results for seedurl '" + seedUrl + "'");
          }
          long millisEnded = System.currentTimeMillis(); 
          System.out.println("Finished Step6 at " + new Date());
        System.out.println("Time spent doing Step6 in secs: " + (millisEnded - millisStarted)/1000);
    }

    private void computeStats() throws Exception {
       DatabaseUtils.printDatabaseStats(daoFactory); 
    }   
 	

    private DAOFactory daoFactory;
    private File criteriaResultsdir;
    private final String seedurlTemplate;
    private final String harvestNameTemplate;
    private final long criteriaresultmultiples;
    private final long numberofseedstoingest;
    
	public LoadTest(long numberofseedstoingest, File criteriaResultsDir, long criteriaresultmultiples) {
	   this.numberofseedstoingest = numberofseedstoingest;
	   this.criteriaResultsdir = criteriaResultsDir;
	   this.criteriaresultmultiples = criteriaresultmultiples;
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
	    return this.numberofseedstoingest;
	}
	
	/**
	 * @return the ingestLog for the file just processed
	 * @throws Exception 
	 */
	public IngestLog insertSeeds() {
		SeedsDAO dao = daoFactory.getSeedsDAO();
		long fivepercent = (numberofseedstoingest / 100) * 5; 
        long linecount=0L;
        long insertedcount=0L;
        long rejectedcount=0L;
        long duplicatecount=0L;
        String trimmedLine = null;
        List<String> logentries = new ArrayList<String>();
        String seedTemplate = "http://www.netarkivet.dk/" + System.currentTimeMillis() + "/";
        try {
        	for (long count=0; count<numberofseedstoingest; count++) {
        	    linecount++;
        	    trimmedLine = seedTemplate + count;
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
                        isError = true;
                        errMsg = "Insertion of url failed: " + e.toString();
                    }
                    if (!inserted && !isError) { // assume duplicate url 
                        rejectreason = URL_REJECT_REASON.DUPLICATE;
                        duplicatecount++;
                    } else if(inserted) { 
                        insertedcount++;
                        // FIXME Removed because deemed unnecessary for LoadTest 
                        //acceptedList.add(trimmedLine);
                    }        	    
                }
 	            if (rejectreason != URL_REJECT_REASON.NONE) {
 	                // FIXME Removed because deemed unnecessary for LoadTest
	            	//logentries.add(rejectreason + ": " + trimmedLine + " " + errMsg);
	            	rejectedcount++; // Also includes errors
	            }
	         
	            logIfpercentageProcessed(linecount, numberofseedstoingest, fivepercent);
	        }
	        
        } catch (Throwable e) {
	        e.printStackTrace();
        } finally {
        	dao.close();
        }
        
	    IngestLog logresult = null;
        try {
            // Dette her skalerer måske ikke, hvis loggen er meget stor. Derfor lægges ingenting i logentries FIXME
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
            System.out.println("Processed " + linecount + " of " + totalLines2 + " seeds (" + percent + " %) at " + new Date());
        }
    }

	private IngestLog logIngestStats(List<String> logentries, long linecount, long insertedcount, long rejectedcount, long duplicatecount) throws Exception {
		IngestLogDAO dao = null;
		try {
			dao = daoFactory.getIngestLogDAO();
			IngestLog log = new IngestLog(logentries, "test-trial-" + System.currentTimeMillis(), linecount, insertedcount, rejectedcount, duplicatecount);
			dao.insertLog(log);
			return log;
		} finally {
			dao.close();
		}
	}
}
