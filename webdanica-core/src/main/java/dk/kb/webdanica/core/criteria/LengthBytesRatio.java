package dk.kb.webdanica.core.criteria;

import java.io.IOException;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;

/**
 * UDF to show the ratio between bytes used to represent the text, 
 * and the length of the string. An attempt to filter away 
 * chinese/japanese pages away. 
 */
public class LengthBytesRatio extends EvalFunc<String> {

    @Override
    public String exec(Tuple input) throws IOException {
        if (input == null || input.size() == 0 || input.get(0) == null) {
            return "LengthBytesRatio: nodata";
        }
        String text = (String) input.get(0);
        int bytesLength = text.getBytes().length; 
        //return "LengthBytesRatio: " + text.length() + "/" + bytesLength;
        Float percent = new Float(bytesLength*100) / new Float(text.length());
        return "LengthBytesRatio: " + percent.toString();
    }
}
