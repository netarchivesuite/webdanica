package dk.kb.webdanica.core.tools;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dk.kb.webdanica.core.datamodel.dao.*;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import dk.kb.webdanica.core.datamodel.Cache;
import dk.kb.webdanica.core.datamodel.DanicaStatus;
import dk.kb.webdanica.core.datamodel.Domain;
import dk.kb.webdanica.core.datamodel.IngestLog;
import dk.kb.webdanica.core.datamodel.Seed;
import dk.kb.webdanica.core.datamodel.Status;
import dk.kb.webdanica.core.datamodel.URL_REJECT_REASON;
import dk.kb.webdanica.core.seeds.filtering.AcceptedProtocols;
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


public static final String ACCEPT_ARGUMENT	= "--accepted";
public static final String ONLYSAVESTATS_ARGUMENT = "--onlysavestats";

/**
 * Main function handling the user arguments, and executing the LoadSeeds tool.
 * @param args
 * @throws Exception
 */
public static void main(String[] args) throws Exception {
    boolean acceptSeedsAsDanica = false;
    if (args.length < 1 || args.length > 3) {
        System.err.println("Wrong number of arguments. One or two is needed. Given was " + args.length + " arguments");
        System.err.println("Correct usage: java LoadSeeds seedsfile [--accepted][--onlysavestats]");
        System.err.println("Exiting program");
        System.exit(1);
    }
    File seedsfile = new File(args[0]);
    if (!seedsfile.isFile()){
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
    String datestamp = "[" + new Date() + "] ";
    System.out.print(datestamp + "Processing seeds from file '" + seedsfile.getAbsolutePath() + "'");
    System.out.println(" with the following schemas accepted: " 
            + StringUtils.join(AcceptedProtocols.getAcceptedProtocols(), ","));
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
    loadseeds.onlysavestats = onlysavestats;

    IngestLog res = loadseeds.processSeeds();
    System.out.println(res.getStatistics());
    File acceptLog = loadseeds.getAcceptLog();
    File rejectLog = loadseeds.getRejectLog();
    File updateLog = loadseeds.getUpdateLog();
    System.out.println("Acceptlog in file: " + (acceptLog==null?"No log written due to error": acceptLog.getAbsolutePath()));
    System.out.println("Rejectlog in file: " + (rejectLog==null?"No log written due to error": rejectLog.getAbsolutePath()));
    System.out.println("Updatelog in file: " + (updateLog==null?"No log written due to error": updateLog.getAbsolutePath()));
}

private File seedsfile;
// TODO remove this superfluous code
private boolean writeAcceptLog = false;
private boolean writeRejectLog = false;
private boolean writeUpdateLog = false;
private File rejectLog = null;
private File acceptLog = null;
private File updateLog = null;
private File errorsLog = null;

private List<String> acceptedList = new ArrayList<String>();
private DAOFactory daoFactory;
private boolean ingestAsDanica;
private boolean onlysavestats;

/**
 * LoadSeeds constructor.
 * Program will remove annotations from the outlink lines, if there are any.
 * @param seedsfile File containing a list of outlinks produced by Heritrix.
 * @param acceptSeedsAsDanica Ingests all seeds as Danica if true 
 */
public LoadSeeds(File seedsfile, boolean acceptSeedsAsDanica) {
    this.seedsfile = seedsfile;
    this.daoFactory = DatabaseUtils.getDao();
    this.ingestAsDanica = acceptSeedsAsDanica;
}

/**
 * Process the lines in the seedsfile given in the LoadSeeds constructor.
 * @return the ingestLog for the file just processed
 * @throws IOException 
 */
public IngestLog processSeeds() throws IOException {
    long insertedcount = 0L;
    long rejectedcount = 0L;
    long duplicatecount = 0L;
    long errorcount = 0L;
    long domainsAddedCount = 0L;
    long updatecount = 0L;

    List<String> logentries = new ArrayList<String>();
    initalizeLogs();

    long lines = 0;
    SeedsDAO sdao = daoFactory.getSeedsDAO();
    DomainsDAO ddao = daoFactory.getDomainsDAO();

    try (BufferedReader fr = new BufferedReader(new FileReader(seedsfile))) {

        String line;
        while ((line = fr.readLine()) != null) {
            if (line.trim().isEmpty()) { // Silently ignore empty lines
                continue;
            }
            ++lines;
            if (lines % 10000 == 0) {
                String datestamp = "[" + new Date() + "]";
                logger.info(datestamp + " Processed {} seeds.", lines);
                System.out.println(datestamp + " Processed " + lines + " seeds");
            }

            if (insertedcount % 10000 == 0 && insertedcount>0) {
                String datestamp = "[" + new Date() + "]";
                logger.info(datestamp + " Accepted {} seeds.", insertedcount);
                System.out.println(datestamp + " Accepted " + insertedcount + " seeds");
            }
            if (rejectedcount % 10000 == 0 && rejectedcount>0) {
                String datestamp = "[" + new Date() + "]";
                logger.info(datestamp + " Rejected {} seeds.", rejectedcount);
                System.out.println(datestamp + " Rejected " + rejectedcount + " seeds");
            }
            if (updatecount % 10000 == 0 && updatecount > 0) {
                String datestamp = "[" + new Date() + "]";
                logger.info(datestamp + " UpdatedLog reached {} lines.", updatecount);
                System.out.println(datestamp + " UpdatedLog reached " + updatecount + " lines");
            }

            String errMsg = "";
            String url = removeAnnotationsIfNecessary(line.trim());
            URL_REJECT_REASON rejectreason = UrlUtils.isRejectableURL(url);
            
            if (!rejectreason.equals(URL_REJECT_REASON.NONE)){
                String logEntry = rejectreason + ": " + url + " " + errMsg;
                if (!onlysavestats) {
                    logentries.add(logEntry);
                }
                rejectedcount++;
                writeTo(logEntry, rejectLog);
                continue;
            } else {
                Seed singleSeed = new Seed(url);
                if (ingestAsDanica) {
                    singleSeed.setDanicaStatus(DanicaStatus.YES);
                    singleSeed.setDanicaStatusReason("Known by curators to be danica");
                    singleSeed.setStatus(Status.DONE);
                    singleSeed.setStatusReason("Set to Status done at ingest to prevent further processing of this url");
                }

                try {
                    boolean inserted;
                    inserted = sdao.insertSeed(singleSeed);

                    if (inserted) {
                        String domainName = singleSeed.getDomain();
                        if (!ddao.existsDomain(domainName)) {
                            Domain newdomain = Domain.createNewUndecidedDomain(domainName);
                            boolean insertedDomain = ddao.insertDomain(newdomain);
                            String domainLogEntry = null;
                            if (!insertedDomain) {
                                domainLogEntry = "Failed to add domain '" + domainName + "' to domains table, the domain of seed '" + url + "'";
                                errorcount++;
                            } else {
                                domainLogEntry = "Added domain '" + domainName + "' to domains table, the domain of seed '" + url + "'";
                            }

                            if (!onlysavestats) {
                                logentries.add("DOMAINS: " + domainLogEntry); 
                            }
                            writeTo("DOMAINS: " + domainLogEntry, updateLog);
                        }
                        insertedcount++;
                        if (!onlysavestats) {
                            acceptedList.add(url);
                        }
                        writeTo(url, acceptLog);

                    } else {
                        if (ingestAsDanica) {
                            // update state of seed if not already in Status.DONE
                            Seed oldSeed = sdao.getSeed(url);
                            if (oldSeed == null) {
                                // Should not happen
                                rejectreason = URL_REJECT_REASON.UNKNOWN;
                                logger.warn("The url '{}' should have been in database. But no record was found", url);
                                errMsg = "The url '" + url + "' should have been in database. But no record was found";
                                // Add errMsg to errors.log with datestamp
                                String datestamp="[" + new Date() + "] ";
                                writeTo(datestamp + errMsg, errorsLog);
                                errorcount++;
                            } else {
                                String updateLogEntry = null;
                                if (oldSeed.getDanicaStatus().equals(DanicaStatus.YES)) {
                                    updateLogEntry = "The seed '" + url + "' is already in the database with DanicaStatus.YES and status '" + oldSeed.getStatus() + "'";
                                } else {
                                    updateLogEntry = "The seed '" + url + "' is already in the database with DanicaStatus=" + oldSeed.getDanicaStatus() + ", and status '" +
                                            oldSeed.getStatus() + "'. Changing to DanicaStatus.YES and status.DONE";

                                    oldSeed.setDanicaStatus(DanicaStatus.YES);
                                    oldSeed.setDanicaStatusReason("Known by curators to be danica");
                                    oldSeed.setStatus(Status.DONE);
                                    oldSeed.setStatusReason("Set to Status done at ingest to prevent further processing of this url");
                                    sdao.updateSeed(oldSeed);
                                }
                                writeTo("UPDATED: " + updateLogEntry, updateLog);

                            }
                        } else {
                            rejectreason = URL_REJECT_REASON.DUPLICATE;
                            duplicatecount++;
                        }
                    }
                } catch (DaoException e) {
                    logger.error("Failure in communication with HBase", e);
                    rejectreason = URL_REJECT_REASON.UNKNOWN;
                    errMsg = "Failure in communication with HBase: " + ExceptionUtils.getFullStackTrace(e);
                    errorcount++;

                    String datestamp="[" + new Date() + "] ";
                    writeTo(datestamp + errMsg, errorsLog);
                }
                if (!rejectreason.equals(URL_REJECT_REASON.UNKNOWN)) {
                    String logEntry = rejectreason + ": " + url + " " + errMsg;
                    if (!onlysavestats) {
                        logentries.add(logEntry);
                    }
                    rejectedcount++;
                    writeTo(logEntry, rejectLog);
                }

            }
        }
    } catch (IOException e) {
        throw new RuntimeException("Failed to read file " + seedsfile, e);
    } finally {
        ddao.close();
        sdao.close();
    }
    // trying to update the cache
    try {
        Cache.getCache(daoFactory);
    } catch (Exception e1) {
        System.err.println("WARNING: failed to update the statecache: " 
                + ExceptionUtils.getFullStackTrace(e1));
    }

    try {
        writeStatsToLogs(insertedcount, rejectedcount, duplicatecount, errorcount, lines, logentries, onlysavestats, domainsAddedCount, updatecount);        
    } catch (Exception e) {
        throw new RuntimeException("Failed to write logs", e);
    }

    try {
        return logIngestStats(logentries, lines, insertedcount, rejectedcount, duplicatecount, errorcount);
    } catch (Exception e) {
        throw new RuntimeException("Failed to log ingest stats",e);
    }

}

private void writeStatsToLogs(long insertedcount, long rejectedcount,
        long duplicatecount, long errorcount, long lines,
        List<String> logentries, boolean onlysavestats, long domainsAddedCount, long updatecount) throws IOException {

    String stats = "#total lines: " + lines + ", # seeds accepted = " + insertedcount + ", # seeds rejected=" + rejectedcount
            + " (of which " + duplicatecount + " are duplicates), errors during ingest: " + errorcount + ", onlysavestats=" + onlysavestats 
            + ", domainsAdded= " + domainsAddedCount + ", updates=" +  updatecount;
    String datestamp= "[" + new Date() + "] ";
    writeTo(datestamp + stats, acceptLog);
    writeTo(datestamp + stats, errorsLog);
    writeTo(datestamp + stats, rejectLog);
    writeTo(datestamp + stats, updateLog);
}

private void writeTo(String logEntry, File logFile) throws IOException {
    //System.out.println("Writing entry to file " + logFile.getAbsolutePath());
    try (FileWriter logFileWriter = new FileWriter(logFile, true);) {
        logFileWriter.write(logEntry);
        logFileWriter.write(System.lineSeparator());
        logFileWriter.flush();
        logFileWriter.close();
    }
}

private void initalizeLogs() throws IOException {
    errorsLog = new File(seedsfile.getParentFile(), seedsfile.getName() + ".errorslog.txt");
    int count = 0;
    while (errorsLog.exists()) {
        errorsLog = new File(seedsfile.getParentFile(), seedsfile.getName() + ".errorslog.txt" + "." + count);
        count++;
    }
    count=0;
    acceptLog = new File(seedsfile.getParentFile(), seedsfile.getName() + ".acceptedlog.txt");
    while (acceptLog.exists()) {
        acceptLog = new File(seedsfile.getParentFile(), seedsfile.getName() + ".acceptedlog.txt" + "." + count);
        count++;
    }

    try (PrintWriter acceptWriter = new PrintWriter(new BufferedWriter(new FileWriter(acceptLog)))) {
        String acceptHeader = "Acceptlog for ingest of file '" + seedsfile.getAbsolutePath() + "' started at '"
                + new Date() + "'";
        acceptWriter.println(acceptHeader);
        acceptWriter.println();
        acceptWriter.flush();
        acceptWriter.close();
    }

    count = 0;
    rejectLog = new File(seedsfile.getParentFile(), seedsfile.getName() + ".rejectedlog.txt");
    while (rejectLog.exists()) {
        rejectLog = new File(seedsfile.getParentFile(), seedsfile.getName() + ".rejected.txt" + "." + count);
        count++;
    }
    try (PrintWriter rejectWriter = new PrintWriter(new BufferedWriter(new FileWriter(rejectLog)))) {
        String rejectHeader = "Rejectlog for ingest of file '" + seedsfile.getAbsolutePath() + "' started at '"
                + new Date() + "'";
        rejectWriter.println(rejectHeader);
        rejectWriter.println();
        rejectWriter.flush();
        rejectWriter.close();
    }


    count = 0;
    updateLog = new File(seedsfile.getParentFile(), seedsfile.getName() + ".updatedLog.txt");
    while (updateLog.exists()) {
        updateLog = new File(seedsfile.getParentFile(), seedsfile.getName() + ".updatedLog.txt" + "." + count);
        count++;
    } 
    try (PrintWriter updatedWriter = new PrintWriter(new BufferedWriter(new FileWriter(updateLog)))) {
        String updatedHeader = "Update and domain Log for ingest of file '" + seedsfile.getAbsolutePath() + "' started at '"
                + new Date() + "'";
        updatedWriter.println(updatedHeader);
        updatedWriter.println();
        updatedWriter.flush();
        updatedWriter.close();
    } 
    System.out.println("AcceptLogs are written to " + acceptLog.getAbsolutePath());
    System.out.println("RejectLogs are written to " + rejectLog.getAbsolutePath());
    System.out.println("UpdateAndDomainLogs are written to " + updateLog.getAbsolutePath());
    System.out.println("Errors are written to " + errorsLog.getAbsolutePath());
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
        long rejectedcount, long duplicatecount, long errorcount) throws Exception {
    IngestLogDAO dao = null;
    try {
        dao = daoFactory.getIngestLogDAO();
        IngestLog log = new IngestLog(logentries, seedsfile.getName(), linecount, insertedcount, rejectedcount, duplicatecount, errorcount);
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
