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
import dk.kb.webdanica.core.datamodel.Status;

public class HBasePhoenixSeedsDAO implements SeedsDAO {

	private static final String INSERT_SQL;

	private static final String EXISTS_SQL;
	
	/*
			url VARCHAR PRIMARY KEY,
		    redirected_url VARCHAR,
		    host VARCHAR(256),
		    domain VARCHAR(256),
		    tld VARCHAR(64) // Top level domain for this seed
		    inserted_time TIMESTAMP,
		    updated_time TIMESTAMP,
   		    danica INTEGER, // see dk.kb.webdanica.datamodel.DanicaStatus enum class
		    status INTEGER, // see dk.kb.webdanica.datamodel.Status enum class
		    status_reason VARCHAR, // textual explanation behind its state
	*/
	static {
		INSERT_SQL = ""
				+ "UPSERT INTO seeds (url, redirected_url, host, domain, tld, inserted_time, updated_time, danica, status, status_reason) "
				+ "VALUES (?,?,?,?,?,?,?,?,?,?) ";
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
			stm.setString(2, singleSeed.getRedirectedUrl());
			stm.setString(3, singleSeed.getHostname());
			stm.setString(4, singleSeed.getDomain());
			stm.setString(5, singleSeed.getTld());
			stm.setTimestamp(6, new Timestamp(insertedDate.getTime()));
			stm.setTimestamp(7, new Timestamp(insertedDate.getTime()));
			stm.setInt(8, singleSeed.getDanicaStatus().ordinal());
			stm.setInt(9, singleSeed.getStatus().ordinal());
			stm.setString(10, singleSeed.getStatusReason());
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
				+ "UPSERT INTO seeds (url, status, status_reason, updated_time) "
				+ "VALUES (?, ?, ?, ?)";
	}

	@Override
	public boolean updateState(Seed singleSeed) throws Exception {
		PreparedStatement stm = null;
		int res = 0;
		try {
			Date newDate = new Date();
			Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
			stm = conn.prepareStatement(UPDATE_STATUS_SQL);
			stm.clearParameters();
			stm.setString(1, singleSeed.getUrl());
			stm.setInt(2, singleSeed.getStatus().ordinal());
			stm.setString(3, singleSeed.getStatusReason());
			stm.setTimestamp(4, new Timestamp(newDate.getTime()));
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
		UPDATE_REDIRECTED_URL_SQL = "UPSERT INTO seeds (url, redirected_url, updated_time) "
				+ "VALUES (?, ?, ?) ";
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
			stm.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
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
							rs.getString("host"),
							rs.getString("domain"),
							rs.getString("tld"),
							rs.getTimestamp("inserted_time").getTime(),
							rs.getTimestamp("updated_time").getTime(),
							DanicaStatus.fromOrdinal(rs.getInt("danica")),
							Status.fromOrdinal(rs.getInt("status")), 
							rs.getString("status_reason")
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

	private static final String SELECT_COUNT_DANICA_SQL;

	static {
		SELECT_COUNT_DANICA_SQL = ""
				+ "SELECT COUNT(*) FROM seeds WHERE danica=?";
	}

	@Override
    public Long getSeedsDanicaCount(DanicaStatus s) throws Exception {
		if (s == null)  {
			return 0L;
		}
		PreparedStatement stm = null;
		ResultSet rs = null;
		long res = 0;
		try {
			Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
			stm = conn.prepareStatement(SELECT_COUNT_DANICA_SQL);
			stm.clearParameters();
			stm.setInt(1, s.ordinal());
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

}
