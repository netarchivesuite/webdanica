package dk.kb.webdanica.datamodel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class HBasePhoenixSeedsDAO {

	private static final String INSERT_SQL;

	static {
		INSERT_SQL = ""
				+ "UPSERT INTO seeds (url, status, inserted_time) "
				+ "VALUES (?,?,?) ";
	}

	public int insertSeed(Connection conn, Seed singleSeed) throws SQLException {
		PreparedStatement stm = null;
		int res = 0;
		try {
			Date insertedDate = new Date();
			stm = conn.prepareStatement(INSERT_SQL);
			stm.clearParameters();
			stm.setString(1, singleSeed.getUrl());
			stm.setInt(2, singleSeed.getState().ordinal());
			stm.setTimestamp(3, new Timestamp(insertedDate.getTime()));
			res = stm.executeUpdate();
			stm.close();
			conn.commit();
		} finally {
			if (stm != null) {
				stm.close();
			}
		}
		return res;
	}

	private static final String UPDATE_STATUS_SQL;

	static {
		UPDATE_STATUS_SQL = ""
				+ "UPSERT INTO seeds (url, status, status_reason) "
				+ "VALUES (?, ?, ?)";
	}

	public int updateState(Connection conn, Seed singleSeed) throws SQLException {
		PreparedStatement stm = null;
		int res = 0;
		try {
			stm = conn.prepareStatement(UPDATE_STATUS_SQL);
			stm.clearParameters();
			stm.setString(1, singleSeed.getUrl());
			stm.setInt(2, singleSeed.getState().ordinal());
			stm.setString(3, singleSeed.getStatusReason());
			res = stm.executeUpdate();
			stm.close();
			conn.commit();
		} finally {
			if (stm != null) {
				stm.close();
			}
		}
		return res;
	}

	private static final String UPDATE_REDIRECTED_URL_SQL;

	static {
		UPDATE_REDIRECTED_URL_SQL = "UPSERT INTO seeds (url, redirected_url) "
				+ "VALUES (?, ?) ";
	}

	public int updateRedirectedUrl(Connection conn, Seed singleSeed) throws SQLException {
		PreparedStatement stm = null;
		int res = 0;
		try {
			stm = conn.prepareStatement(UPDATE_REDIRECTED_URL_SQL);
			stm.clearParameters();
			stm.setString(1, singleSeed.getUrl());
			stm.setString(2, singleSeed.getRedirectedUrl());
			res = stm.executeUpdate();
			stm.close();
			conn.commit();
		} finally {
			if (stm != null) {
				stm.close();
			}
		}
		return res;
	}

	private static final String SEEDS_COUNT_SQL;

	static {
		SEEDS_COUNT_SQL = ""
				+ "SELECT count(*) "
				+ "FROM seeds "
				+ "WHERE status=? ";
	}

	public Long getSeedsCount(Connection conn, Status status) throws SQLException {
		PreparedStatement stm = null;
		ResultSet rs = null;
		long res = 0;
		try {
			stm = conn.prepareStatement(SEEDS_COUNT_SQL);
			stm.clearParameters();
			stm.setInt(1, status.ordinal());
			rs = stm.executeQuery();
			if (rs != null && rs.next()) {
				res = rs.getLong(1);
			}
			stm.close();
			conn.commit();
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

	private static final String SEEDS_BY_STATUS_SQL;

	static {
		SEEDS_BY_STATUS_SQL = "SELECT * "
				+ "FROM seeds "
				+ "WHERE status=? ";
	}

	public List<Seed> getSeeds(Connection conn, Status status) throws SQLException {
		List<Seed> seedList = new LinkedList<Seed>();
		Seed seed;
		PreparedStatement stm = null;
		ResultSet rs = null;
		try {
			stm = conn.prepareStatement(SEEDS_BY_STATUS_SQL);
			stm.clearParameters();
			stm.setInt(1, status.ordinal());
			rs = stm.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					seed = new Seed(
							rs.getString("url"),
							rs.getString("redirected_url"), 
							Status.fromOrdinal(rs.getInt("status")),
							rs.getString("status_reason"),
							rs.getString("hostname"),
							rs.getString("tld"),
							DanicaStatus.fromOrdinal(rs.getInt("danica")),
							0, // dummy value
							false // dummy value
					);
					seedList.add(seed);
				}
			}
			stm.close();
			conn.commit();
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

}
