package dk.kb.webdanica.core.datamodel;

import java.util.HashMap;
import java.util.Map;

import dk.kb.webdanica.core.datamodel.dao.CacheDAO;
import dk.kb.webdanica.core.datamodel.dao.CriteriaResultsDAO;
import dk.kb.webdanica.core.datamodel.dao.DAOFactory;
import dk.kb.webdanica.core.datamodel.dao.HarvestDAO;
import dk.kb.webdanica.core.datamodel.dao.SeedsDAO;

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
	
	public Cache(long long1, Map<Integer, Long> seedStatusCountsMap,
            Map<Integer, Long> seedDanicaStatusCountsMap, long long2, long long3,
            long l) {
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
	}
	
	public static Cache getCache(DAOFactory daofactory) throws Exception {
		CacheDAO dao = daofactory.getCacheDAO();
		return dao.getCache();
	}
	
	
	
}
