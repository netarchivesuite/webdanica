package dk.kb.webdanica.core.datamodel;

import java.util.List;

import dk.kb.webdanica.core.datamodel.Seed;
import dk.kb.webdanica.core.datamodel.Status;
import dk.kb.webdanica.core.datamodel.dao.HBasePhoenixSeedsDAO;
import dk.kb.webdanica.core.datamodel.dao.SeedsDAO;
import dk.kb.webdanica.core.seeds.filtering.IgnoredSuffixes;

/**
 * This depends on the Cassandra webdanica keyspace existing 
 * at localhost (127.0.0.1) on port 9042, and open for all users
 * To run, it requires a WebdanicaSettingsfile containing a list of ignored suffixes
 */
public class SeedsDaoTester {

	public static void main(String[] args) throws Exception {
		SeedsDAO dao = new HBasePhoenixSeedsDAO();
		List<Seed> seeds = dao.getSeeds(Status.NEW,100000);
		System.out.println("Found '" +  seeds.size() + "' size with status NEW before filtering out urls with ignored suffixes");
		for (Seed s: seeds) {
	    	String ignoredSuffix = IgnoredSuffixes.matchesIgnoredExtension(s.getUrl());
	    	if (ignoredSuffix != null) {
	    		s.setStatus(Status.REJECTED);
	    		s.setStatusReason("REJECTED because it matches ignored suffix '" + ignoredSuffix + "'");
	    	} else {
	    		s.setStatus(Status.READY_FOR_HARVESTING);
	    		s.setStatusReason("");
	    	}
	    	dao.updateSeed(s);
	    }
		seeds = dao.getSeeds(Status.NEW,100000);
		System.out.println("Found '" +  seeds.size() + "' size with status NEW after filtering out urls with ignored suffixes");
		seeds = dao.getSeeds(Status.REJECTED,100000);
		System.out.println("Found '" +  seeds.size() + "' size with status REJECTED after filtering out urls with ignored suffixes");
		System.out.println("Found '" +  seeds.size() + "' size with status READY_FOR_HARVESTING after filtering out urls with ignored suffixes");

		for (int i=0; i <= Status.getMaxValidOrdinal(); i++) {
			Long longvalue = dao.getSeedsCount(Status.fromOrdinal(i));
			System.out.println("Found at status " + Status.fromOrdinal(i) +  ": " + longvalue);
		}
		dao.close();
	}

}
