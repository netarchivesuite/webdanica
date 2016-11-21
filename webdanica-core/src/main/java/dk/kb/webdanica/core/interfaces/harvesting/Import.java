package dk.kb.webdanica.core.interfaces.harvesting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;

import dk.kb.webdanica.core.exceptions.WebdanicaException;
import dk.kb.webdanica.core.utils.SettingsUtilities;
import dk.kb.webdanica.core.utils.UrlInfo;
import dk.kb.webdanica.core.utils.UrlUtils;
import dk.netarkivet.common.utils.IteratorUtils;
import dk.netarkivet.common.utils.Settings;
import dk.netarkivet.harvester.HarvesterSettings;
import dk.netarkivet.harvester.datamodel.DBSpecifics;
import dk.netarkivet.harvester.datamodel.Domain;
import dk.netarkivet.harvester.datamodel.DomainConfiguration;
import dk.netarkivet.harvester.datamodel.DomainDAO;
import dk.netarkivet.harvester.datamodel.SeedList;

public class Import {
	 // TODO constant in webdanica_settings
	public static final String WEBDANICA_SEEDS_NAME = "webdanicaseeds";
	
	// TODO any other options which template to use and so on, and #hops, javascript-extraction, and robots.txt status
	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Need file with seeds to import as argument or just a seed");
			System.err.println("Usage: java Import <seed|file> ");
			System.exit(1);
		}
		
		// Verify that -Ddk.netarkivet.settings.file is set and points to an existing file.
		final String NETARCHIVESUITE_SETTING_PROPERTY_KEY = "dk.netarkivet.settings.file";
		SettingsUtilities.testPropertyFile(NETARCHIVESUITE_SETTING_PROPERTY_KEY, true);
		// Verify that database driver exists in classpath. If not exit program
		String dbdriver = DBSpecifics.getInstance().getDriverClassName();
		SettingsUtilities.verifyClass(dbdriver, true);
		
		// Check if argument is a file or just considered a single seed
		String argument = args[0];
		// Assume the argument is a file, and see if it exists
		File argumentAsFile = new File(argument); 
		boolean argumentIsSeedFile = argumentAsFile.exists();
		List<String> seeds = new ArrayList<String>();
		if (!argumentIsSeedFile) {
			System.out.println("Trying to import seed '" + argument + "' into netarkivet.");
			seeds.add(argument);
		} else {
			System.out.println("Trying to import seeds from file '" + argumentAsFile.getAbsolutePath() + "' into netarkivet.");
			seeds.addAll(getSeedsFromFile(argumentAsFile));
			System.out.println("Read " + seeds.size() + " seeds from file.");
		}
		importSeeds(seeds);
		
	}
	
	/**
	 * 
	 * @param seeds
	 * @return
	 */
	public static Map<String,Set<String>> splitUpSeed(List<String> seeds) {
		
		// separate the seeds to different domains sets
		Map<String, Set<String>> domainMap = new TreeMap<String,Set<String>>();
		long ignored = 0;
		for (String seed: seeds) {
			UrlInfo info = UrlUtils.getInfo(seed);
			String domain = info.getDomain();
			if (domain.equals("N/A")) {
				System.err.println("Ignoring seed '" + seed + "': Not recognizable by NAS as valid domain");
				ignored++;
			} else {
				Set<String> domainSet = domainMap.get(domain);
				if (domainSet == null) {
					domainSet = new TreeSet<String>();
				}
				domainSet.add(seed);
				domainMap.put(domain, domainSet);
			}
		}
		System.out.println("Processed " + seeds.size() + " seeds. " + ignored + " seeds were ignored. "); // revert to proper logging
		return domainMap;
	}
	
	public static Set<String> getSeedsFromFile(File argumentAsFile) {
		BufferedReader fr = null;
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
		return seeds;
	}
	
	
	public static void importSeeds(List<String> seeds) {
		// separate the seeds to different domains sets
		Map<String, Set<String>> domainMap = Import.splitUpSeed(seeds);
		DomainDAO dao = DomainDAO.getInstance();
		for (String domain: domainMap.keySet()) {
			List<String> newseeds = new ArrayList<String>(domainMap.get(domain));
			if (dao.exists(domain)) {
				// Domain exists and has default config
				String config = dao.getDefaultDomainConfigurationName(domain);
				Domain d = dao.readKnown(domain);
				boolean hasWebdanicaSeeds = d.hasSeedList(Import.WEBDANICA_SEEDS_NAME);
				SeedList sl = new SeedList(Import.WEBDANICA_SEEDS_NAME, newseeds);
				if (hasWebdanicaSeeds) {
					SeedList slOld = d.getSeedList(Import.WEBDANICA_SEEDS_NAME);
					Set<String> combinedSeeds = new TreeSet<String>(slOld.getSeeds()); // this removes any duplicates
					combinedSeeds.addAll(newseeds);
					List<String> combinedSeedsWithoutDuplicates = new ArrayList<String>(combinedSeeds);
					sl = new SeedList(Import.WEBDANICA_SEEDS_NAME, combinedSeedsWithoutDuplicates);
					String existingComments = sl.getComments();
					String addedComment = "\n\r[" + new Date() + "] Added " + newseeds.size() + " seeds from webdanica to this list.";
					sl.setComments(existingComments + addedComment);
					d.updateSeedList(sl);
				} else {
					d.addSeedList(sl);
				}
				dao.update(d);
				d = dao.readKnown(domain); // re-read object from database
				DomainConfiguration dc = d.getConfiguration(config);
				// Is seedlist s1 already part of configuration?
				boolean existsWebdanicaSeedlistAsPartOfConfig = false;
				for (SeedList s: IteratorUtils.toList(dc.getSeedLists())) {
					if (s.getName().equalsIgnoreCase(Import.WEBDANICA_SEEDS_NAME)) {
						existsWebdanicaSeedlistAsPartOfConfig = true;
					}
				}
				if (existsWebdanicaSeedlistAsPartOfConfig) {
					// we're done (I think)
				} else {
					dc.addSeedList(d, sl);
					dao.update(d);
				}
				
				
			} else {
				// Create domain
				Domain d = Domain.getDefaultDomain(domain);
				// disable the automatic seeds 
				
				 String defaultSeedListName = Settings.get(HarvesterSettings.DEFAULT_SEEDLIST);
				 SeedList defaultSeedList = d.getSeedList(defaultSeedListName);
				 String comments = defaultSeedList.getComments().trim();
				 List<String> newDisabledList = new ArrayList<String>();
				 for (String seed: defaultSeedList.getSeeds()) {
					 if (!seed.startsWith("#")) {
						 newDisabledList.add("#" + seed);
					 } else {
						 newDisabledList.add(seed);
					 }
				 }
				 
				 SeedList newDefaultSeedList = new SeedList(defaultSeedListName, newDisabledList);
				 
				 String logentry = "[" + new Date() + "] Domain seedlist disabled with webdanica-import program";
				 if (comments.isEmpty()) {
					 newDefaultSeedList.setComments(logentry);
				 } else {
					 newDefaultSeedList.setComments("\n\r" + logentry);
				 }
				 d.updateSeedList(newDefaultSeedList);
				 				 
				 SeedList sl = new SeedList(Import.WEBDANICA_SEEDS_NAME, newseeds);
				 d.addSeedList(sl);
				 dao.create(d);
								 
				 //dao.update(d); // refresh object from DB - So new seedlist is in the database
				 d = dao.readKnown(domain);
				 String config = dao.getDefaultDomainConfigurationName(domain);
				 DomainConfiguration dc = d.getConfiguration(config);
				 dc.addSeedList(d, sl);
				 dao.update(d);
			}
		}
	}
}
