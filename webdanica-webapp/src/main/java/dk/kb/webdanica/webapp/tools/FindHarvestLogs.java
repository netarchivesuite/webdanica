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
	public static String HARVESTING_HARVESTLOGDIR = "settings.harvesting.harvestlogDir";
	public static String HARVESTING_HARVEST_LOG_PREFIX = "settings.harvesting.harvestlogPrefix";
	public static String HARVESTING_HARVEST_LOG_READY_SUFFIX = "settings.harvesting.harvestlogReadySuffix";
	public static String HARVESTING_HARVEST_LOG_NOTREADY_SUFFIX = "settings.harvesting.harvestlogNotreadySuffix";
	 * 
	 * @param args no arguments 
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		String harvestLogDirName = SettingsUtilities.getStringSetting(WebdanicaSettings.HARVESTING_HARVESTLOGDIR, Constants.DEFAULT_HARVESTLOGDIR);
		final String harvestLogPrefix = SettingsUtilities.getStringSetting(WebdanicaSettings.HARVESTING_HARVEST_LOG_PREFIX, Constants.DEFAULT_HARVESTLOG_PREFIX);
		final String harvestLogReadySuffix =	SettingsUtilities.getStringSetting(WebdanicaSettings.HARVESTING_HARVEST_LOG_READY_SUFFIX, Constants.DEFAULT_HARVESTLOG_READY_SUFFIX);
		
		File harvestLogDir = new File(harvestLogDirName);
		
		if (!harvestLogDir.isDirectory()) {
			System.err.println("HarvestLogDir '" + harvestLogDir.getAbsolutePath() + "' does not exist or is not a directory");
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
	
	


