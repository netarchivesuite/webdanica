package dk.kb.webdanica.datamodel.criteria;

import java.io.BufferedReader;
import java.io.File;

import dk.kb.webdanica.datamodel.CassandraCriteriaResultsDAO;
import dk.kb.webdanica.datamodel.CriteriaResultsDAO;
import dk.kb.webdanica.utils.StreamUtils;

public class JsonIngester {
	
	public static void main(String[] args) throws Exception {
		String dirName = "85-71-20160823152728799-00000-dia-prod-udv-01.kb.dk.warc.gz";
		String dirName1 = "43-31-20160907115118139-00000-kb-test-webdanica-001.kb.dk.warc.gz";
		File basedir1 = new File("/home/svc/devel/webdanica/06-10-2016-1475765948/" + dirName);
		File basedir = new File("/home/svc/devel/webdanica/10-10-2016-1476112121/" + dirName);
		File basedir2 = new File("/home/svc/devel/webdanica/12-10-2016-1476271809/" + dirName1);

		File ingestF = new File(basedir2, "part-m-00000.gz"); 
		BufferedReader fr = StreamUtils.getBufferedReader(ingestF);
		String line = "";
		String trimmedLine = null;
		//read file and ingest
		CriteriaResultsDAO dao = CassandraCriteriaResultsDAO.getInstance();
		while ((line = fr.readLine()) != null) {
			trimmedLine = line.trim();
			if (!trimmedLine.isEmpty()) {
				SingleCriteriaResult s = new SingleCriteriaResult();
				s.harvestName = "harvestname-" + System.currentTimeMillis();
				s.seedurl = "http://seedurl.domain.dk/" + System.currentTimeMillis();
				SingleCriteriaResult.parseJson(trimmedLine, s);
				System.out.println(s.Cext3);
				dao.insertRecord(s);
			}
		}
	}
}