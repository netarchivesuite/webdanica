package dk.kb.webdanica.core.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dk.kb.webdanica.core.Constants;
import dk.kb.webdanica.core.WebdanicaSettings;
import dk.kb.webdanica.core.datamodel.URL_REJECT_REASON;
import dk.kb.webdanica.core.interfaces.harvesting.SingleSeedHarvest;
import dk.kb.webdanica.core.utils.Settings;
import dk.kb.webdanica.core.utils.SettingsUtilities;
import dk.kb.webdanica.core.utils.UrlUtils;
import dk.netarkivet.harvester.datamodel.DBSpecifics;

public class Harvest {

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
			System.err.println("Need file with seeds to harvest as argument or just a seed");
			System.exit(1);
		}
		
		// Verify that -Dwebdanica.settings.file is set and points to a valid file.
		final String WEBDANICA_SETTING_PROPERTY_KEY = "webdanica.settings.file";
		SettingsUtilities.testPropertyFile(WEBDANICA_SETTING_PROPERTY_KEY, true);
		// Verify that -Ddk.netarkivet.settings.file is set and points to an existing file.
		final String NETARCHIVESUITE_SETTING_PROPERTY_KEY = "dk.netarkivet.settings.file";
		SettingsUtilities.testPropertyFile(NETARCHIVESUITE_SETTING_PROPERTY_KEY, true);
		Set<String> requiredSettings = new HashSet<String>();
		requiredSettings.add(WebdanicaSettings.HARVESTING_SCHEDULE);
		requiredSettings.add(WebdanicaSettings.HARVESTING_TEMPLATE);
		requiredSettings.add(WebdanicaSettings.HARVESTING_MAX_OBJECTS);
		requiredSettings.add(WebdanicaSettings.HARVESTING_MAX_BYTES);
		requiredSettings.add(WebdanicaSettings.HARVESTING_PREFIX);								
		SettingsUtilities.verifyWebdanicaSettings(requiredSettings, true);
		String scheduleName = Settings.get(WebdanicaSettings.HARVESTING_SCHEDULE);
		String templateName = Settings.get(WebdanicaSettings.HARVESTING_TEMPLATE);
		int harvestMaxObjects = Settings.getInt(WebdanicaSettings.HARVESTING_MAX_OBJECTS);
		long harvestMaxBytes = Settings.getLong(WebdanicaSettings.HARVESTING_MAX_BYTES);
		String harvestPrefix = Settings.get(WebdanicaSettings.HARVESTING_PREFIX);

		// Verify that database driver exists in classpath. If not exit program
		String dbdriver = DBSpecifics.getInstance().getDriverClassName();
		SettingsUtilities.verifyClass(dbdriver, true);
		// Verify that arcrepositoryclient exists in classpath. If not exit program
		String arcrepositoryClient = dk.netarkivet.common.utils.Settings.get("settings.common.arcrepositoryClient.class");
		SettingsUtilities.verifyClass(arcrepositoryClient, true);
		
		// Check if argument is a file or just considered a single seed
		String argument = args[0];
		// Assume the argument is a file, and see if it exists
		File argumentAsFile = new File(argument); 
		boolean argumentIsSeedFile = argumentAsFile.exists(); 
		List<SingleSeedHarvest> results = new ArrayList<SingleSeedHarvest>();
		
		if (argumentIsSeedFile) {
			System.out.println("harvesting based on seeds from file '" + argumentAsFile.getAbsolutePath() + "'");
			results = SingleSeedHarvest.doSeriesOfharvests(argumentAsFile, scheduleName, templateName, harvestPrefix, harvestMaxBytes, harvestMaxObjects, true);
		} else {
			URL_REJECT_REASON reason = UrlUtils.isRejectableURL(argument);
			if (reason.equals(URL_REJECT_REASON.NONE)) {
				System.out.println("Do single harvest of seed '" + argument + "'");
				SingleSeedHarvest result = SingleSeedHarvest.doSingleHarvest(argument, scheduleName, templateName, harvestPrefix, harvestMaxBytes, harvestMaxObjects, true);		
				results.add(result);
			} else {
				String errMsg = "No harvesting done. The argument '" + argument + "' is not a valid url";
				System.err.println(errMsg);
				SingleSeedHarvest s = SingleSeedHarvest.getErrorObject(argument, Constants.DUMMY_HARVESTNAME, errMsg, null);
	        	results.add(s);
			}
		}
		
		// Print results to a file in Current Working Directory
		String harvestLogNamePrefix = "harvestlog-";
		if (argumentIsSeedFile) {
			harvestLogNamePrefix = argumentAsFile.getName() + "-" + harvestLogNamePrefix;
		}
		File harvestLog = new File(harvestLogNamePrefix + System.currentTimeMillis() + ".txt");
		
		System.out.println("Writing results of the " + results.size() + " harvests to file '" + harvestLog.getAbsolutePath() 
				+ "'");
		boolean onlySuccessFul = false;
		String harvestLogHeaderPrefix = "Harvestlog for ";
    	String harvestLogHeader = harvestLogHeaderPrefix 
    			+ (argumentIsSeedFile?" seedfile " + argumentAsFile.getAbsolutePath():" seed " + argument);
    	
    	int written = SingleSeedHarvest.writeHarvestLog(harvestLog, harvestLogHeader, onlySuccessFul, results);
    	System.out.println(written + " harvests were written to log '" + harvestLog.getAbsolutePath() + "'");
    	System.out.println();System.out.println();
    	System.out.println("Program exited successfully at date: " +  new Date());
    	System.exit(0);
	}

	
}
		
		