package dk.kb.webdanica.core.interfaces.harvesting;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import dk.kb.webdanica.core.WebdanicaSettings;
import dk.kb.webdanica.core.datamodel.criteria.CriteriaIngest;
import dk.kb.webdanica.core.datamodel.criteria.ProcessResult;
import dk.kb.webdanica.core.datamodel.criteria.SingleCriteriaResult;
import dk.kb.webdanica.core.datamodel.dao.DAOFactory;
import dk.kb.webdanica.core.utils.SettingsUtilities;
import dk.kb.webdanica.core.utils.StreamUtils;
import dk.kb.webdanica.core.utils.SystemUtils;
import dk.netarkivet.harvester.datamodel.JobStatus;

/**	
 * 	Each harvestlog begins with a header like this: 
 *   Harvestlog for harvests initiated by the Webdanica webapp at DATE
 *   ################################################################################
 *   
 * Then we have a number of entries like this     
 *   Seed: http://host.domain.org/
 *   HarvestName: webdanica-trial-1505172711507
 *   Successful: true
 *   EndState: DONE
 *   HarvestedTime: 1505172819137
 *   Files harvested: 7759-8348-20170911233235902-00000-narcana-webdanica01.statsbiblioteket.dk.warc.gz,7759-metadata-1.warc
 *   Errors:  
 *   ################################################################################
 */
public class HarvestLog {
    
    public final static String harvestLogHeaderPrefix = "Harvestlog for";
	public final static String seedPattern = "Seed: ";
	public final static String harvestnamePattern = "HarvestName: ";
	public final static String successfulPattern = "Successful: ";
	public final static String endstatePattern = "EndState: ";
	public final static String harvestedTimePattern = "HarvestedTime: ";
	public final static String filesPattern = "Files harvested: ";
	public final static String errorPattern = "Errors: ";
	
	/**
	 * Read a given harvestlog file and return a list of SingleSeedHarvest objects.
	 * @param harvestlog a given HarvestLog file
	 * @return a list of SingleSeedHarvest objects
	 * @throws IOException if the harvestlog file is not found or some error while reading the file occurs
	 */
	public static List<SingleSeedHarvest> readHarvestLog(File harvestlog) throws IOException {
		List<SingleSeedHarvest> results = new ArrayList<SingleSeedHarvest>();

		BufferedReader fr = StreamUtils.getBufferedReader(harvestlog);        
		String line = "";
		SingleSeedHarvest current = null;
		String trimmedLine = null;

		//read file add to list
		try {
			boolean errorLineWasLast = false;
			while ((line = fr.readLine()) != null) {
				trimmedLine = line.trim();
				if (trimmedLine.isEmpty() || trimmedLine.startsWith("######") || trimmedLine.startsWith(harvestLogHeaderPrefix)) {
					// Skip line
				} else {

					if (line.startsWith(seedPattern)) {
						errorLineWasLast = false;
						// add harvestReport if current != null
						if (current != null) {
							results.add(current);
						}
						// start new harvest
						current = new SingleSeedHarvest();
						current.seed = line.split(seedPattern)[1];
					} else if (line.startsWith(harvestnamePattern)) {
						errorLineWasLast = false;
						current.harvestName = line.split(harvestnamePattern)[1];
					} else if (line.startsWith(successfulPattern)) {
						errorLineWasLast = false;
						current.successful = Boolean.valueOf(line.split(successfulPattern)[1]);
					} else if (line.startsWith(endstatePattern)) {
						errorLineWasLast = false;
						String finalStateStr = line.split(endstatePattern)[1];
						if (finalStateStr.equals("null")) {
							current.finishedState = JobStatus.FAILED;
						} else {
							current.finishedState = JobStatus.valueOf(finalStateStr);
						}
					} else if (line.startsWith(harvestedTimePattern)) {
						errorLineWasLast = false;
						current.harvestedTime = Long.parseLong(line.split(harvestedTimePattern)[1]);	
					} else if (line.startsWith(filesPattern)) {
						errorLineWasLast = false;
						List<String> fileList = new ArrayList<String>();
						String[] filesParts = line.split(filesPattern);
						if (filesParts.length > 1) {
							String files = line.split(filesPattern)[1];
							if (!files.equals("null")) {
								filesParts = files.split(",");
								for (String file: filesParts) {
									fileList.add(file);
								}
							} else {
								System.err.println("No harvest-files identified in line '" + line + "'");
							}
						} else {
							System.err.println("No harvest-files identified in line '" + line + "'");
						}
						current.files = fileList;
					} else if (line.startsWith(errorPattern)) {
						errorLineWasLast = true;
						String error = line.split(errorPattern)[1].trim();
						current.errMsg = new StringBuilder(error);
					} else {
						if (errorLineWasLast) { // Add to error
							current.errMsg.append(line);
						} else {
							System.err.println("Ignoring line: " + line);
						}
					}

				}
			};
			results.add(current); // add the last one to the list
		} finally {
			IOUtils.closeQuietly(fr);
		}
		return results;
	}
	
	/**
	 * Print a report of a list of harvests w/ criteria-results attached if any.
	 * @param harvests a list of harvests
	 * @param outputFile the file object to write out (File is overwritten, if it exists)
	 * @throws IOException If unable to write to the output file.
	 */
	public static void printToReportFile(List<SingleSeedHarvest> harvests, File outputFile) throws IOException {
		outputFile.createNewFile();
		FileWriter fw = new FileWriter(outputFile.getAbsoluteFile());
		BufferedWriter resfile = new BufferedWriter(fw);
		for (SingleSeedHarvest h: harvests) {
			System.out.println("Printing out report for seed: " + h.seed);
			printOut(h, resfile);
		}
		fw.flush();
		fw.close();
	}
	
	/**
	 * Print out the data of a SingleSeedHarvest to the given BufferedWriter.
	 * @param h A SingleSeedHarvest
	 * @param resfile a BufferedWriter
	 * @throws IOException if unable to write to the BufferédWriter
	 */
	private static void printOut(final SingleSeedHarvest h, final BufferedWriter resfile) throws IOException {
		resfile.append("################################################");
		resfile.newLine();
		resfile.append("Analysis of harvest of seed " + h.seed);
		resfile.newLine();
		resfile.append("################################################");
		resfile.newLine();
		if (h.hasErrors()) {
			resfile.append("Errors found: '" + h.getErrMsg()  + "'");
			resfile.newLine();
		}
		List<SingleCriteriaResult> results = h.getCritresults();
		if (results == null || results.isEmpty()) {
			resfile.append("No criteria results found for seed");
			resfile.newLine();
		} else {
			for (SingleCriteriaResult scr: results) {
				resfile.append(scr.getValuesInString(",", ":", SingleCriteriaResult.StringCriteria));
				resfile.newLine();
				resfile.append("Text: " + scr.getCText());
				resfile.newLine();
				resfile.append("Links: " + scr.getClinks());
				resfile.newLine();
				resfile.newLine();
				
			} 
		}
		resfile.newLine();
		resfile.flush();
	}
	
	/**
	 * Process the criteria-results for a list of harvests.
	 * During processing of each harvest, the found criteriaresults is added to the  
	 * @param harvests a list of harvests 
	 * @param baseCriteriaDir The base criteria-directory where to look for the analysises of the harvested materiale.
	 * @param addToDatabase Add the results to the database Yes or No.
	 * @param daofactory factoryClass to connect to hbase
	 * @return a list of HarvestError objects for any errors encountered during processing
	 * @throws Exception
	 */
	public static List<HarvestError> processCriteriaResults(List<SingleSeedHarvest> harvests, File baseCriteriaDir, boolean addToDatabase, DAOFactory daofactory) throws Exception {
		List<HarvestError> errorReports = new ArrayList<HarvestError>();
		boolean rejectDKURLs = SettingsUtilities.getBooleanSetting(
                WebdanicaSettings.REJECT_DK_URLS, false);
		for (SingleSeedHarvest h: harvests) {
			Set<String> errs = new HashSet<String>();
			if (h.getHeritrixWarcs().size() != 0){
				List<SingleCriteriaResult> results = new ArrayList<SingleCriteriaResult>();
				
				for (String filename: h.getHeritrixWarcs()) {
					File ingestDir = new File(baseCriteriaDir, filename);
					// Find all part-m-?????.gz files
					List<String> partfiles = findPartFiles(ingestDir);
					if (partfiles.isEmpty()) {
						errs.add("No partfiles found for file '" + filename + "' in folder '" 
								+ ingestDir.getAbsolutePath() + "'");
					} else {
						for (String partfile: partfiles) {
							File ingest = new File(ingestDir, partfile);
							ProcessResult pr = CriteriaIngest.processFile(ingest, h.seed, h.harvestName, addToDatabase, daofactory, rejectDKURLs );
							if (pr.results.isEmpty()) {
								errs.add("Partfile '" + ingest.getAbsolutePath() 
										+ "' contained no results.original warc: " + filename);
							} 
							results.addAll(pr.results);
						}
						
					}
				}
				if (results.isEmpty()) {
					HarvestError he = new HarvestError(h, "no Criteria-results found: " + StringUtils.join(errs, ",")); 
					errorReports.add(he);
				}
				h.setCriteriaResults(results);
			} else { // no data harvested
				HarvestError he = new HarvestError(h, "no data harvested for seed: " +  StringUtils.join(errs, ","));
				errorReports.add(he);
			}
			h.setErrMsg(StringUtils.join(errs, ","));
			
		}
		return errorReports;
	}

	/**
	 * Look for files starting with "part-m-" e.g. part-m-00000.gz in the result-folder.
	 * @param resultDir the directory where the result of the criteria analysis is written
	 * @return a list of files starting with "part-m-" e.g. part-m-00000.gz in the result-folder
	 */
	public static List<String> findPartFiles(File resultDir) {
		if (resultDir == null) {
			return null;
		}
		String[] parts = resultDir.list(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("part-m-");
			}
		});
		if (parts != null) {
			return Arrays.asList(parts);
		} else {
			return Arrays.asList(new String[]{});
		}
    }
	
	/**
     * Write a list of SingleSeedHarvest results to a harvestlog.
     * @param harvestLog The harvestlog file object to write to.
     * @param harvestLogHeader The header of the harvestlog file.
     * @param includeOnlySuccessFulHarvests If true, we only include successful harvests, otherwise we include them all.
     * @param results the list of SingleSeedHarvest results.
     * @param writeToStdout write to System.out/System.err (true or false)
     * @return the number of harvests written to the file.
     * @throws Exception
     */
    public static int writeHarvestLog(File harvestLog, String harvestLogHeader, boolean includeOnlySuccessFulHarvests, List<SingleSeedHarvest> results, boolean writeToStdout) throws Exception {
        // Initialize harvestLogWriter
        PrintWriter harvestLogWriter = new PrintWriter(new BufferedWriter(new FileWriter(harvestLog)));
        int harvestsWritten = 0;
        harvestLogWriter.println(harvestLogHeader);
        harvestLogWriter.println(StringUtils.repeat("#", 80));
        for (SingleSeedHarvest s: results) {
            if (!s.successful) {
                if (includeOnlySuccessFulHarvests) {
                    String logMsg = "Skipping failed harvest '" + s.harvestName + "' of seed '" + s.seed + "'";
                    SystemUtils.log(logMsg, Level.WARNING, writeToStdout);
                    continue;
                } else {
                    String logMsg = "Including failed harvest '" + s.harvestName + "' of seed '" + s.seed + "' in harvestlog";
                    SystemUtils.log(logMsg, Level.WARNING, writeToStdout);
                }
            }
            harvestLogWriter.println(HarvestLog.seedPattern + s.getSeed());
            harvestLogWriter.println(HarvestLog.harvestnamePattern + s.getHarvestName());
            harvestLogWriter.println(HarvestLog.successfulPattern + s.isSuccessful());
            harvestLogWriter.println(HarvestLog.endstatePattern + s.getFinalState());
            harvestLogWriter.println(HarvestLog.harvestedTimePattern + s.getHarvestedTime());
            harvestLogWriter.println(HarvestLog.filesPattern + StringUtils.join(s.getFiles(), ","));
            String errString = (s.getErrMsg() != null?s.getErrMsg():"");
            String excpString = (s.getException() != null)? "" + s.getException():""; 
            harvestLogWriter.println(HarvestLog.errorPattern + errString + " " + excpString);
            harvestLogWriter.println(StringUtils.repeat("#", 80));
            harvestsWritten++;
        }   
        harvestLogWriter.close();
        return harvestsWritten;
    }
	
}
