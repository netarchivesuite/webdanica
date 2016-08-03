package dk.kb.webdanica.datamodel.criteria;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dk.kb.webdanica.criteria.Words;
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
		
		File danicaHarvestLog = new File("/home/svc/devel/webdanica/toSVC/test_danica_urls.txt.harvestlog");
		File danicaHarvestLogReport = new File("/home/svc/devel/webdanica/toSVC/test_danica_urls.txt.harvestlog.report");
		File notdanicaHarvestLog = new File("/home/svc/devel/webdanica/toSVC/test_non_danica_urls.txt.harvestlog");
		File notdanicaHarvestLogReport = new File("/home/svc/devel/webdanica/toSVC/test_non_danica_urls.txt.harvestlog.report");
		
		File baseCriteriaDir = new File("/home/svc/devel/webdanica/toSVC/03-08-2016-1470237223/");
		List<Harvest> danicaharvests = Harvest.parseHarvestLog(danicaHarvestLog);
		Harvest.processHarvests(danicaharvests, baseCriteriaDir);
		List<Harvest> notdanicaharvests = Harvest.parseHarvestLog(notdanicaHarvestLog);
		Harvest.processHarvests(notdanicaharvests, baseCriteriaDir);
		Harvest.printToFile(danicaharvests, danicaHarvestLogReport);
		Harvest.printToFile(notdanicaharvests, notdanicaHarvestLogReport);
		/*
		for (Harvest h: harvests) {
			System.out.println("harvest of seed: " + h.seed);
			for (SingleCriteriaResult r: h.results) {
				System.out.println(r.getValuesInString("\n", ","));
			}
		}*/
		
	}
	

	/**
	 * 
	 * @param ingestFile
	 * @return
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static ProcessResult processFile(File ingestFile) throws IOException, SQLException {
		boolean checkDoublets = true;
		boolean listIgnored = true;
		return ingest(ingestFile, checkDoublets, listIgnored);
	}
	
	/**
	 * 
	 * @param ingestFile
	 * @param checkDoublets
	 * @param listIgnored
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws IOException
	 */
	public static ProcessResult ingest(File ingestFile, boolean checkDoublets, boolean listIgnored) throws IOException, SQLException {
		long linecount=0L;
		long skippedCount=0L;
		long ignoredCount=0L;
		Set<String> ignoredSet = new HashSet<String>();
		ProcessResult pr = new ProcessResult();
		List<SingleCriteriaResult> results = new ArrayList<SingleCriteriaResult>();
		//Check files

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

				SingleCriteriaResult res = new SingleCriteriaResult(trimmedLine, true);
				if (res.url == null || res.Cext1 == null || res.Cext3Orig == null || res.Cext3Orig.length() != 14) {
					log("Skipping line '" + trimmedLine 
							+ "': Missing one or more of fields url, Cext1, Cext3Orig");
					success = false;
				}
				/*
        	if (success && checkDoublets) {
        		doInsert = !MysqlRes.existsUrl(conn, res, tablename); //&& !existsUrl(conn, res, "ResFromHadoopNotDk") ;
        	}
				 */


				if (success && doInsert) {
					success = prepareLine(res, DataSource.NETARKIVET);
					log("Url '" + res.url + "' has danishCode: " +  res.calcDanishCode);
					// FIXME
					//success = success && MysqlRes.insertLine(conn, res, tablename);
					results.add(res);
				}
				linecount++;
				if (!doInsert) {
					if (listIgnored) {
						ignoredSet.add(res.url + ", " + res.Cext3);
					}
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
		for(String ignored: ignoredSet) {
			log(" - " + ignored);
		}
		
		pr.results = results;
		return pr; // replace with proper ProcessResult construction
		/*
		if (linecount==0) {
			System.out.println("WARNING: ingest file had no lines ingested: " + ingestFile.getAbsolutePath());
		}
		
		if (skippedCount==0) {
			return true;
		} else {
			return false;
		}*/
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
		// See MysqlX.getCalcDkCodeText for explanations
        
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
		if (!tld.isEmpty() && tld.length() < 6) {
			res.C15b = tld;
		} else {
			res.C15b = "-"; 
		}


		/*** calculate C8b if equal to 8a ***/
		if ((res.C8a != null)) { 
			if (res.C8b.equals(res.C8a)) {
				res.C8b = CriteriaUtils.find8bVal(res.url); 
			} 
		}
		
		/*** calculate C3e restricted o/oe,ae, aa in html ***/
		if (res.C3b != null) { //added 9/9
			res.C3e = CriteriaUtils.findNew3ValToken(res.C3b); 
		}

		/*** calculate C3f restricted o/oe,ae, aa in url ***/
		if (res.C3d != null) { //added 9/9
			res.C3f = CriteriaUtils.findNew3ValToken(res.C3d); 
		}

		/*** END: Update missing fields     ***/
		/**************************************/

		//res.calcDanishCode = 3
		if (res.C15b.equals("dk")){
			res.calcDanishCode = 3; // I think we should test in more depth
			return true;
		}

		if (res.C1a != null) {  
			CodesResult coderes = new CodesResult(); 
			coderes = CodesResult.setcodes_mail(res.C1a, res.C5a, res.C5b, res.C15b, "");
			if (coderes.calcDanishCode>0) {
				res.calcDanishCode = coderes.calcDanishCode;
				res.intDanish = coderes.intDanish;
				return true;
			}
		}

		//res.calcDanishCode =20-27, 40-47 - many dk indications 
		if (res.calcDanishCode==0 
				&& res.C15a!=null && res.C16a!=null && res.C17a!=null 
				&& res.Cext1>200 && res.C3a!=null && res.C4a!=null 
				&& res.C5a!=null && res.C5b!=null && res.C6a!=null) {
			CodesResult.setcodes_dkLanguageVeryLikely(res);
		}

		if (res.calcDanishCode==0 && (res.C3a!=null)) {  
			CodesResult coderes = CodesResult.setcodes_languageDklettersNew(res.C3a, res.C5a, res.C5b, res.C15b); 
			if (coderes.calcDanishCode>0) {
				res.calcDanishCode = coderes.calcDanishCode;
				res.intDanish = coderes.intDanish;
			}
		}

		//res.calcDanishCode = 76-77  likely dk language (not norwegain)
		if (res.calcDanishCode==0 && res.C4a!=null && res.C5a!=null) {  
			CodesResult coderes = new CodesResult(); 
			coderes = CodesResult.setcodes_languageDkNew(res.C4a, res.C5a, res.C5b, res.C15b);
			if (coderes.calcDanishCode>0) {
				res.calcDanishCode = coderes.calcDanishCode;
				res.intDanish = coderes.intDanish;
			}
		}

		//find tlf an +45 to 315, 316, 317");
		if (res.calcDanishCode==0 && res.C2a!=null) {  
			CodesResult cr = CodesResult.setcodes_oldPhone(res.C2a,res.C5a,res.C5b, res.C15b); 
			if (cr.calcDanishCode>0) {
				res.calcDanishCode = cr.calcDanishCode ;
				res.intDanish = cr.intDanish;
			}
		}

		//res.calcDanishCode =100-107 small sizes
		if (res.calcDanishCode==0 && res.Cext1<=200) { 
			CodesResult coderes = new CodesResult(); 
			coderes = CodesResult.setcodes_smallSize(res.C4a, res.C3a, res.C3b, res.C3c, res.C3d, res.C6a, res.C6b, res.C6c);
			if(coderes.calcDanishCode>0) {
				res.calcDanishCode = coderes.calcDanishCode;
				res.intDanish = coderes.intDanish;
			}
		}

		//res.calcDanishCode =10-12 asian/arabic languages	
		if (res.calcDanishCode==0 &&  res.C4a!=null ) {  
			CodesResult coderes = new CodesResult(); 
			coderes = CodesResult.setcodes_otherLanguagesChars(res.C4a);
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
	
	public static boolean updateHadoop(Connection conn, File ingestFile) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, IOException {
    	/** ingest file is a part-file with fields to be updated for item in table with tablename */
        boolean ok = true;
    	long linecount=0L;
        long skippedCount=0L;
        long updatedCount=0L;
        Set<String> ignoredSet = new HashSet<String>();

        //add fields if they don't exist already:
        /*
        for (String t : tablenameSet) {
			if (MysqlUpdateTables.hasNoNewHadoopFields(conn, t)) {
				System.out.println("adding fields to: " +  t);
				MysqlUpdateTables.addHadoopFields(conn, t);
			}
		} */

		//Check files
        if (!ingestFile.exists()) {
            System.err.println("ERROR: Cound not find ingest file " + ingestFile.getAbsolutePath());
            System.exit(1);
		}  
        
        System.out.println("--- Processing file: " + ingestFile.getAbsolutePath());
        //writeline(res_fo, "--- Processing file: " + ingestFile.getAbsolutePath());
        
        BufferedReader fr = new BufferedReader(new FileReader(ingestFile));        
        String line ="";
        
        String trimmedLine = null;
    
        //read file and ingest
        while ((line = fr.readLine()) != null) {
            trimmedLine = line.trim();
            if (!trimmedLine.isEmpty()) {
                boolean success = true;
                
                String actTablename = "";
                SingleCriteriaResult res = new SingleCriteriaResult(); // USED for ????
            	SingleCriteriaResult r = new SingleCriteriaResult(); // USED for ????
                
            	// Looks for the table, where the url is ingested originally amongst the Set<String> tableNameSet
            	Set<String> tableNameSet = new HashSet<String>(); // Empty set to make this compile
                for (String t: tableNameSet) {
                	if (actTablename.isEmpty()) {
	                    res = new SingleCriteriaResult(trimmedLine, false);
		            	r = SingleCriteriaResult.readUrl(conn, t, res, false);
		            	if (!r.url.isEmpty()) { // url is ingested in table t
		            		actTablename = t;
                		}
                	}
            	}
                
	            if (actTablename.isEmpty()) {  // url was not found amongst the tables in tableNameSet   
	            	log("WARNING: Url NOT FOUND: '" + r.url + "', " + r.Cext3 + " in line '" + trimmedLine + "l");	            	
                	//MysqlIngester.writeline(res_fo, "WARNING: Url NOT FOUND: '" + r.url + "', " + r.Cext3 + " in line '" + trimmedLine + "l");
                	success = false;
	            } else {
                	r.C2b = res.C2b;
                	r.C3g = res.C3g;
                	r.C6d = res.C6d;
                	r.C7g = res.C7g;
                	r.C7h = res.C7h;
                	r.C8c = res.C8c;
                	r.C9e = res.C9e;
                	r.C9f = res.C9f;
                	r.C10c = res.C10c;
            	}
            	res = r; // This is now a combination of the data read from the database table and the new criteria C2b,C3g,C6d, C7g, C7h, C8c, C9e, C9f and C10c
	                
                if (res.url == null || res.Cext3Orig == null || res.Cext3Orig.length() != 14) {
                	log("Skipping line '" + trimmedLine 
                            + "': Missing one or more of the fields url, Cext1, Cext3Orig");
                	//MysqlIngester.writeline(res_fo, "Skipping line '" + trimmedLine 
                    //        + "': Missing one or more of fields url, Cext1, Cext3Orig");
                	success = false;
                }
                
            	if (success) {
            		//update 3g
            		if (res.C3g!=null && (!res.C3g.isEmpty() && !res.C3g.startsWith("0"))) {
            			String oldC3g = res.C3g;
	                    Set<String> tokens = TextUtils.tokenizeText(res.C3g.substring(1).trim());
	                    List<String> words = Arrays.asList(Words.frequentwordsWithDanishLettersCodedNew);
	                    tokens.retainAll(words);
	                    res.C3g = tokens.size() + " " + TextUtils.conjoin("#", tokens);
	                    log("Updating criteria C3g. Changed from '" + oldC3g + "' to '" + res.C3g + "'");
	            	}

            		//update 8c foreninger
	            	if (res.C8a!=null && (!res.C8a.isEmpty() && !res.C8a.startsWith("0"))) {
	            		String oldC8c = res.C8c;
	            		res.C8c = CriteriaUtils.findC8cval(res.C8a, res.C8c);
	            		log("Updating criteria C8c. Changed from '" + oldC8c + "' to '" + res.C8c + "' using the C8a value '" + res.C8a + "'");
	            		
	            	}

	            	//update 9e firmaer on the basis on 
	            	if (res.C9b!=null && (!res.C9b.isEmpty() && !res.C9b.startsWith("0"))) {
	            		res.C9e = CriteriaUtils.findC9eval(res.C9b, res.C9e);
	            	}

            		//update 10c 
	            	if (res.C10c!=null && (!res.C10c.isEmpty() && !res.C10c.startsWith("0"))) {
	            		res.C10c = CriteriaUtils.findC10cval(res.C10c);
	            	}

	    		    Set<Integer> codeSet = Codes.getCodesForNOTDanishResults(); 
	    		    codeSet.addAll(Codes.getCodesForMaybees());
	    	        /*** calculate C2b phone numbers ***/
	    			if (res.calcDanishCode<=0 || codeSet.contains(res.calcDanishCode)) {
	    		    	CodesResult cr = CodesResult.setcodes_newPhone(res.C2b,res.C5a,res.C5b, res.C15b); 
	    				if (cr.calcDanishCode>0) {
	    					res.calcDanishCode = cr.calcDanishCode ;
	    					res.intDanish = cr.intDanish;
	    				}
    				}
	    			
	    			/** town names */
	    			if (res.calcDanishCode<=0 || codeSet.contains(res.calcDanishCode)) {
						if (!( (res.C7g==null) || (res.C7g.isEmpty()) || (res.C7g.startsWith("0")) )) {
							CodesResult cr = CodesResult.setcodes_mail(res.C1a, res.C5a, res.C5b, res.C15b, res.C7g);
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
                    
                    success = SingleCriteriaResult.updateHadoopLineSingleTable(conn, actTablename, res);
                    
    				if (!success) {
    					String message = "Skipping line '" + trimmedLine 
                                + "': Did not exist";
    					System.out.println(message);
                    	//MysqlIngester.writeline(res_fo, "Skipping line '" + trimmedLine 
                        //        + "': Did not exist");
                	}
            	}
            	if (success) {
					updatedCount++;
                    //System.out.println("--- Processed line: " + updatedCount + " in table: " + actTablename);
                    //writeline(res_fo, "--- Processed line: " + updatedCount + " in table: " + actTablename);
				} else {
					skippedCount++;
				}

            	linecount++;
			}
        }
        fr.close();
        log("Processed lines: " + linecount);
        log("Skipped lines: " + skippedCount);
        log("Updated lines: " + updatedCount);
       
        if (linecount==0) {
            System.out.println("WARNING: ingest file had no lines ingested: " + ingestFile.getAbsolutePath());
        }
        if (skippedCount>0) {
            System.out.println("WARNING: ingest file had skipped lines: " + ingestFile.getAbsolutePath());
        }
        
        ok = (skippedCount==0);
		return ok;
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
}

