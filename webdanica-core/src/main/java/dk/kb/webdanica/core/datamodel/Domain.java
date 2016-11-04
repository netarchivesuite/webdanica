package dk.kb.webdanica.core.datamodel;

import java.sql.Timestamp;
import java.util.List;

public class Domain {
	
	private long updatedTime;
	private List<String> danicaParts;
	private String tld;
	private String statusReason;
	private String domain;
	private String notes;
	private DanicaStatus status;

	public Domain(String domain, String notes, DanicaStatus status,
            Timestamp updatedTime, String statusReason, String tld,
            List<String> danicaParts) {
		this.domain = domain;
		this.notes = notes;
		this.status = status;
		this.statusReason = statusReason;
		this.tld = tld;
		this.danicaParts = danicaParts;
		this.updatedTime = updatedTime.getTime();
		
    }

	public String getDomain() {
	   return domain;
    }

	public Long getUpdatedTime() {
	    return updatedTime;
    }

	public Enum<DanicaStatus> getDanicaStatus() {
	    return status;
    }

	public String getDanicaStatusReason() {
		return statusReason;
	}

	public String getTld() {
		return tld;
	}

}
