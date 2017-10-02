package dk.kb.webdanica.webapp.workflow;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.exception.ExceptionUtils;

import dk.kb.webdanica.core.interfaces.harvesting.SingleSeedHarvest;

/**
 * The HarvesterThread initiates a SingleSeedHarvest in Netarchivesuite and wait for it to finish.
 */
public class HarvesterThread implements Runnable {

	/** The log. */
	private static final Logger logger = Logger.getLogger(HarvestWorkThread.class.getName());
	private String url;
	private String eventHarvestName;
	private String scheduleName;
	private String templateName;
	private long harvestMaxBytes;
	private int harvestMaxObjects;
	private SingleSeedHarvest harvest;
	private boolean harvestSuccess = false;
	
	/**
	 * Constructor for the harvesterThread
	 * @param url The url we want to get harvested by NetarchiveSuite.
	 * @param eventHarvestName The name of the harvest in NetarchiveSuite.
	 * @param scheduleName The name of the schedule used by the harvest.
	 * @param templateName The name of the template used by the harvest.
	 * @param harvestMaxBytes The maximum number of bytes that we can harvest
	 * @param harvestMaxObjects The maximum number of objects that we can harvest
	 */
	public HarvesterThread(String url, String eventHarvestName, String scheduleName, String templateName, long harvestMaxBytes, int harvestMaxObjects) {
		this.url = url;
		this.eventHarvestName=eventHarvestName;
		this.scheduleName=scheduleName;
		this.templateName=templateName;
		this.harvestMaxBytes=harvestMaxBytes;
		this.harvestMaxObjects=harvestMaxObjects;
	}

	@Override
	public void run() {
		harvest = new SingleSeedHarvest(url, eventHarvestName, scheduleName, templateName, harvestMaxBytes, harvestMaxObjects);
		try {
			harvestSuccess = harvest.finishHarvest(false);
		} catch (Throwable e) {
			logger.log(Level.WARNING, "Exception during harvesting:" + ExceptionUtils.getFullStackTrace(e), e);
		}
	}
	
	/**
	 * 
	 * @return true, if the harvest is successful, otherwise false
	 */
	public boolean getHarvestSuccess() {
		return harvestSuccess;
	}
	
	/**
	 * 
	 * @return the SingleSeedHarvest created by the thread. This gives access to harvestdata
	 */
	public SingleSeedHarvest getHarvestResult() {
		return harvest;
	}
	
	/**
	 * 
	 * @deprecated "currently returns NPE when called"
	 * @return true, if the harvest was created successfully in netarchivesuite, false otherwise
	 */
	public boolean constructionOK() {
	    return harvest.getConstructionOK();
	}
}

	
