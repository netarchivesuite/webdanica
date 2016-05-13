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
 * DAO for logging skipped entries during ingest in a single entry.
  
  CREATE table ingestLog (
   logLines list<text>,
   filename text,
   inserted_date timestamp,
   PRIMARY KEY (inserted_date));  
 * 
 */
public class IngestLogDAO implements Database {
	
	public static void main(String args[]) {
		IngestLogDAO dao = IngestLogDAO.getInstance();
		List<String> entries = new ArrayList<String>();
		entries.add("Line one in sample loglist");
		entries.add("Line two in sample loglist");
		IngestLog log = new IngestLog(entries,"unknown");
		dao.insertLog(log);
		dao.close();
	}

	static IngestLogDAO instance;
	
	private Database db;

	private Session session;
	
	private PreparedStatement preparedInsert;
	
	public synchronized static IngestLogDAO getInstance(){
		if (instance == null) {
			instance = new IngestLogDAO();
		} 
		return instance;
	}
	
	public IngestLogDAO() {
		db = new Cassandra();
	}
	
	public void insertLog(IngestLog log){
		init();
		Date insertedDate = log.getDate();
		if (insertedDate ==  null) {
			insertedDate = new Date();
		}
		BoundStatement bound = preparedInsert.bind(log.getLogEntries(), log.getFilename(), insertedDate);
		ResultSet results = session.execute(bound); 
		// TODO can we check, if the insert was successful?
		// Possible solution: http://stackoverflow.com/questions/21147871/cassandara-java-driver-how-are-insert-update-and-delete-results-reported
		Row row = results.one();
		boolean insertFailed = row.getColumnDefinitions().contains("logLines");
		if (insertFailed){
			System.out.println("Insert failed");
		}
	}
	
	/*
	public List<Date> getIngestDates() {
		init();
		ResultSet results = session.execute("SELECT inserted_date from ingestLog");	
	}*/
	
	
	
	
	/** Initialize session and preparedStatement if necessary */
	private void init() {
		if (session == null || session.isClosed()) {
			session = db.getSession();
		}
		if (preparedInsert == null) {
			preparedInsert = session.prepare("INSERT INTO ingestLog (logLines, filename, inserted_date) VALUES (?,?, ?) IF NOT EXISTS");
		}
	}

	@Override
    public boolean isClosed() {
	    return db.isClosed();
    }

	@Override
    public void close() {
	    if (!db.isClosed()) {
	    	db.close();
	    }
	    
    }

	@Override
    public Session getSession() {
	    return session;
    }

}
