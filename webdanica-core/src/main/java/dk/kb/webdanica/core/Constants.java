package dk.kb.webdanica.core;

public class Constants {
	public static final String WEBDANICA_TRANSLATION_BUNDLE = "dk.kb.webdanica.Translations";
	public static final String CASSANDRA = "cassandra";
	public static final String HBASE_PHOENIX = "hbase-phoenix";
	public static final String DEFAULT_DATABASE_SYSTEM = HBASE_PHOENIX;
	public static final String DUMMY_HARVESTNAME = "DUMMY-HARVESTNAME";
	public static final String DEFAULT_HBASE_CONNECTON = "jdbc:phoenix:localhost:2181:/hbase";

	public static final String NODATA = "nodata";
	public static final String EMPTYLIST = "emptylist";
	/**
	 * How big a buffer we use for read()/write() operations on InputStream/ OutputStream.
	 */
	public static final int IO_BUFFER_SIZE = 4096;
	public static final int DEFAULT_MAIL_PORT = 25;
	public static final String DEFAULT_MAIL_HOST = "localhost";
	public static final String DEFAULT_MAIL_ADMIN = "test@localhost";
	public static final String WEBDANICA_SEEDS_NAME = "webdanicaseeds";	
} 
