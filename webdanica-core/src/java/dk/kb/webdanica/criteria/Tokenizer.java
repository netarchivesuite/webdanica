package dk.kb.webdanica.criteria;

import java.io.IOException;
import java.util.Set;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;

import dk.kb.webdanica.utils.Constants;
import dk.kb.webdanica.utils.TextUtils;

public class Tokenizer extends EvalFunc<Tuple> {

    public Tuple exec(Tuple input) throws IOException {
        Tuple result;
        if (input == null || input.size() == 0 || input.get(0) == null) {
            result = TupleFactory.getInstance().newTuple();
            result.append(Constants.getCriteriaName(this) + ": " + Constants.NODATA);
            return result;
        }
        String text = (String) input.get(0);
        Set<String> tokens = TextUtils.tokenizeText(text);

        return TupleFactory.getInstance().newTuple(tokens);
    }
}