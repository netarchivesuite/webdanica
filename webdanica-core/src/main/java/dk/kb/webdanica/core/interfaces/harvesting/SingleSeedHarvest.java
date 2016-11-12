package dk.kb.webdanica.core.interfaces.harvesting;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import dk.kb.webdanica.core.exceptions.WebdanicaException;
import dk.kb.webdanica.core.utils.SettingsUtilities;
import dk.netarkivet.common.distribute.arcrepository.ArcRepositoryClientFactory;
import dk.netarkivet.common.distribute.arcrepository.BitarchiveRecord;
import dk.netarkivet.common.utils.ApplicationUtils;
import dk.netarkivet.common.utils.FileUtils;
import dk.netarkivet.common.utils.StreamUtils;
import dk.netarkivet.common.utils.TimeUtils;
import dk.netarkivet.common.utils.cdx.CDXRecord;
import dk.netarkivet.harvester.datamodel.HarvestDefinition;
import dk.netarkivet.harvester.datamodel.HarvestDefinitionDAO;
import dk.netarkivet.harvester.datamodel.Job;
import dk.netarkivet.harvester.datamodel.JobDAO;
import dk.netarkivet.harvester.datamodel.JobStatus;
import dk.netarkivet.harvester.datamodel.JobStatusInfo;
import dk.netarkivet.harvester.datamodel.PartialHarvest;
import dk.netarkivet.harvester.datamodel.Schedule;
import dk.netarkivet.harvester.datamodel.ScheduleDAO;
import dk.netarkivet.harvester.datamodel.DomainConfiguration;
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
	
	final String seed; // The single seed being harvested;
	private String evName; // The name of the eventharvest
	private JobStatus finishedState;
	private List<String> files;
	private JobStatusInfo statusInfo;
	private Map<String, String> reportMap;
	private String errMsg;
	private boolean successful;
	private Throwable exception;
	private long maxBytes;
	private int maxObjects;
	private long harvestedTime;
	private Long hid;
	
	/**
	 * Currently harvests the site http://www.familien-carlsen.dk using the schedule 'Once'
	 * and Heritrix template 'webdanica_order'
	 * @param args currently no args read
	 * @throws Throwable When the property "dk.netarkivet.settings.file" either is not defined or points to a non existing file
	 */
	public static void main (String[] args) throws Throwable {
		// Verify that netarchiveSuite settings file is defined and exists
		SettingsUtilities.testPropertyFile("dk.netarkivet.settings.file", true);
		long maxBytes = 10000L;
		int maxOjects = 10000;
		SingleSeedHarvest ph = new SingleSeedHarvest("http://www.familien-carlsen.dk", 
				"test1" + System.currentTimeMillis(), "Once", "webdanica_order", maxBytes, maxOjects);
		boolean writeToStdOut = true; 
		boolean success = ph.finishHarvest(writeToStdOut);
		System.out.println("Harvest was successful: " + success);
		System.out.println("final state of harvest: " + ph.getFinalState());
		System.out.println("files harvested: " + StringUtils.join(ph.getFiles(), ","));
	}	
	
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
		this.evName = eventHarvestName;
		List<DomainConfiguration> noDcs = new ArrayList<DomainConfiguration>();
		this.maxBytes = maxBytes;
		this.maxObjects = maxObjects;
		
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
	    this.errMsg = error;
	    this.exception = e;
	    this.evName = harvestName;
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
	
	public String getHarvestName() {  // TODO This should never be null
		return this.evName;
	}
	
	public JobStatus getFinalState() {
	    return this.finishedState;
    }
	
	public JobStatusInfo getJobStatusInfo() {
		return this.statusInfo;
	}

	public Map<String, String> getReportMap() {
	    return reportMap;
    }
	
	/**
	 * Wait until harvest is finished or failed.
	 * @return true, if successful otherwise false;
	 */
	public boolean finishHarvest(boolean writeToSystemOut) {
		Set<JobStatus> finishedStates = new HashSet<JobStatus>();
		finishedStates.add(JobStatus.DONE);
		finishedStates.add(JobStatus.FAILED); 

		while (getHarvestStatus() == null){
			if (writeToSystemOut) 
				System.out.println("Waiting for job for eventharvest '" + evName + "' to be scheduled .."); 
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
		System.out.println("After " + TimeUtils.readableTimeInterval(usedtimeSecs*1000L) +" the job " + jobId + " now has finished state " + status );
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
		System.out.println("Retrieving the list of files belonging to the job: ");
		List<String> lines = Reporting.getFilesForJob(jobId.intValue(), harvestPrefix);
		System.out.println("The following files were harvested: ");
		for (String line: lines) {
			System.out.println(line);
		}
		this.files = lines;
		
		//get the reports associated with the harvest as well, extracted from the metadatawarc.file. 
		this.reportMap = getReports(theJob.getJobID()); //FIXME add now
		this.successful = status.equals(JobStatus.DONE);
		this.harvestedTime = theJob.getActualStop().getTime();
		return this.successful;
    }

	public static Map<String, String> getReports(Long jobID) {
		Map<String, String> reportMap = new HashMap<String,String>();
		List<CDXRecord> records = Reporting.getMetadataCDXRecordsForJob(jobID);
	    for (CDXRecord record : records) {
	    	String key = record.getURL();
	    	System.out.println("Fetching the record: " + key);
	        BitarchiveRecord baRecord = ArcRepositoryClientFactory.getViewerInstance().get(record.getArcfile(), 
	        		record.getOffset());
	        String data = StreamUtils.getInputStreamAsString(baRecord.getData());
	        reportMap.put(key, data);
	    }
	    return reportMap;
    }

	public List<String> getFiles() {
		return this.files;
	}
	
	public boolean successful() {
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
		return this.errMsg;
	}

	public long getHarvestedTime() {
	    return this.harvestedTime;
    }
	
	
	public static SingleSeedHarvest doSingleHarvest(String seed, String eventHarvestName, String scheduleName,
            String templateName, long maxBytes, int maxObjects, boolean writeToStdout) {
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
			String eventHarvestName = harvestPrefix + System.currentTimeMillis(); 
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
    		harvestLogWriter.println(HarvestReport.seedPattern + s.getSeed());
    		harvestLogWriter.println(HarvestReport.harvestnamePattern + s.getHarvestName());
    		harvestLogWriter.println(HarvestReport.successfulPattern + s.successful());
    		harvestLogWriter.println(HarvestReport.endstatePattern + s.getFinalState());
    		harvestLogWriter.println(HarvestReport.harvestedTimePattern + s.getHarvestedTime());
    		harvestLogWriter.println(HarvestReport.filesPattern + StringUtils.join(s.getFiles(), ","));
    		String errString = (s.getErrMsg() != null?s.getErrMsg():"");
    		String excpString = (s.getException() != null)? "" + s.getException():""; 
    		harvestLogWriter.println(HarvestReport.errorPattern + errString + " " + excpString);
    		harvestLogWriter.println(StringUtils.repeat("#", 80));
    		harvestsWritten++;
    	}  	
    	harvestLogWriter.close();
	    return harvestsWritten;
    }
	
	
}
