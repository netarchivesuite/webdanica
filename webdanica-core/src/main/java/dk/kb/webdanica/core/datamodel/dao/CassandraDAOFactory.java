package dk.kb.webdanica.core.datamodel.dao;


public class CassandraDAOFactory implements DAOFactory {

	private BlackListDAO blacklistDao;

	private CriteriaResultsDAO criteriaResultsDao;
	
	private HarvestDAO harvestDAO;
	
	private SeedsDAO seedDao;

	private IngestLogDAO ingestLogDAO;

	public CassandraDAOFactory() {
		seedDao = CassandraSeedDAO.getInstance();
	    harvestDAO = CassandraHarvestDAO.getInstance();
	    criteriaResultsDao = CassandraCriteriaResultsDAO.getInstance();
	    blacklistDao = CassandraBlackListDAO.getInstance();
	    ingestLogDAO = CassandraIngestLogDAO.getInstance();
	}

	@Override
	public SeedsDAO getSeedsDAO() {
		return seedDao;
	}

	@Override
	public CriteriaResultsDAO getCriteriaResultsDAO() {
	    return criteriaResultsDao;
    }

	@Override
	public BlackListDAO getBlackListDAO() {
	    return blacklistDao;
    }

	@Override
	public HarvestDAO getHarvestDAO() {
	    return harvestDAO;
    }

	public IngestLogDAO getIngestLogDAO() {
		return ingestLogDAO;
	}

	@Override
	public void close() {
		seedDao.close();
    }
	
}
