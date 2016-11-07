package dk.kb.webdanica.core.datamodel.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dk.kb.webdanica.core.datamodel.Cache;
import dk.kb.webdanica.core.datamodel.JDBCUtils;

public class HBasePhoenixCacheDAO implements CacheDAO {
	
	@Override
	public Cache getCache() throws Exception {
		Cache cache = null;
		PreparedStatement stm = null;
		ResultSet rs = null;
		try {
			Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
			stm = conn.prepareStatement("");
			stm.clearParameters();
			rs = stm.executeQuery();
			if (rs != null && rs.next()) {
					// Lists of "status-ordinal:count" pairs
					List<String> seedStatusCounts = JDBCUtils.sqlArrayToArrayList(rs.getArray("seedStatusCounts"));
					Map<Integer, Long> seedStatusCountsMap = makeMapFromCountList(seedStatusCounts);
					List<String> seedDanicaStatusCounts = JDBCUtils.sqlArrayToArrayList(rs.getArray("seedDanicaStatusCounts"));
					Map<Integer, Long> seedDanicaStatusCountsMap = makeMapFromCountList(seedDanicaStatusCounts);

					cache = new Cache(rs.getLong("totalSeedsCount"), 
							seedStatusCountsMap,
							seedDanicaStatusCountsMap,
							rs.getLong("harvestCount"),
							rs.getLong("totalCritResults"),
							rs.getTimestamp("last_updated").getTime());
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
}
