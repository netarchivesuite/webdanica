package dk.kb.webdanica.webapp.resources;

import dk.kb.webdanica.core.datamodel.DanicaStatus;

/**
 * "/domainseeds/$DOMAIN/" -   shows seeds for the given domain
 * "/domainseeds/$DOMAIN/$DANICASTATUS" -   shows seeds for the given domain with the given danicastate
 */
public class DomainSeedsRequest {
        
    private String domain; // should not be null. A specific domain to look at
    private DanicaStatus dStatus; // if not null, show only seeds for the domain with the given danicaStatus
    private String pathinfo; // The pathinfo in the request
    private boolean valid; // is this request valid

    public DomainSeedsRequest(String domain, String pathinfo, DanicaStatus dStatus, boolean valid) {
        this.domain = domain;
        this.dStatus = dStatus;
        this.pathinfo = pathinfo;
        this.valid = valid;
    }
    
    /**
     * Try to validate the request as a valid /domainseeds/$DOMAIN/$DANICASTATE request
     * @param pathInfo
     * @return
     */
    public static DomainSeedsRequest getDomainSeedsRequest(String pathInfo) {
        String[] split = pathInfo.split(DomainResource.DOMAIN_SEEDS_PATH);
        // dummy request = invalid request
        DomainSeedsRequest resultKeys = new DomainSeedsRequest(null, pathInfo, null, false);
        if (split.length == 2) {
            String arguments = split[1];
            String[] argumentParts = arguments.split("/");
            if (argumentParts.length == 1) {
                String domain = argumentParts[0];
                resultKeys = new DomainSeedsRequest(domain, pathInfo, null, true);
            } else {
                String domain = argumentParts[0];
                DanicaStatus status = DanicaStatus.valueOf(argumentParts[1]);
                resultKeys = new DomainSeedsRequest(domain, pathInfo, status, true);
            }
        }
        return resultKeys;
    }
    
    
    
    public String getDomain() {
        return this.domain;
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
