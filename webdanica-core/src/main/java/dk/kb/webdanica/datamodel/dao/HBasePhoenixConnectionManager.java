package dk.kb.webdanica.datamodel.dao;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import dk.kb.webdanica.WebdanicaSettings;
import dk.kb.webdanica.utils.Settings;

public class HBasePhoenixConnectionManager {

	protected HBasePhoenixConnectionManager() {
	}

	protected static Object driver;
	protected static String connectionString;

	public static void register() {
		if (driver == null) {
			try {
				driver = Class.forName( "org.apache.phoenix.jdbc.PhoenixDriver" ).newInstance();
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
		}
		String defaultConnectionString = "jdbc:phoenix:localhost:2181:/hbase";
		if (Settings.hasKey(WebdanicaSettings.DATABASE_CONNECTION)) {
			connectionString = Settings.get(WebdanicaSettings.DATABASE_CONNECTION);
			if (connectionString.isEmpty())  {
				connectionString = defaultConnectionString;
			}
		} else {
			connectionString = defaultConnectionString;
		}
		
	}

	protected static Map<Thread, Connection> threadConnectionMap = new TreeMap<Thread, Connection>(new Comparator<Thread>() {
		@Override
		public int compare(Thread o1, Thread o2) {
			return o1.getId() == o2.getId() ? 0 : (o1.getId() > o2.getId() ? 1 : -1);
		}
	});

	public static synchronized Connection getThreadLocalConnection() throws SQLException {
		Connection conn = threadConnectionMap.get(Thread.currentThread());
		if (conn != null && conn.isClosed()) {
			threadConnectionMap.remove(Thread.currentThread());
			conn = null;
		}
		if (conn == null) {
			Properties connprops = new Properties();
			
			conn = DriverManager.getConnection(connectionString, connprops );
			threadConnectionMap.put(Thread.currentThread(), conn);
		}
		return conn;
	}

	public static void closeThreadLocalConnection() throws SQLException {
		Connection conn = threadConnectionMap.remove(Thread.currentThread());
		if (conn != null) {
			conn.close();
		}
	}

	public static void deregister() {
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
