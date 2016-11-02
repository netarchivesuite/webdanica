package dk.kb.webdanica.webapp;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import dk.kb.webdanica.webapp.resources.CriteriaResultResource;
import dk.kb.webdanica.webapp.resources.CriteriaResultResource.CriteriaKeys;

public class CriteriaResultResourceTester {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	public void testCriteriaKeys() {
		String path = "/criteriaresult/webdanica-trial-1470218036468/http%3A%2F%2Fgmpg.org%2Fxfn%2F11/";
		String[] parts = path.split(CriteriaResultResource.CRITERIA_RESULT_PATH);
		CriteriaKeys CK = CriteriaResultResource.getCriteriaKeys(path);
		//assertFalse("CK should not be null", CK == null);
		//System.out.println()
	}

}
