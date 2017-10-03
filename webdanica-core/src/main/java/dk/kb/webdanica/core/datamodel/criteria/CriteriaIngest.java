package dk.kb.webdanica.core.datamodel.criteria;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import dk.kb.webdanica.core.datamodel.Domain;
import dk.kb.webdanica.core.datamodel.Seed;
import dk.kb.webdanica.core.datamodel.Status;
import dk.kb.webdanica.core.datamodel.dao.CriteriaResultsDAO;
import dk.kb.webdanica.core.datamodel.dao.DAOFactory;
import dk.kb.webdanica.core.datamodel.dao.DomainsDAO;
import dk.kb.webdanica.core.datamodel.dao.HarvestDAO;
import dk.kb.webdanica.core.datamodel.dao.SeedsDAO;
import dk.kb.webdanica.core.interfaces.harvesting.HarvestError;
import dk.kb.webdanica.core.interfaces.harvesting.HarvestLog;
import dk.kb.webdanica.core.interfaces.harvesting.SingleSeedHarvest;
import dk.kb.webdanica.core.seeds.filtering.FilterUtils;
import dk.kb.webdanica.core.utils.StreamUtils;
import dk.kb.webdanica.core.utils.SystemUtils;
import dk.kb.webdanica.core.utils.TextUtils;


/**
 * Ingest a file and calculate the probability of being Danish (the DanishCode).
 * @author svc
 */
public class CriteriaIngest {

    /**
     * Reads 
     * @param harvestLog
     * @param baseCriteriaDir
     * @param addHarvestToDatabase
     * @param addCriteriaResultsToDatabase
     * @param daofactory
     * @throws Exception
     */
    public static void ingest(File harvestLog, File baseCriteriaDir,
            boolean addHarvestToDatabase, boolean addCriteriaResultsToDatabase,
            DAOFactory daofactory) throws Exception {
        File basedir = harvestLog.getParentFile();
        String harvestLogReportName = harvestLog.getName() + ".report.txt";
        File harvestLogReport = findReportFile(basedir, harvestLogReportName);
        List<SingleSeedHarvest> harvests = HarvestLog.readHarvestLog(harvestLog);
        if (addHarvestToDatabase) {
            HarvestDAO hdao = daofactory.getHarvestDAO();
            for (SingleSeedHarvest hp : harvests) {
                if (!hdao.exists(hp.getHarvestName())) {
                    hdao.insertHarvest(hp);
                } else {
                    System.out
                            .println("Skip ingest of harvest '"
                                    + hp.getHarvestName()
                                    + "' -  a harvest with this name already exists in database");
                }
            }
        }
        List<HarvestError> errors = HarvestLog.processCriteriaResults(harvests,
                baseCriteriaDir, addCriteriaResultsToDatabase, daofactory);
        if (addCriteriaResultsToDatabase) {
            SeedsDAO sdao = daofactory.getSeedsDAO();
            for (HarvestError e : errors) {
                String seed = e.getHarvest().getSeed();
                String harvestname = e.getHarvest().getHarvestName();
                String error = e.getError();
                Seed s = sdao.getSeed(seed);
                s.setStatus(Status.ANALYSIS_FAILURE);
                s.setStatusReason("Set to status " + Status.ANALYSIS_FAILURE
                        + " using harvestname '" + harvestname
                        + "'. Failures occured during processing: " + error);
                sdao.updateSeed(s);
            }
        } else { // just mention the errors in the screen
            System.out.println("Identified " + errors.size() + " errors");
            for (HarvestError e : errors) {
                System.out.println("Harvest of seed "
                        + e.getHarvest().getSeed() + " has errors: "
                        + e.getError());
            }
        }
        HarvestLog.printToReportFile(harvests, harvestLogReport);
    }
    
    /**
     * Construct a harvestLogReport File that doesn't exist already in the given basedir.
     * @param basedir the base-directory where to write harvestLogReport file  
     * @param harvestLogReportName The name of the harvestLogReport file 
     * @return a harvestLogReport File that doesn't exist already in the given basedir
     */
    private static File findReportFile(File basedir, String harvestLogReportName) {
        File harvestLogReport = new File(basedir, harvestLogReportName);
        int count = 0;
        while (harvestLogReport.exists()) {
            harvestLogReport = new File(basedir, harvestLogReportName + "."
                    + count);
            count++;
        }
        return harvestLogReport;
    }

    /**
     * Process the result of the analysis.
     * @param ingestFile A file containing criteria results in JSON format
     * @param seed The seed of the harvest from which we made the extracted text followed by criteria analysis
     * @param harvestName The name of the harvest in the NetarchiveSuite harvestDB
     * @param addToDatabase Should we add the result of the analysis to hbase?
     * @param daofactory Factoryclass to access hbase
     * @param rejectDKURLs Are we rejecting DK urls?
     * @return ProcessResult object
     * @throws Exception If anything bad happens
     */
    public static ProcessResult processFile(File ingestFile, String seed,
            String harvestName, boolean addToDatabase, DAOFactory daofactory, boolean rejectDKURLs)
            throws Exception {
        long linecount = 0L;
        long skippedCount = 0L;
        long ignoredCount = 0L;
        long insertedCount = 0L;
        Set<String> ignoredSet = new HashSet<String>();
        ProcessResult pr = new ProcessResult();
        List<SingleCriteriaResult> results = new ArrayList<SingleCriteriaResult>();
        
        
        if (!ingestFile.exists()) {
            System.err.println("ERROR: ingest file '"
                    + ingestFile.getAbsolutePath() + "' does not exist!");
            System.exit(1);
        }
        if (addToDatabase) {
            // Verify that seed exists in the seed-table, otherwise create it
            SeedsDAO sdao = daofactory.getSeedsDAO();
            if (!sdao.existsUrl(seed)) { // Note: this should only happen in the
                                         // manual workflow
                Seed s = new Seed(seed);
                s.setStatus(Status.ANALYSIS_COMPLETED);
                s.setStatusReason("Set to status '" + Status.ANALYSIS_COMPLETED
                        + "' as we have now analyzed the seed");
                try {
                    boolean inserted = sdao.insertSeed(s);
                    if (!inserted) {
                        System.err.println("Failed to insert the seed '" + seed
                                + "' into the seedstable");
                    } else {
                        System.out.println("Insert the seed '" + seed
                                + "' into the seedstable");
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

        }
        BufferedReader fr = StreamUtils.getBufferedReader(ingestFile);
        String line = "";
        String trimmedLine = null;
        // read file and ingest
        boolean foundAnalysisOfSeed = false;
        while ((line = fr.readLine()) != null) {
            trimmedLine = line.trim();
            if (!trimmedLine.isEmpty()) {
                boolean doInsert = true;
                boolean success = true;

                SingleCriteriaResult res = new SingleCriteriaResult(
                        trimmedLine, harvestName, seed);

                
                if (res.url == null || res.Cext1 == null
                        || res.Cext3Orig == null
                        || res.Cext3Orig.length() != 14) {
                    SystemUtils.log("Skipping line '"
                            + trimmedLine
                            + "': Missing one or more of fields url, Cext1, Cext3Orig", Level.INFO, true);
                    success = false;
                }
                if (success && doInsert) {
                    try {
                        Classification.analyzeRessource(res, DataSource.NETARKIVET);
                    } catch (Throwable e) {
                        String logMsg = "Ignoring analysis of url '" + res.url
                                + "' from harvest '" + harvestName
                                + "', seed '" + seed + "', ingestfile '"
                                + ingestFile.getAbsolutePath()
                                + "' due to exception ";
                        System.out.println(logMsg);
                        SystemUtils.writeToPrintStream(System.out, e);
                        // mail this to webdanica-admin:
                        String header = "Url '" + res.url
                                + "' ignored during ingest due to exception";
                        SystemUtils.sendAdminMail(header, logMsg, e);
                        continue;
                    }

                    String seedWithExtraSlash = seed + "/";
                    if (res.url.equals(seed)) {
                        foundAnalysisOfSeed = true;
                    } else if (res.url.equals(seedWithExtraSlash)) {
                        foundAnalysisOfSeed = true;
                        res.errorMsg += "Note: harvested url is the seed with a slash '"
                                + seedWithExtraSlash
                                + "'. Changing result-url to the original seedurl '"
                                + seed + "'\n";
                        res.urlOrig = res.url;
                        res.url = seed;
                    }
                    if (addToDatabase) {
                        CriteriaResultsDAO dao = daofactory
                                .getCriteriaResultsDAO();
                        SeedsDAO sdao = daofactory.getSeedsDAO();
                        DomainsDAO ddao = daofactory.getDomainsDAO();

                        boolean inserted = dao.insertRecord(res);
                        if (!inserted) {
                            SystemUtils.log("Record not inserted", Level.WARNING, true);
                        } else {
                            insertedCount++;
                        }
                        
                        boolean insertUrl = !sdao.existsUrl(res.url);
                        boolean rejected = false;
                        Seed s = null;
                        if (insertUrl) {
                            s = new Seed(res.url);
                            if (!ddao.existsDomain(s.getDomain())) {
                                Domain d = Domain.createNewUndecidedDomain(s
                                        .getDomain());
                                if (d.getDomain() != null) {
                                    SystemUtils.log("Inserting domain '" + d.getDomain() + "' in database for seed '" + res.url + "'", Level.INFO, true);
                                    ddao.insertDomain(d);
                                } else {
                                    SystemUtils.log("No domain found for seed '" + res.url + "': no domain insertion done", Level.WARNING, true);
                                }
                            }
                            rejected 
                                = FilterUtils.doFilteringOnSeed(s, daofactory.getBlackListDAO().getLists(true), 
                                        rejectDKURLs, daofactory.getDomainsDAO());
                        } else {
                            s = sdao.getSeed(res.url);
                        }
                        if (!rejected) {
                            Classification.decideDanicaStatusFromResult(res, s);
                        }
                        if (insertUrl) {
                            sdao.insertSeed(s);
                        } else {
                            sdao.updateSeed(s);
                        }
                    }
                    results.add(res);
                }
                linecount++;
                if (!doInsert) {
                    ignoredSet.add(res.url + ", " + res.Cext3);
                    ignoredCount++;
                } else if (!success) {
                    skippedCount++;
                }
            }
        }
        fr.close();
        if (addToDatabase && !foundAnalysisOfSeed) {
            SeedsDAO sdao = daofactory.getSeedsDAO();
            Seed s = sdao.getSeed(seed);
            s.setStatus(Status.ANALYSIS_FAILURE);
            String harvestname = TextUtils.findHarvestNameInStatusReason(s
                    .getStatusReason());
            if (harvestname == null) {
                harvestname = "N/A";
            }
            s.setStatusReason("Set to status '"
                    + Status.ANALYSIS_FAILURE
                    + "' as we have now no criteria analysis of the seed itself. harvestname is '"
                    + harvestname + "'");
            sdao.updateSeed(s);
        }

        boolean verbose = false;
        if (verbose) { // FIXME
            SystemUtils.log("Processed " + linecount + " lines", Level.INFO, true);
            SystemUtils.log("Skipped " + skippedCount + " lines", Level.INFO, true);
            SystemUtils.log("Ignored " + ignoredCount + " lines", Level.INFO, true);
            SystemUtils.log("Inserted " + insertedCount + " records", Level.INFO, true);

            for (String ignored : ignoredSet) {
                SystemUtils.log(" - " + ignored, Level.INFO, true);
            }
        }

        pr.results = results;
        pr.ignored = ignoredSet;
        return pr;
    }   
}
