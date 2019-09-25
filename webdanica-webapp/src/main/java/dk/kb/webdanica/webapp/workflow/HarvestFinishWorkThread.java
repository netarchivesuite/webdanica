package dk.kb.webdanica.webapp.workflow;

import dk.kb.webdanica.core.WebdanicaSettings;
import dk.kb.webdanica.core.datamodel.Cache;
import dk.kb.webdanica.core.datamodel.Seed;
import dk.kb.webdanica.core.datamodel.Status;
import dk.kb.webdanica.core.datamodel.dao.HarvestDAO;
import dk.kb.webdanica.core.datamodel.dao.SeedsDAO;
import dk.kb.webdanica.core.interfaces.harvesting.HarvestLog;
import dk.kb.webdanica.core.interfaces.harvesting.NetarchiveSuiteTools;
import dk.kb.webdanica.core.interfaces.harvesting.SingleSeedHarvest;
import dk.kb.webdanica.core.utils.SettingsUtilities;
import dk.kb.webdanica.webapp.Configuration;
import dk.kb.webdanica.webapp.Constants;
import dk.kb.webdanica.webapp.Environment;
import dk.netarkivet.common.distribute.arcrepository.ArcRepositoryClientFactory;
import dk.netarkivet.harvester.datamodel.DBSpecifics;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The workthread responsible for finishing harvests of seeds with Status.HARVESTING_IN_PROGRESS.
 * then fetching the important information about the finished harvests from Netarchivesuite, 
 * and the warc files produced by the harvests.
 *
 *  FIXME This code lacks refinement, as the code now just for the job of the harvest to finish, and then fetches the reports from the harvest
 */
public class HarvestFinishWorkThread extends WorkThreadAbstract {

    static {
        logger = Logger.getLogger(HarvestFinishWorkThread.class.getName());
    }

    private List<Seed> queueList = new LinkedList<Seed>();

    private List<Seed> workList = new LinkedList<Seed>();

    private SeedsDAO seeddao;

    private HarvestDAO harvestdao;

    private Configuration configuration;

    private boolean harvestingEnabled = false;

    private AtomicBoolean harvestingInProgress = new AtomicBoolean(false);

    private int maxHarvestsAtaTime;


    /**
     * Constructor for the Harvester thread worker object.
     *
     * @param environment The Webdanica webapp environment object
     * @param threadName The name of the thread
     */
    public HarvestFinishWorkThread(Environment environment, String threadName) {
        this.environment = environment;
        this.threadName = threadName;
    }


    public void enqueue(List<Seed> urlRecords) {
        synchronized (queueList) {
            queueList.addAll(urlRecords);
        }
    }

    @Override
    protected void process_init() {
        configuration = Configuration.getInstance();
        seeddao = configuration.getDAOFactory().getSeedsDAO();
        harvestdao = configuration.getDAOFactory().getHarvestDAO();
        File harvestLogDir = configuration.getHarvestLogDir();
        maxHarvestsAtaTime = SettingsUtilities.getIntegerSetting(
            WebdanicaSettings.HARVESTING_MAX_SINGLESEEDHARVESTS,
            Constants.DEFAULT_MAX_HARVESTS);

        // Verify that database driver exists in classpath. If not exit program
        String dbdriver = DBSpecifics.getInstance().getDriverClassName();
        if (!SettingsUtilities.verifyClass(dbdriver, false)) {
            String errMsg = "HarvestWorkFlow will not be enabled as the necessary databasedriver to connect to Netarchivesuite '"
                    + dbdriver + "' does not exist in the classpath";
            logger.log(Level.WARNING, errMsg);
            configuration.getEmailer().sendAdminEmail(
                    "[Webdanica-" + configuration.getEnv()
                            + "] HarvestFinishWorkflow not enabled", errMsg);
            return;
        }
        // Verify that arcrepositoryclient exists in classpath. If not exit
        // program
        String arcrepositoryClient = dk.netarkivet.common.utils.Settings
                .get("settings.common.arcrepositoryClient.class");
        if (!SettingsUtilities.verifyClass(arcrepositoryClient, false)) {
            String errMsg = "HarvestWorkFlow will not be enabled as the necessary acrepositoryClient '"
                    + arcrepositoryClient + "' does not exist in the classpath";
            logger.log(Level.WARNING, errMsg);
            configuration.getEmailer().sendAdminEmail(
                    "[Webdanica-" + configuration.getEnv()
                            + "] HarvestFinishWorkflow not enabled", errMsg);
            return;
        }
        try {
            ArcRepositoryClientFactory.getViewerInstance();
        } catch (Throwable e) {
            String errMsg = "HarvestWorkFlow will not be enabled as the necessary acrepositoryClient '"
                    + arcrepositoryClient + "' has a invalid configuration. We get the following exception: ";
            logger.log(Level.WARNING, errMsg, e);
            configuration.getEmailer().sendAdminEmail(
                    "[Webdanica-" + configuration.getEnv()
                            + "] HarvestFinishWorkflow", errMsg + ExceptionUtils.getFullStackTrace(e));
            return;
        }

        if (maxHarvestsAtaTime < 1) {
            configuration.getEmailer().sendAdminEmail(
                    "[Webdanica-" + configuration.getEnv()
                            + "] HarvestFinishWorkflow not enabled",
                    "Maxharvests is less than 1: " + maxHarvestsAtaTime);
            logger.info("MaxHarvests is less than 1. So HarvestFinishWorkflow is disabled!");
            return;
        }

        if (existsLogdirAndIsWritable(harvestLogDir)) {
            harvestingEnabled = true;
            logger.info("All requirements fullfilled for harvesting. So harvestingEnabled is set to true");
        }

    }
    
    /**
     * Check if the given harvestLogDir exists and is writable.
     * If not, we will not enable the harvestworkflow.
     * @param harvestLogDir a given directory, where to the harvestlogs produced by the harvestworkflow is written.  
     * @return true, if the given harvestLogDir exists and is writable, else false
     */
    private boolean existsLogdirAndIsWritable(File harvestLogDir) {
        if (!harvestLogDir.isDirectory()) {
            String errMsg = "HarvestWorkFlow will not be enabled as the given directory '"
                    + harvestLogDir.getAbsolutePath()
                    + "' does not exist or is not a proper directory";
            logger.log(Level.WARNING, errMsg);
            configuration.getEmailer().sendAdminEmail(
                    "[Webdanica-" + configuration.getEnv()
                            + "] HarvestFinishWorkflow not enabled", errMsg);
            return false;
        }
        logger.info("Trying to write a file to dir '"
                + harvestLogDir.getAbsolutePath()
                + "' with the correct permissions");
        File testFile = new File(harvestLogDir, System.currentTimeMillis()
                + ".txt");

        try {
            boolean fileWasCreated = testFile.createNewFile();
            if (!fileWasCreated) {
                String errMsg = "HarvestWorkFlow will not be enabled as we're unable to write to the directory '"
                        + harvestLogDir.getAbsolutePath() + "'";
                logger.log(Level.WARNING, errMsg);
                configuration.getEmailer().sendAdminEmail(
                        "[Webdanica-" + configuration.getEnv()
                                + "] HarvestFinishWorkflow not enabled", errMsg);
                return false;
            }
            // Try setting the just created file to writable by all
            boolean success = testFile.setWritable(true, false);
            if (!success) {
                String errMsg = "HarvestWorkFlow will not be enabled as we're unable to set the correct permissions when writing a file (e.g rw_rw_rw) to dir '"
                        + harvestLogDir.getAbsolutePath() + "'";
                logger.log(Level.WARNING, errMsg);
                configuration.getEmailer().sendAdminEmail(
                        "[Webdanica-" + configuration.getEnv()
                                + "] HarvestFinishWorkflow not enabled", errMsg);
                return false;
            } else {
                if (!testFile.delete()) {
                    logger.log(Level.WARNING, "Unable to delete testfile '"
                                + testFile.getAbsolutePath() + "'");
                }

                return true;
            }
        } catch (IOException e) {
            String errMsg = "IOException thrown during check that harvestLogDir '"
                    + harvestLogDir.getAbsolutePath() + "' is writable:" + e;
            configuration.getEmailer().sendAdminEmail(
                    "[Webdanica-" + configuration.getEnv()
                            + "] HarvestFinishWorkflow not enabled", errMsg);
            logger.log(Level.WARNING, errMsg);
            return false;
        } catch (SecurityException e) {
            String errMsg = "SecurityException thrown during check that harvestLogDir '"
                    + harvestLogDir.getAbsolutePath() + "' is writable:" + e;
            configuration.getEmailer().sendAdminEmail(
                    "[Webdanica-" + configuration.getEnv()
                            + "] HarvestFinishWorkflow not enabled", errMsg);
            logger.log(Level.WARNING, errMsg);
            return false;
        }
    }

    @Override
    protected void process_run() {
        if (!harvestingEnabled) {
            return;
        }
        // Check the harvest schedule 
        if (!environment.bScheduleHarvestingFinish) {
            return;
        }
        logger.log(Level.INFO, "Running process of thread '" + threadName
               + "' at '" + new Date() + "'");
        if (harvestingInProgress.get()) {
            logger.log(Level.INFO,
                    "Harvesting process already in progress at '" + new Date()
                            + "'. Skipping");
            return;
        } else {
            harvestingInProgress.set(Boolean.TRUE);
        }
        List<Seed> seedsBeingHarvested = null;

        try {
            seedsBeingHarvested = seeddao.getSeeds(
                    Status.HARVESTING_IN_PROGRESS, maxHarvestsAtaTime);
        } catch (Throwable e) {
            String errMsg = "Exception throwgetHarvestLogDirn during method HarvestFinishWorkThread.process_run:"
                    + e;
            logger.log(Level.WARNING, errMsg);
            harvestingInProgress.set(Boolean.FALSE);
            configuration
                    .getEmailer()
                    .sendAdminEmail(
                            "[Webdanica-"
                                    + configuration.getEnv()
                                    + "] HarvestFinishWorkflow failed - unable to receive seeds with status '"
                                    + Status.HARVESTING_IN_PROGRESS + "'", errMsg);
            return;
        }

        enqueue(seedsBeingHarvested);
        if (!seedsBeingHarvested.isEmpty()) {
            logger.log(Level.FINE, "Found '" + seedsBeingHarvested.size()
                    + "' seeds being harvested");
        }
        try {
            synchronized (queueList) {
                for (int i = 0; i < queueList.size(); ++i) {
                    Seed urlRecord = queueList.get(i);
                    workList.add(urlRecord);
                }
                queueList.clear();
            }
            if (!workList.isEmpty()) {
                logger.log(Level.INFO, "Starting processing of " + workList.size()
                        + " seeds currently being harvested");
                lastWorkRun = System.currentTimeMillis();
                startProgress(workList.size());
                harvest(workList);
                stopProgress();
                workList.clear();
                // Update cache
                Cache.updateCache(configuration.getDAOFactory());
            }
        } catch (Throwable e) {
            logger.log(Level.SEVERE, e.toString(), e);
        } finally {
            harvestingInProgress.set(Boolean.FALSE);
        }
    }

    /**
     * Initiate and finish harvests of the Seed objects in the given workList
     * @param workList a given workList of Seed objects.
     */
    private void harvest(List<Seed> workList) {
        List<SingleSeedHarvest> harvests = new ArrayList<SingleSeedHarvest>();
        for (Seed s : workList) {
            boolean failure = false;
            boolean harvestSuccess;

            Throwable savedException = null;
            String eventHarvestName = null;
            String failureReason = "";
            try {
                // Hent den harvest med Constants.DUMMY_HARVESTED_TIME_MEANING_IN_PROGRESS hvis den findes
                SingleSeedHarvest ssh = null;
                for (SingleSeedHarvest h:  harvestdao.getAllWithSeedurl(s.getUrl())){
                    if (h.getHarvestedTime() == Constants.DUMMY_HARVESTED_TIME_MEANING_IN_PROGRESS) {
                        ssh = h;
                    }
                }
                logger.info(String.format("Found unfinished harvest '%s' for seed '%s'", ssh.getHarvestName(), s.getUrl()));
                Long hid = NetarchiveSuiteTools.getHarvestDefinitionID(ssh.getHarvestName());
                if (hid == null) {
                    failureReason = "No harvestid found for harvestname " + ssh.getHarvestName();
                    failure = true;
                } else {
                    // update ssh with the hid so we can continue the finish-fase as before
                    ssh.setHarvestdefinitionID(hid);
                    // FIXME needs refinement. This just waits until the harvest is done if not finished already
                    harvestSuccess = ssh.finishHarvest(false);

                    if (harvestSuccess) {
                        harvests.add(ssh);
                        // update harvest with more information after the harvest is finished
                        harvestdao.insertHarvest(ssh); // insert does the same as update
                    } else {
                        // update harvest with new harvestedTime to avoid processing the harvest again if the seed is revisited later
                        ssh.setHarvestedTime(Constants.DUMMY_HARVESTED_TIME_MEANING_FAILED);
                        harvestdao.insertHarvest(ssh); // insert does the same as update
                        failure = true;
                    }
                }

            } catch (Exception e) {
                logger.log(Level.SEVERE, e.toString(), e);
                failure = true;
                savedException = e;
            } finally {
                if (failure) {
                    s.setStatus(Status.HARVESTING_FAILED);
                    if (savedException != null) {
                        s.setStatusReason("Harvesting of seed (harvestname='"
                                + eventHarvestName
                                + "') failed due to exception: "
                                + ExceptionUtils.getFullStackTrace(savedException));
                    } else if (!failureReason.isEmpty()) {
                        s.setStatusReason("Harvesting of seed (harvestname='"
                                + eventHarvestName + "') failed. Reason: "
                                + failureReason);
                    } else {
                        s.setStatusReason("Harvesting of seed (harvestname='"
                                + eventHarvestName
                                + "') failed -  reason is unknown");
                    }
                } else {
                    s.setStatus(Status.READY_FOR_ANALYSIS);
                    s.setStatusReason("Harvesting finished successfully. harvestname is '"
                            + eventHarvestName + "'. Now ready for analysis");
                }
                try {
                    seeddao.updateSeed(s);
                } catch (Exception e) {
                    String errMsg = "Unable to save state of seed: "
                            + ExceptionUtils.getFullStackTrace(e);
                    logger.log(Level.SEVERE, errMsg, e);
                    configuration
                            .getEmailer()
                            .sendAdminEmail(
                                    "[Webdanica-"
                                            + configuration.getEnv()
                                            + "] HarvestFinishWorkFlow failed - unable to save state of seed w/url '"
                                            + s.getUrl() + "'", errMsg);
                }
            }

        }

        try {
            if (!harvests.isEmpty()) {
                writeHarvestLog(harvests, configuration);
            } else {
                logger.warning("No seeds harvested successfully out of " + workList.size() + " seeds"); 
            }
        } catch (Throwable e) {
            String errMsg = "Unable to write a harvestlog to directory '"
                    + configuration.getHarvestLogDir() + "': " + ExceptionUtils.getFullStackTrace(e);
            logger.log(Level.SEVERE, errMsg, e);
            configuration
                    .getEmailer()
                    .sendAdminEmail(
                            "[Webdanica-"
                                    + configuration.getEnv()
                                    + "] HarvestFinishWorkFlow failure - unable to write harvestlog to disk",
                            errMsg);
        }
    }

    @Override
    protected void process_cleanup() {
        // nothing to do currently
    }

    @Override
    public int getQueueSize() {
        int queueSize = 0;
        synchronized (queueList) {
            queueSize = queueList.size();
        }
        return queueSize;
    }
    
    /**
     * Write a list of SingleSeedHarvests to write to a harvestLog.
     * We assume that the harvestLogDir is writable.
     * Only successful harvests are written to the log.
     * @param harvests a list of SingleSeedHarvests
     * @param conf the configuration used by the workflows.
     * @throws IOException if unable to write to harvestlog
     */
    public static void writeHarvestLog(List<SingleSeedHarvest> harvests,
            Configuration conf) throws IOException {
        String systemTimestamp = SingleSeedHarvest.getTimestamp();
        String logNameInitial = conf.getHarvestLogPrefix() + systemTimestamp
                + conf.getHarvestLogNotReadySuffix();
        String logNameFinal = conf.getHarvestLogPrefix() + systemTimestamp
                + conf.getHarvestLogReadySuffix();
        File harvestLogDir = conf.getHarvestLogDir();
        File harvestLog = new File(harvestLogDir, logNameInitial);
        File harvestLogFinal = new File(harvestLogDir, logNameFinal);
        String harvestLogHeader = HarvestLog.harvestLogHeaderPrefix
                + " harvests initiated by the Webdanica webapp at "
                + new Date();

        // write harvestreport to disk where cronjob have privileges to read,
        // and move the file to a different location
        int written = HarvestLog.writeHarvestLog(harvestLog,
                harvestLogHeader, true, harvests, false);
        if (written == 0) {
            logger.log(Level.WARNING, "No harvests out of " + harvests.size()
                    + " investigated finished successfully, and no harvestlog is written");
            // remove empty harvestlog
            boolean deleted = harvestLog.delete();
            if (!deleted) {
                logger.log(Level.WARNING, "Unable to delete empty harvestlog '"
                        + harvestLog.getAbsolutePath() + "'");
            }
            return;
        }
        boolean success = harvestLog.setWritable(true, false);
        if (!success) {
            logger.log(Level.SEVERE,
                    "Unable to give the harvestlog the correct permissions");
        }
        harvestLog.renameTo(harvestLogFinal);
        // Do we need to set the permissions again?
        logger.info("A harvestlog with " + written + "/" + harvests.size()
                + " results has now been written to file '"
                + harvestLogFinal.getAbsolutePath() + "'");
    }

}
