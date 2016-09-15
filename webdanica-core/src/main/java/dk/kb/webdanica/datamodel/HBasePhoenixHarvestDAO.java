package dk.kb.webdanica.datamodel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import dk.kb.webdanica.interfaces.harvesting.HarvestReport;
import dk.netarkivet.harvester.datamodel.JobStatus;

public class HBasePhoenixHarvestDAO {

	public HarvestReport getHarvestFromResultSet(ResultSet rs) throws SQLException {
		HarvestReport report = null;
		if (rs != null) {
			if (rs.next()) {
				report = new HarvestReport(
						rs.getString("harvestname"),
						rs.getString("seedurl"),
						rs.getBoolean("successful"),
						JDBCUtils.sqlArrayToArrayList(rs.getArray("files")),
						rs.getString("error"),
						JobStatus.fromOrdinal(rs.getInt("finalState")),
						rs.getLong("harvested_time")
				);
			}
		}
		return report; 
	}

	public void getHarvestsFromResultSet(ResultSet rs, List<HarvestReport> harvestsFound) throws SQLException {
		HarvestReport report;
		if (rs != null) {
			while (rs.next()) {
				report = new HarvestReport(
						rs.getString("harvestname"),
						rs.getString("seedurl"),
						rs.getBoolean("successful"),
						JDBCUtils.sqlArrayToArrayList(rs.getArray("files")),
						rs.getString("error"),
						JobStatus.fromOrdinal(rs.getInt("finalState")),
						rs.getLong("harvested_time")
				);
				harvestsFound.add(report);
			}
		}
	}

	public static final String INSERT_SQL;

	static {
		INSERT_SQL = ""
				+ "UPSERT INTO harvests (harvestname, seedurl, finalState, successful, harvested_time, files, error) "
				+ "VALUES (?,?,?,?,?,?,?) ";
	}

	public int insertHarvest(Connection conn, HarvestReport report) throws SQLException {
		java.sql.Array sqlArr = null;
		PreparedStatement stm = null;
		int res = 0;
		try {
			long harvestedTime = report.harvestedTime;
			if (!(harvestedTime > 0)) {
				harvestedTime = System.currentTimeMillis();
				System.err.println("harvestedTime undefined. setting it to  " + harvestedTime);
			}
			List<String> strList = report.getAllFiles();
			String[] strArr = new String[strList.size()];
			strArr = strList.toArray(strArr);
			sqlArr = conn.createArrayOf("VARCHAR", strArr);
			stm = conn.prepareStatement(INSERT_SQL);
			stm.clearParameters();
			stm.setString(1, report.harvestName);
			stm.setString(2, report.seed);
			stm.setInt(3, report.finalState.ordinal());
			stm.setBoolean(4, report.successful);
			stm.setLong(5, harvestedTime);
			stm.setArray(6, sqlArr);
			stm.setString(7, report.error);
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
		return res;
	}

	public static final String SELECT_HARVEST_BY_NAME_SQL = "SELECT * FROM harvests WHERE harvestname=?";

	/**
	 * @param harvestName a given harvestname
	 * @return null, if none found with given harvestname
	 */
	public HarvestReport getHarvest(Connection conn, String harvestName) throws SQLException {
		HarvestReport report = null;
		PreparedStatement stm = null;
		ResultSet rs = null;
		try {
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

	public List<HarvestReport> getAll(Connection conn) throws SQLException {
		List<HarvestReport> harvestsFound = new ArrayList<HarvestReport>();
		PreparedStatement stm = null;
		ResultSet rs = null;
		try {
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

	public List<HarvestReport> getAllWithSeedurl(Connection conn, String seedurl) throws SQLException {
		List<HarvestReport> harvestsFound = new ArrayList<HarvestReport>();
		PreparedStatement stm = null;
		ResultSet rs = null;
		try {
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

	public List<HarvestReport> getAllWithSuccessfulstate(Connection conn, boolean successful) throws SQLException {
		List<HarvestReport> harvestsFound = new ArrayList<HarvestReport>();
		PreparedStatement stm = null;
		ResultSet rs = null;
		try {
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

	//readAllWithFinalStatestatement = session.prepare("SELECT * FROM harvests WHERE finalState=?");

}
