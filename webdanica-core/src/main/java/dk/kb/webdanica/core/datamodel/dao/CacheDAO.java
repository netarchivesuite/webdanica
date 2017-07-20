package dk.kb.webdanica.core.datamodel.dao;

import dk.kb.webdanica.core.datamodel.Cache;

public interface CacheDAO {

	Cache getCache() throws Exception;

	boolean updateCache(Cache cache) throws Exception;
}
