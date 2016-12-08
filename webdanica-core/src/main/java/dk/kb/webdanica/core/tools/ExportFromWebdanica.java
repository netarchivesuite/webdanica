package dk.kb.webdanica.core.tools;

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
import dk.kb.webdanica.core.utils.SettingsUtilities;

public class ExportFromWebdanica {

	/**
	 * 
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		boolean includeAlreadyExported = false;
		if (args.length == 1 && args[0].equalsIgnoreCase("--include_already_exported")) {
			includeAlreadyExported = true;
		}
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
		Set<String> urlsToExport = new TreeSet<String>();
		for (Seed s: readySeeds) {
			s.setExportedState(true);
			dao.updateSeed(s);
			if (s.getRedirectedUrl() != null && !s.getRedirectedUrl().isEmpty()) {
				urlsToExport.add(s.getRedirectedUrl());
			} else {
				urlsToExport.add(s.getUrl());
			}
		}
		// write to file 
		
		
	}

}
