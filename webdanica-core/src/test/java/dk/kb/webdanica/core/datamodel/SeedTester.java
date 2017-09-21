package dk.kb.webdanica.core.datamodel;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Test;

public class SeedTester {

    @Test
    /**
     * Test constructor Seed(String url).
     */
    public void testDefaultConstructor() {
        String testUrl = "http://netarkivet.dk/";
        Seed s = new Seed(testUrl);
        assertNotNull("Constructor should return non null Seed Object", s);
        assertEquals("Should return correct url", testUrl, s.getUrl());
        assertEquals("Should return correct tld", "dk", s.getTld());
        assertEquals("Should return correct domain", "netarkivet.dk", s.getDomain());
        assertEquals("Should return correct hostname", "netarkivet.dk", s.getHostname());
        assertEquals("Should return danicastatus UNDECIDED", DanicaStatus.UNDECIDED, s.getDanicaStatus());
        assertTrue("Should return empty danicastatusreason", s.getDanicaStatusReason().isEmpty());
        assertFalse("Should return false exportedstate", s.getExportedState());
        assertEquals("Should return status NEW", Status.NEW, s.getStatus());
        assertTrue("Should return empty statusreason", s.getStatusReason().isEmpty());
        assertNull("RedirectedUrl should be null", s.getRedirectedUrl());
        assertNull("InsertedTime should be null", s.getInsertedTime());
        assertNull("UpdatedTime should be null", s.getUpdatedTime());
    }
    @Test
    public void testSetters() {
        String testUrl = "http://netarkivet.dk/";
        Seed s = new Seed(testUrl);
        // test setDanicaStatus
        assertEquals("Should return danicastatus UNDECIDED", DanicaStatus.UNDECIDED, s.getDanicaStatus());
        s.setDanicaStatus(DanicaStatus.YES);
        assertEquals("Should return danicastatus YES after calling setDanicaStatus", DanicaStatus.YES, 
                s.getDanicaStatus());
        // test setDanicaStatusReason
        assertTrue("Should return empty danicastatusreason", s.getDanicaStatusReason().isEmpty());
        String reason = "Seed is decided to be danica by curators";
        s.setDanicaStatusReason(reason);
        assertTrue("Should return new danicastatusreason", s.getDanicaStatusReason().equals(reason));
        //test s.setExportedState(b) AND s.setExportedTime(time);
        assertTrue("exportedState should be false and exportedTime null before", 
                s.getExportedState()== false && s.getExportedTime() == null);
        Long newTime = System.currentTimeMillis();
        s.setExportedState(true);
        s.setExportedTime(newTime);
        assertTrue("exportedState should be true and exportedTime not null after", 
                s.getExportedState()== true && s.getExportedTime() != null);
        assertTrue("exportedTime should be equal to newtime: " + newTime, s.getExportedTime().equals(newTime));
        //test s.setRedirectedUrl(redirectedUrl);
        assertNull("redirectedUrl should be url before setting it explicitly", s.getRedirectedUrl());
        String dummyRedirectedUrl = "http://netarkivet.dk/dummyRedirectedLink";
        s.setRedirectedUrl(dummyRedirectedUrl);
        assertTrue("redirectedUrl should equal '" + dummyRedirectedUrl + "'", 
                s.getRedirectedUrl().equals(dummyRedirectedUrl)); 
        //test s.setStatus(newStatus);
        assertEquals("Should return status NEW before setting it explicitly", Status.NEW, s.getStatus());
        assertTrue("Should return empty statusreason ", s.getStatusReason().isEmpty());
        s.setStatus(Status.ANALYSIS_COMPLETED);
        assertEquals("Should return status ANALYSIS_COMPLETED after setting it explicitly", 
                    Status.ANALYSIS_COMPLETED, s.getStatus());
        assertTrue("Should still return empty statusreason ", s.getStatusReason().isEmpty());
        //test s.setStatusReason(newReason);
        String newReason = "Seed now analysed completely";
        s.setStatusReason(newReason);
        assertTrue("Should return statusreason '" + newReason + "'", s.getStatusReason().equals(newReason));
        assertEquals("Should still return status ANALYSIS_COMPLETED after setting the statusReason explicitly", 
                Status.ANALYSIS_COMPLETED, s.getStatus());
    }
    
    @Test
    public void testShowExportedState() {
        String testUrl = "http://netarkivet.dk/";
        Seed s = new Seed(testUrl);
        assertFalse("Should return false exportedstate after construction", s.getExportedState());
        assertTrue("showExportedState should return " + Seed.NOT_YET_EXPORTED_STRING, 
                s.showExportedState().equals(Seed.NOT_YET_EXPORTED_STRING));
        s.setExportedState(true);
        assertTrue("showExportedState should return " + Seed.EXPORTED_BUT_EXPORTEDTIME_UNKNOWN_STRING, 
                s.showExportedState().equals(Seed.EXPORTED_BUT_EXPORTEDTIME_UNKNOWN_STRING));
        Long exportedTime = System.currentTimeMillis();
        s.setExportedTime(exportedTime);
        String dateString = new Date(exportedTime).toString();
        String expectedOutput = "The seed was exported at: " + dateString;
        assertEquals("Should report expected export time", expectedOutput, s.showExportedState());   
    }
    
    
    @Test
    /**
     * Test constructor Seed(url, redirectedUrl, hostname, domain, tld, insertedTime, updatedTime, danicastate, state, 
     * stateReason, exported, exportedTime, danicaStatusReason);
     */
    public void testDatabaseConstructor() {
      Long exportedTimeNow = System.currentTimeMillis();
      Long insertedTime = exportedTimeNow - 100000L;
      String url = "http://netarkivet.dk";
      String redirectedUrl = "https://netarkivet.dk/";
      String hostname = "netarkivet.dk";
      String tld = "dk";
      String domain = "netarkivet.dk";
      Long updatedTime = exportedTimeNow;
      DanicaStatus danicastate = DanicaStatus.PROBABLE;
      Status state = Status.AWAITS_CURATOR_DECISION;
      String stateReason = "some reason for having this state";
      boolean exported = true;
      Long exportedTime = exportedTimeNow;
      String danicaStatusReason = "some reason for having this danicastate";
      Seed s = new Seed(url, redirectedUrl, hostname, domain, tld, insertedTime, updatedTime, 
              danicastate, state, stateReason, exported, exportedTime, danicaStatusReason);
      assertNotNull("Constructor should return non null Seed Object", s);
      assertEquals("Should return correct url", url, s.getUrl());
      assertEquals("Should return correct tld", tld, s.getTld());
      assertEquals("Should return correct domain", domain, s.getDomain());
      assertEquals("Should return correct hostname", hostname, s.getHostname());
      assertEquals("Should return danicastatus PROBABLE", DanicaStatus.PROBABLE, s.getDanicaStatus());
      assertTrue("Should return correct danicastatusreason", s.getDanicaStatusReason().equals(danicaStatusReason));
      assertTrue("Should return true exportedstate", s.getExportedState());
      assertEquals("Should return status AWAITS_CURATOR_DECISION;", Status.AWAITS_CURATOR_DECISION, s.getStatus());
      assertTrue("Should return corrrect statusreason", s.getStatusReason().equals(stateReason));
      assertTrue("RedirectedUrl should be correct", s.getRedirectedUrl().equals(redirectedUrl));
      assertTrue("InsertedTime should be correct", s.getInsertedTime().equals(insertedTime));
      assertTrue("UpdatedTime should be correct", s.getUpdatedTime().equals(updatedTime));      
    }

}
