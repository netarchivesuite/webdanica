package dk.kb.webdanica.core.interfaces.harvesting;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/*
[code] [status] [seed] [redirect]
301 CRAWLED http://www.netarkivet.dk/ http://netarkivet.dk/
200 CRAWLED http://netarkivet.dk
*/

public class SeedReport { 
	
	private static final Logger logger = Logger.getLogger(SeedReport.class.getName());
	
	public static String SEEDS_HEADER_PATTERN = "[code] [status] [seed] [redirect]";
	
	public Map<String,SeedReportEntry> entryMap = new HashMap<String,SeedReportEntry>();

	private String report; 
	
	public SeedReport(String report) {
		this.report = report;
		String[] lines = report.split("\n");
		for (String line: lines) {
			processLine(line);
		}
	}
	
	public String getReportAsString() {
		return this.report;
	}
	
	public Set<String> getSeeds() {
		return entryMap.keySet();
	}
	
	private void processLine(String line) {
		if (!line.contains(SEEDS_HEADER_PATTERN)) {
			SeedReportEntry entry = new SeedReportEntry(line);
			entryMap.put(entry.getSeed(), entry);
		}
    }
	
	public SeedReportEntry getEntry(String seed) {
	    return entryMap.get(seed);
    }

	public static class SeedReportEntry {
		private String line;
		private Integer code;
		private String status;
		private String seed;
		private String redirect;
		
		public SeedReportEntry(String line) {
			this.line = line;
			parseLine(line);
		}

		private void parseLine(String line) {
	        if (line.contains(SEEDS_HEADER_PATTERN)) {
	        	return;
	        } else {
	        	String[] lineparts = line.split(" ");
	        	code = Integer.parseInt(lineparts[0]);
	        	status = lineparts[1];
	        	seed = lineparts[2];
	        	if (lineparts.length == 4) {
	        		redirect = lineparts[3];
	        	}
	        }
        }
		
		String getRedirect() {
			return this.redirect;
		}
		
		String getStatus() {
			return this.status;
		}
		
		String getSeed() {
			return this.seed;
		}
		
		String getOriginalLine() {
			return this.line;
		}
		
		Integer getCode() {
			return this.code;
		}
		
		boolean isCrawled() {
			logger.info("Found status '" + this.status + "'");
			boolean crawled = this.status.equalsIgnoreCase("CRAWLED");
			logger.info("iscrawled decision for seed '" + this.seed + "': " + crawled);
			return crawled;
		}
	}
}
