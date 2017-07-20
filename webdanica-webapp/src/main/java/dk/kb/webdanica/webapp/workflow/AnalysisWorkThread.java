package dk.kb.webdanica.webapp.workflow;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import dk.kb.webdanica.core.datamodel.Seed;
import dk.kb.webdanica.core.datamodel.Status;
import dk.kb.webdanica.core.datamodel.dao.CriteriaResultsDAO;
import dk.kb.webdanica.core.datamodel.dao.HarvestDAO;
import dk.kb.webdanica.core.datamodel.dao.SeedsDAO;
import dk.kb.webdanica.core.interfaces.harvesting.SingleSeedHarvest;
import dk.kb.webdanica.webapp.Configuration;
import dk.kb.webdanica.webapp.Environment;

/**
 * This class (AnalysisWorkThread) is currently under development.
 * Note: The analysis is currently done outside the webapp by cronjobs working on the harvestlogs produced by the harvestingworkflow.
 *
 */
public class AnalysisWorkThread extends WorkThreadAbstract {

	static {
		logger = Logger.getLogger(AnalysisWorkThread.class.getName());
	}

    private List<Seed> queueList = new LinkedList<Seed>();

    private List<Seed> workList = new LinkedList<Seed>();

	private SeedsDAO seeddao;
	
	private HarvestDAO harvestdao;

	private Configuration configuration;
	
	private CriteriaResultsDAO cdao;

	private AtomicBoolean analysisInProgress;
	
	
    /**
     * Constructor for the AnalysisWorkThread worker object.
     * @param environment The Webdanica webapp environment object
     */
    public AnalysisWorkThread(Environment environment, String threadName) {
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
       	cdao = configuration.getDAOFactory().getCriteriaResultsDAO(); 

    }

	@Override
    protected void process_run() {
		logger.log(Level.FINE, "Running process of thread '" +  threadName + "' at '" + new Date() + "'");
		if (analysisInProgress.get()) {
			logger.log(Level.INFO, "Analysis process already in progress at '" + new Date() + "'. Skipping");
			return;
		} else {
			analysisInProgress.set(Boolean.TRUE);
		}
		List<Seed> seedsReadyForAnalysis = null;
		
		try {
		    seedsReadyForAnalysis = seeddao.getSeeds(Status.ANALYSIS_COMPLETED, Integer.MAX_VALUE); 
		} catch (Throwable e) {
			String errMsg = "Exception thrown during method AnalysisWorkThread.process_run:" + e;
		    logger.log(Level.WARNING, errMsg);
		    analysisInProgress.set(Boolean.FALSE);
		    configuration.getEmailer().sendAdminEmail("[Webdanica-" + configuration.getEnv() + "] AnalysisWorkFlow failed - unable to receive seeds with status '" 
		    		+ Status.ANALYSIS_COMPLETED + "'", errMsg);
		    return;
		}
		
   		enqueue(seedsReadyForAnalysis);
   		if (seedsReadyForAnalysis.size() > 0) {
   			logger.log(Level.FINE, "Found '" + seedsReadyForAnalysis.size() + "' seeds ready for analysis");
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
                   logger.log(Level.INFO, "Starting analysis of " + workList.size() + " seeds");
                   lastWorkRun = System.currentTimeMillis();
                   startProgress(workList.size());
                   analysis(workList);  
                   stopProgress();
                   workList.clear();
           	}
           } catch (Throwable e) {
               logger.log(Level.SEVERE, e.toString(), e);
           } finally {
        	   analysisInProgress.set(Boolean.FALSE);
           }
    }

	private void analysis(List<Seed> workList) {
		List<SingleSeedHarvest> harvests = new ArrayList<SingleSeedHarvest>();
		for (Seed s: workList) {
			boolean failure = false;
			boolean harvestSuccess = false;
			Throwable savedException = null;
			// next step: done  || rejected
/*			
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
            } catch (Exception e) {
            	logger.log(Level.SEVERE, e.toString(), e);
            	failure = true;
            	savedException = e;
            } finally {
            	if (failure) {
            		s.setStatus(Status.HARVESTING_FAILED);
            		if (savedException != null) {
            			s.setStatusReason("Harvesting of seed (harvestname='" + eventHarvestName + "' failed due to exception: " + savedException);
            		} else {
            			s.setStatusReason("Harvesting of seed (harvestname='" + eventHarvestName + "' failed -  reason is unknown");
            		}
            	} else {
            		s.setStatus(Status.HARVESTING_FINISHED);
            		s.setStatusReason("Harvesting finished successfully. harvestname is " + eventHarvestName);
            	}
            	try {
	                seeddao.updateSeed(s);
                } catch (Exception e) {
                	String errMsg = "Unable to save state of seed: " + e.toString();
                	logger.log(Level.SEVERE, errMsg, e);
                	configuration.getEmailer().sendAdminEmail("[Webdanica-" + configuration.getEnv() + "] HarvestWorkFlow failed - unable to save state of seed w/url '" + s.getUrl() + "'", errMsg);
                }
            }
		*/	
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

}
