package dk.kb.webdanica.datamodel;

import java.util.List;

public interface SeedsDAO {

	boolean insertSeed(Seed singleSeed) throws Exception;

	boolean updateRedirectedUrl(Seed s) throws Exception;

	boolean updateState(Seed s) throws Exception;

	List<Seed> getSeeds(Status fromOrdinal) throws Exception;

	Long getSeedsCount(Status fromOrdinal) throws Exception;
	
	void close();

}
