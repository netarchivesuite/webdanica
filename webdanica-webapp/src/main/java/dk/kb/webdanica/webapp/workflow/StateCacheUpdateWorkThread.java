package dk.kb.webdanica.webapp.workflow;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import dk.kb.webdanica.core.datamodel.dao.DAOFactory;
import dk.kb.webdanica.core.datamodel.Cache;
import dk.kb.webdanica.webapp.Environment;


public class StateCacheUpdateWorkThread extends WorkThreadAbstract {
	
	static {
        logger = Logger.getLogger(StateCacheUpdateWorkThread.class.getName());
    }

	private DAOFactory daofactory;
	private AtomicBoolean updateInProgress = new AtomicBoolean(false);

	public StateCacheUpdateWorkThread(Environment environment, String threadName) {
		this.environment = environment;
		this.threadName = threadName;
		this.daofactory = environment.getConfig().getDAOFactory();
	}

	@Override
	protected void process_init() {	
	}

	@Override
	protected void process_run() {
		// check if not needs to run now
		if (!environment.bScheduleCacheUpdating) {
			//logger.log(Level.INFO, "Skipping cache update process - environment.bScheduleCacheUpdating is false");
			return;
		}
		if (updateInProgress.get()) {
			 logger.log(Level.INFO,
	                    "State cache update process already in progress at '" + new Date()
	                            + "'. Skipping");
			return;
        } else {
        	updateInProgress.set(Boolean.TRUE);
        	try {
        		Cache.updateCache(daofactory);
        	} catch (Throwable e) {
        		logger.log(Level.WARNING, "Failure during updating of cache", e);
        	} finally {
        		updateInProgress.set(Boolean.FALSE);
        	}
        }
		
	}

	@Override
	protected void process_cleanup() {
		
	}

	@Override
	public int getQueueSize() {
		return 0;
	}

}
