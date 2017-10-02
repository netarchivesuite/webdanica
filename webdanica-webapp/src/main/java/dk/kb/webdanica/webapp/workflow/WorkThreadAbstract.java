package dk.kb.webdanica.webapp.workflow;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.exception.ExceptionUtils;

import dk.kb.webdanica.webapp.Environment;

public abstract class WorkThreadAbstract implements Runnable {

    /** Logging mechanism. */
    protected static Logger logger;

    protected Environment environment;

    public String threadName;

    public Thread thread;

    public Throwable exitCausedBy;

    public boolean bRunning = true;

    public long lastRun;

    public long nextRun;

    public boolean bRun;

    public long lastWorkRun;

    /** Work thread exit switch. */
    public boolean bExit;

    public List<WorkProgress> progressHistory = new LinkedList<WorkProgress>();

    public WorkProgress progress;

    public void start() {
    	if (thread == null || !thread.isAlive()) {
    		exitCausedBy = null;
    		bRunning = true;
    		bRun = false;
    		bExit = false;
            new Thread(this, threadName).start();
            if (logger != null) {
            	logger.log(Level.INFO, this.getClass() + " Thread starting...");
            }
    	}
    }

    public void stop() {
    	if (bRunning && !bExit) {
        	bExit = true;
        	if (logger != null) {
            	logger.log(Level.INFO, this.getClass() + " Thread stopping...");
        	}
    	}
    }

    @Override
    public void run() {
    	thread = Thread.currentThread();
        try {
            logger.log(Level.INFO, this.getClass() + " Thread started.");
            bRun = true;
            process_init();
            bRun = false;
            nextRun = System.currentTimeMillis() + (60 * 1000);
            while (!bExit) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.log(Level.SEVERE, e.toString(), e);
                }
                if (System.currentTimeMillis() > nextRun && !bExit) {
                    bRun = true;
                }
                if (bRun) {
                	lastRun = System.currentTimeMillis();
                	process_run();
                    nextRun = System.currentTimeMillis() + (60 * 1000);
                    bRun = false;
                }
            }
            bRun = true;
            process_cleanup();
            bRun = false;
        } catch (Throwable t) {
            try {
            	exitCausedBy = t;
                logger.log(Level.SEVERE, t.toString(), t);
                // try to send mail to admin about the problem
                environment.getConfig().getEmailer().sendAdminEmail("Workflow '" 
                        + this.threadName + "' crashed", 
                        "Workflow '" + this.threadName + "' crashed w/exception: " 
                                + ExceptionUtils.getFullStackTrace(t));  

            } catch (Throwable t2) {
                // test if this catches some errors
                logger.log(Level.SEVERE, t2.toString(), t2);
                t2.printStackTrace();
            }
        }
        bRunning = false;
        logger.log(Level.INFO, this.getClass() + " Thread stopped.");
    }

    protected abstract void process_init();

    protected abstract void process_run();

    protected abstract void process_cleanup();

    public abstract int getQueueSize();

    public void startProgress(int items) {
		if (progress != null) {
			progress.stopped = System.currentTimeMillis();
			progress.bFailed = true;
		}
    	progress = new WorkProgress(thread.getName(), items);
    }

    public void stopProgress() {
    	if (progress != null) {
        	progress.stopped = System.currentTimeMillis();
        	if (progress.item < progress.items) {
        		progress.bFailed = true;
        	}
        	synchronized (progressHistory) {
            	progressHistory.add(progress);
        	}
        	progress = null;
    	}
    }

    public void trimProgresHistory(long cutoff) {
    	synchronized (progressHistory) {
    		int idx = 0;
    		while (idx < progressHistory.size()) {
    			if (progressHistory.get(idx).stopped >= cutoff) {
    				++idx;
    			} else {
    				progressHistory.remove(idx);
    			}
    		}
    	}
    }

}
