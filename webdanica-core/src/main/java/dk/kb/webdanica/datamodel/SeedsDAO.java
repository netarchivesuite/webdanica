package dk.kb.webdanica.datamodel;

import java.util.List;

public interface SeedsDAO {

	List<Seed> getSeeds(Status fromOrdinal);

	Long getSeedsCount(Status fromOrdinal);

	boolean updateRedirectedUrl(Seed s);

	boolean updateState(Seed s);

	void close();

	boolean insertSeed(Seed singleSeed);

}
