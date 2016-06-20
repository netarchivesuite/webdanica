package dk.kb.webdanica.webapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import com.antiaction.common.cron.CrontabSchedule;
import com.antiaction.common.cron.ScheduleAbstract;
import com.antiaction.common.templateengine.TemplateMaster;
import com.antiaction.common.templateengine.login.LoginTemplateHandler;
import com.antiaction.common.templateengine.storage.TemplateFileStorageManager;

import dk.kb.webdanica.WebdanicaSettings;
import dk.kb.webdanica.datamodel.BlackListDAO;
import dk.kb.webdanica.datamodel.SeedCassandraDAO;
import dk.kb.webdanica.utils.Settings;
import dk.kb.webdanica.utils.SettingsUtilities;
import dk.kb.webdanica.webapp.workflow.FilterWorkThread;
import dk.kb.webdanica.webapp.workflow.WorkThreadAbstract;
import dk.kb.webdanica.webapp.workflow.WorkflowWorkThread;
import dk.netarkivet.common.CommonSettings;
import dk.netarkivet.common.utils.StringUtils;
import dk.netarkivet.common.utils.SystemUtils;

/**
 *  Setup the environment for the Webdanica webapplication running on 8080 as a ROOT.war
 *  Initialization of the Environment instance requires the WEBDANICA_HOME to be set in the environment.
 */
public class Environment {

    /** Logging mechanism. */
    private static final Logger logger = Logger.getLogger(Environment.class.getName());

    public static final String DEFAULT_LOOKUP_CRONTAB = "0 * * * *";
	public static final String DEFAULT_PID_CRONTAB = "0 0 * * *";
	public static final String DEFAULT_ALIVECHECK_CRONTAB = "0 0 * * *";
	public static final String DEFAULT_FETCH_CRONTAB = "0 * * * *";
	public static final String DEFAULT_WAYBACKCHECK_CRONTAB = "0 0 * * *";
	public static final String DEFAULT_ARCHIVECHECK_CRONTAB = "0 0 * * *";
	public static final String DEFAULT_EMAIL_CRONTAB = "0 0 * * *";

    /** servletConfig. */
    private ServletConfig servletConfig = null;

	private String version = null;
	
	/** Env. (UNITTEST/TEST/STAGING/PROD) **/
    private String env;

    private WorkThreadAbstract[] workthreads;
    
    /*
     * Paths.
     */

    private String contextPath;
    private String seedsPath; // Maybe not fields here
	private String seedPath;  // Maybe not fields here
    
    /*
     * Templates.
     */

    private TemplateMaster templateMaster = null;

    private String login_template_name = null;

    private LoginTemplateHandler<User> loginHandler = null;

    /*
     * Misc.
     */

    /** Database <code>DataSource</code> object. */
    //public DataSource dataSource = null;

    
    /*
     * WorkThreads.
     */

    private WorkflowWorkThread workflow;

    private FilterWorkThread filterThread;
    
    /*
    public MonitoringWorkThread monitoring;

    public LookupWorkThread lookup;
    
    public PIDWorkThread pid;

    public AliveWorkThread alive;

    public FetchWorkThread fetch;

    public WaybackWorkThread wayback;

    public ArchiveWorkThread archive;
*/
    public Emailer emailer;

    /*
     * Schedules.
     */

    public ScheduleAbstract lookupSchedule;

    public ScheduleAbstract pidSchedule;

    public ScheduleAbstract aliveCheckSchedule;

    public ScheduleAbstract fetchSchedule;

    public ScheduleAbstract waybackCheckSchedule;

    public ScheduleAbstract archiveCheckSchedule;

    public ScheduleAbstract emailSchedule;

    /*
     * Log.
     */

    public List<LogRecord> newLogRecords = new LinkedList<LogRecord>();

    public List<LogRecord> logRecords = new LinkedList<LogRecord>();
    
	private String mail_admin;
	private int smtp_port;
	private String smtp_host;

	private int defaultItemsPerPage = 25; // create settings

	public SeedCassandraDAO seedDao;

	private ServletContext servletContext;

	public BlackListDAO blacklistDao;

	private String blacklistsPath;

	private String blacklistPath;

	private String ingestlogsPath;

	private String ingestlogPath;

	private File netarchiveSuiteSettingsFile;
	
	private File webdanicaSettingsFile;
	
    /**
     * @param servletContext
     * @param theServletConfig
     * @throws ServletException
     */
	public Environment(ServletContext theServletContext, ServletConfig theServletConfig) throws ServletException {
		this.setServletConfig(theServletConfig);
 		this.servletContext = theServletContext;
 		
		/*
		 * Version.
		 */

		Package pkg = Package.getPackage("dk.kb.webdanica.webapp");
		if (pkg != null) {
			version = pkg.getSpecificationVersion();
		}
		if (version == null) {
			version = "N/A";
		}

		/*
		 * Logging.
		 */

		String loggingPropertiesFilename = servletContext.getRealPath("/WEB-INF/logging.properties");
		File loggingPropertiesFile = new File(loggingPropertiesFilename);
		if (loggingPropertiesFile != null && loggingPropertiesFile.exists() && loggingPropertiesFile.isFile()) {
			try {
				LogManager.getLogManager().readConfiguration(new FileInputStream(loggingPropertiesFile));
				logger.log(Level.INFO, "java.util.logging reconfigured using: " + loggingPropertiesFilename);
			} catch (SecurityException e) {
				e.printStackTrace();
				logger.log(Level.SEVERE, e.toString(), e);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				logger.log(Level.SEVERE, e.toString(), e);
			} catch (IOException e) {
				e.printStackTrace();
				logger.log(Level.SEVERE, e.toString(), e);
			}
		}

		Logger rootLogger = Logger.getLogger("");
		rootLogger.addHandler(new Handler() {
			@Override
			public void publish(LogRecord record) {
				synchronized (newLogRecords) {
					newLogRecords.add(record);
				}
			}
			@Override
			public void flush() {
			}
			@Override
			public void close() throws SecurityException {
			}
		});

		String webdanicaHomeEnv = System.getenv("WEBDANICA_HOME"); 
		if (webdanicaHomeEnv == null) {
			throw new ServletException("'WEBDANICA_HOME' must be defined in the environment!");
		}
		File webdanicaHomeDir = new File(webdanicaHomeEnv);
		if (!webdanicaHomeDir.isDirectory()) {
			throw new ServletException("The path set by 'WEBDANICA_HOME' does not represent a directory: " 
					+ webdanicaHomeDir.getAbsolutePath());
		}
		// relative paths in web.xml will be prefixed by this path + /

		String netarchiveSuiteSettings = getServletConfig().getInitParameter("netarchivesuite-settings");
		if (!netarchiveSuiteSettings.startsWith("/")) {
			netarchiveSuiteSettings = webdanicaHomeDir.getAbsolutePath() + "/" + netarchiveSuiteSettings;
		}
		netarchiveSuiteSettingsFile = new File(netarchiveSuiteSettings);
		if (netarchiveSuiteSettingsFile.isFile()) {	  	
			if (!SettingsUtilities.isValidSimpleXmlSettingsFile(netarchiveSuiteSettingsFile)) {
				throw new ServletException("The parameter 'netarchivesuite-settings' refers to a settingsfile containing invalid contents: " 
						+ netarchiveSuiteSettingsFile.getAbsolutePath());
			}
			System.setProperty("dk.netarkivet.settings.file", netarchiveSuiteSettingsFile.getAbsolutePath());
			dk.netarkivet.common.utils.Settings.reload(); // Strictly not necessary
		} else {
			throw new ServletException("The parameter 'netarchivesuite-settings' refers to a non-existing file: " 
					+ netarchiveSuiteSettingsFile.getAbsolutePath());
		}

		String webdanicaSettings = getServletConfig().getInitParameter("webdanica-settings");
		if (!webdanicaSettings.startsWith("/")) {
			webdanicaSettings = webdanicaHomeDir.getAbsolutePath() + "/" + webdanicaSettings;
		}
		webdanicaSettingsFile = new File(webdanicaSettings);
		if (webdanicaSettingsFile.isFile()) {

			if (!SettingsUtilities.isValidSimpleXmlSettingsFile(webdanicaSettingsFile)) {
				throw new ServletException("The parameter 'webdanica-settings' refers to a settingsfile containing invalid contents: " 
						+ webdanicaSettingsFile.getAbsolutePath());
			}
			System.setProperty("webdanica.settings.file", webdanicaSettingsFile.getAbsolutePath());
			Settings.reload();
		} else {
			throw new ServletException("The parameter 'webdanica-settings' refers to non-existing file: " 
					+ webdanicaSettingsFile.getAbsolutePath());
		}

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
		final String default_smtp_host = "localhost";// TODO move to constants class
		final String defaultMailAdmin = "svc@kb.dk"; // TODO move to constants class
	
		smtp_port = SettingsUtilities.getIntegerSetting(WebdanicaSettings.MAIL_PORT, default_smtp_port);
		smtp_host = SettingsUtilities.getStringSetting(WebdanicaSettings.MAIL_SERVER, default_smtp_host);
		mail_admin = SettingsUtilities.getStringSetting(WebdanicaSettings.MAIL_ADMIN, defaultMailAdmin);		

		logger.info("Connected to NetarchiveSuite system with environmentname: " + 
				dk.netarkivet.common.utils.Settings.get(CommonSettings.ENVIRONMENT_NAME));
		
		String[] ignoredSuffixes = Settings.getAll(WebdanicaSettings.IGNORED_SUFFIXES);
		String[] ignoredProtocols = Settings.getAll(WebdanicaSettings.IGNORED_PROTOCOLS);
		
		logger.info("Following suffixes are currently ignored by webdanica-project:" + StringUtils.conjoin(",", 
				ignoredSuffixes));
		logger.info("Following protocols are currently ignored by webdanica-project:" + StringUtils.conjoin(",", 
				ignoredProtocols));

		/*
		 * Templates.
		 */

		login_template_name = getServletConfig().getInitParameter("login-template");

		if (login_template_name != null && login_template_name.length() > 0) {
			logger.info("Using '" +  login_template_name + "' as login template.");
		} else {
			throw new ServletException("'The property 'login-template' must be configured in the web.xml");
		}



		/*
		 * Crontabs.
		 */

		String lookupCrontab = getServletConfig().getInitParameter("lookup-crontab");
		String pidCrontab = getServletConfig().getInitParameter("pid-crontab");
		String aliveCheckCrontab = getServletConfig().getInitParameter("alive-crontab");
		String fetchCrontab = getServletConfig().getInitParameter("fetch-crontab");
		String waybackCheckCrontab = getServletConfig().getInitParameter("check-crontab");
		String archiveCheckCrontab = getServletConfig().getInitParameter("archive-crontab");
		String emailCrontab = getServletConfig().getInitParameter("email-crontab");
		if (lookupCrontab == null || lookupCrontab.length() == 0) {
			lookupCrontab = DEFAULT_LOOKUP_CRONTAB;
			logger.info("Using default 'lookup-crontab' value of '" + lookupCrontab + "'.");
		} else {
			logger.info("Using 'lookup-crontab' value of '" + lookupCrontab + "'.");
		}
		if (pidCrontab == null || pidCrontab.length() == 0) {
			pidCrontab = DEFAULT_PID_CRONTAB;
			logger.info("Using default 'pid-crontab' value of '" + pidCrontab + "'.");
		} else {
			logger.info("Using 'pid-crontab' value of '" + pidCrontab + "'.");
		}
		if (aliveCheckCrontab == null || aliveCheckCrontab.length() == 0) {
			aliveCheckCrontab = DEFAULT_ALIVECHECK_CRONTAB;
			logger.info("Using default 'alive-crontab' value of '" + aliveCheckCrontab + "'.");
		} else {
			logger.info("Using 'alive-crontab' value of '" + aliveCheckCrontab + "'.");
		}
		if (fetchCrontab == null || fetchCrontab.length() == 0) {
			fetchCrontab = DEFAULT_FETCH_CRONTAB;
			logger.info("Using default 'fetch-crontab' value of '" + fetchCrontab + "'.");
		} else {
			logger.info("Using 'fetch-crontab' value of '" + fetchCrontab + "'.");
		}
		if (waybackCheckCrontab == null || waybackCheckCrontab.length() == 0) {
			waybackCheckCrontab = DEFAULT_WAYBACKCHECK_CRONTAB;
			logger.info("Using default 'check-crontab' value of '" + waybackCheckCrontab + "'.");
		} else {
			logger.info("Using 'check-crontab' value of '" + waybackCheckCrontab + "'.");
		}
		if (archiveCheckCrontab == null || archiveCheckCrontab.length() == 0) {
			archiveCheckCrontab = DEFAULT_ARCHIVECHECK_CRONTAB;
			logger.info("Using default 'archive-crontab' value of '" + archiveCheckCrontab + "'.");
		} else {
			logger.info("Using 'archive-crontab' value of '" + archiveCheckCrontab + "'.");
		}
		if (emailCrontab == null || emailCrontab.length() == 0) {
			emailCrontab = DEFAULT_EMAIL_CRONTAB;
			logger.info("Using default 'email-crontab' value of '" + emailCrontab + "'.");
		} else {
			logger.info("Using 'email-crontab' value of '" + emailCrontab + "'.");
		}
		lookupSchedule = CrontabSchedule.crontabFactory(lookupCrontab);
		pidSchedule = CrontabSchedule.crontabFactory(pidCrontab);
		aliveCheckSchedule = CrontabSchedule.crontabFactory(aliveCheckCrontab);
		fetchSchedule = CrontabSchedule.crontabFactory(fetchCrontab);
		waybackCheckSchedule = CrontabSchedule.crontabFactory(waybackCheckCrontab);
		archiveCheckSchedule = CrontabSchedule.crontabFactory(archiveCheckCrontab);
		emailSchedule = CrontabSchedule.crontabFactory(emailCrontab);

		//db = new Cassandra(); // TODO make a Connect class that hides away the DB specifics.

		seedDao = SeedCassandraDAO.getInstance(); 
		blacklistDao  = BlackListDAO.getInstance();
		
		/*
		 * Initialize emailer
		 */
		boolean dontSendMails = false;
		if (env.equals("UNKNOWN") || env.equals("UNITTEST")) {
			dontSendMails = true;	
		}
		emailer = Emailer.getInstance(smtp_host, smtp_port, null, null, mail_admin, dontSendMails);

		/*
		 * Initialize template master.
		 */

		templateMaster = TemplateMaster.getInstance("default");
		templateMaster.addTemplateStorage(
				TemplateFileStorageManager.getInstance(servletContext.getRealPath("/"), "UTF-8"));

		loginHandler = new LoginTemplateHandler<User>();
		loginHandler.templateMaster = getTemplateMaster();
		loginHandler.templateName = login_template_name;
		loginHandler.title = "Webdanica - Login";
		loginHandler.adminPath = "/";

		/*
		 * Start thread workers.
		 */
		workflow = new WorkflowWorkThread(this, "Workflow");
		workflow.start();
		filterThread = new FilterWorkThread(this, "Seeds filtering");
		filterThread.start();
		workthreads = new WorkThreadAbstract[]{workflow,filterThread};
		
		/*
        monitoring = new MonitoringWorkThread(this, "Monitoring");
        workflow = new WorkflowWorkThread(this, "Workflow");
        lookup = new LookupWorkThread(this, "Lookup");
        pid = new PIDWorkThread(this, "PID");
        alive = new AliveWorkThread(this, "Alive");
        fetch = new FetchWorkThread(this, "Fetch", extractLimit, extractTempdir, archiveDir, waybackPrefix_value);
        wayback = new WaybackWorkThread(this, "Wayback");
        archive = new ArchiveWorkThread(this, "Archive");

        monitoring.start();
        workflow.start();
        lookup.start();
        pid.start();
        alive.start();
        fetch.start();
        wayback.start();
        archive.start();
		 */
		/** Send a mail to the mailAdmin that the system has started */
		String subject = "[Webdanica-"  + env + "] started";
		emailer.sendAdminEmail(subject, getStartMailContents(subject));
	}
	
	
	private String getStartMailContents(String subject) {
	    StringBuilder sb = new StringBuilder();
	    sb.append(subject);
	    sb.append(System.lineSeparator());
	    sb.append("Webdanica Webapp (version  " + getVersion() + ") started on server " + getServer() + " at '" + new Date() + "'");
	    
	    return sb.toString();
    }
    
    private String getServer() {
	    return SystemUtils.getLocalHostName(); // TODO maybe replace with something better (my code in the kbpillar project
    }

	private String getStopMailContents(String subject) {
    	StringBuilder sb = new StringBuilder();
   	    sb.append(subject);
   	    sb.append(System.lineSeparator());
   	    sb.append("Webdanica Webapp (version  " + getVersion() + ") stopped on server " + getServer() + " at '" + new Date() + "'");
   	    return sb.toString();
    }

	/**
     * Do some cleanup. This waits for the different workflow threads to stop running.
     */
    public void cleanup() {
		String subject = "[Webdanica-"  + env + "] stopping";
		emailer.sendAdminEmail(subject, getStopMailContents(subject));
		if (filterThread != null) {
			filterThread.stop();
		}
		if (workflow != null) {
            workflow.stop();
        }
	
		// Closing down working threads
		while (workflow.bRunning || filterThread.bRunning) {
            String threads = (
            		//monitoring.bRunning? " Monitoring": "") + 
            		(workflow.bRunning? " Workflow": "")
            		+ (filterThread.bRunning? " FilterThread": "")
            		/*
                    + (pid.bRunning? " PID": "")
                    + (alive.bRunning? " Alive": "")
                    + (fetch.bRunning? " Fetch": "")
                    + (wayback.bRunning? " Wayback": "")
                    */
                    )
            		;
            logger.log(Level.INFO, "Waiting for threads(" + threads + ") to exit.");
            try {
                Thread.sleep(5000); // Wait 5 seconds before trying again.
            } catch (InterruptedException e) {
            }
        }

		/*
    	if (wayback != null) {
        	wayback.stop();
    	}
    	if (fetch != null) {
        	fetch.stop();
    	}
    	if (alive != null) {
    		alive.stop();
    	}
        if (pid != null) {
            pid.stop();
        }
        if (lookup != null) {
            lookup.stop();
        }
        if (workflow != null) {
            workflow.stop();
        }
        if (monitoring != null) {
        	monitoring.stop();
        }
        while (workflow.bRunning || lookup.bRunning || pid.bRunning || alive.bRunning || fetch.bRunning || wayback.bRunning) {
            String threads = (monitoring.bRunning? " Monitoring": "")
            		+ (workflow.bRunning? " Workflow": "")
            		+ (lookup.bRunning? " Lookup": "")
                    + (pid.bRunning? " PID": "")
                    + (alive.bRunning? " Alive": "")
                    + (fetch.bRunning? " Fetch": "")
                    + (wayback.bRunning? " Wayback": "");
            logger.log(Level.INFO, "Waiting for threads(" + threads + ") to exit.");
            try {
                Thread.sleep(5000); // Wait 5 seconds before trying again.
            } catch (InterruptedException e) {
            }
        }
        wayback = null;
        fetch = null;
        alive = null;
        pid = null;
        lookup = null;
        workflow = null;
        monitoring = null;
        */
		filterThread = null;
		workflow = null;
        emailer = null;
        loginHandler = null;
        templateMaster=null;
        setServletConfig(null);
        seedDao.close();
    }

    public int getDefaultItemsPerPage() {
	    return defaultItemsPerPage;
    }
	
	private void setDefaultItemsPerPage(int defaultItemsPerPage) {
	    this.defaultItemsPerPage = defaultItemsPerPage;
    }


	public String getContextPath() {
	    return contextPath;
    }


	public void setContextPath(String contextPath) {
	    this.contextPath = contextPath;
    }


	public String getVersion() {
	    return version;
    }


	public ServletConfig getServletConfig() {
	    return servletConfig;
    }

	public void setServletConfig(ServletConfig servletConfig) {
	    this.servletConfig = servletConfig;
    }

	public String getSeedsPath() {
	    return seedsPath;
    }

	public void setSeedsPath(String seedsPath) {
	    this.seedsPath = seedsPath;
    }

	public String getSeedPath() {
	    return seedPath;
    }

	public void setSeedPath(String seedPath) {
	    this.seedPath = seedPath;
    }
	
	public WorkThreadAbstract[] getWorkThreads() {
		return this.workthreads;
    }


	public LoginTemplateHandler<User> getLoginHandler() {
	    return this.loginHandler;
    }

	public TemplateMaster getTemplateMaster() {
	    return this.templateMaster;
    }

	/// Path methods for blacklists: 
	
	public String getBlacklistsPath() {
	    return this.blacklistsPath;
    }

	public void setBlacklistsPath(String string) {
		this.blacklistsPath = string;
    }

	public String getBlacklistPath() {
	    return this.blacklistPath;
    }

	public void setBlacklistPath(String string) {
		this.blacklistPath = string;
    }

	/// Path methods for ingestlogs: 
	
	public String getIngestLogsPath() {
	    return this.ingestlogsPath;
    }

	public void setIngestLogsPath(String string) {
		this.ingestlogsPath = string;
    }

	public String getIngestLogPath() {
	    return this.ingestlogPath;
    }

	public void setIngestLogPath(String string) {
		this.ingestlogPath = string;
    }

	// TODO Add to configuration object for webdanica.
	public File getNetarchivesuiteSettingsFile() {
	    return this.netarchiveSuiteSettingsFile;
    }
	// TODO Add to configuration object for webdanica.
	public File getWebdanicaSettingsFile() {
	    return this.webdanicaSettingsFile;	    
    }
}
