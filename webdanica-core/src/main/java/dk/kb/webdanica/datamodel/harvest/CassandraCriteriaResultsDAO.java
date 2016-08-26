package dk.kb.webdanica.datamodel.harvest;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

import dk.kb.webdanica.datamodel.Cassandra;
import dk.kb.webdanica.datamodel.CassandraSettings;
import dk.kb.webdanica.datamodel.criteria.CriteriaIngest;
import dk.kb.webdanica.datamodel.criteria.DataSource;
import dk.kb.webdanica.datamodel.criteria.SingleCriteriaResult;

/**
 * DAO for inserting criteria analysis data.
 * 
 * --- meta information --
 * seedurl: the original seed for this url
 * harvestname: The name of the harvest producing this data
 * url: the url harvested and analysed
 * urlOrig: Is necessary? 
 * 
 * --- Criteria results ---
 *
 * Cext1
 * Cext2
 * Cext3
 * C1a
 * C2a
 * C2b
 * C3a
 * C3b
 * C3c
 * C3d
 * C3e
 * C3f
 * C3g
 * C4a
 * C4b
 * C5a
 * C5b
 * C6a
 * C6b
 * C6c
 * C6d
 * C7a
 * C7b
 * C7c
 * C7d
 * C7e
 * C7f
 * C7g
 * C7h
 * C8a
 * C8b
 * C8c
 * C9a
 * C9b
 * C9c
 * C9d
 * C9e
 * C9f
 * C10a
 * C10b
 * C10c
 * C15a
 * C15b
 * C16a
 * C17a
 * C18a
 * intDanish
 * source
 * calcDanishCode
 * CText
 * CLinks
 */
public class CassandraCriteriaResultsDAO {
	
	public static void main(String[] args) throws IOException, SQLException {
		CassandraCriteriaResultsDAO dao = CassandraCriteriaResultsDAO.getInstance();
		
		dao.deleteRecordsByHarvestname("harvestName"); // delete existing records from database
		
		File ingestFile = new File("/home/svc/devel/webdanica/criteria-test-11-08-2016/criteria-results/11-08-2016-1470934842/84-70-20160808164652141-00000-dia-prod-udv-01.kb.dk.warc.gz/part-m-00000.gz");
		List<SingleCriteriaResult> results 
			= CriteriaIngest.process(ingestFile, "Theseed", "harvestName", false).results;
		System.out.println("Found records: " + results.size());
		for (SingleCriteriaResult s: results) {
			boolean inserted = dao.insertRecord(s);
			System.out.println("record inserted: " + inserted);
		}
		List<SingleCriteriaResult> resultsA = dao.getResultsByHarvestname("harvestName");
		System.out.println("Record found: " +resultsA.size()); 
		List<SingleCriteriaResult> resultsB = dao.getResultsBySeedurl("Theseed");
		System.out.println("Record found: " +resultsB.size());
		System.exit(0);
	}
	

	private static CassandraCriteriaResultsDAO instance;
	private Session session;
	private PreparedStatement preparedInsert;
	
	private PreparedStatement readAllWithUrlstatement;
	private PreparedStatement readAllWithSeedurlstatement;
	private PreparedStatement readAllWithHarvestnamestatement;
	private PreparedStatement deleteAllWithUrlAndHarvestnamestatement;
	
	private boolean newSession = false;
	private final Cassandra db;
	private PreparedStatement readUrlsByharveststatement;
	
	public synchronized static CassandraCriteriaResultsDAO getInstance(){
		if (instance == null) {
			instance = new CassandraCriteriaResultsDAO();
		} 
		return instance;
	}
	
	public CassandraCriteriaResultsDAO() {
		CassandraSettings settings = CassandraSettings.getDefaultSettings();
		db = new Cassandra(settings);
	}
	
	/**
	 * Insert SingleCriteriaResult.
	 * @param singleAnalysis A {@link SingleCriteriaResult} object
	 * @return true, if the insertion was successful, otherwise false
	 */
	public boolean insertRecord(SingleCriteriaResult singleAnalysis) {
		SingleCriteriaResult s = singleAnalysis;
		init();	
		//s.C ,
		Date insertedDate = new Date();
		BoundStatement bound = preparedInsert.bind(
				s.url, s.urlOrig, s.seedurl, s.harvestName, s.Cext1, s.Cext2, s.Cext3, // 1-7 
				s.C.get("C1a"), s.C.get("C2a"), s.C.get("C2b"), // 8-10
				s.C.get("C3a"), s.C.get("C3b"), s.C.get("C3c"), // 11-13
				s.C.get("C3d"), s.C.get("C3e"), s.C.get("C3f"), s.C.get("C3g"), //14-17 
				s.C.get("C4a"), s.C.get("C4b"), //18-19
				s.C.get("C5a"), s.C.get("C5b"), //20-21
				s.C.get("C6a"), s.C.get("C6b"), s.C.get("C6c"), s.C.get("C6d"), //22-25 
				s.C.get("C7a"), s.C.get("C7b"), s.C.get("C7c"), s.C.get("C7d"), //26-29
				s.C.get("C7e"), s.C.get("C7f"), s.C.get("C7g"), s.C.get("C7h"), //30-33
				s.C.get("C8a"), s.C.get("C8b"), s.C.get("C8c"), // 34-36
				s.C.get("C9a"), s.C.get("C9b"), s.C.get("C9c"), s.C.get("C9d"), // 37-40
				s.C.get("C9e"),s.C.get("C9f"), //41-42
				s.C.get("C10a"),s.C.get("C10b"), s.C.get("C10c"), // 43-45 
				s.C.get("C15a"), s.C.get("C15b"), //46-47
				s.C.get("C16a"), s.C.get("C17a"),s.C.get("C18a"), //48-50
				s.intDanish, s.source.ordinal(), s.calcDanishCode, s.CText, s.CLinks, // 51-55
				insertedDate.getTime()); // 56
		ResultSet rs = session.execute(bound);
		Row row = rs.one();
		boolean insertFailed = row.getColumnDefinitions().contains("url");
		return !insertFailed; // Was the insert successful?
	}
	
	public List<SingleCriteriaResult> getResultsByUrl(String url) {
		init();
		BoundStatement bStatement = readAllWithUrlstatement.bind(url);
		ResultSet results = session.execute(bStatement);
		List<SingleCriteriaResult> seedList = new ArrayList<SingleCriteriaResult>();
		
		for (Row row: results.all()) {
			SingleCriteriaResult s = getResultFromRow(row);
			seedList.add(s);
		}
		return seedList; 
	}
	
	public List<SingleCriteriaResult> getResultsBySeedurl(String seedurl) {
		init();
		BoundStatement bStatement = readAllWithSeedurlstatement.bind(seedurl);
		ResultSet results = session.execute(bStatement);
		List<SingleCriteriaResult> seedList = new ArrayList<SingleCriteriaResult>();
		
		for (Row row: results.all()) {
			SingleCriteriaResult s = getResultFromRow(row);
			seedList.add(s);
		}
		return seedList; 
	}
	
	public List<SingleCriteriaResult> getResultsByHarvestname(String harvestname) {
		init();
		BoundStatement bStatement = readAllWithHarvestnamestatement.bind(harvestname);
		ResultSet results = session.execute(bStatement);
		List<SingleCriteriaResult> seedList = new ArrayList<SingleCriteriaResult>();
		
		for (Row row: results.all()) {
			SingleCriteriaResult s = getResultFromRow(row);
			seedList.add(s);
		}
		return seedList; 
	}
	
	public List<String> getHarvestedUrls(String harvestname) {
		init();
		BoundStatement bStatement = readUrlsByharveststatement.bind(harvestname);
		ResultSet results = session.execute(bStatement);
		List<String> urlList = new ArrayList<String>();
		for (Row row: results.all()) {
			String s = row.getString("url");
			urlList.add(s);
		}
		return urlList; 
	}

	private SingleCriteriaResult getResultFromRow(Row row) {
		SingleCriteriaResult s = new SingleCriteriaResult();
		for (String c: SingleCriteriaResult.StringCriteria) {
			s.C.put(c, row.getString(c));
		}
		s.seedurl = row.getString("seedurl");
		
		s.url = row.getString("url");
		s.seedurl= row.getString("seedurl");
		s.harvestName = row.getString("harvestname"); 
		s.Cext1 = row.getLong("Cext1");
		s.Cext2 = row.getLong("Cext2");
		s.Cext3 = row.getLong("Cext3");
		s.urlOrig = row.getString("UrlOrig");
	    //s.Cext3Orig = row.getString("extWDateOrig");
		s.intDanish = row.getFloat("intDanish");
	    s.source = DataSource.fromOrdinal(row.getInt("source"));
	    s.calcDanishCode = row.getInt("calcDanishCode");
	    s.CText = row.getString("CText");
	    s.CLinks = row.getList("CLinks", String.class);
		s.insertedDate = row.getLong("inserted_time");
		s.updatedDate = row.getLong("updated_time");
	    return s;
	}
	
	
	/**
	 * Initialize PreparedStatements and the session
	 */
	private void init() {
		if (session == null || session.isClosed()) {			
			session = db.getSession();
			newSession = true;
		}
		if (preparedInsert == null || newSession) {
			preparedInsert = session.prepare("INSERT INTO criteria_results "
					+ "(url, urlOrig, seedurl, harvestname, Cext1, Cext2, Cext3, " //1-7
					+ "C1a, C2a, C2b, C3a, C3b, C3c, C3d, C3e, C3f, C3g, " // 8-17
					+ "C4a, C4b, C5a, C5b, C6a, C6b, C6c, C6d, " // 18-25
					+ "C7a, C7b, C7c, C7d, C7e, C7f, C7g, C7h, " //26-33
					+ "C8a, C8b, C8c, C9a, C9b, C9c, C9d, C9e, C9f, " //34-42
					+ "C10a, C10b, C10c, C15a, C15b, C16a, C17a, C18a, intDanish, source, calcDanishCode, CText, CLinks, " //43-55
					+ "inserted_time) " //56
					+ " VALUES (" +  
					StringUtils.repeat("?,", 55) + "?) IF NOT EXISTS");							
		}
		
		if (readAllWithUrlstatement == null || newSession) {
			readAllWithUrlstatement = session.prepare("SELECT * FROM criteria_results WHERE url=?");
		}
		
		if (readAllWithSeedurlstatement == null || newSession) {
			readAllWithSeedurlstatement = session.prepare("SELECT * FROM criteria_results WHERE seedurl=? ALLOW FILTERING");
		}
		
		if (readUrlsByharveststatement == null || newSession) {
			readUrlsByharveststatement = session.prepare("SELECT url FROM criteria_results WHERE harvestname=? ALLOW FILTERING");
		}
		
		if (readAllWithHarvestnamestatement == null || newSession) {
			readAllWithHarvestnamestatement = session.prepare("SELECT * FROM criteria_results WHERE harvestname=? ALLOW FILTERING");
		}
		
		if (deleteAllWithUrlAndHarvestnamestatement == null || newSession) {
			deleteAllWithUrlAndHarvestnamestatement = session.prepare("DELETE FROM criteria_results WHERE url=? AND harvestname=?");
		}
		
		
		newSession = false;
    }
	
	public void deleteRecordsByHarvestname(String harvestname) {
		List<String> urls = getHarvestedUrls(harvestname);
		for (String url: urls) {
			deleteRecordsByHarvestnameAndUrl(harvestname, url);
		}
    }

	private void deleteRecordsByHarvestnameAndUrl(String harvestname, String url) {
		init();
		BoundStatement bStatement = deleteAllWithUrlAndHarvestnamestatement.bind(url, harvestname);
		session.execute(bStatement);
    }

	public SingleCriteriaResult getResult(String url, String harvest, String seeduri) {
	    return null;
    }
}
