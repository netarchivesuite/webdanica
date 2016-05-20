package dk.kb.webdanica.datamodel;

import java.util.List;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

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
	
	/*
	public List<Seed> getSeeds(Status status) {
		
		db.getSession().execute()
	}*/
	
	
	public boolean insertSeed(Seed singleSeed) {
		init();	
		BoundStatement bound = preparedInsert.bind(singleSeed.getUrl(), singleSeed.getState().ordinal());
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
			preparedInsert = session.prepare("INSERT INTO seeds (url, status, inserted_time) VALUES (?,?) IF NOT EXISTS");
		}
    }

	public void close() {
	    db.close();
    }
	
	
}
