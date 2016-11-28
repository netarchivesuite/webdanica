package dk.kb.webdanica.core.datamodel.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import dk.kb.webdanica.core.datamodel.AnalysisStatus;
import dk.kb.webdanica.core.interfaces.harvesting.NasReports;
import dk.kb.webdanica.core.interfaces.harvesting.SingleSeedHarvest;
import dk.kb.webdanica.core.utils.DatabaseUtils;
import dk.netarkivet.harvester.datamodel.JobStatus;
/*
------------+--------------+-------------+------------------------+------------+----------------+--------------+----------------+-----------------+-----------------+-----------+----------+------------+
| TABLE_CAT  | TABLE_SCHEM  | TABLE_NAME  |      COLUMN_NAME       | DATA_TYPE  |   TYPE_NAME    | COLUMN_SIZE  | BUFFER_LENGTH  | DECIMAL_DIGITS  | NUM_PREC_RADIX  | NULLABLE  | REMARKS  | COLUMN_DEF |
+------------+--------------+-------------+------------------------+------------+----------------+--------------+----------------+-----------------+-----------------+-----------+----------+------------+
|            |              | HARVESTS    | HARVESTNAME            | 12         | VARCHAR        | null         | null           | null            | null            | 0         |          |            |
|            |              | HARVESTS    | SEEDURL                | 12         | VARCHAR        | null         | null           | null            | null            | 1         |          |            |
|            |              | HARVESTS    | ERROR                  | 12         | VARCHAR        | null         | null           | null            | null            | 1         |          |            |
|            |              | HARVESTS    | SUCCESSFUL             | 16         | BOOLEAN        | null         | null           | null            | null            | 1         |          |            |
|            |              | HARVESTS    | FINALSTATE             | 4          | INTEGER        | null         | null           | null            | null            | 1         |          |            |
|            |              | HARVESTS    | HARVESTED_TIME         | -5         | BIGINT         | null         | null           | null            | null            | 1         |          |            |
|            |              | HARVESTS    | FILES                  | 2003       | VARCHAR ARRAY  | null         | null           | null            | null            | 1         |          |            |
|            |              | HARVESTS    | FETCHED_URLS           | 2003       | VARCHAR ARRAY  | null         | null           | null            | null            | 1         |          |            |
|            |              | HARVESTS    | ANALYSIS_STATE         | 4          | INTEGER        | null         | null           | null            | null            | 1         |          |            |
|            |              | HARVESTS    | ANALYSIS_STATE_REASON  | 12         | VARCHAR        | null         | null           | null            | null            | 1         |          |            |
|            |              | HARVESTS    | REPORTS                | 2003  
*/
public class HBasePhoenixHarvestDAO implements HarvestDAO {

    public static long LIMIT = 100000L;
    
	public SingleSeedHarvest getHarvestFromResultSet(ResultSet rs) throws Exception {
		SingleSeedHarvest report = null;
		if (rs != null) {
			if (rs.next()) {
				report = getSingleSeed(rs);
			}
		}
		return report; 
	}

	public void getHarvestsFromResultSet(ResultSet rs, List<SingleSeedHarvest> harvestsFound) throws Exception {
		if (rs != null) {
			while (rs.next()) {
				harvestsFound.add(getSingleSeed(rs));
			}
		}
	}
	
	private SingleSeedHarvest getSingleSeed(ResultSet rs) throws Exception {
		return new SingleSeedHarvest(
				rs.getString("harvestname"),
				rs.getString("seedurl"),
				rs.getBoolean("successful"),
				DatabaseUtils.sqlArrayToArrayList(rs.getArray("files")),
				rs.getString("error"),
				JobStatus.fromOrdinal(rs.getInt("finalState")),
				rs.getLong("harvested_time"),
				NasReports.makeNasReportsFromJson(
						DatabaseUtils.sqlArrayToArrayList(rs.getArray("reports"))),
				DatabaseUtils.sqlArrayToArrayList(rs.getArray("fetched_urls")),		
				AnalysisStatus.fromOrdinal(rs.getInt("analysis_state")),
				rs.getString("analysis_state_reason")
				);
	}
	
	public static final String INSERT_SQL;

	static {
		INSERT_SQL = ""
				+ "UPSERT INTO harvests (harvestname, seedurl, finalState, successful, harvested_time, files, error, " //1-7
				+ "reports, fetched_urls " //8-9
				+ ",analysis_state, analysis_state_reason" //10-11
				+ ") " 
				+ "VALUES (?,?,?,?,?,?,?,?,?, ?, ?) ";
	}
	
	@Override
	public boolean insertHarvest(SingleSeedHarvest report) throws Exception {
		java.sql.Array sqlArr = null;
		PreparedStatement stm = null;
		int res = 0;
		try {
			long harvestedTime = report.getHarvestedTime();
			if (!(harvestedTime > 0)) {
				harvestedTime = System.currentTimeMillis();
				System.err.println("harvestedTime undefined. setting it to  " + harvestedTime);
			}
			List<String> strListFiles = report.getFiles();
			String[] strArrFiles = new String[strListFiles.size()];
			strArrFiles = strListFiles.toArray(strArrFiles);
			Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
			sqlArr = conn.createArrayOf("VARCHAR", strArrFiles);
			
			stm = conn.prepareStatement(INSERT_SQL);
			stm.clearParameters();
			stm.setString(1, report.getHarvestName());
			stm.setString(2, report.getSeed());
			stm.setInt(3, report.getFinalState().ordinal());
			stm.setBoolean(4, report.isSuccessful());
			stm.setLong(5, harvestedTime);
			stm.setArray(6, sqlArr);
			stm.setString(7, report.getErrMsg());
			//reports, fetched_urls, analysis_state, analysis_state_reason
			if (report.getReports()== null) {
				String[] emptyStrArr = new String[0];
				sqlArr = conn.createArrayOf("VARCHAR", emptyStrArr);
				stm.setArray(8, sqlArr);
			} else {
				List<String> strListReports = report.getReports().getReportsAsJsonLists();
				String[] strArrReports = new String[strListReports.size()];
				strArrReports = strListReports.toArray(strArrReports);
				sqlArr = conn.createArrayOf("VARCHAR", strArrReports);
				stm.setArray(8, sqlArr);
			}	
			
			if (report.getFetchedUrls() == null) {
				String[] emptyStrArr = new String[0];
				sqlArr = conn.createArrayOf("VARCHAR", emptyStrArr);
				stm.setArray(9, sqlArr);
			} else {
				List<String> strListUrls = new ArrayList<String>(report.getFetchedUrls());
				String[] strArrUrls = new String[strListUrls.size()];
				strArrUrls = strListUrls.toArray(strArrUrls);
				sqlArr = conn.createArrayOf("VARCHAR", strArrUrls);
				stm.setArray(9, sqlArr);
			}
			// analysis_state
			AnalysisStatus as = report.getAnalysisState();
			if (as == null) {
				as = AnalysisStatus.UNKNOWN_STATUS;
			}
			stm.setInt(10, as.ordinal());
			// analysis_state_reason
			stm.setString(11, report.getAnalysisStateReason());
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

	public static final String SELECT_HARVEST_BY_NAME_SQL = "SELECT * FROM harvests WHERE harvestname=?";

	public static final String SELECT_HARVEST_COUNT_SQL = "SELECT COUNT(*) FROM harvests";
	
	/**
	 * @param harvestName a given harvestname
	 * @return null, if none found with given harvestname
	 */
	@Override
	public SingleSeedHarvest getHarvest(String harvestName) throws Exception {
		SingleSeedHarvest report = null;
		PreparedStatement stm = null;
		ResultSet rs = null;
		try {
			Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
			stm = conn.prepareStatement(SELECT_HARVEST_BY_NAME_SQL);
			stm.clearParameters();
			stm.setString(1, harvestName);
			rs = stm.executeQuery();
			report = getHarvestFromResultSet(rs);
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (stm != null) {
				stm.close();
			}
		}
		return report; 
	}

	public static final String SELECT_ALL_SQL = "SELECT * FROM harvests";

	@Override
	public List<SingleSeedHarvest> getAll() throws Exception {
		List<SingleSeedHarvest> harvestsFound = new ArrayList<SingleSeedHarvest>();
		PreparedStatement stm = null;
		ResultSet rs = null;
		try {
			Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
			stm = conn.prepareStatement(SELECT_ALL_SQL);
			stm.clearParameters();
			rs = stm.executeQuery();
			getHarvestsFromResultSet(rs, harvestsFound);
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (stm != null) {
				stm.close();
			}
		}
		return harvestsFound; 
	}

 	public static final String GET_ALL_WITH_SEEDURL_SQL = "SELECT * FROM harvests WHERE seedurl=?";

 	public static final String GET_ALL_NAMES_LIMIT_SQL = "SELECT harvestname FROM harvests LIMIT ?";
 	
	@Override
	public List<SingleSeedHarvest> getAllWithSeedurl(String seedurl) throws Exception {
		List<SingleSeedHarvest> harvestsFound = new ArrayList<SingleSeedHarvest>();
		PreparedStatement stm = null;
		ResultSet rs = null;
		try {
			Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
			stm = conn.prepareStatement(GET_ALL_WITH_SEEDURL_SQL);
			stm.clearParameters();
			stm.setString(1, seedurl);
			rs = stm.executeQuery();
			getHarvestsFromResultSet(rs, harvestsFound);
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (stm != null) {
				stm.close();
			}
		}
		return harvestsFound; 
	}

 	public static final String GET_ALL_WITH_SUCCESSFUL_SQL = "SELECT * FROM harvests WHERE successful=?";

	@Override
	public List<SingleSeedHarvest> getAllWithSuccessfulstate(boolean successful) throws Exception {
		List<SingleSeedHarvest> harvestsFound = new ArrayList<SingleSeedHarvest>();
		PreparedStatement stm = null;
		ResultSet rs = null;
		try {
			Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
			stm = conn.prepareStatement(GET_ALL_WITH_SUCCESSFUL_SQL);
			stm.clearParameters();
			stm.setBoolean(1, successful);
			rs = stm.executeQuery();
			getHarvestsFromResultSet(rs, harvestsFound);
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (stm != null) {
				stm.close();
			}
		}
		return harvestsFound; 
	}

	@Override
	public void close() {
	}

    @Override
    public Long getCount() throws Exception {
        PreparedStatement stm = null;
        ResultSet rs = null;
        long res = 0;
        try {
            Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
            stm = conn.prepareStatement(SELECT_HARVEST_COUNT_SQL);
            stm.clearParameters();
            rs = stm.executeQuery();
            if (rs != null && rs.next()) {
                res = rs.getLong(1);
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (stm != null) {
                stm.close();
            }
        }
        return res;
    }

    @Override
    public List<String> getAllNames() throws Exception { // Limit currently hardwired to 100K
        List<String> harvests = new ArrayList<String>();
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
            stm = conn.prepareStatement(GET_ALL_NAMES_LIMIT_SQL);
            stm.clearParameters();
            stm.setLong(1, LIMIT);
            rs = stm.executeQuery();
            getHarvestNamesFromResultSet(rs, harvests);
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (stm != null) {
                stm.close();
            }
        }
        return harvests; 
    }

    private void getHarvestNamesFromResultSet(ResultSet rs, List<String> harvests) throws Exception {
        String name;
        if (rs != null) {
            while (rs.next()) { 
                name = rs.getString("harvestname");
                harvests.add(name);
            }
        }
    }

}
