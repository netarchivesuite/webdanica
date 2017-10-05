package dk.kb.webdanica.core.interfaces.harvesting;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import dk.kb.webdanica.core.batch.WARCExtractUrlsJob;
import dk.kb.webdanica.core.datamodel.AnalysisStatus;
import dk.kb.webdanica.core.datamodel.criteria.SingleCriteriaResult;
import dk.kb.webdanica.core.exceptions.WebdanicaException;
import dk.kb.webdanica.core.interfaces.harvesting.SeedReport.SeedReportEntry;
import dk.netarkivet.common.CommonSettings;
import dk.netarkivet.common.distribute.arcrepository.ArcRepositoryClientFactory;
import dk.netarkivet.common.distribute.arcrepository.BitarchiveRecord;
import dk.netarkivet.common.distribute.arcrepository.Replica;
import dk.netarkivet.common.exceptions.ArgumentNotValid;
import dk.netarkivet.common.utils.ApplicationUtils;
import dk.netarkivet.common.utils.FileUtils;
import dk.netarkivet.common.utils.Settings;
import dk.kb.webdanica.core.utils.StreamUtils;
import dk.kb.webdanica.core.utils.SystemUtils;
import dk.netarkivet.common.utils.TimeUtils;
import dk.netarkivet.common.utils.batch.BatchLocalFiles;
import dk.netarkivet.common.utils.cdx.CDXRecord;
import dk.netarkivet.harvester.datamodel.DomainConfiguration;
import dk.netarkivet.harvester.datamodel.HarvestDefinition;
import dk.netarkivet.harvester.datamodel.HarvestDefinitionDAO;
import dk.netarkivet.harvester.datamodel.Job;
import dk.netarkivet.harvester.datamodel.JobDAO;
import dk.netarkivet.harvester.datamodel.JobStatus;
import dk.netarkivet.harvester.datamodel.PartialHarvest;
import dk.netarkivet.harvester.datamodel.Schedule;
import dk.netarkivet.harvester.datamodel.ScheduleDAO;
import dk.netarkivet.harvester.datamodel.TemplateDAO;
import dk.netarkivet.viewerproxy.webinterface.Reporting;

/**
 * Create event-harvest only containing one seed. 
 * using only_once schedule, using special template disabling non-text harvesting, and disabling javascript (webdanica_order),
 * and disabling deduplication.
 *
 * Requirement: JBDC access to netarchivesuite harvestdatabase 
 * Database url, and user/password information is taken from the 
 * a NetarchiveSuite settingsfile.
 * The java 'dk.netarkivet.settings.file' property needs to defined 
 * using -Ddk.netarkivet.settings.file=/full/path/to/netarchivesuite_settingsfile.xml
 */
public class SingleSeedHarvest {
	
	private static final Logger logger = Logger.getLogger(SingleSeedHarvest.class.getName());
	
	String seed; 
	String harvestName; 
	JobStatus finishedState;
	List<String> files;
	private NasJob statusInfo;
	private NasReports reports;
	StringBuilder errMsg;
	boolean successful;
	private Throwable exception;
	private long maxBytes;
	private int maxObjects;
	long harvestedTime;
	private Long hid;
	private List<SingleCriteriaResult> critResults;
	private List<String> fetchedUrls;
	private AnalysisStatus analysisState;
	private String analysisStateReason;
	private boolean constructionOK=true; 
	
	/**
	 * Constructor for SingleSeedHarvest.
	 * This constructor creates a new event-harvest (harvestdefinition) in the netarchiveSuite harvest database,
	 * and activates it, if the construction is successful. Otherwise it is deactived.
	 * If creation of event-harvest fails, constructionOk=false
	 * We assume here, that the given schedule and/or template exists already. If not, a WebdanicaException is thrown.
	 * We also assume that a harvestdefinition with the given eventHarvestName does not already exist. If it does, a WebdanicaException is thrown.
	 * 
	 * @param seed The single seed to harvest, 
	 * @param eventHarvestName The name of the event-harvest
	 * @param scheduleName The name of the schedule used in the harvest
	 * @param templateName The name of the template used in the harvest
	 * @param maxBytes The max number of bytes allowed to be harvested 
	 * @param maxObjects The max number of objects allowed to be harvested
	 * @throws WebdanicaException if the given schedule or template does not exist.
	 */
	public SingleSeedHarvest(String seed, String eventHarvestName, String scheduleName, String templateName, long maxBytes, int maxObjects) {
		this.seed = seed;
		this.harvestName = eventHarvestName;
		List<DomainConfiguration> noDcs = new ArrayList<DomainConfiguration>();
		this.maxBytes = maxBytes;
		this.maxObjects = maxObjects;
		this.errMsg = new StringBuilder();
		
		if (!ScheduleDAO.getInstance().exists(scheduleName)) {
			throw new WebdanicaException("The chosen schedule with name '" + scheduleName + "' does not exist");
		}
		if (!TemplateDAO.getInstance().exists(templateName)) {
			throw new WebdanicaException("The chosen heritrix template with name '" + templateName + "' does not exist");
		}
		
		Schedule s = ScheduleDAO.getInstance().read(scheduleName);
		HarvestDefinition hd = new PartialHarvest(noDcs, s, eventHarvestName, "Event harvest created by webdanica system at " + new Date() + ". seed= " + seed, "Webdanica Curators");
		HarvestDefinitionDAO dao = HarvestDefinitionDAO.getInstance();
		hid = dao.create(hd);
		
		Map<String,String> attributeValues = new HashMap<String,String>(); // Empty attributeset
		
		PartialHarvest eventHarvest = (PartialHarvest) HarvestDefinitionDAO.getInstance().getHarvestDefinition(eventHarvestName);
		Set<String> seedSet = new HashSet<String>();
		seedSet.add(seed);
		try {
		    eventHarvest.addSeeds(seedSet, templateName, maxBytes, maxObjects, attributeValues);
		} catch (ArgumentNotValid e) {
		    constructionOK=false;
		    String error = "Unable to add seed '" + seed + "' to eventharvest: " + ExceptionUtils.getFullStackTrace(e);
		    logger.warning("Failed to construct harvest for seed '" + seed + "': " +  error);
		    errMsg.append(error);
		    
		}
		if (constructionOK) {
		    eventHarvest.setActive(true);
		    logger.info("Harvest for seed '" + seed + "' constructed successfully");
		} else {
		    eventHarvest.setActive(false);
		}
		HarvestDefinitionDAO.getInstance().update(eventHarvest);		 
	}
	
	public static SingleSeedHarvest getErrorObject(String seed, String harvestName, String error, Throwable exception) {
		SingleSeedHarvest s = new SingleSeedHarvest(seed, harvestName, error, exception);
		return s;
	}
	
	/**
	 * Special SingleSeedHarvest constructor used by the getErrorObject() method.
	 * @param seed The seed to be harvested
	 * @param harvestName a given harvestname
	 * @param error The error met during harvesting of the given seed
	 * @param exception An exception caught during harvesting of the given seed  
	 */
	private SingleSeedHarvest(String seed, String harvestName, String error, Throwable exception) {
	    this.seed = seed;
	    this.errMsg = new StringBuilder(error);
	    this.exception = exception;
	    this.harvestName = harvestName;
	    this.analysisState = AnalysisStatus.NO_ANALYSIS;
	    this.analysisStateReason = "No analysis done, as the harvest has failed";
    }
	
	/**
     * Make a dummy SingleSeedHarvest object usable by the GUI when a proper object is not available
     * and not needed. 
     * @param error An errormessage to write
     * @return a dummy SingleSeedHarvest object
     */
    public static SingleSeedHarvest makeGuiErrorObject(String error) {
        SingleSeedHarvest h = new SingleSeedHarvest();
        h.harvestName = error;
        h.errMsg = new StringBuilder(error);
        h.files = new ArrayList<String>();
        return h;
    }
	
	/**
	 * Used to construct a SingleSeedHarvest object with database information.
	 * @param harvestname a given name of an event-harvest
	 * @param seedurl the seed of the harvest
	 * @param successful was the harvest successful (true or false)
	 * @param files The names of the files harvested by the harvest
	 * @param error Any errors occurred during the harvest
	 * @param finalState The finalState of the harvest (FAILED or DONE)
	 * @param harvestedTime The time when the harvest was finished in Milliseconds since epoch (Job.getActualStop().getTime())
	 * @param reports The reports fetched from the metadata file for the job
	 * @param fetchedUrls the list of urls fetched by the harvest.  
	 * @param analysisStatus The status of the analysis of the harvest
	 * @param analysisReason The statusreason of the analysis of the harvest
	 */
	public SingleSeedHarvest(String harvestname, String seedurl, boolean successful, List<String> files, String error, 
			JobStatus finalState, long harvestedTime, NasReports reports, List<String> fetchedUrls, AnalysisStatus analysisStatus, String analysisReason) {
		this.harvestName = harvestname;
		this.seed = seedurl;
		this.successful = successful;
		if (files != null) {
		    this.files = files;
		} else {
		    this.files = new ArrayList<String>();
		}
		if (error == null) {
			this.errMsg = new StringBuilder();
		} else {
			this.errMsg = new StringBuilder(error);
		}
		this.finishedState = finalState;
		this.harvestedTime = harvestedTime;
		this.reports = reports;
		this.fetchedUrls = fetchedUrls;
		this.analysisState = analysisStatus;
		this.analysisStateReason = analysisReason;
    }
    
	/**
     * Empty constructor used the HarvestLog to construct a SingleSeedHarvest from the 
     * HarvestLog class produced by the harvestWorkThread.
     */
    public SingleSeedHarvest() {
    }
    
	/**
	 * This method waits until this harvest is finished or failed.
	 * If successful, retrieves the reports from the metadata-file, in particular the seedreport, which is used to check, if the seed was actually harvested or not.
	 * Also retrieves a list of fetched urls.
	 *  
	 * @return true, if successful otherwise false;
	 * @throws Exception 
	 */
	public boolean finishHarvest(boolean writeToSystemOut) throws Exception {
		Set<JobStatus> finishedStates = new HashSet<JobStatus>();
		finishedStates.add(JobStatus.DONE);
		finishedStates.add(JobStatus.FAILED);
		
		String logMsg = "Now waiting for job for eventharvest '" + harvestName + "' to be scheduled ..";
		SystemUtils.log(logMsg,Level.INFO, writeToSystemOut);
		NasJob jsi = NetarchiveSuiteTools.getNewHarvestStatus(hid);
		while (jsi == null){
			try {
				Thread.sleep(5000L);
				jsi = NetarchiveSuiteTools.getNewHarvestStatus(hid);
				if (jsi == null) {
				    logMsg = "Still waiting for job for eventharvest '" + harvestName 
                        + "' to be scheduled at date: " + new Date();
				    SystemUtils.log(logMsg,Level.FINE, writeToSystemOut);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		logMsg = "Job for eventharvest '" + harvestName + "' has now been scheduled as job " +  jsi.getJobId() + " at date: " + new Date();
		SystemUtils.log(logMsg,Level.INFO, writeToSystemOut);
        
		// Harvest is now in progress ph.getHarvestStatus() != null
		jsi = NetarchiveSuiteTools.getNewHarvestStatus(hid);
		Long jobId = jsi.getJobId();
		JobStatus status = jsi.getStatus();
		logMsg = "State of Job '" + jobId + "' is now " + status + ". Waiting for job (harvest '" 
		+ harvestName + "') to finish at date: " + new Date();
		SystemUtils.log(logMsg, Level.INFO, writeToSystemOut);
	
		long starttime = System.currentTimeMillis();
		while (!finishedStates.contains(status)) {
		    if (writeToSystemOut) 
                System.out.println("Waiting for job w/id=" + jobId + "(harvest '" + harvestName + "') to finish. Current state is " + status + " at date: " + new Date());
			try {
				Thread.sleep(30000L); // 30 secs sleep
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			jsi = NetarchiveSuiteTools.getNewHarvestStatus(hid);
			status = jsi.getStatus(); // Refresh status
		}
		long endtime = System.currentTimeMillis();
		long usedtimeSecs = (endtime-starttime)/1000;
		logMsg = "After " + TimeUtils.readableTimeInterval(usedtimeSecs*1000L) +" the job " + jobId + "(harvest '" + harvestName + "') now has finished state " + status + " at date: " + new Date();
		SystemUtils.log(logMsg, Level.INFO, writeToSystemOut);
		
		this.finishedState = status;
		this.statusInfo = jsi;
		Job theJob = JobDAO.getInstance().read(jobId);
		// jobEndState should be equal to this.finishedState
		JobStatus jobEndState = theJob.getStatus();
		if (!jobEndState.equals(finishedState)) {
			System.err.println("JobEndState (" + jobEndState + ") is not equal to finishedstate (" +  finishedState + ")!");
		}
		
		String harvestPrefix = theJob.getHarvestFilenamePrefix();
		ApplicationUtils.dirMustExist(FileUtils.getTempDir()); // Inserted to ensure that the getTempDir() exists.
		if (writeToSystemOut)System.out.println("Retrieving the list of files belonging to job w/id=" + jobId);
		List<String> lines = Reporting.getFilesForJob(jobId.intValue(), harvestPrefix);
		boolean filesHarvested = false;
		if (lines != null && !lines.isEmpty()){
			logMsg = "The following files were harvested for job w/id=" + jobId + ": " + StringUtils.join(lines, ",");
			filesHarvested = true;
			SystemUtils.log(logMsg, Level.INFO, writeToSystemOut);
		} else {
			logMsg = "No files was harvested for job w/id=" + jobId;
			SystemUtils.log(logMsg, Level.WARNING, writeToSystemOut);
		}
		this.files = lines;
		
		// attempt to set harvestdefinition represented by this.hid to inactive
		try {
		    HarvestDefinitionDAO hdao = HarvestDefinitionDAO.getInstance();
		        if (hdao.exists(this.hid)) {
		            HarvestDefinition hd = hdao.read(this.hid);
		            hd.setActive(false);
		            hdao.update(hd);
		        } else {  
		            logMsg = "Unable to disable harvestdefinition w/id=" + this.hid + ". Netarchivesuite does not recognize harvestdefinition with this id";
		            SystemUtils.log(logMsg, Level.WARNING, writeToSystemOut);
		        }
		} catch (Throwable e) {
		    logMsg = "Unable to disable harvestdefinition w/id=" + this.hid;
		    SystemUtils.log(logMsg, Level.WARNING, writeToSystemOut, e);
		}
		
		//get the reports associated with the harvest as well, extracted from the metadatawarc.file.
		this.reports = null;
		try {
			this.reports = getReports(theJob.getJobID(), writeToSystemOut);
		} catch (Throwable e) {
		    String error = "Unable to retrieve the reports for job w/id=" + theJob.getJobID() + ": " + e;
		    this.errMsg.append(error);
		    SystemUtils.log(error, Level.WARNING, writeToSystemOut, e);
		}
		if (this.reports != null) {
			logMsg = "Retrieved '" + this.reports.getReports().keySet().size() + "' reports for job w/id=" + jobId;
			SystemUtils.log(logMsg, Level.INFO, writeToSystemOut);
		} 
		
		// get the urls harvested by the job
		this.fetchedUrls = null;
		try {
			this.fetchedUrls = getHarvestedUrls(getHeritrixWarcs(), theJob.getJobID(), writeToSystemOut);
		} catch (Throwable e) {
		    String error = "Unable to retrieve the fetched urls for job w/id=" + theJob.getJobID();
		    this.errMsg.append(error);
		    SystemUtils.log(error, Level.WARNING, writeToSystemOut, e);
		}
		this.successful = status.equals(JobStatus.DONE);
		String failureReason = "";
		if (reports != null && reports.getSeedReport() != null) {
			// Look at the seedreport
			SeedReport sr = reports.getSeedReport();
			SeedReportEntry entry = sr.getEntry(this.seed);
			if (entry == null && !this.seed.endsWith("/")) { // try adding '/' to the end
				entry = sr.getEntry(this.seed + "/");
			}
			if (entry == null) {
				failureReason = " The seed was not found in the seedreport. Only exist entries for: " + StringUtils.join(sr.getSeeds(), ",");
				this.successful = false;
			} else if (!entry.isCrawled()) {
				failureReason = " According to the seedreport, the seed was not crawled. Code = '" +  entry.getCode() + "', Status='" +  entry.getStatus() + "'";
				this.successful = false;
			}	
		}
		
		// Add failureReason to errMsg, if it contains information
		if (!failureReason.isEmpty()) {
			errMsg.append(failureReason);
		}

		// examine the harvested-Heritrix-files, and see if they are warc.gz
		if (!filesHarvested || getHeritrixWarcs().size()==0 ) {
			this.successful = false;
			failureReason += "No heritrix warc files generated by the harvest";
		} else {
			for (String hwarc: getHeritrixWarcs()) {
				if (!SystemUtils.isGzippedWarcfile(hwarc)) {
					this.successful = false;
					failureReason += "The warc-file '" + hwarc + "' is not gzipped. Make sure that your NetarchiveSuite is configured to produce gzipped warc-files";
				}
			}
		}
		
		this.harvestedTime = theJob.getActualStop().getTime();
		if (this.successful) {
			this.analysisState = AnalysisStatus.AWAITING_ANALYSIS;
			this.analysisStateReason = "Harvest was successful, so analysis can be done.";
		} else {
			this.analysisState = AnalysisStatus.NO_ANALYSIS;
			this.analysisStateReason = "Harvest was not successful, so analysis can't be done. " + failureReason;
		}
		return this.successful;
    }
	
	
	/**
	 * Fetch all the harvest reports for the given job.
	 * FIXME currently, we have difficulty fetching the Heritrix3 template. 
	 * @param jobID the id of a given job
	 * @param writeToSystemOut write System.out/System.err (true/false)
	 * @return a NasReports object with all the reports we were able to fetch.
	 */
    public static NasReports getReports(Long jobID, boolean writeToSystemOut) {
		Map<String, String> reportMap = new HashMap<String,String>();
		List<CDXRecord> records = Reporting.getMetadataCDXRecordsForJob(jobID);
	    for (CDXRecord record : records) {
	    	String key = record.getURL();
	    	try {
	    		BitarchiveRecord baRecord = ArcRepositoryClientFactory.getViewerInstance().get(record.getArcfile(), 
	    				record.getOffset());
	    		String data = StreamUtils.getInputStreamAsString(baRecord.getData());
	    		reportMap.put(key, data);
	    	} catch (Throwable e) {
	    		String logMsg = "When trying to get all reports for job w/id=" + jobID + " we failed to extract the report '" + key + "': " + e;
	    		SystemUtils.log(logMsg, Level.WARNING, writeToSystemOut, e);
	    	}
	    }
	    return new NasReports(reportMap);
    }
	
    /**
     * Extract the harvested urls from the warcfiles produced by Heritrix3. 
     * @param warcfiles the warcFiles produced by a given harvest
     * @param jobId The id of the NetarchiveSuite job which produced the warcfiles.
     * @param writeToSystemOut write System.out/System.err (true/false)
     * @return a list of harvested urls (possibly empty, if not warcfiles was produced).
     * @throws Exception
     */
	public static List<String> getHarvestedUrls(List<String> warcfiles, Long jobId, boolean writeToSystemOut) throws Exception {
		List<String> urls = new ArrayList<String>();
		if (warcfiles.isEmpty()) {
			String logMsg = "No heritrixWarcs seems to have been harvested for job w/id=" + jobId + ". Something must have gone wrong. Returning an empty list";
			SystemUtils.log(logMsg, Level.WARNING, writeToSystemOut);
		} else {
			String logMsg = "Fetching the harvested urls from the " + warcfiles.size() + " HeritrixWarcs harvested for job w/id=" + jobId + ": " + StringUtils.join(warcfiles, ",");
			SystemUtils.log(logMsg, Level.INFO, writeToSystemOut);
		}
		for (String warcfilename: warcfiles){
			urls.addAll(getUrlsFromFile(warcfilename));
		}
		String logMsg = "Retrieved " + urls.size() + " urls";
		SystemUtils.log(logMsg, Level.INFO, writeToSystemOut);
		return urls;
	}
	
	/**
	 * Get the urls from a file with the given filename in our archive. 
	 * @param filename a file with the given filename
	 * @return a set of urls contained in the given file.
	 * @throws Exception 
	 */
	static Set<String> getUrlsFromFile(String filename) throws Exception{
		File toFile = new File(FileUtils.getTempDir(), filename);
		Set<String> urls = new TreeSet<String>();
		toFile.createNewFile();
		if (!toFile.isFile()) {
			throw new IOException("Unable not create temporary file '" + toFile.getAbsolutePath() + "'");
		}
		String replicaId = Settings.get(CommonSettings.USE_REPLICA_ID);
		ArcRepositoryClientFactory.getViewerInstance().getFile(filename,Replica.getReplicaFromId(replicaId), toFile);
		
        ByteArrayOutputStream cdxBaos = new ByteArrayOutputStream();
        BatchLocalFiles batchRunner = new BatchLocalFiles(new File[] {toFile});
        batchRunner.run(new WARCExtractUrlsJob(), cdxBaos);
        for (String cdxLine : cdxBaos.toString().split("\n")) {
            urls.add(cdxLine);
        }
        return urls;
    }
	
	/**
	 * Do a single harvest.
	 * @param seed the seed of the single harvest.
	 * @param eventHarvestName The name of the event-harvest.
	 * @param scheduleName The name of the schedule used
	 * @param templateName The name of the template used
	 * @param maxBytes The max number of bytes allowed to be harvested 
	 * @param maxObjects The max number of objects allowed to be harvested 
	 * @param writeToStdout write log to System.out/System.err (true/false)
	 * @return a SingleSeedHarvest object
	 * @throws Exception
	 */
	public static SingleSeedHarvest doSingleHarvest(String seed, String eventHarvestName, String scheduleName,
            String templateName, long maxBytes, int maxObjects, boolean writeToStdout) throws Exception {
	    SingleSeedHarvest ssh = new SingleSeedHarvest(seed, eventHarvestName, scheduleName, templateName, maxBytes, maxObjects);
	    boolean success = ssh.finishHarvest(writeToStdout);
	    if (success) {
	        SystemUtils.log("The Harvest of seed '" + seed + "' was successful", Level.INFO, writeToStdout);
	    } else {
	        SystemUtils.log("The Harvest of seed '" + seed + "' failed", Level.WARNING, writeToStdout);
	    }
	    return ssh;
	    
    }

	/**
	 * Do a series of SingleSeedHarvests.
	 * @param seedsFile A file containing seeds to be harvested.
	 * @param scheduleName The name of the schedule used
	 * @param templateName The name of the template used
	 * @param harvestPrefix The prefix to be used when creating the names of the eventharvests.
	 * @param harvestMaxBytes The max number of bytes allowed to be harvested
	 * @param harvestMaxObjects The max number of objects allowed to be harvested
	 * @param writeToStdout write log to System.out/System.err (true/false)
	 * @return a list of SingleSeedHarvest objects
	 */
	public static List<SingleSeedHarvest> doSeriesOfharvests(File seedsFile,
	        String scheduleName, String templateName, String harvestPrefix, long harvestMaxBytes, int harvestMaxObjects, boolean writeToStdout) {

	    BufferedReader fr = null;
	    List<SingleSeedHarvest> results = new ArrayList<SingleSeedHarvest>();
	    try {
	        fr = new BufferedReader(new FileReader(seedsFile));
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	        // Should not happen: already tested
	    }        

	    //read file 
	    Set<String> seeds = new HashSet<String>();
	    try {
	        String line = "";
	        while ((line = fr.readLine()) != null) {
	            String seed = line.trim();
	            if (!seed.isEmpty()) {
	                seeds.add(seed);
	            }
	        }
	    } catch (IOException e) {
	        throw new WebdanicaException("Exception during the reading of the file '" 
	                + seedsFile.getAbsolutePath() + "'", e);
	    } finally {
	        IOUtils.closeQuietly(fr);
	    }

	    //harvest each seed in the file
	    for (String seed: seeds) {
	        String eventHarvestName = harvestPrefix + SingleSeedHarvest.getTimestamp();
	        try {
	            SingleSeedHarvest ssh = doSingleHarvest(seed, eventHarvestName, scheduleName, templateName, harvestMaxBytes, harvestMaxObjects, writeToStdout);
	            results.add(ssh);
	        } catch (Throwable e) {	        
	            SingleSeedHarvest s = SingleSeedHarvest.getErrorObject(seed, eventHarvestName, "Harvest Failed", e);
	            results.add(s);
	        }
	    }
	    return results;
	}

		
	/////////////////////////////////////////////////////////////////////////////////////
	// Setters and getters for SingleSeedHarvestClass
	/////////////////////////////////////////////////////////////////////////////////////

    public String getSeed() {
        return this.seed;
    }
    
    public String getHarvestName() {
        return this.harvestName;
    }
    
    public JobStatus getFinalState() {
        return this.finishedState;
    }
    
    public NasJob getJobStatusInfo() {
        return this.statusInfo;
    }

    public NasReports getReports() {
        return reports;
    }
    
    public AnalysisStatus getAnalysisState() {
        return this.analysisState;
    }
    
    public String getAnalysisStateReason() {
        return this.analysisStateReason;
    }
    
    public List<String> getFiles() {
        return this.files;
    }
    
    public boolean isSuccessful() {
        return this.successful;
    }
    
    public Throwable getException() {
        return this.exception;
    }
    
    public int getMaxObjects() {
        return this.maxObjects;
    }
    
    public long getMaxBytes() {
        return this.maxBytes;
    }
    
    public String getErrMsg() {
        return this.errMsg.toString();
    }

    public long getHarvestedTime() {
        return this.harvestedTime;
    }
    
    public void setCriteriaResults(List<SingleCriteriaResult> results) {
        this.critResults = results;
    }

    public List<SingleCriteriaResult> getCritresults() {
        return this.critResults;
    }

    public boolean hasErrors() {
        if (this.errMsg.toString().isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    public List<String> getHeritrixWarcs() {
        List<String> allFiles = new ArrayList<String>(); 
        for (String f: this.files) {
            if (!f.contains("metadata")) {
                allFiles.add(f);
            }
        }
        return allFiles;
    }
    
    public Set<String> getMetadataWarcs() {
        Set<String> allFiles = new HashSet<String>(); 
        for (String f: this.files) {
            if (f.contains("metadata")) {
                allFiles.add(f);
            }
        }
        return allFiles;
    }

    public void setErrMsg(String error) {
        this.errMsg = new StringBuilder(error);
        
    }

    public List<String> getFetchedUrls() {
        return this.fetchedUrls;
    }

    public boolean getConstructionOK() {
        return this.constructionOK;
    }
    
    public static String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy-");
        return sdf.format(new Date()) + System.currentTimeMillis();
    }

}
