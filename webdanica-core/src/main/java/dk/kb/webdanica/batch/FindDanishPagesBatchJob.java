package dk.kb.webdanica.batch;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.IOUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.language.LanguageIdentifier;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import dk.netarkivet.common.utils.archive.ArchiveBatchJob;
import dk.netarkivet.common.utils.archive.ArchiveHeaderBase;
import dk.netarkivet.common.utils.archive.ArchiveRecordBase;
import dk.netarkivet.common.utils.batch.BatchLocalFiles;
import dk.netarkivet.common.utils.batch.FileBatchJob;

public class FindDanishPagesBatchJob extends ArchiveBatchJob {
    
    /**
     * Test the ExtractLinksBatchJob using sample data from netarkivet test-environment
     * @param args
     * @throws UnsupportedEncodingException 
     */
    public static void main(String[] args) throws UnsupportedEncodingException {
        File f = new File("/home/svc/files/970-33-20110226015923-00006-sb-test-har-001.arc");
        File f1 = new File("/home/svc/5147-163-20140110140517-00000-kb-test-har-004.kb.dk.warc");
        BatchLocalFiles blf = new BatchLocalFiles(new File[]{f, f1});
        FileBatchJob fbj = new FindDanishPagesBatchJob();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        blf.run(fbj, os);
        
        System.out.println(os.toString("UTF-8"));
        
    }
    
    @Override
    public void processRecord(ArchiveRecordBase record, OutputStream os) {
        ArchiveHeaderBase metadata = record.getHeader();
        String url = metadata.getUrl();
        // Skip, if not html
        if (!metadata.getMimetype().toUpperCase().contains("HTML")) {
            // Skipping because not html
            //System.out.println("mimetype: " + metadata.getMimetype());
        } else {
            //System.out.println("mimetype: " + metadata.getMimetype());
            String text = null;
            try {
                text = getText(record.getInputStream());
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (SAXException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (TikaException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            LanguageIdentifier Li = new LanguageIdentifier(text);
            String lang = Li.getLanguage();
            boolean credible = Li.isReasonablyCertain();
            String res = "language is " + lang + " (certain: " + credible + "): " + url; 
            //System.out.println(res);
            try {
                os.write(new String("\n").getBytes());
                os.write(res.getBytes());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private String getText(InputStream is) throws IOException, SAXException, TikaException {
        ContentHandler contenthandler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        Parser parser = new AutoDetectParser();
        parser.parse(is, contenthandler, metadata, new ParseContext());
        String res = contenthandler.toString();
        System.out.println("text size before trimming: " + res.length());
        // Trim content
        res = res.replaceAll("  ", " ");
        res = res.replaceAll("\n\n", "\n");
        res = res.replaceAll("\t\t", "\t");
        System.out.println("text size after trimming: " + res.length());
        System.out.println("TEXT-begin: ");
        System.out.println(res);
        System.out.println("TEXT-end");
        return res;
    }

    @Override
    public void initialize(OutputStream os) {
     
    }

    @Override
    public void finish(OutputStream os) {
    }
    
    private static String iostreamToString(InputStream in) {
        String res = null;
        try {
            res = IOUtils.toString(in, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
       
        return res;
    }

}
