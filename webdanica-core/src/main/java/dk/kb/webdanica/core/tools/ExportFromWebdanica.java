package dk.kb.webdanica.core.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import dk.kb.webdanica.core.Constants;
import dk.kb.webdanica.core.WebdanicaSettings;
import dk.kb.webdanica.core.datamodel.Seed;
import dk.kb.webdanica.core.datamodel.dao.CassandraDAOFactory;
import dk.kb.webdanica.core.datamodel.dao.DAOFactory;
import dk.kb.webdanica.core.datamodel.dao.HBasePhoenixDAOFactory;
import dk.kb.webdanica.core.datamodel.dao.SeedsDAO;
import dk.kb.webdanica.core.interfaces.harvesting.SingleSeedHarvest;
import dk.kb.webdanica.core.utils.SettingsUtilities;

public class ExportFromWebdanica {

	/**
	 * 
	 * @param args either 0 or --list_already_exported
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		boolean includeAlreadyExported = false;
		if (args.length == 1 && args[0].equalsIgnoreCase("--list_already_exported")) {
			includeAlreadyExported = true;
		}
		boolean writeback = true; // remove this code, when it works
		DAOFactory daoFactory = null;
		String databaseSystem = SettingsUtilities.getStringSetting(
				WebdanicaSettings.DATABASE_SYSTEM, Constants.DEFAULT_DATABASE_SYSTEM);
		if ("cassandra".equalsIgnoreCase(databaseSystem)) {
			daoFactory = new CassandraDAOFactory();
		} else if ("hbase-phoenix".equalsIgnoreCase(databaseSystem)) {
			daoFactory = new HBasePhoenixDAOFactory();
		}
		SeedsDAO dao = daoFactory.getSeedsDAO();
		List<Seed> readySeeds = dao.getSeedsReadyToExport(includeAlreadyExported);
		if (readySeeds.isEmpty()) {
			System.out.println("No seeds found ready for export");
			System.exit(0);
		}
		long alreadyExportedCount = 0;
		Set<String> urlsToExport = new TreeSet<String>();
		for (Seed s: readySeeds) {
			if (writeback) {
				// if already exported don't update the exported_time value
				if (!s.getExportedState()) {
					s.setExportedState(true);
					dao.updateSeed(s);
				} else {
					alreadyExportedCount++;
				}
			}
			// Add the redirected url instead of the original
			if (s.getRedirectedUrl() != null && !s.getRedirectedUrl().isEmpty()) {
				urlsToExport.add(s.getRedirectedUrl());
			} else {
				urlsToExport.add(s.getUrl());
			}
		}
		// write to file 
		String filename = "export-from-webdanica-" + SingleSeedHarvest.getTimestamp() + ".log";
		File outputfile = new File(filename);
		PrintWriter acceptWriter = new PrintWriter(new BufferedWriter(new FileWriter(outputfile)));
		String logHeader = "The " + readySeeds.size() + " danica seeds exported";
    	if (includeAlreadyExported) {
    		logHeader += " of which " + alreadyExportedCount + " were previously exported :";
    	} else {
    		logHeader += ": ";
    	}
    	acceptWriter.println("#" + logHeader);
    	for (String acc: urlsToExport) {
    		acceptWriter.println(acc);
    	}
    	acceptWriter.close();
    	System.out.println(logHeader);
    	System.out.println("The file '" + outputfile.getAbsolutePath() + "' contains the exported seeds");
    	System.exit(0);
		
	}

}
