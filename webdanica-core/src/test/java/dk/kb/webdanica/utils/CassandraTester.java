package dk.kb.webdanica.utils;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.InvalidQueryException;

/**
 * 
 * 
 * /05/11 18:04:10 INFO core.NettyUtil: Did not find Netty's native epoll transport in the classpath, defaulting to NIO.
16/05/11 18:04:10 WARN core.ReplicationStrategy$NetworkTopologyStrategy: Error while computing token map for keyspace webdanica with datacenter datacenter1: could not achieve replication factor 3 (found 1 replicas only), check your keyspace replication settings.
16/05/11 18:04:10 INFO policies.DCAwareRoundRobinPolicy: Using data-center name 'datacenter1' for DCAwareRoundRobinPolicy (if this is incorrect, please provide the correct datacenter name with DCAwareRoundRobinPolicy constructor)
16/05/11 18:04:10 INFO core.Cluster: New Cassandra host /127.0.0.1:9042 added
 
 Uses standard port: can this be changed?

 Use the manual at https://datastax.github.io/java-driver/manual/
 */

public class CassandraTester {

	public static void main(String[] args) {
		String hostname = "127.0.0.1";
		String keyspace = "webdanica";
		Cluster cluster = Cluster.builder().addContactPoint(hostname).build();
		Session session = null;
		try {
			session = cluster.connect(keyspace);	
		} catch (InvalidQueryException e) {
			System.err.println("Unable to connect to Cassandra keyspace '" + keyspace 
					+ "' at " + hostname + ": " + e);
		}
		
		// Insert one record into the users table
		//session.execute("INSERT INTO users (lastname, age, city, email, firstname) VALUES ('Jones', 35, 'Austin', 'bob@example.com', 'Bob')");
		//PreparedStatement statement = session.
		
		
		// Use select to get the user we just entered
		/*
		ResultSet results = session.execute("SELECT * FROM users WHERE lastname='Jones'");
		for (Row row : results) {
			System.out.format("%s %d\n", row.getString("firstname"), row.getInt("age"));
		}
		*/
		
		session.close();
		// Necessary for the java program to terminate
		cluster.close();
	}
}
