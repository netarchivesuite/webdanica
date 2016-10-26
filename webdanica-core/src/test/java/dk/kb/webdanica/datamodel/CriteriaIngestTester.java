package dk.kb.webdanica.datamodel;

import static dk.kb.webdanica.utils.UnitTestUtils.getTestResourceFile;

import java.io.File;

import org.junit.Test;

import dk.kb.webdanica.datamodel.criteria.CriteriaIngest;
import dk.kb.webdanica.datamodel.criteria.ProcessResult;
import dk.kb.webdanica.datamodel.dao.CassandraDAOFactory;
import dk.kb.webdanica.datamodel.dao.DAOFactory;
import dk.kb.webdanica.datamodel.dao.HBasePhoenixDAOFactory;


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
	
	static final String COMBINEDCOMBO_RESULT = "output/COMBINEDCOMBO/1/part-m-00000";
	static final String COMBO_RESULT = "output/COMBO/1/part-m-00000";
	static final String COMBONOV_V5_RESULT = "output/COMBONOV-V5/1/part-m-00000";
	static final String PARSED_TEXT_RESULT = "output/SEQ/431-35-20160317083714655-00000-sb-test-har-001.statsbiblioteket.dk.warc.gz";
		
	@Test
	public void test() throws Exception, InstantiationException, IllegalAccessException, ClassNotFoundException {
	    int count=1;
	    if (count==1){
	        return;
	    }
		File ingestFile = getTestResourceFile(COMBINEDCOMBO_RESULT);
		DAOFactory daofactory = new CassandraDAOFactory();
		ProcessResult pr = CriteriaIngest.processFile(ingestFile,"http://netarkivet.dk", "unknown", false, daofactory );
		ingestFile = getTestResourceFile(COMBO_RESULT);
		pr = CriteriaIngest.processFile(ingestFile, "http://netarkivet.dk", "unknown", false, daofactory);
	}
}
