package dk.kb.webdanica.core.datamodel.dao;

import java.util.List;

import dk.kb.webdanica.core.interfaces.harvesting.HarvestReport;
import dk.kb.webdanica.core.interfaces.harvesting.SingleSeedHarvest;

public interface HarvestDAO {

	boolean insertHarvest(HarvestReport h) throws Exception;
	boolean insertHarvest(SingleSeedHarvest report) throws Exception;

	HarvestReport getHarvest(String harvestName) throws Exception;

	List<HarvestReport> getAll() throws Exception;
	
	List<String> getAllNames() throws Exception;

	List<HarvestReport> getAllWithSeedurl(String seed) throws Exception;

	List<HarvestReport> getAllWithSuccessfulstate(boolean b) throws Exception;
	
	Long getCount() throws Exception;

	void close();

}
