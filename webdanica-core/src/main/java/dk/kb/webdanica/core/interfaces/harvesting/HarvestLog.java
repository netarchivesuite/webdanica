package dk.kb.webdanica.core.interfaces.harvesting;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import dk.kb.webdanica.core.datamodel.criteria.CriteriaIngest;
import dk.kb.webdanica.core.datamodel.criteria.ProcessResult;
import dk.kb.webdanica.core.datamodel.criteria.SingleCriteriaResult;
import dk.kb.webdanica.core.datamodel.dao.DAOFactory;
import dk.kb.webdanica.core.utils.StreamUtils;
import dk.netarkivet.harvester.datamodel.JobStatus;

/*		
Seed: https://changecoachdk.com/resources/
	HarvestName: webdanica-trial-1470219964356
	Successful: true
	EndState: DONE
	Files harvested: 60-47-20160803102623295-00000-dia-prod-udv-01.kb.dk.warc.gz,60-metadata-1.warc

 */
public class HarvestLog {
	public final static String seedPattern = "Seed: ";
	public final static String harvestnamePattern = "HarvestName: ";
	public final static String successfulPattern = "Successful: ";
	public final static String endstatePattern = "EndState: ";
	public final static String harvestedTimePattern = "HarvestedTime: ";
	public final static String filesPattern = "Files harvested: ";
	public final static String errorPattern = "Errors: ";
	
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
				if (trimmedLine.isEmpty() || trimmedLine.startsWith("######")) {
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
						String files = line.split(filesPattern)[1];
						List<String> fileList = new ArrayList<String>();
						if (!files.equals("null")) {
							String[] filesParts = files.split(",");
							for (String file: filesParts) {
								fileList.add(file);
							}
						}
						current.files = fileList;
					} else if (line.startsWith(errorPattern)) {
						errorLineWasLast = true;
						String error = line.split(errorPattern)[1].trim();
						current.errMsg = error;
					} else {
						if (errorLineWasLast) { // Add to error
							current.errMsg += line;
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
		
	public static List<HarvestError> processCriteriaResults(List<SingleSeedHarvest> harvests, File baseCriteriaDir, boolean addToDatabase, DAOFactory daofactory) throws Exception {
		List<HarvestError> errorReports = new ArrayList<HarvestError>();
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
							ProcessResult pr = CriteriaIngest.processFile(ingest, h.seed, h.harvestName, addToDatabase, daofactory );
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


	public static List<String> findPartFiles(File ingestDir) {
		if (ingestDir == null) {
			return null;
		}
		String[] parts = ingestDir.list(new FilenameFilter() {
			
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
	
	public static SingleSeedHarvest makeErrorObject(String error) {
		SingleSeedHarvest h = new SingleSeedHarvest();
	    h.harvestName = error;
	    h.errMsg = error;
	    h.files = new ArrayList<String>();
	    return h;
    }
}
