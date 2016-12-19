package dk.kb.webdanica.core.datamodel.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import dk.kb.webdanica.core.datamodel.DanicaStatus;
import dk.kb.webdanica.core.datamodel.Domain;
import dk.kb.webdanica.core.utils.DatabaseUtils;

public class HBasePhoenixDomainsDAO implements DomainsDAO {
/*
	CREATE TABLE domains (
		    domain VARCHAR PRIMARY KEY,
		    notes VARCHAR,
		    danicastatus INTEGER,
		    updated_time TIMESTAMP,
		    danicastatus_reason VARCHAR(256),
		    tld VARCHAR(64),
		    danica_parts VARCHAR[]
		);
*/
	
	private static final String INSERT_SQL;

	private static final String EXISTS_SQL;
	
	static {
		INSERT_SQL = ""
				+ "UPSERT INTO domains (domain, notes, danicastatus, danicastatus_reason, updated_time, tld) "
				+ "VALUES (?,?,?,?,?,?)";
		EXISTS_SQL = ""
		        + "SELECT count(*) "
                + "FROM domains "
                + "WHERE domain=? ";	
	}

	@Override
	public boolean insertDomain(Domain domain) throws Exception {
	    if (existsDomain(domain.getDomain())) {
	        return false;
	    }
		PreparedStatement stm = null;
		int res = 0;
		try {
			Long updatedTime = System.currentTimeMillis(); 
			if (domain.getUpdatedTime() != null) {
				updatedTime = domain.getUpdatedTime();
			}
			Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
			stm = conn.prepareStatement(INSERT_SQL);
			stm.clearParameters();
			stm.setString(1, domain.getDomain());
			stm.setString(2, domain.getNotes());
			stm.setInt(3, domain.getDanicaStatus().ordinal());
			stm.setString(4, domain.getDanicaStatusReason());
			stm.setTimestamp(5, new Timestamp(updatedTime));
			stm.setString(6, domain.getTld());
			
			
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
	public boolean existsDomain(String domain) throws Exception {
	    PreparedStatement stm = null;
        ResultSet rs = null;
        long res = 0;
        try {
            Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
            stm = conn.prepareStatement(EXISTS_SQL);
            stm.clearParameters();
            stm.setString(1, domain);
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
	
	
	private static final String DOMAINS_COUNT_BY_STATUS_SQL;
	private static final String DOMAINS_COUNT_BY_TLD_SQL;
	private static final String DOMAINS_COUNT_BY_TLD_AND_STATUS_SQL;
	private static final String  DOMAINS_COUNT_ALL_SQL;
	static {
		DOMAINS_COUNT_BY_STATUS_SQL = ""
				+ "SELECT count(*) "
				+ "FROM domains "
				+ "WHERE danicastatus=? ";
		DOMAINS_COUNT_BY_TLD_SQL = ""
				+ "SELECT count(*) "
				+ "FROM domains "
				+ "WHERE tld=? ";
		DOMAINS_COUNT_BY_TLD_AND_STATUS_SQL = ""
				+ "SELECT count(*) "
				+ "FROM domains "
				+ "WHERE danicastatus=? AND tld=? ";
		
		DOMAINS_COUNT_ALL_SQL = ""
                + "SELECT count(*) "
                + "FROM domains ";
	}

	@Override
	public Long getDomainsCount(DanicaStatus status, String tld) throws Exception {
		
		PreparedStatement stm = null;
		ResultSet rs = null;
		long res = 0;
		try {
			Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
			if (status != null && tld != null) {
			    stm = conn.prepareStatement(DOMAINS_COUNT_BY_TLD_AND_STATUS_SQL);
			    stm.clearParameters();
			    stm.setInt(1, status.ordinal());
			    stm.setString(2, tld);
			} else if (tld != null) { // ie. status==null
			    stm = conn.prepareStatement(DOMAINS_COUNT_BY_TLD_SQL);
                stm.clearParameters();
                stm.setString(2, tld);
			} else if (status != null) {  // ie. tld==null
				stm = conn.prepareStatement(DOMAINS_COUNT_BY_STATUS_SQL);
				stm.clearParameters();
                stm.setInt(2, status.ordinal());
			} else { // tld == null && status == null
				stm = conn.prepareStatement(DOMAINS_COUNT_ALL_SQL);
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

	private static final String DOMAIN_BY_STATUS_SQL;
	private static final String DOMAIN_BY_TLD_SQL;
	private static final String DOMAIN_BY_STATUS_AND_TLD_SQL;
	private static final String SELECT_DOMAINS_SQL;
	
	static {
		DOMAIN_BY_STATUS_SQL = "SELECT * "
				+ "FROM domains "
				+ "WHERE danicastatus=? LIMIT ?";
		DOMAIN_BY_TLD_SQL = "SELECT * "
				+ "FROM domains "
				+ "WHERE tld=? LIMIT ?";
		DOMAIN_BY_STATUS_AND_TLD_SQL = "SELECT * "
				+ "FROM domains "
				+ "WHERE danicastatus=? AND tld=? LIMIT ?";
		SELECT_DOMAINS_SQL = "SELECT * "
				+ "FROM domains LIMIT ?";
	}

	@Override
	public List<Domain> getDomains(DanicaStatus status, String tld, int limit) throws Exception {
		List<Domain> seedList = new LinkedList<Domain>();
		Domain domain;
		PreparedStatement stm = null;
		ResultSet rs = null;
		boolean selectOnStatus = false;
		boolean selectOnTld = false;
		if (status != null) {
			selectOnStatus = true;
		}
		if (tld != null) {
			selectOnTld = true;
		}
		
		try {
			Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
			if (selectOnStatus && selectOnTld) {
				stm = conn.prepareStatement(DOMAIN_BY_STATUS_AND_TLD_SQL);
				stm.clearParameters();
				stm.setInt(1, status.ordinal());
				stm.setString(2, tld);
				stm.setInt(3, limit);
			} else if (selectOnStatus) {
				stm = conn.prepareStatement(DOMAIN_BY_STATUS_SQL);
				stm.clearParameters();
				stm.setInt(1, status.ordinal());
				stm.setInt(2, limit);
			} else if (selectOnTld){
				stm = conn.prepareStatement(DOMAIN_BY_TLD_SQL);
				stm.clearParameters();
				stm.setString(1, tld);
				stm.setInt(2, limit);
			} else { // select all within limits
				stm = conn.prepareStatement(SELECT_DOMAINS_SQL);
				stm.clearParameters();
				stm.setInt(1, limit);
			}
			rs = stm.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					domain = getDomain(rs);
					seedList.add(domain);
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

	private Domain getDomain(ResultSet rs) throws SQLException {
		return new Domain(
				rs.getString("domain"),
				rs.getString("notes"),
				DanicaStatus.fromOrdinal(rs.getInt("danicastatus")),
				rs.getTimestamp("updated_time").getTime(),
				rs.getString("danicastatus_reason"),
				rs.getString("tld"),
				DatabaseUtils.sqlArrayToArrayList(rs.getArray("danica_parts"))
				);
	}

	@Override
	public void close() {
	}
	
	private static final String SINGLE_DOMAIN_SELECT_SQL;
	static {
		SINGLE_DOMAIN_SELECT_SQL = "SELECT * "
				+ "FROM domains "
				+ "WHERE domain=? ";
	}

	@Override
	public Domain getDomain(String domainName) throws Exception {
		Domain domain = null;
		PreparedStatement stm = null;
		ResultSet rs = null;
		try {
			Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
			stm = conn.prepareStatement(SINGLE_DOMAIN_SELECT_SQL);
			stm.clearParameters();
			stm.setString(1, domainName);
			rs = stm.executeQuery();
			if (rs != null && rs.next()) {
				domain = getDomain(rs);
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (stm != null) {
				stm.close();
			}
		}
		return domain;
	}
	
	
	@Override
    public boolean update(Domain domain) throws Exception {
		if (!existsDomain(domain.getDomain())) {
			//TODO log
	        return false;
	    }
		int res = 0;
		PreparedStatement stm = null;
		try {
			Long updatedTime = System.currentTimeMillis(); 
			
			Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
			stm = conn.prepareStatement(INSERT_SQL);
			stm.clearParameters();
			stm.setString(1, domain.getDomain());
			stm.setString(2, domain.getNotes());
			stm.setInt(3, domain.getDanicaStatus().ordinal());
			stm.setString(4, domain.getDanicaStatusReason());
			stm.setTimestamp(5, new Timestamp(updatedTime));
			stm.setString(6, domain.getTld());			
			res = stm.executeUpdate();
			conn.commit();
		} finally {
			if (stm != null) {
				stm.close();
			}
		}
		return res != 0;
	}
	
	
	private static final String DISTINCT_TLD_SQL;
	static {
		DISTINCT_TLD_SQL = "SELECT DISTINCT(tld) FROM domains "; 
	}
	
	@Override
	public Set<String> getTlds() throws Exception {
		Set<String> tldList = new TreeSet<String>();
		PreparedStatement stm = null;
		ResultSet rs = null;			
		try {
			Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
			stm = conn.prepareStatement(DISTINCT_TLD_SQL);
			stm.clearParameters();
			rs = stm.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					tldList.add(rs.getString(1));
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
		return tldList; 
	}
}
