package dk.kb.webdanica.core.interfaces.harvesting;

import java.util.ArrayList;
import java.util.List;

import dk.kb.webdanica.core.tools.ImportIntoNetarchiveSuite;
import dk.netarkivet.harvester.HarvesterSettings;
import dk.netarkivet.harvester.datamodel.Domain;
import dk.netarkivet.harvester.datamodel.DomainConfiguration;
import dk.netarkivet.harvester.datamodel.DomainDAO;
import dk.netarkivet.harvester.datamodel.Password;
import dk.netarkivet.harvester.datamodel.SeedList;

// Test program for testing the import functionality
public class ImportTester {
	
	// TODO any other options which template to use and so on, and #hops, javascript-extraction, and robots.txt status
	public static void main(String[] args) {
/*		if (args.length < 1 || args.length > 2) {
			System.err.println("Need file with seeds to import as argument or just a seed");
			System.err.println("Usage: java Import <seed|file> [--add-seeds-to-default-config-if-exists]");
			System.exit(1);
		}
		
		long now = System.currentTimeMillis();
		String webdanicaConfigname = "webdanica_" + now;
		String webdanicaSeedname = "webdanica_" + now;
     	boolean addSeedsToDefaultConfigIfExists = false;
	
 /*    	// check for optional argument
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
*/		
		// Verify that -Ddk.netarkivet.settings.file is set and points to an existing file.
/*		
		final String NETARCHIVESUITE_SETTING_PROPERTY_KEY = "dk.netarkivet.settings.file";
		SettingsUtilities.testPropertyFile(NETARCHIVESUITE_SETTING_PROPERTY_KEY, true);
		// Verify that database driver exists in classpath. If not exit program
		String dbdriver = DBSpecifics.getInstance().getDriverClassName();
		SettingsUtilities.verifyClass(dbdriver, true);
		
*/		//String argument = "http://netarkivet.dk/samplepage1.php";
		String argument = "http://vesterbro1.dk/samplepage.php";
		// Assume the argument is a file, and see if it exists
		String[] args1 = new String[] {argument};
		ImportIntoNetarchiveSuite.main(args1);	
	}



/**
 * 
 * @param domain
 * @param configName
 * @param orderXml
 * @param load
 * @param maxObjects
 * @param maxBytes
 * @param urlListList
 * @param comments
 * @return
 */
public static DomainConfiguration updateDomainConfig(Domain domain, String configName, String[] urlListList, String comments ) {
/*	
	int load = HTMLUtils.parseOptionalLong(context, Constants.MAX_RATE_PARAM,
            (long) dk.netarkivet.harvester.datamodel.Constants.DEFAULT_MAX_REQUEST_RATE).intValue();
    long maxObjects = HTMLUtils.parseOptionalLong(context, Constants.MAX_OBJECTS_PARAM,
            dk.netarkivet.harvester.datamodel.Constants.DEFAULT_MAX_OBJECTS);
    long maxBytes = HTMLUtils.parseOptionalLong(context, Constants.MAX_BYTES_PARAM,
            dk.netarkivet.harvester.datamodel.Constants.DEFAULT_MAX_BYTES);
 */
	int load = dk.netarkivet.harvester.datamodel.Constants.DEFAULT_MAX_REQUEST_RATE;
    long maxObjects = dk.netarkivet.harvester.datamodel.Constants.DEFAULT_MAX_OBJECTS;
    long maxBytes = dk.netarkivet.harvester.datamodel.Constants.DEFAULT_MAX_BYTES;
    
    String orderXml = dk.netarkivet.common.utils.Settings.get(HarvesterSettings.DOMAIN_DEFAULT_ORDERXML);
	
	// Update/create new configuration	
	List<SeedList> seedlistList = new ArrayList<SeedList>();
	for (String seedlistName : urlListList) {
		seedlistList.add(domain.getSeedList(seedlistName));
	}
	DomainConfiguration domainConf;
	if (domain.hasConfiguration(configName)) {
		domainConf = domain.getConfiguration(configName);
	} else { // new DomainConfiguration
		domainConf = new DomainConfiguration(configName, domain, seedlistList, new ArrayList<Password>());
		domain.addConfiguration(domainConf);
	}
	domainConf.setOrderXmlName(orderXml);
	domainConf.setMaxObjects(maxObjects);
	domainConf.setMaxBytes(maxBytes);
	domainConf.setMaxRequestRate(load);
	domainConf.setSeedLists(domain, seedlistList);
	if (comments != null) {
		domainConf.setComments(comments);
	}
	DomainDAO.getInstance().update(domain);
    return domainConf;
}
}	
	
////////////////////////////////////////////////////////////////////////////////////////////
// Obsolete code for adding a new configuration to netarchivesuite
////////////////////////////////////////////////////////////////////////////////////////////
////				
////				
////				
////				
////				// try to add to existing domain
////				if (addSeedsToDefaultConfigIfExists) {
////					
////					String config = dao.getDefaultDomainConfigurationName(domain);
////					Domain d = dao.readKnown(domain);
////					DomainConfiguration dc = d.getConfiguration(config);
////					Iterator<SeedList> si = dc.getSeedLists();
////					List<SeedList> seedlists = new ArrayList<SeedList>();
////					while (si.hasNext()) {
////						seedlists.add(si.next());
////					}
////					System.err.println("Found seedlists: " + seedlists.size());
////					// Add to the found seedlist - should only be one
////					String comments = dc.getComments();
////					String addTocomments = "[" + new Date() + "] Added " + newseeds.size() + " from webdanica to  default config";
////					if (!comments.isEmpty()) {
////						comments += "\n\r" + addTocomments;
////					} else {
////						comments = addTocomments;
////					}
////					
////				    dc.setComments(comments);
////				    //dc.setSeedLists(domain, newSeedlists);
////				   
////				} else {
////					// add new configuration to domain 
////					Domain d = dao.readKnown(domain);
////					String comments = d.getComments();
////					String addTocomments = "\n\r[" + new Date() + "] Adding " + newseeds.size() + " seeds from webdanica to new configuration '" + webdanicaConfigname + "'";
////					SeedList newSeedList = new SeedList(webdanicaSeedname, newseeds);
////					d.addSeedList(newSeedList);
////					d.setComments(comments+ addTocomments);
////					dao.update(d);
////					System.out.println("Seed list added to domain " + domain);
////					d = dao.readKnown(domain);
////					List<SeedList> sListe = new ArrayList<SeedList>();
////					sListe.add(newSeedList);
////					List<Password> pListe = new ArrayList<Password>();
////					DomainConfiguration dc = new DomainConfiguration(webdanicaConfigname, d, sListe, pListe);
////					dc.setComments("[" + new Date() + "] This configuration was created by webdanica with " + newseeds.size() + " seeds");
////	     			d.addConfiguration(dc);
////	     			
////					dao.update(d);
////				}
////			} else { // domain does not exist in NAS domain table.
////				
////				
////			}
////		}
//	
		
