package dk.kb.webdanica.core.datamodel.dao;

import java.sql.SQLException;

public class HBasePhoenixDAOFactory implements DAOFactory {

	private BlackListDAO blacklistDao;

	private CriteriaResultsDAO criteriaResultsDao;
	
	private HarvestDAO harvestDAO;
	
	private SeedsDAO seedDao;

	private IngestLogDAO ingestLogDAO;

	private CacheDAO cacheDao;
	
	private DomainsDAO domainsDao;

	public HBasePhoenixDAOFactory() {
		HBasePhoenixConnectionManager.register();
	    blacklistDao = new HBasePhoenixBlackListDAO();
	    criteriaResultsDao = new HBasePhoenixCriteriaResultsDAO();
	    harvestDAO = new HBasePhoenixHarvestDAO();
	    ingestLogDAO = new HBasePhoenixIngestLogDAO();
		seedDao = new HBasePhoenixSeedsDAO();
		cacheDao = new HBasePhoenixCacheDAO();
		domainsDao = new HBasePhoenixDomainsDAO();
	}

	@Override
	public BlackListDAO getBlackListDAO() {
		return blacklistDao;
	}

	@Override
	public CriteriaResultsDAO getCriteriaResultsDAO() {
		return criteriaResultsDao;
	}

	@Override
	public HarvestDAO getHarvestDAO() {
		return harvestDAO;
	}

	@Override
	public IngestLogDAO getIngestLogDAO() {
		return ingestLogDAO;
	}

	@Override
	public SeedsDAO getSeedsDAO() {
		return seedDao;
	}

	@Override
	public void close() {
		try {
	        HBasePhoenixConnectionManager.closeAllConnections();
        } catch (SQLException e) {
        	
        }
		HBasePhoenixConnectionManager.deregister();
	}

	@Override
    public CacheDAO getCacheDAO() {
		return cacheDao;
    }

	@Override
    public DomainsDAO getDomainsDAO() {
	    return domainsDao;
    }

	@Override
    public String describe() {
	    return "Apache phoenix database interface";
    }

}
