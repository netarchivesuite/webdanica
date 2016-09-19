package dk.kb.webdanica.datamodel;

import java.util.List;
import java.util.UUID;

public interface BlackListDAO {

	boolean insertList(BlackList aBlackList) throws Exception;

	BlackList readBlackList(UUID fromString) throws Exception;

	List<BlackList> getLists(boolean b) throws Exception;

	//List<HarvestReport> getAll();

}
