package dk.kb.webdanica.core.tools;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dk.kb.webdanica.core.datamodel.dao.*;
import org.apache.commons.io.IOUtils;

import dk.kb.webdanica.core.datamodel.DanicaStatus;
import dk.kb.webdanica.core.datamodel.Domain;
import dk.kb.webdanica.core.datamodel.IngestLog;
import dk.kb.webdanica.core.datamodel.Seed;
import dk.kb.webdanica.core.datamodel.Status;
import dk.kb.webdanica.core.datamodel.URL_REJECT_REASON;
import dk.kb.webdanica.core.utils.DatabaseUtils;
import dk.kb.webdanica.core.utils.UrlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Program to load seeds into the webdanica database.
 * Policy:
 * - Seeds already present in the database is ignored
 * - seeds that are not proper URLs are not loaded into the database
 * - seeds that have extensions matching any of the suffixes in our ignored suffixes are also skipped
 * <p>
 * Remember to call program with -Dwebdanica.settings.file=$LOADSEEDS_HOME/webdanica_settings_file
 * and -Ddk.netarkivet.settings.file=$LOADSEEDS_HOME/settings_NAS_Webdanica.xml
 * and -Dlogback.configurationFile=$LOADSEEDS_HOME/silent_logback.xml
 * <p>
 * TESTED with webdanica-core/src/main/resources/outlink-reportfile-final-1460549754730.txt
 * TESTED with webdanica-core/src/main/resources/outlinksWithAnnotations.txt
 * TESTED with webdanica-core/src/main/resources/webdanica-seeds.table
 */
public class LoadSeeds {
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    public static final String ACCEPT_ARGUMENT = "--accepted";
    public static final String ONLYSAVESTATS_ARGUMENT = "--onlysavestats";

    public static void main(String[] args) throws Exception {
        boolean acceptSeedsAsDanica = false;
        if (args.length < 1 || args.length > 3) {
            System.err.println("Wrong number of arguments. One or two is needed. Given was " + args.length + " arguments");
            System.err.println("Correct usage: java LoadSeeds seedsfile [--accepted][--onlysavestats]");
            System.err.println("Exiting program");
            System.exit(1);
        }
        File seedsfile = new File(args[0]);
        if (!seedsfile.isFile()) {
            System.err.println("The seedsfile located '" + seedsfile.getAbsolutePath() + "' does not exist or is not a proper file");
            System.err.println("Exiting program");
            System.exit(1);
        }
        boolean onlysavestats = false;
        if (args.length > 1) { // parse optional arguments
            String arg2 = null;
            String arg3 = null;
            if (args.length == 2) {
                arg2 = args[1];
            } else if (args.length == 3) {
                arg2 = args[1];
                arg3 = args[2];
            }
            if (arg2.equalsIgnoreCase(ACCEPT_ARGUMENT)) {
                acceptSeedsAsDanica = true;
            } else if (arg2.equalsIgnoreCase(ONLYSAVESTATS_ARGUMENT)) {
                onlysavestats = true;
            } else {
                System.err.println("The second argument '" + arg2 + "' is unknown. Don't know what to do. Exiting program");
                System.exit(1);
            }
            if (arg3 != null) {
                if (arg3.equalsIgnoreCase(ACCEPT_ARGUMENT)) {
                    acceptSeedsAsDanica = true;
                } else if (arg3.equalsIgnoreCase(ONLYSAVESTATS_ARGUMENT)) {
                    onlysavestats = true;
                } else {
                    System.err.println("The third argument '" + arg3 + "' is unknown. Don't know what to do. Exiting program");
                    System.exit(1);
                }
            }
        }

        System.out.println("Processing seeds from file '" + seedsfile.getAbsolutePath() + "'");
        if (acceptSeedsAsDanica) {
            System.out.println("Ingesting all seeds as danica!");
        }
        if (onlysavestats) {
            System.out.println("Only saving statistics for the ingest. No update and reject information preserved!");
        }

        System.out.println();
        LoadSeeds loadseeds = new LoadSeeds(seedsfile, acceptSeedsAsDanica);
        loadseeds.writeAcceptLog = true;
        loadseeds.writeRejectLog = true;
        loadseeds.writeUpdateLog = true;
        loadseeds.onlysavestats = true;


        IngestLog res = loadseeds.processSeeds();
        System.out.println(res.getStatistics());
        File acceptLog = loadseeds.getAcceptLog();
        File rejectLog = loadseeds.getRejectLog();
        File updateLog = loadseeds.getUpdateLog();
        System.out.println("Acceptlog in file: " + (acceptLog == null ? "No log written due to error" : acceptLog.getAbsolutePath()));
        System.out.println("Rejectlog in file: " + (rejectLog == null ? "No log written due to error" : rejectLog.getAbsolutePath()));
        System.out.println("Updatelog in file: " + (updateLog == null ? "No log written due to error" : updateLog.getAbsolutePath()));

    }

    private File seedsfile;
    private boolean writeAcceptLog = false;
    private boolean writeRejectLog = false;
    private boolean writeUpdateLog = false;
    private File rejectLog = null;
    private File acceptLog = null;
    private File updateLog = null;
    private List<String> acceptedList = new ArrayList<String>();
    private DAOFactory daoFactory;
    private boolean ingestAsDanica;
    private boolean onlysavestats;

    public LoadSeeds(File seedsfile, boolean acceptSeedsAsDanica) {
        this.seedsfile = seedsfile;
        this.daoFactory = DatabaseUtils.getDao();
        this.ingestAsDanica = acceptSeedsAsDanica;
    }

    /**
     * @return the ingestLog for the file just processed
     */
    public IngestLog processSeeds() {
        long insertedcount = 0L;
        long rejectedcount = 0L;
        long duplicatecount = 0L;

        List<String> logentries = new ArrayList<String>();
        List<String> updatelogentries = new ArrayList<String>();
        List<String> domainLogentries = new ArrayList<String>();

        long lines = 0;

        try (BufferedReader fr = new BufferedReader(new FileReader(seedsfile))) {

            String line;
            while ((line = fr.readLine()) != null) {
                ++lines;
                if (lines % 10000 == 0) {
                    logger.info("Processed {} seeds.", lines);
                }

                String errMsg = "";
                String url = removeAnnotationsIfNecessary(line.trim());

                URL_REJECT_REASON rejectreason = UrlUtils.isRejectableURL(url);

                if (rejectreason == URL_REJECT_REASON.NONE) {
                    Seed singleSeed = new Seed(url);
                    if (ingestAsDanica) {
                        singleSeed.setDanicaStatus(DanicaStatus.YES);
                        singleSeed.setDanicaStatusReason("Known by curators to be danica");
                        singleSeed.setStatus(Status.DONE);
                        singleSeed.setStatusReason("Set to Status done at ingest to prevent further processing of this url");
                    }


                    try (SeedsDAO dao = daoFactory.getSeedsDAO();
                         DomainsDAO ddao = daoFactory.getDomainsDAO();) {

                        boolean inserted;
                        inserted = dao.insertSeed(singleSeed);

                        if (inserted) {
                            String domainName = singleSeed.getDomain();
                            if (!ddao.existsDomain(domainName)) {
                                Domain newdomain = Domain.createNewUndecidedDomain(domainName);
                                boolean insertedDomain = ddao.insertDomain(newdomain);
                                if (!insertedDomain) {
                                    domainLogentries.add("Failed to add domain '" + domainName + "' to domains table, the domain of seed '" + url + "'");
                                } else {
                                    domainLogentries.add("Added domain '" + domainName + "' to domains table, the domain of seed '" + url + "'");
                                }
                            }
                            insertedcount++;
                            if (!onlysavestats) {
                                acceptedList.add(url);
                            }

                        } else {
                            if (ingestAsDanica) {
                                // update state of seed if not already in Status.DONE
                                Seed oldSeed = dao.getSeed(url);
                                if (oldSeed == null) {
                                    // Should not happen
                                    rejectreason = URL_REJECT_REASON.UNKNOWN;
                                    logger.warn("The url '{}' should have been in database. But no record was found", url);
                                    errMsg = "The url '"+url+"' should have been in database. But no record was found";
                                } else {
                                    if (oldSeed.getDanicaStatus().equals(DanicaStatus.YES)) {
                                        updatelogentries.add("The seed '" + url + "' is already in the database with DanicaStatus.YES and status '" + oldSeed.getStatus() + "'");
                                    } else {
                                        updatelogentries.add("The seed '" + url + "' is already in the database with DanicaStatus=" + oldSeed.getDanicaStatus() + ", and status '" +
                                                oldSeed.getStatus() + "'. Changing to DanicaStatus.YES and status.DONE");
                                        oldSeed.setDanicaStatus(DanicaStatus.YES);
                                        oldSeed.setDanicaStatusReason("Known by curators to be danica");
                                        oldSeed.setStatus(Status.DONE);
                                        oldSeed.setStatusReason("Set to Status done at ingest to prevent further processing of this url");
                                        dao.updateSeed(oldSeed);
                                    }
                                }
                            } else {
                                rejectreason = URL_REJECT_REASON.DUPLICATE;
                                duplicatecount++;
                            }
                        }
                    } catch (DaoException e) {
                        logger.error("Failure in communication with HBase",e);
                        rejectreason = URL_REJECT_REASON.UNKNOWN;
                        errMsg = "Failure in communication with HBase: "+e.toString();
                    }

                    if (rejectreason != URL_REJECT_REASON.NONE) {
                        if (!onlysavestats) {
                            logentries.add(rejectreason + ": " + url + " " + errMsg);
                        }
                        rejectedcount++;
                    }
                }

                // add the updatedLog to logentries
                for (String logUpdated : updatelogentries) {
                    logentries.add("UPDATED: " + logUpdated);
                }

                // add the domains to logentries
                for (String logUpdated : domainLogentries) {
                    logentries.add("DOMAINS: " + logUpdated);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file " + seedsfile, e);
        }

        try {
            writeAcceptLog(insertedcount, rejectedcount, duplicatecount, lines);
            writeRejectLog(insertedcount, rejectedcount, duplicatecount, logentries, lines);
            writeUpdateLog(updatelogentries, domainLogentries);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write logs", e);
        }

        try {
            return logIngestStats(logentries, lines, insertedcount, rejectedcount, duplicatecount);
        } catch (Exception e) {
            throw new RuntimeException("Failed to log ingest stats",e);
        }

    }



    private void writeUpdateLog(List<String> updatelogentries, List<String> domainLogentries) throws
            IOException {
        // write update-log
        if (writeUpdateLog) {
            updateLog = new File(seedsfile.getParentFile(), seedsfile.getName() + ".updated.txt");
            int count = 0;
            while (updateLog.exists()) {
                updateLog = new File(seedsfile.getParentFile(), seedsfile.getName() + ".updated.txt" + "." + count);
                count++;
            }
            try (PrintWriter updatedWriter = new PrintWriter(new BufferedWriter(new FileWriter(updateLog)))) {
                String updatedHeader = "Update and domain Log for file '" + seedsfile.getAbsolutePath() + "' ingested at '"
                        + new Date() + "'";
                updatedWriter.println(updatedHeader);
                updatedWriter.println();
                if (!updatelogentries.isEmpty()) {
                    updatedWriter.println("Update - entries:");
                    for (String rej : updatelogentries) {
                        updatedWriter.println(rej);
                    }
                }
                if (!domainLogentries.isEmpty()) {
                    updatedWriter.println("domain-log - entries:");
                    for (String rej : domainLogentries) {
                        updatedWriter.println(rej);
                    }
                }
            }
        }
    }

    private void writeRejectLog(long insertedcount, long rejectedcount, long duplicatecount, List<
            String> logentries, long lines) throws IOException {
        // write reject-log
        if (writeRejectLog) {
            rejectLog = new File(seedsfile.getParentFile(), seedsfile.getName() + ".rejected.txt");
            int count = 0;
            while (rejectLog.exists()) {
                rejectLog = new File(seedsfile.getParentFile(), seedsfile.getName() + ".rejected.txt" + "." + count);
                count++;
            }
            try (PrintWriter rejectWriter = new PrintWriter(new BufferedWriter(new FileWriter(rejectLog)))) {

                String rejectHeader = "Rejectlog for file '" + seedsfile.getAbsolutePath() + "' ingested at '"
                        + new Date() + "'";
                String stats = "total lines: " + lines + ", accepted = " + insertedcount + ", rejected=" + rejectedcount
                        + " (of which " + duplicatecount + " duplicates";
                rejectWriter.println(rejectHeader);
                rejectWriter.println(stats);
                if (!onlysavestats) {
                    rejectWriter.println("Rejected seeds:");
                    for (String rej : logentries) {
                        rejectWriter.println(rej);
                    }
                }
            }

        }
    }

    private void writeAcceptLog(long insertedcount, long rejectedcount, long duplicatecount, long lines) throws
            IOException {
        // write accept-log
        if (writeAcceptLog) {
            acceptLog = new File(seedsfile.getParentFile(), seedsfile.getName() + ".accepted.txt");
            int count = 0;
            while (acceptLog.exists()) {
                acceptLog = new File(seedsfile.getParentFile(), seedsfile.getName() + ".accepted.txt" + "." + count);
                count++;
            }
            try (PrintWriter acceptWriter = new PrintWriter(new BufferedWriter(new FileWriter(acceptLog)))) {
                String acceptHeader = "Acceptlog for file '" + seedsfile.getAbsolutePath() + "' ingested at '"
                        + new Date() + "'";
                String stats = "total lines: " + lines + ", accepted = " + insertedcount + ", rejected=" + rejectedcount
                        + " (of which " + duplicatecount + " duplicates";
                acceptWriter.println(acceptHeader);
                acceptWriter.println(stats);
                if (!acceptedList.isEmpty()) {
                    acceptWriter.println("The " + insertedcount + " accepted :");
                    for (String acc : acceptedList) {
                        acceptWriter.println(acc);
                    }
                } else {
                    if (!onlysavestats) {
                        acceptWriter.println("None were accepted!");
                    }
                }
            }

        }
    }

    private String removeAnnotationsIfNecessary(String trimmedLine) {
        String[] trimmedParts = trimmedLine.split(" ");
        if (trimmedParts.length > 1) {
            return trimmedParts[0];
        } else {
            return trimmedLine;
        }
    }

    private IngestLog logIngestStats(List<String> logentries, long linecount, long insertedcount,
                                     long rejectedcount, long duplicatecount) throws Exception {
        IngestLogDAO dao = null;
        try {
            dao = daoFactory.getIngestLogDAO();
            IngestLog log = new IngestLog(logentries, seedsfile.getName(), linecount, insertedcount, rejectedcount, duplicatecount);
            dao.insertLog(log);
            return log;
        } finally {
            dao.close();
        }
    }

    File getRejectLog() {
        return this.rejectLog;
    }

    File getAcceptLog() {
        return this.acceptLog;
    }

    File getUpdateLog() {
        return this.updateLog;
    }
}
