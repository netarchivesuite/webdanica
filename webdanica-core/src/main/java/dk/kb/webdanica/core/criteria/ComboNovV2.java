package dk.kb.webdanica.core.criteria;

import java.io.IOException;
import java.util.Set;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;

import dk.kb.webdanica.core.utils.Constants;
import dk.kb.webdanica.core.utils.TextUtils;


/** 
 * Combo criteria runner.
 * url = tuple[0}
 * timestamp = tuple[1]
 * text= tuple[2]
 * links     = tuple[3]
 * hostname = tuple[4]
 * 
 */
public class ComboNovV2 extends EvalFunc<String> {
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
        int Cext1 =  text.length();
        result += url + ", Cext3:" + timestamp;
        if (Cext1 > 0) {  
            int bytesLength = text.getBytes().length; 
            Float percent = new Float(bytesLength*100) / new Float(text.length());
            if (percent.intValue() < 200) {
                boolean C2b = C2b_from_a_Nov.computeC2bV2(text);
                result += ", C2b: " + (C2b? "y": "n");
	            Set<String> C3gmatches = C3g_fom_b_Nov.computeC3gV2(text);
	            result += addResultForCriterie("C3g", C3gmatches);
	            Set<String> C6dmatches = C6d_from_a_Nov.ComputeC6dV2(text);
	            result += addResultForCriterie("C6d", C6dmatches);
                Set<String> C7gmatches = C7g_from_a_Nov.computeC7gV2(text);
                result += addResultForCriterie("C7g", C7gmatches);
                Set<String> C7hmatches = C7h_from_e_Nov.computeC7hV2(text);
                result += addResultForCriterie("C7h", C7hmatches);
                Set<String> C8cmatches = C8c_from_a_Nov.computeC8cV2(text);
                result += addResultForCriterie("C8c", C8cmatches);
                Set<String> C9ematches = C9e_from_b_Nov.computeC9eV2(text);
                result += addResultForCriterie("C9e", C9ematches);
                boolean C9f = C9f_from_d_Nov.computeC9fV2(text);
                result += ", C9f: " + (C9f? "y": "n");
                Set<String> C10cmatches = C10c_from_b_Nov.computeC10cV2(text);
                result += addResultForCriterie("C10c", C10cmatches);
	        }
        }
        return result;
    }
    
    private String addResultForCriterie(String criteria, Set<String> matches) {
        return ", " + criteria + ": " + matches.size() + " " 
                + TextUtils.conjoin("#", matches);
    }
}

