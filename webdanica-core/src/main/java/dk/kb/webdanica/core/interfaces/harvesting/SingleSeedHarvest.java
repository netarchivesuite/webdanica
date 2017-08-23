package dk.kb.webdanica.core.interfaces.harvesting;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import dk.kb.webdanica.core.batch.WARCExtractUrlsJob;
import dk.kb.webdanica.core.datamodel.AnalysisStatus;
import dk.kb.webdanica.core.datamodel.criteria.SingleCriteriaResult;
import dk.kb.webdanica.core.exceptions.WebdanicaException;
import dk.kb.webdanica.core.interfaces.harvesting.SeedReport.SeedReportEntry;
import dk.netarkivet.common.CommonSettings;
import dk.netarkivet.common.distribute.arcrepository.ArcRepositoryClientFactory;
import dk.netarkivet.common.distribute.arcrepository.BitarchiveRecord;
import dk.netarkivet.common.distribute.arcrepository.Replica;
import dk.netarkivet.common.exceptions.ArgumentNotValid;
import dk.netarkivet.common.utils.ApplicationUtils;
import dk.netarkivet.common.utils.FileUtils;
import dk.netarkivet.common.utils.Settings;
import dk.kb.webdanica.core.utils.StreamUtils;
import dk.kb.webdanica.core.utils.SystemUtils;
import dk.netarkivet.common.utils.TimeUtils;
import dk.netarkivet.common.utils.batch.BatchLocalFiles;
import dk.netarkivet.common.utils.cdx.CDXRecord;
import dk.netarkivet.harvester.datamodel.DomainConfiguration;
import dk.netarkivet.harvester.datamodel.HarvestDefinition;
import dk.netarkivet.harvester.datamodel.HarvestDefinitionDAO;
import dk.netarkivet.harvester.datamodel.Job;
import dk.netarkivet.harvester.datamodel.JobDAO;
import dk.netarkivet.harvester.datamodel.JobStatus;
import dk.netarkivet.harvester.datamodel.JobStatusInfo;
import dk.netarkivet.harvester.datamodel.PartialHarvest;
import dk.netarkivet.harvester.datamodel.Schedule;
import dk.netarkivet.harvester.datamodel.ScheduleDAO;
import dk.netarkivet.harvester.datamodel.TemplateDAO;
import dk.netarkivet.harvester.webinterface.HarvestStatus;
import dk.netarkivet.harvester.webinterface.HarvestStatusQuery;
import dk.netarkivet.viewerproxy.webinterface.Reporting;

/**
 *  -- Create event-harvest only containing this seed 
 * -- using only_once schedule
 * -- using special template disabling non-text harvesting, and disabling javascript (webdanica_order
 *
 * Requirement: JBDC access to netarchivesuite harvestdatabase 
 * Database url, and user/password information is taken from the 
 * a NetarchiveSuite settingsfile.
 * The java 'dk.netarkivet.settings.file' property needs to defined 
 * using -Ddk.netarkivet.settings.file=/full/path/to/netarchivesuite_settingsfile.xml
 */
public class SingleSeedHarvest {
	
	private static final Logger logger = Logger.getLogger(SingleSeedHarvest.class.getName());
	
	String seed; // The single seed being harvested;
	String harvestName; // The name of the eventharvest
	JobStatus finishedState;
	List<String> files;
	private JobStatusInfo statusInfo;
	private NasReports reports;
	StringBuilder errMsg;
	boolean successful;
	private Throwable exception;
	private long maxBytes;
	private int maxObjects;
	long harvestedTime;
	private Long hid;
	private List<SingleCriteriaResult> critResults;
	private List<String> fetchedUrls;
	private AnalysisStatus analysisState;
	private String analysisStateReason;
	private boolean constructionOK=true; 
	
	/**
	 * @param seed
	 * @param eventHarvestName
	 * @param scheduleName
	 * @param templateName
	 * @param maxBytes
	 * @param maxObjects
	 */
	public SingleSeedHarvest(String seed, String eventHarvestName, String scheduleName, String templateName, long maxBytes, int maxObjects) {
		this.seed = seed;
		this.harvestName = eventHarvestName;
		List<DomainConfiguration> noDcs = new ArrayList<DomainConfiguration>();
		this.maxBytes = maxBytes;
		this.maxObjects = maxObjects;
		this.errMsg = new StringBuilder();
		
		if (!ScheduleDAO.getInstance().exists(scheduleName)) {
			throw new WebdanicaException("The chosen schedule with name '" + scheduleName + "' does not exist");
		}
		if (!TemplateDAO.getInstance().exists(templateName)) {
			throw new WebdanicaException("The chosen heritrix template with name '" + templateName + "' does not exist");
		}
		
		Schedule s = ScheduleDAO.getInstance().read(scheduleName);
		HarvestDefinition hd = new PartialHarvest(noDcs, s, eventHarvestName, "Event harvest created by webdanica system at " + new Date() + ". seed= " + seed, "Webdanica Curators");
		HarvestDefinitionDAO dao = HarvestDefinitionDAO.getInstance();
		hid = dao.create(hd);
		
		Map<String,String> attributeValues = new HashMap<String,String>(); // Empty attributeset
		
		PartialHarvest eventHarvest = (PartialHarvest) HarvestDefinitionDAO.getInstance().getHarvestDefinition(eventHarvestName);
		Set<String> seedSet = new HashSet<String>();
		seedSet.add(seed);
		try {
		    eventHarvest.addSeeds(seedSet, templateName, maxBytes, maxObjects, attributeValues);
		} catch (ArgumentNotValid e) {
		    constructionOK=false;
		    String error = "Unable to add seed '" + seed + "' to eventharvest: " + ExceptionUtils.getFullStackTrace(e);
		    logger.warning("Failed to construct harvest for seed '" + seed + "': " +  error);
		    errMsg.append(error);
		    
		}
		if (constructionOK) {
		    eventHarvest.setActive(true);
		} else {
		    eventHarvest.setActive(false);
		}
		HarvestDefinitionDAO.getInstance().update(eventHarvest);		 
	}
	
	public static SingleSeedHarvest getErrorObject(String seed, String harvestName, String error, Throwable e) {
		SingleSeedHarvest s = new SingleSeedHarvest(seed, harvestName, error, e);
		return s;
	}
	
	private SingleSeedHarvest(String seed, String harvestName, String error, Throwable e) {
	    this.seed = seed;
	    this.errMsg = new StringBuilder(error);
	    this.exception = e;
	    this.harvestName = harvestName;
	    this.analysisState = AnalysisStatus.NO_ANALYSIS;
	    this.analysisStateReason = "No analysis done, as the harvest has failed";
    }
	
	/**
	 * Used to construct a SingleSeedHarvest object with database information.
	 * @param harvestname
	 * @param seedurl
	 * @param successful
	 * @param files
	 * @param error
	 * @param finalState
	 * @param harvestedTime
	 * @param reports
	 * @param fetchedUrls
	 * @param analysisStatus
	 * @param analysisReason
	 */
	public SingleSeedHarvest(String harvestname, String seedurl, boolean successful, List<String> files, String error, 
			JobStatus finalState, long harvestedTime, NasReports reports, List<String> fetchedUrls, AnalysisStatus analysisStatus, String analysisReason) {
		this.harvestName = harvestname;
		this.seed = seedurl;
		this.successful = successful;
		if (files != null) {
		    this.files = files;
		} else {
		    this.files = new ArrayList<String>();
		}
		if (error == null) {
			this.errMsg = new StringBuilder();
		} else {
			this.errMsg = new StringBuilder(error);
		}
		this.finishedState = finalState;
		this.harvestedTime = harvestedTime;
		this.reports = reports;
		this.fetchedUrls = fetchedUrls;
		this.analysisState = analysisStatus;
		this.analysisStateReason = analysisReason;
    }

	public SingleSeedHarvest() {
	   
    }

	/**
	 * 
	 * @return JobStatus of the job in progress (expects only one job to be created)
	 */
	private JobStatusInfo getHarvestStatus() {
		HarvestStatusQuery hsq = new HarvestStatusQuery(hid, 0); 
		HarvestStatus hs = JobDAO.getInstance().getStatusInfo(hsq);
		List<JobStatusInfo> jobs = hs.getJobStatusInfo();
		if (jobs.size() == 0) { // No jobs yet created (What can go wrong here??)
			return null;
		} else if (jobs.size() == 1) {
			return jobs.get(0);
		} else {
			throw new WebdanicaException("Should be either 0 or 1 jobs generated, but there are  " + jobs.size() + " jobs for harvestId " + hid + " and harvestRun 0");   
		}
	} 
	
	public String getSeed() {
		return this.seed;
	}
	
	public String getHarvestName() {
		return this.harvestName;
	}
	
	public JobStatus getFinalState() {
	    return this.finishedState;
    }
	
	public JobStatusInfo getJobStatusInfo() {
		return this.statusInfo;
	}

	public NasReports getReports() {
	    return reports;
    }
	
	public AnalysisStatus getAnalysisState() {
		return this.analysisState;
	}
	
	public String getAnalysisStateReason() {
		return this.analysisStateReason;
	}
	
	
	/**
	 * Wait until harvest is finished or failed.
	 * @return true, if successful otherwise false;
	 * @throws Exception 
	 */
	public boolean finishHarvest(boolean writeToSystemOut) throws Exception {
		Set<JobStatus> finishedStates = new HashSet<JobStatus>();
		finishedStates.add(JobStatus.DONE);
		finishedStates.add(JobStatus.FAILED); 

		while (getHarvestStatus() == null){
			if (writeToSystemOut) 
				System.out.println("Waiting for job for eventharvest '" + harvestName + "' to be scheduled .."); 
			try {
				Thread.sleep(5000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		// Harvest is now in progress ph.getHarvestStatus() != null
		JobStatusInfo jsi = getHarvestStatus();
		Long jobId = jsi.getJobID();
		JobStatus status = jsi.getStatus();
		if (writeToSystemOut) {
			System.out.println("State of Job " + jobId + ": " + status);
		}
		long starttime = System.currentTimeMillis();
		while (!finishedStates.contains(status)) {
			if (writeToSystemOut) 
				System.out.println("Waiting for job '" + jobId + "' to finish. Current state is " + status);
			try {
				Thread.sleep(30000L); // 30 secs sleep
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			jsi = getHarvestStatus();
			status = jsi.getStatus(); // Refresh status
		}
		long endtime = System.currentTimeMillis();
		long usedtimeSecs = (endtime-starttime)/1000;
		if (writeToSystemOut) System.out.println("After " + TimeUtils.readableTimeInterval(usedtimeSecs*1000L) +" the job " + jobId + " now has finished state " + status );
		this.finishedState = status;
		this.statusInfo = jsi;
		Job theJob = JobDAO.getInstance().read(jobId);
		// jobEndState should be equal to this.finishedState
		JobStatus jobEndState = theJob.getStatus();
		if (!jobEndState.equals(finishedState)) {
			System.err.println("JobEndState (" + jobEndState + ") is not equal to finishedstate (" +  finishedState + ")!");
		}
		
		String harvestPrefix = theJob.getHarvestFilenamePrefix();
		ApplicationUtils.dirMustExist(FileUtils.getTempDir()); // Inserted to ensure that the getTempDir() exists.
		if (writeToSystemOut)System.out.println("Retrieving the list of files belonging to the job: ");
		List<String> lines = Reporting.getFilesForJob(jobId.intValue(), harvestPrefix);
		boolean filesHarvested = false;
		if (lines != null && !lines.isEmpty()){
			String logMsg = "The following files were harvested: " + StringUtils.join(lines, ",");
			filesHarvested = true;
			log(logMsg, Level.INFO, writeToSystemOut, null);
			
		} else {
			String logMsg = "No files was harvested. ";
			log(logMsg, Level.WARNING, writeToSystemOut, null);
		}
		this.files = lines;
		
		// attempt to set harvestdefinition represented by this.hid to inactive
		try {
		    HarvestDefinitionDAO hdao = HarvestDefinitionDAO.getInstance();
		        if (hdao.exists(this.hid)) {
		            HarvestDefinition hd = hdao.read(this.hid);
		            hd.setActive(false);
		            hdao.update(hd);
		        } else {  
		            String logMsg = "Unable to disable harvestdefiniton with id=" + this.hid + ". Netarchivesuite does not recognize harvestdefinition with this ID";
		            log(logMsg, Level.WARNING, writeToSystemOut, null);
		        }
		} catch (Throwable e) {
		    String logMsg = "Unable to disable harvestdefiniton with id=" + this.hid;
		    log(logMsg, Level.WARNING, writeToSystemOut, e);
		}
		
		//get the reports associated with the harvest as well, extracted from the metadatawarc.file.
		this.reports = null;
		try {
			this.reports = getReports(theJob.getJobID(), writeToSystemOut);
		} catch (Throwable e) {
		    String error = "Unable to retrieve the reports for job '" + theJob.getJobID() + "': " + e;
		    this.errMsg.append(error);
		    log(error, Level.WARNING, writeToSystemOut, e);
		}
		if (this.reports != null) {
			String logMsg = "Retrieved '" + this.reports.getReports().keySet().size() + "' reports for job '" + jobId + "'";
			log(logMsg, Level.INFO, writeToSystemOut, null);
		} 
		
		// get the urls harvested by the job
		this.fetchedUrls = null;
		try {
			this.fetchedUrls = getHarvestedUrls(getHeritrixWarcs(), writeToSystemOut);
		} catch (Throwable e) {
		    String error = "Unable to retrieve the fetchedUrls for job '" + theJob.getJobID() + "'";
		    this.errMsg.append(error);
		    log(error, Level.WARNING, writeToSystemOut, e);
		}
		this.successful = status.equals(JobStatus.DONE);
		String failureReason = "";
		if (reports != null && reports.getSeedReport() != null) {
			// Look at the seedreport
			SeedReport sr = reports.getSeedReport();
			SeedReportEntry entry = sr.getEntry(this.seed);
			if (entry == null && !this.seed.endsWith("/")) { // try adding '/' to the end
				entry = sr.getEntry(this.seed + "/");
			}
			if (entry == null) {
				failureReason = " The seed was not found in the seedreport. Only exist entries for: " + StringUtils.join(sr.getSeeds(), ",");
				this.successful = false;
			} else if (!entry.isCrawled()) {
				failureReason = " According to the seedreport, the seed was not crawled. Code = '" +  entry.getCode() + "', Status='" +  entry.getStatus() + "'";
				this.successful = false;
			}	
		}
		
		// Add failureReason to errMsg, if it contains information
		if (!failureReason.isEmpty()) {
			errMsg.append(failureReason);
		}

		// examine the harvested-Heritrix-files, and see if they are warc.gz
		if (!filesHarvested || getHeritrixWarcs().size()==0 ) {
			this.successful = false;
			failureReason += "No heritrix warc files generated by the harvest";
		} else {
			for (String hwarc: getHeritrixWarcs()) {
				if (!SystemUtils.isGzippedWarcfile(hwarc)) {
					this.successful = false;
					failureReason += "The warc-file '" + hwarc + "' is not gzipped. Make sure that your NetarchiveSuite is configured to produce gzipped warc-files";
				}
			}
		}
		
		this.harvestedTime = theJob.getActualStop().getTime();
		if (this.successful) {
			this.analysisState = AnalysisStatus.AWAITING_ANALYSIS;
			this.analysisStateReason = "Harvest was successful, so analysis can be done.";
		} else {
			this.analysisState = AnalysisStatus.NO_ANALYSIS;
			this.analysisStateReason = "Harvest was not successful, so analysis can't be done. " + failureReason;
		}
		return this.successful;
    }
	/**
	 * Convenience method to easily log to stdout/stderr or to a logfile 
	 * @param logMsg
	 * @param loglevel
	 * @param writeToSystemOut
	 * @param exception
	 */
	private static void log(String logMsg, Level loglevel, boolean writeToSystemOut, Throwable exception) {
	    String stacktrace = "";
	    if (writeToSystemOut) {
	        if (exception != null) {
	            stacktrace = ExceptionUtils.getFullStackTrace(exception);
	        }
	        if (loglevel == Level.SEVERE || loglevel == Level.WARNING) {
	            System.err.println(logMsg + stacktrace);
	        } else {
	            System.out.println(logMsg + stacktrace);
	        }
        } else {
            if (exception != null) {
                logger.log(loglevel, logMsg, exception);
            } else {
                logger.log(loglevel, logMsg);
            }
        }
    }

    public static NasReports getReports(Long jobID, boolean writeToSystemOut) {
		Map<String, String> reportMap = new HashMap<String,String>();
		List<CDXRecord> records = Reporting.getMetadataCDXRecordsForJob(jobID);
	    for (CDXRecord record : records) {
	    	String key = record.getURL();
	    	try {
	    		BitarchiveRecord baRecord = ArcRepositoryClientFactory.getViewerInstance().get(record.getArcfile(), 
	    				record.getOffset());
	    		String data = StreamUtils.getInputStreamAsString(baRecord.getData());
	    		reportMap.put(key, data);
	    	} catch (Throwable e) {
	    		String logMsg = "When trying to get all reports for job '" +  jobID + "' we failed to extract the report '" + key + "': " + e;
	    		log(logMsg, Level.WARNING, writeToSystemOut, e);
	    	}
	    }
	    return new NasReports(reportMap);
    }
	
	public static List<String> getHarvestedUrls(List<String> warcfiles, boolean writeToSystemOut) throws Exception {
		List<String> urls = new ArrayList<String>();
		if (warcfiles.isEmpty()) {
			String logMsg = "No heritrixWarcs seems to have been harvested. Something must have gone wrong. Returning an empty list";
			log(logMsg, Level.WARNING, writeToSystemOut, null);
		} else {
			String logMsg = "Fetching the harvested from urls from the " + warcfiles.size() + " HeritrixWarcs harvested: " + StringUtils.join(warcfiles, ",");
			log(logMsg, Level.INFO, writeToSystemOut, null);
		}
		for (String warcfilename: warcfiles){
			urls.addAll(getUrlsFromFile(warcfilename));
		}
		String logMsg = "Retrieve " + urls.size() + " urls";
		log(logMsg, Level.INFO, writeToSystemOut, null);
		return urls;
	}
	
	
	static Set<String> getUrlsFromFile(String warcfilename) throws Exception{
		File toFile = new File(FileUtils.getTempDir(), warcfilename);
		Set<String> urls = new TreeSet<String>();
		toFile.createNewFile();
		if (!toFile.isFile()) {
			throw new IOException("Unable not create temporary file '" + toFile.getAbsolutePath() + "'");
		}
		String replicaId = Settings.get(CommonSettings.USE_REPLICA_ID);
		ArcRepositoryClientFactory.getViewerInstance().getFile(warcfilename,Replica.getReplicaFromId(replicaId), toFile);
		
        ByteArrayOutputStream cdxBaos = new ByteArrayOutputStream();
        BatchLocalFiles batchRunner = new BatchLocalFiles(new File[] {toFile});
        batchRunner.run(new WARCExtractUrlsJob(), cdxBaos);
        for (String cdxLine : cdxBaos.toString().split("\n")) {
            urls.add(cdxLine);
        }
        return urls;
    }

	public List<String> getFiles() {
		return this.files;
	}
	
	public boolean isSuccessful() {
		return this.successful;
	}
	
	public Throwable getException() {
		return this.exception;
	}
	
	public int getMaxObjects() {
		return this.maxObjects;
	}
	
	public long getMaxBytes() {
		return this.maxBytes;
	}
	
	public String getErrMsg() {
		return this.errMsg.toString();
	}

	public long getHarvestedTime() {
	    return this.harvestedTime;
    }
	
	
	public static SingleSeedHarvest doSingleHarvest(String seed, String eventHarvestName, String scheduleName,
            String templateName, long maxBytes, int maxObjects, boolean writeToStdout) throws Exception {
	    SingleSeedHarvest ssh = new SingleSeedHarvest(seed, eventHarvestName, scheduleName, templateName, maxBytes, maxObjects);
	    boolean success = ssh.finishHarvest(writeToStdout);
	    if (writeToStdout)System.out.println("Harvest of seed '" + seed + "': " + (success? "succeeded":"failed"));
	    return ssh;
	    
    }

	public static List<SingleSeedHarvest> doSeriesOfharvests(File argumentAsFile,
            String scheduleName, String templateName, String harvestPrefix, long harvestMaxBytes, int harvestMaxObjects, boolean writeToStdout) {
		
		BufferedReader fr = null;
		List<SingleSeedHarvest> results = new ArrayList<SingleSeedHarvest>();
        try {
	        fr = new BufferedReader(new FileReader(argumentAsFile));
        } catch (FileNotFoundException e) {
        	e.printStackTrace();
        	// Should not happen: already tested
        }        

		//read file 
		Set<String> seeds = new HashSet<String>();
		try {
			String line = "";
			while ((line = fr.readLine()) != null) {
	        	String seed = line.trim();
	        	if (!seed.isEmpty()) {
	        		seeds.add(seed);
	        	}
			}
		} catch (IOException e) {
			throw new WebdanicaException("Exception during the reading of the file '" 
					+ argumentAsFile.getAbsolutePath() + "'", e);
		} finally {
        	IOUtils.closeQuietly(fr);
        }
		
		//harvest each seed in the file
		for (String seed: seeds) {
			String eventHarvestName = harvestPrefix + SingleSeedHarvest.getTimestamp();
			try {
				SingleSeedHarvest ssh = doSingleHarvest(seed, eventHarvestName, scheduleName, templateName, harvestMaxBytes, harvestMaxObjects, writeToStdout);
				results.add(ssh);
			} catch (Throwable e) {	        
				SingleSeedHarvest s = SingleSeedHarvest.getErrorObject(seed, eventHarvestName, "Harvest Failed", e);
				results.add(s);
			}
		}
		return results;
	}

	public static int writeHarvestLog(File harvestLog, String harvestLogHeader, boolean includeOnlySuccessFulHarvests, List<SingleSeedHarvest> results, boolean writeToStdout) throws Exception {
		// Initialize harvestLogWriter
    	PrintWriter harvestLogWriter = new PrintWriter(new BufferedWriter(new FileWriter(harvestLog)));
    	int harvestsWritten = 0;
    	harvestLogWriter.println(harvestLogHeader);
    	harvestLogWriter.println(StringUtils.repeat("#", 80));
    	for (SingleSeedHarvest s: results) {
    		if (!s.successful) {
    			if (includeOnlySuccessFulHarvests) {
    				String logMsg = "Skipping failed harvest '" + s.harvestName + "' of seed '" + s.seed + "'";
    				log(logMsg, Level.WARNING, writeToStdout, null);
    				continue;
    			} else {
    				String logMsg = "Including failed harvest '" + s.harvestName + "' of seed '" + s.seed + "' in harvestlog";
    				log(logMsg, Level.WARNING, writeToStdout, null);
    			}
    		}
    		harvestLogWriter.println(HarvestLog.seedPattern + s.getSeed());
    		harvestLogWriter.println(HarvestLog.harvestnamePattern + s.getHarvestName());
    		harvestLogWriter.println(HarvestLog.successfulPattern + s.isSuccessful());
    		harvestLogWriter.println(HarvestLog.endstatePattern + s.getFinalState());
    		harvestLogWriter.println(HarvestLog.harvestedTimePattern + s.getHarvestedTime());
    		harvestLogWriter.println(HarvestLog.filesPattern + StringUtils.join(s.getFiles(), ","));
    		String errString = (s.getErrMsg() != null?s.getErrMsg():"");
    		String excpString = (s.getException() != null)? "" + s.getException():""; 
    		harvestLogWriter.println(HarvestLog.errorPattern + errString + " " + excpString);
    		harvestLogWriter.println(StringUtils.repeat("#", 80));
    		harvestsWritten++;
    	}  	
    	harvestLogWriter.close();
	    return harvestsWritten;
    }

	public void setCriteriaResults(List<SingleCriteriaResult> results) {
	    this.critResults = results;
    }

	public List<SingleCriteriaResult> getCritresults() {
	    return this.critResults;
    }

	public boolean hasErrors() {
		if (this.errMsg.toString().isEmpty()) {
			return false;
		} else {
			return true;
		}
    }

	public List<String> getHeritrixWarcs() {
		List<String> allFiles = new ArrayList<String>(); 
		for (String f: this.files) {
			if (!f.contains("metadata")) {
				allFiles.add(f);
			}
		}
		return allFiles;
	}
	
	public Set<String> getMetadataWarcs() {
		Set<String> allFiles = new HashSet<String>(); 
		for (String f: this.files) {
			if (f.contains("metadata")) {
				allFiles.add(f);
			}
		}
		return allFiles;
	}

	public void setErrMsg(String error) {
	    this.errMsg = new StringBuilder(error);
	    
    }

	public List<String> getFetchedUrls() {
	    return this.fetchedUrls;
    }

	public boolean getConstructionOK() {
	    return this.constructionOK;
	}
	
	public static String getTimestamp() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy-");
		return sdf.format(new Date()) + System.currentTimeMillis();
    }
	

}
