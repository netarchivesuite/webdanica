package dk.kb.webdanica.core.utils;

public class XmlUtils {

    /**
     * Replace invalid xml characters with their ENTITY counterparts.
     * See https://sbforge.org/jira/browse/WEBDAN-288
     * @param inputString A given inputString
     * @return the string cleaned of invalid xml characters
     */
    public static String cleanString(String inputString) {
        String cleanedSeed = inputString;
        cleanedSeed = cleanedSeed.replace("&", "&amp;");
        cleanedSeed = cleanedSeed.replace("\"", "&quot;");
        cleanedSeed = cleanedSeed.replace("\'", "&apos;");
        cleanedSeed = cleanedSeed.replace("<", "&lt;");
        cleanedSeed = cleanedSeed.replace(">", "&gt;");
        return cleanedSeed;
    }

}
