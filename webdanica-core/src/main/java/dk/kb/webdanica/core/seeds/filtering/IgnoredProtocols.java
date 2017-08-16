package dk.kb.webdanica.core.seeds.filtering;

import java.net.URI;
import java.net.URISyntaxException;

import dk.kb.webdanica.core.WebdanicaSettings;
import dk.kb.webdanica.core.utils.Settings;

public class IgnoredProtocols {
    
    private static String[] ignoredProtocols = Settings
            .getAll(WebdanicaSettings.IGNORED_PROTOCOLS);

    /**
     * Test whether a seed has a protocol that matches the list of ignored
     * protocols.
     * 
     * @param seed
     *            a given seed
     * @return the matched protocol, if it matches an ignored protocol, null
     *         otherwise (null is also returned if the schema is not found)
     */
    public static String matchesIgnoredProtocol(String seed) {
        String schema = getSchema(seed);
        if (schema != null) {
            for (String ign : ignoredProtocols) {
                if (schema.equalsIgnoreCase(ign)) {
                    return ign;
                }
            }     
         }
        
        return null;
    }
    
    public static String schemaMatchesIgnoredProtocol(String schema) {
        if (schema != null) {
            for (String ign : ignoredProtocols) {
                if (schema.equalsIgnoreCase(ign)) {
                    return ign;
                }
            }     
         }
        
        return null;
    }
    

    private static String getSchema(String seed) {
        String schema = null;
        try {
            URI url = new URI(seed);
            schema = url.getScheme();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
       
        return schema;
    }

    /**
     * @return the list read from settingsfile of the ignored Suffixes
     */
    public static String[] getIgnoredProtocols() {
        return ignoredProtocols;
    }
}
