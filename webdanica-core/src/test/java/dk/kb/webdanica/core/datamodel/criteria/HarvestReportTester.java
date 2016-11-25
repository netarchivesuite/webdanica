package dk.kb.webdanica.core.datamodel.criteria;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Test;

import dk.kb.webdanica.core.interfaces.harvesting.HarvestLog;
import dk.kb.webdanica.core.utils.UnitTestUtils;

public class HarvestReportTester {

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testFindPartFiles() throws IOException {
		String pathNo = "no-criteria-results";
		String pathYes = "criteria-results/criteria-test-09-08-2016/09-08-2016-1470760002/10-11-20160503111920187-00000-dia-prod-udv-01.kb.dk.warc.gz";
		File fileNo = UnitTestUtils.getTestResourceFile(pathNo);
		File fileYes = UnitTestUtils.getTestResourceFile(pathYes);
		File fileNull = null;
		List<String> partfiles = HarvestLog.findPartFiles(fileYes);
		if (partfiles == null || partfiles.isEmpty()) {
			fail("Should have a partfile in the resultdir " +  pathYes);
		}
		partfiles = HarvestLog.findPartFiles(fileNo);
		if (partfiles == null) {
			fail("Should not return null partfiles from the resultdir " +  pathNo);
		}
		if (!partfiles.isEmpty()) {
			fail("Should have returned en empty list of partfiles from the resultdir " +  pathNo);
		}
		partfiles = HarvestLog.findPartFiles(fileNull);
		
		if (partfiles != null) {
			fail("Should have returned a null list of partfiles from the resultdir " +  fileNull);
		}
	}

}