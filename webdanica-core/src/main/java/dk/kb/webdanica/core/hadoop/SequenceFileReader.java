package dk.kb.webdanica.core.hadoop;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
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

import dk.netarkivet.common.utils.StringUtils;

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
public class SequenceFileReader {
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
       Configuration conf = new Configuration();
       FileSystem fs = FileSystem.getLocal(conf);
       Path seqFilePath = new Path(file.getAbsolutePath());
       SequenceFile.Reader reader = new SequenceFile.Reader(fs, seqFilePath, conf);
       //System.out.println(reader.getKeyClassName());
       Text key = new Text();
       Text val = new Text();
       long entryNumber = 0;
 
       while (reader.next(key, val)) {
           
           System.out.println("Key: " + key.toString());
           entryNumber++;
           //System.out.println("entry #" + entryNumber + " has key = '" + key + "'");
           JSONObject jsn = new JSONObject(val.toString());
           if (jsn.has("status")) {
               continue;
           }
           println(jsn);
          /*if (jsn.has("url")) {
               System.out.println("url found: " + jsn.get("url"));
           } else {
               System.out.println("no url found");
           }
           if (jsn.has("type")) {
               System.out.println("type: " + jsn.get("type"));
           } else {
               System.out.println("no type found");
           }
           if (jsn.has("type")) {
               System.out.println("type: " + jsn.get("type"));
           } else {
               System.out.println("no type found");
           }
           */
          /* 
           //getJsonMap(val);
           //System.out.println("Status: " + jsn.get("status")); 
           if (!jsn.has("content")) {
               //System.out.println("Skipping record #" + entryNumber + ". No content for key: " 
               //        + key);              
           } else {
               Set<String> languages = new HashSet<String>();
               boolean hascontent = jsn.has("content");
               boolean hasboiled = jsn.has("boiled");
               boolean hasSubject = jsn.has("subject");
               boolean hasKeywords = jsn.has("keywords");
               //System.out.println("entry #" + entryNumber + " has key = '" + key + "'");
               // Test-lang:
               String lang = findLang((String) jsn.get("content"));
               languages.add(lang);
               //System.out.println("content: " + lang);
               //System.out.println("content: " + (String) jsn.get("content"));
               if (jsn.has("boiled")) { 
                   lang = findLang((String) jsn.get("boiled"));
                   languages.add(lang);
                   //System.out.println("boiled: " + lang);
                   //System.out.println("Boiled: " + (String) jsn.get("boiled"));
               }
               /*
               if (jsn.has("description")) {
                   System.out.println("description: " + findLang((String) jsn.get("description")));
               }
               */
            /*   
               if (languages.contains("da") || languages.contains("no")) {
                   System.out.println("entry #" + entryNumber + " has key = '" + key + "'");
                   System.out.println(jsn.get("content"));
                   if (jsn.has("date")) {
                       System.out.println("date = " + (String) jsn.getString("date"));
                   }
                   System.out.println(StringUtils.conjoin(",", languages));
               }
              */ 
           }
           //System.out.println("Value is: ");
           //System.out.println(val);
           
           //maxReads--;
       reader.close();
       System.out.println("Entries read: " + entryNumber);
    }
     
       
       
       /*
       System.out.println("Found " +  jsonkeys.size() + " distinct keys");
       int kint=0;
       for (String k: jsonkeys) {
           kint++;
           //System.out.println("kint-value #" + kint + ": " + k); 
       }
       */
       
    
       
    private static void println(JSONObject jsn) {
        for (Object key: jsn.keySet()) {
            String keyString = (String) key;
            System.out.println(keyString + ":" + jsn.get(keyString));
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
