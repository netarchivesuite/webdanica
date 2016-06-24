package dk.kb.webdanica;

public class WebdanicaSettings {

	public static String ENVIRONMENT = "settings.env";
	public static String DATABASE_CLASS = "settings.database.class"; // not yet used
	
	
	/** mail settings. */
	public static String MAIL_SERVER = "settings.mail.host";
	public static String MAIL_PORT = "settings.mail.port";
	public static String MAIL_ADMIN = "settings.mail.admin";
	
	/** seeds settings. */
	
	public static String IGNORED_PROTOCOLS = "settings.seeds.ignoredProtocols.protocol";
    
	public static String IGNORED_SUFFIXES = "settings.seeds.ignoredSuffixes.suffix";
	
	/** webapp settings */
	
	public static String WEBAPP_RESOURCE_PATH = "settings.webapp.resources.resource.path";
	public static String WEBAPP_RESOURCE_SECURED = "settings.webapp.resources.resource.secured";
	
}


