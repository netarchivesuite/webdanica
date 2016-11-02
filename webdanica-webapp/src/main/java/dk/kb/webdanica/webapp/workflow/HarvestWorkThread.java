package dk.kb.webdanica.webapp.workflow;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import dk.kb.webdanica.datamodel.Seed;
import dk.kb.webdanica.datamodel.SeedDAO;
import dk.kb.webdanica.datamodel.Status;
import dk.kb.webdanica.webapp.Configuration;
import dk.kb.webdanica.webapp.Environment;

public class HarvestWorkThread extends WorkThreadAbstract {

	static {
		logger = Logger.getLogger(HarvestWorkThread.class.getName());
	}

    private List<Seed> queueList = new LinkedList<Seed>();

    private List<Seed> workList = new LinkedList<Seed>();

	private SeedDAO seeddao;

	private Configuration configuration;
	
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
       	seeddao = configuration.getSeedDAO();	    
    }

	@Override
    protected void process_run() {
		logger.log(Level.FINE, "Running process of thread '" +  threadName + "' at '" + new Date() + "'");
   		List<Seed> seedsReadyForHarvesting = seeddao.getSeeds(Status.READY_FOR_HARVESTING, 10); // Only take 10 at a time 
   		enqueue(seedsReadyForHarvesting);
   		if (seedsReadyForHarvesting.size() > 0) {
   			//logger.log(Level.INFO, "Found '" + seedsReadyForHarvesting.size() + "' seeds ready for harvesting");
   		}
           try {
               synchronized (queueList) {
               	for (int i=0; i<queueList.size(); ++i) { // FIXME Possibly have a limit to how much is processed at a time?
               		Seed urlRecord = queueList.get(i);
               		workList.add(urlRecord);
               		/*
               		if (!workList.contains(urlRecord)) {
               			workList.add(urlRecord);
               		}*/
               	}
                   queueList.clear();
               }
           	if (workList.size() > 0) {
                   //logger.log(Level.INFO, "Harvest queue: " + workList.size());
                   lastWorkRun = System.currentTimeMillis();
                   //harvest(workList); // implement this method 
                   startProgress(workList.size());
                   stopProgress();
                   workList.clear();
           	}
           } catch (Throwable e) {
               logger.log(Level.SEVERE, e.toString(), e);
           } finally {
              // TODO is there something we should do here
           }
    }

	private void harvest(List<Seed> workList) {
		for (Seed s: workList) {
			//s.setState(Status.HARVESTING_IN_PROGRESS);
			//do harvest
		}
		// write harvestreport to disk where cronjob have privileges to read, and move the file to
		// a different location
		
		// if harvest failed, set state to Status.HARVESTING_FAILED (new state) 
		// if harvest succeeded, set state to Status.HARVESTING_SUCCEEDED (new state)	
		// In both cases, the result is inserted to the HarvestDAO
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
