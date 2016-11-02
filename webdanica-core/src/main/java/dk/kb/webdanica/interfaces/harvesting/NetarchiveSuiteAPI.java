package dk.kb.webdanica.interfaces.harvesting;

import java.io.File;

import dk.netarkivet.archive.arcrepositoryadmin.DBSpecifics;
import dk.netarkivet.common.distribute.JMSConnection;
import dk.netarkivet.common.distribute.arcrepository.ArcRepositoryClient;
import dk.netarkivet.common.distribute.arcrepository.ArcRepositoryClientFactory;
import dk.netarkivet.common.distribute.arcrepository.ViewerArcRepositoryClient;
import dk.netarkivet.harvester.datamodel.DomainDAO;
import dk.netarkivet.harvester.datamodel.HarvestDBConnection;
import dk.netarkivet.harvester.datamodel.JobDAO;

/**
 * Mechanics to switch between two different NetarchiveSuite APIs, each API defined by its own
 * settings.xml
 * When switching between the two systems, dao classes using the wrong settings must be closed and reset.
 * and the Settings 
 */
public class NetarchiveSuiteAPI {
	
	File settingsXMLInUse;
	DomainDAO currentDomainDAO;	
	JobDAO currentJobDAO;
	ViewerArcRepositoryClient currentViewerArcRepositoryClient;
	DBSpecifics currentDBSpecificsClass;  // used by HarvestDBConnection which doesn't need to be reset
	
	//TODO Cleanup of JMSConnection necessary to ensure that batch-jobs are nor sent to the wrong destination:
	// In PrepareHarvest, the Reporting.getFilesForJob() is used
	// This sends batchjobs using the ArcRepositoryClientFactory.getViewerInstance()
	// so ArcRepositoryClientFactory.getViewerInstance().close() must be called during switching to another destination
	
	public NetarchiveSuiteAPI() {		
	}
	
	public boolean initialized() {
		return settingsXMLInUse != null;
	}

	public File getCurrentSettingsFile() {
		return settingsXMLInUse;
	}
	
	public DomainDAO getCurrentDomainDAO() {
		return currentDomainDAO;
	}
	
	public JobDAO getCurrentJobDAO() {
		return currentJobDAO;
	}
	
	public ViewerArcRepositoryClient getCurrentViewerArcRepositoryClient() {
		return currentViewerArcRepositoryClient;
	}
	
	public synchronized void switchToSettings(File newSettingsXML) {
		if (initialized()) {
			reset();
		}
		
	}

	private void reset() {
	    if (currentJobDAO != null) {
	    	currentJobDAO.reset();
	    }
	    /*
	    if (currentDomainDAO != null) {
	    	currentDomainDAO).reset();
	    }
	    if (currentDBSpecificsClass != null) {
	    	currentDBSpecificsClass.reset();
	    }*/
	    HarvestDBConnection.cleanup();	    
    }
}
