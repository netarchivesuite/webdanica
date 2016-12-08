package dk.kb.webdanica.core.datamodel.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

import dk.kb.webdanica.core.datamodel.DanicaStatus;
import dk.kb.webdanica.core.datamodel.Seed;
import dk.kb.webdanica.core.datamodel.Status;

public class HBasePhoenixSeedsDAO implements SeedsDAO {

	private static final String UPSERT_SQL;

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
		    exported boolean,
		    exported_time TIMESTAMP,
		    danica_reason VARCHAR
		    
	*/
	static {
		UPSERT_SQL = ""
				+ "UPSERT INTO seeds (url, redirected_url, host, domain, tld, inserted_time, updated_time, danica, status, status_reason, exported, exported_time, danica_reason) "
				+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?) ";
		
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
	    return upsertSeed(singleSeed, true);
		
	}
	
	private boolean upsertSeed(Seed singleSeed, boolean isInsert) throws Exception {
		PreparedStatement stm = null;
		int res = 0;
		Long now = System.currentTimeMillis();
		Long updatedTime = now;
		Long insertedTime = null;
		Long exportedTime = null;
		if (singleSeed.getExportedState() == true && singleSeed.getExportedTime() == null) {
			exportedTime = now;
		}
		Timestamp exportedTimeAsTimestamp = null;
		if (exportedTime != null) {
			exportedTimeAsTimestamp = new Timestamp(exportedTime);
		}
		if (isInsert) {
			insertedTime = now;
		} else {
			insertedTime = singleSeed.getInsertedTime();
		}
		
		try {
			Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
			stm = conn.prepareStatement(UPSERT_SQL);
			stm.clearParameters();
			stm.setString(1, singleSeed.getUrl());
			stm.setString(2, singleSeed.getRedirectedUrl());
			stm.setString(3, singleSeed.getHostname());
			stm.setString(4, singleSeed.getDomain());
			stm.setString(5, singleSeed.getTld());
			stm.setTimestamp(6, new Timestamp(insertedTime));
			stm.setTimestamp(7, new Timestamp(updatedTime));
			stm.setInt(8, singleSeed.getDanicaStatus().ordinal());
			stm.setInt(9, singleSeed.getStatus().ordinal());
			stm.setString(10, singleSeed.getStatusReason());
			stm.setBoolean(11, singleSeed.getExportedState());
			stm.setTimestamp(12, exportedTimeAsTimestamp);
			stm.setString(13, singleSeed.getDanicaStatusReason());
			res = stm.executeUpdate();
			conn.commit();
		} finally {
			if (stm != null) {
				stm.close();
			}
		}
		return res != 0;
	}
	
	
	
	@Override
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

	
	@Override
	public boolean updateSeed(Seed singleSeed) throws Exception {
		return upsertSeed(singleSeed, false);
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
				+ "WHERE status=? LIMIT ?";
	}

	@Override
	public List<Seed> getSeeds(Status status, int limit) throws Exception {
		List<Seed> seedList = new LinkedList<Seed>();
		PreparedStatement stm = null;
		ResultSet rs = null;
		try {
			Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
			stm = conn.prepareStatement(SEEDS_BY_STATUS_SQL);
			stm.clearParameters();
			stm.setInt(1, status.ordinal());
			stm.setInt(2, limit);
			rs = stm.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					seedList.add(getSeedFromResultSet(rs));
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

	private Seed getSeedFromResultSet(ResultSet rs) throws Exception {
		Timestamp t = rs.getTimestamp("exported_time");
		Long exportedTime = null;
		if (t != null) {
			exportedTime = t.getTime();
		}
		return new Seed(
				rs.getString("url"),
				rs.getString("redirected_url"), 
				rs.getString("host"),
				rs.getString("domain"),
				rs.getString("tld"),
				rs.getTimestamp("inserted_time").getTime(),
				rs.getTimestamp("updated_time").getTime(),
				DanicaStatus.fromOrdinal(rs.getInt("danica")),
				Status.fromOrdinal(rs.getInt("status")), 
				rs.getString("status_reason"),
				rs.getBoolean("exported"),
				exportedTime, 
				rs.getString("danica_reason")
				);
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

	private static final String SEEDS_READY_TO_EXPORT_SQL;
	private static final String SEEDS_DANICA_SQL;
	static {
		SEEDS_READY_TO_EXPORT_SQL = "SELECT * "
				+ "FROM seeds "
				+ "WHERE status=? and danica=? and exported=?";
		SEEDS_DANICA_SQL = "SELECT * "
				+ "FROM seeds "
				+ "WHERE status=? and danica=?";
	}
	
	@Override
    public List<Seed> getSeedsReadyToExport(boolean includeAlreadyExported) throws Exception {
		//DanicaStatus==YES && exported==false && status==DONE  // Seed kan også have DanicaStatus=YES, men have Status REJECTED, hvis domænet allerede er danica
		List<Seed> seedList = new LinkedList<Seed>();
		PreparedStatement stm = null;
		ResultSet rs = null;
		DanicaStatus yes = DanicaStatus.YES;
		Status done = Status.DONE;
		boolean exportedValue = false; // Don't export seeds more than once
		try {
			Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
			if (!includeAlreadyExported) {
				stm = conn.prepareStatement(SEEDS_READY_TO_EXPORT_SQL);
				stm.clearParameters();
				stm.setInt(1, done.ordinal());
				stm.setInt(2, yes.ordinal());
				stm.setBoolean(3, exportedValue);
			} else {
				stm = conn.prepareStatement(SEEDS_DANICA_SQL);
				stm.clearParameters();
				stm.setInt(1, done.ordinal());
				stm.setInt(2, yes.ordinal());
			}
			rs = stm.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					seedList.add(getSeedFromResultSet(rs));
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

	
	private static final String SEED_SELECT_SQL;

	static {
		SEED_SELECT_SQL = "SELECT * "
				+ "FROM seeds "
				+ "WHERE url=?";
	}

	
	@Override
    public Seed getSeed(String url) throws Exception {
		if (!existsUrl(url)) {
	        return null;
	    }
		PreparedStatement stm = null;
		ResultSet rs = null;
		Seed result = null;
		try {
			Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
			stm = conn.prepareStatement(SEED_SELECT_SQL);
			stm.clearParameters();
			stm.setString(1, url);
			rs = stm.executeQuery();
			if (rs != null && rs.next()) {
				result = getSeedFromResultSet(rs);
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (stm != null) {
				stm.close();
			}
		}				
	    return result;
    }
}
