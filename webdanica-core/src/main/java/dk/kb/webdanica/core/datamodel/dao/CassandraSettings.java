package dk.kb.webdanica.core.datamodel.dao;

public class CassandraSettings {

	private String hostName;
	private String keyspace;
	private int clientPort;
	private String userName;
	private String passwd;

	public CassandraSettings(String hostname, String keyspace, int clientPort, String username, String passwd) {
		this.hostName = hostname;
		this.keyspace = keyspace;
		this.clientPort = clientPort;
		this.userName = username;
		this.passwd = passwd;
	}
	public CassandraSettings(String hostname, String keyspace, int clientPort) {
		this.hostName = hostname;
		this.keyspace = keyspace;
		this.clientPort = clientPort;
		this.userName = null; 
		this.passwd = null;
	}
	
	
	public String getHostname() {
	    return this.hostName;
    }

	public String getKeyspace() {
	    return this.keyspace;
    }

	public int getClientPort() {
	    return this.clientPort;
    }
	
	public String getUserName() {
		return this.userName;
	}
	
	public String getPasswd() {
		return this.passwd;
	}

	public static CassandraSettings getSettings() {
		// FIXME read from Webdanica Settings
	    return null;
    }
	public static CassandraSettings getDefaultSettings() {
	    return new CassandraSettings("localhost", "webdanica", 9042);
    }
	
	public boolean isUserPasswdDisabled() {
		return this.userName == null && this.passwd == null;
	}
}
