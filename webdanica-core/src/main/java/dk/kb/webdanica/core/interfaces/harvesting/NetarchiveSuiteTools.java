package dk.kb.webdanica.core.interfaces.harvesting;

import java.util.List;
import java.util.logging.Logger;

import dk.kb.webdanica.core.exceptions.WebdanicaException;
import dk.netarkivet.harvester.datamodel.HarvestDefinitionDAO;
import dk.netarkivet.harvester.datamodel.JobDAO;
import dk.netarkivet.harvester.datamodel.JobStatusInfo;
import dk.netarkivet.harvester.webinterface.HarvestStatus;
import dk.netarkivet.harvester.webinterface.HarvestStatusQuery;

public class NetarchiveSuiteTools {
    
    private static final Logger logger = Logger.getLogger(NetarchiveSuiteTools.class.getName());
    
    /**
     * @param hid
     * @return JobStatus of the job in progress (expects only one job to be created)
     */
    public static JobStatusInfo getHarvestStatus(Long hid) {
        if (hid == null) {
            logger.warning("Null harvestdefinition is not valid");
            return null;
        }
        HarvestDefinitionDAO hdao = HarvestDefinitionDAO.getInstance();
        if (hdao.exists(hid)) {
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
        } else {
            logger.warning("No harvestdefinition w/id=" + hid + " exists!");
            return null;
        }
    } 
    
    /**
     * @param harvestdefinitionName
     * @return JobStatus of the job in progress (expects only one job to be created)
     */
    public static JobStatusInfo getHarvestStatus(String harvestdefinitionName) {
        if (harvestdefinitionName == null) {
            logger.warning("Null harvestdefinitionName is not valid");
            return null;
        }
        HarvestDefinitionDAO hdao = HarvestDefinitionDAO.getInstance();
        
        if (hdao.exists(harvestdefinitionName)) {
            Long hid = hdao.getHarvestDefinition(harvestdefinitionName).getOid();
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
        } else {
            logger.warning("No harvestdefinition w/name='" + harvestdefinitionName + "' exists!");
            return null;
        }
    }
    
    
}
