package dk.kb.webdanica.batch;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;
import org.jwat.common.ByteCountingPushBackInputStream;
import org.jwat.common.HttpHeader;


import org.apache.commons.io.IOUtils;
import org.htmlparser.util.ParserException;

import dk.netarkivet.common.exceptions.IOFailure;
import dk.netarkivet.common.utils.archive.ArchiveBatchJob;
import dk.netarkivet.common.utils.archive.ArchiveHeaderBase;
import dk.netarkivet.common.utils.archive.ArchiveRecordBase;
import dk.netarkivet.common.utils.batch.BatchLocalFiles;
import dk.netarkivet.common.utils.batch.FileBatchJob;


/**
 * 
 * FIXME this currently only works for ARC-files.
 * Note that the data this script is intended for is uncompressed
 * arcfiles.
 *
 */ 
public class BatchJobTemplate extends ArchiveBatchJob {
    
    private long collectedLinks;
    private long skippedRecords;

    /**
     * Test the ExtractLinksBatchJob using sample data from netarkivet test-environment
     * @param args
     * @throws UnsupportedEncodingException 
     */
    public static void main(String[] args) throws UnsupportedEncodingException {
        File f = new File("/home/svc/files/970-33-20110226015923-00006-sb-test-har-001.arc");
        File f1 = new File("/home/svc/files/5147-163-20140110140517-00000-kb-test-har-004.kb.dk.warc");
        //BatchLocalFiles blf = new BatchLocalFiles(new File[]{f, f1});
        BatchLocalFiles blf = new BatchLocalFiles(new File[]{f});
        FileBatchJob fbj = new BatchJobTemplate();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        blf.run(fbj, os);
        
        System.out.println(os.toString("UTF-8"));
        
    }
    
    /*
    @Override
    public boolean postProcess(InputStream in, OutputStream output){
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = br.readLine()) != null) {
                output.write(new String(line + "\n").getBytes());
                output.write(new String("POSTPROCESSING\n").getBytes());
            }
        
        }catch (Throwable e) {
            return false;
        }
        
        return true;
    }
    
    */
    

    @Override
    public void processRecord(ArchiveRecordBase record, OutputStream os) {

        ArchiveHeaderBase metadata = record.getHeader();
        // Skip, if not html
        if (!metadata.getMimetype().toUpperCase().contains("HTML") 
                && !ExtractLinks.urlIsDK(metadata)) {
            // Skipping because not html and not .dk URL
            //System.out.println("mimetype: " + metadata.getMimetype());
            skippedRecords++;
            try {
                os.write(new String("\nUrl skipped: " + metadata.getUrl()).getBytes());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            try {
                //System.out.println("mimetype: " + metadata.getMimetype());
                //HttpHeader header = getHTTPHeader(record.getInputStream(), metadata, true);
                //examineHTTPHeader(header); 
                String text = iostreamToString(record.getInputStream());
                //System.out.println("text=" + text);

                ExtractLinks extractor = new ExtractLinks(text);
                try {
                    for (String link: extractor.getLinksPointingOutsideDK()) {
                        os.write(new String("\n" + link).getBytes());
                        collectedLinks++;
                    }
                    
                    //System.out.println("Collected links: " + collectedLinks.size());
                } catch (ParserException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } 
            } catch (Throwable e1) {
                e1.printStackTrace();
                //stem.out.println("unexpected exception" + e1);
            }
        }
    }

    @Override
    public void initialize(OutputStream os) {
        //collectedLinks = new HashSet<String>();
     
    }

    @Override
    public void finish(OutputStream os) {
        String res = "Found " + collectedLinks + " links which matches the "
                + "not DK-criteria";
        res += "\nSkipped " + skippedRecords + " records";
        /*for (String link: collectedLinks) {
            res += (link + "\n");
        }
        */
        try {
            os.write(new String("\n").getBytes());
            os.write(res.getBytes());
        } catch (IOException e) {
            new IOFailure("Could not write string: " + res, e);
        }    
    }
    
    
    /**
     * 
     * @param in
     * @return
     */
    private static String iostreamToString(InputStream in) {
        String res = null;
        try {
            res = IOUtils.toString(in, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
       
        return res;
    }
    
    private HttpHeader getHTTPHeader(InputStream sar, ArchiveHeaderBase header, boolean keepHeader) {
        
        ByteCountingPushBackInputStream pbin = new ByteCountingPushBackInputStream(sar, 8192);
        HttpHeader httpResponse = null;
        try {
            httpResponse = HttpHeader.processPayload(HttpHeader.HT_RESPONSE,
                    pbin, header.getLength(), null);
            if (!keepHeader) {
                long read = pbin.getConsumed();
                sar.skip(read);
            }
            pbin.close();
        } catch (IOException e) {
            throw new IOFailure("Error reading httpresponse header", e);
        }
        
        return httpResponse;
    }

    private void examineHTTPHeader(HttpHeader header) {
        System.out.println(new String(header.getHeader()));
    }

    private void examineHTMLHeader(String text) {
        
    }
    
}
