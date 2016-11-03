package dk.kb.webdanica.core.hadoop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.tika.language.LanguageIdentifier;
import org.json.JSONObject;


/** JSON library hentet og bygget herfra: 
 * 
 * https://github.com/eskatos/org.json-java
 * 
 * Tips til at bygge hadoop-projekter:
 * http://tikalk.com/build-your-first-hadoop-project-maven
 * 
 * http://stackoverflow.com/questions/16825821/parsing-json-input-in-hadoop-java
 *
 *
 */
public class HadoopSequenceFileReader {
    /**
     * http://itellity.wordpress.com/2013/05/27/xerces-parse-error-with-hadoop-or-solr-feature-httpapache-orgxmlfeaturesxinclude-is-not-recognized/
     * 
     * Code uses hadoop core 0.20.1
     * Extra libs needed: xalan / xerces and
     * -Djavax.xml.parsers.DocumentBuilderFactory=com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl
     * 
     * @param args full path to a sequence file
     * /home/svc/IA-WIDE-005-sample/hadoop/WIDE-20120807190915-00327.warc.gz
     * 
     * IA sequencefile-format: 
     *  key: url + sha1-digest
     *  value: json w/ content 
     * 
     * 
kint-value #1: boiled
kint-value #2: code
kint-value #3: content
kint-value #4: date
kint-value #5: description
kint-value #6: digest
kint-value #7: errorMessage
kint-value #8: keywords
kint-value #9: length
kint-value #10: outlinks

kint-value #11: pdf.Author
kint-value #12: pdf.CreationDate
kint-value #13: pdf.Creator
kint-value #14: pdf.Encrypted
kint-value #15: pdf.File size
kint-value #16: pdf.ModDate
kint-value #17: pdf.Optimized
kint-value #18: pdf.PDF version
kint-value #19: pdf.Page size
kint-value #20: pdf.Pages
kint-value #21: pdf.Producer
kint-value #22: pdf.Tagged

kint-value #23: robots
kint-value #24: status
kint-value #25: subject
kint-value #26: title
kint-value #27: type
kint-value #28: url

     *  
     *  
     *  
     *  
     *  
     * @throws IOException 
     */
    public static Set<String> jsonkeys;
    
    public static void main(String[] args) throws IOException {
       if (args.length < 1) {
           System.err.println("Missing path to sequence file");
           System.exit(1);
       }
       jsonkeys = new TreeSet<String>();
       File file = new File(args[0]);
       File outputFile = new File(file.getParentFile(), file.getName() + "-extracted-text.txt");
       OutputStream os = new FileOutputStream(outputFile);
       System.err.println("Writing output to: " + outputFile.getAbsolutePath());
       String header = "Extracted text from file: " + file.getAbsolutePath() + "\n";  
       os.write(header.getBytes(Charset.forName("UTF-8")));
       os.write("\n".getBytes(Charset.forName("UTF-8")));
       Configuration conf = new Configuration();
       FileSystem fs = FileSystem.getLocal(conf);
       Path seqFilePath = new Path(file.getAbsolutePath());
       SequenceFile.Reader reader = new SequenceFile.Reader(fs, seqFilePath, conf);
       //System.out.println(reader.getKeyClassName());
       Text key = new Text();
       Text val = new Text();
       long entryNumber = 0;
 
       while (reader.next(key, val)) {
           String keyString = "Key: " + key.toString();
           os.write(keyString.getBytes(Charset.forName("UTF-8")));
           os.write("\n".getBytes(Charset.forName("UTF-8")));
           entryNumber++;
           //System.out.println("entry #" + entryNumber + " has key = '" + key + "'");
           JSONObject jsn = new JSONObject(val.toString());
           
           //if (jsn.has("status")) { // has error: ignore record
           //    continue;
           //}
           println(jsn, os);
           
       }
       reader.close();
       String entriesString = "Json Entries read: " + entryNumber + " from  file " +  file.getAbsolutePath();
       System.out.println(entriesString);
       os.write(entriesString.getBytes(Charset.forName("UTF-8")));
       os.write("\n".getBytes(Charset.forName("UTF-8")));
    }
     
       
       
       /*
       System.out.println("Found " +  jsonkeys.size() + " distinct keys");
       int kint=0;
       for (String k: jsonkeys) {
           kint++;
           //System.out.println("kint-value #" + kint + ": " + k); 
       }
       */
       
    
       
    private static void println(JSONObject jsn, OutputStream os ) throws IOException {
        for (Object key: jsn.keySet()) {
            String keyString = (String) key;
            String outputString = keyString + ":" + jsn.get(keyString);
            os.write(outputString.getBytes(Charset.forName("UTF-8")));
            os.write("\n".getBytes(Charset.forName("UTF-8")));
            //System.out.println("class of key is: " + key.getClass().getName());
            //System.out.println("key is '" + keyString + "'");
        }
        
    }

    private static String findLang(String text) {
        LanguageIdentifier Li = new LanguageIdentifier(text);
        String lang = Li.getLanguage();
        boolean credible = Li.isReasonablyCertain();
        String res = "language is " + lang + " (certain: " + credible + ")"; 
        return lang;        
    }

    public static Map<String, String> getJsonMap(Text value) {
        JSONObject jsn = new JSONObject(value.toString());
        for (Object key: jsn.keySet()) {
            String keyString = (String) key;
            jsonkeys.add(keyString);
            //System.out.println("class of key is: " + key.getClass().getName());
            //System.out.println("key is '" + keyString + "'");
        }
        
        
        return null;
    }

}
