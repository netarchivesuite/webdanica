package dk.kb.webdanica.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import dk.kb.webdanica.tools.MysqlRes.CodesResult;
import dk.kb.webdanica.tools.MysqlWorkFlow.HadoopResItem;
import dk.kb.webdanica.tools.MysqlWorkFlow.IgnoreFile;

/*
String url = "jdbc:mysql://localhost/test|webdanica";
Class.forName ("com.mysql.jdbc.Driver").newInstance ();
Connection conn = DriverManager.getConnection (url, "username", "password");
*/

/* for tables ResFromHadoop see MysqlCreateTable for definition
 * for calcDanish codes see MysqlLagkage
 */

public class MysqlIngester {

	/**
     * @param args <JDBC-URL> jdbcUser=<JDBC-username> dbmachine=<e.g. kb-test-webdania-001> table=<tablename> IADATA=false|true dir=<dir-to-ingest> <filename-in dir-to-ingest> checkDoublets=false|true listIgnored=false|true ignoreFile=true|false|warning
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws SQLException 
     * @throws IOException 
     */
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, IOException {
    	//////////////////////////
    	// Check and init parameters
    	
    	String errArgTxt = "Proper args: <JDBC-URL> jdbcUser=<JDBC-username> "
    			+ "dbmachine=<e.g. kb-test-webdania-001> "
    			+ "table=<tablename> IADATA=false|true "
    			+ "<filename-in dir-to-ingest> "
    			+ "checkDoublets=false|true listIgnored=false|true"
    			+ "ignoreFile=true|false|warning";
    	
        if (args.length < 9) {
            System.err.println("Missing args!");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        if (args.length > 9) {
            System.err.println("Too many args!");
            System.err.println(errArgTxt);
            System.exit(1);
        }

        String jdbcUrl = args[0];
        if (!jdbcUrl.startsWith("jdbc:mysql:")) {
            System.err.println("Missing arg jdbc setting starting with 'jdbc:mysql'");
            System.err.println(errArgTxt);
            System.exit(1);
        }

        String jdbcUser = args[1];
        if (!jdbcUser.startsWith("jdbcUser=")) {
            System.err.println("Missing arg jdbcUser setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        jdbcUser = MysqlX.getStringSetting(jdbcUser).toLowerCase();
        
        Class.forName ("com.mysql.jdbc.Driver").newInstance ();
        Connection conn = DriverManager.getConnection (jdbcUrl, jdbcUser, "");    

        /**** args - db-machine ****/
        String dbMachine = args[2];
        if (!dbMachine .startsWith("dbmachine=")) {
            System.err.println("Missing arg dbmachine setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        dbMachine = MysqlX.getStringSetting(dbMachine);

        String tablename = args[3];
        if (!tablename.startsWith("table=")) {
            System.err.println("Missing arg table setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        tablename = MysqlX.getStringSetting(tablename);

        String isIADATAtxt = args[4];
        if (!isIADATAtxt .startsWith("IADATA=")) {
            System.err.println("Missing arg IADATA setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        boolean isIADATA = MysqlX.getBoleanSetting(isIADATAtxt);
        
        File ingestFile = new File(args[5]);
        if (!MysqlX.isPartfile(ingestFile.getName())) {
        	System.err.println("FILE " + ingestFile.getName());
        	System.err.println("File not ingested: " + ingestFile.getName());
            System.exit(1);
        }
        if (!ingestFile.isFile()) {
            System.err.println("The given ingestfile '" + ingestFile.getAbsolutePath() + "' is not a proper file or does not exist");
            System.err.println(errArgTxt);
            System.exit(1);
        } 
        
        String checkDoubletsTxt = args[6];
        if (!checkDoubletsTxt.startsWith("checkDoublets=")) {
            System.err.println("Missing arg checkDoublets setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        boolean checkDoublets = MysqlX.getBoleanSetting(checkDoubletsTxt);
        //System.out.println("CheckDoublets: " + checkDoublets);

        String listIgnoredTxt = args[7];
        if (!listIgnoredTxt.startsWith("listIgnored=")) {
            System.err.println("Missing arg listIgnored setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        boolean listIgnored = MysqlX.getBoleanSetting(listIgnoredTxt);

        String ignoreFileTxt = args[8];
        if (!ignoreFileTxt.startsWith("ignoreFile=")) {
            System.err.println("Missing arg ignoreFile setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        ignoreFileTxt = MysqlX.getStringSetting(ignoreFileTxt);
        IgnoreFile ignoreFile = IgnoreFile.if_false;
        if (ignoreFileTxt.equals("false"))  ignoreFile = IgnoreFile.if_false; 
        else if (ignoreFileTxt.equals("true")) ignoreFile = IgnoreFile.if_true; 
        else if (ignoreFileTxt.equals("warning")) ignoreFile = IgnoreFile.if_warning; 
        else {
            System.err.println("ERROR: Arg IgnoreFile setting is not valid - got '" + ignoreFileTxt + "'");
            System.err.println(errArgTxt);
            System.exit(1);
        }

        //////////////////////////
    	// Start processing
        
        HadoopResItem hres =  MysqlWorkFlow.readItemFromIngestFile(ingestFile, dbMachine, "");
        if (!hres.hadoop_version.isEmpty()) {
            System.err.println("ERROR: this is NOT an ingest file, but a new hadoop update file " + hres.dataresfile.getAbsolutePath());
            System.exit(1);
		}
        
        //String resultFilename = resultDir + "/" + hres.resfilename();
        //File resultFile = new File(resultFilename);
        if (hres.dataresfile.exists()) {
			if (ignoreFile.equals(IgnoreFile.if_false)) {
	            System.err.println("ERROR: result file allready existed " + hres.dataresfile.getAbsolutePath());
	            System.exit(1);
			} else if (ignoreFile.equals(IgnoreFile.if_warning)) {
	            System.out.println("WARNING: result file allready existed " + hres.dataresfile.getAbsolutePath());
			} 
		} 
        hres.dataresfile.createNewFile();
    	FileWriter fw = new FileWriter(hres.dataresfile.getAbsoluteFile());
        BufferedWriter resfile = new BufferedWriter(fw);  
        resfile.write("Running WebdanicaJobs - MysqlIngester");
        resfile.newLine();
        resfile.close();
        

        ingest(conn, ingestFile, hres, tablename, isIADATA, checkDoublets, listIgnored);

        conn.close();
    }
    
    public static void writeline(FileOutputStream ftest, String txt) throws FileNotFoundException, IOException {
        byte[] contentInBytes = txt.getBytes();
        ftest.write(contentInBytes);
        ftest.write("\n".getBytes());
        ftest.flush();
    }

    public static boolean ingest(Connection conn, File ingestFile, HadoopResItem item, String tablename, boolean isIADATA, boolean checkDoublets, boolean listIgnored) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, IOException {
        long linecount=0L;
        long skippedCount=0L;
        long ignoredCount=0L;
        Set<String> ignoredSet = new HashSet<String>();
        
        //Check files

        if (!ingestFile.exists()) {
            System.err.println("ERROR: Cound not find ingest file " + ingestFile.getAbsolutePath());
            System.exit(1);
		}  
        if (!item.dataresfile.exists()) {
            System.err.println("ERROR: Cound not find result file " + item.dataresfile.getAbsolutePath());
            System.exit(1);
		}
        FileOutputStream res_fo = new FileOutputStream(item.dataresfile, true);
        
        //resultFile - item.dataresfile   
    	//FileWriter fw = new FileWriter(resultFile.getAbsoluteFile());
        //BufferedWriter resfile = new BufferedWriter(fw);  
        System.out.println("--- Processing file: " + ingestFile.getAbsolutePath());
        writeline(res_fo, "--- Processing file: " + ingestFile.getAbsolutePath());
        
        BufferedReader fr = new BufferedReader(new FileReader(ingestFile));        
        String line ="";
        
        String trimmedLine = null;
        
        //read file and ingest
        while ((line = fr.readLine()) != null) {
            trimmedLine = line.trim();
            if (!trimmedLine.isEmpty()) {
                boolean doInsert = true;   
                boolean success = true;
                
                MysqlRes.SingleCriteriaResult res = new MysqlRes.SingleCriteriaResult(trimmedLine, true);
                if (res.url == null || res.Cext1 == null || res.Cext3Orig == null || res.Cext3Orig.length() != 14) {
                	writeline(res_fo, "Skipping line '" + trimmedLine 
                            + "': Missing one or more of fields url, Cext1, Cext3Orig");
                	success = false;
                }
                
            	if (success && checkDoublets) {
            		doInsert = !MysqlRes.existsUrl(conn, res, tablename); //&& !existsUrl(conn, res, "ResFromHadoopNotDk") ;
            	}
            	if (success && doInsert) {
                    success = prepareLine( res, isIADATA );   
                	success = success && MysqlRes.insertLine(conn, res, tablename);
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
        
        writeline(res_fo, "Processed " + linecount + " lines");
        writeline(res_fo, "Skipped " + skippedCount + " lines");
        writeline(res_fo, "Ignored " + ignoredCount + " lines");
        for (String ignored: ignoredSet) {
        	writeline(res_fo, " - " + ignored);
        }
        if (linecount==0) {
            System.out.println("WARNING: ingest file had no lines ingested: " + ingestFile.getAbsolutePath());
        }
        
        res_fo.close();
        if (skippedCount==0) {
            return true;
        } else {
            return false;
        }
    }
    
    private static boolean prepareLine(MysqlRes.SingleCriteriaResult res, boolean isIAdata	) throws SQLException {
    	/*** set IA source ***/
	    res.IsIASource = isIAdata;

	    /*** pre-calculate calcDanishCode and other fields ***/
	    // See MysqlX.getCalcDkCodeText for explanations

	    res.calcDanishCode = 0;

	    //res.calcDanishCode = 1  size=0 
	    if (res.Cext1==0) res.calcDanishCode = 1;   //no text

	    /**************************************/
	    /*** Update missing fields          ***/
		
	    /*** calculate C15b ***/
        if (res.calcDanishCode==0) { //calcDanishCode!=0 means the rest of the fields are empty
	    	String tld = MysqlX.findTLD(res.url);
			if (!tld.isEmpty() && tld.length() < 6) {
				res.C15b = tld;
			} else {
				res.C15b = "-"; 
			}
        }
        
        /*** calculate C8b if equal to 8a ***/
	    if (res.calcDanishCode==0 && !(res.C8a == null)) { //calcDanishCode!=0 means the rest of the fields are empty
	    	if (res.C8b.equals(res.C8a)) {
		        res.C8b = MysqlX.find8bVal(res.url); 
	    	} 
	    }
	    
	    /*** calculate C16a ***/
	    /* only  via update since it is time consuming to open database
	     * if (res.calcDanishCode==0 ) {
	    	res.C16a = "" + MysqlX.find16aVal(res.C16a, res.urlOrig, linksBase); 
	    }
	     */

        /*** calculate C3e restricted o/oe,ae, aa in html ***/
	    if (res.calcDanishCode==0 && !(res.C3b == null)) { //added 9/9
		    res.C3e = MysqlX.findNew3ValToken(res.C3b); 
	    }

        /*** calculate C3f restricted o/oe,ae, aa in url ***/
	    if (res.calcDanishCode==0 && !(res.C3d == null)) { //added 9/9
		    res.C3f = MysqlX.findNew3ValToken(res.C3d); 
	    }

	    /*** END: Update missing fields     ***/
	    /**************************************/

	    //res.calcDanishCode = 3
	    if (res.calcDanishCode==0 && res.C15b.equals("dk")){
	        res.calcDanishCode = 3;
		}

		if (res.calcDanishCode==0 && res.C1a!=null ) {  
        	MysqlRes.CodesResult coderes = new MysqlRes.CodesResult(); 
        	coderes = MysqlX.setcodes_mail(res.C1a, res.C5a, res.C5b, res.C15b, "");
        	if (coderes.calcDanishCode>0) {
	        	res.calcDanishCode = coderes.calcDanishCode;
	            res.intDanish = coderes.intDanish;
        	}
		}
        
		//res.calcDanishCode =20-27, 40-47 - many dk indications 
        if (res.calcDanishCode==0 
        		&& res.C15a!=null && res.C16a!=null && res.C17a!=null 
        		&& res.Cext1>200 && res.C3a!=null && res.C4a!=null 
        		&& res.C5a!=null && res.C5b!=null && res.C6a!=null) {
        	MysqlX.setcodes_dkLanguageVeryLikely(res);
        }

        if (res.calcDanishCode==0 && (res.C3a!=null)) {  
	    	MysqlRes.CodesResult coderes = MysqlX.setcodes_languageDklettersNew(res.C3a, res.C5a, res.C5b, res.C15b); 
	    	if (coderes.calcDanishCode>0) {
		    	res.calcDanishCode = coderes.calcDanishCode;
		        res.intDanish = coderes.intDanish;
			}
		}
		
    	//res.calcDanishCode = 76-77  likely dk language (not norwegain)
        if (res.calcDanishCode==0 && res.C4a!=null && res.C5a!=null) {  
	    	MysqlRes.CodesResult coderes = new MysqlRes.CodesResult(); 
	    	coderes = MysqlX.setcodes_languageDkNew(res.C4a, res.C5a, res.C5b, res.C15b);
	    	if (coderes.calcDanishCode>0) {
		    	res.calcDanishCode = coderes.calcDanishCode;
		        res.intDanish = coderes.intDanish;
			}
		}

        //find tlf an +45 to 315, 316, 317");
        if (res.calcDanishCode==0 && res.C2a!=null) {  
        	CodesResult cr = MysqlX.setcodes_oldPhone(res.C2a,res.C5a,res.C5b, res.C15b); 
			if (cr.calcDanishCode>0) {
				res.calcDanishCode = cr.calcDanishCode ;
				res.intDanish = cr.intDanish;
			}
        }

        //res.calcDanishCode =100-107 small sizes
		if (res.calcDanishCode==0 && res.Cext1<=200) { 
			MysqlRes.CodesResult coderes = new MysqlRes.CodesResult(); 
        	coderes = MysqlX.setcodes_smallSize(res.C4a, res.C3a, res.C3b, res.C3c, res.C3d, res.C6a, res.C6b, res.C6c);
        	if(coderes.calcDanishCode>0) {
	        	res.calcDanishCode = coderes.calcDanishCode;
	            res.intDanish = coderes.intDanish;
        	}
		}
	    
	    //res.calcDanishCode =10-12 asian/arabic languages	
        if (res.calcDanishCode==0 &&  res.C4a!=null ) {  
        	MysqlRes.CodesResult coderes = new MysqlRes.CodesResult(); 
        	coderes = MysqlX.setcodes_otherLanguagesChars(res.C4a);
        	if(coderes.calcDanishCode>0) {
	        	res.calcDanishCode = coderes.calcDanishCode;
	            res.intDanish = coderes.intDanish;
        	}
		}
        
	    // See MysqlX.getCalcDkCodeText for explanations
        if (res.calcDanishCode==0) {
        	CodesResult cr = MysqlX.setcodes_notDkLanguageVeryLikely(res); //get calcode and IntDanish  and check in depth
        	if (cr.calcDanishCode>0) {
        		res.calcDanishCode = cr.calcDanishCode;
        		res.intDanish = cr.intDanish;
        	}
        }

	    //res.calcDanishCode = 2 double-char >= 200
		if (res.calcDanishCode==0 && res.Cext2>=200) res.calcDanishCode = 2; //lots of doublechars
		if (res.calcDanishCode==0 && res.Cext2>=130) res.calcDanishCode = 220; //lots of doublechars
		
        ///////////////////////////////////
        // set calcDanishCode-codes for which fields are set
        if (res.calcDanishCode==0) {
        	res.calcDanishCode = MysqlX.findNegativBitmapCalcCode(res);
        }

        return true;
    }
    
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
}
