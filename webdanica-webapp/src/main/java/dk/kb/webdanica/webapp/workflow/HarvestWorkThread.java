package dk.kb.webdanica.webapp.workflow;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import dk.kb.webdanica.core.WebdanicaSettings;
import dk.kb.webdanica.core.datamodel.Seed;
import dk.kb.webdanica.core.datamodel.Status;
import dk.kb.webdanica.core.datamodel.dao.HarvestDAO;
import dk.kb.webdanica.core.datamodel.dao.SeedsDAO;
import dk.kb.webdanica.core.interfaces.harvesting.SingleSeedHarvest;
import dk.kb.webdanica.core.utils.Settings;
import dk.kb.webdanica.core.utils.SettingsUtilities;
import dk.kb.webdanica.webapp.Configuration;
import dk.kb.webdanica.webapp.Constants;
import dk.kb.webdanica.webapp.Environment;
import dk.netarkivet.common.utils.StringUtils;
import dk.netarkivet.harvester.datamodel.DBSpecifics;

public class HarvestWorkThread extends WorkThreadAbstract {

	static {
		logger = Logger.getLogger(HarvestWorkThread.class.getName());
	}

    private List<Seed> queueList = new LinkedList<Seed>();

    private List<Seed> workList = new LinkedList<Seed>();

	private SeedsDAO seeddao;
	
	private HarvestDAO harvestdao;

	private Configuration configuration;
	
	private boolean harvestingEnabled = false;
	
	private AtomicBoolean harvestingInProgress = new AtomicBoolean(false);
	
	private File harvestLogDir;

	private int maxHarvestsAtaTime;

	private String harvestPrefix;
	
	private String scheduleName;

	private String templateName;

	private int harvestMaxObjects;

	private long harvestMaxBytes;
	
	
    /**
     * Constructor for the Harvester thread worker object.
     * @param environment The Webdanica webapp environment object
     */
    public HarvestWorkThread(Environment environment, String threadName) {
        this.environment = environment;
        this.threadName = threadName;
    }
	
    public void enqueue(Seed urlRecord) {
        synchronized (queueList) {
            queueList.add(urlRecord);
        }
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
       	
       	//String harvestLogDirName = configuration.getHarvestLogDirName();
 
       
       	maxHarvestsAtaTime = SettingsUtilities.getIntegerSetting(WebdanicaSettings.HARVESTING_MAX_SINGLESEEDHARVESTS, Constants.DEFAULT_MAX_HARVESTS);
       	harvestLogDir = configuration.getHarvestLogDir();
       	
       	Set<String> requiredSettings = new HashSet<String>();
		requiredSettings.add(WebdanicaSettings.HARVESTING_SCHEDULE);
		requiredSettings.add(WebdanicaSettings.HARVESTING_TEMPLATE);
		requiredSettings.add(WebdanicaSettings.HARVESTING_MAX_OBJECTS);
		requiredSettings.add(WebdanicaSettings.HARVESTING_MAX_BYTES);
		requiredSettings.add(WebdanicaSettings.HARVESTING_PREFIX);
		
		if (!SettingsUtilities.verifyWebdanicaSettings(requiredSettings, false)) {
			String errMsg = "HarvestWorkFlow will not be enabled as some of the required harvesting settings are not defined. Please correct your webdanicasettings file. The required settings are:"
					+ StringUtils.conjoin(",", requiredSettings); 
        	logger.log(Level.WARNING, errMsg);
        	configuration.getEmailer().sendAdminEmail("[Webdanica-" + configuration.getEnv() + "] HarvestWorkFlow not enabled", errMsg);
			return;
		};
		
		scheduleName = Settings.get(WebdanicaSettings.HARVESTING_SCHEDULE);
		templateName = Settings.get(WebdanicaSettings.HARVESTING_TEMPLATE);
		harvestMaxObjects = Settings.getInt(WebdanicaSettings.HARVESTING_MAX_OBJECTS);
		harvestMaxBytes = Settings.getLong(WebdanicaSettings.HARVESTING_MAX_BYTES);
		harvestPrefix = Settings.get(WebdanicaSettings.HARVESTING_PREFIX);
		
		// Verify that database driver exists in classpath. If not exit program
		String dbdriver = DBSpecifics.getInstance().getDriverClassName();
		if (!SettingsUtilities.verifyClass(dbdriver, false)) {
			String errMsg = "HarvestWorkFlow will not be enabled as the necessary databasedriver to connect to Netarchivesuite '" + dbdriver + "' does not exist in the classpath";
        	logger.log(Level.WARNING, errMsg);
        	configuration.getEmailer().sendAdminEmail("[Webdanica-" + configuration.getEnv() + "] HarvestWorkFlow not enabled", errMsg);
			return;
		}
		// Verify that arcrepositoryclient exists in classpath. If not exit program
		String arcrepositoryClient = dk.netarkivet.common.utils.Settings.get("settings.common.arcrepositoryClient.class");
		if (!SettingsUtilities.verifyClass(arcrepositoryClient, false)) {
			String errMsg = "HarvestWorkFlow will not be enabled as the necessary acrepositoryClient '" + arcrepositoryClient + "' does not exist in the classpath";
        	logger.log(Level.WARNING, errMsg);
        	configuration.getEmailer().sendAdminEmail("[Webdanica-" + configuration.getEnv() + "] HarvestWorkFlow not enabled", errMsg);
			return;
		}
       
		if (maxHarvestsAtaTime < 1) {
			configuration.getEmailer().sendAdminEmail("[Webdanica-" + configuration.getEnv() + "] HarvestWorkFlow not enabled", "Maxharvests is less than 1: " +  maxHarvestsAtaTime);
			logger.info("MaxHarvests is less than 1. So HarvestWorkFlow is disabled!");
			return;
		}
		
       	if (existsLogdirAndIsWritable(harvestLogDir)) {
       		harvestingEnabled = true;
       		logger.info("All requirements fullfilled for harvesting. So harvestingEnabled is set to true");
       	} 

    }

	private boolean existsLogdirAndIsWritable(File harvestLogDir) {
		boolean deleteTestFile = true;
       	if (!harvestLogDir.isDirectory()) {
       		String errMsg = "HarvestWorkFlow will not be enabled as the given directory '" + harvestLogDir.getAbsolutePath() + "' does not exist or is not a proper directory";
        	logger.log(Level.WARNING, errMsg);
        	configuration.getEmailer().sendAdminEmail("[Webdanica-" + configuration.getEnv() + "] HarvestWorkFlow not enabled", errMsg);
       		return false;
       	}
       	logger.info("Trying to write a file to dir '" + harvestLogDir.getAbsolutePath() + "' with the correct permissions");
       	File testFile = new File(harvestLogDir, System.currentTimeMillis() + ".txt");
       	
       	try {
	        testFile.createNewFile(); 
	        // Try setting the just created file to writable by all 
	        boolean success = testFile.setWritable(true, false);
	        if (!success) {
	        	String errMsg = "HarvestWorkFlow will not be enabled as we're unable to set the correct permissions when writing a file (e.g rw_rw_rw) to dir '" + harvestLogDir.getAbsolutePath() + "'";
	        	logger.log(Level.WARNING, errMsg);
	        	configuration.getEmailer().sendAdminEmail("[Webdanica-" + configuration.getEnv() + "] HarvestWorkFlow not enabled", errMsg);
	        	return false;
	        } else {
				if (deleteTestFile) {
	        		if (!testFile.delete()) {
	        			logger.log(Level.WARNING, "Unable to delete testfile '" + testFile.getAbsolutePath() + "'");
	        		}
	        	}
	        	return true;
	        }
        } catch (IOException e) {
        	String errMsg = "IOException thrown during check that harvestLogDir '" + harvestLogDir.getAbsolutePath() + "' is writable:" + e;
        	configuration.getEmailer().sendAdminEmail("[Webdanica-" + configuration.getEnv() + "] HarvestWorkFlow not enabled", errMsg);
        	logger.log(Level.WARNING, errMsg);
        	
        	
        	return false;
        } catch (SecurityException e) {
        	String errMsg = "SecurityException thrown during check that harvestLogDir '" + harvestLogDir.getAbsolutePath() + "' is writable:" + e;
        	configuration.getEmailer().sendAdminEmail("[Webdanica-" + configuration.getEnv() + "] HarvestWorkFlow not enabled", errMsg);
        	logger.log(Level.WARNING, errMsg);
        	return false;
        }
    }

	@Override
    protected void process_run() {
		logger.log(Level.FINE, "Running process of thread '" +  threadName + "' at '" + new Date() + "'");
		if (!harvestingEnabled) {
			return;
		}
		if (harvestingInProgress.get()) {
			logger.log(Level.INFO, "Harvesting process already in progress at '" + new Date() + "'. Skipping");
			return;
		} else {
			harvestingInProgress.set(Boolean.TRUE);
		}
		List<Seed> seedsReadyForHarvesting = null;
		
		try {
		    seedsReadyForHarvesting = seeddao.getSeeds(Status.READY_FOR_HARVESTING, maxHarvestsAtaTime); 
		} catch (Throwable e) {
			String errMsg = "Exception thrown during method HarvestWorkThread.process_run:" + e;
		    logger.log(Level.WARNING, errMsg);
		    harvestingInProgress.set(Boolean.FALSE);
		    configuration.getEmailer().sendAdminEmail("[Webdanica-" + configuration.getEnv() + "] HarvestWorkFlow failed - unable to receive seeds with status '" 
		    		+ Status.READY_FOR_HARVESTING + "'", errMsg);
		    return;
		}
		
   		enqueue(seedsReadyForHarvesting);
   		if (seedsReadyForHarvesting.size() > 0) {
   			logger.log(Level.FINE, "Found '" + seedsReadyForHarvesting.size() + "' seeds ready for harvesting");
   		}
           try {
               synchronized (queueList) {
               	for (int i=0; i<queueList.size(); ++i) {
               		Seed urlRecord = queueList.get(i);
               		workList.add(urlRecord);
               	}
                queueList.clear();
               }
           	if (workList.size() > 0) {
                   logger.log(Level.INFO, "Starting harvest of " + workList.size() + " seeds");
                   lastWorkRun = System.currentTimeMillis();
                   startProgress(workList.size());
                   harvest(workList);  
                   stopProgress();
                   workList.clear();
           	}
           } catch (Throwable e) {
               logger.log(Level.SEVERE, e.toString(), e);
           } finally {
        	   harvestingInProgress.set(Boolean.FALSE);
           }
    }

	private void harvest(List<Seed> workList) {
		List<SingleSeedHarvest> harvests = new ArrayList<SingleSeedHarvest>();
		for (Seed s: workList) {
			boolean failure = false;
			boolean harvestSuccess = false;
			Throwable savedException = null;
			s.setStatus(Status.HARVESTING_IN_PROGRESS);
			String eventHarvestName = null;
			String failureReason = "";
			try {
	            seeddao.updateSeed(s);
	            eventHarvestName = harvestPrefix + System.currentTimeMillis();
	            // isolate in a separate thread, so we can stop the process, if it takes too long
	            SingleSeedHarvest h = new SingleSeedHarvest(s.getUrl(), eventHarvestName, scheduleName, templateName, harvestMaxBytes, harvestMaxObjects);
	            
	            harvestSuccess = h.finishHarvest(false);
	            // save harvest in harvest-database
	            boolean inserted = harvestdao.insertHarvest(h);
	            logger.info((harvestSuccess?"Successful":"Failed") + " harvest w/ name " +  h.getHarvestName() + " was " + (inserted? "successfully":"failed to be ") + " inserted into the database");
	            harvests.add(h);
	            if (!h.isSuccessful()) {
	            	failure = true;
	            	failureReason = h.getErrMsg();
	            }
            } catch (Exception e) {
            	logger.log(Level.SEVERE, e.toString(), e);
            	failure = true;
            	savedException = e;
            } finally {
            	if (failure) {
            		s.setStatus(Status.HARVESTING_FAILED);
            		if (savedException != null) {
            			s.setStatusReason("Harvesting of seed (harvestname='" + eventHarvestName + "' failed due to exception: " + savedException);
            		} else if (!failureReason.isEmpty()){
            			s.setStatusReason("Harvesting of seed (harvestname='" + eventHarvestName + "' failed. Reason: " + failureReason);
            		} else {
            			s.setStatusReason("Harvesting of seed (harvestname='" + eventHarvestName + "' failed -  reason is unknown");
            		}
            	} else {
            		s.setStatus(Status.READY_FOR_ANALYSIS);
            		s.setStatusReason("Harvesting finished successfully. harvestname is '" + eventHarvestName + "'. Now ready for analysis");
            	}
            	try {
	                seeddao.updateSeed(s);
                } catch (Exception e) {
                	String errMsg = "Unable to save state of seed: " + e.toString();
                	logger.log(Level.SEVERE, errMsg, e);
                	configuration.getEmailer().sendAdminEmail("[Webdanica-" + configuration.getEnv() + "] HarvestWorkFlow failed - unable to save state of seed w/url '" + s.getUrl() + "'", errMsg);
                }
            }
			
		}
		
		try {
			 writeHarvestLog(harvests, configuration);
        } catch (Throwable e) {
        	String errMsg = "Unable to write a harvestlog to directory '" + configuration.getHarvestLogDir() + "': " + e.toString();
        	logger.log(Level.SEVERE, errMsg, e);
        	configuration.getEmailer().sendAdminEmail("[Webdanica-" + configuration.getEnv() + "] HarvestWorkFlow failure - unable to write harvestlog to disk", errMsg);
        } 
    }
	
	@Override
    protected void process_cleanup() {
	    // TODO Auto-generated method stub
	    
    }

	@Override
    public int getQueueSize() {
	 	int queueSize = 0;
        synchronized (queueList) {
        	queueSize = queueList.size();
        }
        return queueSize;
    }
	
    public static void writeHarvestLog(List<SingleSeedHarvest> harvests, Configuration conf) throws Exception {
    	String systemTimestamp = SingleSeedHarvest.getTimestamp();
		String logNameInitial = conf.getHarvestLogPrefix() + systemTimestamp + conf.getHarvestLogNotReadySuffix();
		String logNameFinal = conf.getHarvestLogPrefix() + systemTimestamp + conf.getHarvestLogReadySuffix();
		File harvestLogDir = conf.getHarvestLogDir();
		File harvestLog = new File(harvestLogDir, logNameInitial);
		File harvestLogFinal = new File(harvestLogDir, logNameFinal);
		String harvestLogHeaderPrefix = "Harvestlog for ";
    	String harvestLogHeader = harvestLogHeaderPrefix 
    			+ " harvests initiated by the Webdanica webapp at " + new Date();
		
		// write harvestreport to disk where cronjob have privileges to read, and move the file to
		// a different location
    	
		int written = SingleSeedHarvest.writeHarvestLog(harvestLog, harvestLogHeader, true, harvests, false);
        if (written == 0) {
        	logger.log(Level.WARNING, "No harvests out of " + harvests.size() + " were successful, and no harvestlog is written");
        	// remove harvestlog
        	boolean deleted = harvestLog.delete();
        	if (!deleted) {
        		logger.log(Level.WARNING, "Unable to delete empty harvestlog '" + harvestLog.getAbsolutePath() + "'");
        	}
        	return;
        }
        boolean success = harvestLog.setWritable(true, false);
        if (!success) {
        	logger.log(Level.SEVERE, "Unable to give the harvestlog the correct permissions");
        }
        harvestLog.renameTo(harvestLogFinal);
        //Do we need to set the permissions again?
        logger.info("A harvestlog with " + written + "/" + harvests.size() + " results has now been written to file '" + harvestLogFinal.getAbsolutePath() + "'");
    }
	
	
	
	
	
	
}
