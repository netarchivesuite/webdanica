package dk.kb.webdanica.datamodel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

/*
CREATE table blacklists (
 uid uuid,
 name text,
 description text,
 blacklist list<text>,   
 last_update bigint, 
 is_active boolean,
 PRIMARY KEY (uid)
 );
 CREATE INDEX on blacklists  (is_active);
*/
public class CassandraBlackListDAO implements BlackListDAO {

	public static void main(String args[]) {
		List<String> list = new ArrayList<String>();
		list.add("an item to avoid");
		list.add("another item to avoid");
		BlackList b = new BlackList("noname", "nodescription",list , false);
		CassandraBlackListDAO dao = CassandraBlackListDAO.getInstance();
		dao.insertList(b);
		List<BlackList> bLists = dao.getLists(false);
		for (BlackList b1: bLists) {
			System.out.println(b1);
		}
		dao.close();
	}
	
	private static CassandraBlackListDAO instance;

	public synchronized static CassandraBlackListDAO getInstance(){
		if (instance == null) {
			instance = new CassandraBlackListDAO();
		} 
		return instance;
	}

	private Cassandra db;

	private Session session;

	private PreparedStatement preparedInsert;

	public CassandraBlackListDAO() {
		db = new Cassandra(CassandraSettings.getDefaultSettings());
	}

	private void init() {
		boolean isNewsession = false;
		if (session == null || session.isClosed()) {
			session = db.getSession();
			isNewsession = true;

		}
		if (preparedInsert == null || isNewsession) {
			preparedInsert = session.prepare(
					"INSERT INTO blacklists (uid, name, description, blacklist, last_update, is_active) "
							+ "VALUES (?, ?, ?, ?, ?, ?) IF NOT EXISTS");
		}
	}
	
	public void insertList(BlackList aBlackList) { // generates a uid value when inserted
		init();
		Long updated_time = System.currentTimeMillis();
		
		BoundStatement bound = preparedInsert.bind(UUID.randomUUID(), aBlackList.getName(), aBlackList.getDescription(), 
				aBlackList.getList(), updated_time, aBlackList.isActive());
		ResultSet results = session.execute(bound); 
		// TODO can we check, if the insert was successful?
		// Possible solution: http://stackoverflow.com/questions/21147871/cassandara-java-driver-how-are-insert-update-and-delete-results-reported
		Row row = results.one();
		boolean insertFailed = row.getColumnDefinitions().contains("blacklist");
		if (insertFailed){
			System.out.println("Insert failed");
		}
	}

	public void deleteList(BlackList aBlackList) {

	}

	public void updateList(BlackList aBlackList) {	
	}

	public List<BlackList> getLists(boolean activeOnly) {
			init();
			String select;
			if (activeOnly) {
				select = "SELECT * FROM blacklists WHERE is_active=true";
			} else {
				select = "SELECT * FROM blacklists";
			}
			ResultSet results = session.execute(select);
			List<BlackList> blacklistList = new ArrayList<BlackList>();
			for (Row row: results.all()) {
				BlackList s = getBlacklistFromRow(row);
				blacklistList.add(s);
			}
			return blacklistList; 
		}	
	
	private BlackList getBlacklistFromRow(Row row) {
		return new BlackList(row.getUUID("uid"),row.getString("name"),row.getString("description"), 
				 row.getList("blacklist", String.class), row.getLong("last_update"), 
				row.getBool("is_active")
				);
    }
	
	public BlackList readBlackList(UUID uid) {
		init();
		PreparedStatement statement = getSession().prepare("SELECT * FROM blacklists WHERE uid=?");
		BoundStatement bStatement = statement.bind(uid);
		ResultSet results = session.execute(bStatement);
		List<Row> allRows = results.all();
		if (allRows.isEmpty() || allRows.size() > 1) {
			return null;
		} else {
			Row singleRow = allRows.get(0);
			BlackList retrievedBlacklist = new BlackList(uid,
				singleRow.getString("name"),singleRow.getString("description"),
				singleRow.getList("blacklist", String.class), singleRow.getLong("last_update"),
				singleRow.getBool("is_active")
				);
			return retrievedBlacklist;
		}
	}


	private Session getSession() {
		return this.session;
	}

	public boolean isClosed() {
		return db.isClosed();
	}

	public void close() {
		if (!db.isClosed()) {
			db.close();
		}

	}	
}
