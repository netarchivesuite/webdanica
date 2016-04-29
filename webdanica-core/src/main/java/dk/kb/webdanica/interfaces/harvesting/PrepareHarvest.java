package dk.kb.webdanica.interfaces.harvesting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dk.kb.webdanica.exceptions.WebdanicaException;
import dk.netarkivet.harvester.datamodel.HarvestDefinition;
import dk.netarkivet.harvester.datamodel.HarvestDefinitionDAO;
import dk.netarkivet.harvester.datamodel.JobDAO;
import dk.netarkivet.harvester.datamodel.JobStatusInfo;
import dk.netarkivet.harvester.datamodel.PartialHarvest;
import dk.netarkivet.harvester.datamodel.Schedule;
import dk.netarkivet.harvester.datamodel.ScheduleDAO;
import dk.netarkivet.harvester.datamodel.DomainConfiguration;
import dk.netarkivet.harvester.webinterface.HarvestStatus;
import dk.netarkivet.harvester.webinterface.HarvestStatusQuery;

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
		PrepareHarvest ph = new PrepareHarvest("http://www.familien-carlsen.dk", "test-" + System.currentTimeMillis(), "Once");
	}
	
	
	
	final String seed;
	private String evName;
	
	
	public PrepareHarvest(String seed, String eventHarvestName, String scheduleName) {
		this.seed = seed;
		this.evName = eventHarvestName;
		List<DomainConfiguration> noDcs = new ArrayList<DomainConfiguration>();
		Schedule s = ScheduleDAO.getInstance().read(scheduleName);
		HarvestDefinition hd = new PartialHarvest(noDcs, s, eventHarvestName, "comments", "audience");
		HarvestDefinitionDAO.getInstance().create(hd);
		Map<String,String> attributeValues = new HashMap<String,String>(); // Empty attributeset
		
		 PartialHarvest eventHarvest = (PartialHarvest) HarvestDefinitionDAO.getInstance().getHarvestDefinition(
	             eventHarvestName);
		 Set<String> seedSet = new HashSet<String>();
		 seedSet.add(seed);
		 long maxBytes = 10000L; // What to write here, if we want to disable quotaenforcing
		 int maxObjects = 10000; // What to write here, if we want to disable quotaenforcing
		 String specialTemplateName =  "webdanica_order";
	     eventHarvest.addSeeds(seedSet, specialTemplateName, maxBytes, maxObjects, attributeValues);
		 
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
