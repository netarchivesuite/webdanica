package dk.kb.webdanica.core.datamodel.dao;

import java.util.List;

import dk.kb.webdanica.core.datamodel.DanicaStatus;
import dk.kb.webdanica.core.datamodel.Domain;

public interface DomainsDAO {

	boolean insertDomain(Domain singleSeed) throws Exception;

	List<Domain> getDomains(DanicaStatus status, String tld, int limit) throws Exception;
	
	Domain getDomain(String domain) throws Exception;

	void close();

	Long getDomainsCount(DanicaStatus status, String tld) throws Exception;
}
