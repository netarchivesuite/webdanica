package dk.kb.webdanica.core.utils;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.AfterClass;
import org.junit.Test;

public class TextUtilsTester {

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testTokenizeUrl() {
		String url = "https://www.example.com/new%20pricing.htm";
		String url0 = "www.example.com/new%20pricing.htm";
		Set<String> tokens = TextUtils.tokenizeUrl(url, false);
		//UnitTestUtils.printStringSet(tokens, "tokens");
		Set<String> tokens1 = TextUtils.tokenizeUrl(url0, false);
		//UnitTestUtils.printStringSet(tokens1, "tokens1");
		assertTrue(tokens.containsAll(tokens1));
		assertTrue(tokens.size() == 6);
	}
	
}
