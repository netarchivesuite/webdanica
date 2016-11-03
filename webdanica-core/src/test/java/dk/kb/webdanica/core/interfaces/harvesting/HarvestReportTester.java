package dk.kb.webdanica.core.interfaces.harvesting;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

import dk.kb.webdanica.core.interfaces.harvesting.HarvestReport;
import dk.kb.webdanica.core.utils.UnitTestUtils;

public class HarvestReportTester {

	@Test
	public void testPrintToFile() throws IOException {
		String path = "reported_domains_20.txt-harvestlog-1473676568727.txt";
		File f = UnitTestUtils.getTestResourceFile("reported_domains_20.txt-harvestlog-1473676568727.txt");
		if (f == null) {
			fail("Unable to find '" +  path + "' in resources");
		}
		List<HarvestReport> reports = HarvestReport.readHarvestLog(f);
		File outputFile = File.createTempFile("out", ".txt", new File("/tmp/"));
		HarvestReport.printToFile(reports, outputFile);
		System.out.println("Results printed to: " + outputFile.getAbsolutePath());
	}	
}
