package dk.kb.webdanica.datamodel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class HBasePhoenixBlackListDAO {

	private static final String INSERT_SQL;

	static {
		INSERT_SQL = ""
				+ "UPSERT INTO blacklists (uid, name, description, blacklist, last_update, is_active) "
				+ "VALUES (?, ?, ?, ?, ?, ?) ";
	}

	public int insertList(Connection conn, BlackList aBlackList) throws SQLException {
		java.sql.Array sqlArr = null;
		PreparedStatement stm = null;
		int res = 0;
		try {
			String uuid = UUID.randomUUID().toString();
			Long updated_time = System.currentTimeMillis();
			List<String> strList = aBlackList.getList();
			String[] strArr = new String[strList.size()];
			strArr = strList.toArray(strArr);
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
			stm.close();
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

}
