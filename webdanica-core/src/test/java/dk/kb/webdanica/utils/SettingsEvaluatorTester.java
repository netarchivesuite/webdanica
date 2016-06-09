package dk.kb.webdanica.utils;

import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SettingsEvaluatorTester {

	@Test
	public void testValidSettingsFile() {
		File good_webdanicaSettingsFile = UnitTestUtils.getTestResourceFile("settings.xml");
		if (!SettingsEvaluator.isValidSimpleXmlSettingsFile(good_webdanicaSettingsFile)) {
			fail("The settingsfile '" +  good_webdanicaSettingsFile.getAbsolutePath() 
					+ "' should have been considered valid, but wasn't");
		}
		if (!SettingsEvaluator.isValidSimpleXmlSettingsFile(good_webdanicaSettingsFile, true)) {
			fail("The settingsfile '" +  good_webdanicaSettingsFile.getAbsolutePath() 
					+ "' should have been considered valid, but wasn't");
		}
		if (!SettingsEvaluator.isValidSimpleXmlSettingsFile(good_webdanicaSettingsFile, false)) {
			fail("The settingsfile '" +  good_webdanicaSettingsFile.getAbsolutePath() 
					+ "' should have been considered valid, but wasn't");
		}
	}	
	
	@Test
	public void testInvalidSettingsFile() {
		File bad_webdanicaSettingsFile = UnitTestUtils.getTestResourceFile("bad_settings.xml");
		if (SettingsEvaluator.isValidSimpleXmlSettingsFile(bad_webdanicaSettingsFile)) {
			fail("The settingsfile '" +  bad_webdanicaSettingsFile.getAbsolutePath() 
					+ "' should have been considered invalid, but wasn't");
		}
		if (SettingsEvaluator.isValidSimpleXmlSettingsFile(bad_webdanicaSettingsFile, true)) {
			fail("The settingsfile '" +  bad_webdanicaSettingsFile.getAbsolutePath() 
					+ "' should have been considered invalid, but wasn't");
		}
		if (SettingsEvaluator.isValidSimpleXmlSettingsFile(bad_webdanicaSettingsFile, false)) {
			fail("The settingsfile '" +  bad_webdanicaSettingsFile.getAbsolutePath() 
					+ "' should have been considered invalid, but wasn't");
		}
	}	

}
