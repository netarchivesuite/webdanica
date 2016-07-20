package dk.kb.webdanica.datamodel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

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

}
