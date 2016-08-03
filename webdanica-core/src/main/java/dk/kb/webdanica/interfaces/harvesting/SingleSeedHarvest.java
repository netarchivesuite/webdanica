package dk.kb.webdanica.interfaces.harvesting;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import dk.kb.webdanica.exceptions.WebdanicaException;
import dk.kb.webdanica.utils.SettingsUtilities;
import dk.netarkivet.common.distribute.arcrepository.ArcRepositoryClientFactory;
import dk.netarkivet.common.distribute.arcrepository.BitarchiveRecord;
import dk.netarkivet.common.utils.ApplicationUtils;
import dk.netarkivet.common.utils.FileUtils;
import dk.netarkivet.common.utils.StreamUtils;
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
	private boolean successful;
	
	/**
	 * Currently harvests the site http://www.familien-carlsen.dk using the schedule 'Once'
	 * and Heritrix template 'webdanica_order'
	 * @param args currently no args read
	 * @throws Throwable When the property "dk.netarkivet.settings.file" either is not defined or points to a non existing file
	 */
	public static void main (String[] args) throws Throwable {
		// Verify that netarchiveSuite settings file is defined and exists
		SettingsUtilities.testPropertyFile("dk.netarkivet.settings.file");
		SingleSeedHarvest ph = new SingleSeedHarvest("http://www.familien-carlsen.dk", 
				"test1" + System.currentTimeMillis(), "Once", "webdanica_order");
		boolean success = ph.finishHarvest();
		System.out.println("Harvest was successful: " + success);
		System.out.println("final state of harvest: " + ph.getFinalState());
		System.out.println("files harvested: " + StringUtils.join(ph.getFiles(), ","));
	}	
	
	/**
	 * Wait until harvest is finished or failed.
	 * @return true, if successful otherwise false;
	 */
	public boolean finishHarvest() {
		
		while (getHarvestStatus() == null){
			System.out.println("Waiting for job to be scheduled .."); 
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
		System.out.println("State of Job " + jobId + ": " + status);
		Set<JobStatus> finishedStates = new HashSet<JobStatus>();
		finishedStates.add(JobStatus.DONE);
		finishedStates.add(JobStatus.FAILED); 
		
		while (!finishedStates.contains(status)) {
			System.out.println("Waiting for job '" + jobId + "' to finish. Current state is " + status);
			try {
				Thread.sleep(30000L); // 30 secs sleep
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			jsi = getHarvestStatus();
			status = jsi.getStatus(); // Refresh status
		}
		
		System.out.println("Job " + jobId + " now has finished state " + status);
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
		//this.reportMap = getReports(theJob.getJobID()); //TODO not tested - add later
		this.successful = status.equals(JobStatus.DONE);
		return this.successful;
    }

	private Map<String, String> getReports(Long jobID) {
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
	
	/**
	 * TODO check that a schedule with the given scheduleName exists, and that a template with the templateName exists.
	 * 
	 * @param seed
	 * @param eventHarvestName
	 * @param scheduleName
	 * @param templateName
	 */
	public SingleSeedHarvest(String seed, String eventHarvestName, String scheduleName, String templateName) {
		this.seed = seed;
		this.evName = eventHarvestName;
		List<DomainConfiguration> noDcs = new ArrayList<DomainConfiguration>();
		Schedule s = ScheduleDAO.getInstance().read(scheduleName);
		HarvestDefinition hd = new PartialHarvest(noDcs, s, eventHarvestName, "event harvest created by webdanica system at " + new Date(), "No specific audience");
		HarvestDefinitionDAO.getInstance().create(hd);
		Map<String,String> attributeValues = new HashMap<String,String>(); // Empty attributeset

		PartialHarvest eventHarvest = (PartialHarvest) HarvestDefinitionDAO.getInstance().getHarvestDefinition(eventHarvestName);
		Set<String> seedSet = new HashSet<String>();
		seedSet.add(seed);
		long maxBytes = 10000L; // TODO What to write here, if we want to disable quotaenforcing
		int maxObjects = 10000; // TODO What to write here, if we want to disable quotaenforcing
		eventHarvest.addSeeds(seedSet, templateName, maxBytes, maxObjects, attributeValues);
		eventHarvest.setActive(true);
		HarvestDefinitionDAO.getInstance().update(eventHarvest);		 
	}
	
	/**
	 * 
	 * @return JobStatus of the job in progress (expects only one job to be created)
	 */
	private JobStatusInfo getHarvestStatus() {
		HarvestDefinition hd = HarvestDefinitionDAO.getInstance().getHarvestDefinition(evName);
		Long oid = hd.getOid();
		HarvestStatusQuery hsq = new HarvestStatusQuery(oid, 0); 
		HarvestStatus hs = JobDAO.getInstance().getStatusInfo(hsq);
		List<JobStatusInfo> jobs = hs.getJobStatusInfo();
		if (jobs.size() == 0) { // No jobs yet created (What can go wrong here??)
			return null;
		} else if (jobs.size() == 1) {
			return jobs.get(0);
		} else {
			throw new WebdanicaException("Should be either 0 or 1 jobs generated, but there are  " + jobs.size() + " jobs for harvestId " + oid + " and harvestRun 0");   
		}
	} 
	
	public String getSeed() {
		return this.seed;
	}
	
	public String getHarvestName() {
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
}
