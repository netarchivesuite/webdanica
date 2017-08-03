package dk.kb.webdanica.core.datamodel.dao;

import java.util.List;

import dk.kb.webdanica.core.datamodel.DanicaStatus;
import dk.kb.webdanica.core.datamodel.Seed;
import dk.kb.webdanica.core.datamodel.Status;

public interface SeedsDAO extends AutoCloseable{

	boolean insertSeed(Seed singleSeed) throws DaoException;
	
	boolean updateSeed(Seed singleSeed) throws DaoException;
	
	List<Seed> getSeeds(Status fromOrdinal, int limit) throws DaoException;

	Long getSeedsCount(Status fromOrdinal) throws DaoException;
	
	void close();

	Long getSeedsDanicaCount(DanicaStatus s) throws DaoException;

	List<Seed> getSeedsReadyToExport(boolean includeAlreadyExportedSeeds) throws DaoException;

	boolean existsUrl(String url) throws DaoException;
	
	Seed getSeed(String url)  throws DaoException;
}
	
	

