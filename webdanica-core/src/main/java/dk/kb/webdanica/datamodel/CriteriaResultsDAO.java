package dk.kb.webdanica.datamodel;

import java.util.List;

import dk.kb.webdanica.datamodel.criteria.SingleCriteriaResult;

public interface CriteriaResultsDAO {

	boolean insertRecord(SingleCriteriaResult s) throws Exception;

	SingleCriteriaResult getSingleResult(String url, String harvest) throws Exception;

	List<SingleCriteriaResult> getResultsByHarvestname(String string) throws Exception;

	List<SingleCriteriaResult> getResultsByUrl(String url) throws Exception;

	List<SingleCriteriaResult> getResultsBySeedurl(String string) throws Exception;

	List<SingleCriteriaResult> getResults() throws Exception;

	List<String> getHarvestedUrls(String harvestname) throws Exception;

	long getCountByHarvest(String harvestName) throws Exception;

	void deleteRecordsByHarvestname(String string) throws Exception;

}
