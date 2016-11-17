package dk.kb.webdanica.core.datamodel.dao;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.logging.Logger;

import dk.kb.webdanica.core.WebdanicaSettings;
import dk.kb.webdanica.core.utils.SettingsUtilities;


public class HBasePhoenixConnectionManager {
	
	private static final Logger logger = Logger.getLogger(HBasePhoenixConnectionManager.class.getName());

	protected HBasePhoenixConnectionManager() {
	}

	protected static Object driver;
	protected static String connectionString;

	public static synchronized void register() {
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
		connectionString = SettingsUtilities.getStringSetting(WebdanicaSettings.DATABASE_CONNECTION, defaultConnectionString);
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
			conn = null;
		}
	}
	
	public static void closeAllConnections() throws SQLException {
		logger.info("Closing down all " + threadConnectionMap.size() + " connections");
		for (Connection conn: threadConnectionMap.values()) {
			if (conn != null) {
				conn.close();
			}
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
	            	logger.warning("Exception thrown during deregistering of driver '" + driver.getClass().getName() + "': " + ex); 
	            }
	        } else {
	            // driver was not registered by the webapp's ClassLoader and may be in use elsewhere
	        	logger.warning("jdbc-driver '" + driver.getClass().getName() + "' not registered by this app, so we don't touch it");       }
	    }
	}

}
