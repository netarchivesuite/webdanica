package dk.kb.webdanica.core.datamodel.dao;


public class HBasePhoenixDAOFactory implements DAOFactory {

	private BlackListDAO blacklistDao;

	private CriteriaResultsDAO criteriaResultsDao;
	
	private HarvestDAO harvestDAO;
	
	private SeedsDAO seedDao;

	private IngestLogDAO ingestLogDAO;

	private CacheDAO cacheDao;

	public HBasePhoenixDAOFactory() {
		HBasePhoenixConnectionManager.register();
	    blacklistDao = new HBasePhoenixBlackListDAO();
	    criteriaResultsDao = new HBasePhoenixCriteriaResultsDAO();
	    harvestDAO = new HBasePhoenixHarvestDAO();
	    ingestLogDAO = new HBasePhoenixIngestLogDAO();
		seedDao = new HBasePhoenixSeedsDAO();
		cacheDao = new HBasePhoenixCacheDAO();
		
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
		HBasePhoenixConnectionManager.deregister();
	}

	@Override
    public CacheDAO getCacheDAO() {
		return cacheDao;
    }

}
