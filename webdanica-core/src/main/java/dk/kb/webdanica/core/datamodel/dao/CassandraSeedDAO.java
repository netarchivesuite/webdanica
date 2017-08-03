package dk.kb.webdanica.core.datamodel.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

import dk.kb.webdanica.core.datamodel.Cassandra;
import dk.kb.webdanica.core.datamodel.DanicaStatus;
import dk.kb.webdanica.core.datamodel.Database;
import dk.kb.webdanica.core.datamodel.Seed;
import dk.kb.webdanica.core.datamodel.Status;

/**
 * DAO class for the seeds table.
 * @author svc
 *
 */
public class CassandraSeedDAO implements SeedsDAO {
		
	static CassandraSeedDAO instance;
	
	private Database db;

	private Session session;
	private PreparedStatement preparedInsert;
	
	private PreparedStatement readAllWithStatestatement;
	
	private PreparedStatement readAllWithStateAndLimitstatement;

	private PreparedStatement preparedUpdateStatement;
	
	private PreparedStatement getSeedsCountStatement;
	
	private boolean newSession = false;

	private PreparedStatement preparedUpdateRedirectedUrl;
	
	public synchronized static CassandraSeedDAO getInstance(){
		if (instance == null) {
			instance = new CassandraSeedDAO();
		} 
		return instance;
	}
	
	public CassandraSeedDAO() {
		CassandraSettings settings = CassandraSettings.getDefaultSettings();
		db = new Cassandra(settings);
	}
	
	public List<Seed> getSeeds(Status status, int limit) throws DaoException {
		try {
			init();
			BoundStatement bStatement = readAllWithStateAndLimitstatement.bind(status.ordinal(), limit);
			ResultSet results = session.execute(bStatement);
			List<Seed> seedList = new ArrayList<Seed>();

			for (Row row : results.all()) {
				Seed s = getSeedFromRow(row);
				seedList.add(s);
			}
			return seedList;
		} catch (Exception e) {
			throw new DaoException(e);
		}
	}
	
	public List<Seed> getSeeds(Status status) throws DaoException {
		try {
			init();
			BoundStatement bStatement = readAllWithStatestatement.bind(status.ordinal());
			ResultSet results = session.execute(bStatement);
			List<Seed> seedList = new ArrayList<Seed>();

			for (Row row : results.all()) {
				Seed s = getSeedFromRow(row);
				seedList.add(s);
			}
			return seedList;
		} catch (Exception e) {
			throw new DaoException(e);
		}
	}

	private Seed getSeedFromRow(Row row) {
		// Not yet tested!
		return new Seed(
				row.getString("url"),
				row.getString("redirected_url"), 
				row.getString("host"),
				row.getString("domain"),
				row.getString("tld"),
				row.getLong("inserted_time"),
				row.getLong("updated_time"),
				DanicaStatus.fromOrdinal(row.getInt("danica")),
				Status.fromOrdinal(row.getInt("status")), 
				row.getString("status_reason"),
				row.getBool("exported"),
				row.getLong("exported_time"),
				row.getString("danica_reason")
				);
    }

	public boolean insertSeed(Seed singleSeed) throws DaoException {
		try {
			init();
			Date insertedDate = new Date();
			BoundStatement bound = preparedInsert.bind(singleSeed.getUrl(), singleSeed.getStatus().ordinal(), insertedDate);
			ResultSet rs = session.execute(bound);
			Row row = rs.one();
			boolean insertFailed = row.getColumnDefinitions().contains("url");
			return !insertFailed; // Was the insert successful?
		} catch (Exception e){
			throw new DaoException(e);
		}
	}

	private void init() throws Exception {
			if (session == null || session.isClosed()) {
				session = db.getSession();
				newSession = true;
			}
			if (preparedInsert == null || newSession) {
				//FIXME This is no longer valid
				preparedInsert = session.prepare("INSERT INTO seeds (url, status, inserted_time) VALUES (?,?,?) IF NOT EXISTS");
			}
		/*
		row.getString("url"),
		row.getString("redirected_url"), 
		row.getString("host"),
		row.getString("domain"),
		row.getString("tld"),
		row.getLong("inserted_time"),
		row.getLong("updated_time"),
		DanicaStatus.fromOrdinal(row.getInt("danica")),
		Status.fromOrdinal(row.getInt("status")), 
		row.getString("status_reason"),
		row.getBool("exported"),
		row.getLong("exported_time")
		*/

			if (preparedUpdateStatement == null || newSession) {
				preparedUpdateStatement = session.prepare("UPDATE seeds SET redirected_url=?, host=?, domain=?, tld=?, inserted_time=?, updated_time=?, danica=?, status=?, status_reason=?, exported=?, exported_time=? WHERE url=? IF EXISTS");
			}
			if (readAllWithStatestatement == null || newSession) {
				readAllWithStatestatement = session.prepare("SELECT * FROM seeds WHERE status=?");
			}
			if (readAllWithStateAndLimitstatement == null || newSession) {
				readAllWithStateAndLimitstatement = session.prepare("SELECT * FROM seeds WHERE status=? LIMIT ?");
			}

			if (getSeedsCountStatement == null || newSession) {
				getSeedsCountStatement = session.prepare("SELECT count(*) FROM seeds WHERE status=?");
			}

			if (preparedUpdateRedirectedUrl == null || newSession) {
				preparedUpdateRedirectedUrl = session.prepare("UPDATE seeds SET redirected_url=? WHERE url=? IF EXISTS");
			}
			newSession = false;

    }
	
	public boolean updateSeed(Seed singleSeed) throws DaoException {
		try {
		init();
		if (singleSeed.getExportedState() == true && singleSeed.getExportedTime() == null) {
			singleSeed.setExportedTime(new Date().getTime());
		}
		// UPDATE seeds SET redirected_url=?, host=?, domain=?, tld=?, inserted_time=?, updated_time=?, danica=?, status=?, status_reason=?, exported=?, exported_time=? WHERE url=? IF EXISTS");
		Long updatedTime = new Date().getTime();
		BoundStatement bound = preparedUpdateStatement.bind(
				singleSeed.getRedirectedUrl(),
				singleSeed.getHostname(),
				singleSeed.getDomain(),
				singleSeed.getTld(),
				singleSeed.getInsertedTime(),
				updatedTime,
				singleSeed.getDanicaStatus().ordinal(),
				singleSeed.getStatus().ordinal(), 
				singleSeed.getStatusReason(),
				singleSeed.getExportedState(),
				singleSeed.getExportedTime()
				);
		ResultSet rs = session.execute(bound);
		Row row = rs.one();
		// Possibly change this code. this depends on appending IF EXISTS to the statement
		boolean updateFailed = row.getColumnDefinitions().contains("url");
		return !updateFailed;
	} catch (Exception e){
		throw new DaoException(e);
	}

}
	
	public Long getSeedsCount(Status status) throws DaoException {
		try {
		init();
		BoundStatement bStatement = getSeedsCountStatement.bind(status.ordinal());
		ResultSet rs = session.execute(bStatement);
		Row row = rs.one();
		return new Long(row.getLong(0));
		} catch (Exception e){
			throw new DaoException(e);
		}

	}	
	

	public void close() {
	    db.close();
    }
/*
	public boolean updateRedirectedUrl(Seed s) {
	    init();
	    BoundStatement bound = preparedUpdateRedirectedUrl.bind(s.getRedirectedUrl(), s.getUrl()); 
		ResultSet rs = session.execute(bound);
		Row row = rs.one();
		// Possibly change this code. this depends on appending IF EXISTS to the statement
		boolean updateFailed = row.getColumnDefinitions().contains("url");
		return !updateFailed;
    }
*/
	@Override
    public Long getSeedsDanicaCount(DanicaStatus s) throws DaoException {
	    // TODO Auto-generated method stub
	    return null;
    }

	@Override
    public List<Seed> getSeedsReadyToExport(boolean includeExported) throws DaoException {
	    // TODO Auto-generated method stub
	    return null;
    }

	@Override
    public boolean existsUrl(String url) throws DaoException {
	    // TODO Auto-generated method stub
	    return false;
    }

	@Override
    public Seed getSeed(String url) throws DaoException {
	    // TODO Auto-generated method stub
	    return null;
    }

}
