package dk.kb.webdanica.datamodel.criteria;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import dk.kb.webdanica.utils.StreamUtils;

public class Harvest {
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


	/*		
	Seed: https://changecoachdk.com/resources/
		HarvestName: webdanica-trial-1470219964356
		Successful: true
		EndState: DONE
		Files harvested: 60-47-20160803102623295-00000-dia-prod-udv-01.kb.dk.warc.gz,60-metadata-1.warc

	 */	
	public static List<Harvest> parseHarvestLog(File harvestlog) throws IOException {
		List<Harvest> results = new ArrayList<Harvest>();

		BufferedReader fr = StreamUtils.getBufferedReader(harvestlog);        
		String line = "";
		Harvest current = null;
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
					current = new Harvest();
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
		
		public static void printToFile(List<Harvest> harvests, File outputFile) throws IOException {
			 outputFile.createNewFile();
		    	FileWriter fw = new FileWriter(outputFile.getAbsoluteFile());
		        BufferedWriter resfile = new BufferedWriter(fw);
		        for (Harvest h: harvests) {
		        	printOut(h, resfile);
		        }
		        fw.close();
		        
		}
		private static void printOut(Harvest h, BufferedWriter resfile) throws IOException {
			resfile.append("################################################");
			resfile.append("Analysis of harvest of seed " + h.seed);
	        resfile.append("################################################");
	        resfile.newLine();
	        for (SingleCriteriaResult scr: h.results) {
	        	resfile.append(scr.getValuesInString(",", ","));
	        	resfile.newLine();
	        }
	        
	        
        }
		
		public static void processHarvests(List<Harvest> harvests, File baseCriteriaDir) throws IOException, SQLException {

			for (Harvest h: harvests) {
				System.out.println("Seed: " + h.seed);
				String filename = null;
				if (h.getHeritrixWarcs().size() != 0){
					List<SingleCriteriaResult> results = new ArrayList<SingleCriteriaResult>();
					for (String file: h.getHeritrixWarcs()) {
						filename = file;
						File ingest = new File(baseCriteriaDir, filename + "/part-m-00000.gz"); // TODO for all part-m files
						if (ingest.exists()) {
							ProcessResult pr = CriteriaIngest.processFile(ingest);
							results.addAll(pr.results);
						} else {
							System.err.println("Skipping data for seed: " + h.seed + ". No part file found at " +  ingest.getAbsolutePath());
						}
					}
					h.setCriteriaResults(results);
				} else 
					System.err.println("Skipping data for seed: " + h.seed + ". No data harvested");
			}
		}
		
		
}



