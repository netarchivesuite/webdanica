package dk.kb.webdanica.datamodel.criteria;


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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dk.kb.webdanica.utils.TextUtils;
import dk.kb.webdanica.criteria.Words;

	/*
	String url = "jdbc:mysql://localhost/test|webdanica";
	Class.forName ("com.mysql.jdbc.Driver").newInstance ();
	Connection conn = DriverManager.getConnection (url, "username", "password");
	*/

	/*
	Update intDanish and calcDanishCode according to criteria data 
	*/

	public class IngestExtraCriteriaOne {
				
		private static Connection conn;
		private static File ingestFile; 
		private static IgnoreFile ignoreFile;
		
	//public class MysqlUpdateNewHadoop {
	    /**
	     * @param args <JDBC-URL> jdbcUser=<JDBC-USER> <filename-in dir-to-ingestupdate> withRead=true|false ignoreFile=true|false|warning";
	     * @throws ClassNotFoundException 
	     * @throws IllegalAccessException 
	     * @throws InstantiationException 
	     * @throws SQLException 
	     * @throws IOException 
	     */    
	    public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, IOException {
	    	/////////////////////////
	    	// arguments
	    	parseArgs(args);
	    	
	    	/************************************************************************************************
	    	 * updates according to arguments
	    	 ************************************************************************************************/
	        //HadoopResItem item = MysqlWorkFlow.readItemFromIngestFile(ingestFile,"", "");      
	    	/*
	    	if (item.hadoop_version.isEmpty()) {
	            System.err.println("ERROR: this is NOT a new hadoop update file, but an ingest file " + item.dataresfile.getAbsolutePath());
	            System.exit(1);
			} */

	    	updateHadoop(conn, ingestFile);
	    	/*	
	        if (item.dataresfile.exists()) {
	            String uHadoopUpdFilename = item.datasubdir.getAbsolutePath() + "/" + MysqlWorkFlow.wf_doneupdatenewHadoopfilename;
	            File uhad = new File(uHadoopUpdFilename); 
	    		if (uhad.exists()) {
	    			System.out.println("allready done: " + item.datasubdir.getAbsolutePath() );
	    		} else {
					if (ignoreFile.equals(IgnoreFile.if_false)) {
			            System.err.println("ERROR: result file allready existed " + item.dataresfile.getAbsolutePath());
			            System.exit(1);
					} else if (ignoreFile.equals(IgnoreFile.if_warning)) {
			            System.out.println("WARNING: result file allready existed " + item.dataresfile.getAbsolutePath());
					} 
			        item.dataresfile.createNewFile();
			    	FileWriter fw = new FileWriter(item.dataresfile.getAbsoluteFile());
			        BufferedWriter resfile = new BufferedWriter(fw);  
			        resfile.write("Running WebdanicaJobs - MysqlUpdateNewHadoop");
			        resfile.newLine();
			        resfile.close();
			        Set<String> tableSet = new HashSet<String>();
			        tableSet.add(item.tablename());
			        
				} 
			} */

	        conn.close();
	}
	 
	    
	    private static void parseArgs(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
	    	String errArgTxt = "Proper args: <JDBC-URL> jdbcUser=<JDBC-username> "
	    			+ "<filename-in dir-to-ingestupdate> "
	    			+ "withRead=true|false"
					+ "ignoreFile=true|false|warning";

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
	        
	        jdbcUser = CriteriaUtils.getStringSetting(jdbcUser).toLowerCase();
	        Class.forName ("com.mysql.jdbc.Driver").newInstance ();
	        String pw = "";
	        conn = DriverManager.getConnection (jdbcUrl, jdbcUser, pw);    

	        File ingestFile = new File(args[2]);
	        
	        if (!ingestFile.isFile()) {
	            System.err.println("The given ingestfile '" + ingestFile.getAbsolutePath() + "' is not a proper file or does not exist");
	            System.err.println(errArgTxt);
	            System.exit(1);
	        } 

	        //String withReadtxt = args[3];
	        //if (!withReadtxt .startsWith("withRead=")) {
	        //    System.err.println("Missing arg withRead setting");
	        //    System.err.println(errArgTxt);
	        //    System.exit(1);
	        //}
	        //boolean withRead = MysqlX.getBoleanSetting(withReadtxt);
	        
	        String ignoreFileTxt = args[3];
	        if (!ignoreFileTxt.startsWith("ignoreFile=")) {
	            System.err.println("Missing arg ignoreFile setting");
	            System.err.println(errArgTxt);
	            System.exit(1);
	        }
	        ignoreFileTxt = CriteriaUtils.getStringSetting(ignoreFileTxt);
	        ignoreFile = IgnoreFile.if_false;
	        if (ignoreFileTxt.equals("false"))  ignoreFile = IgnoreFile.if_false; 
	        else if (ignoreFileTxt.equals("true")) ignoreFile = IgnoreFile.if_true; 
	        else if (ignoreFileTxt.equals("warning")) ignoreFile = IgnoreFile.if_warning; 
	        else {
	            System.err.println("ERROR: Arg IgnoreFile setting is not valid - got '" + ignoreFileTxt + "'");
	            System.err.println(errArgTxt);
	            System.exit(1);
	        }
    }

		public static void writeline(FileOutputStream ftest, String txt) throws FileNotFoundException, IOException {
	        byte[] contentInBytes = txt.getBytes();
	        ftest.write(contentInBytes);
	        ftest.write("\n".getBytes());
	        ftest.flush();
	    }

	    public static boolean updateHadoop(Connection conn, File ingestFile) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, IOException {
	    	/** ingest file is a part-file with fields to be updated for item in table with tablename */
	        boolean ok = true;
	    	long linecount=0L;
	        long skippedCount=0L;
	        long updatedCount=0L;
	        Set<String> ignoredSet = new HashSet<String>();

	        //System.out.println("Must add fileds");
	        //System.out.println("tablenameSet.size: " + tablenameSet.size());
	        
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
	        /* Not necessary
	        if (!item.dataresfile.exists()) {
	            System.err.println("ERROR: Cound not find result file " + item.dataresfile.getAbsolutePath());
	            System.exit(1);
			}
	        FileOutputStream res_fo = new FileOutputStream(item.dataresfile, true);
	        */
	        
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
		            	System.out.println("WARNING: Url NOT FOUND: '" + r.url + "', " + r.Cext3 + " in line '" + trimmedLine + "l");	            	
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
	                	System.out.println("Skipping line '" + trimmedLine 
	                            + "': Missing one or more of the fields url, Cext1, Cext3Orig");
	                	//MysqlIngester.writeline(res_fo, "Skipping line '" + trimmedLine 
	                    //        + "': Missing one or more of fields url, Cext1, Cext3Orig");
	                	success = false;
	                }
	                
	            	if (success) {
	            		//update 3g
	            		if (res.C3g!=null && (!res.C3g.isEmpty() && !res.C3g.startsWith("0"))) {
		                    Set<String> tokens = TextUtils.tokenizeText(res.C3g.substring(1).trim());
		                    List<String> words = Arrays.asList(Words.frequentwordsWithDanishLettersCodedNew);
		                    tokens.retainAll(words);
		                    res.C3g = tokens.size() + " " + TextUtils.conjoin("#", tokens);
		            	}

	            		//update 8c foreninger
		            	if (res.C8a!=null && (!res.C8a.isEmpty() && !res.C8a.startsWith("0"))) {
		            		res.C8c = CriteriaUtils.findC8cval(res.C8a, res.C8c);
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

		    			//update bits
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
	        System.out.println("Processed lines: " + linecount);
	        System.out.println("Skipped lines: " + skippedCount);
	        System.out.println("Updated lines: " + updatedCount);
	        /*
	        for (String ignored: ignoredSet) {
	        	MysqlIngester.writeline(res_fo, " - " + ignored);
	        }
	        /*
	        MysqlIngester.writeline(res_fo, "Processed " + linecount + " lines");
	        MysqlIngester.writeline(res_fo, "Skipped " + skippedCount + " lines");
	        MysqlIngester.writeline(res_fo, "Updated " + updatedCount + " lines");
	        for (String ignored: ignoredSet) {
	        	MysqlIngester.writeline(res_fo, " - " + ignored);
	        }
	        */
	        
	        if (linecount==0) {
	            System.out.println("WARNING: ingest file had no lines ingested: " + ingestFile.getAbsolutePath());
	        }
	        if (skippedCount>0) {
	            System.out.println("WARNING: ingest file had skipped lines: " + ingestFile.getAbsolutePath());
	        }
	        
	        //res_fo.close();
	        ok = (skippedCount==0);
			return ok;
		}
	    
	 }

