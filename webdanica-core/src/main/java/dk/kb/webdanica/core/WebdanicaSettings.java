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
	public static String IGNORED_PROTOCOLS = "settings.seeds.ignoredProtocols.protocol";
	public static String IGNORED_SUFFIXES = "settings.seeds.ignoredSuffixes.suffix";

	/** harvesting settings. */
	public static String HARVESTING_TEMPLATE = "settings.harvesting.template";
	public static String HARVESTING_SCHEDULE = "settings.harvesting.schedule";
	public static String HARVESTING_PREFIX = "settings.harvesting.prefix";
	public static String HARVESTING_MAX_BYTES = "settings.harvesting.maxbytes";
	public static String HARVESTING_MAX_OBJECTS = "settings.harvesting.maxobjects";
	public static String HARVESTING_MAX_HARVESTS = "settings.harvesting.maxharvests";

	/** webapp settings */
	public static String WEBAPP_RESOURCE_PATH = "settings.webapp.resources.resource.path";
	public static String WEBAPP_RESOURCE_SECURED = "settings.webapp.resources.resource.secured";
	public static String WEBAPP_DEFAULT_SECURED_SETTING = "settings.webapp.resources.defaultsecuredsetting";
	
	/** Pig related settings */
	public static String PIG_CITYNAMES_FILEPATH = "settings.pig.citynames.path";
}
