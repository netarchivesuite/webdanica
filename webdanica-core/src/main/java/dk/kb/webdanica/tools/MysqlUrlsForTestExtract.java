package dk.kb.webdanica.tools;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import dk.kb.webdanica.tools.MysqlWorkFlow.*;
import dk.kb.webdanica.tools.MysqlX.*;
import dk.kb.webdanica.tools.MysqlRes.*;

/*
String url = "jdbc:mysql://localhost/test|webdanica";
Class.forName ("com.mysql.jdbc.Driver").newInstance ();
Connection conn = DriverManager.getConnection (url, "username", "password");
*/

public class MysqlUrlsForTestExtract {
    /**
     * @param args <JDBC-URL> jdbcUser=<JDBC-username> machine=<machine-name> datadir=<dir for out-files> calcCode=<to extract urls for - 0 if none> bitno=<to extract urls for - 0 if none> bitset=<0 or 1> chooseCount=<number to be tested> table=all|<tablename> withvalues=true|false ignoreFile=true|false   
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws SQLException 
     * @throws IOException 
     */
	
	private static String row_delim = MysqlX.row_delim;
	private static int recLimit = 50000;
	//private static int urlCount = 1;
	
	// output on form Url # code # forklaring af code?????
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, SQLException {
        /*****************************************/
        /*** Arguments ***************************/
        /*****************************************/
    	String errArgTxt = "Proper args: <JDBC-URL> jdbcUser=<JDBC-username> "
    			+ "machine=<machine-name> "
    			+ "datadir=<data dir where 'url-dir' (for out-files) exixts> "
    			+ "calcCode=<to extract urls for - 0 if none> "
    			+ "bit=<to extract urls for - 0 if none>" 
    			+ "chooseCount=<number to be tested - to be written in header> "
    			+ "table=all|<tablename> "
    			+ "withvalues=<true|false>"
    			+ "ignoreFile=true|false ";
        if (args.length < 11) {
            System.err.println("Missing args!");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        if (args.length > 11) {
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
        if (!machine.startsWith("dbmachine=")) {
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
        File outDir = new File(dataDir.getAbsolutePath() + "/" + MysqlX.urlsfortest_dir);
        if (!outDir.isDirectory()) {
            System.err.println("ERROR: The given datadir + " + MysqlX.urlsfortest_dir + ": '"+ outDir.getAbsolutePath() + "' is not a proper directory or does not exist");
            System.err.println(errArgTxt);
            System.exit(1);
        }

		/**** calcCode ****/
        /** args - calcCode **/
        String calcCodeTxt = args[4];
        if (!calcCodeTxt.startsWith("calcCode=")) {
            System.err.println("ERROR: Missing arg calcCode setting - got " + calcCodeTxt);
            System.err.println(errArgTxt);
            System.exit(1);
        }
        calcCodeTxt = MysqlX.getStringSetting(calcCodeTxt);
        int calcCode = 0;
    	if (calcCodeTxt.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+")) {
    		calcCode = Integer.parseInt(calcCodeTxt);
    	} else {
            System.err.println("arg calCode is not an integer got: " + calcCodeTxt);
            System.err.println(errArgTxt);
            System.exit(1);
    	} 

		/**** bitno ****/
        String bitnoTxt = args[5];
        if (!bitnoTxt.startsWith("bitno=")) {
            System.err.println("ERROR: Missing arg bitno setting - got " + bitnoTxt);
            System.err.println(errArgTxt);
            System.exit(1);
        }
        bitnoTxt = MysqlX.getStringSetting(bitnoTxt);
        int bitno = 0;
    	if (bitnoTxt.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+")) {
    		bitno = Integer.parseInt(bitnoTxt);
    	} else {
            System.err.println("arg bitno is not an integer");
            System.err.println(errArgTxt);
            System.exit(1);
    	} 

		/**** bitset ****/
        String bitsetTxt = args[6];
        if (!bitsetTxt.startsWith("bitset=")) {
            System.err.println("ERROR: Missing arg bitset setting - got " + bitsetTxt);
            System.err.println(errArgTxt);
            System.exit(1);
        }
        bitsetTxt = MysqlX.getStringSetting(bitsetTxt);
        int bitset = 0;
    	if (bitsetTxt.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+")) {
    		bitset = Integer.parseInt(bitsetTxt);
    	} else {
            System.err.println("arg bitset is not an integer");
            System.err.println(errArgTxt);
            System.exit(1);
    	} 

        /**** choose count ****/
        /** args - choose count **/
        String cntTxt = args[7];
        if (!cntTxt.startsWith("chooseCount=")) {
            System.err.println("ERROR: Missing arg chooseCount setting - got " + cntTxt);
            System.err.println(errArgTxt);
            System.exit(1);
        }
        cntTxt = MysqlX.getStringSetting(cntTxt);
        int chooseCount = 0;
    	if (cntTxt.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+")) {
    		chooseCount = Integer.parseInt(cntTxt);
    	} else {
            System.err.println("arg calCode is not an integer");
            System.err.println(errArgTxt);
            System.exit(1);
    	} 

    	/**** args - table(s) ****/
        /** arg - table all|<tablename> **/
        String tablename = args[8];
        if (!tablename.startsWith("table=")) {
            System.err.println("ERROR: Missing arg table setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        tablename = MysqlX.getStringSetting(tablename);


        /**** args - withvalues ****/
        String withvaluesTxt = args[9];
        if (!withvaluesTxt.startsWith("withvalues=")) {
            System.err.println("ERROR: Missing arg withvalues setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        boolean withvalues = MysqlX.getBoleanSetting(withvaluesTxt);
        
        /**** args - ignore file ****/
        String ignoreFileTxt = args[10];
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
        
        /*****************************************/
        /*** Start processing ********************/
        /*****************************************/
        /* for each table
         * - extract first 50000 (recLimit)
         * - choose randomly 1 urls (urlCount)
         * write to file
         */ 
        
        /****** 
        /** Set of tables to extract URLs from */
        System.out.println("*** Finding tables ");
        Set<String> tableSet = new HashSet<String>();
	    if (tablename.equals("all")) {
	    	tableSet = MysqlRes.getTables(conn);
	    } else {
	    	tableSet.add( tablename.trim() );
	    }
        
        Set<String> IAtableSet = new HashSet<String>();
        Set<String> NAStableSet = new HashSet<String>();
        for (String tablenm : tableSet) {
        	if (tablenm.startsWith(MysqlRes.wf_table_prefix)) {
	        	if (MysqlX.isIAtablename(tablenm)) {
	        		IAtableSet.add(tablenm);
            	} else if (MysqlX.isNAStablename(tablenm)) { 
            		NAStableSet.add(tablenm);
            	} else { 
    	            System.out.println("*WARNING: table not IA nor NAS: " + tablenm);
            	}
        	}
        }
	    
        /****** 
        /** Set of Urls to be writen to file */
        for (Source src : Source.values()) {
            System.out.println("*** Finding Urls for " + (src==Source.IA? "IA" : "NAS") + " data");
            Set<SingleCriteriaResult> resSet = new HashSet<SingleCriteriaResult>();
            resSet = getResesForTest(conn, (src==Source.IA? IAtableSet : NAStableSet) , calcCode, bitno, bitset, withvalues);
            
            /****** 
            /** Write Set of Urls to file */
            System.out.println("*** writing outfile ");
            String filename = writeTestReses(outDir, machine, src, calcCode, bitno, bitset, chooseCount, (src==Source.IA? IAtableSet : NAStableSet), resSet, withvalues, ignoreFile);
        	System.out.println("- resulted in file:" + filename);
        }
        conn.close();
    }
    
	public static String writeTestReses(File outDir, String machine, Source src, int calcCode, int bitno, int bitset, int chooseCount, Set<String> tableSet, Set<SingleCriteriaResult> resSet, boolean withvalues, IgnoreFile ignoreFile) throws IOException {
		//create file
        String fn = getFilename(machine, src, calcCode, bitno, bitset) + MysqlX.txtfile_suffix;
        File writeFile = new File(outDir.getAbsolutePath() + "/" + fn);
		if (writeFile.exists() && ignoreFile==IgnoreFile.if_false) {
            System.err.println("ERROR: file allready existed " + writeFile.getAbsolutePath() + writeFile.getName());
            System.exit(1);
		} else {
			writeFile.createNewFile();
		}
		
		
		FileWriter fw = new FileWriter(writeFile.getAbsoluteFile());
        BufferedWriter  bw = new BufferedWriter(fw);        

        //make lines and write them
        String tableTxt =  MysqlX.urlextract_tableline_start  + row_delim + MysqlX.getStringSequence(tableSet,MysqlX.tablename_delim);
        String hdTxt = MysqlX.urlextract_title_start  + "TAKE " + chooseCount + "URLS --- ";
        if (bitno>0) {
        	hdTxt = hdTxt + "for bit no." + bitno
        			+ row_delim +  MysqlX.getBitCalcDkCodeText((short) bitno, Display.inText, false) + " with value '" + bitset + "'"
        			+ row_delim +  MysqlX.getBitCalcDkCodeText((short) bitno, Display.inText, true) + " with value '" + bitset + "'";
        	hdTxt = hdTxt + row_delim;
        	tableTxt = tableTxt + row_delim;
        } else if (calcCode>=0) {
        	hdTxt = hdTxt + "for calcDanishCode "+ calcCode 
        			+ row_delim +  MysqlX.getCalcDkCodeText(calcCode, Display.noCodes , Level.none, false)
        			+ row_delim +  MysqlX.getCalcDkCodeText(calcCode, Display.noCodes , Level.none, true);
        	tableTxt = tableTxt + row_delim ;
        }
		if (withvalues) { //to 3 + 2
        	hdTxt = hdTxt + row_delim + row_delim + row_delim + row_delim; //to 3
        	tableTxt = tableTxt + row_delim + row_delim + row_delim + row_delim;
		} else { //to 41 + 2
			for (int i=1; i<=43; i++) {
				hdTxt = hdTxt + row_delim;
				tableTxt = tableTxt + row_delim;
			}
		}

        bw.write(tableTxt);
		bw.newLine();
		bw.write(hdTxt);
		bw.newLine();
		
		//System.out.println("urlSet.count " + urlSet.size());
		for (SingleCriteriaResult res: resSet) {
			String urlTxt = (res.urlOrig.isEmpty() ? res.url : res.urlOrig);
			// IA-url fx. http://web.archive.org/web/20120812090132/http://www.yachtworld.com/boats/1975/Hubber-Custom-Sport-Fisherman-2398590/Miami/FL/United-States 
			// dvs. web.archive.org/web/[date]/[URL]
			String IAUrl = "http://web.archive.org/web/" + res.Cext3Orig + "/" + urlTxt;

			if (withvalues) {
				bw.write(
						( res.IsIASource ? IAUrl : urlTxt ) 
	        			+ row_delim + urlTxt  
	        			+ row_delim + res.Cext3Orig
	        			+ row_delim + machine
	        			+ row_delim + res.tablename
						+ row_delim + res.getValuesInString(row_delim, ":")
				); //41 + 2
			} else {
		        bw.write(IAUrl 
		        			+ row_delim + urlTxt  
		        			+ row_delim + res.Cext3Orig  
		        			+ row_delim + machine
		        			+ row_delim + res.tablename
		        			+ row_delim + res.calcDanishCode
		        		); // 3 rows + 2
			}
			bw.newLine();
		}
		bw.close();
		return fn;
	}


	private static SingleCriteriaResult getResForTestFromTable(Connection conn, String tablename, int  calcCode, boolean withValues) throws SQLException {
        int recCount = 0;

        CodesSizeIntervals[] intsList = CodesSizeIntervals.values();
        Set<CodesSizeIntervals> intsSet = new HashSet<CodesSizeIntervals>(Arrays.asList(intsList));
        RsRes rsRes = findRandomIntervalRecordset(conn, tablename, calcCode, intsSet);

        SingleCriteriaResult res = new SingleCriteriaResult();
        if (rsRes.rowCount>0) {
	        int act_recLimit = (rsRes.rowCount < recLimit ? rsRes.rowCount : recLimit );
	        Integer pos = MysqlX.randomFromInterval(1, act_recLimit);
	        
	        while (rsRes.rs.next()) {
				recCount++;
				if (pos==recCount) { //(posSet.contains(recCount)) {
			        res.url = rsRes.rs.getString("Url");
			        res.urlOrig = rsRes.rs.getString("UrlOrig");
			        res.Cext3 = rsRes.rs.getTimestamp("extWDate");
			        res.Cext3Orig = rsRes.rs.getString("extWDateOrig");
			        res.calcDanishCode = rsRes.rs.getInt("calcDanishCode");
			        break;
				}
			}
        }
        rsRes.rs.close();
        
 	    if (withValues) {
 	    	boolean extendedNewHadoop = !MysqlUpdateTables.hasNoNewHadoopFields(conn, tablename);
 	    	String selectSQL = "SELECT * FROM " + tablename + " WHERE Url = ? AND extWDate = ? ";
 	        PreparedStatement r = conn.prepareStatement(selectSQL); 
 	        r.setString(1, res.url);
 	        r.setTimestamp(2, res.Cext3);
 	        ResultSet frs = r.executeQuery();
 	    	if (frs.next()) {
				SingleCriteriaResult scr = new SingleCriteriaResult(frs, extendedNewHadoop);
				res = scr;
 	    	}
			r.close();
	   }
	   return res;
	}


	private static RsRes findRandomIntervalRecordset(Connection conn, String tablename, int calcCode, Set<CodesSizeIntervals> intsSet) throws SQLException {
	    if (intsSet.size()==0) {
	    	RsRes res = new RsRes();
        	return res;
	    }

	    //random size interval
	    int interval = MysqlX.randomFromInterval(1, intsSet.size());
        CodesSizeIntervals ci = MysqlX.getSetItemCodesSizeIntervals(intsSet, interval);

        String whereStmt = "calcDanishCode = " + calcCode + " AND " + MysqlX.getWhereInterval(MysqlX.getIntervalNormal(ci));
        String selectSQL = "SELECT Url, UrlOrig, extWDate, extWDateOrig, calcDanishCode FROM " + tablename + " WHERE " + whereStmt + " LIMIT " + recLimit;
        System.out.println("selectSQL: "+ selectSQL);
        PreparedStatement s = conn.prepareStatement(selectSQL); 
        ResultSet rs = s.executeQuery();
        
        int rowcnt = 0;
        if (rs.last()) {
        	rowcnt = rs.getRow();
        	rs.beforeFirst(); // not rs.first() because the rs.next() below will move on, missing the first element
        } else {
	        rowcnt = 0;
	        intsSet.remove(ci);
        }
        System.out.println("rowcnt: "+ rowcnt);
	        
        if (rowcnt>0 || intsSet.size()==0) {
        	RsRes res = new RsRes(rs, rowcnt);
        	return res;
        } else { 
	        rs.close();
        	return findRandomIntervalRecordset(conn, tablename, calcCode, intsSet);
        } 	
	}
	
	private static Set<SingleCriteriaResult> getResesForTest(Connection conn, Set<String> tableSet, int calcCode, int bitno, int bitset, boolean withValues) throws SQLException {
		Set<SingleCriteriaResult> resSet = new HashSet<SingleCriteriaResult>();
		for (String nextTable: tableSet) {
            System.out.println("- processing table: " + nextTable);
			if (nextTable.startsWith(MysqlRes.wf_table_prefix)) {
				if (calcCode>=0 && bitno==0) {
					SingleCriteriaResult res = getResForTestFromTable(conn,  nextTable, calcCode, withValues);
					res.tablename = nextTable;
					if (!res.url.isEmpty()) {
						resSet.add(res);
					}
				} else if (bitno!=0) {
					//chose a arbitrary calcDanishCode with bit set
	        		int bitCalcCode = 0; // random code from bitCodeSet
			        Set<Integer> codeSet = MysqlRes.getCodeSet(conn, nextTable, CodesFraction.codes_nonpositive);  //all negative
			        Set<Integer> bitCodeSet = new HashSet<Integer>();  //all negative with bitset
			        for (int i : codeSet) {
				    	if ((i<0) && (MysqlX.getBit((short)bitno, i)==bitset)) {
				    		bitCodeSet.add(i);
				    	}
			        }
			        int limit = bitCodeSet.size();
			        if (limit==0) {
			            System.out.println("WARNING: Empty bitcode-set");
			        } else {
				        Integer pos = MysqlX.randomFromInterval(1, limit);
				        int actPos = 1; 
				        for (int i : bitCodeSet) {
				        	if (actPos == pos) {
				        		bitCalcCode = i; 
				        	}
				        	actPos++;
			        	}
	
				        //find record	
						SingleCriteriaResult res = getResForTestFromTable(conn,  nextTable, bitCalcCode, withValues);				
						res.tablename = nextTable;
						resSet.add(res);
			        }
				} else {
		            System.err.println("Nothing to process");
		            System.exit(1);
				}
			}
		}
		return resSet;
	}

    private static String getFilename(String machine, Source src, int calcode, int bitno, int bitset) {
    	//SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    	//Date now = new Date(System.currentTimeMillis());
    	String s = MysqlX.urlextract_fileprefix 
    			+ src.name() 
    			+ "_M" + machine 
    			+ "_C" + calcode 
    			+ "_B" + bitno + "-s" + bitset
    			+ "_V" + MysqlIngester.ingest_current_update_no; //"_T" + dateFormat.format(now);
        return s;
    }
}
