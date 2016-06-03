package dk.kb.webdanica.datamodel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

/**
 * DAO class for the seeds table.
 * @author svc
 *
 */
public class SeedDAO {
		
	static SeedDAO instance;
	
	private Database db;

	private Session session;
	private PreparedStatement preparedInsert;
	
	public synchronized static SeedDAO getInstance(){
		if (instance == null) {
			instance = new SeedDAO();
		} 
		return instance;
	}
	
	public SeedDAO() {
		db = new Cassandra();
	}
	
	
	public List<Seed> getSeeds(Status status) {
		init();
		PreparedStatement statement = session.prepare("SELECT * FROM seeds WHERE state=?");
		BoundStatement bStatement = statement.bind(status.ordinal());
		ResultSet results = session.execute(bStatement);
		List<Seed> seedList = new ArrayList<Seed>();
		/*
		for (Row row: results.all()) {
			Seed s = new Seed()
		}*/
		return seedList; 
	}	
	
	public IngestLog readIngestLog(Long timestamp) {
		PreparedStatement statement = session.prepare("SELECT * FROM ingestLog WHERE inserted_date=?");
		BoundStatement bStatement = statement.bind(timestamp);
		ResultSet results = session.execute(bStatement);
		Row singleRow = results.one();
		IngestLog retrievedLog = new IngestLog(singleRow.getList("logLines", String.class), 
				singleRow.getString("filename"), new Date(singleRow.getLong("inserted_date")), 
				singleRow.getLong("linecount"), 
				singleRow.getLong("insertedcount"),
				singleRow.getLong("rejectedcount"),
				singleRow.getLong("duplicatecount"));
		return retrievedLog;
	}
	
	public boolean insertSeed(Seed singleSeed) {
		init();	
		Date insertedDate = new Date();
		BoundStatement bound = preparedInsert.bind(singleSeed.getUrl(), singleSeed.getState().ordinal(), insertedDate);
		ResultSet rs = session.execute(bound);
		Row row = rs.one();
		boolean insertFailed = row.getColumnDefinitions().contains("url");
		return !insertFailed; // Was the insert successful?
	}

	private void init() {
		if (session == null || session.isClosed()) { 
			session = db.getSession();
		}
		if (preparedInsert == null) {
			preparedInsert = session.prepare("INSERT INTO seeds (url, status, inserted_time) VALUES (?,?,?) IF NOT EXISTS");
		}
    }
	
	
	

	public void close() {
	    db.close();
    }
}
