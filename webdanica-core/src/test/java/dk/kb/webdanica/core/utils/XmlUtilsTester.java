package dk.kb.webdanica.core.utils;

import static org.junit.Assert.*;

import org.junit.Test;

import dk.kb.webdanica.core.interfaces.harvesting.CrawlertrapsUtils;


public class XmlUtilsTester {

    @Test
    public void testWEBDAN288() { 
        
        // test that the XmlUtils.cleanString(String) returns a valid String that can be embedded in a XML file
        String seed = "https://twitter.com/intent/tweet?via=dmidk&url=http://beta.dmi.dk/en/hav/groenland-og-arktis/iskort/oestgroenland/";
        boolean valid = CrawlertrapsUtils.isCrawlertrapsWellformedXML(seed);
        // require that seed is invalid before manipulation
        if (valid) {
            fail("seed should not be valid before manipulation");
        }
        String cleaned = XmlUtils.cleanString(seed);
        valid = CrawlertrapsUtils.isCrawlertrapsWellformedXML(cleaned);
        if (!valid) {
            fail("seed should be valid after manipulation");
        }
    }

}
