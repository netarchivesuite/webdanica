package dk.kb.webdanica.core.datamodel.dao;

import java.util.List;

import dk.kb.webdanica.core.datamodel.DanicaStatus;
import dk.kb.webdanica.core.datamodel.Seed;
import dk.kb.webdanica.core.datamodel.Status;

public interface SeedsDAO {

	boolean insertSeed(Seed singleSeed) throws Exception;	
	
	boolean updateSeed(Seed singleSeed) throws Exception;
	
	List<Seed> getSeeds(Status fromOrdinal, int limit) throws Exception;

	Long getSeedsCount(Status fromOrdinal) throws Exception;
	
	void close();

	Long getSeedsDanicaCount(DanicaStatus s) throws Exception;

	List<Seed> getSeedsReadyToExport() throws Exception;

	boolean existsUrl(String url) throws Exception;
	
	Seed getSeed(String url)  throws Exception;
}
	
	

