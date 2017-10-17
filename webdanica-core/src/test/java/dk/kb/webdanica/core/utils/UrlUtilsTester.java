package dk.kb.webdanica.core.utils;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.AfterClass;
import org.junit.Test;

import dk.kb.webdanica.core.datamodel.URL_REJECT_REASON;

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
	
	@Test
	public void testIsRejectableURL() {
	    File webdanicaSettingsFile = UnitTestUtils.getTestResourceFile("webdanica_settings.xml");
	    System.setProperty("webdanica.settings.file", webdanicaSettingsFile.getAbsolutePath());
        Settings.reload();
        
	    String androidAppUrl = "android-app://com.google.android.apps.plus/https/plus.google.com/+metroxpress";
	    URL_REJECT_REASON rejectreason = UrlUtils.isRejectableURL(androidAppUrl);
	    assertEquals("Should be BAD_SCHEME, but was: " + rejectreason, rejectreason, URL_REJECT_REASON.BAD_SCHEME);
	    String normalUrl = "http://www.familien-carlsen.dk/";
	    rejectreason = UrlUtils.isRejectableURL(normalUrl);
	    assertEquals("Should be NONE, but was: " + rejectreason, rejectreason, URL_REJECT_REASON.NONE);
	}
}
