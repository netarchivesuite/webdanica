package dk.kb.webdanica.core.datamodel;

import static org.junit.Assert.*;

import org.junit.Test;

public class SeedTester {

    @Test
    /**
     * Test constructor Seed(String url).
     */
    public void testConstructorOne() {
        String testUrl = "http://netarkivet.dk/";
        Seed s = new Seed(testUrl);
    }
    
    @Test
    /**
     * Test constructor Seed(url, redirectedUrl, hostname, domain, tld, insertedTime, updatedTime, danicastate, state, 
     * stateReason, exported, exportedTime, danicaStatusReason);
     */
    public void testConstructorTwo() {
      //s = new Seed(url, redirectedUrl, hostname, domain, tld, insertedTime, updatedTime, danicastate, state, stateReason, exported, exportedTime, danicaStatusReason);s = new Seed(url, redirectedUrl, hostname, domain, tld, insertedTime, updatedTime, danicastate, state, stateReason, exported, exportedTime, danicaStatusReason);
    }

}
