package dk.kb.webdanica.core.seeds.filtering;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import dk.kb.webdanica.core.seeds.filtering.IgnoredSuffixes;
import dk.kb.webdanica.core.utils.Settings;
import dk.kb.webdanica.core.utils.UnitTestUtils;

@RunWith(JUnit4.class)
public class IgnoredSuffixesTester {
	
	private static final String urlToIgnore = "http://www.familien-carlsen.dk/pania-de-croce.jpg";
	private static final String urlToIgnore1 = "https://vice-images.vice.com/images/series/crops/2015/11/13/desastre-em-mariana-1447437601-crop_social.jpg?resize=*:*&output-quality=75";
	private static final String urlToIgnore2 = "https://visitanalytics.userreport.com/hit.gif?t=BOS087a815b";
	private static final String urlToAccept = "http://www.familien-carlsen.dk/checkLodder.html";	
	private static final String VALID_WEBANICA_SETTINGS_FILENAME = "settings.xml";
	
	
	@BeforeClass
	public static void init() {
		File good_webdanicaSettingsFile = UnitTestUtils.getTestResourceFile(VALID_WEBANICA_SETTINGS_FILENAME);
		System.setProperty(Settings.SETTINGS_FILE_PROPERTY,	 good_webdanicaSettingsFile.getAbsolutePath());
		Settings.reload();
	}
	@Test
	public void testIgnore() {
		String result = IgnoredSuffixes.matchesIgnoredExtension(urlToIgnore);
		if (result == null) {
			fail("The url '" + urlToIgnore + "' should have been ignored but wasn't");
		}
		result = IgnoredSuffixes.matchesIgnoredExtension(urlToIgnore1);
		if (result == null) {
			fail("The url '" + urlToIgnore1 + "' should have been ignored but wasn't");
		}
		
		result = IgnoredSuffixes.matchesIgnoredExtension(urlToIgnore2);
		if (result == null) {
			fail("The url '" + urlToIgnore2 + "' should have been ignored but wasn't");
		}
	}
	@Test
	public void testAccept() {
		String result = IgnoredSuffixes.matchesIgnoredExtension(urlToAccept);
		if (result != null) {
			fail("The url '" + urlToAccept + "' should not have been ignored but was");
		}
	}
	
	@Test
	public void testGetIgnoredSuffixes() {
		String[] result = IgnoredSuffixes.getIgnoredSuffixes();
		if (result == null || result.length == 0) {
			fail("The list of ignoredSuffixes should not have been null");
		}
	}

}
