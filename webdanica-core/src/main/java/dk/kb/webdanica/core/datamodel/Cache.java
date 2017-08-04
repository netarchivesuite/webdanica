package dk.kb.webdanica.core.datamodel;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import dk.kb.webdanica.core.datamodel.dao.CacheDAO;
import dk.kb.webdanica.core.datamodel.dao.CriteriaResultsDAO;
import dk.kb.webdanica.core.datamodel.dao.DAOFactory;
import dk.kb.webdanica.core.datamodel.dao.HBasePhoenixDAOFactory;
import dk.kb.webdanica.core.datamodel.dao.HarvestDAO;
import dk.kb.webdanica.core.datamodel.dao.SeedsDAO;
import dk.kb.webdanica.core.datamodel.dao.*;

/** 
 * Class for a cache of the counts of the database
 * 
 * list/count: domains from seeds or from domain
 * list/count: tlds from seeds/domains select
 * count: #seeds for each status	(select distinct(status) from seeds), for each: select count
 * count: #seeds for each danica status (select distinct(danica_status) from seeds), for each value: select count
 * 
 * Not needed:
 * count: blacklists (active/disabled/all)
 */
public class Cache {
	
	private long totalSeedsCount; 
	private Map<Integer, Long> seedStatusCountsMap;
	private Map<Integer, Long> seedDanicaStatusCountsMap;
	private long harvestcount;
	private long criteriaResults;
    private long updatedTime;
	private Long uuid;

    public static void main (String[] args) throws Exception {
    	DAOFactory dao = new HBasePhoenixDAOFactory();
    	System.out.println("Starting to update statecache at " +  new Date());
    	Cache.updateCache(dao);
    }
    
    /**
     * 
     * @param totalSeedsCount
     * @param seedStatusCountsMap
     * @param seedDanicaStatusCountsMap
     * @param harvestcount
     * @param criteriaResults
     * @param updatedTime
     */
	public Cache(long totalSeedsCount, Map<Integer, Long> seedStatusCountsMap,
            Map<Integer, Long> seedDanicaStatusCountsMap, long harvestcount, long criteriaResults,
            long updatedTime, Long uuid) {
		
		this.totalSeedsCount = totalSeedsCount;
		this.seedStatusCountsMap = seedStatusCountsMap;
		this.seedDanicaStatusCountsMap = seedDanicaStatusCountsMap;
		this.harvestcount = harvestcount;
		this.criteriaResults = criteriaResults;
		this.updatedTime = updatedTime;
		this.uuid = uuid;
    }

	public Cache(long totalSeedsCount, Map<Integer, Long> seedStatusCountMap,
			Map<Integer, Long> seedDanicaStatusCountMap, long harvestCount,
			long totalCritResults, long updatedTime) {
		this.totalSeedsCount = totalSeedsCount;
		this.seedStatusCountsMap = seedStatusCountMap;
		this.seedDanicaStatusCountsMap = seedDanicaStatusCountMap;
		this.harvestcount = harvestCount;
		this.criteriaResults = totalCritResults;
		this.updatedTime = updatedTime;
	}

	public static void updateCache(DAOFactory daoFactory) throws Exception {
		SeedsDAO dao = daoFactory.getSeedsDAO();
		long totalSeedsCount = dao.getSeedsCount(null);
	    Map<Integer, Long> seedStatusCountMap = new HashMap<Integer,Long>();
	    for (Status s: Status.values()) { // or (select distinct(status) from seeds)
	    	seedStatusCountMap.put(s.ordinal(), dao.getSeedsCount(s));
	    }
	    Map<Integer, Long> seedDanicaStatusCountMap = new HashMap<Integer,Long>();
	    for (DanicaStatus s: DanicaStatus.values()) { // or (select distinct(danica_status) from seeds)
	    	seedDanicaStatusCountMap.put(s.ordinal(), dao.getSeedsDanicaCount(s));
	    }
	    HarvestDAO hdao = daoFactory.getHarvestDAO();
	    long harvestCount = hdao.getCount();
	    CriteriaResultsDAO cdao = daoFactory.getCriteriaResultsDAO(); 
	    long totalCritResults = cdao.getCountByHarvest(null); // null meaning = get total count
	    
	    Cache cache = new Cache(totalSeedsCount, seedStatusCountMap, seedDanicaStatusCountMap, harvestCount, totalCritResults, System.currentTimeMillis());
	    daoFactory.getCacheDAO().updateCache(cache);
	}
	
	public static Cache getCache(DAOFactory daofactory) throws Exception {
		CacheDAO dao = daofactory.getCacheDAO();
		return dao.getCache();
	}

	public long getTotalSeedsCount() {
		return totalSeedsCount;
	}

	public void setTotalSeedsCount(long totalSeedsCount) {
		this.totalSeedsCount = totalSeedsCount;
	}

	public Map<Integer, Long> getSeedStatusCountsMap() {
		return seedStatusCountsMap;
	}

	public void setSeedStatusCountsMap(Map<Integer, Long> seedStatusCountsMap) {
		this.seedStatusCountsMap = seedStatusCountsMap;
	}

	public Map<Integer, Long> getSeedDanicaStatusCountsMap() {
		return seedDanicaStatusCountsMap;
	}

	public void setSeedDanicaStatusCountsMap(
			Map<Integer, Long> seedDanicaStatusCountsMap) {
		this.seedDanicaStatusCountsMap = seedDanicaStatusCountsMap;
	}

	public long getHarvestcount() {
		return harvestcount;
	}

	public void setHarvestcount(long harvestcount) {
		this.harvestcount = harvestcount;
	}

	public long getCriteriaResults() {
		return criteriaResults;
	}

	public void setCriteriaResults(long criteriaResults) {
		this.criteriaResults = criteriaResults;
	}

	public long getUpdatedTime() {
		return updatedTime;
	}

	public void setUpdatedTime(long updatedTime) {
		this.updatedTime = updatedTime;
	}

	public Long getUuid() {
		return uuid;
	}

	public void setUuid(Long uuid) {
		this.uuid = uuid;
	}

	/** 
	 * 
	 * @return dummy cache values (-1L for all values)
	 */
	public static Cache getDummyCache() {
		Long dummyValue = -1L;
	    Map<Integer, Long> seedStatusCountMap = new HashMap<Integer,Long>();
	    for (Status s: Status.values()) { 
	    	seedStatusCountMap.put(s.ordinal(), dummyValue);
	    }
	    Map<Integer, Long> seedDanicaStatusCountMap = new HashMap<Integer,Long>();
	    for (DanicaStatus s: DanicaStatus.values()) { 
	    	seedDanicaStatusCountMap.put(s.ordinal(), dummyValue);
	    }
		return new Cache(dummyValue, seedStatusCountMap, seedDanicaStatusCountMap, dummyValue, dummyValue, System.currentTimeMillis());
	}
	
}
