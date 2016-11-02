package dk.kb.webdanica.datamodel.dao;

public class HbaseSettings {

	private String connectionString;

	public HbaseSettings(String connectionString) {
		this.connectionString = connectionString;
	}
	
	
	public String getConnectionString() {
	    return this.connectionString;
    }
	public static HbaseSettings getDefaultSettings() {
	    return new HbaseSettings("jdbc:phoenix:localhost:2181:/hbase");
    }
}
