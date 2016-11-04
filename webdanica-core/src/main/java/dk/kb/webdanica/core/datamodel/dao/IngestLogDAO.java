package dk.kb.webdanica.core.datamodel.dao;

import java.util.List;

import dk.kb.webdanica.core.datamodel.IngestLog;

public interface IngestLogDAO {

	public boolean insertLog(IngestLog log) throws Exception;
	
	public List<Long> getIngestDates() throws Exception;
	
	public IngestLog readIngestLog(Long timestamp) throws Exception;

	public void close();
}
