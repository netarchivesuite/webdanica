package dk.kb.webdanica.core.datamodel.criteria;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.simple.parser.ParseException;

import dk.kb.webdanica.core.criteria.FrequentWords;
import dk.kb.webdanica.core.datamodel.DanicaStatus;
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
import dk.kb.webdanica.core.utils.StreamUtils;
import dk.kb.webdanica.core.utils.SystemUtils;
import dk.kb.webdanica.core.utils.TextUtils;

/**
 * Ingest a file and calculate the probability of being Danish (the DanishCode).
 * @author svc
 */
public class CriteriaIngest {
	
	public static void ingest(File harvestLog, File baseCriteriaDir, boolean addHarvestToDatabase, boolean addCriteriaResultsToDatabase, DAOFactory daofactory) throws Exception {
		File basedir = harvestLog.getParentFile();
		String harvestLogReportName = harvestLog.getName() + ".report.txt";
		File harvestLogReport = findReportFile(basedir, harvestLogReportName);
		List<SingleSeedHarvest> harvests = HarvestLog.readHarvestLog(harvestLog);
		if (addHarvestToDatabase) {
			HarvestDAO hdao = daofactory.getHarvestDAO();
			for (SingleSeedHarvest hp: harvests) {
				if (!hdao.exists(hp.getHarvestName())) {
					hdao.insertHarvest(hp);
				} else {
					System.out.println("Skip ingest of harvest '" + hp.getHarvestName() + "' -  a harvest with this name already exists in database");
				}
			}
		}
		List<HarvestError> errors = HarvestLog.processCriteriaResults(harvests, baseCriteriaDir, addCriteriaResultsToDatabase, daofactory);
		if (addCriteriaResultsToDatabase) {
			SeedsDAO sdao = daofactory.getSeedsDAO();
			for (HarvestError e: errors) {
				String seed = e.getHarvest().getSeed();
				String harvestname = e.getHarvest().getHarvestName();
				String error = e.getError();
				Seed s = sdao.getSeed(seed);
				s.setStatus(Status.ANALYSIS_FAILURE);
				s.setStatusReason("Set to status " 
						+ Status.ANALYSIS_FAILURE + " using harvestname '" + harvestname + "'. Failures occured during processing: " + error);
				sdao.updateSeed(s);
			}
		} else { // just mention the errors in the screen
			System.out.println("Identified " + errors.size() + " errors");
			for (HarvestError e: errors) {
				System.out.println("Harvest of seed " + e.getHarvest().getSeed() + " has errors: " + e.getError());
			}
		}
		HarvestLog.printToReportFile(harvests, harvestLogReport);
	}	

	private static File findReportFile(File basedir, String harvestLogReportName) {
		File harvestLogReport = new File(basedir, harvestLogReportName);
		int count = 0;
		while(harvestLogReport.exists()) {
			harvestLogReport = new File(basedir, harvestLogReportName + "." +  count);
			count++;
		}
	    return harvestLogReport;
    }

	/**
	 * 
	 * @param ingestFile
	 * @param listIgnored
	 * @param harvestName
	 * @param seed
	 * @param addToDatabase 
	 * @return ProcessResult object
	 * @throws IOException
	 * @throws ParseException 
	 */
	public static ProcessResult processFile(File ingestFile, String seed, String harvestName, 
				boolean addToDatabase, DAOFactory daofactory) throws Exception {
		long linecount=0L;
		long skippedCount=0L;
		long ignoredCount=0L;
		long insertedCount=0L;
		Set<String> ignoredSet = new HashSet<String>();
		ProcessResult pr = new ProcessResult();
		List<SingleCriteriaResult> results = new ArrayList<SingleCriteriaResult>();

		if (!ingestFile.exists()) {
			System.err.println("ERROR: ingest file '" + ingestFile.getAbsolutePath() + "' does not exist!");
			System.exit(1);
		}  
		if (addToDatabase) {
			// Verify that seed exists in the seed-table, otherwise create it
			SeedsDAO sdao = daofactory.getSeedsDAO();
			if (!sdao.existsUrl(seed)) { // Note: this should only happen in the manual workflow
				Seed s = new Seed(seed);
				s.setStatus(Status.ANALYSIS_COMPLETED);
				s.setStatusReason("Set to status '" 
						+ Status.ANALYSIS_COMPLETED 
						+ "' as we have now analyzed the seed");
				try {
					boolean inserted = sdao.insertSeed(s);
					if (!inserted) {
						System.err.println("Failed to insert the seed '" + seed + "' into the seedstable"); 
					} else {
						System.out.println("Insert the seed '" + seed + "' into the seedstable");
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
			
		}
		BufferedReader fr = StreamUtils.getBufferedReader(ingestFile);        
		String line = "";
		String trimmedLine = null;
		//read file and ingest
		boolean foundAnalysisOfSeed = false;
		while ((line = fr.readLine()) != null) {
			trimmedLine = line.trim();
			if (!trimmedLine.isEmpty()) {
				boolean doInsert = true;   
				boolean success = true;

				SingleCriteriaResult res = new SingleCriteriaResult(trimmedLine, harvestName, seed);
				if (res.url == null || res.Cext1 == null || res.Cext3Orig == null || res.Cext3Orig.length() != 14) {
					log("Skipping line '" + trimmedLine 
							+ "': Missing one or more of fields url, Cext1, Cext3Orig");
					success = false;
				}
				if (success && doInsert) {
					try {
						prepareLine(res, DataSource.NETARKIVET);
					} catch (Throwable e) {
						String logMsg = "Ignoring analysis of url '" + res.url + "' from harvest '" + harvestName + "', seed '" + seed + "', ingestfile '" 
								+ ingestFile.getAbsolutePath() + "' due to exception ";
						System.out.println(logMsg);
						SystemUtils.writeToPrintStream(System.out, e);
						// mail this to webdanica-admin:
						String header = "Url '" + res.url + "' ignored during ingest due to exception";
						SystemUtils.sendAdminMail(header, logMsg, e);
						continue;
					}
					// REMOVED log for loadTest FIXME
					//log("Url '" + res.url + "' has danishCode: " +  res.calcDanishCode);
					String seedWithExtraSlash = seed + "/"; 
					if (res.url.equals(seed)) {
						foundAnalysisOfSeed = true;
					} else if (res.url.equals(seedWithExtraSlash)) {
						foundAnalysisOfSeed = true;
						res.errorMsg += "Note: harvested url is the seed with a slash '" + seedWithExtraSlash + "'. Changing result-url to the original seedurl '" + seed + "'\n"; 
						res.urlOrig = res.url;
						res.url = seed;
					}
					if (addToDatabase) {
						CriteriaResultsDAO dao = daofactory.getCriteriaResultsDAO();
						SeedsDAO sdao = daofactory.getSeedsDAO();
						DomainsDAO ddao = daofactory.getDomainsDAO();
						
						boolean inserted = dao.insertRecord(res);
						if (!inserted) {
							log_error("Record not inserted");
						} else {
							insertedCount++;
						}
						
						boolean insertUrl = !sdao.existsUrl(res.url);
						Seed s = null;
						if (insertUrl) {
							s = new Seed(res.url);
							if (!ddao.existsDomain(s.getDomain())) {
								Domain d = Domain.createNewUndecidedDomain(s.getDomain());
								ddao.insertDomain(d);
							}
						} else {
							s = sdao.getSeed(res.url);
						}
						String statusReasonPrefix = "Harvested by harvest '" +  res.harvestName + "' and now successfully analyzed. ";
						int danishcode = res.calcDanishCode;
						if (Codes.IsLikelyDanica(danishcode)) {
							s.setDanicaStatus(DanicaStatus.YES);
							s.setDanicaStatusReason("Set to DanicaStatus YES, because of danishcode '" + danishcode + "' belongs to the likelydk category");
							s.setStatus(Status.DONE);
							s.setStatusReason( statusReasonPrefix + "Processing finished as we now consider this seed Danica");
						} else if (Codes.IsLikelyNotDanica(danishcode)) {
							s.setDanicaStatus(DanicaStatus.NO);
							s.setDanicaStatusReason("Set to DanicaStatus NO, because of danishcode '" + danishcode + "' belongs to the notlikelydk category");
							s.setStatus(Status.DONE);
							s.setStatusReason(statusReasonPrefix + "Processing finished as we now consider this seed not-Danica");
						} else {
							s.setDanicaStatus(DanicaStatus.UNDECIDED);
							s.setDanicaStatusReason("Still UNDECIDED, as the danishcode '" + danishcode + "'doesn't give us any safe indications of danicastatus");
							s.setStatus(Status.AWAITS_CURATOR_DECISION);
							s.setStatusReason(statusReasonPrefix + "Now awaiting a curatorial decision");
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
			String harvestname = TextUtils.findHarvestNameInStatusReason(s.getStatusReason());
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
		if (verbose) { //FIXME
			log("Processed " + linecount + " lines");
			log("Skipped " + skippedCount + " lines");
			log("Ignored " + ignoredCount + " lines");
			log("Inserted " + insertedCount + " records");

			for(String ignored: ignoredSet) {
				log(" - " + ignored);
			}
		}
		
		pr.results = results;
		pr.ignored = ignoredSet;
		return pr; 	
	}

	private static void log_error(String string) {
		System.err.println(string);
	    
    }
	private static void log(String string) {
	    System.out.println(string);
	    
    }

	/**
	 * This sets some extra criteria not included in the CombinedCombo.
	 * @param res
	 * @param source
	 * @return
	 */
	private static void prepareLine(SingleCriteriaResult res, DataSource source) {
		/*** set source ***/
		res.source = source;
		// Remove because of noise FIXME
        //log("Set source to: " + source);
		/*** pre-calculate calcDanishCode and other fields ***/
		// See CalcDanishCode.getCalcDkCodeText for explanations
        
		res.calcDanishCode = 0;

		//res.calcDanishCode = 1  size=0 
		if (res.Cext1==0){ 
			res.calcDanishCode = 1;   //no text
			return; // we stop now: as we believe the rest of the fields are empty 
		}
		
		/*******************************************/
		/*** Update missing fields  if necessary ***/
		/*******************************************/
		
		/*** calculate C15b ***/
		String tld = CriteriaUtils.findTLD(res.url);
		if (!tld.isEmpty()) {
			res.C.put("C15b", tld);
		} else {
			res.C.put("C15b", "-"); 
		}

		/*** calculate C8b if equal to 8a ***/
		if ((res.C.get("C8a") != null)) { 
			if (res.C.get("C8b").equals(res.C.get("C8a"))) {
				res.C.put("C8b", CriteriaUtils.find8bVal(res.url)); 
			} 
		}
		
		/*** calculate C3e restricted o/oe,ae, aa in html ***/
		if (res.C.get("C3b") != null) { //added 9/9
			res.C.put("C3e", CriteriaUtils.findNew3ValToken(res.C.get("C3b"))); 
		}

		/*** calculate C3f restricted o/oe,ae, aa in url ***/
		if (res.C.get("C3d") != null) { //added 9/9
				res.C.put("C3f",CriteriaUtils.findNew3ValToken(res.C.get("C3d"))); 
		}

		/*** END: Update missing fields     ***/
		/**************************************/

		// Find a danishCode for the result
		
		if (res.C.get("C1a") != null) {  
			CodesResult coderes = CodesResult.setcodes_mail(res.C.get("C1a"), res.C.get("C5a"), res.C.get("C5b"), 
					res.C.get("C15b"), res.C.get("C7g"));
			if (coderes.calcDanishCode>0) {
				res.calcDanishCode = coderes.calcDanishCode;
				res.intDanish = coderes.intDanish;
				return;
			}
		}

		// test c4a og c4b
		if (res.C.get("C4a").equals("da")) {
			// look at the percentage in C4b
			String languagesFound = res.C.get("C4b");
			if (CalcDanishCode.checkForDanishCode4(res, languagesFound)) {
				return;
			}
		}
		
		//res.calcDanishCode =20-27, 40-47 - many dk indications 
		if (res.C.get("C15a")!=null && res.C.get("C16a")!=null && res.C.get("C17a")!=null 
				&& res.Cext1>200 && res.C.get("C3a")!=null && res.C.get("C4a")!=null 
				&& res.C.get("C5a")!=null && res.C.get("C5b")!=null && res.C.get("C6a")!=null) {
			CodesResult.setcodes_dkLanguageVeryLikely(res);
			if (res.calcDanishCode > 0) {
				return;
			}
		}

		if (res.C.get("C3a")!=null) {  
			CodesResult coderes = CodesResult.setcodes_languageDklettersNew(res.C.get("C3a"), res.C.get("C5a"), 
					res.C.get("C5b"), res.C.get("C15b")); 
			if (coderes.calcDanishCode>0) {
				res.calcDanishCode = coderes.calcDanishCode;
				res.intDanish = coderes.intDanish;
				return;
			}
		}

		//res.calcDanishCode = 76-77  likely dk language (not norwegian)
		if (res.C.get("C4a") !=null && res.C.get("C5a") !=null) {  
			CodesResult coderes = CodesResult.setcodes_languageDkNew(res.C.get("C4a"), res.C.get("C5a"), res.C.get("C5b"), 
					res.C.get("C15b"));
			if (coderes.calcDanishCode>0) {
				res.calcDanishCode = coderes.calcDanishCode;
				res.intDanish = coderes.intDanish;
				return;
			}
		}

		//find tlf an +45 to 315, 316, 317");
		if (res.C.get("C2a")!=null) {  
			CodesResult cr = CodesResult.setcodes_oldPhone(res.C.get("C2a"),res.C.get("C5a"),res.C.get("C5b"), 
					res.C.get("C15b")); 
			if (cr.calcDanishCode>0) {
				res.calcDanishCode = cr.calcDanishCode ;
				res.intDanish = cr.intDanish;
				return;
			}
		}
		

		//res.calcDanishCode =100-107 small sizes
		if (res.Cext1<=200) { 
			CodesResult coderes = CodesResult.setcodes_smallSize(res.C.get("C4a"), res.C.get("C3a"), res.C.get("C3b"), res.C.get("C3c"), 
					res.C.get("C3d"), res.C.get("C6a"), res.C.get("C6b"), res.C.get("C6c"));
			if(coderes.calcDanishCode>0) {
				res.calcDanishCode = coderes.calcDanishCode;
				res.intDanish = coderes.intDanish;
				return;
			}
		}

		//res.calcDanishCode =10-12 asian/arabic languages	
		if (res.C.get("C4a")!= null ) {  
			CodesResult coderes = CodesResult.setcodes_otherLanguagesChars(res.C.get("C4a"));
			if (coderes.calcDanishCode>0) {
				res.calcDanishCode = coderes.calcDanishCode;
				res.intDanish = coderes.intDanish;
				return;
			}
		}

		// See CalcDanishCode.getCalcDkCodeText for explanations
		CodesResult cr = CodesResult.setcodes_notDkLanguageVeryLikely(res); //get callcode and IntDanish  and check in depth
		if (cr.calcDanishCode>0) {
			res.calcDanishCode = cr.calcDanishCode;
			res.intDanish = cr.intDanish;
			return;
		}


		//res.calcDanishCode = 2: double-char (Cext2 >= 200)
		if (res.Cext2>=200) { 
			res.calcDanishCode = 2; //lots of doublechars
			return;
		}
		//res.calcDanishCode = 220: double-char( 130 <= Cext2 < 200)
		if (res.Cext2>=130) {
			res.calcDanishCode = 220; //lots of doublechars
			return;
		}
		
		
		//res.calcDanishCode = 3
		if (res.C.get("C15b").equals("dk")){ // TLD-check
			res.calcDanishCode = 3; // I think we should test in more depth
			return;
		}
		
		///////////////////////////////////
		// set calcDanishCode-codes for which fields are set
		res.calcDanishCode = CodesResult.findNegativBitmapCalcCode(res);
		return;
	}
	
	private static void someUpdateCode(SingleCriteriaResult res) {
		//update 3g
		update3g(res);
		//update 8c foreninger
		update8c(res);


		//update 9e firmaer on the basis on
		update9e(res);

		//update 10c 
		String C10c = res.C.get("C10c");
		if (C10c!=null && (!C10c.isEmpty() && !C10c.startsWith("0"))) {
			res.C.put("C10c", CriteriaUtils.findC10cval(C10c));
		}

		Set<Integer> codeSet = Codes.getCodesForNOTDanishResults(); 
		codeSet.addAll(Codes.getCodesForMaybees());
		/*** calculate C2b phone numbers ***/
		if (res.calcDanishCode<=0 || codeSet.contains(res.calcDanishCode)) {
			CodesResult cr = CodesResult.setcodes_newPhone(res.C.get("C2b"),res.C.get("C5a"),
					res.C.get("C5b"),res.C.get("C15b")); 
			if (cr.calcDanishCode>0) {
				res.calcDanishCode = cr.calcDanishCode ;
				res.intDanish = cr.intDanish;
			}
		}

		/** town names */
		if (res.calcDanishCode<=0 || codeSet.contains(res.calcDanishCode)) {
			String C7g = res.C.get("C7g"); 
			if (!( (C7g==null) || (C7g.isEmpty()) || (C7g.startsWith("0")) )) {
				CodesResult cr = CodesResult.setcodes_mail(
						res.C.get("C1a"), res.C.get("C5a"), res.C.get("C5b"), res.C.get("C15b"), C7g);
				if (cr.calcDanishCode>0) {
					res.calcDanishCode = cr.calcDanishCode;
					res.intDanish = cr.intDanish;
				} else {
					res.calcDanishCode = 230; 
					res.intDanish = 75/100;
				}
			}
		}

		for (CodesResult.NotDkExceptions ex: CodesResult.NotDkExceptions.values()) {
			if (res.calcDanishCode<=0 || codeSet.contains(res.calcDanishCode) ) {
				CodesResult cr = CodesResult.setcodes_notDkLanguageVeryLikelyNewFields(res, ex); //get calcode and IntDanish  and check in depth
				if (cr.calcDanishCode>0) {
					res.calcDanishCode = cr.calcDanishCode;
					res.intDanish = cr.intDanish;
				}
			}
		}

		//update bits if already negativeBitmapCalcCode
		if (res.calcDanishCode <= 0) {
			res.calcDanishCode = CodesResult.findNegativBitmapCalcCode(res);
		}


	}
	
	private static void update9e(SingleCriteriaResult res) {
		String C9b = res.C.get("C9b");
    	String C9e = res.C.get("C9e");
    	if (C9b!=null && (!C9b.isEmpty() && !C9b.startsWith("0"))) {
    		res.C.put("C9e", CriteriaUtils.findC9eval(C9b, C9e));
    	}
	    
    }
	private static void update8c(SingleCriteriaResult res) {
		String C8a = res.C.get("C8a");
	    String C8c = res.C.get("C8c");
    	if (C8a!=null && (!C8a.isEmpty() && !C8a.startsWith("0"))) {
    		String oldC8c = C8c;
    		C8c = CriteriaUtils.findC8cval(C8a, C8c);
    		log("Updating criteria C8c. Changed from '" + oldC8c + "' to '" + C8c + "' using the C8a value '" + C8a + "'");
    		res.C.put("C8c", C8c);
    	}
	    
    }
	private static void update3g(SingleCriteriaResult res) {
		String C3g = res.C.get("C3g");
		if (C3g!=null && (!C3g.isEmpty() && !C3g.startsWith("0"))) {
			String oldC3g = C3g;
            Set<String> tokens = TextUtils.tokenizeText(C3g.substring(1).trim());
            List<String> words = Arrays.asList(FrequentWords.frequentwordsWithDanishLettersCodedNew);
            tokens.retainAll(words);
            C3g = tokens.size() + " " + TextUtils.conjoin("#", tokens);
            log("Updating criteria C3g. Changed from '" + oldC3g + "' to '" + C3g + "'");
            res.C.put("C3g", C3g);
    	}
	    
    }	
}
