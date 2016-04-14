package dk.kb.webdanica.criteria;

import java.io.File;
import java.io.IOException;

import org.apache.pig.EvalFunc;
import org.apache.pig.PigWarning;
import org.apache.pig.data.Tuple;

import dk.kb.webdanica.utils.Constants;

/** 
 * C16. 
 * See if the url is matched by any outlink from a current .dk snapshot harvest.
 * This list is implemented as a Berkeley DB JE database.
 */
public class C16 extends EvalFunc<String>{
    
    public static final String LinkDatabaseHomeKey = "LINKDATABASE_HOME";
    
    @Override
    public String exec(Tuple input) throws IOException {
        if (input == null || input.size() == 0 || input.get(0) == null) {
            return Constants.getCriteriaName(this) + ": " + Constants.NODATA;
        }
        String url = (String) input.get(0);
        LinksBase lb = null;
        File basedir = null;
        if (System.getenv(LinkDatabaseHomeKey) != null){
            basedir = new File(System.getenv(LinkDatabaseHomeKey));
        } else {
            String errMsg = "The property '" + LinkDatabaseHomeKey + "' is undefined. Skipping function";
            System.err.println(errMsg);
            warn(errMsg, PigWarning.UDF_WARNING_1);
            return null;
        }
        if (!basedir.exists()) {
            String errMsg = "The property '" + LinkDatabaseHomeKey + "' refers to unknown directory '"
                    + basedir.getAbsolutePath() + "'";
            System.err.println(errMsg);
            warn(errMsg, PigWarning.UDF_WARNING_1);
            return null;
        }
        
        try {
            lb = new LinksBase(basedir);
        } catch (Exception e) {
            warn("Starting Linksbase in directory '" + basedir.getAbsolutePath() + "' failed " + e, PigWarning.UDF_WARNING_1);
            return "C16: failed";
        }
        
        try {
            if (lb.hasUrl(url)) {
                return "C16: " + lb.getFrequency(url); 
            } else {
                return "C16: " + "0";
            }
        } catch (Exception e) {
            warn("Looking url in database '" + basedir.getAbsolutePath() + "' failed " + e, PigWarning.UDF_WARNING_1);
            return "C16: failed";
        }
    }

    public static int computeC16(String url) {
        
        return 0;
    }
    
}
