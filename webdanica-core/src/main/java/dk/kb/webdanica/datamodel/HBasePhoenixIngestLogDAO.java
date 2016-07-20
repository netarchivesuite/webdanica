package dk.kb.webdanica.datamodel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class HBasePhoenixIngestLogDAO {

	private static final String INSERT_SQL;

	static {
		INSERT_SQL = ""
				+ "UPSERT INTO ingestLog (logLines, filename, inserted_date, linecount, insertedcount, rejectedcount, duplicatecount) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?) ";
	}

	public int insertLog(Connection conn, IngestLog log) throws SQLException {
		java.sql.Array sqlArr = null;
		PreparedStatement stm = null;
		int res = 0;
		try {
			Long insertedDate = System.currentTimeMillis();
			if (log.getDate() != null) {
				insertedDate = log.getDate().getTime();
			}
			List<String> strList = log.getLogEntries();
			String[] strArr = new String[strList.size()];
			strArr = strList.toArray(strArr);
			sqlArr = conn.createArrayOf("VARCHAR", strArr);
			stm = conn.prepareStatement(INSERT_SQL);
			stm.clearParameters();
			stm.setArray(1, sqlArr);
			stm.setString(2, log.getFilename());
			stm.setLong(3, insertedDate);
			stm.setLong(4, log.getLinecount());
			stm.setLong(5, log.getInsertedcount());
			stm.setLong(6, log.getRejectedcount());
			stm.setLong(7, log.getDuplicatecount());
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
