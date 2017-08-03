package dk.kb.webdanica.core.datamodel.dao;

import java.util.List;
import java.util.Set;

import dk.kb.webdanica.core.datamodel.DanicaStatus;
import dk.kb.webdanica.core.datamodel.Domain;

public interface DomainsDAO extends AutoCloseable{

	boolean insertDomain(Domain singleSeed) throws DaoException;

	List<Domain> getDomains(DanicaStatus status, String tld, int limit) throws DaoException;
	
	Domain getDomain(String domain) throws DaoException;

	void close();

	Long getDomainsCount(DanicaStatus status, String tld) throws DaoException;

	boolean existsDomain(String domain) throws DaoException;

	boolean update(Domain d) throws DaoException;

	Set<String> getTlds() throws DaoException;
}
