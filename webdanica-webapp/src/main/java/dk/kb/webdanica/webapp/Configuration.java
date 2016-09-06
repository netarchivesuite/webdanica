package dk.kb.webdanica.webapp;

import dk.kb.webdanica.WebdanicaSettings;
import dk.kb.webdanica.datamodel.WgetSettings;
import dk.kb.webdanica.utils.Settings;
import dk.kb.webdanica.utils.SettingsUtilities;
import dk.kb.webdanica.datamodel.BlackListDAO;
import dk.kb.webdanica.datamodel.CassandraBlackListDAO;
import dk.kb.webdanica.datamodel.CassandraSeedDAO;
import dk.kb.webdanica.datamodel.CriteriaResultsDAO;
import dk.kb.webdanica.datamodel.HarvestDAO;
import dk.kb.webdanica.datamodel.SeedDAO;
import dk.kb.webdanica.datamodel.harvest.CassandraCriteriaResultsDAO;
import dk.kb.webdanica.datamodel.harvest.CassandraHarvestDAO;

public class Configuration {

	/** Env. (UNITTEST/TEST/STAGING/PROD) **/
    private String env;
    private Emailer emailer;
    private int smtpPort;
    private String smtpHost;
    private String mailAdmin;
    private String[] ignoredSuffixes;
	private String[] ignoredProtocols;
	
	private BlackListDAO blacklistDao;

	private CriteriaResultsDAO criteriaResultsDao;
	
	private HarvestDAO harvestDAO;
	
	private SeedDAO seedDao;
    
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

		initDb();
	}

	private void initDb() {
	    seedDao = CassandraSeedDAO.getInstance();
	    harvestDAO = CassandraHarvestDAO.getInstance();
	    criteriaResultsDao = CassandraCriteriaResultsDAO.getInstance();
	    blacklistDao = CassandraBlackListDAO.getInstance();
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
	
	public SeedDAO getSeedDAO() {
		return this.seedDao;
	}

	public CriteriaResultsDAO getCriteriaResultsDao() {
	    return this.criteriaResultsDao;
    }

	public BlackListDAO getBlacklistDao() {
	    return this.blacklistDao;
    }

	public HarvestDAO getHarvestDAO() {
	    return this.harvestDAO;
    }

	public void close() {
		seedDao.close();
    }
	
}
