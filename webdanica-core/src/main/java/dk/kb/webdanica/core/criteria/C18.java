package dk.kb.webdanica.core.criteria;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;

import com.maxmind.geoip.Country;

import dk.kb.webdanica.core.utils.Constants;

/**
 *  Databases can be downloaded here:
 *  http://dev.maxmind.com/geoip/legacy/geolite/
 *  https://github.com/maxmind/geoip-api-java/releases
 *  and IPv4 country list. Maybe also IPv6 country list
 *  What about ASN lists?
 */
public class C18 extends EvalFunc<String> {
    
    public static final String GEOIP_FILE_KEY = "GEOIP_FILE";

    @Override
    public String exec(Tuple input) throws IOException {
        if (input == null || input.size() == 0 || input.get(0) == null) {
            return Constants.getCriteriaName(this) + ": " + Constants.NODATA;
        }

        String hostname = (String) input.get(0);
        GeoIPDatabase geop = null;
        try {
            geop = GeoIPDatabase.getInstance();
        } catch (Throwable e) {
            return Constants.getCriteriaName(this) + ": failed with reason: " + e.getMessage();
        }

        try {
            boolean belongs = hostbelongstoDanishIP(geop, hostname);
            return (belongs?"C18: y":"C18: n");
        } catch (Throwable e) {
            e.printStackTrace();
            return Constants.getCriteriaName(this) + ": failed";
        }
    } 
    /**
     * Test program for testing functionality.
     * Tests country for hostname www.kb.dk (true), and www.vatican.va (false)
     * @param args not used.
     * @throws IOException 
     * @throws UnknownHostException 
     */
    public static void main(String[] args) throws UnknownHostException, IOException {
        String testHostname = "www.kb.dk";
        File geoIpFile = new File("/home/svc/GeoIP.dat");
        GeoIPDatabase geop = new GeoIPDatabase(geoIpFile);
        System.out.println(hostbelongstoDanishIP(geop, testHostname));
        System.out.println(hostbelongstoDanishIP(geop, "www.vatican.va"));
        
        if (System.getProperty(GEOIP_FILE_KEY) != null){
            System.out.println("GEOIP FILE: " + System.getProperty(GEOIP_FILE_KEY));
        } else {
            System.out.println("GEOIP FILE undefined");
        }
        
    }
    
    
    public static synchronized boolean hostbelongstoDanishIP(GeoIPDatabase geop, String hostname) throws IOException, UnknownHostException {
        // Get IP for hostname
        // Assume not, if UnknownHostException
        
        /* InetAddress address = InetAddress.getByName(hostname);
        // Assume we have a String ipAddress (in dot-decimal form).
        String IpAddress = address.getHostAddress(); 
        */

        Country country = geop.getCountry(hostname);
        return country.getCode().equalsIgnoreCase("DK");
    }
}
