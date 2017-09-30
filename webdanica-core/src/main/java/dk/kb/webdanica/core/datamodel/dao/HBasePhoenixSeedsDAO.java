package dk.kb.webdanica.core.datamodel.dao;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import dk.kb.webdanica.core.datamodel.DanicaStatus;
import dk.kb.webdanica.core.datamodel.Seed;
import dk.kb.webdanica.core.datamodel.Status;
import dk.kb.webdanica.core.utils.CloseUtils;

public class HBasePhoenixSeedsDAO implements SeedsDAO {

    private static final String UPSERT_SQL;

    private static final String EXISTS_SQL;
    
    private static final Logger logger = Logger.getLogger(HBasePhoenixSeedsDAO.class.getName());

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
    public boolean insertSeed(Seed singleSeed) throws DaoException {
        if (existsUrl(singleSeed.getUrl())) {
            return false;
        }
        return upsertSeed(singleSeed, true);

    }

    private boolean upsertSeed(Seed singleSeed, boolean isInsert) throws DaoException {
        int res = 0;
        Long now = System.currentTimeMillis();
        Long updatedTime = now;
        Long insertedTime;
        Long exportedTime = null;
        if (singleSeed.getExportedState() && singleSeed.getExportedTime() == null) {
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
            if (insertedTime==null) {
                logger.warning("InsertedTime shouldn't be null for updates, but was for seed w/url '"  + singleSeed.getUrl() 
                        + "'. Setting insertedTime for current time");
                insertedTime=now;
            }
        }

        try {
            Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
            try (PreparedStatement stm = conn.prepareStatement(UPSERT_SQL);) {
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
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        }
        return res != 0;
    }


    @Override
    public boolean existsUrl(String url) throws DaoException {
        long res = 0;
        try {
            Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
            try (PreparedStatement stm = conn.prepareStatement(EXISTS_SQL);) {
                stm.clearParameters();
                stm.setString(1, url);
                try (ResultSet rs = stm.executeQuery();) {
                    if (rs != null && rs.next()) {
                        res = rs.getLong(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        }
        return res != 0L;
    }


    @Override
    public boolean updateSeed(Seed singleSeed) throws DaoException {
        return upsertSeed(singleSeed, false);
    }


    private static final String SEEDS_COUNT_BY_STATUS_SQL;
    private static final String SEEDS_COUNT_ALL_SQL;
    private static final String SEEDS_COUNT_BY_DOMAIN_SQL;

    static {
        SEEDS_COUNT_BY_STATUS_SQL = ""
                + "SELECT count(*) "
                + "FROM seeds "
                + "WHERE status=? ";
        SEEDS_COUNT_ALL_SQL = ""
                + "SELECT count(*) "
                + "FROM seeds ";
        SEEDS_COUNT_BY_DOMAIN_SQL = ""
                + "SELECT count(*) "
                + "FROM seeds "
                + "WHERE domain=? ";
    }

	@Override
	public Long getSeedsCount(Status status) throws DaoException {
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
		} catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            CloseUtils.closeQuietly(stm);
            CloseUtils.closeQuietly(rs);
		}
		return res;
	}	

    private static final String SEEDS_BY_STATUS_SQL;
    private static final String SEEDS_BY_DOMAIN_SQL;
    private static final String SEEDS_BY_DOMAIN_AND_STATE_SQL;

    static {
        SEEDS_BY_STATUS_SQL = "SELECT * "
                + "FROM seeds "
                + "WHERE status=? LIMIT ?";
        SEEDS_BY_DOMAIN_SQL = "SELECT * "
                + "FROM seeds "
                + "WHERE domain=? LIMIT ?";
        SEEDS_BY_DOMAIN_AND_STATE_SQL = "SELECT * "
                + "FROM seeds "
                + "WHERE domain=? AND status=? LIMIT ?";
    }

    @Override
    public List<Seed> getSeeds(Status status, int limit) throws DaoException {
        List<Seed> seedList = new LinkedList<Seed>();
        try {
            Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
            try (PreparedStatement stm = conn.prepareStatement(SEEDS_BY_STATUS_SQL);) {
                stm.clearParameters();
                stm.setInt(1, status.ordinal());
                stm.setInt(2, limit);
                try (ResultSet rs = stm.executeQuery();) {
                    if (rs != null) {
                        while (rs.next()) {
                            seedList.add(getSeedFromResultSet(rs));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        }
        return seedList;
    }
    
    @Override
    public List<Seed> getSeeds(Status status, String domain, int limit) throws DaoException {
        List<Seed> seedList = new LinkedList<Seed>();
        try {
            Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
            try (PreparedStatement stm = conn.prepareStatement(SEEDS_BY_DOMAIN_AND_STATE_SQL);) {
                stm.clearParameters();
                stm.setString(1, domain);
                stm.setInt(2, status.ordinal());
                stm.setInt(3, limit);
                try (ResultSet rs = stm.executeQuery();) {
                    if (rs != null) {
                        while (rs.next()) {
                            seedList.add(getSeedFromResultSet(rs));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        }
        return seedList;
    }
    
    @Override
    public List<Seed> getSeeds(String domain, int limit) throws DaoException {
        List<Seed> seedList = new LinkedList<Seed>();
        try {
            Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
            try (PreparedStatement stm = conn.prepareStatement(SEEDS_BY_DOMAIN_SQL);) {
                stm.clearParameters();
                stm.setString(1, domain);
                stm.setInt(2, limit);
                try (ResultSet rs = stm.executeQuery();) {
                    if (rs != null) {
                        while (rs.next()) {
                            seedList.add(getSeedFromResultSet(rs));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        }
        return seedList;
    }
    
    

    private Seed getSeedFromResultSet(ResultSet rs) throws SQLException {
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
    public Long getSeedsDanicaCount(DanicaStatus s) throws DaoException {
        if (s == null) {
            return 0L;
        }
        long res = 0;
        try {
            Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
            try (PreparedStatement stm = conn.prepareStatement(SELECT_COUNT_DANICA_SQL);) {
                stm.clearParameters();
                stm.setInt(1, s.ordinal());
                try (ResultSet rs = stm.executeQuery();) {
                    if (rs != null && rs.next()) {
                        res = rs.getLong(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DaoException(e);
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
    public List<Seed> getSeedsReadyToExport(boolean includeAlreadyExported) throws DaoException {
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
		} catch (SQLException e) {
            throw new DaoException(e);
        } finally {
		    CloseUtils.closeQuietly(rs);
		    CloseUtils.closeQuietly(stm);
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
    public Seed getSeed(String url) throws DaoException {
        if (!existsUrl(url)) {
            return null;
        }
        Seed result = null;
        try {
            Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
            try (PreparedStatement stm = conn.prepareStatement(SEED_SELECT_SQL);) {
                stm.clearParameters();
                stm.setString(1, url);
                try (ResultSet rs = stm.executeQuery();) {
                    if (rs != null && rs.next()) {
                        result = getSeedFromResultSet(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        }
        return result;
    }

    @Override
    public Long getDomainSeedsCount(String domain) throws DaoException {
        if (domain == null || domain.isEmpty()) {
            return 0L;
        }
        long res = 0;
        try {
            Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
            try (PreparedStatement stm = conn.prepareStatement(SEEDS_COUNT_BY_DOMAIN_SQL);) {
                stm.clearParameters();
                stm.setString(1, domain);
                try (ResultSet rs = stm.executeQuery();) {
                    if (rs != null && rs.next()) {
                        res = rs.getLong(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        }

        return res;
    }
}
