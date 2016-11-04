package dk.kb.webdanica.core.datamodel.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import dk.kb.webdanica.core.datamodel.BlackList;
import dk.kb.webdanica.core.datamodel.JDBCUtils;

public class HBasePhoenixBlackListDAO implements BlackListDAO {

	private static final String INSERT_SQL;

	static {
		INSERT_SQL = ""
				+ "UPSERT INTO blacklists (uid, name, description, blacklist, last_update, is_active) "
				+ "VALUES (?, ?, ?, ?, ?, ?) ";
	}

	@Override
	public boolean insertList(BlackList aBlackList) throws Exception {
		java.sql.Array sqlArr = null;
		PreparedStatement stm = null;
		int res = 0;
		try {
			String uuid = UUID.randomUUID().toString();
			Long updated_time = System.currentTimeMillis();
			List<String> strList = aBlackList.getList();
			String[] strArr = new String[strList.size()];
			strArr = strList.toArray(strArr);
			Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
			sqlArr = conn.createArrayOf("VARCHAR", strArr);
			stm = conn.prepareStatement(INSERT_SQL);
			stm.clearParameters();
			stm.setString(1, uuid);
			stm.setString(2, aBlackList.getName());
			stm.setString(3, aBlackList.getDescription());
			stm.setArray(4, sqlArr);
			stm.setLong(5, updated_time);
			stm.setBoolean(6, aBlackList.isActive());
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

	private static final String GET_BLACKLIST_SQL;

	static {
		GET_BLACKLIST_SQL = "SELECT * FROM blacklists WHERE uid=? ";
	}

	@Override
	public BlackList readBlackList(UUID uid) throws Exception {
		BlackList retrievedBlacklist = null;
		PreparedStatement stm = null;
		ResultSet rs = null;
		try {
			Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
			stm = conn.prepareStatement(GET_BLACKLIST_SQL);
			stm.clearParameters();
			stm.setString(1, uid.toString());
			rs = stm.executeQuery();
			if (rs != null) {
				if (rs.next()) {
					retrievedBlacklist = new BlackList(
							uid,
							rs.getString("name"),
							rs.getString("description"),
							JDBCUtils.sqlArrayToArrayList(rs.getArray("blacklist")),
							rs.getLong("last_update"),
							rs.getBoolean("is_active")
					);
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
		return retrievedBlacklist;
	}

	private static final String GET_ACTIVE_SQL;

	private static final String GET_ALL_SQL;

	static {
		GET_ACTIVE_SQL = "SELECT * "
				+ "FROM blacklists "
				+ "WHERE is_active=true ";

		GET_ALL_SQL = "SELECT * "
				+ "FROM blacklists ";
	}

	@Override
	public List<BlackList> getLists(boolean activeOnly) throws Exception {
		List<BlackList> blacklistList = new ArrayList<BlackList>();
		BlackList blacklist = null;
		PreparedStatement stm = null;
		ResultSet rs = null;
		try {
			Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
			if (activeOnly) {
				stm = conn.prepareStatement(GET_ACTIVE_SQL);
			} else {
				stm = conn.prepareStatement(GET_ALL_SQL);
			}
			stm.clearParameters();
			rs = stm.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					blacklist = new BlackList(
							UUID.fromString(rs.getString("uid")),
							rs.getString("name"),
							rs.getString("description"),
							//JDBCUtils.sqlArrayRecordSetToList(rs.getArray("blacklist"), String.class),
							JDBCUtils.sqlArrayToArrayList(rs.getArray("blacklist")),
							rs.getLong("last_update"),
							rs.getBoolean("is_active")
					);
					blacklistList.add(blacklist);
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
		return blacklistList; 
	}

    @Override
    public void close() {
        // TODO Auto-generated method stub
        
    }	

}
