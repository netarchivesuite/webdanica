package dk.kb.webdanica.datamodel;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import dk.kb.webdanica.datamodel.criteria.DataSource;
import dk.kb.webdanica.datamodel.criteria.SingleCriteriaResult;
import dk.kb.webdanica.datamodel.dao.HBasePhoenixCriteriaResultsDAO;

@RunWith(JUnit4.class)
public class TestHBasePhoenixCriteriaResultsDAO {

	@Test
	public void test_hbasephoenix_criteriaresults_dao() {
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

			SingleCriteriaResult singleAnalysis = new SingleCriteriaResult("trimmedLine", "harvestName", "seedurl");

			HBasePhoenixCriteriaResultsDAO dao = new HBasePhoenixCriteriaResultsDAO();
			dao.insertRecord(conn, singleAnalysis);

			List<String> clinks = new ArrayList<String>();
			clinks.add("1");
			clinks.add("2");
			clinks.add("3");

			singleAnalysis = new SingleCriteriaResult("trimmedLine", "harvestName", "seedurl");
			singleAnalysis.url = "url";
			singleAnalysis.Cext1 = 1L;
			singleAnalysis.Cext1 = 2L;
			singleAnalysis.Cext1 = 3L;
			for (int i=0; i<SingleCriteriaResult.StringCriteria.length; ++i) {
				singleAnalysis.C.put(SingleCriteriaResult.StringCriteria[i], Integer.toString(i + 1));
			}
			singleAnalysis.intDanish = 3.1415f;
			singleAnalysis.source = DataSource.NETARKIVET;
			singleAnalysis.calcDanishCode = 5;
			singleAnalysis.CLinks = clinks;
		    singleAnalysis.CText = "ctext";

			dao.insertRecord(conn, singleAnalysis);

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
