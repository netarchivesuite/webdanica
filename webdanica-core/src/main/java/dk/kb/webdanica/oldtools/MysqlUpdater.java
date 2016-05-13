package dk.kb.webdanica.oldtools;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

import dk.kb.webdanica.criteria.LinksBase;
import dk.kb.webdanica.oldtools.MysqlRes.*;
import dk.kb.webdanica.oldtools.MysqlWorkFlow.HadoopResItem;
import dk.kb.webdanica.oldtools.MysqlX.CodesSizeIntervals;
import dk.kb.webdanica.oldtools.MysqlX.Interval;
import dk.kb.webdanica.oldtools.MysqlX.NotDkExceptions;

/*
String url = "jdbc:mysql://localhost/test|webdanica";
Class.forName ("com.mysql.jdbc.Driver").newInstance ();
Connection conn = DriverManager.getConnection (url, "username", "password");
*/

/*
Update intDanish and calcDanishCode according to criteria data 
*/

public class MysqlUpdater {
    /**
     * @param args <JDBC-URL> jdbcUser=<JDBC-USER> berDb=<berkeley db dir> machine=<test1...> datadir=<e.g. /data1/resultater/> table=<tablename>|all updateOperation=correct_NOTdk|update10|update11|update12|update13|update14|update15|update16|update17|update18|update19|update20|updateBerdb|updateHadoop|Turkey|Arabic|C15bUpdate|c8bCalc|setbitmap|calcDkcode|codeInterval|codeAll <calccode or 0> (setbitmap means set to negative)";
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws SQLException 
     * @throws IOException 
     */

    enum UpdateOperation {
    	c15bCalc,
    	c8bCalc,
    	setbitmap,
    	calcDkcode,
    	calcDkcodeInterval,
    	calcDkcodeAll,
    	turkey,
    	arabic,
    	updateBerdb,
    	update10, 
    	update11,
    	update12,
    	update13,
    	update14,
    	update15,
    	update16,
    	update17,
    	update18,
    	update19,
    	update20,
    	correct_NOTdk,
    	noCalc
    }

    private static LinksBase linksBase = null;
    
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, IOException {
    	/////////////////////////
    	// arguments
    	
    	String errArgTxt = "Proper args: <JDBC-URL> jdbcUser=<JDBC-USER>"
    			+ "berDb=<berkeley db dir>" //"berDb=/home/test/db-1"
    			+ "machine=<test1...>"
    			+ "datadir=<e.g. /data1/resultater/> "
    			+ "table=<tablename>|all "
    			+ "updateOperation=correct_NOTdk|update10|update11|update12|update13|update14|update15|update16|update17|update18|update19|update20|updateBerdb|updateHadoop|Turkey|Arabic|C15bUpdate|C8bCalc|setbitmap|calcDkcode|codeInterval|codeAll "
    			+ "<calccode or 0> (setbitmap means set to negative)";

    	if (args.length < 7) {
            System.err.println("Missing args!");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        if (args.length > 8) {
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

    	String berDbTxt = args[2];
        if (!berDbTxt.startsWith("berDb=")) {
            System.err.println("Missing arg berDb setting got: " + berDbTxt);
            System.err.println(errArgTxt);
            System.exit(1);
        }
    	if (berDbTxt.startsWith("berDb=")) {
    		berDbTxt = MysqlX.getStringSetting(berDbTxt);
    	}
        File linksBaseDir = new File(berDbTxt);

    	/**** args - machine ****/
        String machineTxt = args[3];
        if (!machineTxt.startsWith("machine=")) {
            System.err.println("Missing arg machine=<test1...> setting - got " + machineTxt);
            System.err.println(errArgTxt);
            System.exit(1);
        }
        String machine = MysqlX.getStringSetting(machineTxt);
        
        
    	/**** args - data-dir ****/
        String datadirTxt = args[4];
        if (!datadirTxt.startsWith("datadir=")) {
            System.err.println("Missing arg datadir setting - got " + datadirTxt);
            System.err.println(errArgTxt);
            System.exit(1);
        }
        datadirTxt = MysqlX.getStringSetting(datadirTxt);
    	//System.out.println("datadirTxt: " + datadirTxt);

        /**** args - table ****/
        String table = args[5];
        if (!table.startsWith("table=")) {
            System.err.println("Missing arg table setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        table = MysqlX.getStringSetting(table);
        if (table.isEmpty()) {
            System.err.println("Missing tablename");
            System.err.println(errArgTxt);
            System.exit(1);
        }
    	
        int calcDanishCode = 0;        
        UpdateOperation updateOp = UpdateOperation.noCalc;
    	String opTxt = args[6];
        if (!opTxt.startsWith("updateOperation=")) {
            System.err.println("Missing arg updateOperation setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
    	if (opTxt.startsWith("updateOperation=")) {
            opTxt = MysqlX.getStringSetting(opTxt);
        }  
    	if (opTxt.startsWith("C15bUpdate")) {
    		updateOp = UpdateOperation.c15bCalc;        	
        } else if (opTxt.startsWith("C8bCalc")) {
        	updateOp = UpdateOperation.c8bCalc;      
        } else if (opTxt.startsWith("Turkey")) {
        	updateOp = UpdateOperation.turkey;      
        } else if (opTxt.startsWith("Arabic")) {
        	updateOp = UpdateOperation.arabic;      
        } else if (opTxt.startsWith("setbitmap")) {
        	updateOp = UpdateOperation.setbitmap; 
        	calcDanishCode = 0; 
        } else if (opTxt.startsWith("calcDkcode")) {
        	updateOp = UpdateOperation.calcDkcode; 
        } else if (opTxt.startsWith("codeInterval")) {
        	updateOp = UpdateOperation.calcDkcodeInterval;
        } else if (opTxt.startsWith("update10")) {
        	updateOp = UpdateOperation.update10;
        } else if (opTxt.startsWith("update11")) {
        	updateOp = UpdateOperation.update11;
        } else if (opTxt.startsWith("update12")) {
        	updateOp = UpdateOperation.update12;
        } else if (opTxt.startsWith("update13")) {
        	updateOp = UpdateOperation.update13;
        } else if (opTxt.startsWith("update14")) {
        	updateOp = UpdateOperation.update14;
        } else if (opTxt.startsWith("update15")) {
        	updateOp = UpdateOperation.update15;
        } else if (opTxt.startsWith("update16")) {
        	updateOp = UpdateOperation.update16;
        } else if (opTxt.startsWith("update17")) {
        	updateOp = UpdateOperation.update17;
        } else if (opTxt.startsWith("update18")) {
        	updateOp = UpdateOperation.update18;
        } else if (opTxt.startsWith("update19")) {
        	updateOp = UpdateOperation.update19;
        } else if (opTxt.startsWith("update20")) {
        	updateOp = UpdateOperation.update20;
        } else if (opTxt.startsWith("correct_NOTdk")) {
        	updateOp = UpdateOperation.correct_NOTdk;
        } else if (opTxt.startsWith("updateBerdb")) {
        	updateOp = UpdateOperation.updateBerdb;
        } else if (opTxt.startsWith("codeAll")) {
        	updateOp = UpdateOperation.calcDkcodeAll; 
        } else {
            System.err.println("Arg updateOperation setting is NOT valid - got '" + opTxt + "'");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        
    	if (args.length>7) { 
	    	String codeTxt = args[7];
    		if (updateOp == UpdateOperation.calcDkcode || updateOp == UpdateOperation.calcDkcodeInterval) { 
            	if (codeTxt.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+")) {
            		calcDanishCode = Integer.parseInt(codeTxt);
                    if (!(calcDanishCode==1  
                    	|| calcDanishCode==2  
                        ||calcDanishCode==3
                    	|| calcDanishCode==10 
                    	|| calcDanishCode==11 
                        || (calcDanishCode>=200 && calcDanishCode<=203)  
                    	|| (calcDanishCode>=100 && calcDanishCode<=107)  
                    	|| (calcDanishCode>=20 && calcDanishCode<=27)  
                    	|| (calcDanishCode>=40 && calcDanishCode<=47)
    	          		|| (calcDanishCode>=30 && calcDanishCode<=35) 
    	          		|| (calcDanishCode>=50 && calcDanishCode<=55) ) ) 
                    {//Unknown calcDanishCode
    		            System.err.println("Unknown calcDanishCode =" + calcDanishCode);
    		            System.err.println(errArgTxt);
    		            System.exit(1);
    		    	}
            	} else {
                    System.err.println("Arg calcDanishCode did not contain integer '" + codeTxt + "'");
                    System.err.println(errArgTxt);
                    System.exit(1);
            	}
        	} else {
                System.err.println("Arg calcDanishCode was not expected for operation '" + opTxt + "'");
                System.err.println(errArgTxt);
                System.exit(1);
        	}
    	}
    	
    	if (updateOp.equals(UpdateOperation.updateBerdb)) {
            // Initializing LinksBase for lookup in berkeley DB
            // dir probably = /home/test/db-1
            if (!linksBaseDir.isDirectory()) {
                System.err.println("Given argument '" + berDbTxt + "' is not a valid directory");
                System.exit(1);
            }
            try {
                linksBase = new LinksBase(linksBaseDir);
            } catch (Exception e) {
                throw new IOException("Unable to initialize linksBase with DB directory '" + linksBaseDir.getAbsolutePath() + "'", e);
            }
    	}

    	/************************************************************************************************
    	 * updates according to arguments
    	 ************************************************************************************************/
    	
        // Set of tables to extract lagkage from
        Set<String> tableSet = new HashSet<String>();
        if (table.equals("all")) {
	    	tableSet = MysqlRes.getTables(conn);
        } else {
        	tableSet.add( table.trim() );
        }
        
        int limit = 100000;
        if (machine.equals("test1")) {
        	limit = 50000;
        }
        if ( machine.equals("test5") || machine.equals("test6") || machine.equals("test2")) {
        	limit = 30000;
        }
        
    	for (String tablename : tableSet) {
            System.out.println("proc table: '" + table + "'");
    		if (tablename.startsWith(MysqlRes.wf_table_prefix) ) { // only new ResHadoop_
    	        System.out.println("proc tablename: '" + tablename + "'");
    	
		        HadoopResItem item =  MysqlWorkFlow.readItemFromTablename(tablename, datadirTxt, "", "");
	        	Set<Integer> codeSet = new HashSet<Integer>();
		        
    	        Set<String> fSet = MysqlRes.getFields(conn, tablename);
	            boolean extendedNewHadoopTable = fSet.contains("C2b");

	            switch (updateOp) {
		          case setbitmap:   
			    	setNegativeCalcDanishCode(conn, tablename, calcDanishCode, true);
			    	break;
		          case calcDkcode:
		          case calcDkcodeInterval:
		          case calcDkcodeAll:
		          	String sql = "";
		          	String whereStmt = "";
			    	if (calcDanishCode==0 && updateOp!=UpdateOperation.calcDkcodeAll) { 
			            System.err.println("Unknown calcDanishCode =" + calcDanishCode + " -- do you mean operation=setbitmap?");
			            System.err.println(errArgTxt);
			            System.exit(1);
		          	}
		  			sql = "SELECT * FROM " + tablename;
		  			whereStmt = "(calcDanishCode<=0)";
		
		  			if (updateOp!=UpdateOperation.calcDkcodeAll) {
				    	if (calcDanishCode==1) { //zeroSize
				    		whereStmt = whereStmt + (whereStmt.isEmpty() ? "" : " AND ") + "extSize=0";
				    	} else if (calcDanishCode==2)  { //doubleBytes - bytes per char > 2 
				          	whereStmt = whereStmt + (whereStmt.isEmpty() ? "" : " AND ") + "extDblChar>=200";
				    	} else if (calcDanishCode==3)  { //tld=dk
				    		whereStmt = whereStmt + (whereStmt.isEmpty() ? "" : " AND ") + "C15b='dk'";
				    	} else if (calcDanishCode>=10 && calcDanishCode<=12)  { // languages codes from //http://www.loc.gov/standards/iso639-2/php/code_list.php
				    		whereStmt = whereStmt + (whereStmt.isEmpty() ? "" : " AND ") + " NOT C4a is NULL"; 
				    	} else if ((calcDanishCode>=20 && calcDanishCode<=27) || (calcDanishCode>=40 && calcDanishCode<=47)) { //dkLanguageVeryLikely 
				    		whereStmt = whereStmt + (whereStmt.isEmpty() ? "" : " AND ") + "extSize>200" //only consider when there are lots of text for n-gram --- CAN BE for different codes 
							+ " AND length(C3a)>2"   //includes æ,ø or å
							+ " AND C4a='da'"		 //n-gram points at Danish language
							+ " AND length(C5a)>2"   //includes typical and distingisable Danish words
							+ " AND length(C5b)<=2"  //do not include typical Norwegain words
							+ " AND length(C6a)>2";  //includes frequently used Danish words
							//+ " C15a, C16a, C17a depends on parameters
				    	} else if ((calcDanishCode>=30 && calcDanishCode<=31)   || (calcDanishCode>=50 && calcDanishCode<=51)) {
				    		whereStmt = whereStmt + (whereStmt.isEmpty() ? "" : " AND ") + "extSize>200"  //only consider when there are lots of text for n-gram
							+ " AND (C1a is NULL OR length(C1a)<=2)" //Do NOT include country’s TLD email address 
							+ " AND (C2a is NULL OR length(C2a)<=2)" //Do NOT include national phone number 
							+ " AND (C3a is NULL OR length(C3a)<=2)" //Do NOT include æ,ø or å
							+ " AND (C3b is NULL OR length(C3b)<=2)" //Do NOT include frequently used Danish words with coded æ, ø, å on form ae, oe/o, aa 
							+ " AND (C3c is NULL OR length(C3c)<=2)" //Do NOT include same as C3a, but on the URL in uft8 URL encoding
							+ " AND (C4a is NULL OR (C4a<>'da' AND C4a<>'no' AND C4a<>'sv'))"		//n-gram does NOT points at Danish language
							+ " AND (C5a is NULL OR length(C5a)<=2)" //do NOT includes typical and distingisable Danish words
							+ " AND (C6a is NULL OR length(C6a)<=2)" // NOT top 150 frequently used Danish words, top 250 verbs, top 250 adjectives, top 250 nouns. All 4 lists from Society for Danish Language and Literature (http://dsl.dk/)
							+ " AND (C6b is NULL OR length(C6b)<=2)" // NOT typical Danish words like 'dansk', 'Danmark' and 'forening'
							+ " AND (C6c is NULL OR length(C6c)<=2)" // NOT same as C6b, but on the URL, plus typical Danish notions '/dk/' or '/da/' 
							+ " AND (C7a is NULL OR length(C7a)<=2)" // NOT list of 45 largest Danish towns (http://www.stednavneudvalget.ku.dk/) 
							+ " AND (C7b is NULL OR length(C7b)<=2)" // NOT same as C7a, but on the URL 
							+ " AND (C7c is NULL OR length(C7c)<=2)" // NOT list of 75 typical Danish suffixes in town names like x'by' (for x'town')
							+ " AND (C7d is NULL OR length(C7d)<=2)" // NOT same as C7c, but on the URL 
							+ " AND (C7e is NULL OR length(C7e)<=2)" // NOT København (Copenhagen) and Danmark (Denmark) translated to English, German, French and other European languages as well as Turkish, Somali and Romanian.
							+ " AND (C7f is NULL OR length(C7f)<=2)" // NOT same as C7e, but on the URL 
							+ " AND (C8a is NULL OR length(C8a)<=2)" // NOT look in list of 150 (out of 515) Danish associations and unions 
							+ " AND (C8b is NULL OR length(C8b)<=2)" // NOT same as C8a, but on the URL
							+ " AND (C9a is NULL OR length(C9a)<=2)" // NOT look for special Danish ways to denote the type of a company, which is 'Aps' for a company based on partnership and 'A/S' for companies on the stock market
							+ " AND (C9b is NULL OR length(C9b)<=2)" // NOT look in list of 150 out of 320 Danish company names (http://www.bloomberg.com/) 
							+ " AND (C9c is NULL OR length(C9c)<=2)" // NOT same as C9b, but on the URL
							+ " AND (C9d is NULL OR length(C9d)<=2)" // NOT search for CVR + 8 digits for registered Danish company number 
							+ " AND (C10a is NULL OR length(C10a)<=2)" // NOT look for typical patterns in Danish surnames like names ending in 'sen' (for son)
							+ " AND (C10b is NULL OR length(C10b)<=2)" // NOT look in list of 150 frequently used Danish first names and surnames
							+ " AND (C16a is NULL OR C16a=0)" //There are NO known .dk sites that points to the webpage 
							+ " AND (C17a is NULL OR C17a=0)"; //The webpage does NOT points to other .dk sites
							// C15a depends on parameters - C18 - Waiting with IP-addresses
				    	} else if (calcDanishCode>=100 && calcDanishCode<=107) {
				    		whereStmt = whereStmt + (whereStmt.isEmpty() ? "" : " AND ") + "extSize<=200";
				    	} 
			    	}
		
		  			Counters cr = new Counters();
		  			sql = sql + (whereStmt.isEmpty() ? "" : " WHERE " + whereStmt);
				    processCalcDanishCountAndIntDanish(conn, tablename, sql, calcDanishCode, updateOp, cr, false);
		    		
		            System.out.println("Processed " + cr.recCount + " records");
		            System.out.println("Updated " + cr.updateCount + " records");
		            System.out.println("Skipped " + cr.skippedCount + " records");
		            System.out.println("Ignored " + cr.ignoredCount + " records");
			    	break;
		          case c15bCalc:
		        	c15bUpdate(conn, tablename);
		  	    	break;
		          case c8bCalc:         
		          	c8bUpdate(conn, tablename);
		  	    	break;
		          case arabic:
		        	arabicUpdate(conn, tablename);
		    		String arExeTxt = "touch " + item.datasubdir.getAbsolutePath() + "/" + MysqlWorkFlow.wf_updatefilename_prefix + MysqlIngester.ingest_arabic_update_no;
		        	Runtime.getRuntime().exec(arExeTxt);
		        	System.out.println("- executed: '" +  arExeTxt + "'");
		    	    break;
		          case turkey:
		        	turkeyUpdate(conn, tablename);  
		    		String turkExeTxt = "touch " + item.datasubdir.getAbsolutePath() + "/" + MysqlWorkFlow.wf_updatefilename_prefix + MysqlIngester.ingest_turk_update_no;
		        	Runtime.getRuntime().exec(turkExeTxt);
		        	System.out.println("- executed: '" +  turkExeTxt + "'");
		    	    break;
		          case update10:
		        	update10(conn, tablename);  
		    		String u10ExeTxt = "touch " + item.datasubdir.getAbsolutePath() + "/" + MysqlWorkFlow.wf_updatefilename_prefix + MysqlIngester.ingest_big0909_update_no;
		        	Runtime.getRuntime().exec(u10ExeTxt);
		        	System.out.println("- executed: '" +  u10ExeTxt + "'");
		      	    break;
		          case update11:
			        codeSet = MysqlRes.getCodeSet(conn, tablename, CodesFraction.codes_nonpositive);
			        updateXxFromCodeSet(updateOp , conn, tablename, "*", codeSet, true);
		        	String u11ExeTxt = "touch " + item.datasubdir.getAbsolutePath() + "/" + MysqlWorkFlow.wf_updatefilename_prefix + MysqlIngester.ingest_mail0110_update_no;
		        	Runtime.getRuntime().exec(u11ExeTxt);
		        	System.out.println("- executed: '" +  u11ExeTxt + "'");
		      	    break;
		          case update12:
			        String u12UpdFilename = item.datasubdir.getAbsolutePath() + "/" + MysqlWorkFlow.wf_updatefilename_prefix + MysqlIngester.ingest_3efagain_update_no;
		            File uf12 = new File(u12UpdFilename); 
		    		if (!uf12.exists()) { 
				        codeSet = MysqlRes.getCodeSet(conn, tablename, CodesFraction.codes_nonpositive);
				        updateXxFromCodeSet(updateOp , conn, tablename, "*", codeSet, true);
			        	Runtime.getRuntime().exec("touch " + u12UpdFilename);
			        	System.out.println("- executed: '" +  "touch " + u12UpdFilename + "'");
		    		} else {
			        	System.out.println("ignored " + tablename);
		    		}
		      	    break;
		          case update13:
			        String u13UpdFilename = item.datasubdir.getAbsolutePath() + "/" + MysqlWorkFlow.wf_updatefilename_prefix + MysqlIngester.ingest_largeportionsupdate;
		            File uf13 = new File(u13UpdFilename); 
		    		if (!uf13.exists()) { 
			        	codeSet = new HashSet<Integer>();
						codeSet.add(-98112);
						codeSet.add(-229184);
						codeSet.add(0);
			            updateXxFromCodeSet(updateOp, conn, tablename, "*", codeSet, true);
			            Runtime.getRuntime().exec("touch " + u13UpdFilename);
			        	System.out.println("- executed: '" +  "touch " + u13UpdFilename + "'");
		    		}
		      	    break;
		          case update14:
			        String u14UpdFilename = item.datasubdir.getAbsolutePath() + "/" + MysqlWorkFlow.wf_updatefilename_prefix + MysqlIngester.ingest_wrong6_dklan;
		            File uf14 = new File(u14UpdFilename); 
		    		if (!uf14.exists()) { 
			        	// code 6 and more from jakob
			        	Set<Integer> allCodeSet = MysqlRes.getCodeSet(conn, tablename, CodesFraction.codes_nonpositive);
					    for (int code : allCodeSet) {
				        	if ((MysqlX.getBit((short)1, code)==1) ||
				        		(MysqlX.getBit((short)2, code)==1) ||
				        		(MysqlX.getBit((short)3, code)==1) ) {
				        		codeSet.add(code);
				        	}
					    }
						codeSet.add(6);
			            updateXxFromCodeSet(updateOp, conn, tablename, "*", codeSet, false);
			            Runtime.getRuntime().exec("touch " + u14UpdFilename);
			        	System.out.println("- executed: '" +  "touch " + u14UpdFilename + "'");
		    		}
		      	    break;
		          case update15:
		        	  //************************************************************************
		        	  //****************************************
			        String u15UpdFilename = item.datasubdir.getAbsolutePath() + "/" + MysqlWorkFlow.wf_updatefilename_prefix + MysqlIngester.ingest_wrong3efandbits;
		            File uf15 = new File(u15UpdFilename); 
		    		if (!uf15.exists()) { 
			        	// reset bits
			        	
			            // code 6  not taken
		    			System.out.println("code 6");
			        	Set<Integer> codeSet6 = new HashSet<Integer>();
			        	codeSet6.add(6);
			            updateXxFromCodeSet(UpdateOperation.update14, conn, tablename, "*", codeSet6, false);

			            // C3e and f + bits
			            /////WAIT on test1
		    			if (!machine.equals("test1")) {
		    				System.out.println("C3e and f");
				            update15sub(conn, tablename, limit);
		    			} else {
				            Runtime.getRuntime().exec("touch " + u15UpdFilename + "_MISSING");
				        	System.out.println("- executed: '" +  "touch " + u15UpdFilename + "_MISSING" + "'");
		    			}
			            
			            Runtime.getRuntime().exec("touch " + u15UpdFilename);
			        	System.out.println("- executed: '" +  "touch " + u15UpdFilename + "'");
		    		}
		      	    break;
		          case update16:
			        String u16UpdFilename = item.datasubdir.getAbsolutePath() + "/" + MysqlWorkFlow.wf_updatefilename_prefix + MysqlIngester.ingest_reset_unusable_codes;
		            File uf16 = new File(u16UpdFilename); 
		    		if (!uf16.exists()) { 
			            update16sub(conn, tablename, limit);
			            Runtime.getRuntime().exec("touch " + u16UpdFilename);
			        	System.out.println("- executed: '" +  "touch " + u16UpdFilename + "'");
		    		}
		      	    break;
		          case update17:
			        String u17UpdFilename = item.datasubdir.getAbsolutePath() + "/" + MysqlWorkFlow.wf_updatefilename_prefix + MysqlIngester.ingest_new_codes;
		            File uf17 = new File(u17UpdFilename); 
		    		if (!uf17.exists()) { 
		    			System.out.println("set code 208,209 for C2b phones");
		    	        if (extendedNewHadoopTable) {
						    update17subSet208_209(conn, tablename);
					    }

		    			System.out.println("set code 5 for C1a - update");
						update17subSet5(conn, tablename);

		    	        System.out.println("set code 230 for C7g");
		    	        if (extendedNewHadoopTable) {
						    update17subSet230(conn, tablename);
					    }

	    	        	System.out.println("set dk unlike from new fields");
		    	        if (extendedNewHadoopTable) {
		    	        	codeSet = MysqlRes.getCodeSet(conn, tablename, CodesFraction.codes_nonpositive);
					        updateXxFromCodeSet(updateOp , conn, tablename, "*", codeSet, true);
					    }
		    	        
			            Runtime.getRuntime().exec("touch " + u17UpdFilename);
			        	System.out.println("- executed: '" +  "touch " + u17UpdFilename + "'");
		    		}
		      	    break;
		          case update18:
			        String u18UpdFilename = item.datasubdir.getAbsolutePath() + "/" + MysqlWorkFlow.wf_updatefilename_prefix + MysqlIngester.ingest_cleanup;
		            File uf18 = new File(u18UpdFilename); 
		    		if (!uf18.exists()) { 
			    		System.out.println("dk laguange from 70's to 110-120's");
			    		update18languageTikka(conn, tablename, extendedNewHadoopTable);
			    		update18languageaeoeaa(conn, tablename, extendedNewHadoopTable);

			    		System.out.println("reset 208, 209 to 310, 311, 312");
		    	        if (fSet.contains("C2b")) {
		    	        	update18newphone(conn, tablename);
		    	        }
						     
			    		System.out.println("find tlf an +45 to 315, 316, 317");
			    		update18oldphone(conn, tablename, extendedNewHadoopTable);

			    		System.out.println("reset 206, 207 to 320, 321, 322");
			    		update18mail(conn, tablename, extendedNewHadoopTable);
			    		
			    		/*System.out.println("set dk unlike from new fields - function changes");
			    		System.out.println("new fields word list");
	    	        	//NOT likely dk CHECK unions and companies is taken in reupdate instead
		    	        if (fSet.contains("C7g")) {
		    	        	codeSet = MysqlRes.getCodeSet(conn, tablename, CodesFraction.codes_nonpositive);
					        updateXxFromCodeSet(updateOp , conn, tablename, "*", codeSet, true);
					    }*/
		    	        
		    			System.out.println("clean up tld=dk");
		    	   		update18tld(conn, tablename);

			            Runtime.getRuntime().exec("touch " + u18UpdFilename);
			        	System.out.println("- executed: '" +  "touch " + u18UpdFilename + "'");
		    		}
		      	    break;
		          case update19:
			        String u19UpdFilename = item.datasubdir.getAbsolutePath() + "/" + MysqlWorkFlow.wf_updatefilename_prefix + MysqlIngester.ingest_0s;
		            File uf19 = new File(u19UpdFilename); 
		    		if (!uf19.exists()) {
			    		System.out.println("set bits for 0's");
			            long cnt = 20001;
			            while (cnt>20000) {
			            	cnt = update19code0(conn, tablename, extendedNewHadoopTable);
			            }
			            Runtime.getRuntime().exec("touch " + u19UpdFilename);
			        	System.out.println("- executed: '" +  "touch " + u19UpdFilename + "'");
		    		}
			      	break;
		          case update20:
			        String u20UpdFilename = item.datasubdir.getAbsolutePath() + "/" + MysqlWorkFlow.wf_updatefilename_prefix + MysqlIngester.ingest_0cleanups;
		            File uf20 = new File(u20UpdFilename); 
		    		if (!uf20.exists()) {
			    		System.out.println("correct 300s");
			    		update20nosvtlderror300s(conn, tablename, extendedNewHadoopTable, 312);
			    		update20nosvtlderror300s(conn, tablename, extendedNewHadoopTable, 317);
			    		update20nosvtlderror300s(conn, tablename, extendedNewHadoopTable, 323);
			    		update20nosvtlderror300s(conn, tablename, extendedNewHadoopTable, 324);

			    		System.out.println("correct 120-128");
			        	Set<Integer> nonposCodeSet = MysqlRes.getCodeSet(conn, tablename, CodesFraction.codes_nonpositive);
					    for (int code : nonposCodeSet) {
				        	if (MysqlX.getBit((short)3, code)==1) { //language C4a dk, no or sv
				        		codeSet.add(code);
				        	}
					    }
					    update20_100sfromCodeSet(conn, tablename, codeSet, 120, extendedNewHadoopTable);

			    		System.out.println("correct 110-112");
			    		codeSet = new HashSet<Integer>();
			    		for (int code : nonposCodeSet) {
				        	if (MysqlX.getBit((short)2, code)==1) { //æøå
				        		codeSet.add(code);
				        	}
					    }
					    update20_100sfromCodeSet(conn, tablename, codeSet, 110, extendedNewHadoopTable);
		    			
			    		System.out.println("old-non-dk");
			    		codeSet = new HashSet<Integer>();
					    for (int code : nonposCodeSet) {
				        	if (
				        			    (MysqlX.getBit((short)1, code)==0)   //1a,2a
					   			     && (MysqlX.getBit((short)2, code)==0)  //3a,3c
					   			     && (MysqlX.getBit((short)9, code)==0)  //3b,3d
					   			     && (MysqlX.getBit((short)3, code)==0)  //C4a = 'da' 'no' or 'sv'	language like danish
					   			     && (MysqlX.getBit((short)4, code)==0)  //C6b,C6c
					   			     && (MysqlX.getBit((short)5, code)==0)  //C7a,C7b,C7e,C7f
					   			     && (MysqlX.getBit((short)6, code)==0)  //C9a,C9d
					   			     && (MysqlX.getBit((short)14, code)==0)) { //C10a,C10b 
				        		codeSet.add(code);
				        	}
					    }
		        		codeSet.add(0);
					    update20_notlikelydk(conn, tablename, codeSet, extendedNewHadoopTable);
					    
			            Runtime.getRuntime().exec("touch " + u20UpdFilename);
			        	System.out.println("- executed: '" +  "touch " + u20UpdFilename + "'");
		    		}
		      	    break;
		          case correct_NOTdk:  
				        String u21UpdFilename = item.datasubdir.getAbsolutePath() + "/" + MysqlWorkFlow.wf_updatefilename_prefix + MysqlIngester.ingest_correctNotDk;
			            File uf21 = new File(u21UpdFilename);
			            
				        /*if ((!update) &&  res.C4a!=null ) {  
				        	MysqlRes.CodesResult coderes = new MysqlRes.CodesResult(); 
				        	coderes = MysqlX.setcodes_otherLanguagesChars(res.C4a);
				        	if (coderes.calcDanishCode!=0) { 
				            	res.calcDanishCode = coderes.calcDanishCode;
				                res.intDanish = coderes.intDanish;
				    	    	update=true;
				    		}
						}*/
			            
			    		if (!uf21.exists()) {
			    			
			    			//codeSet.add(2);  - only 302? //ignore 1 and 3
			    			//if (res.calcDanishCode==0 && res.Cext2>=200) res.calcDanishCode = 2; //lots of doublechars
			    			//if (res.calcDanishCode==0 && res.Cext2>=130) res.calcDanishCode = 220; //lots of doublechars
			    			
			    			//TODO: select count(*) from ResHadoop_m1_d5_p1_1 where calcDanishCode=302 AND extDblChar>=200;
			    			
			    	    	/*if (c4a.equals("zh") || c4a.equals("ja") || c4a.equals("ko"))  { 
			    	    		// Chinese, Japanese, Korean 
			    	    		code = 10;
			    	    	} else if (c4a.equals("bo") || c4a.equals("hi") || c4a.equals("mn") || c4a.equals("my") || c4a.equals("ne") || c4a.equals("ta") || c4a.equals("th") || c4a.equals("vi"))  { 
			    	    		// asian : Tibetan, Hindi, Mongolian, Burmese, Nepali, Tamil, Thai, Vietnamese
			    	    		code = 11;
			    	    	} else if (c4a.equals("he") || c4a.equals("fa") || c4a.equals("ur") || c4a.equals("yi") || c4a.equals("ar"))  { // arabic : Hebrew, Persian, Urdu, Yiddish ALSO arabic NOT Turkish
			    	    		code = 12;
			    			}*/
			    		    codeSet.add(220); //"Cext2>= 130 (<200)" : "bytes per char > 1,3 (<2)");
			    		    
			    			codeSet.add(112); //dk language æøå  – NO dk & 'no' words & ‘no’ or ‘sv’ tld
			    		    codeSet.add(122); //C4a=da - C5a=0 & C5b>0 & C15b<>no/sv":" - NO dk & 'no' words & ‘no’ or ‘sv’ tld"
			    		    codeSet.add(125); //C4a=no - C5a=0 & C5b>0 & C15b<>no/sv":" - NO dk & 'no' words & ‘no’ or ‘sv’ tld"
			    		    codeSet.add(128); //C4a=sv - C5a=0 & C5b>0 & C15b<>no/sv":" - NO dk & 'no' words & ‘no’ or ‘sv’ tld"
			    		    codeSet.add(312); // tlf C5a=0 & C5b>0 & C15b<>no/sv":" - NO dk & 'no' words & ‘no’ or ‘sv’ tld");
			    		    codeSet.add(317); // tlf C5a=0 & C5b>0 & C15b<>no/sv":" - NO dk & 'no' words & ‘no’ or ‘sv’ tld");
			    		    codeSet.add(327); // mail C7g=0 -  C5a=0 & C5b>0 & C15b<>no/sv":" - NO dk & 'no' words & ‘no’ or ‘sv’ tld")
			    		    
			    			//not like danish - ignore?
			    			/*for (int code=30; code<=35; code++) { 
			    		    	codeSet.add(code);
			    		    }
			    		    codeSet.add(38); 
			    		    for (int code=50; code<=55; code++) {
			    		    	codeSet.add(code);
			    		    }
			    		    codeSet.add(58); 
			    		    for (int code=100; code<=107; code++) {
			    		    	codeSet.add(code);
			    		    }*/
			    		    

				    		System.out.println("correctNotDk");
				    		//update21nosvtlderror300s(conn, tablename, extendedNewHadoopTable, 312);
			    		}
		        	  break;
		          case updateBerdb:
				        String uBdbUpdFilename = item.datasubdir.getAbsolutePath() + "/" + MysqlWorkFlow.wf_update_links_filename ;
			            File ufdb = new File(uBdbUpdFilename); 
			    		if (!ufdb.exists()) {
			    			/*if (true) {
			    			System.out.println("TEST: ");
			    			String l = "http://www.facebook.com/Pixmania.nl";
			    			System.out.println("'" + l + "': '" + MysqlRes.urlInBerkley(l, linksBase) + "'");
			    			l = "http://www.facebook.com/PlanetEarth";
			    			System.out.println("'" + l + "': '" + MysqlRes.urlInBerkley(l, linksBase) + "'");
			    	        String selectSQL = "SELECT Url, UrlOrig FROM ResHadoop_m200_dA_p1_12 LIMIT 1";
					        PreparedStatement s = conn.prepareStatement(selectSQL); 
					        ResultSet rs = s.executeQuery();
					    	rs.next();
					    	l = rs.getString("UrlOrig");
			    			System.out.println("'" + l + "' (orig) : '" + MysqlRes.urlInBerkley(l, linksBase) + "'");
					    	l = rs.getString("Url");
			    			System.out.println("'" + l + "' : '" + MysqlRes.urlInBerkley(l, linksBase) + "'");
			    			rs.close();
			    			} else { */
			    			codeSet = MysqlRes.getCodeSet(conn, tablename, CodesFraction.codes_all);
				            updateXxFromCodeSet(updateOp, conn, tablename, "Url, UrlOrig, extWDate, C16a, calcDanishCode", codeSet, false);
				            Runtime.getRuntime().exec("touch " + uBdbUpdFilename);
				        	System.out.println("- executed: '" +  "touch " + uBdbUpdFilename + "'");
			    		}
		          case noCalc:         
		  	    	break;
		    	}
    		}
    	}
        conn.close();
    }
    
    private static void arabicUpdate(Connection conn, String tablename) throws SQLException {
        long recCount=0L;
        long skippedCount=0L;
        long ignoredCount=0L;
        long updateCount = 0L;
        Set<Integer> codeSet = new HashSet<Integer>();
        codeSet = MysqlRes.getCodeSet(conn, tablename, CodesFraction.codes_nonpositive);
	    
	    for (int i : codeSet) {
	    	System.out.println("Processing " + i);
	    	if (MysqlX.getBit((short)2, i)==0) { //(!bitSet('-4', i)   
		        String selectSQL = "SELECT * FROM " + tablename + " WHERE calcDanishCode=" + i;
		        PreparedStatement s = conn.prepareStatement(selectSQL); 
		        ResultSet rs = s.executeQuery();
		    	while (rs.next()) {
					recCount++;
					if (rs.getString("C4a").equals("ar")) {
                        if (!MysqlRes.updateLineCalcDanishCodeAndIntDanish(conn, tablename, 12, 1/100, rs.getString("Url"),  rs.getTimestamp("extWDate"))) {
                        	skippedCount++; 
        				} else {
        	    			updateCount++;
                		}
					} else {
						ignoredCount++;
					}
		    	}
		    	rs.close();
			    s.close();
			}
	    }
        System.out.println("Processed " + recCount + " records");
        System.out.println("Updated " + updateCount + " records");
        System.out.println("Skipped " + skippedCount + " records");
        System.out.println("Ignored " + ignoredCount + " records");
    }

    
    private static void update10(Connection conn, String tablename) throws SQLException {
    	//new codes 
		//  no dk indications               ignore-6a size>250	=> calcDanishCode =32
		//  no dk indications, but C15a="y"	ignore-6a size>250	=> calcDanishCode =33
		//  no dk indications               ignore-6a+7cd size>250	=> calcDanishCode =34
		//  no dk indications, but C15a="y"	ignore-6a+7cd size>250	=> calcDanishCode =35
		//  no dk indications               ignore-6a size:200-250	=> calcDanishCode =52
		//  no dk indications, but C15a="y"	ignore-6a size:200-250	=> calcDanishCode =53
		//  no dk indications               ignore-6a+7cd size:200-250	=> calcDanishCode =54
		//  no dk indications, but C15a="y"	ignore-6a+7cd size:200-250	=> calcDanishCode =55
		//  no dk indications               ignore-6a+7cd  8ab>0  	=> calcDanishCode =200 //foreninger (SKAL UNDERSØGES MEGET!)
		//  no dk indications, but C15a="y"	ignore-6a+7cd  8ab>0  	=> calcDanishCode =201 //foreninger (SKAL UNDERSØGES MEGET!)
		//  no dk indications               ignore-6a+7cd  9ab>0 	=> calcDanishCode =202 //firmaer (SKAL UNDERSØGES MEGET!) 	
		//  no dk indications, but C15a="y"	ignore-6a+7cd  9ab>0 	=> calcDanishCode =203 //firmaer (SKAL UNDERSØGES MEGET!) 	
	    //  new bit codes
    	// changed from 1/8 2014 now code is int bit 16  C10a>0    	
        //                                       bit 17 3b,d, hvor små o oe ord er frasorteret -NYE        
    	// set new C3e and C3f
    	
        long recCount=0L;
        long skippedCount=0L;
        long ignoredCount=0L;
        long updateCount = 0L;

        Set<Integer> codeSet = new HashSet<Integer>();
        
        String selectSQL = "SELECT DISTINCT calcDanishCode FROM "+ tablename; //new hack?
        PreparedStatement t = conn.prepareStatement(selectSQL); 
        ResultSet trs = t.executeQuery();
    	while (trs.next()) {
    		codeSet.add(trs.getInt("calcDanishCode"));
    	}
    	trs.close();
	    t.close();
	    
	    for (int code : codeSet) {
	    	if (code<0) {
		    	System.out.println("Processing " + code);
	
		    	selectSQL = "SELECT * FROM " + tablename + " WHERE calcDanishCode=" + code;
		        PreparedStatement s = conn.prepareStatement(selectSQL); 
		        ResultSet rs = s.executeQuery();
		        boolean checkNoDanish = (MysqlX.getBit((short)1, code)==0)  //1a,2a
	   			     && (MysqlX.getBit((short)2, code)==0)  //3a,3c
	   			     && (MysqlX.getBit((short)9, code)==0)  //3b,3d
	   			     && (MysqlX.getBit((short)3, code)==0)  //C4a = 'da' 'no' or 'sv'	language like danish
	   			     && (MysqlX.getBit((short)4, code)==0)  //C6b,C6c
	   			     && (MysqlX.getBit((short)5, code)==0)  //C7a,C7b,C7e,C7f
	   			     && (MysqlX.getBit((short)6, code)==0)  //C9a,C9d
	   			     && (MysqlX.getBit((short)14, code)==0); //C10a,C10b
	
		        while (rs.next()) {
		        	CriteriaUpdate10 res = new CriteriaUpdate10(rs);
		    		boolean update = false;
		    		
			        /*** calculate new C3e and C3f ***/
					recCount++;
				    if (!(res.C3b == null)) { //added 9/9
					    res.C3e = MysqlX.findNew3ValToken(res.C3b); 
					    update = true;
				    }
				    if (!(res.C3d == null)) { //added 9/9
				    	res.C3f = MysqlX.findNew3ValToken(res.C3d); 
					    update = true;
				    }
	
				    if(checkNoDanish) {
				    	String C5a = rs.getString("C5a"); 
				        if ((C5a==null) || (C5a.startsWith("0"))) { // Candidate & //do NOT includes typical and distingisable Danish words
				        	boolean incl6a = MysqlX.getBit((short)10, code)==1;
				        	boolean inclTld = MysqlX.getBit((short)7, code)==1;
				        	boolean incl7c = false;
				        	boolean incl7d = false;
				        	if (MysqlX.getBit((short)11, code)==1) {
					            String C7c = rs.getString("C7c");
					            String C7d = rs.getString("C7d");
					            incl7c = (C7c!=null);
					        	incl7c = incl7c && !C7c.startsWith("0");
					            incl7d = (C7d!=null);
					        	incl7d = incl7d && !C7d.startsWith("0");
				        	}
				        	boolean incl8a = false;
				        	boolean incl8b = false;
				        	if (MysqlX.getBit((short)12, code)==1) {
					            String C8a = rs.getString("C8a");
					            String C8b = rs.getString("C8b");
					            incl8a = (C8a!=null);
					        	incl8a = incl8a && !C8a.startsWith("0");
					            incl8b = (C8b!=null);
					        	incl8b = incl8b && !C8b.startsWith("0");
				        	}
				        	boolean incl9b = false;
				        	boolean incl9c = false;
				        	if (MysqlX.getBit((short)13, code)==1) {
					            String C9b = rs.getString("C9b");
					            String C9c = rs.getString("C9c");
					            incl9b = (C9b!=null);
					        	incl9b = incl9b && !C9b.startsWith("0");
					            incl9c = (C9c!=null);
					        	incl9c = incl9c && !C9c.startsWith("0");
				        	}
				            boolean bigSize = (MysqlX.getBit((short)15, code)==1); // 50 for size 200-250
				    	    int interval = (bigSize ? 30 : 50);
					    	if (!incl6a && !incl7c && !incl7d && !incl8a && !incl8b && !incl9b && !incl9c) {
					            res.calcDanishCode = (interval + (inclTld ? 1 : 0));
					            res.intDanish = 1/100;
					        } else if (!incl7c && !incl7d && !incl8a && !incl8b && !incl9b && !incl9c) { //ignore-6a 32,33,52,53 
					            res.calcDanishCode = (interval + 2 + (inclTld ? 1 : 0));
					            res.intDanish = 2/100;
					        } else if (!incl8a && !incl8b && !incl9b && !incl9c) { //ignore-6a + 7cd 34,35,54,55
					            res.calcDanishCode = (interval + 4 + (inclTld ? 1 : 0));
					            res.intDanish = 4/100;
					        } else if (incl8a || incl8b ) { //ignore-8ab +...
					            res.calcDanishCode = (200 + (inclTld ? 1 : 0));
					            res.intDanish = 10/100;
					        } else if (incl9b || incl9c ) { //ignore-9ab +...
					            res.calcDanishCode = (202 + (inclTld ? 1 : 0));
					            res.intDanish = 10/100;
					        }
				        }
				    } //not danish
				    update = update || res.calcDanishCode > 0;
			    	if (res.calcDanishCode<=0) {
				    	// changed from 1/8 2014 now code is int     	
			            String C10a = rs.getString("C10a");
				        if (((C10a!=null) &&  (C10a.length()>2))) { 
				        	res.calcDanishCode = MysqlX.setBit(16, res.calcDanishCode);
				        	update = true;
				        }
				        //3b,d, hvor små o oe ord er frasorteret -NYE         
				        if ((!res.C3e.startsWith("0")) || (!res.C3f.startsWith("0"))) { 
				        	res.calcDanishCode = MysqlX.setBit(17, res.calcDanishCode);  
				        	update = true;
			        	}
			    	}
			    	if (update) {
	                    if (!updateLineUpdate10(conn, tablename, res)) {
	                    	skippedCount++; 
	    				} else {
	    	    			updateCount++;
	            		}
					} else {
						ignoredCount++;
			    	}
		        } //while rs
		    	rs.close();
			    s.close();
		    }
	        System.out.println("Processed " + recCount + " records");
	        System.out.println("Updated " + updateCount + " records");
	        System.out.println("Skipped " + skippedCount + " records");
	        System.out.println("Ignored " + ignoredCount + " records");
    	}
    }

    private static boolean UpdateXxCallOp(UpdateOperation op, Connection conn, String tablename, String sql, int code) throws SQLException  {
        PreparedStatement stmt = conn.prepareStatement(sql); 
        ResultSet rs = stmt.executeQuery();
        boolean moreRecs = false;
        if (op.equals(UpdateOperation.update11))  {
        	System.out.println("call: 'update11'");
        	moreRecs = update11subRs(conn, tablename, rs, code);
        } else if (op.equals(UpdateOperation.update12)) {
        	System.out.println("call: 'update12'");
        	moreRecs = update12subRs(conn, tablename, rs, code);
        } else if (op.equals(UpdateOperation.update13)) {
        	System.out.println("call: 'update13'");
        	moreRecs = update13subRs(conn, tablename, rs, code);
        } else if (op.equals(UpdateOperation.update14)) {
        	System.out.println("call: 'update14'");
        	moreRecs = update14subRs(conn, tablename, rs, code);
        } else if (op.equals(UpdateOperation.update15)) {
        	System.out.println("call: 'update15'");
        	moreRecs = update15subRs(conn, tablename, rs, code);
        } else if (op.equals(UpdateOperation.update17)) {
        	System.out.println("call: 'update17'");
        	moreRecs = update17subRs(conn, tablename, rs, code);
        } else if (op.equals(UpdateOperation.updateBerdb)) {
        	System.out.println("call: 'update Berkeley db'");
        	moreRecs = updateBerDbsubRs(conn, tablename, rs, code);
        }
        
    	rs.close();
    	stmt.close();
    	return moreRecs;
    }

	private static void updateXxFromCodeSet(UpdateOperation op, Connection conn, String tablename, String fieldList, Set<Integer> codeSet, boolean onlyLargeTables) throws SQLException  {
		System.out.println("Number of codes " + codeSet.size());
	    for (int code : codeSet) {
	        System.out.println("Processing " + code);
	        String selectSQL = "SELECT " + fieldList + " FROM " + tablename + " WHERE calcDanishCode=" + code;
	        if ( code == 6 ) {
	        	int cnt = 0;
	        	boolean moreRecs = true;
	        	while (moreRecs) {
    				cnt++;
            		String iSelectSQL = selectSQL + " LIMIT 100000";
			        System.out.println("Processing " + iSelectSQL + " - cnt: " + cnt);
			        moreRecs= UpdateXxCallOp(op, conn, tablename, iSelectSQL, code);
    			} 
	        } else if (code == 2 || code == -32576 ) { 
	        	if ( (!onlyLargeTables) || (onlyLargeTables && (tablename.contains("i") 
	        			|| tablename.contains("m2_d6_p2") || tablename.contains("m1_d3_p3") || tablename.contains("m1_d2_p2"))) ) {
	        		Set<Interval> intervalSet = new HashSet<Interval>();
	        		//if (code == 2 || code == 6 ) {
	        		//	System.out.println("based on CodesSizeIntervalsDetailed0:");
	        		//	for (CodesSizeIntervalsDetailed0 ci: CodesSizeIntervalsDetailed0.values()) {
	        		//		intervalSet.add(MysqlX.getIntervalDetailed0(ci));
	        		//		System.out.println("getWhereInterval: " + MysqlX.getWhereInterval(MysqlX.getIntervalDetailed0(ci)));
	        		//	}
	        		//} else {
        			System.out.println("based on CodesSizeIntervals NORMAL:");
        			for (CodesSizeIntervals ci: CodesSizeIntervals.values()) {
        				intervalSet.add(MysqlX.getIntervalNormal(ci));
        				System.out.println("getWhereInterval: " + MysqlX.getWhereInterval(MysqlX.getIntervalNormal(ci)));
        			}
	        		//}
	            	for (Interval intv: intervalSet) {
	            		String iSelectSQL = selectSQL + " AND " + MysqlX.getWhereInterval(intv);
				        System.out.println("Processing " + iSelectSQL);
				        UpdateXxCallOp(op, conn, tablename, iSelectSQL, code);
		        	}
		        } else {
			        UpdateXxCallOp(op, conn, tablename, selectSQL, code);
		        }
	        } else {
		        UpdateXxCallOp(op, conn, tablename, selectSQL, code);
	        }
        }
    }
    
    private static boolean update11subRs(Connection conn, String tablename, ResultSet rs, int code) throws SQLException {
        long recCount=0L;
        long skippedCount=0L;
        long ignoredCount=0L;
        long updateCount = 0L;

        boolean checkMailPh = (MysqlX.getBit((short)1, code)==1);  //1a,2a

        while (rs.next()) {
			recCount++;
	
			SingleCriteriaResult res = new SingleCriteriaResult(rs, false);
			boolean update = false;
	
			/* Reset positive START */
		    //res.calcDanishCode = 1  size=0 
		    if (res.Cext1==0) {
		    	res.calcDanishCode = 1;   //no text
		    	update=true;
		    }
	
		    //res.calcDanishCode = 2 double-char >= 200
			if ((!update) && res.Cext2>=200) {
				res.calcDanishCode = 2; //lots of dioublechars
		    	update=true;
			}
			
		    //res.calcDanishCode = 3
		    if ((!update) && res.C15b.equals("dk")){
		        res.calcDanishCode = 3;
		    	update=true;
			}
	
			//res.calcDanishCode =100-107 small sizes
			if ((!update) && res.Cext1<=200) { 
				MysqlRes.CodesResult coderes = new MysqlRes.CodesResult(); 
	        	coderes = MysqlX.setcodes_smallSize(res.C4a, res.C3a, res.C3b, res.C3c, res.C3d, res.C6a, res.C6b, res.C6c);
	        	if (coderes.calcDanishCode!=0) { 
	            	res.calcDanishCode = coderes.calcDanishCode;
	                res.intDanish = coderes.intDanish;
	    	    	update=true;
	    		}
			}
		    
		    //res.calcDanishCode =10-12 asian/arabic languages	
	        if ((!update) &&  res.C4a!=null ) {  
	        	MysqlRes.CodesResult coderes = new MysqlRes.CodesResult(); 
	        	coderes = MysqlX.setcodes_otherLanguagesChars(res.C4a);
	        	if (coderes.calcDanishCode!=0) { 
	            	res.calcDanishCode = coderes.calcDanishCode;
	                res.intDanish = coderes.intDanish;
	    	    	update=true;
	    		}
			}
	        
		    //res.calcDanishCode =20-27, 40-47 - many dk indications 
	        if ((!update) 
	        		&& res.C15a!=null && res.C16a!=null && res.C17a!=null 
	        		&& res.Cext1>200 && res.C3a!=null && res.C4a!=null 
	        		&& res.C5a!=null && res.C5b!=null && res.C6a!=null) {
	        	MysqlX.setcodes_dkLanguageVeryLikely(res);
	        	if (res.calcDanishCode>0) {
	        		update=true;
	        	}
	        }
	
	        if (!update) {
	        	CodesResult cr = MysqlX.setcodes_notDkLanguageVeryLikely(res); //get calcode and IntDanish  and check in depth
	        	if (cr.calcDanishCode>0) {
	        		res.calcDanishCode = cr.calcDanishCode;
	        		res.intDanish = cr.intDanish;
	        		update=true;
	        	}
	        }
	        
			/* Reset positive END */
	
	        /*** calculate ***/
			if (checkMailPh && !update) {
			    //res.calcDanishCode = 6  tlf (C2a>0) (varied acc. to occ. of 45) update 11
		        if (res.C2a!=null ) {  
		        	MysqlRes.CodesResult coderes = MysqlX.setcodes_WRONGphone(res.C2a);
	            	if (coderes.calcDanishCode!=0) { 
		            	res.calcDanishCode = coderes.calcDanishCode;
		                res.intDanish = coderes.intDanish;
		    	    	update=true;
		    		}
				}
			    
			    //res.calcDanishCode = 5  mail (C1a>0)
		        /*depricated
		        if (res.C1a!=null ) {  
		        	MysqlRes.CodesResult coderes = MysqlX.setcodes_mail(res.C1a);
	            	if (coderes.calcDanishCode!=0) { 
		            	res.calcDanishCode = coderes.calcDanishCode;
		                res.intDanish = coderes.intDanish;
		    	    	update=true;
		    		}
				}*/
	        } 
	    	if (update) {
	            if (!updateLineUpdate11(conn, tablename, res)) {
	            	skippedCount++; 
				} else {
	    			updateCount++;
	    		}
			} else {
				ignoredCount++;
	    	}
	    } //while rs    
        System.out.println("Processed " + recCount + " records");
        System.out.println("Updated " + updateCount + " records");
        System.out.println("Skipped " + skippedCount + " records");
        System.out.println("Ignored " + ignoredCount + " records");
        return (recCount!=0);
    }
    
    private static boolean update12subRs(Connection conn, String tablename, ResultSet rs, int code) throws SQLException {
        long recCount=0L;
        long skippedCount=0L;
        long ignoredCount=0L;
        long updateCount = 0L;

        while (rs.next()) {
			recCount++;

			CriteriaUpdate10 res = new CriteriaUpdate10(rs);
    		boolean update = false;

            
    		/*** calculate ***/
    		/*** update C3ef again ***/
		    if (!(res.C3e == null) && !res.C3e.isEmpty()) { 
		    	String s = MysqlX.findNew3ValToken(res.C3b);
		    	if (!res.C3e.equals(s)) {
		    		res.C3e = s;
		    		update = true;
			    }
		    }
		    if (!(res.C3f == null) && !res.C3f.isEmpty()) { 
		    	String s = MysqlX.findNew3ValToken(res.C3d);
		    	if (!res.C3f.equals(s)) {
		    		res.C3f = s;
		    		update = true;
			    }
		    }
    		
    		//3b,d, hvor små o oe ord er frasorteret -NYE         
	        if ((res.C3e!=null) && ( (!res.C3e.startsWith("0")) && (!res.C3e.isEmpty()) )) { 
	        	res.calcDanishCode = MysqlX.setBit(18, res.calcDanishCode);  
	        	update = true;
        	}
	    	if (!update) {
		        if ((res.C3f!=null) && ( (!res.C3f.startsWith("0")) && (!res.C3f.isEmpty()) )) { 
		        	res.calcDanishCode = MysqlX.setBit(18, res.calcDanishCode);  
		        	update = true;
	        	}
        	}
		    
	    	if (update) {
                if (!updateLineUpdate10(conn, tablename, res)) {
                	skippedCount++; 
				} else {
	    			updateCount++;
        		}
			} else {
				ignoredCount++;
	    	}
	    } //while rs
        System.out.println("Processed " + recCount + " records");
        System.out.println("Updated " + updateCount + " records");
        System.out.println("Skipped " + skippedCount + " records");
        System.out.println("Ignored " + ignoredCount + " records");
        return (recCount!=0);
	}
        

    private static boolean update13subRs(Connection conn, String tablename, ResultSet rs, int code) throws SQLException {        
        long recCount=0L;
        long skippedCount=0L;
        long ignoredCount=0L;
        long updateCount = 0L;

        while (rs.next()) {
			recCount++;
			
			SingleCriteriaResult res = new SingleCriteriaResult(rs, false);
			boolean update = false;
	
			/* Reset positive START */
		    //res.calcDanishCode = 1  size=0 
		    if (res.Cext1==0) {
		    	res.calcDanishCode = 1;   //no text
		    	update=true;
		    }
	
		    //res.calcDanishCode = 2 double-char >= 200
			if ((!update) && res.Cext2>=200) {
				res.calcDanishCode = 2; //lots of dioublechars
		    	update=true;
			}
			
		    //res.calcDanishCode = 3
		    if ((!update) && res.C15b.equals("dk")){
		        res.calcDanishCode = 3;
		    	update=true;
			}
	
			//res.calcDanishCode =100-107 small sizes
			if ((!update) && res.Cext1<=200) { 
				MysqlRes.CodesResult coderes = new MysqlRes.CodesResult(); 
	        	coderes = MysqlX.setcodes_smallSize(res.C4a, res.C3a, res.C3b, res.C3c, res.C3d, res.C6a, res.C6b, res.C6c);
	        	if (coderes.calcDanishCode!=0) { 
	            	res.calcDanishCode = coderes.calcDanishCode;
	                res.intDanish = coderes.intDanish;
	    	    	update=true;
	    		}
			}
		    
		    //res.calcDanishCode =10-12 asian/arabic languages	
	        if ((!update) &&  res.C4a!=null ) {  
	        	MysqlRes.CodesResult coderes = new MysqlRes.CodesResult(); 
	        	coderes = MysqlX.setcodes_otherLanguagesChars(res.C4a);
	        	if (coderes.calcDanishCode!=0) { 
	            	res.calcDanishCode = coderes.calcDanishCode;
	                res.intDanish = coderes.intDanish;
	    	    	update=true;
	    		}
			}
	        
		    //res.calcDanishCode =20-27, 40-47 - many dk indications 
	        if ((!update) 
	        		&& res.C15a!=null && res.C16a!=null && res.C17a!=null 
	        		&& res.Cext1>200 && res.C3a!=null && res.C4a!=null 
	        		&& res.C5a!=null && res.C5b!=null && res.C6a!=null) {
	        	MysqlX.setcodes_dkLanguageVeryLikely(res);
	        	if (res.calcDanishCode>0) {
	        		update=true;
	        	}
	        }
	
	        if (!update) {
	        	CodesResult cr = MysqlX.setcodes_notDkLanguageVeryLikely(res); //get calcode and IntDanish  and check in depth
	        	if (cr.calcDanishCode>0) {
	        		res.calcDanishCode = cr.calcDanishCode;
	        		res.intDanish = cr.intDanish;
	        		update=true;
	        	}
	        }
	        
	        if (!update && res.calcDanishCode==0) {
	        	int c = MysqlX.findNegativBitmapCalcCode(res);
	        	if (c!=res.calcDanishCode) {
	        		res.calcDanishCode = c;
	        		update=true;
	        	}
	        }

	        /* Reset positive END */

	        /*** calculate 11 ***/
	        boolean checkMailPh = (MysqlX.getBit((short)1, code)==1);  //1a,2a
			if (checkMailPh && !update) {
			    //res.calcDanishCode = 6  tlf (C2a>0) (varied acc. to occ. of 45)  update 13
		        if (res.C2a!=null ) {  
		        	MysqlRes.CodesResult coderes = MysqlX.setcodes_WRONGphone(res.C2a);
	            	if (coderes.calcDanishCode!=0) { 
		            	res.calcDanishCode = coderes.calcDanishCode;
		                res.intDanish = coderes.intDanish;
		    	    	update=true;
		    		}
				}
			    
			    //res.calcDanishCode = 5  mail (C1a>0)
		        /*depricated
		        if (res.C1a!=null ) {  
		        	MysqlRes.CodesResult coderes = MysqlX.setcodes_mail(res.C1a);
	            	if (coderes.calcDanishCode!=0) { 
		            	res.calcDanishCode = coderes.calcDanishCode;
		                res.intDanish = coderes.intDanish;
		    	    	update=true;
		    		}
				}*/
	        } 

	        /*** calculate 12 ***/
    		/*** update C3ef again ***/
		    if (!(res.C3e == null) && !res.C3e.isEmpty()) { 
		    	String s = MysqlX.findNew3ValToken(res.C3b);
		    	if (!res.C3e.equals(s)) {
		    		res.C3e = s;
		    		update = true;
			    }
		    }
		    if (!(res.C3f == null) && !res.C3f.isEmpty()) { 
		    	String s = MysqlX.findNew3ValToken(res.C3d);
		    	if (!res.C3f.equals(s)) {
		    		res.C3f = s;
		    		update = true;
			    }
		    }
    		
    		//3b,d, hvor små o oe ord er frasorteret -NYE         
	        if ((res.C3e!=null) && ( (!res.C3e.startsWith("0")) && (!res.C3e.isEmpty()) )) { 
	        	int c = MysqlX.setBit(18, res.calcDanishCode);
	        	if (c!=res.calcDanishCode) { 
	        		res.calcDanishCode = c;
	        		update=true;
	        	}
        	}
	    	if (!update) {
		        if ((res.C3f!=null) && ( (!res.C3f.startsWith("0")) && (!res.C3f.isEmpty()) )) { 
		        	int c = MysqlX.setBit(18, res.calcDanishCode);
		        	if (c!=res.calcDanishCode) { 
		        		res.calcDanishCode = c;
		        		update=true;
		        	}
	        	}
        	}
		    
	    	if (update) {
	            if (!updateLineUpdate11(conn, tablename, res)) {
                	skippedCount++; 
				} else {
	    			updateCount++;
        		}
			} else {
				ignoredCount++;
	    	}
	    } //while rs
        System.out.println("Processed " + recCount + " records");
        System.out.println("Updated " + updateCount + " records");
        System.out.println("Skipped " + skippedCount + " records");
        System.out.println("Ignored " + ignoredCount + " records");
        return (recCount!=0);
	}
    
    private static boolean update14subRs(Connection conn, String tablename, ResultSet rs, int code) throws SQLException {        
        long recCount=0L;
        long skippedCount=0L;
        long ignoredCount=0L;
        long updateCount = 0L;

        while (rs.next()) {
			recCount++;
			
			SingleCriteriaResult res = new SingleCriteriaResult(rs, false);
			boolean update = false;

			/** Correct 6 */
			if (res.calcDanishCode == 6) {
				res.calcDanishCode = 0;
				res.intDanish = 0F;
				update = true;

				//res.calcDanishCode = 5  mail (C1a>0)
				 /*depricated
				  * if (res.calcDanishCode==0 &&  res.C1a!=null ) {  
		        	MysqlRes.CodesResult coderes = new MysqlRes.CodesResult(); 
		        	coderes = MysqlX.setcodes_mail(res.C1a);
			    	if (coderes.calcDanishCode>0) {
			        	res.calcDanishCode = coderes.calcDanishCode;
			            res.intDanish = coderes.intDanish;
			            update = true;
					}
				}
				*/
	
		        if (!update && res.calcDanishCode==0) {
		        	int c = MysqlX.findNegativBitmapCalcCode(res);
		        	if (c!=res.calcDanishCode) {
		        		res.calcDanishCode = c;
		        		update=true;
		        	}
	        	}
	        }

			/** Find new values */
	        if (res.calcDanishCode<0) {
	        	//boolean checkMailPh = MysqlX.getBit((short)1, code)==1;
	        	/*boolean check_ae_oe_aa = MysqlX.getBit((short)2, code)==1;
	        	boolean checkLanguage = MysqlX.getBit((short)3, code)==1;
	        	
	        	/res.calcDanishCode = 7,8  tlf instead of 6
		        if (checkMailPh && res.C2a!=null && res.C5a!=null) {  
			    	MysqlRes.CodesResult coderes = new MysqlRes.CodesResult(); 
			    	coderes = MysqlX.setcodes_RIGHTphone(res.C2a, res.C5a, res.C5b);
			    	if (coderes.calcDanishCode>0) {
				    	res.calcDanishCode = coderes.calcDanishCode;
				        res.intDanish = coderes.intDanish;
				        update=true;
					}
				}*/
		        
	        	//res.calcDanishCode = 70-75  likely dk language (on æøå) (not norwegain)
	        	/* depricatedif (check_ae_oe_aa && res.C5a!=null) {  
			    	MysqlRes.CodesResult coderes = new MysqlRes.CodesResult(); 
			    	coderes = MysqlX.setcodes_languageDkletters(res.C3a, res.C3c, res.C5a, res.C5b);
			    	if (coderes.calcDanishCode>0) {
				    	res.calcDanishCode = coderes.calcDanishCode;
				        res.intDanish = coderes.intDanish;
				        update=true;
					}
				}

	        	//res.calcDanishCode = 76-77  likely dk language (not norwegain)
		        /* depricatedif (checkLanguage && res.C5a!=null) {  
			    	MysqlRes.CodesResult coderes = new MysqlRes.CodesResult(); 
			    	coderes = MysqlX.setcodes_languageDk(res.C4a, res.C5a, res.C5b, res.C);
			    	if (coderes.calcDanishCode>0) {
				    	res.calcDanishCode = coderes.calcDanishCode;
				        res.intDanish = coderes.intDanish;
				        update=true;
					}
				} */
	        }
	        
	    	if (update) {
	            if (!updateLineUpdate11(conn, tablename, res)) {
                	skippedCount++; 
				} else {
	    			updateCount++;
        		}
			} else {
				ignoredCount++;
	    	}
	    	
	    } //while rs
        System.out.println("Processed " + recCount + " records");
        System.out.println("Updated " + updateCount + " records");
        System.out.println("Skipped " + skippedCount + " records");
        System.out.println("Ignored " + ignoredCount + " records");
        return (recCount!=0);
	}

    private static void update15sub(Connection conn, String tablename, int limit) throws SQLException {
    	for (int i=0; i<=9; i++) {
    		String selectSQL = "SELECT Url, extWDate, calcDanishCode, intDanish, C3b, C3d, C3e, C3f, extDblChar FROM " + tablename 
        		+ " WHERE calcDanishCode= C3b LIKE '" + i + "%' AND C3e IS NULL";
        	int cnt = 0;
        	boolean moreRecs = true;
        	while (moreRecs) {
				cnt++;
        		String iSelectSQL = selectSQL + " LIMIT " + limit;
		        System.out.println("Processing " + iSelectSQL + " - cnt: " + cnt);
		        moreRecs = update15_3x(conn, tablename, iSelectSQL);
			} 
		} 
    	for (int i=0; i<=9; i++) {
    		String selectSQL = "SELECT Url, extWDate, calcDanishCode, intDanish, C3b, C3d, C3e, C3f, extDblChar FROM " + tablename 
        		+ " WHERE calcDanishCode= C3d LIKE '" + i + "%' AND C3f IS NULL";
        	int cnt = 0;
        	boolean moreRecs = true;
        	while (moreRecs) {
				cnt++;
        		String iSelectSQL = selectSQL + " LIMIT " + limit;
		        System.out.println("Processing " + iSelectSQL + " - cnt: " + cnt);
		        moreRecs = update15_3x(conn, tablename, iSelectSQL);
			} 
		} 
    }
    
    private static boolean update15_3x(Connection conn, String tablename, String sql) throws SQLException {
        long recCount=0L;
        long skippedCount=0L;
        long ignoredCount=0L;
        long updateCount = 0L;

        PreparedStatement stmt = conn.prepareStatement(sql); 
        ResultSet rs = stmt.executeQuery();
    	boolean update = false;
        while (rs.next()) {
			recCount++;
			update = false;
			CriteriaUpdate10 res = new CriteriaUpdate10(rs);
			if (res.C3b != null) { //added 9/9
		    	String s = MysqlX.findNew3ValToken(res.C3b);
			    if (!s.equals(res.C3e)) {
			    	res.C3e = s;
			    	if (res.calcDanishCode<0 && (res.C3e!=null) && ( (!res.C3e.startsWith("0")) && (!res.C3e.isEmpty()) ))  res.calcDanishCode = MysqlX.setBit(20, res.calcDanishCode);
			    	update = true;
			    }
		    }
		    if (!(res.C3d == null)) { //added 9/9
		    	String s = MysqlX.findNew3ValToken(res.C3d);
			    if (!s.equals(res.C3f)) {
			    	res.C3f = s;
			    	if (res.calcDanishCode<0 && (res.C3f!=null) && ( (!res.C3f.startsWith("0")) && (!res.C3f.isEmpty()) ))  
			    		res.calcDanishCode = MysqlX.setBit(20, res.calcDanishCode);
			    	update = true;
			    }
		    }

			if (update) {
	            if (!updateLineUpdate10(conn, tablename, res)) {
	            	skippedCount++; 
				} else {
	    			updateCount++;
	    		}
			} else {
				ignoredCount++;
	    	}
    	
	    } //while rs'
        rs.close();
        stmt.close();
	    System.out.println("Processed " + recCount + " records");
	    System.out.println("Updated " + updateCount + " records");
	    System.out.println("Skipped " + skippedCount + " records");
	    System.out.println("Ignored " + ignoredCount + " records");
	    return (recCount!=0);
    }
    
    private static boolean update15subRs(Connection conn, String tablename, ResultSet rs, int code) throws SQLException {
        long recCount=0L;
        long skippedCount=0L;
        long ignoredCount=0L;
        long updateCount = 0L;

        while (rs.next()) {
			recCount++;
			
			CriteriaUpdateCalcCode res = new CriteriaUpdateCalcCode(rs);
			boolean update = false;
		    
		    /** Find new values */
		    if (res.calcDanishCode<0) {
	        	code = res.calcDanishCode;
	        	code = MysqlX.unsetBit(10, code);
	        	code = MysqlX.unsetBit(14, code);
	        	code = MysqlX.unsetBit(17, code);
	        	code = MysqlX.unsetBit(18, code);
	        	if (code!=res.calcDanishCode) {
	        		res.calcDanishCode=code;
			    	update = true;
	        	}
		    }
        
	    	if (update) {
	            if (!updateLineUpdateCalcCode(conn, tablename, res)) {
	            	skippedCount++; 
				} else {
	    			updateCount++;
	    		}
			} else {
				ignoredCount++;
	    	}
	    	
	    } //while rs
	    System.out.println("Processed " + recCount + " records");
	    System.out.println("Updated " + updateCount + " records");
	    System.out.println("Skipped " + skippedCount + " records");
	    System.out.println("Ignored " + ignoredCount + " records");
	    return (recCount!=0);
    }

    private static void update16sub(Connection conn, String tablename, int limit) throws SQLException {
        System.out.println("Reset codes");
    	int code = 0;
    	for (int i=1; i<=8; i++) {
    		switch(i) {
    		  case 1: code =200; break;  
    		  case 2: code =201; break;
    		  case 3: code =202; break;
    		  case 4: code =203; break;
    		  case 5: code =74; break;
    		  case 6: code =75; break;
    		  case 7: code =7; break;
    		  case 8: code =8; break;
    		}
    		String sql = "SELECT * FROM " + tablename 
    			  + " WHERE calcDanishCode=" + code;
		        System.out.println("Processing " + sql);
		    update16_resetCode(conn, tablename, sql);
		} 
    	
        System.out.println("Update dblChar");
    	MysqlRes.updateDblChar130(conn, tablename);    	
    }

    private static boolean update16_resetCode(Connection conn, String tablename, String sql) throws SQLException {
        long recCount=0L;
        long skippedCount=0L;
        long ignoredCount=0L;
        long updateCount = 0L;

        PreparedStatement stmt = conn.prepareStatement(sql); 
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
			recCount++;
			SingleCriteriaResult res = new SingleCriteriaResult(rs, false);
			res.calcDanishCode=MysqlX.findNegativBitmapCalcCode(res);
			res.intDanish=0F;

			if (!updateLineUpdate11(conn, tablename, res)) {
            	skippedCount++; 
			} else {
    			updateCount++;
    		}
	    } //while rs
	    System.out.println("Processed " + recCount + " records");
	    System.out.println("Updated " + updateCount + " records");
	    System.out.println("Skipped " + skippedCount + " records");
	    System.out.println("Ignored " + ignoredCount + " records");
	    return (recCount!=0);
    }


    private static void update17subSet230(Connection conn, String tablename) throws SQLException {
        long recCount=0L;
        long skippedCount=0L;
        long ignoredCount=0L;
        long updateCount = 0L;
        
    	String sql = "SELECT * FROM " + tablename 
    			  + " WHERE (NOT C7g IS NULL) AND (C7g<>'') AND (C7g<>'0')";
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();

        Set<Integer> codeSet = MysqlX.getCodesForNOTDanishResults(); 
        while (rs.next()) {
			recCount++;
			boolean update = false;
			SingleCriteriaResult res = new SingleCriteriaResult(rs, true);
			
        	if (res.calcDanishCode<=0 || codeSet.contains(res.calcDanishCode)) {
				if (!( (res.C7g==null) || (res.C7g.isEmpty()) || (res.C7g.startsWith("0")) )) {
					res.calcDanishCode = 230;
					res.intDanish = 75;
					update=true;
	    		}
    		}
			if (update) {
				if (!updateLineUpdate11(conn, tablename, res)) {
	            	skippedCount++; 
				} else {
	    			updateCount++;
	    		}
			} else {
				ignoredCount++;
			}
	    } //while rs
	    System.out.println("Processed " + recCount + " records");
	    System.out.println("Updated " + updateCount + " records");
	    System.out.println("Skipped " + skippedCount + " records");
	    System.out.println("Ignored " + ignoredCount + " records");
    }
    
    private static void update17subSet5 (Connection conn, String tablename) throws SQLException {
    	long recCount=0L;
        long skippedCount=0L;
        long ignoredCount=0L;
        long updateCount = 0L;


    	String sql = "SELECT * FROM " + tablename 
  			  + " WHERE (NOT C1a IS NULL) AND (C1a<>'') AND (C1a<>'0')";
	    PreparedStatement stmt = conn.prepareStatement(sql);
	    ResultSet rs = stmt.executeQuery();

	    Set<Integer> codeSet = MysqlX.getCodesForNOTDanishResults(); 
	    codeSet.add(5);
	    while (rs.next()) {
			recCount++;
			boolean update = false;
			SingleCriteriaResult res = new SingleCriteriaResult(rs, false);
			
			if (res.calcDanishCode<=0 || codeSet.contains(res.calcDanishCode)) {
				if (!( (res.C1a==null) || (res.C1a.isEmpty()) || (res.C1a.startsWith("0")) )) {
					if ((res.C5b==null) || (res.C5b.isEmpty()) || (res.C5b.startsWith("0"))) {
						res.calcDanishCode = 206;
						res.intDanish = 85;
					} else {
						res.calcDanishCode = 207;
						res.intDanish = 80;
					}
					update=true;
	    		}
			}
			
			if (update) {
				if (!updateLineUpdate11(conn, tablename, res)) {
	            	skippedCount++; 
				} else {
	    			updateCount++;
	    		}
			} else {
				ignoredCount++;
			}
	    } //while rs
	    System.out.println("Processed " + recCount + " records");
	    System.out.println("Updated " + updateCount + " records");
	    System.out.println("Skipped " + skippedCount + " records");
	    System.out.println("Ignored " + ignoredCount + " records");
    }
    
    private static void update17subSet208_209(Connection conn, String tablename) throws SQLException {
    	long recCount=0L;
        long skippedCount=0L;
        long ignoredCount=0L;
        long updateCount = 0L;


    	String sql = "SELECT * FROM " + tablename 
  			  + " WHERE (NOT C2b IS NULL) AND (C2b<>'') AND (C2b<>'n')";
	    PreparedStatement stmt = conn.prepareStatement(sql);
	    ResultSet rs = stmt.executeQuery();

	    Set<Integer> codeSet = MysqlX.getCodesForNOTDanishResults(); 
	    while (rs.next()) {
			recCount++;
			boolean update = false;
			SingleCriteriaResult res = new SingleCriteriaResult(rs, true);
			
			if (res.calcDanishCode<=0 || codeSet.contains(res.calcDanishCode)) {
				if ( (res.C2b!=null) && (!res.C2b.isEmpty()) && (!res.C2b.startsWith("n")) ) {
					if ((res.C5b==null) || (res.C5b.isEmpty()) || (res.C5b.startsWith("0"))) {
						res.calcDanishCode = 208;
						res.intDanish = 85;
					} else {
						res.calcDanishCode = 209;
						res.intDanish = 80;
					}
					update=true;
	    		}
			}
			if (update) {
				if (!updateLineUpdate11(conn, tablename, res)) {
	            	skippedCount++; 
				} else {
	    			updateCount++;
	    		}
			} else {
				ignoredCount++;
			}
	    } //while rs
	    System.out.println("Processed " + recCount + " records");
	    System.out.println("Updated " + updateCount + " records");
	    System.out.println("Skipped " + skippedCount + " records");
	    System.out.println("Ignored " + ignoredCount + " records");
    }
    
    private static boolean update17subRs(Connection conn, String tablename, ResultSet rs, int code) throws SQLException {
    	//unlike dk
    	//lav nye 30'ere, 50'ere
    	//		300	NOT likely dk – på nye felter
    	//lav nye 200-203: 
    	//      301	NOT likely dk CHECK unions
    	//      302	NOT likely dk CHECK companies

        long recCount=0L;
        long skippedCount=0L;
        long ignoredCount=0L;
        long updateCount = 0L;

        while (rs.next()) {
			recCount++;
			
			SingleCriteriaResult res = new SingleCriteriaResult(rs, true);
			boolean update = false;
		    
		    /** Find new values */
		    if (res.calcDanishCode<0) {
		    	CodesResult cr = MysqlX.setcodes_notDkLanguageVeryLikelyNewFields(res, NotDkExceptions.noException); //get calcode and IntDanish  and check in depth
		    	if (cr.calcDanishCode>0) {
		    		res.calcDanishCode = cr.calcDanishCode;
		    		res.intDanish = cr.intDanish;
			    	update = true;
	        	}
		    }
        
	    	if (update) {
	            if (!updateLineUpdate11(conn, tablename, res)) {
	            	skippedCount++; 
				} else {
	    			updateCount++;
	    		}
			} else {
				ignoredCount++;
	    	}
	    	
	    } //while rs
	    System.out.println("Processed " + recCount + " records");
	    System.out.println("Updated " + updateCount + " records");
	    System.out.println("Skipped " + skippedCount + " records");
	    System.out.println("Ignored " + ignoredCount + " records");
	    return (recCount!=0);
    }

    private static void update18languageTikka(Connection conn, String tablename, boolean extendedNewHadoopTable) throws SQLException {
    	long recCount=0L;
        long skippedCount=0L;
        long updateCount = 0L;

        for (int code=76; code<=79; code++) {
	    	String sql = "SELECT * FROM " + tablename 
	  			  + " WHERE calcDanishCode=" + code;
		    PreparedStatement stmt = conn.prepareStatement(sql);
		    ResultSet rs = stmt.executeQuery();
	
		    while (rs.next()) {
				recCount++;
				SingleCriteriaResult res = new SingleCriteriaResult(rs, extendedNewHadoopTable);
				
				CodesResult cr = MysqlX.setcodes_languageDkNew(res.C4a, res.C5a, res.C5b, res.C15b);
				if (cr.calcDanishCode>0) {
					res.calcDanishCode = cr.calcDanishCode ;
					res.intDanish = cr.intDanish;
				} else {
		        	res.calcDanishCode = MysqlX.findNegativBitmapCalcCode(res);
	    		}

				if (!updateLineUpdate11(conn, tablename, res)) {
	            	skippedCount++; 
				} else {
	    			updateCount++;
	    		}
		    } //while rs
		    rs.close();
		}
	    System.out.println("Processed " + recCount + " records");
	    System.out.println("Updated " + updateCount + " records");
	    System.out.println("Skipped " + skippedCount + " records");
    }

    private static void update18languageaeoeaa(Connection conn, String tablename, boolean extendedNewHadoopTable) throws SQLException {
    	long recCount=0L;
        long skippedCount=0L;
        long updateCount = 0L;

        for (int code=70; code<=73; code++) {
	    	String sql = "SELECT * FROM " + tablename 
	  			  + " WHERE calcDanishCode=" + code;
		    PreparedStatement stmt = conn.prepareStatement(sql);
		    ResultSet rs = stmt.executeQuery();
	
		    while (rs.next()) {
				recCount++;
				SingleCriteriaResult res = new SingleCriteriaResult(rs, extendedNewHadoopTable);
				
				CodesResult cr = MysqlX.setcodes_languageDklettersNew(res.C3a, res.C5a, res.C5b, res.C15b);
				if (cr.calcDanishCode>0) {
					res.calcDanishCode = cr.calcDanishCode ;
					res.intDanish = cr.intDanish;
				} else {
		        	res.calcDanishCode = MysqlX.findNegativBitmapCalcCode(res);
	    		}

				if (!updateLineUpdate11(conn, tablename, res)) {
	            	skippedCount++; 
				} else {
	    			updateCount++;
	    		}
		    } //while rs
		    rs.close();
		}
	    System.out.println("Processed " + recCount + " records");
	    System.out.println("Updated " + updateCount + " records");
	    System.out.println("Skipped " + skippedCount + " records");
    }
    
    private static void update18tld(Connection conn, String tablename) throws SQLException {
    	String sql = "UPDATE " + tablename + " " ;
    	sql = sql  + "SET calcDanishCode=3 WHERE C15b='dk'";
    	PreparedStatement s = conn.prepareStatement(sql); 
	    s.executeUpdate();
	    s.close();
    }

    private static void update18mail(Connection conn, String tablename, boolean extendedNewHadoopTable) throws SQLException {
    	long recCount=0L;
        long skippedCount=0L;
        long ignoredCount=0L;
        long updateCount = 0L;

    	String sql = "SELECT * FROM " + tablename 
  			  + " WHERE calcDanishCode=206 OR calcDanishCode=207";
	    PreparedStatement stmt = conn.prepareStatement(sql);
	    ResultSet rs = stmt.executeQuery();

	    while (rs.next()) {
			recCount++;
			boolean update = false;
			SingleCriteriaResult res = new SingleCriteriaResult(rs, extendedNewHadoopTable);
			
			CodesResult cr = MysqlX.setcodes_mail(res.C1a, res.C5a, res.C5b, res.C15b, res.C7g);
			if (cr.calcDanishCode>0) {
				res.calcDanishCode = cr.calcDanishCode ;
				res.intDanish = cr.intDanish;
				update=true;
			}

			if (update) {
				if (!updateLineUpdate11(conn, tablename, res)) {
	            	skippedCount++; 
				} else {
	    			updateCount++;
	    		}
			} else {
				ignoredCount++;
			}
	    } //while rs
	    System.out.println("Processed " + recCount + " records");
	    System.out.println("Updated " + updateCount + " records");
	    System.out.println("Skipped " + skippedCount + " records");
	    System.out.println("Ignored " + ignoredCount + " records");
    }

    private static void update18oldphone(Connection conn, String tablename, boolean extendedNewHadoopTable) throws SQLException {
    	long recCount=0L;
        long skippedCount=0L;
        long ignoredCount=0L;
        long updateCount = 0L;

    	String sql = "SELECT * FROM " + tablename 
  			  + " WHERE (C2a LIKE '%tlf%+45%' or C2a LIKE '%+45%tlf%') AND calcDanishCode<0";
	    PreparedStatement stmt = conn.prepareStatement(sql);
	    ResultSet rs = stmt.executeQuery();

	    while (rs.next()) {
			recCount++;
			boolean update = false;
			SingleCriteriaResult res = new SingleCriteriaResult(rs, extendedNewHadoopTable);
			
			CodesResult cr = MysqlX.setcodes_oldPhone(res.C2a,res.C5a,res.C5b, res.C15b); 
			if (cr.calcDanishCode>0) {
				res.calcDanishCode = cr.calcDanishCode ;
				res.intDanish = cr.intDanish;
				update=true;
			}

			if (update) {
				if (!updateLineUpdate11(conn, tablename, res)) {
	            	skippedCount++; 
				} else {
	    			updateCount++;
	    		}
			} else {
				ignoredCount++;
			}
	    } //while rs
	    System.out.println("Processed " + recCount + " records");
	    System.out.println("Updated " + updateCount + " records");
	    System.out.println("Skipped " + skippedCount + " records");
	    System.out.println("Ignored " + ignoredCount + " records");
    }
    
    private static void update18newphone(Connection conn, String tablename) throws SQLException {
    	long recCount=0L;
        long skippedCount=0L;
        long ignoredCount=0L;
        long updateCount = 0L;

    	String sql = "SELECT * FROM " + tablename 
  			  + " WHERE (C2b='y')";
	    PreparedStatement stmt = conn.prepareStatement(sql);
	    ResultSet rs = stmt.executeQuery();
	    Set<Integer> codeSet = MysqlX.getCodesForNOTDanishResults(); 

	    while (rs.next()) {
			recCount++;
			boolean update = false;
			SingleCriteriaResult res = new SingleCriteriaResult(rs, true);
			
			if (res.calcDanishCode<=0 || codeSet.contains(res.calcDanishCode)) {
		    	CodesResult cr = MysqlX.setcodes_newPhone(res.C2b,res.C5a,res.C5b, res.C15b); 
				if (cr.calcDanishCode>0) {
					res.calcDanishCode = cr.calcDanishCode ;
					res.intDanish = cr.intDanish;
					update=true;
				}
			}

			if (update) {
				if (!updateLineUpdate11(conn, tablename, res)) {
	            	skippedCount++; 
				} else {
	    			updateCount++;
	    		}
			} else {
				ignoredCount++;
			}
	    } //while rs
	    System.out.println("Processed " + recCount + " records");
	    System.out.println("Updated " + updateCount + " records");
	    System.out.println("Skipped " + skippedCount + " records");
	    System.out.println("Ignored " + ignoredCount + " records");
    }
    
    private static long update19code0(Connection conn, String tablename, boolean extendedNewHadoopTable) throws SQLException {
    	long recCount=0L;
        long skippedCount=0L;
        long ignoredCount=0L;
        long updateCount = 0L;

    	String sql = "SELECT * FROM " + tablename 
  			  + " WHERE calcDanishCode=0 LIMIT 50000";
	    PreparedStatement stmt = conn.prepareStatement(sql);
	    ResultSet rs = stmt.executeQuery();

	    while (rs.next()) {
			recCount++;
			boolean update = false;
			SingleCriteriaResult res = new SingleCriteriaResult(rs, extendedNewHadoopTable);
			
        	res.calcDanishCode = MysqlX.findNegativBitmapCalcCode(res);
        	update = res.calcDanishCode !=0;

        	if (update) {
				if (!updateLineUpdate11(conn, tablename, res)) {
	            	skippedCount++; 
				} else {
	    			updateCount++;
	    		}
			} else {
				ignoredCount++;
			}
	    } //while rs
	    rs.close();
	    System.out.println("Processed " + recCount + " records");
	    System.out.println("Updated " + updateCount + " records");
	    System.out.println("Skipped " + skippedCount + " records");
	    System.out.println("Ignored " + ignoredCount + " records");
	    return recCount;
    }
    
	private static void update20_notlikelydk(Connection conn, String tablename, Set<Integer> codeSet, boolean extendedNewHadoopTable) throws SQLException  {
	   	 // 111	dk language æøå in html --  noske ord + se or no tld - forfra –kommer senere

		System.out.println("Number of codes " + codeSet.size());
	    for (int code : codeSet) {
	        System.out.println("Processing " + code);
	        String sql = "SELECT * FROM " + tablename + " WHERE "
	        		+ "calcDanishCode=" + code;
	        PreparedStatement stmt = conn.prepareStatement(sql); 
	        ResultSet rs = stmt.executeQuery();

	        long recCount=0L;
	        long skippedCount=0L;
	        long ignoredCount=0L;
	        long updateCount = 0L;

	        while (rs.next()) {
				recCount++;
				SingleCriteriaResult res = new SingleCriteriaResult(rs, extendedNewHadoopTable);
				CodesResult cr = MysqlX.setcodes_notDkLanguageVeryLikely(res);
				boolean update = false;
	   			if (cr.calcDanishCode!=res.calcDanishCode) {
	   				res.calcDanishCode = cr.calcDanishCode;
	   				res.intDanish = cr.intDanish;
	   				update=true;
	   			}
	   			if (update) {
	   				if (!updateLineUpdate11(conn, tablename, res)) {
	   	            	skippedCount++; 
	   				} else {
	   	    			updateCount++;
	   	    		}
	   			} else {
	   				ignoredCount++;
	   			}
	   	    } //while rs
	   	    System.out.println("Processed " + recCount + " records");
	   	    System.out.println("Updated " + updateCount + " records");
	   	    System.out.println("Skipped " + skippedCount + " records");
	   	    System.out.println("Ignored " + ignoredCount + " records");
	        rs.close();
      }
  }

	private static void update20_100sfromCodeSet(Connection conn, String tablename, Set<Integer> codeSet, int updatecode, boolean extendedNewHadoopTable) throws SQLException  {
	   	 // 111	dk language æøå in html --  noske ord + se or no tld - forfra –kommer senere

		System.out.println("Number of codes " + codeSet.size());
	    for (int code : codeSet) {
	        System.out.println("Processing " + code);
	        String sql = "SELECT * FROM " + tablename + " WHERE "
	        		+ "calcDanishCode=" + code
   				+ " AND (C15b ='no' OR C15b ='sv')";
	        PreparedStatement stmt = conn.prepareStatement(sql); 
	        ResultSet rs = stmt.executeQuery();

	        long recCount=0L;
	        long skippedCount=0L;
	        long ignoredCount=0L;
	        long updateCount = 0L;

	        while (rs.next()) {
				recCount++;
				SingleCriteriaResult res = new SingleCriteriaResult(rs, extendedNewHadoopTable);
				CodesResult cr = MysqlX.setcodes_languageDkNew(res.C4a, res.C5a, res.C5b, res.C15b);
				boolean update = false;
				if (updatecode==110) {
					cr = MysqlX.setcodes_languageDklettersNew(res.C3a, res.C5a, res.C5b, res.C15b);
				} else if (updatecode==120) {
					cr = MysqlX.setcodes_languageDkNew(res.C4a, res.C5a, res.C5b, res.C15b);
				} else {
					System.out.println("EROOR worng code " + code);
				}
	   			if (cr.calcDanishCode!=res.calcDanishCode || cr.intDanish!=res.intDanish) {
	   				res.calcDanishCode = cr.calcDanishCode;
	   				res.intDanish = cr.intDanish;
	   				update=true;
	   			}

	   			if (update) {
	   				if (!updateLineUpdate11(conn, tablename, res)) {
	   	            	skippedCount++; 
	   				} else {
	   	    			updateCount++;
	   	    		}
	   			} else {
	   				ignoredCount++;
	   			}
	   	    } //while rs
	   	    System.out.println("Processed " + recCount + " records");
	   	    System.out.println("Updated " + updateCount + " records");
	   	    System.out.println("Skipped " + skippedCount + " records");
	   	    System.out.println("Ignored " + ignoredCount + " records");
	        rs.close();
       }
   }
    private static void update20nosvtlderror300s(Connection conn, String tablename, boolean extendedNewHadoopTable, int code) throws SQLException {
        // 310 	C2b tlf/ph  noske ord + se or no tld	Er i 317 –kommer senere 
        // 315	C2a has +45 and tlf/ph  noske ord + se or no tld	Er i 317 –kommer senere 
        // 320,C1a mails noske ord + se or no tld	Er i 323,324 –kommer senere
       	long recCount=0L;
        long skippedCount=0L;
        long ignoredCount=0L;
        long updateCount = 0L;
        
    	String sql = "SELECT * FROM " + tablename + " WHERE (calcDanishCode=" + code + ")"; //312, 317, 323, 324
       	PreparedStatement stmt = conn.prepareStatement(sql);
   	    ResultSet rs = stmt.executeQuery();

   	    while (rs.next()) {
   			recCount++;
   			boolean update = false;
   			CodesResult cr = new CodesResult();
   			SingleCriteriaResult res = new SingleCriteriaResult(rs, extendedNewHadoopTable);
   			if (code==317) {
   	   			cr = MysqlX.setcodes_oldPhone(res.C2a, res.C5a, res.C5b, res.C15b);
   			} else if (code==312) {
   				cr = MysqlX.setcodes_newPhone(res.C2b, res.C5a, res.C5b, res.C15b);
			} else if (code==323 || code==324 ) {
   				cr = MysqlX.setcodes_mail(res.C1a, res.C5a, res.C5b, res.C15b, res.C7g);
   			}
   			
   			if (cr.calcDanishCode!=res.calcDanishCode || cr.intDanish!=res.intDanish) {
   				res.calcDanishCode = cr.calcDanishCode;
   				res.intDanish = cr.intDanish;
   				update=true;
   			}

   			if (update) {
   				if (!updateLineUpdate11(conn, tablename, res)) {
   	            	skippedCount++; 
   				} else {
   	    			updateCount++;
   	    		}
   			} else {
   				ignoredCount++;
   			}
   	    } //while rs
   	    System.out.println("Processed " + recCount + " records");
   	    System.out.println("Updated " + updateCount + " records");
   	    System.out.println("Skipped " + skippedCount + " records");
   	    System.out.println("Ignored " + ignoredCount + " records");
   	 }

    //update21
    // bit? arabic 
    //500	Domain TLD in berley
	//501	Domain TLD + next in berley
    //old phone in not danish codes?
    //*** calculate new C3e and C3f ***/ on NOT __UPDATE15??? elelr omvendt
    //if (!(res.C3b == null)) { //added 9/9
	//    res.C3e = MysqlX.findNew3ValToken(res.C3b);  update = true; }
    //if (!(res.C3d == null)) { //added 9/9 res.C3f = MysqlX.findNew3ValToken(res.C3d); update = true; }

    private static boolean updateBerDbsubRs(Connection conn, String tablename, ResultSet rs, int code) throws SQLException {        
        long recCount=0L;
        long skippedCount=0L;
        long ignoredCount=0L;
        long updateCount = 0L;

        while (rs.next()) {
			recCount++;
			
			CriteriaUpdateC16a res = new CriteriaUpdateC16a(rs);
			boolean update = false;
	
			if (res.C16a!=null && !res.C16a.isEmpty()) {//else not interesting due too other parameters 0-size or double char
				String url = (res.urlOrig==null || res.urlOrig.isEmpty() ? res.url : res.urlOrig);
				int berNo = MysqlRes.urlInBerkley(url, linksBase);
				//System.out.println("url: " + url + " --- " + berNo);
				if (berNo==0 && url.endsWith("/")) {
					url = url.substring(0,url.length()-1);
					//System.out.println("url: " + url + " --- " + berNo);
				}
			    int actC16Val = Integer.parseInt(res.C16a); 
		    	if (berNo != 0) {  
		    		int newC16Val = actC16Val + berNo;
		        	res.C16a = ("" + newC16Val).trim();
		        	if ((code < 0) && (MysqlX.getBit((short)8, code)==0)) {
		        		MysqlX.setBit(8, res.calcDanishCode);
		    		}
	    	    	update=true;
				}
			}
	    	if (update) {
	            if (!updateLineUpdateBerDb(conn, tablename, res)) {
                	skippedCount++; 
				} else {
	    			updateCount++;
        		}
			} else {
				ignoredCount++;
	    	}
	    	
	    } //while rs
        System.out.println("Processed " + recCount + " records");
        System.out.println("Updated " + updateCount + " records");
        System.out.println("Skipped " + skippedCount + " records");
        System.out.println("Ignored " + ignoredCount + " records");
        return (recCount!=0);
	}

    private static void turkeyUpdate(Connection conn, String tablename) throws SQLException {
        long recCount=0L;
        long skippedCount=0L;
        long ignoredCount=0L;
        long updateCount = 0L;

        String selectSQL = "SELECT * FROM " + tablename + " WHERE calcDanishCode=12";
        PreparedStatement s = conn.prepareStatement(selectSQL); 
        ResultSet rs = s.executeQuery();
    	while (rs.next()) {
			recCount++;
			if (rs.getString("C4a").equals("tr")) {
				int actcode=0;
          		SingleCriteriaResult res = new SingleCriteriaResult(rs, false);

          		if (actcode==0 && res.Cext1>200) {
                	boolean changed = MysqlX.setcodes_dkLanguageVeryLikely(res); //get calcode and IntDanish and check in depth
                	if (changed) {
                		actcode = res.calcDanishCode;
                        if (!MysqlRes.updateLineCalcDanishCodeAndIntDanish(conn, tablename, actcode, res.intDanish, res.url, res.Cext3 )) {
                        	skippedCount++; 
        				} else {
        	    			updateCount++;
                		}
        			}
                }
          		if (actcode==0 && res.Cext1>200) {
          			CodesResult cr = MysqlX.setcodes_notDkLanguageVeryLikely(res); //get calcode and IntDanish  and check in depth
                	if (res.calcDanishCode!=cr.calcDanishCode) {
                		actcode = res.calcDanishCode;
                        if (!MysqlRes.updateLineCalcDanishCodeAndIntDanish(conn, tablename, actcode, res.intDanish, res.url, res.Cext3 )) {
                        	skippedCount++; 
        				} else {
        	    			updateCount++;
                		}
        			}
    			}

          		if (actcode==0) {
          			actcode = MysqlX.findNegativBitmapCalcCode(res); //convert to new version with negative bitmap 
          			if (actcode!=0) {
                        if (!MysqlRes.updateLineCalcDanishCode(conn, tablename, actcode, res.url, res.Cext3 )) {
                        	skippedCount++; 
        				} else {
        	    			updateCount++;
                		}
        			}  else {
        				ignoredCount++;	  // still 0   	
        			}
    			}
			} // if turkish else ignore
		}
	    s.close();
        System.out.println("Processed " + recCount + " records");
        System.out.println("Updated " + updateCount + " records");
        System.out.println("Skipped " + skippedCount + " records");
        System.out.println("Ignored " + ignoredCount + " records");
    }

    private static void setNegativeCalcDanishCode(Connection conn, String tablename, int calcDanishCode, boolean extendedNewHadoop) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
        long recCount=0L;
        long skippedCount=0L;
        long ignoredCount=0L;

        String selectSQL = "SELECT * FROM " + tablename + " WHERE calcDanishCode=0";
        PreparedStatement s = conn.prepareStatement(selectSQL); 
        ResultSet rs = s.executeQuery();
    	while (rs.next()) {
			recCount++;
	        SingleCriteriaResult res = new SingleCriteriaResult(rs, extendedNewHadoop);
			res.calcDanishCode=  MysqlX.findNegativBitmapCalcCode(res); //convert to new version with negative bitmap 
			if  (res.calcDanishCode==0) { // no changes
				ignoredCount++; 
			} else if (!MysqlRes.updateLineCalcDanishCode(conn, tablename, res.calcDanishCode, res.url, res.Cext3)) {
				skippedCount++;
			//} else { System.out.println("Updated 0 to " + res.calcDanishCode);
			}
		}
	    s.close();
        
        System.out.println("Processed " + recCount + " records");
        System.out.println("Skipped " + skippedCount + " records");
        System.out.println("Ignored " + ignoredCount + " records");
	}

    private static void processCalcDanishCountAndIntDanish (Connection conn, String tablename, String sql, int inputCalcCode, UpdateOperation updateOp, Counters cr,boolean extendedNewHadoopTable) throws SQLException {
        PreparedStatement s = conn.prepareStatement(sql); 
        ResultSet rs = s.executeQuery();
        while (rs.next()) {
        	cr.recCount++;
			//System.out.println("url " +  rs.getString("Url"));
			//System.out.println("recCount " +  cr.recCount);

        	long extSize = rs.getLong("extSize");
            int actcode = 0;
            

            if ((actcode==0) && (updateOp==UpdateOperation.calcDkcodeAll || inputCalcCode==1)	&& extSize==0) { //zeroSize
          		actcode=1;
                if (!MysqlRes.updateLineCalcDanishCode(conn, tablename, actcode, rs.getString("Url"),  rs.getTimestamp("extWDate"))) {
        			cr.skippedCount++; 
				} else {
	    			cr.updateCount++;
        		}
	    	} 
          	if ((actcode==0) && (updateOp==UpdateOperation.calcDkcodeAll || inputCalcCode==2) && rs.getLong("extDblChar")>=200)  { //doubleBytes
          		actcode=2;
	            if (!MysqlRes.updateLineCalcDanishCode(conn, tablename, actcode, rs.getString("Url"), rs.getTimestamp("extWDate"))) {
	            	cr.skippedCount++; 
				} else {
	    			cr.updateCount++;
        		}
	    	}
          	if ((actcode==0) && (updateOp==UpdateOperation.calcDkcodeAll || inputCalcCode==3) && rs.getString("C15b").equals("dk"))  {
          		actcode=3;
	            if (!MysqlRes.updateLineCalcDanishCode(conn, tablename, actcode, rs.getString("Url"),  rs.getTimestamp("extWDate"))) {
	            	cr.skippedCount++; 
				} else {
	    			cr.updateCount++;
        		}
	    	} 
          	if ((actcode==0) && (updateOp==UpdateOperation.calcDkcodeAll || (inputCalcCode>=100 && inputCalcCode<=107)) && extSize<=200) { //unstableSmallSizes n-gram<>dk and n-gram<>se and n-gram<>no and...
            	CodesResult coderes = new  CodesResult();

            	coderes = MysqlX.setcodes_smallSize(rs.getString("C4a"), 
          				rs.getString("C3a"), rs.getString("C3b"), rs.getString("C3c"), rs.getString("C3d"), 
          				rs.getString("C6a"), rs.getString("C6b"), rs.getString("C6c"));
            	actcode = (int) coderes.calcDanishCode;

            	if ( (actcode!=0) && 
            			(updateOp==UpdateOperation.calcDkcodeAll 
            			|| updateOp==UpdateOperation.calcDkcodeInterval
            			|| actcode==inputCalcCode) ) {	    		
    	            if (!MysqlRes.updateLineCalcDanishCodeAndIntDanish(conn, tablename, actcode, coderes.intDanish, rs.getString("Url"),rs.getTimestamp("extWDate"))) {
    					cr.skippedCount++; 
    				} else {
    	    			cr.updateCount++;
            		}
    			} else { // some details did not match actcode
    				actcode=0;
    			}
	    	} 
          	if ((actcode==0) && (updateOp==UpdateOperation.calcDkcodeAll || (inputCalcCode>=10 && inputCalcCode<=12)) && extSize>200) { //other languages 
	    		//language codes from http://www.loc.gov/standards/iso639-2/php/code_list.php
            	CodesResult coderes = new CodesResult();
        		coderes = MysqlX.setcodes_otherLanguagesChars(rs.getString("C4a"));
            	actcode = (int) coderes.calcDanishCode;
            	if ( (actcode!=0) &&
            			(updateOp==UpdateOperation.calcDkcodeAll 
            			|| updateOp==UpdateOperation.calcDkcodeInterval
            			|| actcode==inputCalcCode)) {	    		
    	            if (!MysqlRes.updateLineCalcDanishCodeAndIntDanish(conn, tablename, actcode, coderes.intDanish, rs.getString("Url"),rs.getTimestamp("extWDate"))) {
    	            	cr.skippedCount++; 
    				} else {
    	    			cr.updateCount++;
            		}
    			} else { // some details did not match actcode
    				actcode=0;
    			}
          	}

          	if (actcode==0) { 
          		SingleCriteriaResult res = new SingleCriteriaResult(rs, extendedNewHadoopTable);

          		if ((actcode==0 && extSize>200) && 
          			(  updateOp==UpdateOperation.calcDkcodeAll || 
     					  (inputCalcCode>=20 && inputCalcCode<=27) || (inputCalcCode>=40 && inputCalcCode<=47))) 
	          	{
                	boolean changed = MysqlX.setcodes_dkLanguageVeryLikely(res); //get calcode and IntDanish and check in depth
                	if (changed && 
                			(updateOp==UpdateOperation.calcDkcodeAll 
                			|| updateOp==UpdateOperation.calcDkcodeInterval
                			|| actcode==inputCalcCode)) 
                	{
                		actcode = res.calcDanishCode;
                        if (!MysqlRes.updateLineCalcDanishCodeAndIntDanish(conn, tablename, actcode, res.intDanish, res.url, res.Cext3 )) {
                        	cr.skippedCount++; 
        				} else {
        	    			cr.updateCount++;
                		}
        			}
                }
     			
          		if ((actcode==0 && extSize>200) && 
          				( updateOp==UpdateOperation.calcDkcodeAll || (inputCalcCode>=30 && inputCalcCode<=31) 
		   		         || (inputCalcCode>=50 && inputCalcCode<=51)))  
	          	{
          			CodesResult cr2 = MysqlX.setcodes_notDkLanguageVeryLikely(res); //get calcode and IntDanish  and check in depth
                	if (cr2.calcDanishCode!=res.calcDanishCode && 
                			(updateOp==UpdateOperation.calcDkcodeAll 
                			|| updateOp==UpdateOperation.calcDkcodeInterval
                			|| actcode==inputCalcCode)) 
                	{
                		actcode = res.calcDanishCode;
                        if (!MysqlRes.updateLineCalcDanishCodeAndIntDanish(conn, tablename, actcode, res.intDanish, res.url, res.Cext3 )) {
                        	cr.skippedCount++; 
        				} else {
        	    			cr.updateCount++;
                		}
        			}
    			}

          		if (actcode==0 && res.calcDanishCode== 0 && (updateOp==UpdateOperation.calcDkcodeAll)) {
          			actcode = MysqlX.findNegativBitmapCalcCode(res); //convert to new version with negative bitmap 

          			if (actcode!=0) {
                        if (!MysqlRes.updateLineCalcDanishCode(conn, tablename, actcode, res.url, res.Cext3 )) {
                        	cr.skippedCount++; 
        				} else {
        	    			cr.updateCount++;
                		}
        			}  else {
        				cr.ignoredCount++;	  // still 0   	
        			}
				} else {
	          		if (actcode==0) { // no changes
	          			cr.ignoredCount++;	 
    				}
				}
	        }
        } // rs loop
        rs.close();
        System.out.println("Processed " + cr.recCount + " lines");
        System.out.println("Updated " + cr.recCount + " lines");
        System.out.println("Skipped " + cr.skippedCount + " lines");
        System.out.println("Ignored " + cr.ignoredCount + " lines");
    }
    
    private static void c15bUpdate(Connection conn, String tablename) throws SQLException {
        long recCount=0L;
        long skippedCount=0L;
		String selectSQL = "SELECT Url, extWDate FROM " + tablename + " " // <= 2 means it is "0 " or "0"
				+ "WHERE calcDanishCode<=0"
				+ " AND C15b=''";
        PreparedStatement s = conn.prepareStatement(selectSQL); 
        ResultSet rs = s.executeQuery();
        while (rs.next()) {
			recCount++;
			String url = rs.getString("Url");
			String tld = MysqlX.findTLD(url);
			if (tld.isEmpty() || tld.length() >= 6) {
				tld = "-"; 
			}
			if (!updateLineC15b(conn, tablename, tld, url, rs.getTimestamp("extWDate"))) {
				skippedCount++; 
			}
		    s.close();
        }
        System.out.println("Processed " + recCount + " records");
        System.out.println("Skipped " + skippedCount + " records");    
    }
    
    private static void c8bUpdate(Connection conn, String tablename) throws SQLException {
        long recCount=0L;
        long skippedCount=0L;
        long ignoredCount=0L;
		String selectSQL = "SELECT Url, extWDate, C8a FROM " + tablename + " " // <= 2 means it is "0 " or "0"
				+ "WHERE calcDanishCode<=0"
				+ " AND C8a=C8b";
        PreparedStatement s = conn.prepareStatement(selectSQL); 
        ResultSet rs = s.executeQuery();
        while (rs.next()) {
			recCount++;
			String url = rs.getString("Url");
			String c8bVal = MysqlX.find8bVal(url);
			if (c8bVal.equals(rs.getString("C8a").trim()))  {
				ignoredCount++;
			} else {
				if (!updateLineC8b(conn, tablename, c8bVal, url, rs.getTimestamp("extWDate"))) {
					skippedCount++; 
				}
			}
        }
	    s.close();
        
        System.out.println("Processed " + recCount + " records");
        System.out.println("Skipped " + skippedCount + " records");    
        System.out.println("Ignored " + ignoredCount + " records");    
    }
    
    private static boolean updateLineUpdateCalcCode(Connection conn, String tablename, CriteriaUpdateCalcCode res) throws SQLException {
	    PreparedStatement s = conn.prepareStatement("UPDATE " + tablename + " " 
	            + "SET"
	            + " calcDanishCode = ? "
	    		+ "WHERE Url = ? AND extWDate = ?" 
	           ); 
	    int index = 1;
	    s.setInt(index, res.calcDanishCode);
	    index++;
	    s.setString(index, res.url);
	    index++;
	    s.setTimestamp(index, res.extWDate);
	    
	    s.executeUpdate();
	    s.close();
	    return true;
    }

    private static boolean updateLineUpdate10(Connection conn, String tablename, CriteriaUpdate10 res) throws SQLException {
	    PreparedStatement s = conn.prepareStatement("UPDATE " + tablename + " " 
	            + "SET"
	            + " C3e = ?, "
	            + " C3f = ?, "
	            + " calcDanishCode = ?, "
	            + " intDanish = ? "
	    		+ "WHERE Url = ? AND extWDate = ?" 
	           ); 
	    int index = 1;
	    if (res.C3e==null) s.setNull(index, java.sql.Types.VARCHAR);
	    else s.setString(index, res.C3e);
	    index++;
	    if (res.C3f==null) s.setNull(index, java.sql.Types.VARCHAR);
	    else s.setString(index, res.C3f);
	    
	    index++;
	    s.setInt(index, res.calcDanishCode);
	    index++;
	    s.setFloat(index, res.intDanish);
	    
	    index++;
	    s.setString(index, res.url);
	    index++;
	    s.setTimestamp(index, res.extWDate);
	    
	    s.executeUpdate();
	    s.close();
	    return true;
    }

    private static boolean updateLineUpdate11(Connection conn, String tablename, SingleCriteriaResult res) throws SQLException {
	    PreparedStatement s = conn.prepareStatement("UPDATE " + tablename + " " 
	            + "SET"
	            + " calcDanishCode = ?, "
	            + " intDanish = ? "
	    		+ "WHERE Url = ? AND extWDate = ?" 
	           ); 
	    int index = 1;
	    s.setInt(index, res.calcDanishCode);
	    index++;
	    s.setFloat(index, res.intDanish);
	    
	    index++;
	    s.setString(index, res.url);
	    index++;
	    s.setTimestamp(index, res.Cext3);
	    
	    s.executeUpdate();
	    s.close();
	    return true;
    }

    private static boolean updateLineUpdateBerDb(Connection conn, String tablename, CriteriaUpdateC16a res) throws SQLException {
	    PreparedStatement s = conn.prepareStatement("UPDATE " + tablename + " " 
	            + "SET"
	            + " C16a = ?,"
	            + " calcDanishCode = ? "
	    		+ "WHERE Url = ? AND extWDate = ?" 
	           ); 
	    int index = 1;
	    s.setString(index, res.C16a);
	    index++;
	    s.setInt(index, res.calcDanishCode);
	    index++;
	    s.setString(index, res.url);
	    index++;
	    s.setTimestamp(index, res.extWDate);
	    
	    s.executeUpdate();
	    s.close();
	    return true;
    }

    private static boolean updateLineC15b(Connection conn, String tablename, String tld, String recUrl, java.sql.Timestamp recDate) throws SQLException {
	    PreparedStatement s = conn.prepareStatement("UPDATE " + tablename + " " 
	            + "SET C15b = ? "
	    		+ "WHERE Url = ? AND extWDate = ?" 
	           ); 
	    
	    int index = 1;
	    s.setString(index, tld);
	    index++;
	    s.setString(index, recUrl);
	    index++;
	    s.setTimestamp(index, recDate);
	    s.executeUpdate();
	    s.close();
	    return true;
    }

    private static boolean updateLineC8b(Connection conn, String tablename, String c8bVal, String recUrl, java.sql.Timestamp recDate) throws SQLException {
    	PreparedStatement s = conn.prepareStatement("UPDATE " + tablename + " " 
	            + "SET C8b = ? "
	    		+ "WHERE Url = ? AND extWDate = ?" 
	           ); 
	    
    	int index = 1;
	    s.setString(index, c8bVal);
	    index++;
	    s.setString(index, recUrl);
	    index++;
	    s.setTimestamp(index, recDate);
	    s.executeUpdate();
	    s.close();
	    return true;
    }

    private static class Counters {
      	long recCount=0L;
      	long updateCount=0L;
        long skippedCount=0L;
        long ignoredCount=0L;
    }
}

