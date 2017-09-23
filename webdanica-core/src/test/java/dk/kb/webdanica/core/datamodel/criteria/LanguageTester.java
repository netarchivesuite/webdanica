package dk.kb.webdanica.core.datamodel.criteria;

import java.util.List;

import org.junit.Test;

import static org.junit.Assert.*;

public class LanguageTester {

    String testLanguages = "so: MEDIUM (0.714284)#br: MEDIUM (0.142858)#sl: MEDIUM (0.142857)";
    
    @Test
    public void testConstructor() {
        //"so: MEDIUM (0.714284)"
        Language l = new Language("so", 0.714284f);
        assertEquals("code should be 'so'", "so", l.getCode());
        assertEquals("confidence should be 0.714284f", 0.714284f, l.getConfidence(), 0.0f);
    }
    
    @Test
    public void testFindLanguages() {
        List<Language> languages = Language.findLanguages(testLanguages);
        assertNotNull("languages list returned should be not null", languages);
        assertFalse("languages list returned should be not empty", languages.isEmpty());
        assertTrue("languages list returned should have 3 elements", languages.size() == 3);
        Language L1 = languages.get(0); verifyLanguage(L1, "so", 0.714284f);
        Language L2 = languages.get(1); verifyLanguage(L2, "br", 0.142858f);
        Language L3 = languages.get(2); verifyLanguage(L3, "sl", 0.142857f);
    }

    public void verifyLanguage(Language l, String expectedCode, float expectedconfidence) {
        assertEquals("code should be '" + expectedCode + "'", expectedCode, l.getCode());
        assertEquals("confidence should be '" + expectedconfidence + "'", expectedconfidence, 
                l.getConfidence(), 0.0f);
    }
}
