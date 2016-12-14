package dk.kb.webdanica.core.datamodel.dao;

import java.util.List;
import java.util.Set;

import dk.kb.webdanica.core.datamodel.DanicaStatus;
import dk.kb.webdanica.core.datamodel.Domain;

public interface DomainsDAO {

	boolean insertDomain(Domain singleSeed) throws Exception;

	List<Domain> getDomains(DanicaStatus status, String tld, int limit) throws Exception;
	
	Domain getDomain(String domain) throws Exception;

	void close();

	Long getDomainsCount(DanicaStatus status, String tld) throws Exception;

	boolean existsDomain(String domain) throws Exception;

	boolean update(Domain d) throws Exception;

	Set<String> getTlds() throws Exception;
}
