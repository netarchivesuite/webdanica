package dk.kb.webdanica.oldtools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dk.kb.webdanica.criteria.Words;
import dk.kb.webdanica.oldtools.MysqlRes.CodesResult;
import dk.kb.webdanica.oldtools.MysqlWorkFlow.HadoopResItem;
import dk.kb.webdanica.oldtools.MysqlX.NotDkExceptions;
import dk.kb.webdanica.utils.TextUtils;

/*
String url = "jdbc:mysql://localhost/test|webdanica";
Class.forName ("com.mysql.jdbc.Driver").newInstance ();
Connection conn = DriverManager.getConnection (url, "username", "password");
*/

/*
Update intDanish and calcDanishCode according to criteria data 
*/

public class MysqlUpdateNewHadoopReUpdate {
    /**
     * @param args <JDBC-URL> jdbcUser=<JDBC-USER> datadir=<e.g. /data1/resultater/> deffile=<file with inf. of finished data>
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws SQLException 
     * @throws IOException 
     */
    
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, IOException {
    	/////////////////////////
    	// arguments

    	String errArgTxt = "Proper args: <JDBC-URL> jdbcUser=<JDBC-username> "
    					+ "datadir=<e.g. /data1/resultater/>  "
    	    			+ "deffile=<file with inf. of finished data> ";
    	if (args.length < 4) {
            System.err.println("Missing args!");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        if (args.length > 4) {
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

        /**** args - data-dir ****/
        String datadirTxt = args[2];
        if (!datadirTxt.startsWith("datadir=")) {
            System.err.println("Missing arg datadir setting - got " + datadirTxt);
            System.err.println(errArgTxt);
            System.exit(1);
        }
        datadirTxt = MysqlX.getStringSetting(datadirTxt);
    	//System.out.println("datadirTxt: " + datadirTxt);
        File dataDir = new File(datadirTxt);
        if (!dataDir.isDirectory()) {
            System.err.println("ERROR: The given data-dir '" + dataDir.getAbsolutePath() + "' is not a proper directory or does not exist");
            System.err.println(errArgTxt);
            System.exit(1);
        }

        /**** args - def-file ****/
        String defFileTxt = args[3];
        if (!defFileTxt.startsWith("deffile=")) {
            System.err.println("Missing arg defFile setting - got " + defFileTxt);
            System.err.println(errArgTxt);
            System.exit(1);
        }
        defFileTxt = MysqlX.getStringSetting(defFileTxt);
        File defFile = new File(defFileTxt);
        if (!defFile.isFile()) {
            System.err.println("ERROR: The given def-file '" + defFile.getAbsolutePath() + "' is not a proper file or does not exist");
            System.err.println(errArgTxt);
            System.exit(1);
        }

        /************************************************************************************************
    	 * Updates intDanish and calcDanishCode according to arguments/criteria data  
    	 ************************************************************************************************/
        Set<HadoopResItem> itemSet = new HashSet<HadoopResItem>();
        itemSet = MysqlWorkFlow.readItemsFromDefFile(defFile, dataDir.getAbsolutePath(), "");
        
        for (HadoopResItem item : itemSet) {
        	
	        if (!item.dataresfile.exists()) {
	            System.err.println("ERROR: this has not been ingested yet - thus cannot be updated file " + item.dataresfile.getAbsolutePath());
	            System.exit(1);
			}
	
	        if (item.hadoop_version.isEmpty()) {
	            System.err.println("ERROR: this is NOT a new hadoop update file, but an ingest file " + item.dataresfile.getAbsolutePath());
	            System.exit(1);
			}
	        
	        String uHadoopUpdFilename = item.datasubdir.getAbsolutePath() + "/" + MysqlWorkFlow.wf_doneupdatenewHadoopfilename + MysqlIngester.ingest_cleanup;
	        File uhad = new File(uHadoopUpdFilename); 
			if (uhad.exists()) {
				System.out.println("allready done: " + item.datasubdir.getAbsolutePath() );
			} else {
		    	File[] dirfiles = item.datasubdir.listFiles();
				List<File> sortedFiles = new ArrayList<File>(dirfiles.length);
		        for (File f : dirfiles) {
		        	sortedFiles.add(f);
		        }
		        Collections.sort(sortedFiles);
		        for (File f : sortedFiles) {
		        	if (MysqlX.isPartfile(f.getName())) {
		        	    updateHadoopnewfields(conn, f, item, item.tablename());
		        	} 
		        }
	        }
        }
        conn.close();
    }
    
    public static boolean updateHadoopnewfields(Connection conn, File ingestFile, HadoopResItem item, String tablename) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, IOException {
        boolean ok = true;
    	long linecount=0L;
        long skippedCount=0L;
        long updatedCount=0L;
        long ignoredCount = 0L;

        //add fields
		if (MysqlUpdateTables.hasNoNewHadoopFields(conn, item.tablename())) {
			System.out.println("updating fields to: " +  item.tablename());
			MysqlUpdateTables.addHadoopFields(conn, item.tablename());
		}

		//Check files
        if (!ingestFile.exists()) {
            System.err.println("ERROR: Cound not find ingest file " + ingestFile.getAbsolutePath());
            System.exit(1);
		}  
        
        System.out.println("--- Processing file: " + ingestFile.getAbsolutePath());
        
        BufferedReader fr = new BufferedReader(new FileReader(ingestFile));        
        String line ="";
        String trimmedLine = null;
    
        //read file and ingest
        while ((line = fr.readLine()) != null) {
            trimmedLine = line.trim();
            if (!trimmedLine.isEmpty()) {
                boolean success = true;
                
                MysqlRes.SingleCriteriaResult hr = new MysqlRes.SingleCriteriaResult(trimmedLine, false);
                if (hr.url == null || hr.Cext3Orig == null || hr.Cext3Orig.length() != 14) {
                	System.out.println("Skipping line '" + trimmedLine 
                            + "': Missing one or more of fields url, Cext1, Cext3Orig");
                	success = false;
                } 
                MysqlRes.SingleCriteriaResult dbr = MysqlRes.readUrl(conn, item.tablename(), hr, true);
            	if (dbr.url.isEmpty()) {
	            	System.out.println("WARNING: Url NOT FOUND: '" + dbr.url + "', " + dbr.Cext3 + " in line '" + trimmedLine );	            	
                	success = false;
            	} 
                
                boolean update = false;
            	if (success) {
            		//update 3g
	            	if (hr.C3g!=null && (!hr.C3g.isEmpty() && !hr.C3g.startsWith("0"))) {
	                    Set<String> tokens = TextUtils.tokenizeText(hr.C3g.substring(1).trim());
	                    List<String> words = Arrays.asList(Words.frequentwordsWithDanishLettersCodedNew);
	                    tokens.retainAll(words);
	                    hr.C3g = tokens.size() + " " + TextUtils.conjoin("#", tokens);
	                    if (dbr.C3g==null || (!dbr.C3g.equals(hr.C3g))) {
	                    	dbr.C3g = hr.C3g;
	                    	update = true;
	                    }
	            	}

            		//update 8c foreninger
	            	if (dbr.C8a!=null && (!dbr.C8a.isEmpty() && !dbr.C8a.startsWith("0"))) {
	            		String C8c = MysqlX.findC8cval(dbr.C8a, hr.C8c);
	                    if (dbr.C8c==null || (!dbr.C8c.equals(C8c))) {
	                    	dbr.C8c = C8c;
	                    	update = true;
	                    }
	            	}

	            	//update 9e firmaer
	            	if (dbr.C9b!=null && (!dbr.C9b.isEmpty() && !dbr.C9b.startsWith("0"))) {
	            		String C9e = MysqlX.findC9eval(dbr.C9b, hr.C9e);
	                    if (dbr.C9e==null || !dbr.C9e.equals(C9e)) {
	                    	dbr.C9e = C9e;
	                    	update = true;
	                    }
	            	}

            		//update 10c names
	            	if (hr.C10c!=null && (!hr.C10c.isEmpty() && !hr.C10c.startsWith("0"))) {
	            		String C10c = MysqlX.findC10cval(hr.C10c);
	                    if (dbr.C10c==null || !dbr.C10c.equals(C10c)) {
	                    	dbr.C10c = C10c;
	                    	update = true;
	                    }
	            	}

	    		    Set<Integer> codeSet = MysqlX.getCodesForNOTDanishResults(); 
	    	        /*** calculate C2b phone numbers ***/
	    			if (dbr.calcDanishCode<=0 || codeSet.contains(dbr.calcDanishCode)) {
	    		    	CodesResult cr = MysqlX.setcodes_newPhone(hr.C2b, dbr.C5a, dbr.C5b, dbr.C15b); 
	    				if (cr.calcDanishCode>0) {
	    					dbr.calcDanishCode = cr.calcDanishCode;
	    					dbr.intDanish = cr.intDanish;
	                    	update = true;
	    				}
    				}
	    			
	    			for (NotDkExceptions ex: NotDkExceptions.values()) {
		    			if (dbr.calcDanishCode<=0 || codeSet.contains(dbr.calcDanishCode) ) {
					    	CodesResult cr = MysqlX.setcodes_notDkLanguageVeryLikelyNewFields(dbr, ex); //get calcode and IntDanish  and check in depth
					    	if (cr.calcDanishCode>0) {
					    		dbr.calcDanishCode = cr.calcDanishCode;
					    		dbr.intDanish = cr.intDanish;
		                    	update = true;
				        	}
					    }
				    }

	    			//update bits
                    if (dbr.calcDanishCode <= 0) {
                    	int code = MysqlX.findNegativBitmapCalcCode(dbr);
	                    if (code!=dbr.calcDanishCode) {
	                    	dbr.calcDanishCode = code;
	                    	update = true;
	                    }
                    }

                    if (update) {
                    	Set<String> tset = new HashSet<String>();
                    	tset.add(tablename);
	                    success = MysqlRes.updateHadoopLines(conn, tset, dbr);
	    				if (!success) {
	    					System.out.println("Skipping line '" + trimmedLine 
	                                + "': Did not exist");
	    					skippedCount++;
	                	} else {
	    					updatedCount++;
	                	}
    				} else {
    					ignoredCount++;
    				}
            	}
				linecount++;
			}
        }
        fr.close();
        
        System.out.println("Processed " + linecount + " lines");
        System.out.println("Skipped " + skippedCount + " lines");
        System.out.println("Updated " + updatedCount + " lines");
        System.out.println("Ignored " + ignoredCount + " lines");
        if (linecount==0) {
            System.out.println("WARNING: ingest file had no lines ingested: " + ingestFile.getAbsolutePath());
        }
        if (skippedCount>0) {
            System.out.println("WARNING: ingest file had skipped lines: " + ingestFile.getAbsolutePath());
        }
        
        ok = (skippedCount==0);
		return ok;
	}
 }