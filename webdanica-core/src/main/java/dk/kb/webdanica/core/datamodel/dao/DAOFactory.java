package dk.kb.webdanica.core.datamodel.dao;


public interface DAOFactory extends AutoCloseable {

	public SeedsDAO getSeedsDAO();
	
	public DomainsDAO getDomainsDAO();

	public CriteriaResultsDAO getCriteriaResultsDAO();

	public BlackListDAO getBlackListDAO();

	public HarvestDAO getHarvestDAO();

	public IngestLogDAO getIngestLogDAO();

	public CacheDAO getCacheDAO();
	
	public String describe();
	
	public void close();
}
