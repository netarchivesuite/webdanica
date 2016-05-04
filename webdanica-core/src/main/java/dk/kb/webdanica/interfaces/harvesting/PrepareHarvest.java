package dk.kb.webdanica.interfaces.harvesting;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dk.kb.webdanica.exceptions.WebdanicaException;
import dk.netarkivet.common.utils.ApplicationUtils;
import dk.netarkivet.common.utils.FileUtils;
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
 * Prepare harvest of a single seed.
 *  (Requirement: JBDC access to netarchivesuuite harvestdatabase 
 *
 * -- Create event-harvest only containing this seed 
 * -- using only_once schedule
 * -- using special template disabling non-text harvesting, and disabling javascript
 */
public class PrepareHarvest {
	
	public static void main (String[] args) {
		//-Ddk.netarkivet.settings.file=/home/test/WEBDANICA/conf/settings_GUIApplication.xml 
		//-Ddk.netarkivet.settings.file=/home/svc/devel/webdanica/webdanica-core/src/resources/settings_Webdanica.xml
		PrepareHarvest ph = new PrepareHarvest("http://www.familien-carlsen.dk", "test-" + System.currentTimeMillis(), "Once", "webdanica_order");
		
		while (ph.getHarvestStatus() == null){
			System.out.println("Waiting for job to be scheduled .."); 
			try {
	            Thread.sleep(5000L);
            } catch (InterruptedException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
            }
		}
		
		// Harvest is now in progress ph.getHarvestStatus() != null
		JobStatusInfo jsi = ph.getHarvestStatus();
		Long jobId = jsi.getJobID();
		JobStatus status = jsi.getStatus();
		System.out.println("State of Job " + jobId + ": " + status);
		Set<JobStatus> finishedState = new HashSet<JobStatus>();
		finishedState.add(JobStatus.DONE);
		finishedState.add(JobStatus.FAILED);
		
		while (!finishedState.contains(status)) {
			System.out.println("Waiting for job to finish. Current state is " + status);
			try {
	            Thread.sleep(5000L);
            } catch (InterruptedException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
            }
			status = ph.getHarvestStatus().getStatus(); // Refresh status
		}
		System.out.println("Job " + jobId + " now has finished state " + status);
		
		// Look up the files associated with the job using a batchjob
		// e.g. by the method used by the jsp page:
		//  QA/QA-getfiles.jsp?jobid=4&harvestprefix=4-5
		Job theJob = JobDAO.getInstance().read(jobId);
		JobStatus jobEndState = theJob.getStatus();
		
		String harvestPrefix = theJob.getHarvestFilenamePrefix();
		ApplicationUtils.dirMustExist(FileUtils.getTempDir()); // Inserted to ensure that the getTempDir() exists.
		List<String> lines = Reporting.getFilesForJob(jobId.intValue(), harvestPrefix);
		System.out.println("The following files were harvested: ");
		for (String line: lines) {
			System.out.println(line);
		}
	}	
	
	
	final String seed;
	private String evName;
	
	
	public PrepareHarvest(String seed, String eventHarvestName, String scheduleName, String templateName) {
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
		 long maxBytes = 10000L; // What to write here, if we want to disable quotaenforcing
		 int maxObjects = 10000; // What to write here, if we want to disable quotaenforcing
	     eventHarvest.addSeeds(seedSet, templateName, maxBytes, maxObjects, attributeValues);
		 
		 eventHarvest.setActive(true);
		 HarvestDefinitionDAO.getInstance().update(eventHarvest);		 
	}
		
	public JobStatusInfo getHarvestStatus() {
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
}
