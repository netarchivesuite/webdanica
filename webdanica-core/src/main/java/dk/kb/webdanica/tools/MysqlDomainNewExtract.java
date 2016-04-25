package dk.kb.webdanica.tools;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
//import java.text.SimpleDateFormat;
import java.util.*;
import java.io.IOException;

import dk.kb.webdanica.tools.MysqlWorkFlow.HadoopResItem;
import dk.kb.webdanica.tools.MysqlX.*;
import dk.kb.webdanica.tools.MysqlRes.*;
import dk.kb.webdanica.tools.MysqlDomainExtract.*;

public class MysqlDomainNewExtract {
    /**
     * @param args <JDBC-URL> jdbcUser=<JDBC-username> machine=<machine-name> datadir=<dir for out-files> tabe=all|<tablename> level=all|l1|l1_l2|l2|l3 dbop=all|mysql|berkeley|new|none order=none|asc|desc    
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws SQLException 
     * @throws IOException 
     */
	
    enum NewExtractDbOperation {
    	newall, nyeall, nyeignorecase, newmysql, nyemysql, newberkeley, nyeberkeley, newdomain, nyedomain, nyedomainDM, nyedomainDM_M, nyedomainNEW, nyedomainNEW_M, none
    }

    // output on form Url # code # forklaring af code?????
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, SQLException {
        /*****************************************/
        /*** Arguments ***************************/
        /*****************************************/
    	String errArgTxt = "Proper args: <JDBC-URL> jdbcUser=<JDBC-username> "
    			+ "machine=<machine-name> "
    			+ "datadir=<data dir where 'url-dir' (for out-files) exixts> "
    			+ "table=all|<tablename> "
    			+ "level=all|l1|l1_l2|l2|l3 "
    			+ "dbop=newall, nyeall, nyeignorecase, newmysql, nyemysql, newberkeley, nyeberkeley, newdomain, nyedomain, nyedomainDM, nyedomainDM_M, nyedomainNEW, nyedomainNEW_M "
    			+ "order=none|asc|desc ";
        if (args.length < 8) {
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
        
        /**** args - machine ****/
        /** arg - machine name **/
        String machine = args[2];
        if (!machine.startsWith("machine=")) {
            System.err.println("ERROR: Missing arg machine setting - got " + machine);
            System.err.println(errArgTxt);
            System.exit(1);
        }
        machine = MysqlX.getStringSetting(machine);

        /**** args - data-dir ****/
        String datadirTxt = args[3];
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
        File seeds1 = new File(dataDir.getAbsolutePath() + "/" + MysqlX.seeds_dir + "/" + MysqlX.seedfile1);
        if (!seeds1.isFile()) {
            System.err.println("ERROR: The given data-dir does not contains file '" + seeds1.getAbsolutePath() + "'");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        File seeds2 = new File(dataDir.getAbsolutePath() + "/" + MysqlX.seeds_dir + "/" + MysqlX.seedfile2);
        if (!seeds2.isFile()) {
            System.err.println("ERROR: The given data-dir does not contains file '" + seeds2.getAbsolutePath() + "'");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        File seeds_lc1 = new File(dataDir.getAbsolutePath() + "/" + MysqlX.seeds_dir + "/" + MysqlX.seedfile_lc1);
        if (!seeds_lc1.isFile()) {
            System.err.println("ERROR: The given data-dir does not contains file '" + seeds_lc1.getAbsolutePath() + "'");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        File seeds_lc2 = new File(dataDir.getAbsolutePath() + "/" + MysqlX.seeds_dir + "/" + MysqlX.seedfile_lc2);
        if (!seeds_lc2.isFile()) {
            System.err.println("ERROR: The given data-dir does not contains file '" + seeds_lc2.getAbsolutePath() + "'");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        
    	/**** args - table(s) ****/
        /** arg - table all|<tablename> **/
        String tablenames = args[4];
        if (!tablenames.startsWith("table=")) {
            System.err.println("ERROR: Missing arg table setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        tablenames = MysqlX.getStringSetting(tablenames);
        
        /**** args - level ****/
        LookupLevel pLevel = LookupLevel.all;
        String pLevelTxt = args[5];
        if (!pLevelTxt.startsWith("level=")) {
            System.err.println("Missing arg level setting - got " + pLevelTxt);
            System.err.println(errArgTxt);
            System.exit(1);
        }
        pLevelTxt = MysqlX.getStringSetting(pLevelTxt);
    	if (pLevelTxt.equals("all")) {
    		pLevel = LookupLevel.all;        	
        } else if (pLevelTxt.equals("l1")) {
        	pLevel = LookupLevel.l1;      
        } else if (pLevelTxt.equals("l2")) {
        	pLevel = LookupLevel.l2;      
        } else if (pLevelTxt.equals("l1_l2")) {
        	pLevel = LookupLevel.l1_l2;      
        } else if (pLevelTxt.equals("l3")) {
        	pLevel = LookupLevel.l3;      
        } else {
            System.err.println("Arg level setting is NOT valid - got '" + pLevelTxt + "'");
            System.err.println(errArgTxt);
            System.exit(1);
        }

    	/**** args - dbOp ****/
    	NewExtractDbOperation dbOp = NewExtractDbOperation.none;
    	boolean nye = true;
    	String opTxt = args[6];
        if (!opTxt.startsWith("dbop=")) {
            System.err.println("Missing arg dbop setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
    	if (opTxt.startsWith("dbop=")) {
            opTxt = MysqlX.getStringSetting(opTxt);
        }  
    	if (opTxt.startsWith("newall")) {
    		dbOp = NewExtractDbOperation.newall;     
        	nye = false;
    	 } else if(opTxt.startsWith("nyeall")) {
        	dbOp = NewExtractDbOperation.nyeall;     
        	nye = true;
        } else if (opTxt.equals("newmysql")) {
        	dbOp = NewExtractDbOperation.newmysql;      
        	nye = false;
        } else if (opTxt.equals("nyemysql")) {
        	dbOp = NewExtractDbOperation.nyemysql;      
        	nye = true;
        } else if (opTxt.equals("newberkeley")) {
        	dbOp = NewExtractDbOperation.newberkeley;  
        	nye = false;
        } else if (opTxt.equals("nyeberkeley")) {
        	dbOp = NewExtractDbOperation.nyeberkeley;  
        	nye = true;
        } else if (opTxt.equals("newdomain")) {
        	dbOp = NewExtractDbOperation.newdomain;  
        	nye = false;
        } else if (opTxt.equals("nyedomain")) {
        	dbOp = NewExtractDbOperation.nyedomain;  
        	nye = true;
        } else if (opTxt.equals("nyedomainDM")) {
        	dbOp = NewExtractDbOperation.nyedomainDM;  
        	nye = true;
        } else if (opTxt.equals("nyedomainDM_M")) {
        	dbOp = NewExtractDbOperation.nyedomainDM_M;  
        	nye = true;
        } else if (opTxt.equals("nyedomainNEW")) {
        	dbOp = NewExtractDbOperation.nyedomainNEW;  
        	nye = true;
        } else if (opTxt.equals("nyedomainNEW_M")) {
        	dbOp = NewExtractDbOperation.nyedomainNEW_M;  
        	nye = true;
        } else if (opTxt.equals("nyeignorecase")) {
        	dbOp = NewExtractDbOperation.nyeignorecase;  
        	nye = true;
        } else {
            System.err.println("Arg updateOperation setting is NOT valid - got '" + opTxt + "'");
            System.err.println(errArgTxt);
            System.exit(1);
        }

    	/**** args - order ****/
    	LookUpOrder order = LookUpOrder.none;
    	String orderTxt = args[7];
        if (!orderTxt.startsWith("order=")) {
            System.err.println("Missing arg order setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        orderTxt = MysqlX.getStringSetting(orderTxt);
    	if (orderTxt.startsWith("none")) {
    		order = LookUpOrder.none;        	
        } else if (orderTxt.startsWith("asc")) {
        	order = LookUpOrder.asc;      
        } else if (orderTxt.startsWith("desc")) {
        	order = LookUpOrder.desc;      
        } else {
            System.err.println("Arg order of look up setting is NOT valid - got '" + orderTxt + "'");
            System.err.println(errArgTxt);
            System.exit(1);
        }

        /*****************************************/
        /*** Start processing ********************/
    	/*** Writing domain levels into files for likely Danish of finished table data ***/ 
        /*****************************************/
        
    	//SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    	//Date now = new Date(System.currentTimeMillis());

		Set<Integer> dkCodeSet = MysqlX.getCodesForDanishResults();
        Set<String> allTableSet = MysqlRes.getTables(conn);
        Set<String> codesDoneSet = getCodesDone(dataDir);
        System.out.println("*** Finding tables ");
        Set<String> tableSet = new HashSet<String>();
	    if (tablenames.equals("all")) {
	    	tableSet = allTableSet;
	    } else {
	    	tableSet.add( tablenames.trim() );
	    }
        
        /****** 
        /** Set of tables to extract URLs from */

        if (dbOp==NewExtractDbOperation.newall || dbOp==NewExtractDbOperation.nyeall ||dbOp==NewExtractDbOperation.nyemysql || dbOp==NewExtractDbOperation.newmysql) {
        	/****** 
	        /** Set of Urls to be writen to file from IA or NAS table sets */
            Set<HadoopResItem> IAitemSet = new HashSet<HadoopResItem>();
            Set<HadoopResItem> NAitemSet = new HashSet<HadoopResItem>();
            
	        /** read merged status for tables to see if it is finished */
            Map<String,HadoopResItem> mergedItemMap = MysqlAllStatusExtract.extractAllStatus(conn, "", datadirTxt);
            
	        /** find relevant tables */
            for (String tablenm : tableSet) {
        		if (tablenm.startsWith(MysqlRes.wf_table_prefix)) {
        	    	HadoopResItem tmp = new HadoopResItem("", "", "", "");
                	tmp.set_nos_from_name( tablenm.substring(MysqlRes.wf_table_prefix.length()), MysqlWorkFlow.wf_table_delim);
                	String key = tmp.getname(MysqlWorkFlow.wf_dir_delim);
	        		//System.out.println("key  " + key);
                	if (mergedItemMap.containsKey(key)) {
    	        		//System.out.println("key cointained " + key);
        				tmp = mergedItemMap.get(key);
        				if (tmp.allOk("v5",20,0, ExtractDbOperation.none)) {
        	        		//System.out.println("to be added " + key);
        	            	if (MysqlX.isIAtablename(tablenm)) {
        	            		IAitemSet.add(tmp);
        	            	} else if (MysqlX.isNAStablename(tablenm)) { 
        	            		NAitemSet.add(tmp);
        	            	} else { 
        	    	            System.out.println("*WARNING: table not IA nor NAS: " + tablenm);
        	            	}
        				}
        			}
        		}
            }
            System.out.println("*IAitemSet.size: " + IAitemSet.size());
            System.out.println("*NAitemSet.size: " + NAitemSet.size());
            
	    	/****** 
	        /** read previously found domains  */
            Map<String,DomNewSet> domsMap = new HashMap<String, DomNewSet>();
            for (int level=1;level<=MysqlX.noDomainLevels ;level++) {
            	if ( !MysqlX.skipLevel(pLevel, level) ) {
	                for (Source src : Source.values()) {
		        		for (int code : dkCodeSet) {
				        	String t = ""; 
		    				if (nye) t = MysqlRes.domainNyeTableName(level, src, code);
		    				else t = MysqlRes.domainNewTableName(level, src, code);
					    	if (allTableSet.contains(t)) {
			        			DomNewSet domSet = MysqlRes.readDomNewSet(conn, level, src, code, nye); 
				            	domsMap.put(t, domSet);
					    	}
		                }
	                }
                }
            }
            System.out.println("*Existing domains: " + domsMap.size());

            for (Source src : Source.values()) {
	            System.out.println("***** Finding domain levels " + pLevel.name() + " for " + src.name() + " data");
	            
        		Set<HadoopResItem> itemSet = (src==Source.IA? IAitemSet : NAitemSet);

        		for (int code : dkCodeSet) {
            		System.out.println("**** for code " + code);
            		Set<String> currentCodesSet = new HashSet<String>();
	        		for (int level=1;level<=MysqlX.noDomainLevels;level++) {
                    	if ( !MysqlX.skipLevel(pLevel, level) ) {
                    		String dt = "";
		    				if (nye) dt = MysqlRes.domainNyeTableName(level, src, code);
		    				else dt = MysqlRes.domainNewTableName(level, src, code);
		            		currentCodesSet.add("_" + dt);
    	                }
	                }
            		
		        	for (HadoopResItem item : itemSet) {
		        		boolean skipTable = true;
		        		for (int lv=1;MysqlX.inclExtract(pLevel,lv);lv++) {
		        			//System.out.println("tableitem.urlLevelExtracted.get(" + (lv-1) + "): " + tableitem.urlLevelExtracted.get(lv-1));
		        			skipTable = skipTable && item.urlLevelNewExtracted.get(lv-1);
		        		}
		        		if (skipTable) System.out.println("***** Already done for tablename " + item.tablename() + " levels " + pLevel.name());
    	                
		        		boolean skipCode = true;
		        		for (String currCode: currentCodesSet) {
			    			skipCode = skipCode && codesDoneSet.contains(currCode);
    	                }
		        		if (skipCode) System.out.println("***** Already done for Code " + code + " levels " + pLevel.name());
		        		
		        		if ((!skipTable) && !skipCode) { 
			        		System.out.println("***** for tablename " + item.tablename() + " levels " + pLevel.name());
		                
    	                	if (code == 123) {
        	                	Set<String> urlSet = new HashSet<String>();
    	                		long sz = 0;
        	                	String pattern = "";
    	                		
        	                	pattern = "url like 'http://www.ne%'";
        	                	System.out.println("--- for urlset " + pattern);
        	                	urlSet = MysqlRes.getUrlsForCalcCode(conn, item.tablename(), code, pattern);
        	                	sz = urlSet.size();
    	                		if (sz==0)  {
    	                			System.out.println("*** No Urls for " + item.tablename());
    	                		} else {
    	                    		System.out.println("*** urlSet " + sz);
    	                    		processUrlset(conn, nye, allTableSet, src, pLevel, code, urlSet);
    	                		}

        	                	pattern = "url NOT like 'http://www.ne%' AND url like 'http://www.n%'";
        	                	System.out.println("--- for urlset " + pattern);
        	                	urlSet = MysqlRes.getUrlsForCalcCode(conn, item.tablename(), code, pattern);
        	                	sz = urlSet.size();
    	                		if (sz==0)  {
    	                			System.out.println("*** No Urls for " + item.tablename());
    	                		} else {
    	                    		System.out.println("*** urlSet " + sz);
    	                    		processUrlset(conn, nye, allTableSet, src, pLevel, code, urlSet);
    	                		}

        	                	pattern = "url like 'https:%'";
        	                	System.out.println("--- for urlset " + pattern);
        	                	urlSet = MysqlRes.getUrlsForCalcCode(conn, item.tablename(), code, pattern);
        	                	sz = urlSet.size();
    	                		if (sz==0) {
    	                			System.out.println("*** No Urls for " + item.tablename());
    	                		} else {
    	                    		System.out.println("*** urlSet " + sz);
    	                    		processUrlset(conn, nye, allTableSet, src, pLevel, code, urlSet);
    	                		}

        	                	pattern = "(url NOT like 'http://www.n%') AND (url NOT like 'https:%')";
        	                	System.out.println("--- for urlset " + pattern);
        	                	urlSet = MysqlRes.getUrlsForCalcCode(conn, item.tablename(), code, pattern);
        	                	sz = urlSet.size();
    	                		if (sz==0) {
    	                			System.out.println("*** No Urls for " + item.tablename());
    	                		} else {
    	                    		processUrlset(conn, nye, allTableSet, src, pLevel, code, urlSet);
    	                		}
    	                	} else {
    	                		Set<String> urlSet = MysqlRes.getUrlsForCalcCode(conn, item.tablename(), code);
    	                		long sz = urlSet.size();
    	                		if (sz==0) {
    	                			System.out.println("*** No Urls for " + item.tablename());
    	                		} else {
    	                    		System.out.println("*** urlSet " + sz);
    	                    		processUrlset(conn, nye, allTableSet, src, pLevel, code, urlSet);
        	                	}
    	                	}
    	                }
            		}
                	for (String currCode: currentCodesSet) {
                		if (!codesDoneSet.contains(currCode)) {
 		    	            Runtime.getRuntime().exec("touch " + dataDir.getAbsolutePath() + "/" + currCode);
		    	        	System.out.println("- executed: '" +  "touch " + dataDir.getAbsolutePath() + "/" + currCode + "'");
		    	        	codesDoneSet.add(currCode);
                		}
                    }
	        	}
	        	for (HadoopResItem item : itemSet) {
	                /** Write updatetime */
	                System.out.println("*** writing status of domainlevels ");
	    	        String tld1updFilename = 
	    	        		dataDir.getAbsolutePath() + "/" + item.datasubdirname().replace("-v5","") + "/"
	    	        		+ MysqlWorkFlow.wf_urllevelsnew_extractfilename_prefix + "_";
	                File tld1upd = new File(tld1updFilename); 
	        		if (!tld1upd.exists()) { 
	                    for (int i=1;i<=MysqlX.noDomainLevels;i++) {
	                    	if ( !MysqlX.skipLevel(pLevel, i) ) {
			    	            Runtime.getRuntime().exec("touch " + tld1updFilename + i);
			    	        	System.out.println("- executed: '" +  "touch " + tld1updFilename + i + "'");
		                    }
	                    }
	        		}
        		}
	        }
        }

        
        /**************************************************************/
        /***************** Lookup berk urls *************/
        if (dbOp.equals(NewExtractDbOperation.newall) || dbOp.equals(NewExtractDbOperation.nyeall) ||dbOp.equals(NewExtractDbOperation.nyeberkeley) || dbOp.equals(NewExtractDbOperation.newberkeley)
        		|| dbOp.equals(NewExtractDbOperation.nyeignorecase) ) {
        	System.out.println("**** Starting lookup of berkeley");
        	
	        boolean caseSensitive = dbOp.equals(NewExtractDbOperation.nyeignorecase);

	        for (int level=1;level<=MysqlX.noDomainLevels;level++) {
        		LookedupBerkSet luBerk = new LookedupBerkSet();
            	LookedupBerkSet luBerkL1Cnt0 = new LookedupBerkSet();
            	LookedupBerkSet luBerkL2Cnt0 = new LookedupBerkSet();
            	if ( !MysqlX.skipLevel(pLevel, level) ) {
            		if (!caseSensitive) {
		        		luBerk = MysqlRes.readLookedupBerkSet(conn, level);
		            	if (level==2) luBerkL1Cnt0 = MysqlRes.readLookedupBerkSetFromDonmainLev(conn, 1, nye, "LookedUpInBerkeley=1 AND CntInBerkeley=0");
		            	if (level==3) luBerkL2Cnt0 = MysqlRes.readLookedupBerkSetFromDonmainLev(conn, 2, nye, "LookedUpInBerkeley=1 AND CntInBerkeley=0");
			            System.out.println("*** berkely level " + luBerk.level);
			            System.out.println("count berkSets " + luBerk.luBerkMap.size());
			            System.out.println("count berkL1Cnt0 " + luBerkL1Cnt0.luBerkMap.size());
			            System.out.println("count berkL2Cnt0 " + luBerkL2Cnt0.luBerkMap.size());
            		}
			        
            		System.out.println("*** Updating for level " + level);
		            String table = "";
    				if (nye) table = MysqlRes.domainNyeTableName(level);
    				else table = MysqlRes.domainNewTableName(level);
			        Set<String> fields = MysqlRes.getFields(conn, table);

			        if (!fields.contains("OnlyCaseSensitive")) {
			        	addOnlyCaseSensitiveField(conn, table);
			        }
		            
			        String wherestmt = "LookedUpInBerkeley=false";
			        File s1 = seeds1;
			        String s1name = "allseeds1";
			        File s2 = seeds2;
			        String s2name = "allseeds2";
			        if (caseSensitive) {
			        	wherestmt = "(LookedUpInBerkeley=true AND (OnlyCaseSensitive=false OR  OnlyCaseSensitive is null) AND CntInBerkeley=0)";
				        s1 = seeds_lc1;
				        s1name = "allseeds_lc1";
				        s2 = seeds_lc2;
				        s2name = "allseeds_lc2";
			        } 
            		System.out.println("wherestmt " + wherestmt);
			        	
			        if (order.equals(LookUpOrder.asc) || order.equals(LookUpOrder.desc)) {
	        			Set<String> firstchars = new HashSet<String>();
	        			firstchars.add("www.0");firstchars.add("www.1");firstchars.add("www.2");firstchars.add("www.3");firstchars.add("www.4");
	        			firstchars.add("www.5");firstchars.add("www.6");firstchars.add("www.7");firstchars.add("www.8");firstchars.add("www.9");
	        			firstchars.add("y");firstchars.add("z");
	        			firstchars.add("0");firstchars.add("1");firstchars.add("2");firstchars.add("3");firstchars.add("4");
	        			firstchars.add("5");firstchars.add("6");firstchars.add("7");firstchars.add("8");firstchars.add("9");
	        			firstchars.add("www.a");firstchars.add("www.b");firstchars.add("www.c");firstchars.add("www.d");firstchars.add("www.e");firstchars.add("www.f");
	        			firstchars.add("www.g");firstchars.add("www.h");firstchars.add("www.i");firstchars.add("www.j");firstchars.add("www.k");firstchars.add("www.l");
	        			firstchars.add("www.m");firstchars.add("www.n");firstchars.add("www.o");firstchars.add("www.p");firstchars.add("www.q");firstchars.add("www.r");
	        			firstchars.add("www.s");firstchars.add("www.t");firstchars.add("www.u");firstchars.add("www.v");firstchars.add("www.x");
	        			firstchars.add("www.y");firstchars.add("www.z");
	        			firstchars.add("a");firstchars.add("b");firstchars.add("c");firstchars.add("d");firstchars.add("e");firstchars.add("f");
	        			firstchars.add("g");firstchars.add("h");firstchars.add("i");firstchars.add("j");firstchars.add("k");firstchars.add("l");
	        			firstchars.add("m");firstchars.add("n");firstchars.add("o");firstchars.add("p");firstchars.add("q");firstchars.add("r");
	        			firstchars.add("s");firstchars.add("t");firstchars.add("u");firstchars.add("v");firstchars.add("x");
	        			
	        			List<String> sortedchars = new ArrayList<String>(firstchars.size());
	        			sortedchars.addAll(firstchars);
	        			
	        			if (order.equals(LookUpOrder.asc)) Collections.sort(sortedchars);
	        			else if (order.equals(LookUpOrder.desc)) Collections.sort(sortedchars, Collections.reverseOrder());
	        			
	        			DomNewSet nds = new DomNewSet();
	        			for (String c: sortedchars) {
	    			        nds = MysqlRes.readDomNewSet(conn, table, "Domain like '" + c + "%' AND " + wherestmt); //domains to be search for 
	    		            System.out.println("*** Updating berkely for level " + nds.level + " CHAR " + c );
	    		            System.out.println("count new DomSets " + nds.domMap.size());
    		            	lookupBerkUrlsNew(conn, level, nye, nds, luBerk, luBerkL1Cnt0, luBerkL2Cnt0, s1, s2, s1name, s2name, caseSensitive);
	        			}
	        			//The rest if any
				        nds = MysqlRes.readDomNewSet(conn, table, wherestmt); //domains to be search for 
			            System.out.println("*** Updating berkely for level " + nds.level);
			            System.out.println("count new DomSets " + nds.domMap.size());
				        lookupBerkUrlsNew(conn, level, nye, nds, luBerk, luBerkL1Cnt0, luBerkL2Cnt0,  s1, s2, s1name, s2name, caseSensitive);
	        		} else {
	        			DomNewSet nds = new DomNewSet();
				        nds = MysqlRes.readDomNewSet(conn,table, wherestmt); //domains to be search for 
		            		
			            System.out.println("*** Updating berkely for level " + nds.level);
			            System.out.println("count new DomSets " + nds.domMap.size());
			            
				        lookupBerkUrlsNew(conn, level, nye, nds, luBerk, luBerkL1Cnt0, luBerkL2Cnt0,  s1, s2, s1name, s2name, caseSensitive);
	            	}
	        	}
	        }
        }

        /***************** Lookup new domians from old *************/
        System.out.println("dbOp: " + dbOp.name());
        System.out.println("Lookup: " + (dbOp==NewExtractDbOperation.newdomain 
        		|| dbOp==NewExtractDbOperation.nyedomainDM
				|| dbOp==NewExtractDbOperation.nyedomainDM
				|| dbOp==NewExtractDbOperation.nyedomain
				|| dbOp==NewExtractDbOperation.nyedomainNEW
                || dbOp==NewExtractDbOperation.nyedomainNEW_M));
        if (dbOp==NewExtractDbOperation.newdomain 
        		|| dbOp==NewExtractDbOperation.nyedomainDM
        				|| dbOp==NewExtractDbOperation.nyedomainDM
        				|| dbOp==NewExtractDbOperation.nyedomain
        				|| dbOp==NewExtractDbOperation.nyedomainNEW
                        || dbOp==NewExtractDbOperation.nyedomainNEW_M) {
        	System.out.println("**** Starting lookup for new domains");
        	
        	for (int level=1;level<=MysqlX.noDomainLevels;level++) { 
        		boolean goOn = !MysqlX.skipLevel(pLevel, level);
        		
    			//Find to and from tables
				String fromtable = "";
	            String totable = "";
        		if (!goOn) {
    	            System.out.println("*** Skipping level " + level);
        		} else {
		            System.out.println("*** Updating for level " + level);
					if (nye) 
						totable = MysqlRes.domainNyeTableName(level);
					else 
						totable = MysqlRes.domainNewTableName(level);
					if (!allTableSet.contains(totable)) {
						System.out.println("-- totable  did not exist " + totable);
						goOn = false;
					}
				
					if (dbOp==NewExtractDbOperation.nyedomainDM)
						fromtable = MysqlRes.domainTableName(level) + "_DM";
					else if (dbOp==NewExtractDbOperation.nyedomainDM_M )
						fromtable = MysqlRes.domainTableName(level) + "_DM_M" + machine;
					else if (dbOp==NewExtractDbOperation.nyedomainNEW )
						fromtable = MysqlRes.domainNewTableName(level);
					else if (dbOp==NewExtractDbOperation.nyedomainNEW_M )
						fromtable = MysqlRes.domainNewTableName(level) + "_M" + machine;
					else if (dbOp==NewExtractDbOperation.newdomain || dbOp==NewExtractDbOperation.nyedomain)
						fromtable = MysqlRes.domainTableName(level);
					if (!allTableSet.contains(fromtable)) {
						System.out.println("-- totable  did not exist " + fromtable);
						goOn = false;
					}
				}
				
				if (goOn) {
	        		System.out.println("old table: " + fromtable); 
	        		LookedupBerkSet luBerk = MysqlRes.readLookedupBerkSetFromDonmainLev(conn, level, fromtable, "LookedUpInBerkeley=1"); 
		            System.out.println("old table: count " + luBerk.luBerkMap.size());

		            System.out.println("new table: " + totable);
	        		DomNewSet newdoms = MysqlRes.readDomNewSet(conn, totable);
		            System.out.println("count new doms " + newdoms.domMap.size());
	        		
		            System.out.println("*** Updating berkely for level " + level);
		            for (String key : newdoms.domMap.keySet()) {
		            	DomainNewLevel newd = newdoms.domMap.get(key);
		            	if (luBerk.luBerkMap.containsKey(key)) {
		            		LookedUpBerk lub = luBerk.luBerkMap.get(key);
		            		newd.LookedUpInBerkeley = true;
		            		newd.CntInBerkeley = lub.CntInBerkeley;
		            		MysqlRes.updateNewDomainLevelLookupInf(conn, newdoms.table, newd);
		            	}
		            }
	        	}
        	}
        }
        
        conn.close();
		System.out.println("*** Finished");
    }

	private static void lookupBerkUrlsNew(Connection conn, int level, boolean nye, 
			DomNewSet ds,
			LookedupBerkSet luBerk, LookedupBerkSet luBerkL1Cnt0, LookedupBerkSet luBerkL2Cnt0, 
			File seeds1, File seeds2, String seeds1name, String seeds2name,
			boolean caseSensitive
	) throws SQLException {
		for (String key : ds.domMap.keySet()) {
			DomainNewLevel dom = ds.domMap.get(key);
			if ((!dom.LookedUpInBerkeley) || caseSensitive) {
				if (luBerk.luBerkMap.keySet().contains(dom.KeyDomain)) {
					dom.LookedUpInBerkeley = true;
					dom.CntInBerkeley = luBerk.luBerkMap.get(dom.KeyDomain).CntInBerkeley;
	        		System.out.println("*** found in 'ALL' database " + dom.Domain );
				} else {
	    			String[] parts = dom.Domain.split("/"); //Expected level1 + level2
					String dL1 = parts[0];;
					String dL2 = "";
	    	        if (parts.length>1) dL2 = dL1 + "/" + parts[1];
	    	        
	            	if (level==2 || level==3) {
	            		if (luBerkL1Cnt0.luBerkMap.keySet().contains(dL1)) {
	            			dom.LookedUpInBerkeley = true;
	            			dom.CntInBerkeley = 0;
	            			System.out.println("*** excluded from level 1 " + dL1 + " of " + dom.Domain );
	            		}
	        		}
	
	            	if (!dom.LookedUpInBerkeley && level==3) {
	            		if (luBerkL2Cnt0.luBerkMap.keySet().contains(dL2)) {
	            			dom.LookedUpInBerkeley = true;
	            			dom.CntInBerkeley = 0;
	            			System.out.println("*** excluded from level 2 " + dL2 + " of " + dom.Domain );
	            		}
	        		}
	    		}
				if ((!dom.LookedUpInBerkeley) || caseSensitive) {
					String domain = dom.Domain;
	    			if (caseSensitive) domain = "//" + domain.toLowerCase();
	    			
    				dom.CntInBerkeley = MysqlX.findGreplinesInFile(seeds1, seeds1name, domain);
	        		if (dom.CntInBerkeley == 0) {
	        			dom.CntInBerkeley = MysqlX.findGreplinesInFile(seeds2, "seeds2name", domain);
	        		}
	        		dom.LookedUpInBerkeley = true;
	    		}
				String table = "";
				if (nye) table = MysqlRes.domainNyeTableName(ds.level);
				else table = MysqlRes.domainNewTableName(ds.level);
				DomainNewLevel entry = MysqlRes.readIfexistsDomainNew(conn, table, dom.Domain);
				if (entry.Domain.isEmpty()) {
					System.out.println("*** WARNING DOMAIN NOT FOUND domain " + dom.Domain );
	        	} else {
	        		dom.OnlyCaseSensitive = caseSensitive;
	        		MysqlRes.updateNewDomainLevelLookupInf(conn,table, dom); //updateBerkeleyDomainLevel
	        	}
	    	} else {
	    		System.out.println("*** ignoring domain " + dom.Domain );
	    	}
		}
	}

	private static Set<String> getCodesDone(File dataDir) {
		Set<String> codesDoneSet = new HashSet<String>();
		
		File[] dirfiles = dataDir.listFiles();
	    
		for (File f: dirfiles) {
		    String fname = f.getName();
    		if (fname.startsWith("_" + MysqlRes.domaintable_prefix) 
    			&& fname.contains("_" + MysqlRes.domaintable_level_infix )
    			&& (fname.contains("_" + Source.IA.name()) || fname.contains("_" + Source.NAS.name()))
    			&& fname.contains("_C")) {
    			codesDoneSet.add(fname);
    		}
	    }
	    return codesDoneSet;
    }

	private static void processUrlset(Connection conn, boolean nye, Set<String> allTableSet, 
						Source src, LookupLevel pLevel, int code, Set<String> urlSet
	) throws SQLException {
    	List<DomNewSet> domSets = new ArrayList<DomNewSet>();
        for (int level=1;level<=MysqlX.noDomainLevels;level++) {
        	if ( !MysqlX.skipLevel(pLevel, level) ) {
                String dt = "";
				if (nye) dt = MysqlRes.domainNyeTableName(level, src, code);
				else dt = MysqlRes.domainNewTableName(level, src, code);
				
				
                DomNewSet domsetx = new DomNewSet (level, src, code, dt);	
		    	if (!allTableSet.contains(dt)) {
		    		MysqlRes.createDomainNewLevelTable(conn, dt);
		    		allTableSet.add(dt);
		    	} else {
		    		domsetx =  MysqlRes.readDomNewSet(conn, level, src, code, nye);
		    	}
                domSets.add(domsetx);
                
        	} else {
                DomNewSet domsetx = new DomNewSet (level, src, code, "");	
                domSets.add(domsetx);
	    	}
        }
        for (DomNewSet ds : domSets) {
    		System.out.println("** table " + ds.table);
    		System.out.println("** entries " + ds.domMap.size());
        }
        
		for (String url: urlSet) {
			String[] parts = url.split("/");
			//Expected http: + / + / + level1 + level2
			//           0     1   2     3         4
			if (parts.length<=2) {
	            System.err.println("ERROR: Domain Levels 1+2: " + url );
	            System.exit(1);
			} else {
				String s = "";
				for (int level = 1; level<=MysqlX.noDomainLevels; level++) {
                	if ( MysqlX.inclExtract(pLevel, level) ) {
        	            if (parts.length>level+1) {
        	            	s = ( level!=1 ? s + "/" : "" ) +parts[level+1].trim();
        	            	String key = s.toLowerCase().trim();
        	            	if (key.length()>=900) 
        	            		key=key.substring(0,900);
	                    	if ( !MysqlX.skipLevel(pLevel, level) ) {
	            				DomNewSet domsetx = domSets.get(level-1);
	            				if (!domsetx.domMap.containsKey(key)) {
		            				DomainNewLevel dl = new DomainNewLevel(domsetx.level, s, domsetx.src, 0, domsetx.code);
		            				domsetx.domMap.put(key, dl);
	            				}
            					if (src.equals(Source.IA)) domsetx.domMap.get(key).CntInIaData++;
            					else domsetx.domMap.get(key).CntInNasData++;
            	            }
        	            }
    				}
				}
			}
		}
        for (DomNewSet ds : domSets) {
    		System.out.println("** table " + ds.table);
    		System.out.println("-- entries " + ds.domMap.size());
        }
		
		//write found to database
		
		for (DomNewSet domsetx : domSets) {
    		for (String key: domsetx.domMap.keySet()) {
    			DomainNewLevel dom = domsetx.domMap.get(key);
    			if (!domsetx.table.contains("_C")) {
	                System.err.println("*** wrong table: " + domsetx.table);
    				System.exit(1);
    			}
    			if (dom.entryRead) { 
	                //System.err.println("*** update table: " + domsetx.table);
    				MysqlRes.updateNewDomainLevel(conn, domsetx.table, dom);
    			} else {
	                //System.err.println("*** insert table: " + domsetx.table);
    				MysqlRes.insertDomainNewLevel(conn, domsetx.table, dom);
    			}
    		}
    	}
	}

	public static boolean addOnlyCaseSensitiveField(Connection conn, String table) throws SQLException {
		String sql = "ALTER TABLE " + table + " ADD (OnlyCaseSensitive TINYINT(1))";
		PreparedStatement s = conn.prepareStatement(sql);
	    s.executeUpdate();
	    s.close();
		return true;
	}
}
