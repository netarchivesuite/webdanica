/*
 * Created on 18/06/2013
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

package dk.kb.webdanica.webapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.sql.DataSource;

import com.antiaction.common.cron.CrontabSchedule;
import com.antiaction.common.cron.ScheduleAbstract;
import com.antiaction.common.templateengine.TemplateMaster;
import com.antiaction.common.templateengine.login.LoginTemplateHandler;
import com.antiaction.common.templateengine.storage.TemplateFileStorageManager;
import com.antiaction.multithreading.datasource.DataSourceReference;

import dk.kb.webdanica.WebdanicaSettings;
import dk.kb.webdanica.datamodel.Cassandra;
import dk.kb.webdanica.datamodel.SeedDAO;
import dk.kb.webdanica.utils.Settings;
import dk.kb.webdanica.webapp.workflow.FilterWorkThread;
import dk.kb.webdanica.webapp.workflow.WorkflowWorkThread;
import dk.netarkivet.common.CommonSettings;
import dk.netarkivet.common.utils.StringUtils;


public class Environment {

    /** Logging mechanism. */
    private static final Logger logger = Logger.getLogger(Environment.class.getName());

    public static final String waybackPrefixDefault = "http://kb-test-dab-01.kb.dk:8080/wayback/";

    public static final String DEFAULT_LOOKUP_CRONTAB = "0 * * * *";
	public static final String DEFAULT_PID_CRONTAB = "0 0 * * *";
	public static final String DEFAULT_ALIVECHECK_CRONTAB = "0 0 * * *";
	public static final String DEFAULT_FETCH_CRONTAB = "0 * * * *";
	public static final String DEFAULT_WAYBACKCHECK_CRONTAB = "0 0 * * *";
	public static final String DEFAULT_ARCHIVECHECK_CRONTAB = "0 0 * * *";
	public static final String DEFAULT_EMAIL_CRONTAB = "0 0 * * *";

    /** servletConfig. */
    public ServletConfig servletConfig = null;

	public String version = null;

    public String env;

    //public File webInfFile;

    /*
     * Paths.
     */

    public String contextPath;

    public String seedsPath;
	public String seedPath;
    
    /*
     * Templates.
     */

    public TemplateMaster templateMaster = null;

    private String login_template_name = null;

    public LoginTemplateHandler<User> loginHandler = null;

    /*
     * Misc.
     */

    /** Database <code>DataSource</code> object. */
    //public DataSource dataSource = null;

    /*
     * WorkThreads.
     */

    public WorkflowWorkThread workflow;

    public FilterWorkThread filterThread;
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

	private Cassandra db;

	private String mail_admin;

	public int defaultItemsPerPage = 25; // create settings

	public SeedDAO seedDao;

	

    /**
     * @param servletContext
     * @param theServletConfig
     * @throws ServletException
     */
    public Environment(ServletContext servletContext, ServletConfig theServletConfig) throws ServletException {
        this.servletConfig = theServletConfig;

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

        String netarchiveSuiteSettings = servletConfig.getInitParameter("netarchivesuite-settings");
        String webdanicaSettings = servletConfig.getInitParameter("webdanica-settings");
        
        if (!netarchiveSuiteSettings.startsWith("/")) {
        	netarchiveSuiteSettings = webdanicaHomeDir.getAbsolutePath() + "/" + netarchiveSuiteSettings;
        }
        File netarchiveSuiteSettingsFile = new File(netarchiveSuiteSettings);
        
        
        if (netarchiveSuiteSettingsFile.isFile()) {
        	
        	/* TODO validate netarchiveSuite settings file (evt. workaround for the missing isValidSettingsfile
        	if (!dk.netarkivet.common.utils.Settings.isValidSettingsfile(netarchiveSuiteSettingsFile)) {
        		throw new ServletException("The parameter 'webdanica-settings' refers to settingsfile containing invalid contents: " 
            			+ webdanicaSettingsFile.getAbsolutePath());
        	}
        	
        	System.setProperty("dk.netarkivet.settings.file", netarchiveSuiteSettingsFile.getAbsolutePath());
        	dk.netarkivet.common.utils.Settings.reload();
        	*/
        	System.setProperty("dk.netarkivet.settings.file", netarchiveSuiteSettingsFile.getAbsolutePath());
        } else {
        	throw new ServletException("The parameter 'netarchivesuite-settings' refers to non-existing file: " 
        			+ netarchiveSuiteSettingsFile.getAbsolutePath());
        }
        
        
        if (!webdanicaSettings.startsWith("/")) {
        	webdanicaSettings = webdanicaHomeDir.getAbsolutePath() + "/" + webdanicaSettings;
        }
        File webdanicaSettingsFile = new File(webdanicaSettings);
        if (webdanicaSettingsFile.isFile()) {
        	
        	if (!Settings.isValidSettingsfile(webdanicaSettingsFile)) {
        		throw new ServletException("The parameter 'webdanica-settings' refers to settingsfile containing invalid contents: " 
            			+ webdanicaSettingsFile.getAbsolutePath());
        	}
        	
        	System.setProperty("webdanica.settings.file", webdanicaSettingsFile.getAbsolutePath());
        	Settings.reload();
        } else {
        	throw new ServletException("The parameter 'webdanica-settings' refers to non-existing file: " 
        			+ webdanicaSettingsFile.getAbsolutePath());
        }

        /*
         * Env. (TEST/STAGING/PROD)
         */
        env = "UNKNOWN";
        if (Settings.hasKey(WebdanicaSettings.ENVIRONMENT)) {
        	String envString = Settings.get(WebdanicaSettings.ENVIRONMENT);
        	if (!envString.isEmpty()) {
        		env = envString.toUpperCase();
        	}
        }
        
        /*
         * Read SMTP settings (smtp-host, smtp-port).
         */
        String smtp_host;
        if (Settings.hasKey(WebdanicaSettings.MAIL_SERVER) 
        		&& !Settings.get(WebdanicaSettings.MAIL_SERVER).isEmpty()) {
        	smtp_host = Settings.get(WebdanicaSettings.MAIL_SERVER);
        } else {
        	logger.warning("smtp-host not defined, using 'localhost' instead");
        	smtp_host = "localhost";
        }
        
        int smtp_port = 25;
        /*
        if (Settings.hasKey(WebdanicaSettings.MAIL_PORT) 
        		&& !Settings.getInt(WebdanicaSettings.MAIL_SERVER)) {
        	smtp_host = Settings.get(WebdanicaSettings.MAIL_SERVER);
        } else {
        	logger.warning("smtp-host not defined, using 'localhost' instead");
        	smtp_host = "localhost";
        }
        int smtp_port = getIntegerInitParameter("smtp-port", default_smtp_port);
        */
        
        if (Settings.hasKey(WebdanicaSettings.MAIL_ADMIN) 
        		&& !Settings.get(WebdanicaSettings.MAIL_ADMIN).isEmpty()) {
        	mail_admin = Settings.get(WebdanicaSettings.MAIL_ADMIN);
        } else {
        	logger.warning("mail-admin not defined, using 'svc@kb.dk' instead");
        	mail_admin = "svc@kb.dk";
        }
        
        logger.info("Connected to NetarchiveSuite system with environmentname: " + 
        		dk.netarkivet.common.utils.Settings.get(CommonSettings.ENVIRONMENT_NAME));
        String[] ignoredSuffixes = Settings.getAll(WebdanicaSettings.IGNORED_SUFFIXES);
        String[] ignoredProtocols = Settings.getAll(WebdanicaSettings.IGNORED_PROTOCOLS);
        logger.info("Following suffixes are currently ignored by webdanica-project:" + StringUtils.conjoin(",", 
        		ignoredSuffixes));
        
        logger.info("Following protocols are currently ignored by webdanica-project:" + StringUtils.conjoin(",", 
        		ignoredProtocols));
        
        /*
         * DataSource.
         */

        String db_url = servletConfig.getInitParameter("db-url");
        String db_username = servletConfig.getInitParameter("db-username");
        String db_password = servletConfig.getInitParameter("db-password");        
        
        /*
         * Templates.
         */

        login_template_name = servletConfig.getInitParameter("login-template");

        if (login_template_name != null && login_template_name.length() > 0) {
            logger.info("Using '" +  login_template_name + "' as login template.");
        } else {
            throw new ServletException("'login_template_name' must be configured!");
        }

       

        /*
         * Crontabs.
         */

        String lookupCrontab = servletConfig.getInitParameter("lookup-crontab");
        String pidCrontab = servletConfig.getInitParameter("pid-crontab");
        String aliveCheckCrontab = servletConfig.getInitParameter("alive-crontab");
        String fetchCrontab = servletConfig.getInitParameter("fetch-crontab");
        String waybackCheckCrontab = servletConfig.getInitParameter("check-crontab");
        String archiveCheckCrontab = servletConfig.getInitParameter("archive-crontab");
        String emailCrontab = servletConfig.getInitParameter("email-crontab");
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

        db = new Cassandra(); // TODO make a Connect class that hides away the DB specifics.
        
        seedDao = SeedDAO.getInstance(); 
        
        /*
         * Initialize emailer
         */
        
        emailer = Emailer.getInstance(smtp_host, smtp_port, null, null, mail_admin);

        /*
         * Initialize database configuration (dataSource)
         */
        
        Map<String, String> attribs = new HashMap<String, String>();
        Map<String, String> props = new HashMap<String, String>();

        attribs.put("driver-class", "org.postgresql.Driver");
        attribs.put("connection-url", db_url);
        attribs.put("user-name", db_username);
        attribs.put("password", db_password);
        //dataSource = DataSourceReference.getDataSource(attribs, props);

        /*
         * Initialize template master.
         */

        templateMaster = TemplateMaster.getInstance("default");
        templateMaster.addTemplateStorage(TemplateFileStorageManager.getInstance(servletContext.getRealPath("/"), "UTF-8"));

        loginHandler = new LoginTemplateHandler<User>();
        loginHandler.templateMaster = templateMaster;
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
		String subject = "[Webdanica-"  + env + "] started";
		sendAdminEmail(subject, "");
    }

    private int getIntegerInitParameter(String parameter, int defaultValue) throws ServletException {
    	String valueStr = servletConfig.getInitParameter(parameter);
    	int value = defaultValue;
    	if (valueStr != null && valueStr.length() > 0) {
            try {
            	value = Integer.parseInt(valueStr);
            } catch (NumberFormatException e) {
                throw new ServletException("'" + parameter + "' must be a valid integer > 0! " + "Unable to extract valid integer from string '" + valueStr + "'.");
            }
        } else {
            logger.warning("'" + parameter + "' set to default value '" + defaultValue + "' instead of read from web.xml");
        }
    	return value;
	}
    
	/**
     * Do some cleanup. This waits for the different workflow threads to stop running.
     */
    public void cleanup() {
		String subject = "[Webdanica-"  + env + "] stopping";
		sendAdminEmail(subject, "");
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
        emailer = null;
        loginHandler = null;
        templateMaster = null;
        //dataSource = null;
        servletConfig = null;
        db.close();
        
    }

    public void sendAdminEmail(String subject, String body) {
		emailer.send(mail_admin, subject, body);
    }

}
