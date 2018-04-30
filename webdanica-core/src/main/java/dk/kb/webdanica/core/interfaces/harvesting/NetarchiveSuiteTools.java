package dk.kb.webdanica.core.interfaces.harvesting;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import dk.netarkivet.common.utils.ExceptionUtils;
import dk.netarkivet.harvester.datamodel.HarvestDBConnection;
import dk.netarkivet.harvester.datamodel.HarvestDefinition;
import dk.netarkivet.harvester.datamodel.HarvestDefinitionDAO;
import dk.netarkivet.harvester.datamodel.JobStatus;

public class NetarchiveSuiteTools {
    
    private static final Logger logger = Logger.getLogger(NetarchiveSuiteTools.class.getName());
    
    
    public static HarvestDefinition getHarvestDefinition(String harvestdefinitionName) {
        if (harvestdefinitionName == null) {
            logger.warning("Null harvestdefinitionName is not valid");
            return null;
        }
        HarvestDefinitionDAO hdao = HarvestDefinitionDAO.getInstance();
        
        if (hdao.exists(harvestdefinitionName)) {
            return hdao.getHarvestDefinition(harvestdefinitionName);
        } else {
            logger.warning("No harvestdefinition w/name='" + harvestdefinitionName + "' exists!");
            return null;
        }
    }
    
    public static HarvestDefinition getHarvestDefinition(Long hid) {
        if (hid == null) {
            logger.warning("Null harvestdefinitionId is not valid");
            return null;
        }
        HarvestDefinitionDAO hdao = HarvestDefinitionDAO.getInstance();
        
        if (hdao.exists(hid)) {
            return hdao.read(hid);
        } else {
            logger.warning("No harvestdefinition w/id='" + hid + "' exists!");
            return null;
        }
    }
    
    public static Long getHarvestDefinitionID(String hdName) {
        HarvestDefinitionDAO hdao = HarvestDefinitionDAO.getInstance();
        if (!hdao.exists(hdName)) {
            System.err.println("No harvestdefinition exists with name='" + hdName + "'");
            return null;
        } else {
            return hdao.getHarvestDefinition(hdName).getOid();
        }
    }
    
    /**
     * Tool for retrieving a list of jobs related to the given harvestdefinitionID.
     * @param hid A given harvestdefinitionID
     * @return the possibly empty list of jobs related to the given harvestdefinitionID
     */
    public static List<NasJob> getJobs(Long hid) {
        List<NasJob> results = new ArrayList<NasJob>();
        Connection con = HarvestDBConnection.get();
        try {
            PreparedStatement stm = con.prepareStatement("SELECT job_id, status FROM jobs WHERE harvest_id=?");
            stm.setLong(1, hid);
            ResultSet result = stm.executeQuery();
            while (result.next()) {
                NasJob j = new NasJob(result.getLong(1), JobStatus.fromOrdinal(result.getInt(2)), hid);
                results.add(j);
            }
            stm.close();
        } catch (SQLException e) {
          logger.log(Level.WARNING, 
                  "Exception while finding jobs for hid = " + hid + ", cause= " + ExceptionUtils.getSQLExceptionCause(e), e);
        } finally {
            HarvestDBConnection.release(con);
        }
        return results;
    }
    
    /**
     * Tool for retrieving a job (if any) related to the given harvestdefinitionID.
     * @param hid A given harvestdefinitionID
     * @return the job related to the given harvestdefinitionID, or null, if none found
     */
    public static NasJob getNewHarvestStatus(Long hid) {
        List<NasJob> results = getJobs(hid);
        if (results.isEmpty()) {
            return null;
        } else if (results.size() == 1) {
            return results.get(0);
        } else {
            String jobsFound = "";
            for (NasJob j: results) {
                jobsFound += j.getJobId() + " ";
            }
            logger.warning("Returning null, as more than one job returned for hid=" + hid +  ". Jobs found=" + jobsFound);
            return null;
        }
    }
}
