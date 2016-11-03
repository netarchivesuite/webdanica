package dk.kb.webdanica.core.datamodel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Properties;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import dk.kb.webdanica.core.datamodel.Seed;
import dk.kb.webdanica.core.datamodel.Status;
import dk.kb.webdanica.core.datamodel.dao.HBasePhoenixConnectionManager;
import dk.kb.webdanica.core.datamodel.dao.HBasePhoenixSeedsDAO;

@RunWith(JUnit4.class)
public class TestHBasePhoenixSeedsDAO {

	@Test
	public void test_hbasephoenix_seeds_dao() {
		HBasePhoenixConnectionManager.register();

		Connection conn = null;
		Properties connprops = new Properties();

		try {
			conn = DriverManager.getConnection( "jdbc:phoenix:localhost", connprops );

			Seed seed = new Seed("http://www.kb.dk/");

			HBasePhoenixSeedsDAO dao = new HBasePhoenixSeedsDAO();
			dao.insertSeed(seed);

			seed.setState(Status.NEW);
			seed.setStatusReason("Just added.");
			dao.updateState(seed);

			seed.setRedirectedUrl("http://www.karburator.dk/");
			dao.updateRedirectedUrl(seed);

			long cnt = dao.getSeedsCount(Status.NEW);
			// debug
			System.out.println(cnt);

			cnt = dao.getSeedsCount(Status.AWAITS_CURATOR_FINALAPPROVAL);
			// debug
			System.out.println(cnt);

			List<Seed> seedList = dao.getSeeds(Status.NEW, 100000);
			for (int i=0; i<seedList.size(); ++i) {
				seed = seedList.get(i);
				System.out.println(seed.getUrl());
			}

			conn.close();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		HBasePhoenixConnectionManager.deregister();
	}

}
