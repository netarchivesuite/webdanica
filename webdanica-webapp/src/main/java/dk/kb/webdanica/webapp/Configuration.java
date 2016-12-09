package dk.kb.webdanica.webapp;

import java.io.File;

import dk.kb.webdanica.core.WebdanicaSettings;
import dk.kb.webdanica.core.datamodel.WgetSettings;
import dk.kb.webdanica.core.datamodel.dao.DAOFactory;
import dk.kb.webdanica.core.utils.DatabaseUtils;
import dk.kb.webdanica.core.utils.Emailer;
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
	private String harvestLogDirName;
	private String harvestLogPrefix;
	private String harvestLogReadySuffix;
	private String harvestLogNotReadySuffix;

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
		smtpPort = SettingsUtilities.getIntegerSetting(WebdanicaSettings.MAIL_PORT, dk.kb.webdanica.core.Constants.DEFAULT_MAIL_PORT);
		smtpHost = SettingsUtilities.getStringSetting(WebdanicaSettings.MAIL_SERVER, dk.kb.webdanica.core.Constants.DEFAULT_MAIL_HOST);
		mailAdmin = SettingsUtilities.getStringSetting(WebdanicaSettings.MAIL_ADMIN, dk.kb.webdanica.core.Constants.DEFAULT_MAIL_ADMIN);		
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
		
		harvestLogDirName = SettingsUtilities.getStringSetting(WebdanicaSettings.HARVESTING_HARVESTLOGDIR, Constants.DEFAULT_HARVESTLOGDIR); 
       	harvestLogPrefix = SettingsUtilities.getStringSetting(WebdanicaSettings.HARVESTING_HARVEST_LOG_PREFIX, Constants.DEFAULT_HARVESTLOG_PREFIX);
       	harvestLogReadySuffix = SettingsUtilities.getStringSetting(WebdanicaSettings.HARVESTING_HARVEST_LOG_READY_SUFFIX,Constants.DEFAULT_HARVESTLOG_READY_SUFFIX);
       	harvestLogNotReadySuffix = SettingsUtilities.getStringSetting(WebdanicaSettings.HARVESTING_HARVEST_LOG_NOTREADY_SUFFIX, Constants.DEFAULT_HARVESTLOG_NOTREADY_SUFFIX);
      
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
	
	public String getHarvestLogDirName() {
		return this.harvestLogDirName;
	}
	public File getHarvestLogDir() {
	    return new File(harvestLogDirName);
    }
	public String getHarvestLogPrefix() {
		return this.harvestLogPrefix;
	}
	public String getHarvestLogReadySuffix() {
		return this.harvestLogReadySuffix;
	}
	public String getHarvestLogNotReadySuffix() {
		return this.harvestLogNotReadySuffix;
	}
	
	public DAOFactory getDAOFactory() {
		return daoFactory;
	}

	public void close() {
		daoFactory.close();
    }
	
}
