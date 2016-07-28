package dk.kb.webdanica.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import dk.kb.webdanica.WebdanicaSettings;
import dk.kb.webdanica.datamodel.URL_REJECT_REASON;
import dk.kb.webdanica.interfaces.harvesting.SingleSeedHarvest;
import dk.kb.webdanica.utils.Settings;
import dk.kb.webdanica.utils.SettingsUtilities;
import dk.kb.webdanica.utils.UrlUtils;

public class Harvest {

	/**
	 * Harvest a single seed using the NAS settings xml defined by 
	 * -Ddk.netarkivet.settings.file=/full/path/to/nas/settingsfile
	 * and the in the webdanica-settings-file defined by the 
	 * java property -Dwebdanica.settings.file=/full/path/to/webdanica/settingsfile
	 * @throws Throwable 
	 * 
	 */
	public static void main(String[] args) throws Throwable {
		// Verify that -Dwebdanica.settings.file is set and points to a valid file.
		final String WEBDANICA_SETTING_PROPERTY_KEY = "webdanica.settings.file";
		SettingsUtilities.testPropertyFile(WEBDANICA_SETTING_PROPERTY_KEY);
		// Verify that -Ddk.netarkivet.settings.file is set and points to an existing file.
		final String NETARCHIVESUITE_SETTING_PROPERTY_KEY = "dk.netarkivet.settings.file";
		SettingsUtilities.testPropertyFile(NETARCHIVESUITE_SETTING_PROPERTY_KEY);
		String scheduleName = Settings.get(WebdanicaSettings.HARVESTING_SCHEDULE);
		String templateName = Settings.get(WebdanicaSettings.HARVESTING_TEMPLATE);

		if (args.length != 1) {
			System.err.println("Need file with seeds to harvest as argument or just a seed");
			System.exit(1);
		}
		String argument = args[0];
		// Consider it as file first:
		File argumentAsFile = new File(argument);
		//List<SingleS>
		if (argumentAsFile.exists()) {
			System.out.println("harvesting based on seeds from file '" + argumentAsFile.getAbsolutePath() + "'");
			doSeriesOfharvests(argumentAsFile, scheduleName, templateName);
		} else {
			URL_REJECT_REASON reason = UrlUtils.isRejectableURL(argument);
			if (reason.equals(URL_REJECT_REASON.NONE)) {
				System.out.println("Do single harvest of seed '" + argument + "'");
				doSingleHarvest(argument, scheduleName, templateName);
			} else {
				System.out.println("No harvesting done. The argument '" + argument + "' is not a valid url");

			}
		}
	}

	private static SingleSeedHarvest doSingleHarvest(String seed, String scheduleName,
            String templateName) {
		final String prefix = "webdanica-trial-";
		String eventHarvestName = prefix + System.currentTimeMillis();
		
	    SingleSeedHarvest ssh = new SingleSeedHarvest(seed, eventHarvestName, scheduleName, templateName);
	    boolean success = ssh.finishHarvest();
	    System.out.println("Harvest of seed '" + seed + "': " + (success? "succeeded":"failed"));
	    return ssh;
	    
    }

	private static List<SingleSeedHarvest> doSeriesOfharvests(File argumentAsFile,
            String scheduleName, String templateName) {
		
		BufferedReader fr = null;
		List<SingleSeedHarvest> results = new ArrayList<SingleSeedHarvest>();
        try {
	        fr = new BufferedReader(new FileReader(argumentAsFile));
        } catch (FileNotFoundException e) {
        	e.printStackTrace();
        	// Should not happen: already tested
        }        
		String line = "";
		String seed = null;

		//read file and ingest
		try {
	        while ((line = fr.readLine()) != null) {
	        	seed = line.trim();
	        	if (!seed.isEmpty()) {
	        		SingleSeedHarvest ssh = doSingleHarvest(seed, scheduleName, templateName);
	        		results.add(ssh);
	        	}
	        }
        } catch (IOException e) {	        
	        e.printStackTrace();
        } finally {
        	IOUtils.closeQuietly(fr);
        }
		
		return results;
	}
}
		
		