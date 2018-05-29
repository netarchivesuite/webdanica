package dk.kb.webdanica.webapp.tools;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import dk.kb.webdanica.core.WebdanicaSettings;
import dk.kb.webdanica.core.utils.SettingsUtilities;
import dk.kb.webdanica.webapp.Constants;

public class FindHarvestLogs {

	/**
	 * Find out all harvestlogs ready for analysis from 
	 * 
	 * Settings used: 
	 *	settings.harvesting.harvestlogDir
	 *	settings.harvesting.harvestlogPrefix
	 *	settings.harvesting.harvestlogReadySuffix
	 *  settings.harvesting.maxHarvestlogsProcessedEachTime
	 *  
	 * And (if not set) their associated defaults in dk.kb.webdanica.webapp.Constants
	 * @param args no arguments 
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
	    boolean LOGGING_ENABLED = false;
		String harvestLogDirName = SettingsUtilities.getStringSetting(WebdanicaSettings.HARVESTING_HARVESTLOGDIR, Constants.DEFAULT_HARVESTLOGDIR, LOGGING_ENABLED);
		final String harvestLogPrefix = SettingsUtilities.getStringSetting(WebdanicaSettings.HARVESTING_HARVEST_LOG_PREFIX, Constants.DEFAULT_HARVESTLOG_PREFIX, LOGGING_ENABLED);
		final String harvestLogReadySuffix = SettingsUtilities.getStringSetting(WebdanicaSettings.HARVESTING_HARVEST_LOG_READY_SUFFIX, Constants.DEFAULT_HARVESTLOG_READY_SUFFIX, LOGGING_ENABLED);
		final int maxNumberOfHarvestLogsReturned = SettingsUtilities.getIntegerSetting(WebdanicaSettings.HARVESTING_MAX_HARVESTLOGS_PROCESSED_EACH_TIME, Constants.DEFAULT_MAX_HARVESTLOGS_PROCESSED, LOGGING_ENABLED);
		
		File harvestLogDir = new File(harvestLogDirName);
		
		if (!harvestLogDir.isDirectory()) {
			System.err.println("ERROR: HarvestLogDir '" + harvestLogDir.getAbsolutePath() + "' does not exist or is not a directory");
			System.err.println("Exiting program with exit code 1");
			System.exit(1);
		}
		if (maxNumberOfHarvestLogsReturned < 0) {
		    System.err.println("ERROR: settings.harvesting.maxHarvestlogsProcessedEachTime (" + maxNumberOfHarvestLogsReturned + ") is negative!");
            System.err.println("Exiting program with exit code 1");
            System.exit(1);
		}
		
		String[] files = harvestLogDir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (name.startsWith(harvestLogPrefix) && name.endsWith(harvestLogReadySuffix)) {
					return true;
				} 
				return false;
			};
		});
		String prefix = harvestLogDir.getAbsolutePath() + "/";
		int printed = 0;
		for (String filename: files) {
			System.out.print(prefix + filename + " ");
			printed++;
			if (printed >= maxNumberOfHarvestLogsReturned) {
			    break;
			}
		}
		System.exit(0);
	}
}


