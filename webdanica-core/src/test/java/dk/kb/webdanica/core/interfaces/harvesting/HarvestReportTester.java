package dk.kb.webdanica.core.interfaces.harvesting;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import dk.kb.webdanica.core.interfaces.harvesting.HarvestLog;
import dk.kb.webdanica.core.utils.UnitTestUtils;
import dk.netarkivet.common.exceptions.IOFailure;
import dk.netarkivet.common.utils.FileUtils;
import dk.netarkivet.common.utils.cdx.CDXRecord;

public class HarvestReportTester {

	@Test
	public void testPrintToFile() throws IOException {
		String path = "reported_domains_20.txt-harvestlog-1473676568727.txt";
		File f = UnitTestUtils.getTestResourceFile(path);
		if (f == null) {
			fail("Unable to find '" +  path + "' in resources");
		}
		List<SingleSeedHarvest> reports = HarvestLog.readHarvestLog(f);
		File outputFile = File.createTempFile("out", ".txt", new File("/tmp/"));
		HarvestLog.printToReportFile(reports, outputFile);
		System.out.println("Results printed to: " + outputFile.getAbsolutePath());
	}	
	
	@Test
	public void testIsRecordForJob() {
	    String path = "batch_result_for_jobid_15.txt";
	    File f = UnitTestUtils.getTestResourceFile(path);
	    List<CDXRecord> records = getCXDRecords(f);
	    int recordsCount = records.size();
	    assertTrue("#records should be 288", recordsCount==288);
	    int recordsCountJobId15 = 0;
	    
	    for (CDXRecord record: records) {
	        if (SingleSeedHarvest.isRecordForJob(record, 15L, true)) {
	            System.out.println("Key for jobId=15: " +  record.getURL());
	            recordsCountJobId15++;
	        }
        }
	    assertTrue("#records for JobID 15 should be 24 but was " +  recordsCountJobId15, recordsCountJobId15==24);
	}
	
	public static List<CDXRecord> getCXDRecords(File f) {
	    List<CDXRecord> records;
	    BufferedReader reader = null;
	    try {
	        reader = new BufferedReader(new FileReader(f));
	        records = new ArrayList<CDXRecord>();
	        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
	            String[] parts = line.split("\\s+");
	            CDXRecord record = new CDXRecord(parts);
	            records.add(record);
	        }
	    } catch (IOException e) {
	        throw new IOFailure("Unable to read results from file '" + f + "'", e);
	    } finally {
	        IOUtils.closeQuietly(reader);
	        FileUtils.remove(f);
	    }
	    return records;
	}
}
