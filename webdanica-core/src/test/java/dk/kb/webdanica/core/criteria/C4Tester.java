package dk.kb.webdanica.core.criteria;

import static org.junit.Assert.*;
import static dk.kb.webdanica.core.utils.UnitTestUtils.getTestResourceFileAsString;

import java.io.IOException;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import dk.kb.webdanica.core.criteria.C4;
import dk.kb.webdanica.core.datamodel.criteria.CalcDanishCode;
import dk.kb.webdanica.core.datamodel.criteria.SingleCriteriaResult;

public class C4Tester {

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
	public void testComputeNewC4() throws IOException {
	    if (true) {
	        return;
	    }
		//String sampleLangTextFilePath = getTestResourceFileAsString("language-samples/sample-text.txt");
		String sampleLangTextFilePath = getTestResourceFileAsString("language-samples/sample-text.da");
		List<String> results = C4.computeNewC4(sampleLangTextFilePath);
		String c4a = results.get(0);
		String c4b = results.get(1);
		assertEquals(c4a, "da");
		assertEquals(c4b, "da: HIGH (0.999995)");	
	}
	
	@Test
	public void testCheckForDanishCode4() {
		String Languages_da = "da: HIGH (0.999996)";
		String Languages_en = "en: HIGH (0.999995)";
		String LanguagesMixt = "da: MEDIUM (0.714284)#en: MEDIUM (0.285713)";
		SingleCriteriaResult scr = new SingleCriteriaResult();
		assertTrue(CalcDanishCode.checkForDanishCode4(scr, Languages_da));
		assertTrue(scr.calcDanishCode == 4);
		assertTrue(scr.intDanish == 1.0F);
		scr = new SingleCriteriaResult();
		assertFalse(CalcDanishCode.checkForDanishCode4(scr, Languages_en));
		assertTrue(scr.calcDanishCode != 4);
		assertTrue(scr.intDanish != 1.0F);
		scr = new SingleCriteriaResult();
		assertFalse(CalcDanishCode.checkForDanishCode4(scr, LanguagesMixt));
		assertTrue(scr.calcDanishCode != 4);
		assertTrue(scr.intDanish != 1.0F);
	}  
	

}
