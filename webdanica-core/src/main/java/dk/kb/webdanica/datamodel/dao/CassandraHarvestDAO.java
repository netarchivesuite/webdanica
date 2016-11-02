package dk.kb.webdanica.datamodel.dao;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

import dk.kb.webdanica.datamodel.Cassandra;
import dk.kb.webdanica.datamodel.HarvestDAO;
import dk.kb.webdanica.interfaces.harvesting.HarvestReport;
import dk.netarkivet.harvester.datamodel.JobStatus;

/*
use webdanica;
CREATE TABLE harvests ( 
    harvestname text,
    seedurl text,
    error       text,
    successful  boolean,
    finalState  int,
    harvested_time bigint,
    files list<text>,
    fetched_urls list<text>, 
    seed_report text,
    crawllog text,
    PRIMARY KEY (harvestname)
    );
    CREATE INDEX harvest_finalstate_idx ON webdanica.harvests (finalState);
    CREATE INDEX harvest_seedurl_idx ON webdanica.harvests (seedurl);
    CREATE INDEX harvest_successful_idx ON webdanica.harvests (successful);
*/
public class CassandraHarvestDAO implements HarvestDAO {
	
	private static CassandraHarvestDAO instance;
	private Session session;
	private PreparedStatement preparedInsert;
	private PreparedStatement readWithHarvestnamestatement;
	private PreparedStatement readAllWithSeedurlstatement;
	private PreparedStatement readAllWithFinalStatestatement;
	private PreparedStatement readAllWithSuccessfulstatement;
	private boolean newSession = false;
	private final Cassandra db;
	
	public static void main(String[] args) throws IOException {
		//File basedir = UnitTestUtils.getTestResourceFile(
		File basedir = new File("/home/svc/devel/webdanica/webdanica-core/src/test/resources/criteria-results/criteria-test-23-08-2016");
		File harvestlog = new File(basedir, "nl-urls-harvestlog.txt");
		List<HarvestReport> harvests = HarvestReport.readHarvestLog(harvestlog);
		
		HarvestDAO dao = CassandraHarvestDAO.getInstance();
		int duplicateCount = 0;
		try {
			System.out.println("harvest count: " + harvests.size());
			for (HarvestReport h: harvests) {
				boolean successful = dao.insertHarvest(h);
				if (!successful) {
					duplicateCount++;
				}
			}
			List<HarvestReport> alle = dao.getAll();
			System.out.println("harvest db count: " + alle.size());
			System.out.println("harvest duplicate count: " + duplicateCount);
			HarvestReport h = alle.get(0);
			System.out.println(h.toString());
			HarvestReport firstReport = dao.getHarvest(h.harvestName);
			System.out.println(firstReport.toString());
			List<HarvestReport> seedUrlReports = dao.getAllWithSeedurl(h.seed);
			System.out.println("reports with seedurl '" + h.seed + "': " + seedUrlReports.size());
			List<HarvestReport> successfullReports = dao.getAllWithSuccessfulstate(true);
			System.out.println("reports with successful : " + successfullReports.size());
			List<HarvestReport> unsuccessfullReports = dao.getAllWithSuccessfulstate(false);
			System.out.println("reports with unsuccessful : " + unsuccessfullReports.size());
			
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			dao.close();
		}
	}

	public synchronized static HarvestDAO getInstance(){
		if (instance == null) {
			instance = new CassandraHarvestDAO();
		} 
		return instance;
	}
	
	public CassandraHarvestDAO() {
		CassandraSettings settings = CassandraSettings.getDefaultSettings();
		db = new Cassandra(settings);
	}

	public boolean insertHarvest(HarvestReport report) {
		init();
		long harvestedTime = report.harvestedTime;
		if (!(harvestedTime > 0)) {
			harvestedTime = System.currentTimeMillis();
			System.err.println("harvestedTime undefined. setting it to  " + harvestedTime);
		}
		//harvestname, seedurl, finalState, successful, harvested_time, files, error
		BoundStatement bound = preparedInsert.bind(report.harvestName, report.seed, 
				report.finalState.ordinal(), report.successful, harvestedTime,  
				report.getAllFiles(), report.error);
				
		ResultSet rs = session.execute(bound);
		Row row = rs.one();
		boolean insertFailed = row.getColumnDefinitions().contains("seedurl");
		return !insertFailed; // Was the insert successful?
	}

	public List<HarvestReport> getAll() {
		init();
		List<HarvestReport> harvestsFound = new ArrayList<HarvestReport>();
		ResultSet results = session.execute("SELECT * FROM harvests");
		for (Row row: results.all()) {
			HarvestReport h = getHarvestFromRow(row);
			harvestsFound.add(h);
		}
		return harvestsFound; 
	}
	
	/**
	 * @param harvestName a given harvestname
	 * @return null, if none found with given harvestname
	 */
	public HarvestReport getHarvest(String harvestName) {
		init();
		BoundStatement bound = readWithHarvestnamestatement.bind(harvestName);
		ResultSet results = session.execute(bound);
		Row row = results.one();
		return getHarvestFromRow(row);
	}
	
	private HarvestReport getHarvestFromRow(Row row) {
		return new HarvestReport(row.getString("harvestname"), row.getString("seedurl"), row.getBool("successful"), row.getList("files", String.class),
				row.getString("error"), JobStatus.fromOrdinal(row.getInt("finalState")), row.getLong("harvested_time"));
	}
	
	public List<HarvestReport> getAllWithSeedurl(String seedurl) {
		init();
		List<HarvestReport> harvestsFound = new ArrayList<HarvestReport>();
		BoundStatement bound = readAllWithSeedurlstatement.bind(seedurl);
		ResultSet results = session.execute(bound);
		for (Row row: results.all()) {
			HarvestReport h = getHarvestFromRow(row);
			harvestsFound.add(h);
		}
		return harvestsFound; 
	}
	public List<HarvestReport> getAllWithSuccessfulstate(boolean successful) {
		init();
		List<HarvestReport> harvestsFound = new ArrayList<HarvestReport>();
		BoundStatement bound = readAllWithSuccessfulstatement.bind(successful);
		ResultSet results = session.execute(bound);
		for (Row row: results.all()) {
			HarvestReport h = getHarvestFromRow(row);
			harvestsFound.add(h);
		}
		return harvestsFound; 
	}
	
	
	
	
	
	private void init() {
		if (session == null || session.isClosed()) {			
			session = db.getSession();
			newSession = true;
		}
		if (preparedInsert == null || newSession) {
			/*
			seedurl text, 
		    harvestname text,
		    successful  boolean,
		    finalState  int,
		    harvested_time bigint,
		    files list<text>,
		    error       text
		    */
			preparedInsert = session.prepare("INSERT INTO harvests (harvestname, seedurl, finalState, successful, harvested_time, files, error) VALUES (?,?,?,?,?,?,?) IF NOT EXISTS");
		}
		
		
		if (readAllWithFinalStatestatement == null || newSession) {
			readAllWithFinalStatestatement = session.prepare("SELECT * FROM harvests WHERE finalState=?");
		}
		
		if (readWithHarvestnamestatement == null || newSession) {
			readWithHarvestnamestatement = session.prepare("SELECT * FROM harvests WHERE harvestname=?");
		}
		
		if (readAllWithSuccessfulstatement == null || newSession) {
			readAllWithSuccessfulstatement = session.prepare("SELECT * FROM harvests WHERE successful=?");
		}
		
		if (readAllWithSeedurlstatement == null || newSession) {
			readAllWithSeedurlstatement = session.prepare("SELECT * FROM harvests WHERE seedurl=?");
		}
		newSession = false;
    }

	@Override
    public void close() {
	    db.close();
    }

    @Override
    public Long getCount() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getAllNames() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }
	
}
