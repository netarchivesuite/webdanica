package dk.kb.webdanica.core.datamodel;

import java.util.List;

public interface SeedDAO {
	List<Seed> getSeeds(Status fromOrdinal, int limit);

	List<Seed> getSeeds(Status fromOrdinal);

	Long getSeedsCount(Status fromOrdinal);

	boolean updateRedirectedUrl(Seed s);

	boolean updateState(Seed s);

	void close();

	boolean insertSeed(Seed singleSeed);

}
