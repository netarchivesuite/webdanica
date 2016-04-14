package dk.kb.webdanica.criteria;

import java.io.IOException;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;

/**
 * UDF to filter away for records that have status code 404.
 * returns true, if the record is not a 404 record
 * TODO: rename to Found  
 *
 */
public class NotFound extends EvalFunc<Boolean> {

    @Override
    public Boolean exec(Tuple input) throws IOException {
        if (input == null || input.size() == 0 || input.get(0) == null) {
            return false;
        }
        String code = ((String) input.get(0)).toLowerCase();
        Boolean ignoreFile = false;
        
        if (code.equalsIgnoreCase("404")) {
            ignoreFile = true;
        }
        
        return !ignoreFile;
    }
}
