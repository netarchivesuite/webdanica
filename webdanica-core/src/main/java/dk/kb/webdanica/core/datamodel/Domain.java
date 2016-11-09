package dk.kb.webdanica.core.datamodel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dk.kb.webdanica.core.utils.UrlUtils;

public class Domain {
	
	private long updatedTime;
	private List<String> danicaParts;
	private String tld;
	private String statusReason;
	private String domain;
	private String notes;
	private DanicaStatus status;

	public Domain(String domain, String notes, DanicaStatus status,
            Long updatedTime, String statusReason, String tld,
            List<String> danicaParts) {
		this.domain = domain;
		this.notes = notes;
		this.status = status;
		this.statusReason = statusReason;
		this.tld = tld;
		this.danicaParts = danicaParts;
		this.updatedTime = updatedTime;
    }
	
	public static Domain createNewAcceptedDomain(String domain) {
		String tld = UrlUtils.findTld(domain);
		String notes = "[" +  new Date() + "]: Domain accepted as Danica domain by user";
		String danicaReason = "Domain accepted as Danica domain by user";
		List<String> parts = new ArrayList<String>();
		return new Domain(domain, notes, DanicaStatus.YES, null, danicaReason, tld, parts);
	}
	
	public static Domain createNewUndecidedDomain(String domain) {
		String tld = UrlUtils.findTld(domain);
		String danicaReason = "Domain is ingested with danicastatus undecided";
		List<String> parts = new ArrayList<String>();
		String notes = "[" +  new Date() + "]: Domain inserted into table domains with status UNDECIDED";
		return new Domain(domain, notes, DanicaStatus.UNDECIDED, null, danicaReason, tld, parts);
	}
	

	public String getDomain() {
	   return domain;
    }

	public Long getUpdatedTime() {
	    return updatedTime;
    }

	public DanicaStatus getDanicaStatus() {
	    return status;
    }

	public String getDanicaStatusReason() {
		return statusReason;
	}

	public String getTld() {
		return tld;
	}

	public List<String> getDanicaParts() {
		return this.danicaParts;
	}
	public String getNotes() {
		return notes;
	}

	public void setNotes(String newnotes) {
		this.notes = newnotes;
	    
    }

	public void setDanicaStatus(DanicaStatus newstate) {
	    this.status = newstate;    
    }
	
	public void setDanicaStatusReason(String newReason) {
		this.statusReason = newReason;
	}
	
}
