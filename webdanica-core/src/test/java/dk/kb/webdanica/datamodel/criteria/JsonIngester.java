package dk.kb.webdanica.datamodel.criteria;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import dk.kb.webdanica.utils.StreamUtils;

public class JsonIngester {
	
	public static void main(String[] args) throws IOException {
		String dirName = "85-71-20160823152728799-00000-dia-prod-udv-01.kb.dk.warc.gz";
		File basedir1 = new File("/home/svc/devel/webdanica/06-10-2016-1475765948/" + dirName);
		File basedir = new File("/home/svc/devel/webdanica/10-10-2016-1476112121/" + dirName);
		
		File ingestF = new File(basedir, "part-m-00000.gz"); 
		BufferedReader fr = StreamUtils.getBufferedReader(ingestF);
		String line = "";
		String trimmedLine = null;
		//read file and ingest
		while ((line = fr.readLine()) != null) {
			trimmedLine = line.trim();
			if (!trimmedLine.isEmpty()) {
				SingleCriteriaResult s = new SingleCriteriaResult();
				SingleCriteriaResult.parseJson(trimmedLine, s);
			}
		}
	}
}