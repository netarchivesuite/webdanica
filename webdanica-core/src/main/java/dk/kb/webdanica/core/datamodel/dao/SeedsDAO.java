package dk.kb.webdanica.core.datamodel.dao;

import java.util.List;

import dk.kb.webdanica.core.datamodel.Seed;
import dk.kb.webdanica.core.datamodel.Status;

public interface SeedsDAO {

	boolean insertSeed(Seed singleSeed) throws Exception;

	boolean updateRedirectedUrl(Seed s) throws Exception;

	boolean updateState(Seed s) throws Exception;

	List<Seed> getSeeds(Status fromOrdinal, int limit) throws Exception;

	Long getSeedsCount(Status fromOrdinal) throws Exception;
	
	void close();

}
	
	

