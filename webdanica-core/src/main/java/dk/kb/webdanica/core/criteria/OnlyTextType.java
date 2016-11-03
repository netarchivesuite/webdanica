package dk.kb.webdanica.core.criteria;

import java.io.IOException;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;

/**
 * 
 * The idea was to filter away non-text.
 * This has been done, already, it seems.
 * So this class is @deprecated
 *
 */
public class OnlyTextType extends EvalFunc<Boolean> {

    @Override
    public Boolean exec(Tuple input) throws IOException {
        if (input == null || input.size() == 0 || input.get(0) == null) {
            return false;
        }
        String code = ((String) input.get(0)).toLowerCase();
        Boolean ignoreFile = false;
        
        if (code.startsWith("text")) {
            ignoreFile = true;
        }
        
        return !ignoreFile;
    }
}
