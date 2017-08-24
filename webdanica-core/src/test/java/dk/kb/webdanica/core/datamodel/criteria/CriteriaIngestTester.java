package dk.kb.webdanica.core.datamodel.criteria;

import static dk.kb.webdanica.core.utils.UnitTestUtils.getTestResourceFile;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.json.simple.parser.ParseException;

import dk.kb.webdanica.core.datamodel.criteria.CriteriaIngest;
import dk.kb.webdanica.core.datamodel.criteria.ProcessResult;
import dk.kb.webdanica.core.datamodel.dao.DAOFactory;
import dk.kb.webdanica.core.datamodel.dao.HBasePhoenixDAOFactory;


/**
 * Test of the CriteriaIngest functionality.
 * Using the files:
 * 1: textextract of file webdanica-core/src/test/resources/input/431-35-20160317083714655-00000-sb-test-har-001.statsbiblioteket.dk.warc.gz
 * webdanica-core/src/test/resources/output/SEQ/431-35-20160317083714655-00000-sb-test-har-001.statsbiblioteket.dk.warc.gz
 * 
 * 2: COMBO criteria run using the build from the research project
 * 3: COMBO-Nov5 criteria run using the build from the research project
 * 4: CombinedCombo - run of merging the script behind 2 and 3 - using tika app 1.13 and Optimaize language detector
 */
public class CriteriaIngestTester {
	
	public static void main(String[] args) throws Exception {
	    DAOFactory daoFactory = new HBasePhoenixDAOFactory();
		//File f = new File("/home/test/criteria-results/03-08-2016-1470237223/68-55-20160803110922385-00000-dia-prod-udv-01.kb.dk.warc.gz/part-m-00000.gz");
		//System.out.println(isGzippedFile(f));
		
		// Read a harvestlog, and look for the associated criteria-results in the criteria-results folder. 
		// a parameter: get all, get the latest
  
	        
		// TEST2: Nyt sample fra fredag d. 
		File basedir = new File("/home/svc/devel/webdanica/criteria-test-11-08-2016");
		File baseCriteriaDir = new File(basedir, "11-08-2016-1470934842");
		File HarvestLogTest1 = new File(basedir, "harvestlog-1470674884515.txt");
		File HarvestLogTest2 = new File(basedir, "test_danica_urls.txt.harvestlog");
		File HarvestLogTest3 = new File(basedir, "test_non_danica_urls.txt.harvestlog");
		
		//doTest(HarvestLogTest1, baseCriteriaDir, true);
		//doTest(HarvestLogTest2, baseCriteriaDir, true);
		//doTest(HarvestLogTest3, baseCriteriaDir, true);
		File basedir1 = new File("/home/svc/devel/webdanica/criteria-test-23-08-2016");
		File baseCriteriaDir1 = new File(basedir1, "23-08-2016-1471968184");
		File HarvestLogTest4 = new File(basedir1,"nl-urls-harvestlog.txt"); 
		CriteriaIngest.ingest(HarvestLogTest4, baseCriteriaDir1,false, false, daoFactory);

		//runTest3(daoFactory);
		
		//runTest1(daoFactory);
		//runTest2(daoFactory);
		System.exit(0);
	}
	
	private static void runTest3(DAOFactory daoFactory) throws Exception, IOException, SQLException, ParseException{
		File basedir = new File("/home/svc/devel/webdanica/criteria-test-11-08-2016");
		String harvestLogName = "harvestlog-1470674884515.txt";
		File HarvestLog = new File(basedir, harvestLogName);
		File baseCriteriaDir = new File(basedir, "11-08-2016-1470934842");
		CriteriaIngest.ingest(HarvestLog, baseCriteriaDir, false, false, daoFactory);
	}

	private static void runTest2(DAOFactory daoFactory) throws Exception { 
			File basedir = new File("/home/svc/devel/webdanica/criteria-test-09-08-2016");
			File HarvestLog = new File(basedir, "harvestlog-1470674884515.txt");
			File baseCriteriaDir = new File(basedir, "09-08-2016-1470760002");
			CriteriaIngest.ingest(HarvestLog, baseCriteriaDir,false, false, daoFactory);
    }

	private static void runTest1(DAOFactory daoFactory) throws Exception {
		File danicaHarvestLog = new File("/home/svc/devel/webdanica/toSVC/test_danica_urls.txt.harvestlog");
		File notdanicaHarvestLog = new File("/home/svc/devel/webdanica/toSVC/test_non_danica_urls.txt.harvestlog");
		File baseCriteriaDir = new File("/home/svc/devel/webdanica/toSVC/03-08-2016-1470237223/");
		CriteriaIngest.ingest(danicaHarvestLog, baseCriteriaDir, false, false, daoFactory);
		CriteriaIngest.ingest(notdanicaHarvestLog, baseCriteriaDir, false, false, daoFactory);	
	}
	
	
	static final String COMBINEDCOMBO_RESULT = "output/COMBINEDCOMBO/1/part-m-00000";
	static final String COMBO_RESULT = "output/COMBO/1/part-m-00000";
	static final String COMBONOV_V5_RESULT = "output/COMBONOV-V5/1/part-m-00000";
	static final String PARSED_TEXT_RESULT = "output/SEQ/431-35-20160317083714655-00000-sb-test-har-001.statsbiblioteket.dk.warc.gz";
	
	
	
	
	
	public void test(DAOFactory daoFactory) throws Exception {
		File ingestFile = getTestResourceFile(COMBINEDCOMBO_RESULT);
		boolean rejectDKURLS = false;
		boolean addToDatabase = false;
		ProcessResult pr = CriteriaIngest.processFile(ingestFile,"http://netarkivet.dk", "unknown", addToDatabase, daoFactory, rejectDKURLS);
		ingestFile = getTestResourceFile(COMBO_RESULT);
		pr = CriteriaIngest.processFile(ingestFile, "http://netarkivet.dk", "unknown", addToDatabase, daoFactory, rejectDKURLS);
	}
}
