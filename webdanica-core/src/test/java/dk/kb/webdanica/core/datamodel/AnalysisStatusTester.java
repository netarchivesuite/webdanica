package dk.kb.webdanica.core.datamodel;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.Test;

public class AnalysisStatusTester {

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testFromOrdinal() {
		AnalysisStatus failed = AnalysisStatus.ANALYSIS_FAILED;
		AnalysisStatus success = AnalysisStatus.ANALYSIS_SUCCESSFUL;
		AnalysisStatus awaiting = AnalysisStatus.AWAITING_ANALYSIS;
		AnalysisStatus noAnalysis = AnalysisStatus.NO_ANALYSIS;
		AnalysisStatus unknown = AnalysisStatus.UNKNOWN_STATUS;
		assertTrue(AnalysisStatus.fromOrdinal(0).equals(awaiting));
		assertTrue(AnalysisStatus.fromOrdinal(1).equals(noAnalysis));
		assertTrue(AnalysisStatus.fromOrdinal(2).equals(success));
		assertTrue(AnalysisStatus.fromOrdinal(3).equals(failed));
		assertTrue(AnalysisStatus.fromOrdinal(4).equals(unknown));
	}

}
