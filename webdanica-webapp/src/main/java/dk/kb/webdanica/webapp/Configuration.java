package dk.kb.webdanica.webapp;

import dk.kb.webdanica.core.WebdanicaSettings;
import dk.kb.webdanica.core.datamodel.WgetSettings;
import dk.kb.webdanica.core.datamodel.dao.DAOFactory;
import dk.kb.webdanica.core.utils.DatabaseUtils;
import dk.kb.webdanica.core.utils.Settings;
import dk.kb.webdanica.core.utils.SettingsUtilities;

public class Configuration {

	
	/** Env. (UNITTEST/TEST/STAGING/PROD) **/
    private String env;
    private Emailer emailer;
    private int smtpPort;
    private String smtpHost;
    private String mailAdmin;
    private String[] ignoredSuffixes;
    private String[] ignoredProtocols;

    private DAOFactory daoFactory;

    private static Configuration config;

    public static synchronized Configuration getInstance() {
    	if (config == null) {
    		config = new Configuration();
    	}
    	return config;
    }
    
    
    private Configuration() {
    	// initialize your configuration based on your WebdanicaSettings
    	/*
		 * Env. (UNITTEST/TEST/STAGING/PROD)
		 */
		env = "UNKNOWN";
		if (Settings.hasKey(WebdanicaSettings.ENVIRONMENT)) {
			String envString = Settings.get(WebdanicaSettings.ENVIRONMENT);
			if (!envString.isEmpty()) {
				env = envString.toUpperCase();
			}
		}
		/*
		 * Read SMTP settings (smtp-host, smtp-port, mail-admin ).
		 */
		final int default_smtp_port = 25;// TODO move to constants class
		final String default_smtp_host = "localhost";
		final String defaultMailAdmin = "svc@kb.dk";

		smtpPort = SettingsUtilities.getIntegerSetting(WebdanicaSettings.MAIL_PORT, default_smtp_port);
		smtpHost = SettingsUtilities.getStringSetting(WebdanicaSettings.MAIL_SERVER, default_smtp_host);
		mailAdmin = SettingsUtilities.getStringSetting(WebdanicaSettings.MAIL_ADMIN, defaultMailAdmin);		
		ignoredSuffixes = Settings.getAll(WebdanicaSettings.IGNORED_SUFFIXES);
		ignoredProtocols = Settings.getAll(WebdanicaSettings.IGNORED_PROTOCOLS);
		
		/*
		 * Initialize emailer
		 */
		boolean dontSendMails = false;
		if (env.equals("UNKNOWN") || env.equals("UNITTEST")) {
			dontSendMails = true;	
		}
		
		emailer = Emailer.getInstance(smtpHost, smtpPort, null, null, mailAdmin, dontSendMails);
		daoFactory = DatabaseUtils.getDao();
	}

	public WgetSettings getWgetSettings() {
		return new WgetSettings();
	}
	
	public String getEnv() {
		return this.env;
	}

	public String[] getIgnoredSuffixes() {
	    return this.ignoredSuffixes;
    }
	
	public String[] getIgnoredProtocols() {
	    return this.ignoredProtocols;
    }
	
	public Emailer getEmailer() {
		return this.emailer;
	}
	
	public DAOFactory getDAOFactory() {
		return daoFactory;
	}

	public void close() {
		daoFactory.close();
    }
	
}
