package dk.kb.webdanica.webapp;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.Test;

import dk.kb.webdanica.core.datamodel.DanicaStatus;
import dk.kb.webdanica.webapp.resources.DomainsRequest;
import dk.kb.webdanica.webapp.resources.DomainRequest;
import dk.kb.webdanica.webapp.resources.SeedRequest;
import dk.kb.webdanica.webapp.resources.SeedsResource;

public class ResourceUtilsTester {

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testGetUrlFromPathinfo() {
		String samplePath = "/seed/aHR0cDovL3Jpc2FnZXIuaW5mby8=/";
		SeedRequest sr = SeedRequest.getUrlFromPathinfo(samplePath, 
				SeedsResource.SEED_PATH);
		assertNotNull(sr.getUrl());
		assertNull(sr.getNewState());
	}

	/**
     * Try to validate the request as a valid /domains/ request:
     * /domains/" lists all TLD domains registered -  points to a number of /domains/TLD/ pages
     * "/domains/$TLD/" lists all known domains belonging to the given TLD -  points to domain pages
     * "/domains/$TLD/danicastate/" lists all known domains belonging to the given TLD with the given danicastate
     */
	@Test 
    public void testGetDomainsRequest() {
        String invalidPathInfo = "/domains";
        String invalidShowAllDomainsFromTldWithBadDstate = "/domains/se/27/";
        String validShowAllTlds = "/domains/";
        String validShowAllDomainsFromTld = "/domains/se/";
        String validShowAllDomainsFromTldWithDstate = "/domains/se/0/";
        
        DomainsRequest dr = DomainsRequest.getDomainsRequest(invalidPathInfo);
        assertFalse(invalidPathInfo + " should not be valid", dr.getValid());
        dr = DomainsRequest.getDomainsRequest(invalidShowAllDomainsFromTldWithBadDstate);
        assertFalse(invalidShowAllDomainsFromTldWithBadDstate + " should not be valid"
                , dr.getValid());
        dr = DomainsRequest.getDomainsRequest(validShowAllTlds);
        assertTrue(validShowAllTlds + " should be valid",  dr.getValid());
        
        dr = DomainsRequest.getDomainsRequest(validShowAllDomainsFromTld);
        assertTrue(validShowAllDomainsFromTld + " should be valid",  dr.getValid());
        dr = DomainsRequest.getDomainsRequest(validShowAllDomainsFromTldWithDstate);
        assertTrue(validShowAllDomainsFromTldWithDstate + " should be valid",dr.getValid());
        assertTrue(dr.getDanicaState().equals(DanicaStatus.UNDECIDED));
    }
    
	/**
     * Try to validate the request as a valid /domain/ request:
     * /domain/$DOMAIN" show the domain in question
     */
    @Test 
    public void testGetDomainRequest() {
        String invalidPathInfo = "/domain";
        String validShowDomain = "/domain/kb.dk/";
        String invalidShowDomainsWithExtraArgument = "/domain/kb.dk/ggg/";
        
        DomainRequest dr = DomainRequest.getDomainRequest(invalidPathInfo);
        assertFalse(invalidPathInfo + " should not be valid", dr.getValid());
        dr = DomainRequest.getDomainRequest(invalidShowDomainsWithExtraArgument);
        assertFalse(invalidShowDomainsWithExtraArgument + " should not be valid", dr.getValid());
        dr = DomainRequest.getDomainRequest(validShowDomain);
        assertTrue(validShowDomain + " should be valid", dr.getValid());
        assertTrue(dr.getDomain().equals("kb.dk"));
        assertTrue(dr.getPathInfo().equals(validShowDomain));
    }
	
	
	
	
}
