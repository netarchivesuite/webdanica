package dk.kb.webdanica.criteria;

import java.io.IOException;
import java.util.Set;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;

import dk.kb.webdanica.utils.Constants;
import dk.kb.webdanica.utils.TextUtils;


public class C6b extends EvalFunc<String>{

    @Override
    public String exec(Tuple input) throws IOException {

        if (input == null || input.size() == 0 || input.get(0) == null) {
            return Constants.getCriteriaName(this) + ": " + Constants.NODATA;
        }
        String text = (String)input.get(0);
        // // FIXME Should be SearchWord, but this does not work.
        // Using SearchPattern
        Set<String> foundMatches = ComputeC6b(text);

        /*
        Set<String> foundMatches1 = new HashSet<String>();
        Set<String> tokens = TextUtils.tokenizeText(text);
        System.out.println("Number of tokens:" + tokens.size());

        for (String word: new String[] {"dansk","danmark", "forening" }) {

            for (String token: tokens) {
                System.out.println("token: '" + token + "'");
                if (token.matches(word)) {
                    foundMatches1.add(word);
                    break;
                }
            }
        }

        if (foundMatches.size() != foundMatches1.size()) {
            System.out.println("c6b- pattern search gav: " + foundMatches.size());
            System.out.println("c6b- token search gav: " + foundMatches1.size());

        } */


        String res = (foundMatches.size() != 0)? 
                ("C6b: " + TextUtils.conjoin("#", foundMatches)) : "C6b: emptylist";
        return res;

    }

    public static Set<String> ComputeC6b(String text) {
        return TextUtils.SearchPattern(
                text, new String[] {"dansk","danmark", "forening" });
    }    

}
