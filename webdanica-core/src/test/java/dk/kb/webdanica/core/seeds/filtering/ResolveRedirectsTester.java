package dk.kb.webdanica.core.seeds.filtering;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import dk.kb.webdanica.core.seeds.filtering.ResolveRedirects;

public class ResolveRedirectsTester {
	
	String WGET_PATH;
	File wgetPath;
	File tmpFolder;
	int delayInSecs;
	int tries;
	
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	   // todo define some settings
	   // Test that wget exists
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		WGET_PATH = "/usr/bin/wget"; // TODO read from settings
		wgetPath = new File(WGET_PATH);
		tmpFolder = new File("/tmp");
		delayInSecs=0;
		tries = 1;
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		String testUrl = "http://t.co/LDWqmtDM"; 
		String expectedRedirectResult = "https://wiki.ubuntu.com/UbuntuOpenWeek [following]";

		// change this to unittest
		ResolveRedirects rr = new ResolveRedirects(wgetPath, delayInSecs, tries, tmpFolder);
		String result = rr.resolveRedirectedUrl(testUrl);
		if (!result.equals(expectedRedirectResult)) {
			fail("The testUrl '" + testUrl + "' should have resolved to '" + expectedRedirectResult 
					+ "' but resolved to: '" + result + "'");
		}

	}
}
