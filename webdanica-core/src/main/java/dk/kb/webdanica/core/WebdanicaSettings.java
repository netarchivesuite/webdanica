package dk.kb.webdanica.core;

public class WebdanicaSettings {

	public static String ENVIRONMENT = "settings.env";
	public static String DATABASE_SYSTEM = "settings.database.system";
	public static String DATABASE_CONNECTION = "settings.database.connection";

	/** mail settings. */
	public static String MAIL_SERVER = "settings.mail.host";
	public static String MAIL_PORT = "settings.mail.port";
	public static String MAIL_ADMIN = "settings.mail.admin";

	/** seeds settings. */
	public static String ACCEPTED_PROTOCOLS = "settings.seeds.acceptedProtocols.protocol";
	public static String IGNORED_SUFFIXES = "settings.seeds.ignoredSuffixes.suffix";
	public static String REJECT_DK_URLS = "settings.seeds.rejectDkUrls"; // default is true
	
	
	/** analysis settings. */
	public static String CONSIDER_SEED_NOT_DANICA_IF_NOT_EXPLICITLY_DANICA = "settings.analysis.considerSeedNotDanicaIfNotExplicitlyDanica";// default is false 

	/** harvesting settings. */
	public static String HARVESTING_TEMPLATE = "settings.harvesting.template";
	public static String HARVESTING_SCHEDULE = "settings.harvesting.schedule";
	public static String HARVESTING_PREFIX = "settings.harvesting.prefix";
	public static String HARVESTING_MAX_BYTES = "settings.harvesting.maxbytes";
	public static String HARVESTING_MAX_OBJECTS = "settings.harvesting.maxobjects";
	public static String HARVESTING_MAX_SINGLESEEDHARVESTS = "settings.harvesting.maxSingleSeedHarvests";
	public static String HARVESTING_HARVESTLOGDIR = "settings.harvesting.harvestlogDir";
	public static String HARVESTING_HARVEST_LOG_PREFIX = "settings.harvesting.harvestlogPrefix";
	public static String HARVESTING_HARVEST_LOG_READY_SUFFIX = "settings.harvesting.harvestlogReadySuffix";
	public static String HARVESTING_HARVEST_LOG_NOTREADY_SUFFIX = "settings.harvesting.harvestlogNotreadySuffix";
	public static String HARVESTING_MAX_TIMEINMILLIS = "settings.harvesting.harvestMaxTimeInMillis";
    public static String HARVESTING_MAX_HARVESTLOGS_PROCESSED_EACH_TIME = "settings.harvesting.maxHarvestlogsProcessedEachTime";
	
	/** crawlertraps settings. */
	public static String CRAWLERTRAPS_MAX_TRAP_SIZE = "settings.crawlertraps.maxTrapSize";
	
	/** webapp settings */
	public static String WEBAPP_RESOURCE_PATH = "settings.webapp.resources.resource.path";
	public static String WEBAPP_RESOURCE_SECURED = "settings.webapp.resources.resource.secured";
	public static String WEBAPP_DEFAULT_SECURED_SETTING = "settings.webapp.resources.defaultsecuredsetting";

	/** schedule for how often to start the filtering thread. */ 
	public static final String WEBAPP_CRONTAB_FILTERING = "settings.webapp.crontab.filtering";

	/** schedule for how often to start the harvesting thread. */
	public static final String WEBAPP_CRONTAB_HARVESTING_INIT = "settings.webapp.crontab.harvestingInit";

	public static final String WEBAPP_CRONTAB_HARVESTING_FINISH = "settings.webapp.crontab.harvestingFinish";

	/** schedule for how often to start the statecaching thread. */
	public static final String WEBAPP_CRONTAB_STATECACHING = "settings.webapp.crontab.statecaching";
	
	public static final String WEBAPP_MAX_FILTERING_RECORDS_PER_RUN = "settings.webapp.filtering.maxRecordsProcessedPerRun";
	public static final String WEBAPP_MAX_URL_LENGTH_TO_SHOW = "settings.webapp.maxUrlLengthToShow";
	public static final String WEBAPP_MAX_URLS_TO_FETCH = "settings.webapp.maxUrlsToFetch";

	/** Pig related settings */
	public static String PIG_CITYNAMES_FILEPATH = "settings.pig.citynames.path";
}
