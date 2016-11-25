package dk.kb.webdanica.core.datamodel.dao;

import java.util.List;

import dk.kb.webdanica.core.interfaces.harvesting.SingleSeedHarvest;

public interface HarvestDAO {
	
	boolean insertHarvest(SingleSeedHarvest report) throws Exception;

	SingleSeedHarvest getHarvest(String harvestName) throws Exception;

	List<SingleSeedHarvest> getAll() throws Exception;
	
	List<String> getAllNames() throws Exception;

	List<SingleSeedHarvest> getAllWithSeedurl(String seed) throws Exception;

	List<SingleSeedHarvest> getAllWithSuccessfulstate(boolean b) throws Exception;
	
	Long getCount() throws Exception;

	void close();

}
