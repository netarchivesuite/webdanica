package dk.kb.webdanica.datamodel;

import java.util.List;

import dk.kb.webdanica.datamodel.criteria.SingleCriteriaResult;

public interface CriteriaResultsDAO {

	SingleCriteriaResult getSingleResult(String url, String harvest);

	long getCountByHarvest(String harvestName);

	void deleteRecordsByHarvestname(String string);

	boolean insertRecord(SingleCriteriaResult s);

	List<SingleCriteriaResult> getResultsByHarvestname(String string);

	List<SingleCriteriaResult> getResultsBySeedurl(String string);

	List<SingleCriteriaResult> getResults();	

}
