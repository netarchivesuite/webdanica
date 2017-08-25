package dk.kb.webdanica.core.criteria;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import dk.kb.webdanica.core.datamodel.criteria.CalcDanishCode;
import dk.kb.webdanica.core.datamodel.criteria.Codes;
import dk.kb.webdanica.core.datamodel.criteria.Codes.Category;
import dk.kb.webdanica.core.datamodel.criteria.CodesResult.Display;
import dk.kb.webdanica.core.datamodel.criteria.CodesResult.Level;

public class DanishCodeTester {

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

	@Test 
	public void testNewCuratorLikelyDanishCodes() {
	// have introduced new Curatorcodes
	for (int code=400; code<= 414; code++) {
	    assertTrue("code " + code + " should belong to  Category.LIKELY_DK", 
	            Codes.getCategory(code) == Category.LIKELY_DK);
	    String desc = CalcDanishCode.getCalcDkCodeText(code, Display.noCodes, Level.none, false);
	    assertTrue("code " + code + " should have adequate description", desc != null && !desc.isEmpty());
	    //System.out.println(desc);
	    
	    }
	}
}
