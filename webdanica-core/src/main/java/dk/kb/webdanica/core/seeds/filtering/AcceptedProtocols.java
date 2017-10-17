package dk.kb.webdanica.core.seeds.filtering;

import java.net.URI;
import java.net.URISyntaxException;

import dk.kb.webdanica.core.WebdanicaSettings;
import dk.kb.webdanica.core.utils.Settings;

public class AcceptedProtocols {
    
    private static String[] protocols = Settings
            .getAll(WebdanicaSettings.ACCEPTED_PROTOCOLS);

    /**
     * Test whether a seed has a protocol that matches the list of accepted
     * protocols.
     * 
     * @param seed
     *            a given seed
     * @return the matched protocol, if it matches an accepted protocol, null
     *         otherwise (null is also returned if the schema is not found)
     */
    public static String matchesAcceptedProtocol(String seed) {
        String schema = getSchema(seed);
        if (schema != null) {
            for (String ign : protocols) {
                if (schema.equalsIgnoreCase(ign)) {
                    return ign;
                }
            }     
         }
        
        return null;
    }
    
    public static String schemaMatchesAcceptedProtocol(String schema) {
        if (schema != null) {
            for (String ign : protocols) {
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
     * @return the list read from settingsfile of the accepted protocols
     */
    public static String[] getAcceptedProtocols() {
        return protocols;
    }
}
