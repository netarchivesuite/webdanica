package dk.kb.webdanica.core.utils;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.Test;

public class UrlUtilsTester {

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testGetInfo() {
		String bbcUrl = "http://www.bbc.co.uk/subpage/";
		String netarkivetUrl = "http://netarkivet.dk/";
		String familiencarlsenUrl = "http://www.familien-carlsen.dk/";
		UrlInfo bbcUrlInfo= UrlUtils.getInfo(bbcUrl);
		UrlInfo netarkivetUrlInfo= UrlUtils.getInfo(netarkivetUrl);
		UrlInfo familiencarlsenUrlInfo = UrlUtils.getInfo(familiencarlsenUrl);
		// Check tld
		assertEquals("Should be co.uk, but was: " + bbcUrlInfo.getTld(), "co.uk", bbcUrlInfo.getTld());
		assertEquals("Should be dk, but was: " + familiencarlsenUrlInfo.getTld(), "dk", familiencarlsenUrlInfo.getTld());
		assertEquals("Should be dk, but was: " + netarkivetUrlInfo.getTld(), "dk", netarkivetUrlInfo.getTld());	
	}

}
