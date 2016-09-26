package dk.kb.webdanica.datamodel;

import java.util.List;

import dk.kb.webdanica.interfaces.harvesting.HarvestReport;

public interface HarvestDAO {

	boolean insertHarvest(HarvestReport h) throws Exception;

	HarvestReport getHarvest(String harvestName) throws Exception;

	List<HarvestReport> getAll() throws Exception;

	List<HarvestReport> getAllWithSeedurl(String seed) throws Exception;

	List<HarvestReport> getAllWithSuccessfulstate(boolean b) throws Exception;

	void close();

}