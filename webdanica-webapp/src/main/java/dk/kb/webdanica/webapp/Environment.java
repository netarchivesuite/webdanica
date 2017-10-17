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

import dk.kb.webdanica.core.WebdanicaSettings;
import dk.kb.webdanica.core.utils.Settings;
import dk.kb.webdanica.core.utils.SettingsUtilities;
import dk.kb.webdanica.webapp.resources.BlackListResource;
import dk.kb.webdanica.webapp.resources.CriteriaResultResource;
import dk.kb.webdanica.webapp.resources.CriteriaResultsResource;
import dk.kb.webdanica.webapp.resources.DomainResource;
import dk.kb.webdanica.webapp.resources.HarvestResource;
import dk.kb.webdanica.webapp.resources.HarvestsResource;
import dk.kb.webdanica.webapp.resources.IngestLogResource;
import dk.kb.webdanica.webapp.resources.ResourcesMap;
import dk.kb.webdanica.webapp.resources.SeedsResource;
import dk.kb.webdanica.webapp.workflow.FilterWorkThread;
import dk.kb.webdanica.webapp.workflow.HarvestWorkThread;
import dk.kb.webdanica.webapp.workflow.StateCacheUpdateWorkThread;
import dk.kb.webdanica.webapp.workflow.WorkThreadAbstract;
import dk.kb.webdanica.webapp.workflow.WorkflowWorkThread;
import dk.netarkivet.common.CommonSettings;
import dk.netarkivet.common.exceptions.PermissionDenied;
import dk.netarkivet.common.utils.ApplicationUtils;
import dk.netarkivet.common.utils.FileUtils;
import dk.netarkivet.common.utils.StringUtils;
import dk.netarkivet.common.utils.SystemUtils;

/**
 *  Setup the environment for the Webdanica webapplication running on 8080 as a ROOT.war
 *  Initialization of the Environment instance requires the WEBDANICA_HOME to be set in the environment.
 */
public class Environment {

    /** Logging mechanism. */
    private static final Logger logger = Logger.getLogger(Environment.class.getName());

    /** servletConfig. */
    private ServletConfig servletConfig = null;

    private String version = null;

    private WorkThreadAbstract[] workthreads;

    /*
     * Paths.
     */

    private String contextPath;
    private String seedsPath;
    private String seedPath;
    private String blacklistsPath;
    private String blacklistPath;
    private String ingestlogsPath;
    private String ingestlogPath;
    private String criteriaResultPath;
    private String criteriaResultsPath;
    private String harvestsPath;
    private String harvestPath;
    private String domainsPath;
    private String domainPath;

    /*
     * Templates.
     */

    private TemplateMaster templateMaster = null;

    private String login_template_name = null;

    private LoginTemplateHandler<User> loginHandler = null;


    /*
     * WorkThreads.
     */

    private WorkflowWorkThread workflow;

    private FilterWorkThread filterThread;

    private HarvestWorkThread harvesterThread;

    private StateCacheUpdateWorkThread statecacheThread;


    /*
     * Schedules.
     */

    public ScheduleAbstract filterSchedule;

    public ScheduleAbstract harvestSchedule;

    public ScheduleAbstract cacheUpdatingSchedule;

    public boolean bScheduleHarvesting = false;
    public boolean bScheduleFiltering = false;
    public boolean bScheduleCacheUpdating = false;

    /*
     * Log.
     */

    public List<LogRecord> newLogRecords = new LinkedList<LogRecord>();

    public List<LogRecord> logRecords = new LinkedList<LogRecord>();

    private int defaultItemsPerPage = 25; // create settings

    private ServletContext servletContext;

    private File netarchiveSuiteSettingsFile;

    private File webdanicaSettingsFile;

    private ResourcesMap resourcesMap;

    private Configuration theconfig;

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

        // Test, if we run java 8 
        final int MIN_JAVA_VERSION = 8;
        final String java_version_used = System.getProperty("java.version"); 
        if (!isValidJavaVersion(MIN_JAVA_VERSION)) {
            throw new ServletException("The java version used by tomcat is too old. We now require a java " + MIN_JAVA_VERSION 
                    + ". The version used by tomcat is " + java_version_used);
        }
        String netarchiveSuiteSettings = getServletConfig().getInitParameter("netarchivesuite-settings");
        if (!netarchiveSuiteSettings.startsWith("/")) {
            netarchiveSuiteSettings = webdanicaHomeDir.getAbsolutePath() + "/" + netarchiveSuiteSettings;
        }
        netarchiveSuiteSettingsFile = new File(netarchiveSuiteSettings);
        if (netarchiveSuiteSettingsFile.isFile()) {	  	
            if (!SettingsUtilities.isValidSimpleXmlSettingsFile(netarchiveSuiteSettingsFile, true)) {
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

            if (!SettingsUtilities.isValidSimpleXmlSettingsFile(webdanicaSettingsFile, true)) {
                throw new ServletException("The parameter 'webdanica-settings' refers to a settingsfile containing invalid contents: " 
                        + webdanicaSettingsFile.getAbsolutePath());
            }
            System.setProperty("webdanica.settings.file", webdanicaSettingsFile.getAbsolutePath());
            Settings.reload();
        } else {
            throw new ServletException("The parameter 'webdanica-settings' refers to non-existing file: " 
                    + webdanicaSettingsFile.getAbsolutePath());
        }

        logger.info("Connected to NetarchiveSuite system with environmentname: " + 
                dk.netarkivet.common.utils.Settings.get(CommonSettings.ENVIRONMENT_NAME));

        // validate the netarchivesuite tmpdir folder
        File tmpdir = FileUtils.getTempDir();
        if (tmpdir.exists()){
            if (tmpdir.isFile()) {
                throw new ServletException("The netarchivesuite setting 'settings.common.tempDir'(" + tmpdir.getAbsolutePath() + ") is invalid: The setting refers to a file, not a directory");
            }
            File testFile = new File(tmpdir, System.currentTimeMillis()
                    + ".txt");
            boolean fileWasCreated = false;
            try {
                fileWasCreated = testFile.createNewFile();
            } catch (IOException e) {
                logger.warning("Unable to create file in tmpdir: " + tmpdir.getAbsolutePath());
            }
            if (!fileWasCreated) {
                throw new ServletException("The netarchivesuite setting 'settings.common.tempDir'(" + tmpdir.getAbsolutePath() + ") is invalid: we don't have write privileges to this directory");
            }
        } else {
            try {
                ApplicationUtils.dirMustExist(tmpdir);
            } catch (PermissionDenied e) {
                throw new ServletException("The netarchivesuite setting 'settings.common.tempDir'(" + tmpdir.getAbsolutePath() + ") is invalid: we don't have privileges to create this directory");
            }
        }
        
        logger.info("Using NetarchiveSuite 'settings.common.tempDir':" + tmpdir.getAbsolutePath());
        
        theconfig = Configuration.getInstance();
        logger.info("Following suffixes are currently ignored by the webdanica-project:" + StringUtils.conjoin(",", 
                theconfig.getIgnoredSuffixes()));
        logger.info("Following protocols (schemas) are currently by the webdanica-project:" + StringUtils.conjoin(",", 
                theconfig.getAcceptedProtocols()));

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
        String filteringCrontab = SettingsUtilities.getStringSetting(WebdanicaSettings.WEBAPP_CRONTAB_FILTERING, dk.kb.webdanica.webapp.Constants.DEFAULT_FILTERING_CRONTAB); 
        String harvestingCrontab = SettingsUtilities.getStringSetting(WebdanicaSettings.WEBAPP_CRONTAB_HARVESTING, dk.kb.webdanica.webapp.Constants.DEFAULT_HARVESTING_CRONTAB);
        String statecachingCrontab = SettingsUtilities.getStringSetting(WebdanicaSettings.WEBAPP_CRONTAB_STATECACHING, dk.kb.webdanica.webapp.Constants.DEFAULT_STATECACHING_CRONTAB);

        filterSchedule = CrontabSchedule.crontabFactory(filteringCrontab);
        harvestSchedule = CrontabSchedule.crontabFactory(harvestingCrontab);
        cacheUpdatingSchedule = CrontabSchedule.crontabFactory(statecachingCrontab);


        // Read resources and their secured status from settings.
        // TODO Currently the resourcesMap.getResourceByPath(path) always returns a ResourceDescription
        // if the resource is not found, it sets the secure-status as false, later it will be true (login required)
        resourcesMap = new ResourcesMap();

        // initialize the necessary paths of the webapp

        this.contextPath = servletContext.getContextPath();
        this.blacklistPath = getContextPath() + BlackListResource.BLACKLIST_PATH;
        this.blacklistsPath = getContextPath() + BlackListResource.BLACKLISTS_PATH;		
        this.seedPath = getContextPath() + SeedsResource.SEED_PATH;
        this.seedsPath = getContextPath() + SeedsResource.SEEDS_PATH;
        this.harvestPath = getContextPath() + HarvestResource.HARVEST_PATH;
        this.harvestsPath = getContextPath() + HarvestsResource.HARVESTS_PATH;
        this.criteriaResultPath = getContextPath() + CriteriaResultResource.CRITERIA_RESULT_PATH;
        this.criteriaResultsPath = getContextPath() + CriteriaResultsResource.CRITERIA_RESULTS_PATH;
        this.domainPath = getContextPath() + DomainResource.DOMAIN_PATH;
        this.domainsPath = getContextPath() + DomainResource.DOMAIN_LIST_PATH;
        this.ingestlogPath = getContextPath() + IngestLogResource.INGESTLOG_PATH;
        this.ingestlogsPath = getContextPath() + IngestLogResource.INGESTLOGS_PATH;
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
        harvesterThread = new HarvestWorkThread(this, "Harvest worker");
        harvesterThread.start();
        statecacheThread = new StateCacheUpdateWorkThread(this, "StateCache Update worker"); 
        statecacheThread.start();

        workthreads = new WorkThreadAbstract[]{workflow,filterThread, harvesterThread, statecacheThread};

        /** Send a mail to the mailAdmin that the system has started */
        String subject = "[Webdanica-"  + theconfig.getEnv() + "] started";
        theconfig.getEmailer().sendAdminEmail(subject, getStartMailContents(subject));
    }

    private String getStartMailContents(String subject) {
        StringBuilder sb = new StringBuilder();
        sb.append(subject);
        sb.append(System.lineSeparator());
        sb.append("Webdanica Webapp (version " + getVersion() + ") started on server " + getServer() + " at '" + new Date() + "'");

        return sb.toString();
    }

    private String getServer() {
        return SystemUtils.getLocalHostName();
    }

    private String getStopMailContents(String subject) {
        StringBuilder sb = new StringBuilder();
        sb.append(subject);
        sb.append(System.lineSeparator());
        sb.append("Webdanica Webapp (version " + getVersion() + ") stopped on server " + getServer() + " at '" + new Date() + "'");
        return sb.toString();
    }

    /**
     * Do some cleanup. This waits for the different workflow threads to stop running.
     */
    public void cleanup() {
        String subject = "[Webdanica-"  + theconfig.getEnv() + "] stopping";
        theconfig.getEmailer().sendAdminEmail(subject, getStopMailContents(subject));
        if (filterThread != null) {
            filterThread.stop();
        }		
        if (harvesterThread != null) {
            harvesterThread.stop();
        }
        if (statecacheThread != null) {
            statecacheThread.stop();
        }


        if (workflow != null) {
            workflow.stop();
        }

        // Closing down working threads

        while (workflow.bRunning || filterThread.bRunning || harvesterThread.bRunning) {
            String threads = (
                    //monitoring.bRunning? " Monitoring": "") + 
                    (workflow.bRunning? " Workflow": "")
                    + (filterThread.bRunning? " FilterThread": "")
                    + (harvesterThread.bRunning? " HarvesterThread": "")
                    + (statecacheThread.bRunning? " StateCacheThread": ""))
                    ;
            logger.log(Level.INFO, "Waiting for threads(" + threads + ") to exit.");
            try {
                Thread.sleep(5000); // Wait 5 seconds before trying again.
            } catch (InterruptedException e) {
                //
            }
        }
        statecacheThread = null;
        harvesterThread = null;
        filterThread = null;
        workflow = null;
        loginHandler = null;
        templateMaster = null;
        setServletConfig(null);
        // Should we close all the dao classes independently
        theconfig.close();
    }

    public int getDefaultItemsPerPage() {
        return defaultItemsPerPage;
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


    public WorkThreadAbstract[] getWorkThreads() {
        return this.workthreads;
    }


    public LoginTemplateHandler<User> getLoginHandler() {
        return this.loginHandler;
    }

    public TemplateMaster getTemplateMaster() {
        return this.templateMaster;
    }

    ////////////////////////////////////
    // Path  methods
    ////////////////////////////////////

    public String getContextPath() {
        return contextPath;
    }

    public String getSeedsPath() {
        return seedsPath;
    }

    public String getSeedPath() {
        return seedPath;
    }

    public String getBlacklistsPath() {
        return this.blacklistsPath;
    }

    public String getBlacklistPath() {
        return this.blacklistPath;
    }

    public String getIngestLogsPath() {
        return this.ingestlogsPath;
    }

    public String getIngestLogPath() {
        return this.ingestlogPath;
    }

    public String getCriteriaResultPath() {
        return this.criteriaResultPath;
    }

    public String getCriteriaResultsPath() {
        return this.criteriaResultsPath;
    }

    public String getHarvestsPath() {
        return this.harvestsPath;
    }

    public String getHarvestPath() {
        return this.harvestPath;
    }

    public String getDomainsPath() {
        return this.domainsPath;
    }

    public String getDomainPath() {
        return this.domainPath;
    }

    public Configuration getConfig() {
        return this.theconfig;
    }

    public File getNetarchivesuiteSettingsFile() {
        return this.netarchiveSuiteSettingsFile;
    }
    public File getWebdanicaSettingsFile() {
        return this.webdanicaSettingsFile;	    
    }

    public ResourcesMap getResourcesMap() {
        return this.resourcesMap;	    
    }
    
    public static boolean isValidJavaVersion(int minVersion) {
        String[] version = System.getProperty("java.version").split("\\.");
        if (version.length != 3) {
            // Don't verify the version any more. But return true
            return true;
        } else {  // sample version: 1.8.0_141
            int thisVersion = Integer.valueOf(version[1]);
            return thisVersion >= minVersion;
        }
    }
}
