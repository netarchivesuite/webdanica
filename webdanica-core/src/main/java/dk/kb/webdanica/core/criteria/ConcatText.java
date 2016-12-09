package dk.kb.webdanica.core.criteria;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.Tuple;

/**
 * Concatenate all relevant textfields in the parsed data to one text, 
 * and any available linktext. All textfields are separated by a newline.
 */
public class ConcatText extends EvalFunc<String> {

    String[] textfields = new String[] {"boiled", "keywords", "title", "subject",
            "content", "description"};
    
    String outlinks_field = "outlinks";
    File dir = new File("/home/svc/netarkivet.out");
    //private static int count = 0;
    
    @Override
    public String exec(Tuple input) throws IOException {
        if (input == null || input.size() == 0 || input.get(0) == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        //System.out.println("INPUTSIZE = " + input.size());
        
        Map m = (Map) input.get(0);
        //System.out.println("MAPSIZE = " + m.size());
        //String url = (String) m.get("url");
        for (String field: textfields) {
            if (m.containsKey(field)) {
                //System.out.println("add field - " + field);
                sb.append(m.get(field) + "\n");
            }
        }
        // Find the link text and add them to the concatenated text.
        if (m.containsKey(outlinks_field)) {
            //System.out.println("add field - " + outlinks_field);
            DataBag o = (DataBag) m.get(outlinks_field);
            //System.out.println(o.size());
            Iterator o1 = o.iterator();
            while (o1.hasNext()) {
                Tuple f = (Tuple) o1.next();
                Map m1 = (Map)f.get(0);
                if (m1.containsKey("text")) {
                    //System.out.println("add text: " + m1.get("text"));
                    sb.append(m1.get("text") + "\n");
                } else {
                    //System.out.println("No text found");
                }
            }   
        }
        
        //System.out.println("Concat-text results in text of size " + sb.length());
        //FileUtils.writeStringToFile(new File(dir, "" + count), url + "\n\n" + sb.toString());
        //count++;
        return sb.toString();
        
    }
}    

