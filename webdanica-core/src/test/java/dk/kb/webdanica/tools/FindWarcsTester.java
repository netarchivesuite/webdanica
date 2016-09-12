package dk.kb.webdanica.tools;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import dk.kb.webdanica.utils.UnitTestUtils;

@RunWith(JUnit4.class)
public class FindWarcsTester {
	
	@Test
	public void testFindFilenames() throws IOException {
		String path = "reported_domains_20.txt-harvestlog-1473676568727.txt";
		File f = UnitTestUtils.getTestResourceFile("reported_domains_20.txt-harvestlog-1473676568727.txt");
		if (f == null) {
			fail("Unable to find '" +  path + "' in resources");
		}
		Set<String> filenames= FindHarvestWarcs.getFilenames(f);
		assertFalse("'null' string should be part of set", filenames.contains("null"));
		
		assertTrue("Should be 17 warc.gz files but was " + filenames.size(), filenames.size() == 17);
		/*
		List<HarvestReport> reports = HarvestReport.readHarvestLog(f);
		for (HarvestReport h: reports) {
			System.out.println(h.error);
		}*/
	}
	
}
