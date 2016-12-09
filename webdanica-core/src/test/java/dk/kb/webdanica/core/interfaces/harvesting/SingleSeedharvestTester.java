package dk.kb.webdanica.core.interfaces.harvesting;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import dk.kb.webdanica.core.utils.SettingsUtilities;

public class SingleSeedharvestTester {

		/**
		 * Currently harvests the site http://www.familien-carlsen.dk using the schedule 'Once'
		 * and Heritrix template 'webdanica_order'
		 * @param args currently no args read
		 * @throws Throwable When the property "dk.netarkivet.settings.file" either is not defined or points to a non existing file
		 */
		public static void main (String[] args) throws Throwable {
			// Verify that netarchiveSuite settings file is defined and exists
			SettingsUtilities.testPropertyFile("dk.netarkivet.settings.file", true);
			Set<String> files = new HashSet<String>();
		/*	
			String file = "147-11-20161014142136228-00000-dia-prod-udv-01.kb.dk.warc.gz";
			files.add(file);
			Set<String> urlsfound = SingleSeedHarvest.getUrlsFromFile(file);
			for (String u: urlsfound) {
				System.out.println(u);
			}
			*/
			
		    
		    NasReports nr = SingleSeedHarvest.getReports(147L, true); 
		    //System.out.println(nr.getSeedReport());
		    SeedReport sr = nr.getSeedReport();
			//for (String s: reports.keySet()) {
			//	System.out.println(s);
			//}
			
			//doTestHarvest();
			/*
			List<CDXRecord> records = SingleSeedHarvest.getHarvestedUrls(148L);
			for (CDXRecord c: records) {
				System.out.println(c);
			}
			*/
			//new BatchLocalFiles(incomingFiles);
			
			
		}	
		
		private static void doTestHarvest() throws Exception {
			long maxBytes = 10000L;
			int maxOjects = 10000;
			SingleSeedHarvest ph = new SingleSeedHarvest("http://www.familien-carlsen.dk", 
					"test1" + System.currentTimeMillis(), "Once", "webdanica_order", maxBytes, maxOjects);
			boolean writeToStdOut = true; 
			boolean success = ph.finishHarvest(writeToStdOut);
			System.out.println("Harvest was successful: " + success);
			System.out.println("final state of harvest: " + ph.getFinalState());
			System.out.println("files harvested: " + StringUtils.join(ph.getFiles(), ","));
	    }
	}

