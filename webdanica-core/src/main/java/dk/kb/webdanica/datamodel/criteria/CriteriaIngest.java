package dk.kb.webdanica.datamodel.criteria;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dk.kb.webdanica.criteria.Words;
import dk.kb.webdanica.datamodel.harvest.CassandraCriteriaResultsDAO;
import dk.kb.webdanica.interfaces.harvesting.HarvestError;
import dk.kb.webdanica.interfaces.harvesting.HarvestReport;
import dk.kb.webdanica.utils.StreamUtils;
import dk.kb.webdanica.utils.TextUtils;

/**
 * Ingest a file and calculate the probability of being Danish (the DanishCode).
 * @author svc
 */
public class CriteriaIngest {
	
	public static void main(String[] args) throws IOException, SQLException {
		//File f = new File("/home/test/criteria-results/03-08-2016-1470237223/68-55-20160803110922385-00000-dia-prod-udv-01.kb.dk.warc.gz/part-m-00000.gz");
		//System.out.println(isGzippedFile(f));
		
		// Read a harvestlog, and look for the associated criteria-results in the criteria-results folder. 
		// a parameter: get all, get the latest

		// TEST2: Nyt sample fra fredag d. 
		File basedir = new File("/home/svc/devel/webdanica/criteria-test-11-08-2016");
		File baseCriteriaDir = new File(basedir, "11-08-2016-1470934842");
		File HarvestLogTest1 = new File(basedir, "harvestlog-1470674884515.txt");
		File HarvestLogTest2 = new File(basedir, "test_danica_urls.txt.harvestlog");
		File HarvestLogTest3 = new File(basedir, "test_non_danica_urls.txt.harvestlog");
		
		//doTest(HarvestLogTest1, baseCriteriaDir, true);
		//doTest(HarvestLogTest2, baseCriteriaDir, true);
		//doTest(HarvestLogTest3, baseCriteriaDir, true);
		File basedir1 = new File("/home/svc/devel/webdanica/criteria-test-23-08-2016");
		File baseCriteriaDir1 = new File(basedir1, "23-08-2016-1471968184");
		File HarvestLogTest4 = new File(basedir1,"nl-urls-harvestlog.txt"); 
		ingest(HarvestLogTest4, baseCriteriaDir1,false);

		//runTest3();
		
		//runTest1();
		//runTest2();
		System.exit(0);
	}
	public static void ingest(File harvestLog, File baseCriteriaDir, boolean addToDatabase) throws IOException, SQLException {
		File basedir = harvestLog.getParentFile();
		String harvestLogReportName = harvestLog.getName() + ".report.txt";
		File harvestLogReport = findReportFile(basedir, harvestLogReportName);
		List<HarvestReport> danicaharvests = HarvestReport.readHarvestLog(harvestLog);
		List<HarvestError> errors = HarvestReport.processCriteriaResults(danicaharvests, baseCriteriaDir,addToDatabase);
		for (HarvestError e: errors) {
			System.out.println("Harvest of seed " + e.getReport().seed + " has errors: " + e.getError());
		}
		HarvestReport.printToFile(danicaharvests, harvestLogReport);
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

	private static void runTest3() throws IOException, SQLException{
		File basedir = new File("/home/svc/devel/webdanica/criteria-test-11-08-2016");
		String harvestLogName = "harvestlog-1470674884515.txt";
		File HarvestLog = new File(basedir, harvestLogName);
		File baseCriteriaDir = new File(basedir, "11-08-2016-1470934842");
		ingest(HarvestLog, baseCriteriaDir, false);
	}

	private static void runTest2() throws IOException, SQLException { 
			File basedir = new File("/home/svc/devel/webdanica/criteria-test-09-08-2016");
			File HarvestLog = new File(basedir, "harvestlog-1470674884515.txt");
			File baseCriteriaDir = new File(basedir, "09-08-2016-1470760002");
			ingest(HarvestLog, baseCriteriaDir,false);
    }

	private static void runTest1() throws IOException, SQLException {
		File danicaHarvestLog = new File("/home/svc/devel/webdanica/toSVC/test_danica_urls.txt.harvestlog");
		File notdanicaHarvestLog = new File("/home/svc/devel/webdanica/toSVC/test_non_danica_urls.txt.harvestlog");
		File baseCriteriaDir = new File("/home/svc/devel/webdanica/toSVC/03-08-2016-1470237223/");
		ingest(danicaHarvestLog, baseCriteriaDir, false);
		ingest(notdanicaHarvestLog, baseCriteriaDir, false);	
	}


	/**
	 * 
	 * @param ingestFile
	 * @param harvestName 
	 * @param seed 
	 * @return
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static ProcessResult processFile(File ingestFile, String seed, String harvestName, boolean addToDatabase) throws IOException, SQLException {
		boolean listIgnored = true;
		return process(ingestFile, seed, harvestName, addToDatabase);
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
	 */
	public static ProcessResult process(File ingestFile, String seed, String harvestName, 
				boolean addToDatabase) throws IOException {
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
		
		BufferedReader fr = StreamUtils.getBufferedReader(ingestFile);        
		String line = "";
		String trimmedLine = null;
		//read file and ingest
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
					success = prepareLine(res, DataSource.NETARKIVET);
					log("Url '" + res.url + "' has danishCode: " +  res.calcDanishCode);
					if (addToDatabase) {
						CassandraCriteriaResultsDAO dao = CassandraCriteriaResultsDAO.getInstance();
						boolean inserted = dao.insertRecord(res);
						if (!inserted) {
							log_error("Record not inserted");
						} else {
							insertedCount++;
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

		log("Processed " + linecount + " lines");
		log("Skipped " + skippedCount + " lines");
		log("Ignored " + ignoredCount + " lines");
		log("Inserted " + insertedCount + " records");
		for(String ignored: ignoredSet) {
			log(" - " + ignored);
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
	private static boolean prepareLine(SingleCriteriaResult res, DataSource source) {
		/*** set source ***/
		res.source = source;
        log("Set source to: " + source);
		/*** pre-calculate calcDanishCode and other fields ***/
		// See CalcDanishCode.getCalcDkCodeText for explanations
        
		res.calcDanishCode = 0;

		//res.calcDanishCode = 1  size=0 
		if (res.Cext1==0){ 
			res.calcDanishCode = 1;   //no text
			//TODO return false instead of true
			return true; // we stop now: as we believe the rest of the fields are empty 
		}

		/**************************************/
		/*** Update missing fields          ***/
		
		/*** calculate C15b                 ***/
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

		//res.calcDanishCode = 3
		if (res.C.get("C15b").equals("dk")){
			res.calcDanishCode = 3; // I think we should test in more depth
			return true;
		}

		if (res.C.get("C1a") != null) {  
			CodesResult coderes = new CodesResult(); 
			coderes = CodesResult.setcodes_mail(res.C.get("C1a"), res.C.get("C5a"), res.C.get("C5b"), 
					res.C.get("C15b"), ""); // FIXME this is not correct, is it?
			if (coderes.calcDanishCode>0) {
				res.calcDanishCode = coderes.calcDanishCode;
				res.intDanish = coderes.intDanish;
				return true;
			}
		}

		//res.calcDanishCode =20-27, 40-47 - many dk indications 
		if (res.calcDanishCode==0 
				&& res.C.get("C15a")!=null && res.C.get("C16a")!=null && res.C.get("C17a")!=null 
				&& res.Cext1>200 && res.C.get("C3a")!=null && res.C.get("C4a")!=null 
				&& res.C.get("C5a")!=null && res.C.get("C5b")!=null && res.C.get("C6a")!=null) {
			CodesResult.setcodes_dkLanguageVeryLikely(res);
		}

		if (res.calcDanishCode==0 && (res.C.get("C3a")!=null)) {  
			CodesResult coderes = CodesResult.setcodes_languageDklettersNew(res.C.get("C3a"), res.C.get("C5a"), 
					res.C.get("C5b"), res.C.get("C15b")); 
			if (coderes.calcDanishCode>0) {
				res.calcDanishCode = coderes.calcDanishCode;
				res.intDanish = coderes.intDanish;
			}
		}

		//res.calcDanishCode = 76-77  likely dk language (not norwegian)
		if (res.calcDanishCode==0 && res.C.get("C4a")!=null && res.C.get("C5a")!=null) {  
			CodesResult coderes = new CodesResult(); 
			coderes = CodesResult.setcodes_languageDkNew(res.C.get("C4a"), res.C.get("C5a"), res.C.get("C5b"), 
					res.C.get("C15b"));
			if (coderes.calcDanishCode>0) {
				res.calcDanishCode = coderes.calcDanishCode;
				res.intDanish = coderes.intDanish;
			}
		}

		//find tlf an +45 to 315, 316, 317");
		if (res.calcDanishCode==0 && res.C.get("C2a")!=null) {  
			CodesResult cr = CodesResult.setcodes_oldPhone(res.C.get("C2a"),res.C.get("C5a"),res.C.get("C5b"), 
					res.C.get("C15b")); 
			if (cr.calcDanishCode>0) {
				res.calcDanishCode = cr.calcDanishCode ;
				res.intDanish = cr.intDanish;
			}
		}

		//res.calcDanishCode =100-107 small sizes
		if (res.calcDanishCode==0 && res.Cext1<=200) { 
			CodesResult coderes = new CodesResult(); 
			coderes = CodesResult.setcodes_smallSize(res.C.get("C4a"), res.C.get("C3a"), res.C.get("C3b"), res.C.get("C3c"), 
					res.C.get("C3d"), res.C.get("C6a"), res.C.get("C6b"), res.C.get("C6c"));
			if(coderes.calcDanishCode>0) {
				res.calcDanishCode = coderes.calcDanishCode;
				res.intDanish = coderes.intDanish;
			}
		}

		//res.calcDanishCode =10-12 asian/arabic languages	
		if (res.calcDanishCode==0 &&  res.C.get("C4a")!=null ) {  
			CodesResult coderes = new CodesResult(); 
			coderes = CodesResult.setcodes_otherLanguagesChars(res.C.get("C4a"));
			if(coderes.calcDanishCode>0) {
				res.calcDanishCode = coderes.calcDanishCode;
				res.intDanish = coderes.intDanish;
			}
		}

		// See MysqlX.getCalcDkCodeText for explanations
		if (res.calcDanishCode==0) {
			CodesResult cr = CodesResult.setcodes_notDkLanguageVeryLikely(res); //get calcode and IntDanish  and check in depth
			if (cr.calcDanishCode>0) {
				res.calcDanishCode = cr.calcDanishCode;
				res.intDanish = cr.intDanish;
			}
		}

		//res.calcDanishCode = 2: double-char (Cext2 >= 200)
		if (res.calcDanishCode==0 && res.Cext2>=200) res.calcDanishCode = 2; //lots of doublechars
		//res.calcDanishCode = 220: double-char( 130 <= Cext2 < 200)
		if (res.calcDanishCode==0 && res.Cext2>=130) res.calcDanishCode = 220; //lots of doublechars

		///////////////////////////////////
		// set calcDanishCode-codes for which fields are set
		if (res.calcDanishCode==0) {
			res.calcDanishCode = CodesResult.findNegativBitmapCalcCode(res);
		}
		
		return true;
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
            List<String> words = Arrays.asList(Words.frequentwordsWithDanishLettersCodedNew);
            tokens.retainAll(words);
            C3g = tokens.size() + " " + TextUtils.conjoin("#", tokens);
            log("Updating criteria C3g. Changed from '" + oldC3g + "' to '" + C3g + "'");
            res.C.put("C3g", C3g);
    	}
	    
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
}
	
	
/*
	public static String ingest_turk_update_no = "0008"; //updated tyrk and arabic codes  after 30/8 (7/9)
	public static String ingest_arabic_update_no = "0009"; //updated tyrk and arabic codes  after 30/8 (7/9)
	public static String ingest_big0909_update_no = "0010"; //added code 32-35, 52-55, 200-203 and new calccodes (mediunint)
	public static String ingest_mail0110_update_no = "0011"; //added code 5-6, and bit 18
	public static String ingest_3efagain_update_no = "0012"; //added code bit 19
	public static String ingest_largeportionsupdate = "0013"; //
	public static String ingest_wrong6_dklan = "0014"; //correct 6 and set for dk language
	public static String ingest_wrong3efandbits = "0015"; //correct 6 and set for dk language
	public static String ingest_reset_unusable_codes = "0016"; //reset unused codes
	public static String ingest_new_codes = "0017"; //new codes
	public static String ingest_cleanup = "0018"; //cleanup
	public static String ingest_0s = "0019"; //set bits for 0's
	public static String ingest_0cleanups = "0020"; //set bits for 0's
	public static String ingest_correctNotDk = "0021"; //set bits for 0's
	public static String ingest_current_update_no = ingest_0cleanups; //ingest_reset_unusable_codes;
	*/ 


