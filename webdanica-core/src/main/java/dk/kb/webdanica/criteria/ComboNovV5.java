package dk.kb.webdanica.criteria;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;

import dk.kb.webdanica.utils.Constants;
import dk.kb.webdanica.utils.TextUtils;


/** 
 * Combo criteria runner.
 * url = tuple[0}
 * timestamp = tuple[1]
 * text= tuple[2]
 * links     = tuple[3]
 * hostname = tuple[4]
 * 
 * String result produced by ComboNovV5.exec:
 * 
 * URL, Cext1: sizeOfInput, Cext3=timestamp  
 * followed by comma-separated results for (if Cext1 > 0):
 * Cext2 - Include Asian Symbols test
 * if Cext2 < 200 (normal text):
 * 
 * C2b
 * C3g
 * C6d
 * C7g
 * C7h
 * C8c
 * C9e
 * C9f
 * C10c
 */
public class ComboNovV5 extends EvalFunc<String> {
    @Override
    public String exec(Tuple input) throws IOException {
        if (input == null || input.size() == 0 || input.get(0) == null) {
            return Constants.getCriteriaName(this) + ": " + Constants.NODATA;
        }

        String result = "";
        String url = (String) input.get(0);
        String timestamp = (String) input.get(1);
        String text = (String) input.get(2);
        
        //System.out.println("TUPLE-length: " + input.size());

        //calc Cext1 '- Size of web-page'
        Set<String> tokens = TextUtils.tokenizeText(text);
        int Cext1 =  text.length();
        result += url + ", Cext3:" + timestamp;
        if (Cext1 > 0) {  
            int bytesLength = text.getBytes().length; 
            Float percent = new Float(bytesLength*100) / new Float(text.length());
            if (percent.intValue() < 200) {
                boolean C2b = C2b_from_a_Nov.computeC2b(text);
                result += ", C2b: " + (C2b? "y": "n");
                Set<String> c3tokens = copyTokens(tokens);
	            Set<String> C3gmatches = C3g_fom_b_Nov.computeC3gV5(c3tokens);
	            result += addResultForCriterie("C3g", C3gmatches);
                Set<String> c6tokens = copyTokens(tokens);
	            Set<String> C6dmatches = C6d_from_a_Nov.computeC6dV5(c6tokens);
	            result += addResultForCriterie("C6d", C6dmatches);
                Set<String> c7gtokens = copyTokens(tokens);
                Set<String> C7gmatches = C7g_from_a_Nov.computeC7gV5(c7gtokens);
                result += addResultForCriterie("C7g", C7gmatches);
                Set<String> c7htokens = copyTokens(tokens);
                Set<String> C7hmatches = C7h_from_e_Nov.computeC7hV5(c7htokens);
                result += addResultForCriterie("C7h", C7hmatches);
                Set<String> c8tokens = copyTokens(tokens);
                Set<String> C8cmatches = C8c_from_a_Nov.computeC8cV5(c8tokens);
                result += addResultForCriterie("C8c", C8cmatches);
                Set<String> c9tokens = copyTokens(tokens);
                Set<String> C9ematches = C9e_from_b_Nov.computeC9eV5(c9tokens);
                result += addResultForCriterie("C9e", C9ematches);
                boolean C9f = C9f_from_d_Nov.computeC9f(text);
                result += ", C9f: " + (C9f? "y": "n");
                Set<String> c10tokens = copyTokens(tokens);
                Set<String> C10cmatches = C10c_from_b_Nov.computeC10cV5(c10tokens);
                result += addResultForCriterie("C10c", C10cmatches);
	        }
        }
        return result;
    }

    private Set<String> copyTokens(Set<String> tokens) {
    	Set<String> res = new HashSet<String>();
    	res.addAll(tokens);
        return res;
    }
    
    private String addResultForCriterie(String criteria, Set<String> matches) {
        return ", " + criteria + ": " + matches.size() + " " 
                + TextUtils.conjoin("#", matches);
    }
}

