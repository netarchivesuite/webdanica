package dk.kb.webdanica.criteria;

import java.io.IOException;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;

/**
 * UDF to filter away stylesheets (.css), robots.txt, and .js (javascript) files
 * Returns true if the url does not match any of these formats.
 */
public class IsNotRobotsTxt extends EvalFunc<Boolean> {

    @Override
    public Boolean exec(Tuple input) throws IOException {
        if (input == null || input.size() == 0 || input.get(0) == null) {
            return false;
        }
        String url = ((String) input.get(0)).toLowerCase();
        Boolean ignoreFile = false;
        
        if (url.endsWith("robots.txt") || 
                url.endsWith(".css") ||
                url.endsWith(".js")) {
            ignoreFile = true;
        }
        
        return !ignoreFile;
    }
}
