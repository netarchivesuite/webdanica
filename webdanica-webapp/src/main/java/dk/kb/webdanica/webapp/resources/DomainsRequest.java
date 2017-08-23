package dk.kb.webdanica.webapp.resources;

import dk.kb.webdanica.core.datamodel.DanicaStatus;
import dk.kb.webdanica.core.exceptions.WebdanicaException;
import dk.kb.webdanica.core.utils.UnitTestUtils;

/**
 * "/domains/" lists all TLD domains registered -  points to a number of /domains/TLD/ pages
 * "/domains/$TLD/" lists all known domains belonging to the given TLD -  points to domain pages
 * "/domains/$TLD/danicastate/" lists all known domains belonging to the given TLD with the given danicastate
 */
public class DomainsRequest {
        
    private String tld; // if not null, show domains from a  specific tld
    private DanicaStatus dState; // if not null, show domains with a specific DanicaStatus 
    private String pathinfo; // The pathinfo in the request
    private boolean valid; // is this request valid

    public DomainsRequest(String tld, DanicaStatus dState, String pathinfo, boolean valid) {
        this.tld = tld;
        this.dState = dState;
        this.pathinfo = pathinfo;
        this.valid = valid;
    }

    public static void main(String[] args) {
        String pathInfo = "/domains/";
        String[] split = pathInfo.split(DomainResource.DOMAIN_LIST_PATH);
        UnitTestUtils.describeArray(split);
    }
    
    
    
    /**
     * Try to validate the request as a valid /domains/ request:
     * /domains/" lists all TLD domains registered -  points to a number of /domains/TLD/ pages
     * "/domains/$TLD/" lists all known domains belonging to the given TLD -  points to domain pages
     * "/domains/$TLD/danicastate/" lists all known domains belonging to the given TLD with the given danicastate
     * @param pathInfo
     * @return
     */
    public static DomainsRequest getDomainsRequest(String pathInfo) {
        String pattern = DomainResource.DOMAIN_LIST_PATH;
        if (pathInfo.equals(pattern)) { // default request
            return new DomainsRequest(null, null, pathInfo, true);
        }
        // Dummy invalid request 
        DomainsRequest requestFound = new DomainsRequest(null, null, pathInfo, false);
        String[] split = pathInfo.split(pattern);
        if (split.length > 1) { // Should be true, if valid request
            String arguments = split[1];
            String[] argumentParts = arguments.split("/");
            if (argumentParts.length > 2) {
                return requestFound;
            } else if (argumentParts.length == 2) { // matching/tld/danicastate
                String tld = argumentParts[0];
                boolean valid = true;
                DanicaStatus dStatus = null;
                try {
                    dStatus = DanicaStatus.fromOrdinal(Integer.valueOf(argumentParts[1]));
                } catch (IllegalArgumentException e) {
                    valid = false;
                } catch (WebdanicaException e) {
                    valid = false;
                }
                requestFound = new DomainsRequest(tld, dStatus, pathInfo, valid);
            } else {
                String tld = argumentParts[0];
                requestFound = new DomainsRequest(tld, null, pathInfo, true);
            }
        }
        return requestFound;
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
    
    public String getTld() {
        return this.tld;
    }
    
    public boolean getValid() {
        return this.valid;
    }
    
    public String getPathInfo() {
        return this.pathinfo;
    }
    
    public DanicaStatus getDanicaState() {
        return this.dState;
    }
    
}
