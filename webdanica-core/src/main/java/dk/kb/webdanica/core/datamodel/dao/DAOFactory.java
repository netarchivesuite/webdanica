package dk.kb.webdanica.core.datamodel.dao;

import dk.kb.webdanica.core.datamodel.BlackListDAO;
import dk.kb.webdanica.core.datamodel.CriteriaResultsDAO;
import dk.kb.webdanica.core.datamodel.HarvestDAO;
import dk.kb.webdanica.core.datamodel.IngestLogDAO;
import dk.kb.webdanica.core.datamodel.SeedsDAO;

public interface DAOFactory {

	public SeedsDAO getSeedsDAO();

	public CriteriaResultsDAO getCriteriaResultsDAO();

	public BlackListDAO getBlackListDAO();

	public HarvestDAO getHarvestDAO();

	public IngestLogDAO getIngestLogDAO();

	public void close();
	
}
