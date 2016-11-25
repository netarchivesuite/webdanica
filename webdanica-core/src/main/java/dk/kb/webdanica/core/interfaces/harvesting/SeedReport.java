package dk.kb.webdanica.core.interfaces.harvesting;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
[code] [status] [seed] [redirect]
301 CRAWLED http://www.netarkivet.dk/ http://netarkivet.dk/
200 CRAWLED http://netarkivet.dk
*/

public class SeedReport { 
	
	public static String SEEDS_HEADER_PATTERN = "[code] [status] [seed] [redirect]";
	
	public Map<Integer,Set<SeedReportEntry>> entryMap = new HashMap<Integer,Set<SeedReportEntry>>(); 
	
	public SeedReport(String report) {
		String[] lines = report.split("\n");
		for (String line: lines) {
			processLine(line);
		}
	}
	
	private void processLine(String line) {
		if (!line.contains(SEEDS_HEADER_PATTERN)) {
			SeedReportEntry entry = new SeedReportEntry(line);
			if (!entryMap.containsKey(entry.code)) {
				Set<SeedReportEntry> newEntrySet = new HashSet<SeedReportEntry>();
				entryMap.put(entry.code, newEntrySet);
			}
			Set<SeedReportEntry> entrySet = entryMap.get(entry.code);
			entrySet.add(entry);
		}
    }

	public SeedReport(List<String> lines) {
		for (String line: lines) {
			processLine(line);
		}
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

		private void parseLine(String line2) {
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
	}
	
	
	
}
