package dk.kb.webdanica.core.datamodel.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import dk.kb.webdanica.core.utils.DatabaseUtils;
import dk.kb.webdanica.core.datamodel.criteria.DataSource;
import dk.kb.webdanica.core.datamodel.criteria.SingleCriteriaResult;

public class HBasePhoenixCriteriaResultsDAO implements CriteriaResultsDAO {
	
	private static final Logger logger = Logger.getLogger(HBasePhoenixCriteriaResultsDAO.class.getName());

	private SingleCriteriaResult getResultFromResultSet(ResultSet rs) throws Exception {
		SingleCriteriaResult s = null;
		s = new SingleCriteriaResult();
		s.url = rs.getString("url");
		s.urlOrig = rs.getString("UrlOrig");
		s.seedurl = rs.getString("seedurl");
		s.harvestName = rs.getString("harvestname");
		s.hostname = rs.getString("hostname");
		s.domainName = rs.getString("domain");
		s.errorMsg = rs.getString("error");
		s.Cext1 = rs.getLong("Cext1");
		s.Cext2 = rs.getLong("Cext2");
		s.Cext3 = rs.getLong("Cext3");
		//s.Cext3Orig = row.getString("extWDateOrig");
		for (String c: SingleCriteriaResult.StringCriteria) {
			s.C.put(c, rs.getString(c));
		}
		s.intDanish = rs.getFloat("intDanish");
		s.source = DataSource.fromOrdinal(rs.getInt("source"));
		s.calcDanishCode = rs.getInt("calcDanishCode");
		s.CText = rs.getString("CText");
		s.CLinks = DatabaseUtils.sqlArrayToArrayList(rs.getArray("CLinks"));
		s.insertedDate = rs.getLong("inserted_time");
		s.updatedDate = rs.getLong("updated_time");
		return s;
	}

	private List<SingleCriteriaResult> getResultsFromResultSet(ResultSet rs) throws Exception {
		List<SingleCriteriaResult> resultList = new ArrayList<SingleCriteriaResult>();
		if (rs != null) {
			while (rs.next()) {
				resultList.add(getResultFromResultSet(rs));
			}
		}
		return resultList;
	}
	
	public static final String INSERT_SQL;

	static {
		INSERT_SQL = ""
				+ "UPSERT INTO criteria_results "
				+ "(url, urlOrig, seedurl, harvestname, hostname, domain, error, Cext1, Cext2, Cext3, " //1-10
				+ "C1a, C2a, C2b, C3a, C3b, C3c, C3d, C3e, C3f, C3g, " // 11-20
				+ "C4a, C4b, C5a, C5b, C6a, C6b, C6c, C6d, " // 21-28
				+ "C7a, C7b, C7c, C7d, C7e, C7f, C7g, C7h, " //29-36
				+ "C8a, C8b, C8c, C9a, C9b, C9c, C9d, C9e, C9f, " //37-44
				+ "C10a, C10b, C10c, C15a, C15b, C16a, C17a, C18a, intDanish, source, calcDanishCode, CText, CLinks, " //46-58
				+ "inserted_time, updated_time) " //59-60
				+ " VALUES (" + StringUtils.repeat("?,", 59) + "?) ";							
	}

	/**
	 * Insert SingleCriteriaResult.
	 * @param singleAnalysis A {@link SingleCriteriaResult} object
	 * @return true, if the insertion was successful, otherwise false
	 */
	@Override
	public boolean insertRecord(SingleCriteriaResult singleAnalysis) throws Exception {
		boolean result = upsertRecord(singleAnalysis, false);
		return result;
	}
	
	@Override
	public boolean updateRecord(SingleCriteriaResult singleAnalysis) throws Exception {
		boolean result = upsertRecord(singleAnalysis, true);
		return result;
	}
		
	
	private boolean upsertRecord(SingleCriteriaResult singleAnalysis, boolean isUpdate) throws Exception {
		Long insertedTime = null;
		Long updatedTime = null;
		if (isUpdate) {
			insertedTime = singleAnalysis.insertedDate;
			updatedTime = new Date().getTime();
		} else {
			insertedTime = new Date().getTime();
			updatedTime = insertedTime;
		}
		java.sql.Array sqlArr = null;
		PreparedStatement stm = null;
		int idx = 1;
		int res = 0;
		try {
			List<String> strList = singleAnalysis.CLinks;
			String[] strArr = new String[strList.size()];
			strArr = strList.toArray(strArr);
			Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
			sqlArr = conn.createArrayOf("VARCHAR", strArr);
			stm = conn.prepareStatement(INSERT_SQL);
			stm.clearParameters();
			stm.setString(idx++, singleAnalysis.url);
			stm.setString(idx++, singleAnalysis.urlOrig);
			stm.setString(idx++, singleAnalysis.seedurl);
			stm.setString(idx++, singleAnalysis.harvestName);
			stm.setString(idx++, singleAnalysis.hostname);
			stm.setString(idx++, singleAnalysis.domainName);
			stm.setString(idx++, singleAnalysis.errorMsg);
			
			if (singleAnalysis.Cext1 == null) {
				stm.setNull(idx, Types.BIGINT);
			} else {
				stm.setLong(idx, singleAnalysis.Cext1);
			}
			idx++;
			if (singleAnalysis.Cext2 == null) {
				stm.setNull(idx, Types.BIGINT);
			} else {
				stm.setLong(idx, singleAnalysis.Cext2);
			}
			idx++;
			if (singleAnalysis.Cext3 == null) {
				stm.setNull(idx, Types.BIGINT);
			} else {
				stm.setLong(idx, singleAnalysis.Cext3);
			}
			idx++;
			for (int i=0; i<SingleCriteriaResult.StringCriteria.length; ++i) {
				stm.setString(idx++, singleAnalysis.C.get(SingleCriteriaResult.StringCriteria[i]));
			}
			stm.setFloat(idx++, singleAnalysis.intDanish);
			if (singleAnalysis.source == null) {
				stm.setNull(idx, Types.BIGINT);
			} else {
				stm.setInt(idx, singleAnalysis.source.ordinal());
			}
			idx++;
			stm.setInt(idx++, singleAnalysis.calcDanishCode);
			stm.setString(idx++, singleAnalysis.CText);
			stm.setArray(idx++, sqlArr);
			stm.setLong(idx++, insertedTime);
			stm.setLong(idx++, updatedTime);
			res = stm.executeUpdate();
			conn.commit();
		} finally {
			if (sqlArr != null) {
				sqlArr.free();
			}
			if (stm != null) {
				stm.close();
			}
		}
		return res != 0;
	}
	
	public static final String READ_ALL_WITH_URL_SQL;

	static {
		READ_ALL_WITH_URL_SQL = ""
				+ "SELECT * FROM criteria_results "
				+ "WHERE url=?";
	}
	
	@Override
	public List<SingleCriteriaResult> getResultsByUrl(String url) throws Exception {
		List<SingleCriteriaResult> resultList = new ArrayList<SingleCriteriaResult>();
		PreparedStatement stm = null;
		ResultSet rs = null;
		try {
			Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
			stm = conn.prepareStatement(READ_ALL_WITH_URL_SQL);
			stm.clearParameters();
			stm.setString(1, url);
			rs = stm.executeQuery();
			resultList = getResultsFromResultSet(rs);
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (stm != null) {
				stm.close();
			}
		}
		return resultList; 
	}

	public static final String READ_ALL_WITH_SEEDURL_SQL;

	static {
		READ_ALL_WITH_SEEDURL_SQL = ""
				+ "SELECT * FROM criteria_results "
				+ "WHERE seedurl=?";
	}

	@Override
	public List<SingleCriteriaResult> getResultsBySeedurl(String seedurl) throws Exception {
		List<SingleCriteriaResult> seedList = new ArrayList<SingleCriteriaResult>();
		PreparedStatement stm = null;
		ResultSet rs = null;
		try {
			Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
			stm = conn.prepareStatement(READ_ALL_WITH_SEEDURL_SQL);
			stm.clearParameters();
			stm.setString(1, seedurl);
			rs = stm.executeQuery();
			seedList = getResultsFromResultSet(rs);
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (stm != null) {
				stm.close();
			}
		}
		return seedList; 
	}
	
	public static final String READ_ALL_WITH_HARVESTNAME_SQL;

	static {
		READ_ALL_WITH_HARVESTNAME_SQL = ""
				+ "SELECT * FROM criteria_results "
				+ "WHERE harvestname=?";
	}

	@Override
	public List<SingleCriteriaResult> getResultsByHarvestname(String harvestname) throws Exception {
		List<SingleCriteriaResult> seedList = new ArrayList<SingleCriteriaResult>();
		PreparedStatement stm = null;
		ResultSet rs = null;
		try {
			Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
			stm = conn.prepareStatement(READ_ALL_WITH_HARVESTNAME_SQL);
			stm.clearParameters();
			stm.setString(1, harvestname);
			rs = stm.executeQuery();
			seedList = getResultsFromResultSet(rs);
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (stm != null) {
				stm.close();
			}
		}
		return seedList; 
	}
	
	public static final String READ_URLS_BY_HARVESTNAME_SQL;

	static {
		READ_URLS_BY_HARVESTNAME_SQL = ""
				+ "SELECT url FROM criteria_results "
				+ "WHERE harvestname=?";
	}
	
	@Override
	public List<String> getHarvestedUrls(String harvestname) throws Exception {
		List<String> urlList = new ArrayList<String>();
		PreparedStatement stm = null;
		ResultSet rs = null;
		try {
			Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
			stm = conn.prepareStatement(READ_URLS_BY_HARVESTNAME_SQL);
			stm.clearParameters();
			stm.setString(1, harvestname);
			rs = stm.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					String s = rs.getString("url");
					urlList.add(s);
				}
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (stm != null) {
				stm.close();
			}
		}
		return urlList; 
	}

	@Override
	public void deleteRecordsByHarvestname(String harvestname) throws Exception {
		List<String> urls = getHarvestedUrls(harvestname);
		for (String url: urls) {
			deleteRecordsByHarvestnameAndUrl(harvestname, url);
		}
    }

	public static final String DELETE_ALL_WIT_HURLANDHARVESTNAME_SQL;

	static {
		DELETE_ALL_WIT_HURLANDHARVESTNAME_SQL = ""
				+ "DELETE FROM criteria_results "
				+ "WHERE url=? AND harvestname=?";
	}

	private void deleteRecordsByHarvestnameAndUrl(String harvestname, String url) throws Exception {
		PreparedStatement stm = null;
		try {
			Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
			stm = conn.prepareStatement(DELETE_ALL_WIT_HURLANDHARVESTNAME_SQL);
			stm.clearParameters();
			stm.setString(1, harvestname);
			stm.setString(2, url);
			stm.executeUpdate();
		} finally {
			if (stm != null) {
				stm.close();
			}
		}
    }

	public static final String READ_WITH_URLANDHARVESTNAME_SQL;

	static {
		READ_WITH_URLANDHARVESTNAME_SQL = ""
				+ "SELECT * FROM criteria_results "
				+ "WHERE url=? AND harvestname=?";
	}

	@Override
	public SingleCriteriaResult getSingleResult(String url, String harvest) throws Exception {
		SingleCriteriaResult s = null;
		PreparedStatement stm = null;
		ResultSet rs = null;
		try {
			Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
			stm = conn.prepareStatement(READ_WITH_URLANDHARVESTNAME_SQL);
			stm.clearParameters();
			stm.setString(1, url);
			stm.setString(2, harvest);
			rs = stm.executeQuery();
			if (rs != null && rs.next()) {
				s = getResultFromResultSet(rs);
			} else {
				logger.warning("No CriteriaResult found for url '" + url + "' and harvest '" + harvest + "'!");
			}
		} catch (Throwable e) {
			logger.severe("Failure retrieving a CriteriaResult found for url '" + url + "' and harvest '" + harvest + "': " + e);
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (stm != null) {
				stm.close();
			}
		}
	    return s;
    }

	private static final String READ_ALL_SQL = "SELECT * FROM criteria_results";

	@Override
	public List<SingleCriteriaResult> getResults() throws Exception {
		List<SingleCriteriaResult> list = new ArrayList<SingleCriteriaResult>();
		PreparedStatement stm = null;
		ResultSet rs = null;
		try {
			Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
			stm = conn.prepareStatement(READ_ALL_SQL);
			stm.clearParameters();
			rs = stm.executeQuery();
			list = getResultsFromResultSet(rs);
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (stm != null) {
				stm.close();
			}
		}
		return list; 
    }

	public static final String GET_COUNT_WITH_HARVESTNAME_SQL;
	public static final String GET_COUNT_SQL;

	static {
		GET_COUNT_WITH_HARVESTNAME_SQL = ""
				+ "SELECT count(*) FROM criteria_results "
				+ "WHERE harvestname=?";
		
		 GET_COUNT_SQL = ""
	                + "SELECT count(*) FROM criteria_results";
	}

	@Override
	public long getCountByHarvest(String harvestName) throws Exception {
		long count = 0;
		PreparedStatement stm = null;
		ResultSet rs = null;
		try {
			Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
			if (harvestName == null) {
			    stm = conn.prepareStatement(GET_COUNT_SQL);
			    stm.clearParameters();
			} else {
			    stm = conn.prepareStatement(GET_COUNT_WITH_HARVESTNAME_SQL);
			    stm.clearParameters();
			    stm.setString(1, harvestName);
			}
			rs = stm.executeQuery();
			if (rs != null && rs.next()) {
                count = rs.getLong(1);
            }
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (stm != null) {
				stm.close();
			}
		}
		return count;
	}

}
