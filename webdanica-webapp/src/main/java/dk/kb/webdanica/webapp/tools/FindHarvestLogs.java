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
	 * And (if not set) their associated defaults in dk.kb.webdanica.webapp.Constants
	 * @param args no arguments 
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		String harvestLogDirName = SettingsUtilities.getStringSetting(WebdanicaSettings.HARVESTING_HARVESTLOGDIR, Constants.DEFAULT_HARVESTLOGDIR, false);
		final String harvestLogPrefix = SettingsUtilities.getStringSetting(WebdanicaSettings.HARVESTING_HARVEST_LOG_PREFIX, Constants.DEFAULT_HARVESTLOG_PREFIX, false);
		final String harvestLogReadySuffix = SettingsUtilities.getStringSetting(WebdanicaSettings.HARVESTING_HARVEST_LOG_READY_SUFFIX, Constants.DEFAULT_HARVESTLOG_READY_SUFFIX, false);
		
		File harvestLogDir = new File(harvestLogDirName);
		
		if (!harvestLogDir.isDirectory()) {
			System.err.println("ERROR: HarvestLogDir '" + harvestLogDir.getAbsolutePath() + "' does not exist or is not a directory");
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
		for (String filename: files) {
			System.out.print(prefix + filename + " ");
		}
		System.exit(0);
	}
}
	
	


