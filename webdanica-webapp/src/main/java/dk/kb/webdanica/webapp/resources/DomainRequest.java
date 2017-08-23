package dk.kb.webdanica.webapp.resources;

import dk.kb.webdanica.core.utils.UnitTestUtils;

/**
 * "/domain/$DOMAIN/" -   shows details for the given domain
 */
public class DomainRequest {
        
    private String domain; // if not null, show a specific domain
    private String pathinfo; // The pathinfo in the request
    private boolean valid; // is this request valid

    public DomainRequest(String domain, String pathinfo, boolean valid) {
        this.domain = domain;
        this.pathinfo = pathinfo;
        this.valid = valid;
    }

    public static void main(String[] args) {
        String pathInfo = "/domains/";
        String[] split = pathInfo.split(DomainResource.DOMAIN_LIST_PATH);
        UnitTestUtils.describeArray(split);
    }
    
    /**
     * Try to validate the request as a valid /domain/$DOMAIN/ request
     * @param pathInfo
     * @return
     */
    public static DomainRequest getDomainRequest(String pathInfo) {
        String[] split = pathInfo.split(DomainResource.DOMAIN_PATH);
        // dummy request = invalid request
        DomainRequest resultKeys = new DomainRequest(null, pathInfo, false);
        if (split.length == 2) {
            String arguments = split[1];
            String[] argumentParts = arguments.split("/");
            if (argumentParts.length == 1) {
                String domain = argumentParts[0];
                resultKeys = new DomainRequest(domain, pathInfo, true);
            }
        }
        return resultKeys;
    }
    
    
    
    public String getDomain() {
        return this.domain;
    }
        
    public boolean getValid() {
        return this.valid;
    }
    
    public String getPathInfo() {
        return this.pathinfo;
    }
}
