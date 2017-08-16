package dk.kb.webdanica.core.datamodel.dao;

import java.util.List;
import java.util.UUID;

import dk.kb.webdanica.core.datamodel.BlackList;

public interface BlackListDAO {

	boolean insertList(BlackList aBlackList) throws Exception;

	BlackList readBlackList(UUID fromString) throws Exception;

	List<BlackList> getLists(boolean b) throws Exception;

	void close();

}
