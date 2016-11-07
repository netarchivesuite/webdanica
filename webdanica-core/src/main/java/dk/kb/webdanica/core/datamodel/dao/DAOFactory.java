package dk.kb.webdanica.core.datamodel.dao;


public interface DAOFactory {

	public SeedsDAO getSeedsDAO();

	public CriteriaResultsDAO getCriteriaResultsDAO();

	public BlackListDAO getBlackListDAO();

	public HarvestDAO getHarvestDAO();

	public IngestLogDAO getIngestLogDAO();

	public void close();

	public CacheDAO getCacheDAO();
	
}
