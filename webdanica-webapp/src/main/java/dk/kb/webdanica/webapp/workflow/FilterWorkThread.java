package dk.kb.webdanica.webapp.workflow;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import dk.kb.webdanica.datamodel.Seed;
import dk.kb.webdanica.datamodel.SeedDAO;
import dk.kb.webdanica.datamodel.Status;
import dk.kb.webdanica.seeds.filtering.IgnoredSuffixes;
import dk.kb.webdanica.webapp.Environment;

/**
 * Seeds filter work-thread.
 * A worker thread to reject seeds matching ignored suffixes or matching regexps in our active blacklists or 
 * matching blacklisted/rejected domains in our domain table.
 */
public class FilterWorkThread extends WorkThreadAbstract {

	static {
		logger = Logger.getLogger(FilterWorkThread.class.getName());
	}

    private List<Seed> queueList = new LinkedList<Seed>();

    private List<Seed> workList = new LinkedList<Seed>();

	private SeedDAO dao;

    /**
     * Constructor for the NAS thread worker object.
     * @param environment DAB environment object
     */
    public FilterWorkThread(Environment environment, String threadName) {
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
    public int getQueueSize() {
    	int queueSize = 0;
        synchronized (queueList) {
        	queueSize = queueList.size();
        }
        return queueSize;
    }

    @Override
	protected void process_init() {
    	dao = environment.seedDao;
    	
	}

	@Override
	protected void process_run() {
		logger.log(Level.FINE, "Running process of thread '" +  threadName + "' at '" + new Date() + "'");
		List<Seed> seedsNeedFiltering = dao.getSeeds(Status.NEW);
		enqueue(seedsNeedFiltering);
		if (seedsNeedFiltering.size() > 0) {
			logger.log(Level.INFO, "Found '" + seedsNeedFiltering.size() + "' seeds ready for filtering");
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
                logger.log(Level.INFO, "Filter queue: " + workList.size());
                lastWorkRun = System.currentTimeMillis();
                filter(dao, workList);
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

	private void filter(SeedDAO dao, List<Seed> workList) {
	    for (Seed s: workList) {
	    	String ignoredSuffix = IgnoredSuffixes.matchesIgnoredExtension(s.getUrl());
	    	if (ignoredSuffix != null) {
	    		s.setState(Status.REJECTED);
	    		s.setStatusReason("REJECTED becausen it matches ignored suffix '" + ignoredSuffix + "'");
	    	} else {
	    		s.setState(Status.READY_FOR_HARVESTING);
	    		s.setStatusReason("");
	    	}
	    	dao.updateState(s);
	    }
	    
    }

	@Override
	protected void process_cleanup() {
	}

}
