package dk.kb.webdanica.webapp.workflow;

import java.util.concurrent.Callable;

import dk.kb.webdanica.core.interfaces.harvesting.SingleSeedHarvest;

public class HarvesterThread implements Callable<SingleSeedHarvest> {

	    /** The log. */
	    //private static final Logger log = LoggerFactory.getLogger(HarvesterThread.class);

	    public HarvesterThread() {
	    }

	    /**
	     * This method does the actual indexing.
	     *
	     * @return true, if the indexing completes successfully; otherwise it returns false
	     */
	    @Override
	    public SingleSeedHarvest call() {
	        try {
	        	
	        } catch (Throwable t) {
	           
	        }
	        return null;
	    }

}

	
