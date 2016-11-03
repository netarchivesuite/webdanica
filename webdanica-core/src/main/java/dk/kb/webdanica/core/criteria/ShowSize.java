package dk.kb.webdanica.core.criteria;

import java.io.IOException;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;

/**
 * UDF to show the size of the text extract, we use for our
 * criteria analyses based on the text.
 *
 */
public class ShowSize extends EvalFunc<String> {

    @Override
    public String exec(Tuple input) throws IOException {
        if (input == null || input.size() == 0 || input.get(0) == null) {
            return "Size: nodata";
        }
        String text = (String) input.get(0);
        return "Size: " + text.length();
    }
}
