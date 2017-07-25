package dk.kb.webdanica.core.tools;

import dk.kb.webdanica.core.interfaces.harvesting.NasReports;
import dk.kb.webdanica.core.interfaces.harvesting.SingleSeedHarvest;
import dk.kb.webdanica.core.utils.SettingsUtilities;
import dk.netarkivet.harvester.datamodel.DBSpecifics;
import dk.netarkivet.common.utils.FileUtils;


public class HarvestShowReports {

	/**
	 * Fetch the reports of a single NAS job 
	 * using the NAS settings xml defined by 
	 * -Ddk.netarkivet.settings.file=/full/path/to/nas/settingsfile
	 * and the in the webdanica-settings-file defined by the 
	 * java property -Dwebdanica.settings.file=/full/path/to/webdanica/settingsfile
	 * @throws Exception 
	 * 
	 */
	public static void main(String[] args) throws Exception {
		if (args.length < 1 || args.length > 2) {
			System.err.println("JobID [--dont-print] ");
			System.exit(1);
		}
		String argument = args[0];
		Long jobID = Long.parseLong(argument);
		
		boolean printReports = true;
		if (args.length > 1) {
			if (args[1].equalsIgnoreCase("--dont-print")) {
				printReports = false;
				System.out.println("Running script for job '" + jobID + "' in don't print reports mode");	
			} else {
				System.err.println("Ignoring invalid second argument: " + args[1]);
			}
		} else {
			System.out.println("Running script for job '" + jobID + "' in print reports mode");
		}
		
		// Verify that -Dwebdanica.settings.file is set and points to a valid file.
		final String WEBDANICA_SETTING_PROPERTY_KEY = "webdanica.settings.file";
		SettingsUtilities.testPropertyFile(WEBDANICA_SETTING_PROPERTY_KEY, true);
		// Verify that -Ddk.netarkivet.settings.file is set and points to an existing file.
		final String NETARCHIVESUITE_SETTING_PROPERTY_KEY = "dk.netarkivet.settings.file";
		SettingsUtilities.testPropertyFile(NETARCHIVESUITE_SETTING_PROPERTY_KEY, true);
	
		// Verify that database driver exists in classpath. If not exit program
		String dbdriver = DBSpecifics.getInstance().getDriverClassName();
		SettingsUtilities.verifyClass(dbdriver, true);
		// Verify that arcrepositoryclient exists in classpath. If not exit program
		String arcrepositoryClient = dk.netarkivet.common.utils.Settings.get("settings.common.arcrepositoryClient.class");
		SettingsUtilities.verifyClass(arcrepositoryClient, true);
		
		FileUtils.getTempDir().mkdirs();
	
		NasReports reports = SingleSeedHarvest.getReports(jobID, true);
		System.out.println("Retrieved " + reports.getReports().keySet() + " reports for job '" + jobID + "'"); 
		if (printReports) {
			for (String report: reports.getReports().keySet()) {
				System.out.println("report '" + report + "': " + reports.getReport(report));
				System.out.println();
			} 
		}
		System.exit(0);
	}
}
		
		