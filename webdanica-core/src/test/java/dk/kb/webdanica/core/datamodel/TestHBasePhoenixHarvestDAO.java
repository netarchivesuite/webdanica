package dk.kb.webdanica.core.datamodel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import dk.kb.webdanica.core.datamodel.dao.HBasePhoenixConnectionManager;
import dk.kb.webdanica.core.datamodel.dao.HBasePhoenixHarvestDAO;
import dk.kb.webdanica.core.interfaces.harvesting.HarvestReport;
import dk.netarkivet.harvester.datamodel.JobStatus;

@RunWith(JUnit4.class)
public class TestHBasePhoenixHarvestDAO {

	@Test
	public void test_hbasephoenix_harvest_dao() {
		HBasePhoenixConnectionManager.register();

		Connection conn = null;
		Properties connprops = new Properties();

		try {
			conn = DriverManager.getConnection( "jdbc:phoenix:localhost", connprops );

			List<String> files = new ArrayList<String>();
			files.add("1");
			files.add("2");
			files.add("3");

			HarvestReport report = new HarvestReport("harvestname", "seedurl", true, files, "error", JobStatus.STARTED, new Date().getTime());
			List<HarvestReport> harvestList;

			HBasePhoenixHarvestDAO dao = new HBasePhoenixHarvestDAO();
			dao.insertHarvest(report);

			report = dao.getHarvest("harvestName");
			harvestList = dao.getAll();
			harvestList = dao.getAllWithSeedurl("seedurl");
			harvestList = dao.getAllWithSuccessfulstate(true);

			conn.close();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		HBasePhoenixConnectionManager.deregister();
	}

}
