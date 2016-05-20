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
   inserted_date long, //millis since epoch
   PRIMARY KEY (inserted_date));  
 * 
 */
public class IngestLogDAO implements Database {
	
	public static void main(String args[]) {
		IngestLogDAO dao = IngestLogDAO.getInstance();
		List<String> entries = new ArrayList<String>();
		entries.add("Line one in sample loglist");
		entries.add("Line two in sample loglist");
		long linecount=2;
		long rejectedcount=2;
		long insertedcount=0;
		long duplicatecount=2;
		try {
			IngestLog log = new IngestLog(entries,"unknown",  linecount, insertedcount, rejectedcount, duplicatecount) ;
			dao.insertLog(log);
			for (Long date: dao.getIngestDates()) {
				System.out.println(new Date(date));
			}
			
		} finally {
			dao.close();
		}
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
		Long insertedDate = System.currentTimeMillis();
		if (log.getDate() != null) {
			insertedDate = log.getDate().getTime();
		}
		
		BoundStatement bound = preparedInsert.bind(log.getLogEntries(), log.getFilename(), insertedDate, log.getLinecount(), log.getInsertedcount(), log.getRejectedcount(), log.getDuplicatecount());
		ResultSet results = session.execute(bound); 
		// TODO can we check, if the insert was successful?
		// Possible solution: http://stackoverflow.com/questions/21147871/cassandara-java-driver-how-are-insert-update-and-delete-results-reported
		Row row = results.one();
		boolean insertFailed = row.getColumnDefinitions().contains("logLines");
		if (insertFailed){
			System.out.println("Insert failed");
		}
	}
	
	public List<Long> getIngestDates() { // as represented as millis from epoch
		init();
		ResultSet results = session.execute("SELECT inserted_date from ingestLog");
		List<Long> ingestDates = new ArrayList<Long>();
		for (Row row: results.all()) {
				ingestDates.add(row.getLong("inserted_date"));
		}
		return ingestDates;
	}
	
	/** Initialize session and preparedStatement if necessary */
	private void init() {
		if (session == null || session.isClosed()) {
			session = db.getSession();
		}
		if (preparedInsert == null) {
			preparedInsert = session.prepare("INSERT INTO ingestLog (logLines, filename, inserted_date, linecount, insertedcount, rejectedcount, duplicatecount) VALUES (?, ?, ?, ?, ?, ?, ?) IF NOT EXISTS");
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
