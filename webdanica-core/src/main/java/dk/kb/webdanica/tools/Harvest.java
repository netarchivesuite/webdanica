package dk.kb.webdanica.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import dk.kb.webdanica.WebdanicaSettings;
import dk.kb.webdanica.datamodel.URL_REJECT_REASON;
import dk.kb.webdanica.exceptions.WebdanicaException;
import dk.kb.webdanica.interfaces.harvesting.HarvestReport;
import dk.kb.webdanica.interfaces.harvesting.SingleSeedHarvest;
import dk.kb.webdanica.utils.Settings;
import dk.kb.webdanica.utils.SettingsUtilities;
import dk.kb.webdanica.utils.UrlUtils;
import dk.netarkivet.harvester.datamodel.DBSpecifics;

public class Harvest {

	/**
	 * Harvest a single seed or a file with a list of seeds 
	 * using the NAS settings xml defined by 
	 * -Ddk.netarkivet.settings.file=/full/path/to/nas/settingsfile
	 * and the in the webdanica-settings-file defined by the 
	 * java property -Dwebdanica.settings.file=/full/path/to/webdanica/settingsfile
	 * @throws IOException If unable to create or write to harvestLog
	 * 
	 */
	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.err.println("Need file with seeds to harvest as argument or just a seed");
			System.exit(1);
		}
		
		// Verify that -Dwebdanica.settings.file is set and points to a valid file.
		final String WEBDANICA_SETTING_PROPERTY_KEY = "webdanica.settings.file";
		SettingsUtilities.testPropertyFile(WEBDANICA_SETTING_PROPERTY_KEY);
		// Verify that -Ddk.netarkivet.settings.file is set and points to an existing file.
		final String NETARCHIVESUITE_SETTING_PROPERTY_KEY = "dk.netarkivet.settings.file";
		SettingsUtilities.testPropertyFile(NETARCHIVESUITE_SETTING_PROPERTY_KEY);
		Set<String> requiredSettings = new HashSet<String>();
		requiredSettings.add(WebdanicaSettings.HARVESTING_SCHEDULE);
		requiredSettings.add(WebdanicaSettings.HARVESTING_TEMPLATE);
		requiredSettings.add(WebdanicaSettings.HARVESTING_MAX_OBJECTS);
		requiredSettings.add(WebdanicaSettings.HARVESTING_MAX_BYTES);
		requiredSettings.add(WebdanicaSettings.HARVESTING_PREFIX);								
		SettingsUtilities.verifyWebdanicaSettings(requiredSettings);
		String scheduleName = Settings.get(WebdanicaSettings.HARVESTING_SCHEDULE);
		String templateName = Settings.get(WebdanicaSettings.HARVESTING_TEMPLATE);
		int harvestMaxObjects = Settings.getInt(WebdanicaSettings.HARVESTING_MAX_OBJECTS);
		long harvestMaxBytes = Settings.getLong(WebdanicaSettings.HARVESTING_MAX_BYTES);
		String harvestPrefix = Settings.get(WebdanicaSettings.HARVESTING_PREFIX);

		// Verify that database driver exists in classpath. If not exit program
		String dbdriver = DBSpecifics.getInstance().getDriverClassName();
		SettingsUtilities.verifyClassOrExit(dbdriver);
		// Verify that arcrepositoryclient exists in classpath. If not exit program
		String arcrepositoryClient = dk.netarkivet.common.utils.Settings.get("settings.common.arcrepositoryClient.class");
		SettingsUtilities.verifyClassOrExit(arcrepositoryClient);
		
		// Check if argument is a file or just considered a single seed
		String argument = args[0];
		// Assume the argument is a file, and see if it exists
		File argumentAsFile = new File(argument); 
		boolean argumentIsSeedFile = argumentAsFile.exists(); 
		List<SingleSeedHarvest> results = new ArrayList<SingleSeedHarvest>();
		
		if (argumentIsSeedFile) {
			System.out.println("harvesting based on seeds from file '" + argumentAsFile.getAbsolutePath() + "'");
			results = doSeriesOfharvests(argumentAsFile, scheduleName, templateName, harvestPrefix, harvestMaxBytes, harvestMaxObjects);
		} else {
			URL_REJECT_REASON reason = UrlUtils.isRejectableURL(argument);
			if (reason.equals(URL_REJECT_REASON.NONE)) {
				System.out.println("Do single harvest of seed '" + argument + "'");
				SingleSeedHarvest result = doSingleHarvest(argument, scheduleName, templateName, harvestPrefix, harvestMaxBytes, harvestMaxObjects);
				
				results.add(result);
			} else {
				String errMsg = "No harvesting done. The argument '" + argument + "' is not a valid url";
				System.out.println(errMsg);
				SingleSeedHarvest s = SingleSeedHarvest.getErrorObject(argument, errMsg, null);
	        	results.add(s);
			}
		}
		// Print results to a file in Current Working Directory
		File harvestLog = new File("harvestlog-" + System.currentTimeMillis() + ".txt");
		System.out.println("Writing results of the " + results.size() + " harvests to file '" + harvestLog.getAbsolutePath() 
				+ "'");
		
		// Initialize harvestLogWriter
    	PrintWriter harvestLogWriter = new PrintWriter(new BufferedWriter(new FileWriter(harvestLog)));
    	String harvestLogHeaderPrefix = "Harvestlog for ";
    	String harvestLogHeader = harvestLogHeaderPrefix 
    			+ (argumentIsSeedFile?" seedfile " + argumentAsFile.getAbsolutePath():" seed " + argument);
    	
    	harvestLogWriter.println(harvestLogHeader);
    	harvestLogWriter.println(StringUtils.repeat("#", 80));
    	for (SingleSeedHarvest s: results) {
    		harvestLogWriter.println(HarvestReport.seedPattern + s.getSeed());
    		harvestLogWriter.println(HarvestReport.harvestnamePattern + s.getHarvestName());
    		harvestLogWriter.println(HarvestReport.successfulPattern + s.successful());
    		harvestLogWriter.println(HarvestReport.endstatePattern + s.getFinalState());
    		harvestLogWriter.println(HarvestReport.harvestedTimePattern + s.getHarvestedTime());
    		harvestLogWriter.println(HarvestReport.filesPattern + StringUtils.join(s.getFiles(), ","));
    		String errString = (s.getErrMsg() != null?s.getErrMsg():"");
    		String excpString = (s.getException() != null)? "" + s.getException():""; 
    		harvestLogWriter.println(HarvestReport.errorPattern + errString + " " + excpString);
    		harvestLogWriter.println(StringUtils.repeat("#", 80));
    	}
    	   	
    	harvestLogWriter.close();
    	System.out.println("Program exited at date: " +  new Date());
    	System.exit(0);
	}

	private static SingleSeedHarvest doSingleHarvest(String seed, String scheduleName,
            String templateName, String harvestPrefix, long maxBytes, int maxObjects) {
		String eventHarvestName = harvestPrefix + System.currentTimeMillis();
		
	    SingleSeedHarvest ssh = new SingleSeedHarvest(seed, eventHarvestName, scheduleName, templateName, maxBytes, maxObjects);
	    boolean success = ssh.finishHarvest();
	    System.out.println("Harvest of seed '" + seed + "': " + (success? "succeeded":"failed"));
	    return ssh;
	    
    }

	private static List<SingleSeedHarvest> doSeriesOfharvests(File argumentAsFile,
            String scheduleName, String templateName, String harvestPrefix, long harvestMaxBytes, int harvestMaxObjects) {
		
		BufferedReader fr = null;
		List<SingleSeedHarvest> results = new ArrayList<SingleSeedHarvest>();
        try {
	        fr = new BufferedReader(new FileReader(argumentAsFile));
        } catch (FileNotFoundException e) {
        	e.printStackTrace();
        	// Should not happen: already tested
        }        

		//read file 
		Set<String> seeds = new HashSet<String>();
		try {
			String line = "";
			while ((line = fr.readLine()) != null) {
	        	String seed = line.trim();
	        	if (!seed.isEmpty()) {
	        		seeds.add(seed);
	        	}
			}
		} catch (IOException e) {
			throw new WebdanicaException("Exception during the reading of the file '" 
					+ argumentAsFile.getAbsolutePath() + "'", e);
		} finally {
        	IOUtils.closeQuietly(fr);
        }
		
		//harvest each seed in the file
		for (String seed: seeds) {
			try {
				SingleSeedHarvest ssh = doSingleHarvest(seed, scheduleName, templateName, harvestPrefix, harvestMaxBytes, harvestMaxObjects);
				results.add(ssh);
			} catch (Throwable e) {	        
				SingleSeedHarvest s = SingleSeedHarvest.getErrorObject(seed, "Harvest Failed", e);
				results.add(s);
			}
		}
		return results;
	}
}
		
		