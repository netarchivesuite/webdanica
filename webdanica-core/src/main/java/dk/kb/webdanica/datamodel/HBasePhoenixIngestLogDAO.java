package dk.kb.webdanica.datamodel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
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

	private static final String GET_INGEST_DATES_SQL;

	static {
		GET_INGEST_DATES_SQL = ""
				+ "SELECT inserted_date "
				+ "from ingestLog";
	}

	public List<Long> getIngestDates(Connection conn) throws SQLException { // as represented as millis from epoch
		List<Long> ingestDates = new ArrayList<Long>();
		PreparedStatement stm = null;
		ResultSet rs = null;
		try {
			stm = conn.prepareStatement(GET_INGEST_DATES_SQL);
			stm.clearParameters();
			rs = stm.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					ingestDates.add(rs.getLong("inserted_date"));
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
		return ingestDates;
	}

	private static final String GET_INGEST_BY_DATE_SQL;

	static {
		GET_INGEST_BY_DATE_SQL = ""
				+ "SELECT * "
				+ "FROM ingestLog "
				+ "WHERE inserted_date=?";
	}

	public IngestLog readIngestLog(Connection conn, Long timestamp) throws SQLException {
		IngestLog retrievedLog = null;
		PreparedStatement stm = null;
		ResultSet rs = null;
		try {
			stm = conn.prepareStatement(GET_INGEST_BY_DATE_SQL);
			stm.clearParameters();
			stm.setLong(1, timestamp);
			rs = stm.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					retrievedLog = new IngestLog(
							JDBCUtils.sqlArrayToArrayList(rs.getArray("logLines")),
							rs.getString("filename"),
							new Date(rs.getLong("inserted_date")),
							rs.getLong("linecount"),
							rs.getLong("insertedcount"),
							rs.getLong("rejectedcount"),
							rs.getLong("duplicatecount")
					);
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
		return retrievedLog;
	}
	
}
