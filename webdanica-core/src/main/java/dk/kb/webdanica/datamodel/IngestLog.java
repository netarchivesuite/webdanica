package dk.kb.webdanica.datamodel;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
//https://datastax.github.io/java-driver/manual/#cql-to-java-type-mapping

public class IngestLog {

	private Date insertedDate;
	private String filename;
	private List<String> logEntries;
	
	public IngestLog(List<String> entries, String filename) {
		this.logEntries = entries;
		this.filename = filename;
	}
	
	public IngestLog(List<String> entries, String filename, Date insertedDate) {
		this.filename = filename;
		this.logEntries = entries;
		this.insertedDate = insertedDate;
	}
	
	public String getFilename() {
	    return this.filename;
    }
	
	public Date getDate() {
	    return insertedDate;
    }
	
	public List<String> getLogEntries() {
		return logEntries;
	}
	
/*
	
	public ByteBuffer getBlob {
		StringBuilder sb = new StringBuilder();
		sb.append(logEntry);
		ByteBuffer bb = ByteBuffer.allocate(sb.capacity());
		
		bb.put(sb.toString().getBytes(Charset.forName("UTF8")));
	    return bb;
    }
	
	*/
}
