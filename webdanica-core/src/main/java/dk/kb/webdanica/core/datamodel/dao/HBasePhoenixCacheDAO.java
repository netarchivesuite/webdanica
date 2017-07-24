package dk.kb.webdanica.core.datamodel.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import dk.kb.webdanica.core.datamodel.Cache;
import dk.kb.webdanica.core.utils.DatabaseUtils;

/**
 * CREATE TABLE statecache (
 *   uuid BIGINT PRIMARY KEY,
 *   totalSeedsCount BIGINT,
 *   harvestCount BIGINT,
 *   totalCritResults BIGINT,
 *   seedStatusCounts VARCHAR[],
 *   seedDanicaStatusCounts VARCHAR[],
 *   last_updated TIMESTAMP
 * );
 *
 */
public class HBasePhoenixCacheDAO implements CacheDAO {
	
	private static final Logger logger = Logger.getLogger(HBasePhoenixCacheDAO.class.getName());
	
	private static final String INSERT_SQL; 
	static {
		INSERT_SQL = ""
				+ "UPSERT INTO statecache (uuid, totalSeedsCount, harvestCount, totalCritResults, seedStatusCounts, seedDanicaStatusCounts, last_updated)"
				+ "VALUES (?, ?, ?, ?, ?, ?, ?) ";
	}
	
	@Override
	public Cache getCache() throws Exception {
		Cache cache = null;
		PreparedStatement stm = null;
		ResultSet rs = null;
		try {
			Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
			stm = conn.prepareStatement("select * from statecache");
			stm.clearParameters();
			rs = stm.executeQuery();
			if (rs != null && rs.next()) {
					// Lists of "status-ordinal:count" pairs
					List<String> seedStatusCounts = DatabaseUtils.sqlArrayToArrayList(rs.getArray("seedStatusCounts"));
					Map<Integer, Long> seedStatusCountsMap = makeMapFromCountList(seedStatusCounts);
					List<String> seedDanicaStatusCounts = DatabaseUtils.sqlArrayToArrayList(rs.getArray("seedDanicaStatusCounts"));
					Map<Integer, Long> seedDanicaStatusCountsMap = makeMapFromCountList(seedDanicaStatusCounts);

					cache = new Cache(rs.getLong("totalSeedsCount"), 
							seedStatusCountsMap,
							seedDanicaStatusCountsMap,
							rs.getLong("harvestCount"),
							rs.getLong("totalCritResults"),
							rs.getTimestamp("last_updated").getTime(),
							rs.getLong("uuid"));
			} else {
				logger.log(Level.WARNING, "No statecache record found - null record returned");
				return null;
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (stm != null) {
				stm.close();
			}
		}
	
		return cache;
	}

	public static Map<Integer, Long> makeMapFromCountList(List<String> countList) {
		Map<Integer, Long> map = new HashMap<Integer,Long>();
		for (String countString : countList) {
			String[] counts = countString.split(":");
			map.put(Integer.valueOf(counts[0]), Long.valueOf(counts[1])); 
		}
		return map;
	}
	
	public static List<String> makeListFromMap(Map<Integer, Long> countMap) {
		List<String> countList = new ArrayList<String>();
		for (Entry<Integer, Long> entry: countMap.entrySet()) {
			countList.add(entry.getKey() + ":" +  entry.getValue());
		}
		return countList;
	}
	

	@Override
	public boolean updateCache(Cache cache) throws Exception {
		long uid = System.currentTimeMillis();
		Cache existingCache = getCache();
		boolean cacheEmpty = existingCache == null; // If no cache yet available, getCache returns null
		if (!cacheEmpty) {
			uid = existingCache.getUuid();
		}
			
		java.sql.Array sqlArrA = null;
		java.sql.Array sqlArrB = null;
		PreparedStatement stm = null;
		int res = 0;
		try {
			Long updated_time = System.currentTimeMillis();
			List<String> strListA = makeListFromMap(cache.getSeedStatusCountsMap());
			List<String> strListB = makeListFromMap(cache.getSeedDanicaStatusCountsMap());
			
			String[] strArrA = new String[strListA.size()];
			String[] strArrB = new String[strListB.size()];
			
			strArrA = strListA.toArray(strArrA);
			strArrB = strListB.toArray(strArrB);
			Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
			sqlArrA = conn.createArrayOf("VARCHAR", strArrA);
			sqlArrB = conn.createArrayOf("VARCHAR", strArrB);
			
			stm = conn.prepareStatement(INSERT_SQL);
			stm.clearParameters();
			stm.setLong(1, uid);
			stm.setLong(2, cache.getTotalSeedsCount());
			stm.setLong(3, cache.getHarvestcount());
			stm.setLong(4, cache.getCriteriaResults());
			stm.setArray(5, sqlArrA);
			stm.setArray(6, sqlArrB);
			
			stm.setTimestamp(7, new Timestamp(updated_time));
			res = stm.executeUpdate();
			conn.commit();
		} finally {
			if (sqlArrA != null) {
				sqlArrA.free();
			}
			if (sqlArrB != null) {
				sqlArrB.free();
			}
			
			if (stm != null) {
				stm.close();
			}
		}
		return res != 0;
	}
}
