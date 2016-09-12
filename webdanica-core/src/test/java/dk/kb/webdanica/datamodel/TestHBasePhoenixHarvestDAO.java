package dk.kb.webdanica.datamodel;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import dk.kb.webdanica.interfaces.harvesting.HarvestReport;
import dk.netarkivet.harvester.datamodel.JobStatus;

@RunWith(JUnit4.class)
public class TestHBasePhoenixHarvestDAO {

	@Test
	public void test_hbasephoenix_harvest_dao() {
		try {
			Class.forName( "org.apache.phoenix.jdbc.PhoenixDriver" ).newInstance();
		}
		catch (ClassNotFoundException e) {
			System.out.println( "Error: could not find jdbc driver." );
			e.printStackTrace();
		}
		catch (InstantiationException e) {
			System.out.println( "Error: could not instantiate jdbc driver." );
			e.printStackTrace();
		}
		catch (IllegalAccessException e) {
			System.out.println( "Error: could not access jdbc driver." );
			e.printStackTrace();
		}

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
			dao.insertHarvest(conn, report);

			report = dao.getHarvest(conn, "harvestName");
			harvestList = dao.getAll(conn);
			harvestList = dao.getAllWithSeedurl(conn, "seedurl");
			harvestList = dao.getAllWithSuccessfulstate(conn, true);

			conn.close();
		}
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Now deregister JDBC drivers in this context's ClassLoader:
	    // Get the webapp's ClassLoader
	    ClassLoader cl = Thread.currentThread().getContextClassLoader();
	    // Loop through all drivers
	    Enumeration<Driver> drivers = DriverManager.getDrivers();
	    while (drivers.hasMoreElements()) {
	        Driver driver = drivers.nextElement();
	        if (driver.getClass().getClassLoader() == cl) {
	            // This driver was registered by the webapp's ClassLoader, so deregister it:
	            try {
	                DriverManager.deregisterDriver(driver);
	            } catch (SQLException ex) {
	            }
	        } else {
	            // driver was not registered by the webapp's ClassLoader and may be in use elsewhere
	        }
	    }
	}

}
