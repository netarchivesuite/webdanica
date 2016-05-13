package dk.kb.webdanica.oldtools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.*;

import dk.kb.webdanica.oldtools.MysqlWorkFlow.HadoopResItem;
import dk.kb.webdanica.oldtools.MysqlX.*;

/*
String url = "jdbc:mysql://localhost/test|webdanica";
Class.forName ("com.mysql.jdbc.Driver").newInstance ();
Connection conn = DriverManager.getConnection (url, "username", "password");
*/

/*
Update TABLE ResFromHadoop with C15b varchar(20) with TLD
*/

public class MysqlStatExtract {

    /**
     * @param args <JDBC-URL> jdbcUser=<JDBC-username> dbmachine=<database machine-name> table=all|<tablename> datadir=<e.g. /data1/resultater/>   
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws SQLException 
     * @throws IOException 
     */

	private static String row_delim = MysqlX.row_delim;
	
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, IOException  {
    	String errArgTxt="Proper args: <JDBC-URL> jdbcUser=<JDBC-username> "
    			+ "dbmachine=<machine-name> "
    			//+ "seqno=<to separate from other statistics> "
    			+ "table=all|<tablename> "
    			+ "datadir=<e.g. /data1/resultater/> ";
    			//+ "stattype=kage|freq ";
    			//+ "codes=allCodes|positive|intervals|allStats " codes must be allCodes
    			//+ "displayCode=noCodes|onlyCodes|inText|separateText|allDisplays "; must be only codes

    	if (args.length < 5) {
            System.err.println("ERROR: Missing args!"); //maybe file
            System.err.println(errArgTxt);
            System.exit(1);
        }
        if (args.length > 5) {
            System.err.println("ERROR: Too many args!");
            System.err.println(errArgTxt);
            System.exit(1);
        }

        /**** args - jdbc ****/
        /** arg 0 - jdbc url **/
        String jdbcUrl = args[0];
        if (!jdbcUrl.startsWith("jdbc:mysql:")) {
            System.err.println("ERROR: Missing arg jdbc setting starting with 'jdbc:mysql'");
            System.err.println(errArgTxt);
            System.exit(1);
        }

        /** arg 1 - jdbc user **/
        String jdbcUser = args[1];
        if (!jdbcUser.startsWith("jdbcUser=")) {
            System.err.println("ERROR: Missing arg jdbcUser setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        jdbcUser = MysqlX.getStringSetting(jdbcUser).toLowerCase();
        
        Class.forName ("com.mysql.jdbc.Driver").newInstance ();
        Connection conn = DriverManager.getConnection (jdbcUrl, jdbcUser, "");    
        
        /**** args - dbmachine ****/
        /** arg - database machine name **/
        String dbmachine = args[2];
        if (!dbmachine.startsWith("dbmachine=")) {
            System.err.println("ERROR: Missing arg dbmachine setting - got " + dbmachine);
            System.err.println(errArgTxt);
            System.exit(1);
        }
        dbmachine = MysqlX.getStringSetting(dbmachine);

        /**** args - table(s) ****/
        /** arg - table all|<tablename> **/
        String tablename = args[3];
        if (!tablename.startsWith("table=")) {
            System.err.println("ERROR: Missing arg table setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        tablename = MysqlX.getStringSetting(tablename);

        /**** args - datadir files dir ****/
        String datadirTxt = args[4];
        if (!datadirTxt.startsWith("datadir=")) {
            System.err.println("ERROR: Missing arg datadir setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        datadirTxt = MysqlX.getStringSetting(datadirTxt);
        
        /**** args - stattype ****
        String stattypeTxt = args[5];
        if (!stattypeTxt.startsWith("stattype=")) {
            System.err.println("ERROR: Missing arg stattype setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        stattypeTxt = MysqlX.getStringSetting(stattypeTxt);
        StatType stattype = StatType.stat_none;
        if (stattypeTxt.equals("kage"))  stattype = StatType.stat_kage; 
        else if (stattypeTxt.equals("freq")) stattype = StatType.stat_freq; 
        else {
            System.err.println("ERROR: Arg stattype setting is not valid - got '" + stattypeTxt + "'");
            System.err.println(errArgTxt);
            System.exit(1);
        } */
        
        /*****************************************/
        /*** Start processing ********************/
        /*****************************************/

        
        // Set of tables to extract statistics from
        Set<String> tableSet = new HashSet<String>();
        if (tablename.equals("all")) {
	    	tableSet = MysqlRes.getTables(conn);
        } else {
        	tableSet.add( tablename.trim() );
        }
        
        ////////////////////////
        // for all tables on machine - make statistics and put in common place
        
        for (String nextTable: tableSet) {
        	if (nextTable.startsWith(MysqlRes.wf_table_prefix)) {
	        	//item with table and res-dir
    			System.out.println("Finding data for table: " + nextTable);
        		HadoopResItem item =  MysqlWorkFlow.readItemFromTablename(nextTable, datadirTxt, dbmachine, "");

        		// set item.IAdata 
    			int ia = findIsIASource(conn, item.tablename());
    			if (ia==-1) item.emptytable = true; 
    			else item.IAdata = (ia==1);

        		// set item.hadoopfileProduced
    			String hadoopmarkFilename = "";
    			if (item.interval.isEmpty()) { 
    				hadoopmarkFilename = item.datasubdir.getAbsolutePath() + "-v5/" + MysqlWorkFlow.wf_doneupdatenewHadoopfilename;
    			} else {
    				hadoopmarkFilename = item.datasubdir.getAbsolutePath();
    				hadoopmarkFilename = hadoopmarkFilename.replace(item.interval, "99x1-v5/" + MysqlWorkFlow.wf_doneupdatenewHadoopfilename);
    			}
    			System.out.println("hadoopmarkFilename: " + hadoopmarkFilename);
    				
    			
	        	File hadoopmark = new File(hadoopmarkFilename); 
	        	item.hadoopfileProduced = hadoopmark.exists();
        		
	        	//extractStatForItem(conn, item, StatType.stat_freq);
	    		System.out.println("Finding data for " + (item.hadoopfileProduced?"NEWHADOOP ":"") + "table: " + nextTable);
        		boolean ok = extractStatForItem(conn, item);
        		
        		//touch dstatupdae file
	        	if (ok) {
		        	String statmarkFilename = 
		        			item.datasubdir.getAbsolutePath() 
		        			+ "/" + MysqlWorkFlow.wf_donestatfilename 
		        			+ MysqlIngester.ingest_current_update_no;
		        	File statmark = new File(statmarkFilename);
		    		if (!statmark.exists()) {
		    			statmark.createNewFile(); //touch
		    		}
	    		} 
	        }
        }
        conn.close();
    }

	enum HadoopRuns {
		hadoops1,
		hadoops2
	}
	
	private static String getHrInfix(HadoopRuns sc) {
		String s = "";
		switch (sc) {
			case hadoops1: s="h1_"; break;
			case hadoops2: s="h2_"; break;
		}
		return s;
	}
	
	private static int getHrIndex(HadoopRuns sc) {
		int i = -1;
		switch (sc) {
			case hadoops1: i=0; break;
			case hadoops2: i=1; break;
		}
		return i;
	}
	
	public static boolean extractStatForItem(Connection conn, HadoopResItem item) throws IOException, SQLException {
    	Set<StatType> statSet = new HashSet<StatType>();
    	boolean goOn = true;
    	boolean ok = true;

    	//basis for lagkage/code-freq is in place
		String updatedFilename = item.datasubdir.getAbsolutePath() + "/" + MysqlWorkFlow.wf_updatefilename_prefix + MysqlIngester.ingest_current_update_no;
	    File updatedFile = new File(updatedFilename); 
		if (goOn && !updatedFile.exists()) {
			System.out.println("WARNING: the " + item.datasubdir.getAbsolutePath() + " dir is not updated to " + MysqlIngester.ingest_current_update_no);
			System.out.println("         therefore no stat was produced");
			ok = false;
		}


		//create stat file to write if it does not already exist
		//String statFilename = item.datadir.getAbsolutePath() + "/" + MysqlX.statistics_dir;
		String kageDir = item.datadir.getAbsolutePath() + "/" + MysqlX.statistics_dir + "/" + MysqlX.lagkageextract_dir;
		String freqDir = item.datadir.getAbsolutePath() + "/" + MysqlX.statistics_dir + "/" + MysqlX.frequenceextract_dir;
		String statSuffix = MysqlX.getStatFileSuffix(item, MysqlIngester.ingest_current_update_no); // MysqlX.allCodes, Display.onlyCodes
		File[][] kageExtractFileList = new File[Source.values().length][HadoopRuns.values().length]; 
		File[][] freqExtractFileList = new File[Source.values().length][HadoopRuns.values().length];
		
		if (ok && goOn) {
	        for (HadoopRuns sh : HadoopRuns.values()) {
	            for (Source src : Source.values()) {
	            	kageExtractFileList[getHrIndex(sh)][MysqlX.getSourceIndex(src)] =
		            		new File(kageDir + "/" + MysqlX.lagkage_fileprefix 
		    	        			+ MysqlX.getSourceInfix(src) 
		    	        			+ getHrInfix(sh) 
		    	        			+ statSuffix  + MysqlX.txtfile_suffix );
		            freqExtractFileList[getHrIndex(sh)][MysqlX.getSourceIndex(src)] =
		            		new File(freqDir + "/" + MysqlX.frequence_fileprefix 
		            				+ MysqlX.getSourceInfix(src) 
		    	        			+ getHrInfix(sh) 
		            				+ statSuffix + MysqlX.txtfile_suffix );
	            }
	        }
        }
        
    	File kageExtractFile = new File("dummy");
    	File freqExtractFile = new File("dummy");
    	Source actSrc = (item.IAdata ? Source.IA : Source.NAS);
    	HadoopRuns actHr = (item.hadoopfileProduced ? HadoopRuns.hadoops2 : HadoopRuns.hadoops1);

    	// find types of statistics to produce
    	if (ok && goOn) {
    		kageExtractFile = kageExtractFileList[getHrIndex(actHr)][MysqlX.getSourceIndex(actSrc)];
    		freqExtractFile = freqExtractFileList[getHrIndex(actHr)][MysqlX.getSourceIndex(actSrc)];
	        	
            if (kageExtractFile.exists()) {
				System.out.println("IGNORING: kage-stat file was already produced for table: " + item.tablename());
			} else {
				if (actHr==HadoopRuns.hadoops1) {
					String oldFn = kageExtractFile.getAbsolutePath().replace(getHrInfix(HadoopRuns.hadoops1), "");
					File oldf = new File(oldFn);
					if (oldf.exists()) {
						System.out.println("RENAMING: old kage-stat file: " + oldf.getName() + "to new: " + kageExtractFile.getName() + " for table " + item.tablename());
						oldf.renameTo(kageExtractFile);
					} else {
						statSet.add(StatType.stat_kage);
					}
				} else {
					statSet.add(StatType.stat_kage);
				}
			}

            if (freqExtractFile.exists()) {
				System.out.println("IGNORING: bit freq stat file was already produced for table: " + item.tablename() );
			} else {
				if (actHr==HadoopRuns.hadoops1) {
					String oldFn = freqExtractFile.getAbsolutePath().replace(getHrInfix(HadoopRuns.hadoops1), "");
					File oldf = new File(oldFn);
					if (oldf.exists()) {
						System.out.println("RENAMING: old freq-stat file: " + oldf.getName() + "to new: " + freqExtractFile.getName() + " for table " + item.tablename());
						oldf.renameTo(freqExtractFile);
					} else {
						statSet.add(StatType.stat_freq);
					}
				} else {
					statSet.add(StatType.stat_freq);
				}
			}
			goOn = statSet.size()>0;
		}

    	if (ok && goOn) {
			/** Find stat data */
			Statistics kagestat = new Statistics();
			Statistics freqstat = new Statistics();
			kagestat = findLagkageData(conn, item.tablename());
			if (statSet.contains(StatType.stat_freq)) {
				freqstat = findBitfrequenceData(kagestat);
			}
			
		    /** Write stat data */
		    //params
	    	Set<String> tableInSet = new HashSet<String>();
	    	tableInSet.add(item.tablename());
	        updateTotalUrls(kagestat);
			if (statSet.contains(StatType.stat_freq)) {
		        freqstat.totalurls = kagestat.totalurls;
		        freqstat.total0urls = kagestat.total0urls;
			}

			// for table qwrite stats to file IA/NAS hadoop1/hadoop2
			if (statSet.contains(StatType.stat_kage)) {
				List<Integer> sortedKageKeys = new ArrayList<Integer>(kagestat.countMap.size());
				sortedKageKeys.addAll(kagestat.countMap.keySet());
		        Collections.sort(sortedKageKeys);

		        System.out.println("Write kage to  " + kageExtractFile.getAbsolutePath());
		        kageExtractFile.createNewFile();
		        writeStatFile(kageExtractFile, StatType.stat_kage, kagestat, sortedKageKeys, Display.onlyCodes, tableInSet ); 
			}	        
			if (statSet.contains(StatType.stat_freq)) {
				List<Integer> sortedFreqKeys = new ArrayList<Integer>(freqstat.countMap.size());
				sortedFreqKeys.addAll(freqstat.countMap.keySet());
		        Collections.sort(sortedFreqKeys);

		        System.out.println("Write freq to  " + freqExtractFile.getAbsolutePath());
				freqExtractFile.createNewFile();
		        writeStatFile(freqExtractFile, StatType.stat_freq, freqstat, sortedFreqKeys, Display.onlyCodes, tableInSet );
			}
	        System.out.println("Resulted in file: " + kageExtractFile.getAbsolutePath() + " and " + freqExtractFile.getAbsolutePath());
		}
		return ok;
	}

	public static int findIsIASource(Connection conn, String tablename) throws SQLException {
		int isIA = -1;
		String selectSQL = "SELECT IsIASource FROM " + tablename + " LIMIT 1";
        PreparedStatement s = conn.prepareStatement(selectSQL); 
        ResultSet rs = s.executeQuery();
        if (rs.next()) {
			isIA = (rs.getBoolean("IsIASource")?1:0);
		} else {
			System.out.println("WARNING: empty table - could not find IAdata");
		}
	    s.close();
		return isIA;
	}

	public static Statistics findLagkageData(Connection conn, String tablename) throws SQLException {
		Statistics stat = new Statistics();
		//get inf. to lagkage - map of (code , count) from lagkage query
		stat.level = Level.allcodes;

        Long recCount = 0L;
        String selectSQL = "SELECT calcDanishCode, count(*) as cnt FROM " + tablename + " GROUP BY calcDanishCode";
        PreparedStatement s = conn.prepareStatement(selectSQL); 
        ResultSet rs = s.executeQuery();
        while (rs.next()) {
			recCount++;

			//update map
			int key = rs.getInt("calcDanishCode");
			long cntVal = rs.getLong("cnt");
			if (stat.countMap.containsKey(key)) {
				cntVal=cntVal + stat.countMap.get(key);
			}
			stat.countMap.put(key, cntVal);
		}
	    s.close();
		return stat;
	}

	public static Statistics findBitfrequenceData(Statistics kageStat) {
		Statistics stat = new Statistics();
		//get inf. to code freq stat - map of (code , count) from query
		// from inf. to lagkage - map of (code , count) from lagkage query

        //init stat map
		for (int i=1; i<=MysqlX.maxbit; i++) { 
			stat.countMap.put(i, 0L);
    	}

		for (int code : kageStat.countMap.keySet()) {
			//update map
			if (code < 0) { //otherwise set
				long val = kageStat.countMap.get(code);
				for (int i=1; i<=MysqlX.maxbit; i++) { 
					if (MysqlX.getBit((short)i, code)==1) {
						long newval = val + stat.countMap.get(i); 
						stat.countMap.put(i, newval);
					}
				}
			}
		}

		//for (int i=1; i<=maxbit; i++) { 
		//	System.out.println("stat " + i + " - " + stat.countMap.get(i));
    	//}

		
		return stat;
	}

	public static void updateTotalUrls(Statistics lk) {
        long total = 0L;
        long total0 = 0L;
		for (int key: lk.countMap.keySet()) {
		    Long value =  lk.countMap.get(key);
		    total = total + value;
		    if (key <= 0)  total0 = total0 + value;
		}
		lk.totalurls= total;
		lk.total0urls= total0;
	}
	
	public static void writeStatFile(File writeFile, StatType styp, Statistics lk, List<Integer> sortedKeys, Display display,  Set<String> tableSet) throws IOException {
		//make write possible on file
		FileWriter fw = new FileWriter(writeFile.getAbsoluteFile());
        BufferedWriter  bw = new BufferedWriter(fw);        

		//Set prefix
        String prefix = "";
        if (display==Display.noCodes || display==Display.onlyCodes || display==Display.inText) prefix = row_delim ; //text before count
        else if (display==Display.separateText) prefix = row_delim + row_delim ; //(number , text) before count
        else if (display==Display.allDisplays) prefix = row_delim + row_delim + row_delim + " " ; //(number , text(number), text) before count
        
        //make lines and write them
		bw.write(MysqlX.stat_tableline_start  + row_delim + MysqlX.getStringSequence(tableSet,MysqlX.tablename_delim));
		bw.newLine();

		if (styp==StatType.stat_kage) {
			bw.write(prefix + lagkage_title_start  + " - " + MysqlX.getLevelName(lk.level) + " - dispay: " + display.name());
		} else { //styp==StatType.stat_freq
			bw.write(prefix + codefrequence_title_start  + " - " + MysqlX.getLevelName(lk.level) + " - dispay: " + display.name());
		}
		bw.newLine();
		
		for (int key: sortedKeys) {
		    Long value =  lk.countMap.get(key);
		    
		    //System.out.println("value " + value);
		    
		    if (styp==StatType.stat_kage) {
			    bw.write(MysqlX.getCalcDkCodeText(key, display, lk.level,false) + row_delim + value);
			} else { //styp==StatType.stat_freq
			    bw.write(MysqlX.getBitCalcDkCodeText((short) key, display,false) + row_delim + value);
		    }
			bw.newLine();
		}
		
		bw.write(codefrequence_0totalline_start + row_delim + lk.total0urls);
		bw.newLine();

		bw.write(stat_totalline_start + row_delim + lk.totalurls);
		bw.newLine();
		
		bw.close();
	}
	

    public static String status_title_start = "Status for each item with belonging dir and table";
    public static String codefrequence_title_start = "Number of Urls having negative bits set for listed tables";
    public static String lagkage_title_start = "Number of Urls for listed tables";
    public static String stat_totalline_start = "Total"; 
    public static String codefrequence_0totalline_start = "Total of <=0"; 
}
