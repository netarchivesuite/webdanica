package dk.kb.webdanica.webapp;

public class Constants {
	public static final String WEBAPP_NAME = "WEBDANICA";
	public static final String SPACE = " ";
	public static final String DEFAULT_HARVESTLOGDIR = "/home/harvestlogs/";
	public static final String DEFAULT_HARVESTLOG_PREFIX= "harvestLog-";
	public static final String DEFAULT_HARVESTLOG_READY_SUFFIX = ".txt";
	public static final String DEFAULT_HARVESTLOG_NOTREADY_SUFFIX = ".txt.open";
	
	public static final int DEFAULT_MAX_HARVESTS = 10;
	
	public static final boolean DEFAULT_REJECT_DK_URLS_VALUE = false;
	public static final String DEFAULT_FILTERING_CRONTAB = 	"0 * * * *";
	public static final String DEFAULT_HARVESTING_CRONTAB = "0 * * * *";
	public static final String DEFAULT_STATECACHING_CRONTAB = "0 * * * *";
	public static final long MAX_SEEDS_TO_FETCH = 10000;
	public static final boolean DEFAULT_WEBAPP_DEFAULT_SECURED_SETTING = false;
	
}
