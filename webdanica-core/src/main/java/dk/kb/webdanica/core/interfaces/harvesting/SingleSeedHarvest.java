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

import dk.kb.webdanica.core.batch.WARCExtractUrlsJob;
import dk.kb.webdanica.core.datamodel.AnalysisStatus;
import dk.kb.webdanica.core.datamodel.criteria.SingleCriteriaResult;
import dk.kb.webdanica.core.exceptions.WebdanicaException;
import dk.kb.webdanica.core.interfaces.harvesting.SeedReport.SeedReportEntry;
import dk.netarkivet.common.CommonSettings;
import dk.netarkivet.common.distribute.arcrepository.ArcRepositoryClientFactory;
import dk.netarkivet.common.distribute.arcrepository.BitarchiveRecord;
import dk.netarkivet.common.distribute.arcrepository.Replica;
import dk.netarkivet.common.utils.ApplicationUtils;
import dk.netarkivet.common.utils.FileUtils;
import dk.netarkivet.common.utils.Settings;
import dk.netarkivet.common.utils.StreamUtils;
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
		
		eventHarvest.addSeeds(seedSet, templateName, maxBytes, maxObjects, attributeValues);
		eventHarvest.setActive(true);
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
		if (writeToSystemOut){ 
			System.out.println("The following files were harvested: ");
			for (String line: lines) {
				System.out.println(line);
			}
		}
		this.files = lines;
		
		//get the reports associated with the harvest as well, extracted from the metadatawarc.file.
		this.reports = null;
		try {
			this.reports = getReports(theJob.getJobID());
		} catch (Throwable e) {
			if (writeToSystemOut) {
				e.printStackTrace();
			} else {
				String error = "Unable to retrieve the reports for job '" + theJob.getJobID() + "': " + e; 
				logger.warning(error);
				this.errMsg.append(error);
			}
		}
		// get the urls harvested by the job
		this.fetchedUrls = null;
		try {
			this.fetchedUrls = getHarvestedUrls(getHeritrixWarcs(), writeToSystemOut);
		} catch (Throwable e) {
			if (writeToSystemOut) {
				e.printStackTrace();
			} else {
				String error = "Unable to retrieve the fetchedUrls for job '" + theJob.getJobID() + "'";
				logger.log(Level.WARNING, error, e);
				this.errMsg.append(error);
			}
		}
		this.successful = status.equals(JobStatus.DONE);
		String failureReason = "";
		if (reports != null && reports.getSeedReport() != null) {
			// Look at the seedreport
			SeedReport sr = reports.getSeedReport();
			SeedReportEntry entry = sr.getEntry(this.seed);
			if (entry == null) {
				failureReason = " The seed was not found in the seedreport. Only exists these entries:  " +  StringUtils.join(sr.getSeeds(), ",");
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
		
		this.harvestedTime = theJob.getActualStop().getTime();
		if (this.successful) {
			this.analysisState = AnalysisStatus.AWAITING_ANALYSIS;
			this.analysisStateReason = "Harvest was successful, so analysis can be done.";
		} else {
			this.analysisState = AnalysisStatus.NO_ANALYSIS;
			this.analysisStateReason = "Harvest was not successful, so analysis can't be done." + failureReason;
		}
		return this.successful;
    }

	public static NasReports getReports(Long jobID) {
		Map<String, String> reportMap = new HashMap<String,String>();
		List<CDXRecord> records = Reporting.getMetadataCDXRecordsForJob(jobID);
	    for (CDXRecord record : records) {
	    	String key = record.getURL();
	    	//System.out.println("Fetching the record: " + key);
	        BitarchiveRecord baRecord = ArcRepositoryClientFactory.getViewerInstance().get(record.getArcfile(), 
	        		record.getOffset());
	        String data = StreamUtils.getInputStreamAsString(baRecord.getData());
	        reportMap.put(key, data);
	    }
	    return new NasReports(reportMap);
    }
	
	public static List<String> getHarvestedUrls(List<String> warcfiles, boolean writeToSystemOut) throws Exception {
		List<String> urls = new ArrayList<String>();
		if (warcfiles.isEmpty()) {
			String logMsg = "No heritrixWarcs seems to have been harvested. Something must have gone wrong. Returning an empty list";
			if (writeToSystemOut) {
				System.err.println(logMsg);
			} else {
				logger.warning(logMsg);
			}
		} else {
			String logMsg = "Fetching the harvested from urls from the " + warcfiles.size() + " HeritrixWarcs harvested: " + StringUtils.join(warcfiles, ","); 
			if (writeToSystemOut) {
				System.out.println(logMsg);
			} else {
				logger.info(logMsg);
			}
		}
		for (String warcfilename: warcfiles){
			urls.addAll(getUrlsFromFile(warcfilename));
		}
		String logMsg = "Retrieve " + urls.size() + " urls";
		if (writeToSystemOut) {
			System.out.println(logMsg);
		} else {
			logger.info(logMsg);
		}
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

	public static int writeHarvestLog(File harvestLog, String harvestLogHeader, boolean onlySuccessFul, List<SingleSeedHarvest> results) throws Exception {
		// Initialize harvestLogWriter
    	PrintWriter harvestLogWriter = new PrintWriter(new BufferedWriter(new FileWriter(harvestLog)));
    	int harvestsWritten = 0;
    	harvestLogWriter.println(harvestLogHeader);
    	harvestLogWriter.println(StringUtils.repeat("#", 80));
    	for (SingleSeedHarvest s: results) {
    		if (onlySuccessFul && !s.successful) {
    			continue;
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

	public static String getTimestamp() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy-");
		return sdf.format(new Date()) + System.currentTimeMillis();
    }

}
