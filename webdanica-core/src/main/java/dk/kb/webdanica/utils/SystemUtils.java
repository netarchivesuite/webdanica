package dk.kb.webdanica.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

/**
 * Provide some general System Utilities.
 */
public class SystemUtils {

    /** Logging mechanism. */
    private static final Logger LOG = Logger.getLogger(SystemUtils.class.getName());

    /**
    * Provide the hostname of the machine on which the program is running.
    * @return the hostname as a {@link String}
    * Borrowed from Yggdrasil code base
    */
    public static String getHostName() throws UnknownHostException {
        String hostName;
        try {
            //Trying to get hostname through InetAddress
            InetAddress iAddress = InetAddress.getLocalHost();
            hostName = iAddress.getHostName();

            //Trying to do better and get Canonical hostname
            String canonicalHostName = iAddress.getCanonicalHostName();         
            hostName = canonicalHostName;

            if (StringUtils.isNotEmpty(hostName)) {
                LOG.info("Hostname provided  by iAddress: " + hostName);
                return hostName;
            }

        } catch (UnknownHostException  e) {
            // Failed the standard Java way, trying alternative ways.
        }

        // Trying to get hostname through environment properties.
        //      
        hostName = System.getenv("COMPUTERNAME");
        if (hostName != null) {
            LOG.info("Hostname provided by System.getenv COMPUTERNAME: " + hostName);
            return hostName;
        }
        hostName = System.getenv("HOSTNAME");
        if (hostName != null) {
            LOG.info("Hostname provided by System.getenv HOSTNAME: " + hostName);
            return hostName;
        }
        // Nothing worked, hostname undetermined.
        LOG.warning("Hostname undetermined");
        throw new UnknownHostException("Hostname undetermined");
    }
}
