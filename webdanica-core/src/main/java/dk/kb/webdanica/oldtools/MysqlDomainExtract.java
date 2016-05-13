package dk.kb.webdanica.oldtools;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
//import java.text.SimpleDateFormat;
import java.util.*;
import java.io.IOException;

import dk.kb.webdanica.oldtools.MysqlRes.*;
import dk.kb.webdanica.oldtools.MysqlWorkFlow.HadoopResItem;
import dk.kb.webdanica.oldtools.MysqlX.*;

public class MysqlDomainExtract {
    /**
     * @param args <JDBC-URL> jdbcUser=<JDBC-username> machine=<machine-name> datadir=<dir for out-files> tabe=all|<tablename> level=all|l1|l1_l2|l2|l3 dbop=all|mysql|berkeley|newdom|none order=none|asc|desc    
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws SQLException 
     * @throws IOException 
     */

    enum ExtractDbOperation {
    	all, mysql, berkeley, none
    }
	
    enum LookUpOrder {
    	random,asc,desc, none
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
    			+ "dbop=all|mysql|berkeley|newdom "
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
        File seeds1 = new File(dataDir.getAbsolutePath() + "/seeds/allseeds-job1og2.txt");
        if (!seeds1.isFile()) {
            System.err.println("ERROR: The given data-dir does not contains file '" + seeds1.getAbsolutePath() + "'");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        File seeds2 = new File(dataDir.getAbsolutePath() + "/seeds/allseeds-job3.txt");
        if (!seeds2.isFile()) {
            System.err.println("ERROR: The given data-dir does not contains file '" + seeds2.getAbsolutePath() + "'");
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
        ExtractDbOperation dbOp = ExtractDbOperation.none;
    	String opTxt = args[6];
        if (!opTxt.startsWith("dbop=")) {
            System.err.println("Missing arg dbop setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        opTxt = MysqlX.getStringSetting(opTxt);
        System.out.println("opTxt '" + opTxt + "'");
    	if (opTxt.startsWith("all")) {
    		dbOp = ExtractDbOperation.all;        	
        } else if (opTxt.equals("mysql")) {
        	dbOp = ExtractDbOperation.mysql;      
        } else if (opTxt.equals("berkeley")) {
        	dbOp = ExtractDbOperation.berkeley;  
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
        
    	if(machine.equals("sol1") || machine.equals("test5") || machine.equals("test7") || machine.equals("Htest5") || machine.equals("Htest7")) {
            System.err.println("Wrong machine: " + machine);
            System.exit(1);
    	}

    	
    	//SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    	//Date now = new Date(System.currentTimeMillis());

        Set<String> allTableSet = MysqlRes.getTables(conn);
        System.out.println("*** Finding tables ");
        Set<String> tableSet = new HashSet<String>();
	    if (tablenames.equals("all")) {
	    	tableSet = allTableSet;
	    } else {
	    	tableSet.add( tablenames.trim() );
	    }
        
	    

        /****** 
        /** Set of tables to extract URLs from */

        if (dbOp==ExtractDbOperation.all || dbOp==ExtractDbOperation.mysql) {
    	    //Create level tables if not there - 
    	    //manual delete of contents if bnew updates!!!!!
            for (int i=1;i<=MysqlX.noDomainLevels;i++) {
            	if ( !MysqlX.skipLevel(pLevel, i) ) {
	            	String t = MysqlRes.domainTableName(i);
	    	    	if (!allTableSet.contains(t)) {
	    	    		MysqlRes.createDomainLevelTable(conn, t);
	    	    	}
    	    	}
            }

            /****** 
	        /** Set of Urls to be writen to file from IA or NAS table sets */
            Set<HadoopResItem> IAtableSet = new HashSet<HadoopResItem>();
            Set<HadoopResItem> NAStableSet = new HashSet<HadoopResItem>();
            
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
        	            		IAtableSet.add(tmp);
        	            	} else if (MysqlX.isNAStablename(tablenm)) { 
        	            		NAStableSet.add(tmp);
        	            	} else { 
        	    	            System.out.println("*WARNING: table not IA nor NAS: " + tablenm);
        	            	}
        				}
        			}
        		}
            }
    	    
	    	/****** 
	        /** read previously found domains */
            List<DomSet> domSets = new ArrayList<DomSet>();
            for (int level=1;level<=MysqlX.noDomainLevels;level++) {
            	if ( !MysqlX.skipLevel(pLevel, level) ) {
	        		DomSet domSet = new DomSet(); 
	            	domSet = MysqlRes.readDomSet(conn, level); 
	            	domSets.add(domSet);
            	} else {
	        		DomSet domSet = new DomSet(); 
	            	domSets.add(domSet);
                }
            }

            
            for (Source src : Source.values()) {
	            System.out.println("**** Finding domain levels 1-" + MysqlX.noDomainLevels + " for " + src.name() + " data");
	            
	        	for (HadoopResItem tableitem : (src==Source.IA? IAtableSet : NAStableSet)) {
	        		boolean skip = true;
	        		System.out.println("tablename " + tableitem.tablename());
	        		for (int lv=1;lv<=MysqlX.noDomainLevels;lv++) {
	        			//System.out.println("tableitem.urlLevelExtracted.get(" + (lv-1) + "): " + tableitem.urlLevelExtracted.get(lv-1));
	        			skip = skip && tableitem.urlLevelExtracted.get(lv-1);
	        		}
	        		if (skip) System.out.println("*** Already done for tablename " + tableitem.tablename());
	        		else {
		        		System.out.println("*** for tablename " + tableitem.tablename());
		        		Set<Integer> codeSet = MysqlX.getCodesForDanishResults();
		                
		        		for (int code : codeSet) {
		                    System.out.println("** for code " + code);
		            		Set<String> urlSet = MysqlRes.getUrlsForCalcCode(conn, tableitem.tablename(), code);
		                    System.out.println("** urlSet " + urlSet.size());
		            		for (String url: urlSet) {
		            			String[] parts = url.split("/");
		            			//Expected http: + / + / + level1 + level2
		            			//           0     1   2     3         4
		            			if (parts.length<=2) {
		            	            System.err.println("ERROR: Domain Levels 1+2: " + url );
		            	            System.exit(1);
		            			} else {
		            				String s = parts[2];
		            				DomainLevel dl = new DomainLevel(s, src);
		            				domSets.get(0).domSet.add(dl);
		            	            for (int i=2;i<=MysqlX.noDomainLevels;i++) {
			            	            if (parts.length>i+1) {
			            	            	s = s + "/" + parts[1+i];
				            				DomainLevel dli = new DomainLevel(s, src);
				            				dli.calcCodes.add(code);
			            					domSets.get(i-1).domSet.add(dli);
			                			}
		            	            }
		            			}
		            		}
		                    for (int i=1;i<=MysqlX.noDomainLevels;i++) {
			            		for (DomainLevel dom: domSets.get(i-1).domSet) {
			            			String tn = MysqlRes.domainTableName(i);
			            			MysqlRes.updateDomainLevel(conn, tn, dom);
			            		}
		            		}
		            	}

		                /** Write updatetime */
		                System.out.println("*** writing status of domainlevels ");
		    	        String tld1updFilename = 
		    	        		dataDir.getAbsolutePath() + "/" + tableitem.datasubdirname().replace("-v5","") + "/"
		    	        		+ MysqlWorkFlow.wf_urllevels_extractfilename_prefix + "_";
		                File tld1upd = new File(tld1updFilename); 
		        		if (!tld1upd.exists()) { 
		                    for (int i=1;i<=MysqlX.noDomainLevels;i++) {
			    	            Runtime.getRuntime().exec("touch " + tld1updFilename + i);
			    	        	System.out.println("- executed: '" +  "touch " + tld1updFilename + i + "'");
		                    }
		        		}
	        		}
	        	}
	        }
        }

        
        /***************** Lookup berk urls *************/
        if (dbOp==ExtractDbOperation.all || dbOp==ExtractDbOperation.berkeley ) {
        	System.out.println("**** Starting lookup of berkeley");
        	
        	for (int level=1;level<=MysqlX.noDomainLevels;level++) {
        		if (!MysqlX.skipLevel(pLevel, level)) {
	        		LookedupBerkSet luBerk = MysqlRes.readLookedupBerkSet(conn, level);
	            	LookedupBerkSet luBerkL1Cnt0 = new LookedupBerkSet();
	            	LookedupBerkSet luBerkL2Cnt0 = new LookedupBerkSet();
	            	if (level==2) luBerkL1Cnt0 = MysqlRes.readLookedupBerkSetFromDonmainLev(conn, 1, false,  "LookedUpInBerkeley=1 AND CntInBerkeley=0");
	            	if (level==3) luBerkL2Cnt0 = MysqlRes.readLookedupBerkSetFromDonmainLev(conn, 2, false, "LookedUpInBerkeley=1 AND CntInBerkeley=0");
		            System.out.println("*** Updating for level " + level);
		            System.out.println("*** berkely level " + luBerk.level);
		            System.out.println("count berkSets " + luBerk.luBerkMap.size());
		            System.out.println("count berkL1Cnt0 " + luBerkL1Cnt0.luBerkMap.size());
		            System.out.println("count berkL2Cnt0 " + luBerkL2Cnt0.luBerkMap.size());
	        		
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
	        			
	        			DomSet ds = new DomSet();
	        			for (String c: sortedchars) {
	        		        ds = MysqlRes.readDomSet(conn, level, "Domain like '" + c + "%' AND LookedUpInBerkeley=false"); //domains to be search for 
	    		            System.out.println("*** Updating berkely for level " + ds.level + " CHAR " + c );
	    		            System.out.println("count DomSets " + ds.domSet.size());
	    			        lookupBerkUrls(conn, level, ds, luBerk, luBerkL1Cnt0, luBerkL2Cnt0, seeds1, seeds2);
	        			}
	        			//The rest if any
				        ds = MysqlRes.readDomSet(conn, level, "LookedUpInBerkeley=false"); //domains to be search for 
			            System.out.println("*** Updating berkely for level " + ds.level);
			            System.out.println("count DomSets " + ds.domSet.size());
				        lookupBerkUrls(conn, level, ds, luBerk, luBerkL1Cnt0, luBerkL2Cnt0, seeds1, seeds2);
	        		} else {
	        			DomSet ds = new DomSet();
				        ds = MysqlRes.readDomSet(conn, level, "LookedUpInBerkeley=false"); //domains to be search for 
		            		
			            System.out.println("*** Updating berkely for level " + ds.level);
			            System.out.println("count DomSets " + ds.domSet.size());
				        lookupBerkUrls(conn, level, ds, luBerk, luBerkL1Cnt0, luBerkL2Cnt0, seeds1, seeds2);
	            	}
            	}
        	}
        }

        conn.close();
		System.out.println("*** Finished");
    }

	private static void lookupBerkUrls(Connection conn, 
			int level, DomSet ds,
			LookedupBerkSet luBerk, LookedupBerkSet luBerkL1Cnt0, LookedupBerkSet luBerkL2Cnt0, 
			File seeds1, File seeds2
	) throws SQLException{
		for (DomainLevel dom: ds.domSet) {
			if (!dom.LookedUpInBerkeley) {
				if (luBerk.luBerkMap.keySet().contains(dom.Domain)) {
					dom.LookedUpInBerkeley = true;
					dom.CntInBerkeley = luBerk.luBerkMap.get(dom.Domain).CntInBerkeley;
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
	    		if (!dom.LookedUpInBerkeley) {
	    			dom.CntInBerkeley = MysqlX.findGreplinesInFile(seeds1, "allseeds1", dom.Domain);
	        		if (dom.CntInBerkeley ==0) {
	        			dom.CntInBerkeley  = MysqlX.findGreplinesInFile(seeds2, "allseeds2", dom.Domain);
	        		}
	        		dom.LookedUpInBerkeley = true;
	    		}
				String table = MysqlRes.domainTableName(ds.level);
				DomainLevel entry = MysqlRes.readIfexistsDomain(conn, table, dom.Domain);
				if (entry.Domain.isEmpty()) {
					System.out.println("*** WARNING DOMAIN NOT FOUND domain " + dom.Domain );
	        	} else {
	        		entry.CntInBerkeley = dom.CntInBerkeley;    
	        		entry.LookedUpInBerkeley = dom.LookedUpInBerkeley;
	        		entry.calcCodes = dom.calcCodes;
	        		MysqlRes.updateDomainLevel(conn, MysqlRes.domainTableName(ds.level), dom); //updateBerkeleyDomainLevel
	        	}
	    	} else {
	    		System.out.println("*** ignoring domain " + dom.Domain );
	    	}
		}
	}
}
