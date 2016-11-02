package dk.kb.webdanica.datamodel.dao;

import dk.kb.webdanica.datamodel.BlackListDAO;
import dk.kb.webdanica.datamodel.CriteriaResultsDAO;
import dk.kb.webdanica.datamodel.HarvestDAO;
import dk.kb.webdanica.datamodel.IngestLogDAO;
import dk.kb.webdanica.datamodel.SeedsDAO;

public interface DAOFactory {

	public SeedsDAO getSeedsDAO();

	public CriteriaResultsDAO getCriteriaResultsDAO();

	public BlackListDAO getBlackListDAO();

	public HarvestDAO getHarvestDAO();

	public IngestLogDAO getIngestLogDAO();

	public void close();
	
}
