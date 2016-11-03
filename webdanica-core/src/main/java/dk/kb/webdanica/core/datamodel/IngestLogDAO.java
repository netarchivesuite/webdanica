package dk.kb.webdanica.core.datamodel;

import java.util.List;

public interface IngestLogDAO {

	public boolean insertLog(IngestLog log) throws Exception;
	
	public List<Long> getIngestDates() throws Exception;
	
	public IngestLog readIngestLog(Long timestamp) throws Exception;

	public void close();
}
