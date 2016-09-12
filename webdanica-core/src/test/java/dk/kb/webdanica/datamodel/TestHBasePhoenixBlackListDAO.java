package dk.kb.webdanica.datamodel;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestHBasePhoenixBlackListDAO {

	@Test
	public void test_hbasephoenix_blacklist_dao() {
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

			List<String> aList = new ArrayList<String>(3);
			aList.add("One");
			aList.add("Two");
			aList.add("Three");

			BlackList aBlackList = new BlackList("name", "description", aList, true);

			HBasePhoenixBlackListDAO dao = new HBasePhoenixBlackListDAO();
			dao.insertList(conn, aBlackList);

			UUID uid = UUID.fromString("cbd23b95-6951-4136-ad26-e609928adc22");
			aBlackList = dao.readBlackList(conn, uid);

			aList = aBlackList.getList();
			for (int i=0; i<aList.size(); ++i) {
				System.out.println(aList.get(i));
			}

			List<BlackList> blacklistList = dao.getLists(conn, true);
			System.out.println(blacklistList.size());

			blacklistList = dao.getLists(conn, false);
			System.out.println(blacklistList.size());

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
