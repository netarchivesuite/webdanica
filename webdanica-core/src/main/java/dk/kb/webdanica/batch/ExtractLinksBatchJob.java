package dk.kb.webdanica.batch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.htmlparser.util.ParserException;

import dk.netarkivet.common.exceptions.IOFailure;
import dk.netarkivet.common.utils.archive.ArchiveBatchJob;
import dk.netarkivet.common.utils.archive.ArchiveHeaderBase;
import dk.netarkivet.common.utils.archive.ArchiveRecordBase;

/**
 * 
 * TODO this currently only works for ARC-files.
 * Note that this script is intended for use on uncompressed
 * arcfiles.
 *
 */
public class ExtractLinksBatchJob extends ArchiveBatchJob {
    
    private long collectedLinks;
    private long skippedRecords;

    @Override
    public void processRecord(ArchiveRecordBase record, OutputStream os) {

        ArchiveHeaderBase metadata = record.getHeader();
        // Skip, if not html and not .dk URL
        if (!metadata.getMimetype().toUpperCase().contains("HTML") 
                && !ExtractLinks.urlIsDK(metadata)) {
            skippedRecords++;
        } else {
            try {
                String text = iostreamToString(record.getInputStream());

                ExtractLinks extractor = new ExtractLinks(text);
                try {
                    for (String link: extractor.getLinksPointingOutsideDK()) {
                        //os.write(new String("\n" + link).getBytes());
                        os.write(new String("\n" + link).getBytes("UTF-8"));
                        collectedLinks++;
                    }
                } catch (ParserException e) {
                    e.printStackTrace();
                } 
            } catch (Throwable e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void initialize(OutputStream os) {
        skippedRecords = 0L;
        collectedLinks = 0L;
        
    }

    @Override
    public void finish(OutputStream os) {
        String res = "\nFound " + collectedLinks + " links which matches the "
                + "not DK-criteria";
        res += "\nSkipped " + skippedRecords + " records";
        
        try {
            //os.write(new String("\n").getBytes());
            os.write(res.getBytes());
        } catch (IOException e) {
            new IOFailure("Could not write string: " + res, e);
        }    
    }
    
    /**
     * Convert inputstream into text string. Wrapper method around IOUtils.toString method tyo catch any exceptions.
     * @param in the given inputstream
     * @return a textstring or null if something went wrong
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
}
