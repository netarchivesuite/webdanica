package dk.kb.webdanica.datamodel;

import java.util.List;

import dk.kb.webdanica.datamodel.Seed;
import dk.kb.webdanica.datamodel.SeedDAO;
import dk.kb.webdanica.datamodel.Status;
import dk.kb.webdanica.seeds.filtering.IgnoredSuffixes;

/**
 * This depends on the Cassandra webdanica keyspace existing 
 * at localhost (127.0.0.1) on port 9042, and open for all users
 * To run, it requires a WebdanicaSettingsfile containing a list of ignored suffixes
 */
public class SeedsDaoTester {

	public static void main(String[] args) {
		SeedDAO dao = SeedDAO.getInstance();
		List<Seed> seeds = dao.getSeeds(Status.NEW);
		System.out.println("Found '" +  seeds.size() + "' size with status NEW before filtering out urls with ignored suffixes");
		for (Seed s: seeds) {
	    	String ignoredSuffix = IgnoredSuffixes.matchesIgnoredExtension(s.getUrl());
	    	if (ignoredSuffix != null) {
	    		s.setState(Status.REJECTED);
	    		s.setStatusReason("REJECTED because it matches ignored suffix '" + ignoredSuffix + "'");
	    	} else {
	    		s.setState(Status.READY_FOR_HARVESTING);
	    		s.setStatusReason("");
	    	}
	    	dao.updateState(s);
	    }
		seeds = dao.getSeeds(Status.NEW);
		System.out.println("Found '" +  seeds.size() + "' size with status NEW after filtering out urls with ignored suffixes");
		seeds = dao.getSeeds(Status.REJECTED);
		System.out.println("Found '" +  seeds.size() + "' size with status REJECTED after filtering out urls with ignored suffixes");
		seeds = dao.getSeeds(Status.READY_FOR_HARVESTING);
		System.out.println("Found '" +  seeds.size() + "' size with status READY_FOR_HARVESTING after filtering out urls with ignored suffixes");
		dao.close();

	}

}