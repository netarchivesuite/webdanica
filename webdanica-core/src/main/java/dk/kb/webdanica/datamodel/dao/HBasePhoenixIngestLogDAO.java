package dk.kb.webdanica.datamodel.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dk.kb.webdanica.datamodel.IngestLog;
import dk.kb.webdanica.datamodel.IngestLogDAO;
import dk.kb.webdanica.datamodel.JDBCUtils;

public class HBasePhoenixIngestLogDAO implements IngestLogDAO {

	private static final String INSERT_SQL;

	static {
		INSERT_SQL = ""
				+ "UPSERT INTO ingestLog (logLines, filename, inserted_date, linecount, insertedcount, rejectedcount, duplicatecount) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?) ";
	}

	@Override
	public boolean insertLog(IngestLog log) throws Exception {
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
			Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
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

	private static final String GET_INGEST_DATES_SQL;

	static {
		GET_INGEST_DATES_SQL = ""
				+ "SELECT inserted_date "
				+ "from ingestLog";
	}

	@Override
	public List<Long> getIngestDates() throws Exception { // as represented as millis from epoch
		List<Long> ingestDates = new ArrayList<Long>();
		PreparedStatement stm = null;
		ResultSet rs = null;
		try {
			Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
			stm = conn.prepareStatement(GET_INGEST_DATES_SQL);
			stm.clearParameters();
			rs = stm.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					ingestDates.add(rs.getLong("inserted_date"));
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
		return ingestDates;
	}

	private static final String GET_INGEST_BY_DATE_SQL;

	static {
		GET_INGEST_BY_DATE_SQL = ""
				+ "SELECT * "
				+ "FROM ingestLog "
				+ "WHERE inserted_date=?";
	}

	@Override
	public IngestLog readIngestLog(Long timestamp) throws Exception {
		IngestLog retrievedLog = null;
		PreparedStatement stm = null;
		ResultSet rs = null;
		try {
			Connection conn = HBasePhoenixConnectionManager.getThreadLocalConnection();
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

    @Override
    public void close() {
        // TODO Auto-generated method stub
        
    }
	
}
