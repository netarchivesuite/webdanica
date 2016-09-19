package dk.kb.webdanica.datamodel.dao;

import dk.kb.webdanica.datamodel.BlackListDAO;
import dk.kb.webdanica.datamodel.CriteriaResultsDAO;
import dk.kb.webdanica.datamodel.HarvestDAO;
import dk.kb.webdanica.datamodel.IngestLogDAO;
import dk.kb.webdanica.datamodel.SeedsDAO;

public class HBasePhoenixDAOFactory implements DAOFactory {

	@Override
	public BlackListDAO getBlackListDAO() {
		//return new HBasePhoenixBlackListDAO();
		return null;
	}

	@Override
	public CriteriaResultsDAO getCriteriaResultsDAO() {
		//return new HBasePhoenixCriteriaResultsDAO();
		return null;
	}

	@Override
	public HarvestDAO getHarvestDAO() {
		//return new HBasePhoenixHarvestDAO();
		return null;
	}

	@Override
	public IngestLogDAO getIngestLogDAO() {
		//return new HBasePhoenixIngestLogDAO();
		return null;
	}

	@Override
	public SeedsDAO getSeedsDAO() {
		//return new HBasePhoenixSeedsDAO();
		return null;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
	}

}
