package dk.kb.webdanica.core.criteria;

import java.io.File;
import java.io.IOException;

import com.maxmind.geoip.Country;
import com.maxmind.geoip.LookupService;

public class GeoIPDatabase {
    private static GeoIPDatabase instance = null;
    private LookupService lookupService;
    
    public static final String GEOIP_FILE_KEY = "GEOIP_FILE";
    
    public GeoIPDatabase(File geoIPDatabase) throws IOException {
        this.lookupService = new LookupService(geoIPDatabase.getAbsolutePath());
    }
    
    public synchronized static GeoIPDatabase getInstance() throws IOException {
        if (instance == null) {
            if (System.getenv(GEOIP_FILE_KEY) != null){
                File geoIpDat = new File(System.getenv(GEOIP_FILE_KEY));
                instance = new GeoIPDatabase(geoIpDat);
            } else {
               throw new IOException("Env variable '" + GEOIP_FILE_KEY + "' is undefined");
            }
               
        }
        return instance;
    }
    
    public LookupService getLookupService() {
        return this.lookupService;
    }
    
    public Country getCountry(String IpAddress) {
        return lookupService.getCountry(IpAddress);
    }
    
    
}
