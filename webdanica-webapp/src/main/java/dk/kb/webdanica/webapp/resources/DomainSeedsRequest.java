package dk.kb.webdanica.webapp.resources;

import dk.kb.webdanica.core.datamodel.DanicaStatus;
import dk.kb.webdanica.core.datamodel.Status;

/**
 * Request 1: "/domainseeds/$DOMAIN/" -   shows seeds for the given domain
 * Request 2: "/domainseeds/$DOMAIN/$STATUS/$DANICASTATUS/" - shows seeds for the given domain with the given state and given danicastate
 *  if STATUS = ALL, and DANICASTATUS = ALL, then request 2 is tantamount to the request 1.
 */
public class DomainSeedsRequest {
        
    private String domain; // should not be null. A specific domain to look at
    private Status status; // if not null, show only seeds for the domain with the given danicaStatus
    private DanicaStatus dStatus; // if not null, show only seeds for the domain with the given danicaStatus
    
    private String pathinfo; // The pathinfo in the request
    private boolean valid; // is this request valid

    /**
     * 
     * @param domain
     * @param pathinfo
     * @param status
     * @param dStatus
     * @param valid
     */
    public DomainSeedsRequest(String domain, String pathinfo, Status status, DanicaStatus dStatus, boolean valid) {
        this.domain = domain;
        this.status = status;
        this.dStatus = dStatus;
        this.pathinfo = pathinfo;
        this.valid = valid;
    }
    
    /**
     * Try to validate the request as a valid request, 
     * either /domainseeds/$DOMAIN/ (request 1) or 
     *        /domainseeds/$DOMAIN/$STATE/$DANICASTATE/ (request 2)
     * @param pathInfo
     * @return a DomainSeedsRequest
     */
    public static DomainSeedsRequest getDomainSeedsRequest(String pathInfo) {
        String[] split = pathInfo.split(DomainResource.DOMAIN_SEEDS_PATH);
        // dummy request = invalid request
        DomainSeedsRequest result = new DomainSeedsRequest(null, pathInfo, null, null, false);
        if (split.length == 2) {
            String arguments = split[1];
            String[] argumentParts = arguments.split("/");
            if (argumentParts.length == 1) {
                String domain = argumentParts[0];
                result = new DomainSeedsRequest(domain, pathInfo, null, null, true);
            } else if (argumentParts.length == 3) {
                String domain = argumentParts[0];
                Status status = getStatusFromString(argumentParts[1]);
                DanicaStatus dstatus = getDanicaStatusFromString(argumentParts[2]);
                result = new DomainSeedsRequest(domain, pathInfo, status, dstatus, true);
            }
        } 
        return result;
    }
    
    private static Status getStatusFromString(String status) {
        Status s = null;
        try {
            s = Status.valueOf(status);
        } catch (IllegalArgumentException e) {
            // ignore
        }
        return s;
    }

    private static DanicaStatus getDanicaStatusFromString(String dstatus) {
        DanicaStatus s = null;
        try {
            s = DanicaStatus.valueOf(dstatus);
        } catch (IllegalArgumentException e) {
            // ignore
        }
        return s;
    }
    
    public String getDomain() {
        return this.domain;
    }
    
    public Status getStatus() {
        return this.status;
    }
    
    public DanicaStatus getDanicaStatus() {
        return this.dStatus;
    }
    
        
    public boolean getValid() {
        return this.valid;
    }
    
    public String getPathInfo() {
        return this.pathinfo;
    }
}
