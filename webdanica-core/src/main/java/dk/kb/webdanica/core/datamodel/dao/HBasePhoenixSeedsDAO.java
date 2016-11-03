package dk.kb.webdanica.core.datamodel.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import dk.kb.webdanica.core.datamodel.DanicaStatus;
import dk.kb.webdanica.core.datamodel.Seed;
import dk.kb.webdanica.core.datamodel.SeedsDAO;
import dk.kb.webdanica.core.datamodel.Status;

public class HBasePhoenixSeedsDAO implements SeedsDAO {

	private static final String INSERT_SQL;

	private static final String EXISTS_SQL;
	
	static {
		INSERT_SQL = ""
				+ "UPSERT INTO seeds (url, status, inserted_time) "
				+ "VALUES (?,?,?) ";
		EXISTS_SQL = ""
		        + "SELECT count(*) "
                + "FROM seeds "
                + "WHERE url=? ";	
	}

	@Override
	public boolean insertSeed(Seed singleSeed) throws Exception {
	    if (existsUrl(singleSeed.getUrl())) {
	        return false;
	    }
		PreparedStatement stm = null;
		int res = 0;
		try {
			Date insertedDate = new Date();
			Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
			stm = conn.prepareStatement(INSERT_SQL);
			stm.clearParameters();
			stm.setString(1, singleSeed.getUrl());
			stm.setInt(2, singleSeed.getState().ordinal());
			stm.setTimestamp(3, new Timestamp(insertedDate.getTime()));
			res = stm.executeUpdate();
			conn.commit();
		} finally {
			if (stm != null) {
				stm.close();
			}
		}
		return res != 0;
	}

	public boolean existsUrl(String url) throws Exception {
	    PreparedStatement stm = null;
        ResultSet rs = null;
        long res = 0;
        try {
            Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
            stm = conn.prepareStatement(EXISTS_SQL);
            stm.clearParameters();
            stm.setString(1, url);
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
        return res != 0L;
	}
	
	
	private static final String UPDATE_STATUS_SQL;

	static {
		UPDATE_STATUS_SQL = ""
				+ "UPSERT INTO seeds (url, status, status_reason) "
				+ "VALUES (?, ?, ?)";
	}

	@Override
	public boolean updateState(Seed singleSeed) throws Exception {
		PreparedStatement stm = null;
		int res = 0;
		try {
			Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
			stm = conn.prepareStatement(UPDATE_STATUS_SQL);
			stm.clearParameters();
			stm.setString(1, singleSeed.getUrl());
			stm.setInt(2, singleSeed.getState().ordinal());
			stm.setString(3, singleSeed.getStatusReason());
			res = stm.executeUpdate();
			conn.commit();
		} finally {
			if (stm != null) {
				stm.close();
			}
		}
		return res != 0;
	}

	private static final String UPDATE_REDIRECTED_URL_SQL;

	static {
		UPDATE_REDIRECTED_URL_SQL = "UPSERT INTO seeds (url, redirected_url) "
				+ "VALUES (?, ?) ";
	}

	@Override
	public boolean updateRedirectedUrl(Seed singleSeed) throws Exception {
		PreparedStatement stm = null;
		int res = 0;
		try {
			Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
			stm = conn.prepareStatement(UPDATE_REDIRECTED_URL_SQL);
			stm.clearParameters();
			stm.setString(1, singleSeed.getUrl());
			stm.setString(2, singleSeed.getRedirectedUrl());
			res = stm.executeUpdate();
			conn.commit();
		} finally {
			if (stm != null) {
				stm.close();
			}
		}
		return res != 0;
	}

	private static final String SEEDS_COUNT_BY_STATUS_SQL;
	private static final String SEEDS_COUNT_ALL_SQL;
	static {
		SEEDS_COUNT_BY_STATUS_SQL = ""
				+ "SELECT count(*) "
				+ "FROM seeds "
				+ "WHERE status=? ";
		SEEDS_COUNT_ALL_SQL = ""
                + "SELECT count(*) "
                + "FROM seeds ";
	}

	@Override
	public Long getSeedsCount(Status status) throws Exception {
		PreparedStatement stm = null;
		ResultSet rs = null;
		long res = 0;
		try {
			Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
			if (status != null) {
			    stm = conn.prepareStatement(SEEDS_COUNT_BY_STATUS_SQL);
			    stm.clearParameters();
			    stm.setInt(1, status.ordinal());
			} else {
			    stm = conn.prepareStatement(SEEDS_COUNT_ALL_SQL);
                stm.clearParameters();
			}
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

	private static final String SEEDS_BY_STATUS_SQL;

	static {
		SEEDS_BY_STATUS_SQL = "SELECT * "
				+ "FROM seeds "
				+ "WHERE status=? ";
	}

	@Override
	public List<Seed> getSeeds(Status status, int limit) throws Exception {
		List<Seed> seedList = new LinkedList<Seed>();
		Seed seed;
		PreparedStatement stm = null;
		ResultSet rs = null;
		try {
			Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
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

	@Override
	public void close() {
	}

}
