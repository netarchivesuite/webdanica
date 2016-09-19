package dk.kb.webdanica.datamodel;

import java.util.List;

public interface IngestLogDAO {

	public void insertLog(IngestLog log);
	
	public List<Long> getIngestDates();
	
	public IngestLog readIngestLog(Long timestamp);

}
