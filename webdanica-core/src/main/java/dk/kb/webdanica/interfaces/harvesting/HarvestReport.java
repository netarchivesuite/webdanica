package dk.kb.webdanica.interfaces.harvesting;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import dk.kb.webdanica.datamodel.criteria.CriteriaIngest;
import dk.kb.webdanica.datamodel.criteria.ProcessResult;
import dk.kb.webdanica.datamodel.criteria.SingleCriteriaResult;
import dk.kb.webdanica.exceptions.WebdanicaException;
import dk.kb.webdanica.utils.StreamUtils;

/*		
Seed: https://changecoachdk.com/resources/
	HarvestName: webdanica-trial-1470219964356
	Successful: true
	EndState: DONE
	Files harvested: 60-47-20160803102623295-00000-dia-prod-udv-01.kb.dk.warc.gz,60-metadata-1.warc

 */
public class HarvestReport {
	public final static String seedPattern = "Seed: ";
	public final static String harvestnamePattern = "HarvestName: ";
	public final static String successfulPattern = "Successful: ";
	public final static String endstatePattern = "EndState: ";
	public final static String filesPattern = "Files harvested: ";
	
	public String seed;
	public String harvestName;
	public String Successful;
	public String EndState;
	public String[] FilesHarvested;
	public List<SingleCriteriaResult> results;
	private boolean resultsInitiated;
	private Set<String> errors;

	public static List<HarvestReport> readHarvestLog(File harvestlog) throws IOException {
		List<HarvestReport> results = new ArrayList<HarvestReport>();

		BufferedReader fr = StreamUtils.getBufferedReader(harvestlog);        
		String line = "";
		HarvestReport current = null;
		String trimmedLine = null;

		//read file add to list
		try {
			while ((line = fr.readLine()) != null) {
				trimmedLine = line.trim();
				if (trimmedLine.isEmpty() || trimmedLine.startsWith("######")) {
					// Skip line
				} else {
					if (line.startsWith(seedPattern)) {
						// start new harvest
						current = new HarvestReport();
						current.seed = line.split(seedPattern)[1];
					} else if (line.startsWith(harvestnamePattern)) {
						current.harvestName = line.split(harvestnamePattern)[1];
					} else if (line.startsWith(successfulPattern)) {
						current.Successful = line.split(successfulPattern)[1];
					} else if (line.startsWith(endstatePattern)) {
						current.EndState = line.split(endstatePattern)[1];
					} else if (line.startsWith(filesPattern)) {
						String files = line.split(filesPattern)[1];
						current.FilesHarvested = files.split(",");
						results.add(current);
					}
				}
			};
		} finally {
			IOUtils.closeQuietly(fr);
		}
		return results;
	}
	
	
	public Set<String> getAllFiles() {
		Set<String> allFiles = new HashSet<String>(); 
		for (String f: this.FilesHarvested) {
			allFiles.add(f);
		}
		return allFiles;
	}

	public List<String> getHeritrixWarcs() {
		List<String> allFiles = new ArrayList<String>(); 
		for (String f: this.FilesHarvested) {
			if (!f.contains("metadata")) {
				allFiles.add(f);
			}
		}
		return allFiles;
	}
	
	public Set<String> getMetadataWarcs() {
		Set<String> allFiles = new HashSet<String>(); 
		for (String f: this.FilesHarvested) {
			if (f.contains("metadata")) {
				allFiles.add(f);
			}
		}
		return allFiles;
	}
	
	public void setCriteriaResults(List<SingleCriteriaResult> results) {
		this.results = results;
	}
	
	public boolean resultsFound() {
		if (!resultsInitiated) {
			throw new WebdanicaException("CriteriaResults not yet associated with this harvest (" + this.harvestName + ")");
		} else {
			return results.size() > 0;
		}
	}

	public boolean resultsCalculated() {
		return this.resultsInitiated;
	}
	
	public static void printToFile(List<HarvestReport> harvests, File outputFile) throws IOException {
		outputFile.createNewFile();
		FileWriter fw = new FileWriter(outputFile.getAbsoluteFile());
		BufferedWriter resfile = new BufferedWriter(fw);
		for (HarvestReport h: harvests) {
			System.out.println("Printing out: " + h.seed);
			printOut(h, resfile);
		}
		fw.flush();
		fw.close();

	}
	private static void printOut(final HarvestReport h, final BufferedWriter resfile) throws IOException {
		resfile.append("################################################");
		resfile.newLine();
		resfile.append("Analysis of harvest of seed " + h.seed);
		resfile.newLine();
		resfile.append("################################################");
		resfile.newLine();
		if (!h.errors.isEmpty()) {
			resfile.append("Errors found: " + StringUtils.join(h.errors, ","));
			resfile.newLine();
		}
		if (h.results.isEmpty()) {
			resfile.append("No results found for seed");
			resfile.newLine();
		} else {
			for (SingleCriteriaResult scr: h.results) {
				resfile.append(scr.getValuesInString(",", ":"));
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
		
	public static List<HarvestError> processCriteriaResults(List<HarvestReport> harvests, File baseCriteriaDir, boolean addToDatabase) throws IOException, SQLException {
		List<HarvestError> errorReports = new ArrayList<HarvestError>();
		for (HarvestReport h: harvests) {
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
							ProcessResult pr = CriteriaIngest.processFile(ingest, h.seed, h.harvestName, addToDatabase);
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
			h.setErrors(errs);
			
		}
		return errorReports;
	}


	private void setErrors(Set<String> errs) {
	    this.errors = errs;
	    
    }


	private static List<String> findPartFiles(File ingestDir) {
		String[] parts = ingestDir.list(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("part-m-");
			}
		});
		
	    return Arrays.asList(parts);
    }
}
