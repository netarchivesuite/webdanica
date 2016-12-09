package dk.kb.webdanica.core.tools;

import dk.kb.webdanica.core.interfaces.harvesting.NasReports;
import dk.kb.webdanica.core.interfaces.harvesting.SingleSeedHarvest;
import dk.kb.webdanica.core.utils.SettingsUtilities;
import dk.netarkivet.harvester.datamodel.DBSpecifics;

public class HarvestShowReports {

	/**
	 * Harvest a single seed or a file with a list of seeds 
	 * using the NAS settings xml defined by 
	 * -Ddk.netarkivet.settings.file=/full/path/to/nas/settingsfile
	 * and the in the webdanica-settings-file defined by the 
	 * java property -Dwebdanica.settings.file=/full/path/to/webdanica/settingsfile
	 * @throws Exception 
	 * 
	 */
	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.err.println("JobID");
			System.exit(1);
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
	
		// Check if argument is a file or just considered a single seed
		String argument = args[0];
		Long jobID = Long.parseLong(argument);
		NasReports reports = SingleSeedHarvest.getReports(jobID, true);
		for (String report: reports.getReports().keySet()) {
			System.out.println("report '" + report + "': " + reports.getReport(report));
			System.out.println();
		}
		System.exit(0);
	}
}
		
		