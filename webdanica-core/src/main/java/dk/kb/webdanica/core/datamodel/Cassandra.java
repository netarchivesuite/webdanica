package dk.kb.webdanica.core.datamodel;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.InvalidQueryException;

import dk.kb.webdanica.core.datamodel.dao.CassandraSettings;

public class Cassandra implements Database {

	private Session session;
	private Cluster cluster;
	private String hostname;
	private String keyspace;
	private int clientPort;
	
	public Cassandra(CassandraSettings settings){
		// client port is 9042 by default 
		// See https://github.com/datastax/java-driver/wiki/Connection-requirements
		//the client port can be configured using the native_transport_port in cassandra.yaml.
		hostname = settings.getHostname();
		keyspace = settings.getKeyspace();
		clientPort = settings.getClientPort();
				
		cluster = Cluster.builder().addContactPoint(hostname).withPort(clientPort)
				//.withCredentials(username, password) 
				.build();
		session = null;
		try {
			session = cluster.connect(keyspace);
		} catch (InvalidQueryException e) {
			System.err.println("Unable to connect to Cassandra keyspace '" + keyspace 
					+ "' at " + hostname + ": " + e);
		}
	}
	
	@Override
    public boolean isClosed() {
	    return cluster.isClosed();
    }

	@Override
    public void close() {
	    cluster.close();
    }

	@Override
    public Session getSession() {
	    return session;
    }
	
}
