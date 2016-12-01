package dk.kb.webdanica.core.interfaces.harvesting;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.json.simple.JSONObject;

import dk.kb.webdanica.core.datamodel.criteria.CriteriaJson;

/*
metadata://netarkivet.dk/crawl/logs/alerts.log?heritrixVersion=3.3.0-LBS-2014-03&harvestid=11&jobid=147
metadata://netarkivet.dk/crawl/reports/mimetype-report.txt?heritrixVersion=3.3.0-LBS-2014-03&harvestid=11&jobid=147
metadata://netarkivet.dk/crawl/reports/seeds-report.txt?heritrixVersion=3.3.0-LBS-2014-03&harvestid=11&jobid=147
metadata://netarkivet.dk/crawl/logs/nonfatal-errors.log?heritrixVersion=3.3.0-LBS-2014-03&harvestid=11&jobid=147
metadata://netarkivet.dk/crawl/logs/scope.log?heritrixVersion=3.3.0-LBS-2014-03&harvestid=11&jobid=147
metadata://netarkivet.dk/crawl/index/cdx?majorversion=2&minorversion=0&harvestid=11&jobid=147&filename=147-11-20161014142136228-00000-dia-prod-udv-01.kb.dk.warc.gz
metadata://netarkivet.dk/crawl/logs/heritrix_out.log?heritrixVersion=3.3.0-LBS-2014-03&harvestid=11&jobid=147
metadata://netarkivet.dk/crawl/reports/crawl-report.txt?heritrixVersion=3.3.0-LBS-2014-03&harvestid=11&jobid=147
metadata://netarkivet.dk/crawl/logs/uri-errors.log?heritrixVersion=3.3.0-LBS-2014-03&harvestid=11&jobid=147
metadata://netarkivet.dk/crawl/setup/crawler-beans.cxml?heritrixVersion=3.3.0-LBS-2014-03&harvestid=11&jobid=147
metadata://netarkivet.dk/crawl/setup/harvestInfo.xml?heritrixVersion=3.3.0-LBS-2014-03&harvestid=11&jobid=147
metadata://netarkivet.dk/crawl/setup/seeds.txt?heritrixVersion=3.3.0-LBS-2014-03&harvestid=11&jobid=147
metadata://netarkivet.dk/crawl/reports/archivefiles-report.txt?heritrixVersion=3.3.0-LBS-2014-03&harvestid=11&jobid=147
metadata://netarkivet.dk/crawl/reports/threads-report.txt?heritrixVersion=3.3.0-LBS-2014-03&harvestid=11&jobid=147
metadata://netarkivet.dk/crawl/setup/duplicatereductionjobs?majorversion=1&minorversion=0&harvestid=11&harvestnum=8&jobid=147
metadata://netarkivet.dk/crawl/reports/hosts-report.txt?heritrixVersion=3.3.0-LBS-2014-03&harvestid=11&jobid=147
metadata://netarkivet.dk/crawl/logs/job.log?heritrixVersion=3.3.0-LBS-2014-03&harvestid=11&jobid=147
metadata://netarkivet.dk/crawl/reports/responsecode-report.txt?heritrixVersion=3.3.0-LBS-2014-03&harvestid=11&jobid=147
metadata://netarkivet.dk/crawl/logs/crawl.log?heritrixVersion=3.3.0-LBS-2014-03&harvestid=11&jobid=147
metadata://netarkivet.dk/crawl/logs/runtime-errors.log?heritrixVersion=3.3.0-LBS-2014-03&harvestid=11&jobid=147
metadata://netarkivet.dk/crawl/logs/heritrix3_err.log?heritrixVersion=3.3.0-LBS-2014-03&harvestid=11&jobid=147
metadata://netarkivet.dk/crawl/reports/frontier-summary-report.txt?heritrixVersion=3.3.0-LBS-2014-03&harvestid=11&jobid=147
metadata://netarkivet.dk/crawl/logs/heritrix3_out.log?heritrixVersion=3.3.0-LBS-2014-03&harvestid=11&jobid=147
metadata://netarkivet.dk/crawl/reports/source-report.txt?heritrixVersion=3.3.0-LBS-2014-03&harvestid=11&jobid=147
metadata://netarkivet.dk/crawl/reports/processors-report.txt?heritrixVersion=3.3.0-LBS-2014-03&harvestid=11&jobid=147
metadata://netarkivet.dk/crawl/logs/progress-statistics.log?heritrixVersion=3.3.0-LBS-2014-03&harvestid=11&jobid=147
*/

public class NasReports {
	
		private static final Logger logger = Logger.getLogger(NasReports.class.getName());
	
		public static final String CRAWL_REPORT_PATTERN = "reports/crawl-report.txt";
		public static final String SEEDS_REPORT_PATTERN = "reports/seeds-report.txt";
		public static final String HOSTS_REPORT_PATTERN = "reports/hosts-report.txt";
		public static final String SEEDS_SETUP_PATTERN =  "setup/seeds.txt";
		public static final String CRAWL_LOGS_PATTERN =  "logs/crawl.log";
		private Map<String, String> reports;
	
		public NasReports(Map<String,String> reports) {
			this.reports = reports;
		}
		
		public Map<String, String> getReports() {
			return this.reports;
		}
		
		public SeedReport getSeedReport() {
			String report = getreport(SEEDS_REPORT_PATTERN);
			SeedReport sr = null;
			
			if (report != null) {
				try {
					sr = new SeedReport(report);
				} catch (Throwable e) {
					logger.warning("Failed to parse the report as a seedsport: " +  e);
				}
			} else {
				logger.warning("No seeds_report found among the reports");
			}
			return sr;
		}
		
		public String getCrawlReport() {
			return getreport(CRAWL_REPORT_PATTERN);		
		}
		public String getHostsReport() {
			return getreport(HOSTS_REPORT_PATTERN);
		}
		
		public String getSeedsTxt() {
			return getreport(SEEDS_SETUP_PATTERN);
		}
		
		public String getCrawlLogs() {
			return getreport(CRAWL_LOGS_PATTERN);
		}
		
		private String getreport(String reportPattern) {
			for (String s: reports.keySet()) {
				if (s.contains(reportPattern)) {
					return reports.get(s);
				}
			}
			return null;
        }

		public List<String> getReportsAsJsonLists() {
			List<String> jsonList = new LinkedList<String>();
			for (String key: reports.keySet()) {
				JSONObject object = new JSONObject();
				object.put(key, reports.get(key));
				jsonList.add(object.toJSONString());
			}
			return jsonList;
		}
		
		public Set<String> getReportHeaders() {
			return reports.keySet();
		}
		
		public boolean hasReport(String key) {
			return reports.containsKey(key);
		}
		
		public String getReport(String key) {
			return reports.get(key);
		}
		
		public static NasReports makeNasReportsFromJson(List<String> jsonLists) {
			if (jsonLists == null) {
				return null;
			}
			Map<String,String> reports = new HashMap<String,String>();
			for (String line: jsonLists){
				CriteriaJson CJ = new CriteriaJson(line);
				if (CJ.isValid()) {
					for (String key: CJ.getKeys()) {
						//System.out.println("Found value: " + key);
						reports.put(key, CJ.getValue(key));
					}
				}
			}
			return new NasReports(reports);
		}
		
}
