package dk.kb.webdanica.datamodel;

import java.util.List;

import dk.kb.webdanica.interfaces.harvesting.HarvestReport;

public interface HarvestDAO {

	HarvestReport getHarvest(String harvestName);

	boolean insertHarvest(HarvestReport h);

	List<HarvestReport> getAll();

	void close();

	List<HarvestReport> getAllWithSeedurl(String seed);

	List<HarvestReport> getAllWithSuccessfulstate(boolean b);

}
