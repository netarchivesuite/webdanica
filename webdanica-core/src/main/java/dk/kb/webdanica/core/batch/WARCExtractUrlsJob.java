package dk.kb.webdanica.core.batch;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

import org.archive.io.warc.WARCRecord;

import dk.netarkivet.common.utils.archive.ArchiveHeaderBase;
import dk.netarkivet.common.utils.archive.ArchiveRecordBase;
import dk.netarkivet.common.utils.archive.HeritrixArchiveRecordWrapper;
import dk.netarkivet.common.utils.batch.WARCBatchFilter;
import dk.netarkivet.common.utils.warc.WARCBatchJob;

/**
 * Batch job that extracts information about the harvested urls.
 */
@SuppressWarnings({"serial"})
public class WARCExtractUrlsJob extends WARCBatchJob {

    /** Logger for this class. */
	 private static final Logger logger = Logger.getLogger(WARCExtractUrlsJob.class.getName());

    /**
     * Constructs a new job for extracting CDX indexes.
     *
     */
    public WARCExtractUrlsJob() {
        batchJobTimeout = 7 * 100000;
    }

    /**
     * Filters out the NON-RESPONSE records.
     *
     * @return The filter that defines what WARC records are wanted in the output CDX file.
     * @see dk.netarkivet.common.utils.warc.WARCBatchJob#getFilter()
     */
    @Override
    public WARCBatchFilter getFilter() {
        // Per default we want to index all response records.
        return WARCBatchFilter.EXCLUDE_NON_RESPONSE_RECORDS;
    }

    /**
     * Initialize any data needed (none).
     *
     * @see dk.netarkivet.common.utils.warc.WARCBatchJob#initialize(OutputStream)
     */
    @Override
    public void initialize(OutputStream os) {
    }

    /**
     * Process this entry, reading metadata into the output stream.
     *
     * @throws IOFailure on trouble reading WARC record data
     * @see dk.netarkivet.common.utils.warc.WARCBatchJob#processRecord(WARCRecord, OutputStream)
     */
    @Override
    public void processRecord(WARCRecord sar, OutputStream os) {
        logger.fine("Processing WARCRecord with offset: " + sar.getHeader().getOffset());
     
        ArchiveRecordBase record = new HeritrixArchiveRecordWrapper(sar);
        ArchiveHeaderBase header = record.getHeader();
        String url = header.getUrl() + "\n";
        try {
	        os.write(url.getBytes("UTF-8"));
        } catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
    }

    /**
     * End of the batch job.
     *
     * @see dk.netarkivet.common.utils.warc.WARCBatchJob#finish(OutputStream)
     */
    @Override
    public void finish(OutputStream os) {
    }


    /**
     * @return Humanly readable description of this instance.
     */
    public String toString() {
        return getClass().getName() + ", with Filter: " + getFilter();
    }

}