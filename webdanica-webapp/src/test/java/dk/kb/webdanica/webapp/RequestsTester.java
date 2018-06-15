package dk.kb.webdanica.webapp;

import static org.junit.Assert.*;

import org.junit.Test;

import dk.kb.webdanica.core.datamodel.DanicaStatus;
import dk.kb.webdanica.core.datamodel.Status;
import dk.kb.webdanica.webapp.resources.DomainSeedsRequest;

/** 
 * Unittests for the Request classes in the dk.kb.webdanica.webapp.resources package
 *
 */
public class RequestsTester {

    @Test
    public void testDomainSeedsRequest() {
        /* correct 
         * /domainseeds/$DOMAIN/" -   shows seeds for the given domain
         * /domainseeds/$DOMAIN/$STATUS/$DANICASTATUS/
         */
        String input = "/domainseeds/DOMAIN/";
        DomainSeedsRequest dsr = DomainSeedsRequest.getDomainSeedsRequest(input);
        assertTrue(dsr.getDomain().equals("DOMAIN"));
        assertTrue(dsr.getPathInfo().equals(input));
        assertTrue(dsr.getStatus() == null);
        assertTrue(dsr.getDanicaStatus() == null);
        assertTrue(dsr.getValid());
        
        input = "/domainseeds/";
        dsr = DomainSeedsRequest.getDomainSeedsRequest(input);
        assertFalse(dsr.getValid());
        
        input = "/domainseeds/DOMAIN/ALL/ALL";
        dsr = DomainSeedsRequest.getDomainSeedsRequest(input);
        assertTrue(dsr.getStatus() == null);
        assertTrue(dsr.getDanicaStatus() == null);
        assertTrue(dsr.getDomain().equals("DOMAIN"));
        assertTrue(dsr.getValid());
        
        // INVALID
        input = "/domainseeds/DOMAIN/ALL/ALL/ALL";
        dsr = DomainSeedsRequest.getDomainSeedsRequest(input);
        assertFalse(dsr.getValid());
        
        input = "/domainseeds/DOMAIN/DONE/YES/"; // Status = Done, DanicaStatus= YES
        dsr = DomainSeedsRequest.getDomainSeedsRequest(input);
        assertTrue(dsr.getValid());
        assertTrue(dsr.getStatus() == Status.DONE);
        assertTrue(dsr.getDanicaStatus() == DanicaStatus.YES);
        assertTrue(dsr.getDomain().equals("DOMAIN"));
    }

}
