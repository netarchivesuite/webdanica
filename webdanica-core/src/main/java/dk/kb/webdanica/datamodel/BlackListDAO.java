package dk.kb.webdanica.datamodel;

import java.util.List;
import java.util.UUID;

public interface BlackListDAO {

	BlackList readBlackList(UUID fromString);

	List<BlackList> getLists(boolean b);

	//List<HarvestReport> getAll();

}
