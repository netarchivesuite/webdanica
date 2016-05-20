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
	private long linecount;
	private long insertedcount;
	private long rejectedcount;
	private long duplicatecount;
	
	public IngestLog(List<String> entries, String filename, long linecount, long insertedcount, long rejectedcount, long duplicatecount) {
		this.logEntries = entries;
		this.filename = filename;
		this.linecount= linecount;
		this.insertedcount = insertedcount;
		this.rejectedcount = rejectedcount;
		this.duplicatecount= duplicatecount;
	}
	
	public IngestLog(List<String> entries, String filename, Date insertedDate, long linecount, long insertedcount, long rejectedcount, long duplicatecount) {
		this.filename = filename;
		this.logEntries = entries;
		this.insertedDate = insertedDate;
		this.linecount= linecount;
		this.insertedcount = insertedcount;
		this.rejectedcount = rejectedcount;
		this.duplicatecount= duplicatecount;
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

	public long getLinecount() {
	    return linecount;
    }

		public long getInsertedcount() {
	    return insertedcount;
    }

	public long getRejectedcount() {
	    return rejectedcount;
    }

	
	public long getDuplicatecount() {
	    return duplicatecount;
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
