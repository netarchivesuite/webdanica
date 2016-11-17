package dk.kb.webdanica.core.interfaces.harvesting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
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
import dk.netarkivet.harvester.datamodel.DBSpecifics;
import dk.netarkivet.harvester.datamodel.Domain;
import dk.netarkivet.harvester.datamodel.DomainConfiguration;
import dk.netarkivet.harvester.datamodel.DomainDAO;
import dk.netarkivet.harvester.datamodel.Password;
import dk.netarkivet.harvester.datamodel.SeedList;

public class Import {

	// TODO any other options which template to use and so on, and #hops, javascript-extraction, and robots.txt status
	public static void main(String[] args) {
		if (args.length < 1 || args.length > 2) {
			System.err.println("Need file with seeds to import as argument or just a seed");
			System.err.println("Usage: java Import <seed|file> [--add-seeds-to-default-config-if-exists]");
			System.exit(1);
		}
		
		long now = System.currentTimeMillis();
		String webdanicaConfigname = "webdanica_" + now;
		String webdanicaSeedname = "webdanica_" + now;
     	boolean addSeedsToDefaultConfigIfExists = false;
	
     	// check for optional argument
		String optionalArgument = "--add-seeds-to-default-config-if-exists";
		if (args.length == 2) {
			if (args[1].equals(optionalArgument)) {
				addSeedsToDefaultConfigIfExists = true;
			} 
		}
		
		if (!addSeedsToDefaultConfigIfExists) {
			System.out.println("The argument '--add-seeds-to-default-config-if-exists is not' was not found. ");
			System.out.println("Therefore a new webdanica-config will be created for any existing domain with confname='" + webdanicaConfigname + "', and seedname='" + webdanicaSeedname + "'");
		} else {
			System.out.println("The argument '--add-seeds-to-default-config-if-exists is not' was found. ");
			System.out.println("Therefore in this case a new webdanica-config for the domain is created with confname='" + webdanicaConfigname + "', and seedname='" + webdanicaSeedname + "'");
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
		processSeeds(seeds, addSeedsToDefaultConfigIfExists, webdanicaConfigname, webdanicaSeedname);
		
	}
	
	public static void processSeeds(List<String> seeds, boolean addSeedsToDefaultConfigIfExists, String webdanicaConfigname, String webdanicaSeedname) {
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
		System.out.println("Processed " + seeds.size() + " seeds. " + ignored + " seeds were ignored. "); 
		DomainDAO dao = DomainDAO.getInstance();
		for (String domain: domainMap.keySet()) {
			List<String> newseeds = new ArrayList<String>(domainMap.get(domain));
			if (dao.exists(domain)) {
				// try to add to existing domain
				if (addSeedsToDefaultConfigIfExists) {
					String config = dao.getDefaultDomainConfigurationName(domain);
					Domain d = dao.readKnown(domain);
					DomainConfiguration dc = d.getConfiguration(config);
					Iterator<SeedList> si = dc.getSeedLists();
					List<SeedList> seedlists = new ArrayList<SeedList>();
					while (si.hasNext()) {
						seedlists.add(si.next());
					}
					System.out.println("Found seedlists: " + seedlists.size());
					// Add to the found seedlist - should only be one
					String comments = dc.getComments();
					String addTocomments = "[" + new Date() + "] Added " + newseeds.size() + " from webdanica to  default config";  
					comments += addTocomments;
				    dc.setComments(comments);
				    //dc.setSeedLists(domain, newSeedlists)
				} else {
					// add new configuration
					Domain d = Domain.getDefaultDomain(domain);
					String addTocomments = "[" + new Date() + "] Added " + newseeds.size() + " from webdanica to  default config";
					List<SeedList> sListe = new ArrayList<SeedList>();
					List<Password> pListe = new ArrayList<Password>();
					DomainConfiguration dc = new DomainConfiguration(webdanicaConfigname, d, sListe, pListe);
					
					dc.addSeedList(d, new SeedList(webdanicaSeedname, newseeds));
				}
			}
		}
		
		
		
		
		
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
}
