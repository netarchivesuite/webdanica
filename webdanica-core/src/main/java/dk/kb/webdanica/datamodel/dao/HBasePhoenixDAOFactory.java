package dk.kb.webdanica.datamodel.dao;

import dk.kb.webdanica.datamodel.BlackListDAO;
import dk.kb.webdanica.datamodel.CriteriaResultsDAO;
import dk.kb.webdanica.datamodel.HarvestDAO;
import dk.kb.webdanica.datamodel.IngestLogDAO;
import dk.kb.webdanica.datamodel.SeedsDAO;

public class HBasePhoenixDAOFactory implements DAOFactory {

	private BlackListDAO blacklistDao;

	private CriteriaResultsDAO criteriaResultsDao;
	
	private HarvestDAO harvestDAO;
	
	private SeedsDAO seedDao;

	private IngestLogDAO ingestLogDAO;

	public HBasePhoenixDAOFactory() {
	    blacklistDao = new HBasePhoenixBlackListDAO();
	    criteriaResultsDao = new HBasePhoenixCriteriaResultsDAO();
	    harvestDAO = new HBasePhoenixHarvestDAO();
	    ingestLogDAO = new HBasePhoenixIngestLogDAO();
		seedDao = new HBasePhoenixSeedsDAO();
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
	}

}
