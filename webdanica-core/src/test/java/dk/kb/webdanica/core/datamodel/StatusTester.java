package dk.kb.webdanica.core.datamodel;

import static org.junit.Assert.*;

import java.util.Locale;

import org.junit.Test;

import dk.kb.webdanica.core.datamodel.Status;
import dk.kb.webdanica.core.exceptions.WebdanicaException;
import dk.netarkivet.common.utils.I18n;

public class StatusTester {

	@Test
	public void testWebdanicaResourceBundle() {
		I18n i18n = new I18n(dk.kb.webdanica.core.Constants.WEBDANICA_TRANSLATION_BUNDLE);
		Locale locDA = new Locale("da");
		Locale locEN = new Locale("en");
		
		String label = Status.getInternationalizationHeaderLabel(Status.NEW.ordinal());
		
		// Note: we trim the results to ignore trailing whitespace in the translation files;
		String translationDA = i18n.getString(locDA, label).trim(); 
		assertEquals(translationDA, "Oprettet");
		String translationEN = i18n.getString(locEN, label).trim();
		assertEquals(translationEN, "Created");
		
		
		
	}
	@Test
	public void testI18nLabelsExist() {
		for (int i=0; i <= Status.getMaxValidOrdinal(); i++) {
			assertFalse(Status.getInternationalizationHeaderLabel(i) == null);
			assertFalse(Status.getInternationalizationHeaderLabel(i).isEmpty());
			assertFalse(Status.getInternationalizationDescriptionLabel(i) == null);
			assertFalse(Status.getInternationalizationDescriptionLabel(i).isEmpty());
		}
		
		try {
			Status.getInternationalizationHeaderLabel(Status.getMaxValidOrdinal() + 1);
			fail("Should have thrown a WebdanicaException on invalid status ordinal");
		} catch(WebdanicaException e) {
			// Expected
		}
		try {
			Status.getInternationalizationDescriptionLabel(Status.getMaxValidOrdinal() + 1);
			fail("Should have thrown a WebdanicaException on invalid status ordinal");
		} catch(WebdanicaException e) {
			// Expected
		}
	}
	
	@Test
	public void testFromOrdinal() {
		for (int i=0; i <= Status.getMaxValidOrdinal(); i++) {
			Status s= Status.fromOrdinal(i);
			assertTrue(s.ordinal() == i);
		}
		try {
			Status.fromOrdinal(Status.getMaxValidOrdinal() + 1);
			fail("Should have thrown a WebdanicaException on invalid status ordinal");
		} catch(WebdanicaException e) {
			// Expected
		}
	}
	
}
