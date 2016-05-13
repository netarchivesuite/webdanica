package dk.kb.webdanica.oldtools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.IOException;

import dk.kb.webdanica.oldtools.MysqlWorkFlow.IgnoreFile;
import dk.kb.webdanica.oldtools.MysqlX.*;

public class MysqlDomainStatsExtracts {
    /**
     * @param args <JDBC-URL> jdbcUser=<JDBC-username> machine=<machine-name> datadir=<dir for out-files> level=all|l1|l1_l2|l2|l3 table=machine|newall|nyeall setting stat=all|domain|code|multicode ignoreFile=true|false|warning    
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws SQLException 
     * @throws IOException 
     */

    // output on form Url # code # forklaring af code?????
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, SQLException {
        /*****************************************/
        /*** Arguments ***************************/
        /*****************************************/
    	String errArgTxt = "Proper args: <JDBC-URL> jdbcUser=<JDBC-username> "
    			+ "machine=<machine-name> "
    			+ "datadir=<data dir where 'url-dir' (for out-files) exixts> "
    			+ "level=all|l1|l1_l2|l2|l3 "
    			+ "table=machine|newall|nyeall "
    			+ "stat=IA_NAS|overview|codes "
    			+ "ignoreFile=true|false|warning ";
        if (args.length < 8) {
            System.err.println("Missing args!");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        if (args.length > 8) {
            System.err.println("Too many args! expect 8 got " +  args.length );
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
		File domDir =  new File(dataDir.getAbsolutePath() + "/" + MysqlX.statistics_dir + "/" + MysqlX.domainextract_dir);
        if (!domDir.isDirectory()) {
            System.err.println("ERROR: The given data-dir does not contains file '" + domDir.getAbsolutePath() + "'");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        
        /**** args - level ****/
        LookupLevel pLevel = LookupLevel.all;
        String pLevelTxt = args[4];
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

        /** arg - table **/
        String tableTxt = args[5];
        if (!tableTxt.startsWith("table=")) {
            System.err.println("ERROR: Missing arg  table=machine|newall|nyeall setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        tableTxt = MysqlX.getStringSetting(tableTxt);
        if ((!tableTxt.equals("machine")) && (!tableTxt.equals("newall")) && (!tableTxt.equals("nyeall"))) {
            System.err.println("ERROR: illigal arg  table=machine|newall|nyeall setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }

        /** arg - stat type **/
        StatType statType = StatType.all;
        String statTypeTxt = args[6];
        if (!statTypeTxt.startsWith("stat=")) {
            System.err.println("Missing arg level setting - got " + statTypeTxt);
            System.err.println(errArgTxt);
            System.exit(1);
        }
        statTypeTxt = MysqlX.getStringSetting(statTypeTxt);
    	if (statTypeTxt.startsWith("all")) {
    		statType = StatType.all;        	
        } else if (statTypeTxt.startsWith("IA_NAS")) {
        	statType = StatType.IA_NAS;      
        } else if (statTypeTxt.startsWith("overview")) {
        	statType = StatType.overview;      
        } else if (statTypeTxt.startsWith("codes")) {
        	statType = StatType.codes;      
        } else {
            System.err.println("Arg stat type setting is NOT valid - got '" + statTypeTxt + "'");
            System.err.println(errArgTxt);
            System.exit(1);
        }

        /** arg - ignoreFileTxt **/
        String ignoreFileTxt = args[7];
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

    	//statfiles
        File[] statOverviewFiles = new File[MysqlX.noDomainLevels];
        File[] statIANASFiles = new File[MysqlX.noDomainLevels];
        File[] statCodesFiles = new File[MysqlX.noDomainLevels];
        File statUrlsLev1NasNotInNA = new File(domDir.getAbsoluteFile() + "/" 
       										+ getExtractDomainFilename(machine, "UrlsNasNotInNA_", 1)
       										+ MysqlX.txtfile_suffix );
        File statUrlsLev1IaNotInNA= new File(domDir.getAbsoluteFile() + "/" 
											+ getExtractDomainFilename(machine, "UrlsIaNotInNA_", 1)
											+ MysqlX.txtfile_suffix );
        File statUrlsLev1IaNasNotInNA= new File(domDir.getAbsoluteFile() + "/" 
				+ getExtractDomainFilename(machine, "UrlsIaNasNotInNA_", 1)
				+ MysqlX.txtfile_suffix );
        for (int level=1;level<=MysqlX.noDomainLevels ;level++) {
        	if ( !MysqlX.skipLevel(pLevel, level) ) {
                System.out.println("Make files for level " + level);
	        	int i = level -1;
	        	
	        	File statOverviewExtractFile = 
	            		new File(domDir.getAbsoluteFile() + "/"
	            		          + getExtractDomainFilename(machine, "Overview_", level)
	            		          + MysqlX.txtfile_suffix );
	            statOverviewFiles[i] = statOverviewExtractFile;
	            
	            File statIANASExtractFile = 
	            		new File(domDir.getAbsoluteFile() + "/" 
	            		          + getExtractDomainFilename(machine, "IANA_", level)
	            		          + MysqlX.txtfile_suffix );
	            statIANASFiles[i] = statIANASExtractFile;

	            File statCodesExtractFile = 
	            		new File(domDir.getAbsoluteFile() + "/" 
	            		          + getExtractDomainFilename(machine, "Codes_", level)
	            		          + MysqlX.txtfile_suffix );
	            statCodesFiles[i] = statCodesExtractFile;
        	}
        }
        for (File f: statOverviewFiles) if (!(f==null)) createFile(f, ignoreFile);        	
        for (File f: statIANASFiles) if (!(f==null)) createFile(f, ignoreFile);
        for (File f: statCodesFiles) if (!(f==null)) createFile(f, ignoreFile);

        
        DomStat[] stats = new DomStat[MysqlX.noDomainLevels];
        CodeStats[] codestats = new CodeStats[MysqlX.noDomainLevels];
        Set<String> urlsLev1NasNotInNAstat = new HashSet<String>();
        Set<String> urlsLev1IaNotInNAstat = new HashSet<String>();
        Set<String> urlsLev1IaNasNotInNAstat = new HashSet<String>();
        
        for (int level=1;level<=MysqlX.noDomainLevels;level++) {
        	DomStat stat = new DomStat();
        	CodeStats codestat = new CodeStats();
        	if ( !MysqlX.skipLevel(pLevel, level) ) {
	        	stat = calcDomStat(conn, tableTxt, statType, level);
	        	codestat = calcCodeStats(conn, tableTxt, statType, level);

        		if (level==1) {
        	        urlsLev1NasNotInNAstat = calcUrlsNotInNA(conn, DataSource.source_NAS, 1, tableTxt);
        	        urlsLev1IaNotInNAstat = calcUrlsNotInNA(conn, DataSource.source_IA, 1, tableTxt);
        	        urlsLev1IaNasNotInNAstat = calcUrlsNotInNA(conn, DataSource.source_none, 1, tableTxt);
            	}
        	}
        	stats[level-1] = stat;
        	codestats[level-1] = codestat;
        }
        
    	conn.close();
        	
        for (int level=1;level<=MysqlX.noDomainLevels;level++) {
        	int i = level-1;
        	if ( !MysqlX.skipLevel(pLevel, level) ) {
        		if (!(statIANASFiles[i]==null)) writeDomainStatNASIAFile(stats[i], statIANASFiles[i], machine, level);
        		if (!(statIANASFiles[i]==null)) writeDomainStatOverviewFile(stats[i], statOverviewFiles[i], machine, level);
        		if (!(statIANASFiles[i]==null)) writeDomainStatOverviewFile(stats[i], statOverviewFiles[i], machine, level);
        		if (level==1) {
        	        writeurlsLev1File(DataSource.source_NAS, urlsLev1NasNotInNAstat, statUrlsLev1NasNotInNA, "Danish NAS urls NOT in Nestarkivet", machine,level);
        	        writeurlsLev1File(DataSource.source_IA, urlsLev1IaNotInNAstat, statUrlsLev1IaNotInNA, "Danish IA urls NOT in Nestarkivet", machine, level);
        	        writeurlsLev1File(DataSource.source_none, urlsLev1IaNasNotInNAstat, statUrlsLev1IaNasNotInNA, "Danish IA and NAS urls NOT in Nestarkivet", machine, level);
        		}
            }
        }
    }
    

    public static String getExtractDomainFilename(String machine, String infix, int level) {
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    	Date now = new Date(System.currentTimeMillis());
    	String s = MysqlX.domain_fileprefix 
    			+ infix
    			+ "M" + machine + "_"
        		+ "L" + level + "_"
				+ "T" + dateFormat.format(now);
        return s;
    }
    
    private static boolean createFile(File f, IgnoreFile ignoreFile) {
		if (f.exists()) {
			if (ignoreFile.equals(IgnoreFile.if_false)) {
	            System.err.println("ERROR: merge file allready existed " + f.getAbsolutePath() + f.getName());
	            System.exit(1);
			} else if (ignoreFile.equals(IgnoreFile.if_warning)) {
	            System.out.println("WARNING: merge file allready existed " + f.getAbsolutePath());
			} 
		} 
		return true;
	} 

    enum StatType {
    	all, overview, IA_NAS, codes
    }

    
    public static class DomStat {
        // Stat 1: Update status for all
        // #entries, #ia/not nas, #not ia/nas, #ia/nas, # not ia/not nas (not possible)

        // Stat 2+3 : In berkely for IA and NAS
        // #entries, #lookedup, #exists
    	public int level = 0;
    	public long occurDomains = 0;		//writeDomainStatusFile
    	public long cntDomains = 0;			//writeDomainStatusFile
    	public long existsInBerkeley = 0;	//writeDomainDataFile
    	public long lookedupInBerkeley = 0;	//writeDomainDataFile 
    	public long cntTables = 0;												

    	public long IA_NAS = 0;				//writeDomainStatusFile
    	public long IA_notNAS = 0;			//writeDomainStatusFile
    	public long notIA_NAS = 0;			//writeDomainStatusFile
    	public long notIA_notNAS = 0;		//writeDomainStatusFile
    	
    	public long NAS_existsInBerkeley = 0;	//writeDomainDataFile
    	public long NAS_lookedupInBerkeley = 0;	//writeDomainDataFile
    	public long NAS_occurcount = 0; 		//writeDomainDataFile
    	public long NAS_entrycount = 0;			//writeDomainDataFile

    	public long IA_existsInBerkeley = 0;	//writeDomainDataFile
    	public long IA_lookedupInBerkeley = 0;	//writeDomainDataFile
    	public long IA_occurcount = 0;			//writeDomainDataFile
    	public long IA_entrycount = 0;			//writeDomainDataFile

    	public long IANAS_existsInBerkeley = 0;	//writeDomainDataFile
    	public long IANAS_lookedupInBerkeley = 0;	//writeDomainDataFile
    	public long IANAS_occurcount = 0;			//writeDomainDataFile
    	public long IANAS_entrycount = 0;			//writeDomainDataFile
    }

    public static class DomainStat { 
        // Stat 1: Update status for all
        // #entries, #ia/not nas, #not ia/nas, #ia/nas, # not ia/not nas (not possible)

        // Stat 2+3 : In berkely for IA and NAS
        // #entries, #lookedup, #exists
    	String maxlevel = ""; //l1, l2, l3, above 
    	public int cntDomains = 0;

    	public long IA = 0;
    	public long NAS = 0;
    	public long IA_AND_NAS = 0;

    	public long IA_AND_InBerkeley = 0;
    	public long NAS_AND_InBerkeley  = 0;
    	public long IA_AND_NAS_AND_InBerkeley  = 0;

    	public long IA_AND_LookedupInBerkeley = 0;
    	public long NAS_AND_LookedupInBerkeley  = 0;
    	public long IA_AND_NAS_AND_LookedupInBerkeley  = 0;
    }

    
    public static Set<String> calcUrlsNotInNA (Connection conn, DataSource src, int level, String tableType) throws SQLException {
    	Set<String> urlSet = new HashSet<String>();
    	
        System.out.println("Finding urls for level: " + level + " src " + src.name());
    	String table = "";
    	if (tableType.equals("machine")) {
    		table = MysqlRes.domainTableName(level);
    	} else if (tableType.equals("newall")) {
    		table= MysqlRes.domainNewTableName(level) + "_ALL";
    	} else if (tableType.equals("nyeall")) {
    		table= MysqlRes.domainNyeTableName(level) + "_ALL";
    	}
    	
        Set<String> allTableSet = MysqlRes.getTables(conn);
    	if (!allTableSet.contains(table)) {
            System.out.println("table: " + table + "was not there");
    	} else {
	        System.out.println("Finding " + src.name() + " level " + level + "not in Netarkivet");
	        String w = "";
	        switch (src) {
	          case source_NAS: w = "(CntInNasData>0 AND CntInIaData=0)"; break;
	          case source_IA: w = "(CntInNasData=0 AND CntInIaData>0)"; break;
	          case source_none: w = "(CntInNasData>0 AND CntInIaData>0)"; break;
	        }
	        urlSet = MysqlRes.getTableUrlEntries(conn, table, w + " AND CntInBerkeley>0 AND LookedUpInBerkeley!=0");
    	}
    	return urlSet;
    }

    public static DomStat calcDomStat (Connection conn, String tableType, StatType statType, int level) throws SQLException {
    	DomStat stat = new DomStat();	
    	stat.level = level;

        System.out.println("Finding stat for level: " + level);
    	String table = "";
    	if (tableType.equals("machine")) {
    		table = MysqlRes.domainTableName(level);
    	} else if (tableType.equals("newall")) {
    		table= MysqlRes.domainNewTableName(level) + "_ALL";
    	} else if (tableType.equals("nyeall")) {
    		table= MysqlRes.domainNyeTableName(level) + "_ALL";
    	}
    	
        Set<String> allTableSet = MysqlRes.getTables(conn);
    	if (!allTableSet.contains(table)) {
            System.out.println("table: " + table + "was not there");
    	} else {

	    	System.out.println("Finding cntDomains");
	    	stat.cntDomains = MysqlRes.getTableCntEntries(conn, table, "");
	    	
	    	if (statType.equals(StatType.all) || statType.equals(StatType.IA_NAS)) {
		        System.out.println("Finding IA_NAS");
		    	stat.IA_NAS = MysqlRes.getTableCntEntries(conn, table, 
		    						"CntInIaData>0 AND CntInNasData>0"); 
		        System.out.println("Finding IA_notNAS");
		    	stat.IA_notNAS = MysqlRes.getTableCntEntries(conn, table, 
		    						"CntInIaData>0 AND CntInNasData=0");
		        System.out.println("Finding notIA_NAS");
		    	stat.notIA_NAS = MysqlRes.getTableCntEntries(conn, table, 
		    						"CntInIaData=0 AND CntInNasData>0");
		        System.out.println("Finding notIA_notNAS");
		    	stat.notIA_notNAS = MysqlRes.getTableCntEntries(conn, table, 
		    						"CntInIaData=0 AND CntInNasData=0");
	    	}
	
	    	if (statType.equals(StatType.all) || statType.equals(StatType.overview)) {

		        System.out.println("Finding NAS_entrycount");
		    	stat.NAS_entrycount = MysqlRes.getTableCntEntries(conn, table, "CntInNasData>0 AND CntInIaData=0"); 
		        System.out.println("Finding NAS_occurcount");
		    	stat.NAS_occurcount = MysqlRes.findSumDomains(conn, table, "CntInNasData", "CntInIaData=0"); 
		    	System.out.println("Finding NAS_existsInBerkeley");
		    	stat.NAS_existsInBerkeley = MysqlRes.getTableCntEntries(conn, table, "CntInNasData>0 AND CntInIaData=0 AND CntInBerkeley>0");
		    	System.out.println("Finding NAS_lookedupInBerkeley");
		    	stat.NAS_lookedupInBerkeley = MysqlRes.getTableCntEntries(conn, table, "CntInNasData>0 AND CntInIaData=0 AND LookedUpInBerkeley!=0");
	
		        System.out.println("Finding IA_entrycount");
		    	stat.IA_entrycount = MysqlRes.getTableCntEntries(conn, table, "CntInIaData>0 AND CntInNasData=0");
		        System.out.println("Finding IA_occurcount");
		    	stat.IA_occurcount = MysqlRes.findSumDomains(conn, table, "CntInIaData", "CntInNasData=0");
		        System.out.println("Finding IA_existsInBerkeley");
		    	stat.IA_existsInBerkeley = MysqlRes.getTableCntEntries(conn, table, "CntInIaData>0 AND CntInNasData=0 AND CntInBerkeley>0");
		    	System.out.println("Finding IA_lookedupInBerkeley");
		    	stat.IA_lookedupInBerkeley = MysqlRes.getTableCntEntries(conn, table, "CntInIaData>0 AND CntInNasData=0 AND LookedUpInBerkeley!=0");


		        System.out.println("Finding IANAS_entrycount");
		    	stat.IANAS_entrycount = MysqlRes.getTableCntEntries(conn, table, "CntInIaData>0 AND CntInNasData>0");
		        System.out.println("Finding IANAS_occurcount");
		    	stat.IANAS_occurcount = MysqlRes.findSumDomains(conn, table, "CntInIaData", "CntInNasData>0")
		    							+ MysqlRes.findSumDomains(conn, table, "CntInNasData", "CntInIaData>0");
		        System.out.println("Finding IANAS_existsInBerkeley");
		    	stat.IANAS_existsInBerkeley = MysqlRes.getTableCntEntries(conn, table, "CntInIaData>0 AND CntInNasData>0 AND CntInBerkeley>0");
		    	System.out.println("Finding IANAS_lookedupInBerkeley");
		    	stat.IANAS_lookedupInBerkeley = MysqlRes.getTableCntEntries(conn, table, "CntInIaData>0 AND CntInNasData>0 AND LookedUpInBerkeley!=0");


		    	System.out.println("Finding occurDomains");
		    	stat.occurDomains = stat.NAS_occurcount + stat.IA_occurcount +  stat.IANAS_occurcount; 

		    	System.out.println("Finding existsInBerkeley");
		    	stat.existsInBerkeley = stat.IA_existsInBerkeley  + stat.NAS_existsInBerkeley  + stat.IANAS_existsInBerkeley;
	
		        System.out.println("Finding existsInBerkeley");
		    	stat.lookedupInBerkeley = stat.IA_lookedupInBerkeley + stat.NAS_lookedupInBerkeley + stat.IANAS_lookedupInBerkeley;
	
	    	}
    	}
    	return stat;
    }

    
    public static boolean writeurlsLev1File(DataSource src, Set<String> urlSet, File f, String title, String machine, int level) throws IOException {
        // Stat 1: Update status for all
        // #entries, #ia/not nas, #not ia/nas, #ia/nas, # not ia/not nas (not possible)
		FileWriter fw = new FileWriter(f.getAbsoluteFile());
        BufferedWriter  bw = new BufferedWriter(fw);        

        String s =  "Source: " + src.name() 
        				+ ", Level: " + level
        				+ ", Machine: " + machine;
		bw.write(s); bw.newLine();
		bw.write(title); bw.newLine();
		for (String u: urlSet) {
			bw.write(u); bw.newLine();
		}
		
		bw.close();
    	return true;
    }

    public static boolean writeDomainStatNASIAFile(DomStat domStat, File f, String machine, int level) throws IOException {
        // Stat 1: Update status for all
        // #entries, #ia/not nas, #not ia/nas, #ia/nas, # not ia/not nas (not possible)
		FileWriter fw = new FileWriter(f.getAbsoluteFile());
        BufferedWriter  bw = new BufferedWriter(fw);        

        String header =  " Machine " + MysqlX.row_delim 
					+   " Level " + MysqlX.row_delim 
					+   " No. entries " + MysqlX.row_delim 
        			+   "     IA and     NAS " + MysqlX.row_delim 
        			+   " not IA and     NAS " + MysqlX.row_delim 
        			+   "     IA and not NAS " + MysqlX.row_delim 
        			+   " not IA and not NAS " + MysqlX.row_delim 
        			;
		bw.write(header);
		bw.newLine();

        String data =  machine + MysqlX.row_delim 
				+  level + MysqlX.row_delim 
				+  domStat.cntDomains + MysqlX.row_delim 
    			+  domStat.IA_NAS    + MysqlX.row_delim 
    			+  domStat.notIA_NAS + MysqlX.row_delim 
    			+  domStat.IA_notNAS + MysqlX.row_delim 
    			+  domStat.notIA_notNAS + MysqlX.row_delim 
    			;
		bw.write(data);
		bw.newLine();
		
		bw.close();
    	return true;
    }
    
    public static boolean writeDomainStatOverviewFile(DomStat domStat, File f, String machine, int level) throws IOException {
        // Stat 2+3 : In berkely for IA and NAS
        // #entries, #lookedup, #exists
		FileWriter fw = new FileWriter(f.getAbsoluteFile());
        BufferedWriter  bw = new BufferedWriter(fw);        

        String header = " Machine " + MysqlX.row_delim 
        			+   " Level " + MysqlX.row_delim 
    				+   " Source " + MysqlX.row_delim 
        			+   " cnt occured in urls" + MysqlX.row_delim 
        			+   " cnt distinct domains "  + MysqlX.row_delim 
        			+   " looked-up in Netarkivet " + MysqlX.row_delim 
        			+   " is allready in Netarkivet " + MysqlX.row_delim 
        			;
        bw.write(header);
		bw.newLine();

		String data = "";
    	//All
    	data = machine + MysqlX.row_delim 
				+ level + MysqlX.row_delim 
				+ "All" + MysqlX.row_delim 
    		    + domStat.occurDomains + MysqlX.row_delim 
    		    + domStat.cntDomains + MysqlX.row_delim 
    			+ domStat.lookedupInBerkeley + MysqlX.row_delim 
    			+ domStat.existsInBerkeley + MysqlX.row_delim 
    			;
		bw.write(data);
		bw.newLine();
    	
    	//NAS
    	data = machine + MysqlX.row_delim 
				+ level + MysqlX.row_delim 
				+ Source.NAS.name() + MysqlX.row_delim 
    		    + domStat.NAS_occurcount + MysqlX.row_delim 
    		    + domStat.NAS_entrycount + MysqlX.row_delim 
    			+ domStat.NAS_lookedupInBerkeley + MysqlX.row_delim 
    			+ domStat.NAS_existsInBerkeley + MysqlX.row_delim 
    			;
		bw.write(data);
		bw.newLine();

    	//IA
		data = machine + MysqlX.row_delim 
				+ level + MysqlX.row_delim 
        		+ Source.IA.name() + MysqlX.row_delim 
    			+ domStat.IA_occurcount + MysqlX.row_delim 
    		    + domStat.IA_entrycount + MysqlX.row_delim 
    			+ domStat.IA_lookedupInBerkeley + MysqlX.row_delim 
    			+ domStat.IA_existsInBerkeley + MysqlX.row_delim 
    			;

		bw.write(data);
		bw.newLine();

		//IA AND NAL
		data = machine + MysqlX.row_delim 
				+ level + MysqlX.row_delim 
        		+ Source.IA.name() + "+" + Source.NAS.name() + MysqlX.row_delim 
    			+ domStat.IANAS_occurcount + MysqlX.row_delim 
    		    + domStat.IANAS_entrycount + MysqlX.row_delim 
    			+ domStat.IANAS_lookedupInBerkeley + MysqlX.row_delim 
    			+ domStat.IANAS_existsInBerkeley + MysqlX.row_delim 
    			;
		bw.write(data);
		bw.newLine();
		
		bw.close();
    	return true;
    }

    public static class CodeStats { 
    	public int code = 0;
    	Map<Integer,CodeStat> codestat = new HashMap<Integer,CodeStat>();
    }

    public static class CodeStat { 
    	public int code = 0;

    	public long IA = 0;
    	public long NAS = 0;
    	public long IA_AND_NAS = 0;
    	
    	public long IA_AND_InBerkeley = 0;
    	public long NAS_AND_InBerkeley  = 0;
    	public long IA_AND_NAS_AND_InBerkeley  = 0;

    	public long IA_AND_LookedupInBerkeley = 0;
    	public long NAS_AND_LookedupInBerkeley  = 0;
    	public long IA_AND_NAS_AND_LookedupInBerkeley  = 0;
    }

    public static CodeStats calcCodeStats (Connection conn, String tableType, StatType statType, int level) throws SQLException {
    	CodeStats codeStats = new CodeStats(); 
    	
        Set<Integer> dkCodeSet = MysqlX.getCodesForDanishResults();
		for (int code : dkCodeSet) {
			System.out.println("--- for code: " + code);
			codeStats.code = code;
			codeStats.codestat.put(code, calcCodeStat(conn, tableType, statType, level, code));
	    }
        return codeStats;
    }

    public static CodeStat calcCodeStat (Connection conn, String tableType, StatType statType, int level, int code) throws SQLException {
    	CodeStat codesStat = new CodeStat(); 
        Set<String> allTableSet = MysqlRes.getTables(conn);
    	
        if ((statType.equals(StatType.all) || statType.equals(StatType.codes)) && !tableType.equals("nyeall")) {
            System.out.println("Finding code stat for level: " + level);
			String table = "";
	    	table= MysqlRes.domainNyeTableName(level, code) + "_ALL";
    	    	
	    	if (!allTableSet.contains(table)) {
	            System.out.println("table: " + table + "was not there");
	    	} else {

	    		System.out.println("Finding IA_NAS");
		        codesStat.IA = MysqlRes.getTableCntEntries(conn, table, 
		    			"CntInIaData>0"); 
		        codesStat.NAS = MysqlRes.getTableCntEntries(conn, table, 
						"CntInNasData>0"); 
		        codesStat.IA_AND_NAS = MysqlRes.getTableCntEntries(conn, table, 
						"CntInIaData>0 AND CntInNasData>0"); 
        	
		        codesStat.IA_AND_InBerkeley = MysqlRes.getTableCntEntries(conn, table, 
		    			"CntInIaData>0 AND CntInBerkeley>0"); 
		        codesStat.NAS_AND_InBerkeley = MysqlRes.getTableCntEntries(conn, table, 
						"CntInNasData>0 AND CntInBerkeley>0"); 
		        codesStat.IA_AND_NAS_AND_InBerkeley = MysqlRes.getTableCntEntries(conn, table, 
						"CntInIaData>0 AND CntInNasData>0 AND CntInBerkeley>0"); 

		        codesStat.IA_AND_LookedupInBerkeley = MysqlRes.getTableCntEntries(conn, table, 
		    			"CntInIaData>0 AND LookedUpInBerkeley!=0"); 
		        codesStat.NAS_AND_LookedupInBerkeley = MysqlRes.getTableCntEntries(conn, table, 
						"CntInNasData>0 AND LookedUpInBerkeley!=0"); 
		        codesStat.IA_AND_NAS_AND_LookedupInBerkeley = MysqlRes.getTableCntEntries(conn, table, 
						"CntInIaData>0 AND CntInNasData>0 AND LookedUpInBerkeley!=0"); 
        	}
    	}
    	return codesStat;
	}

    public static boolean writeDomainStatCodesFile(CodeStats codeStats, File f, String machine, int level) throws IOException {
        // Stat 1: Update status for all
        // #entries, #ia/not nas, #not ia/nas, #ia/nas, # not ia/not nas (not possible)
		FileWriter fw = new FileWriter(f.getAbsoluteFile());
        BufferedWriter  bw = new BufferedWriter(fw);        

        String header =  " Machine " + MysqlX.row_delim 
					+   " Code " + MysqlX.row_delim 
					+   " IA " + MysqlX.row_delim 
        			+   " NAS " + MysqlX.row_delim 
        			+   " IA_AND_NAS " + MysqlX.row_delim 
					+   " InBerkeley IA " + MysqlX.row_delim 
        			+   " InBerkeley NAS " + MysqlX.row_delim 
        			+   " InBerkeley IA_AND_NAS " + MysqlX.row_delim 
					+   " LookedupInBerkeley IA " + MysqlX.row_delim 
        			+   " LookedupInBerkeley NAS " + MysqlX.row_delim 
        			+   " LookedupInBerkeley IA_AND_NAS " + MysqlX.row_delim 
        			;
		bw.write(header);
		bw.newLine();
		
		List<Integer> sortedKeys = new ArrayList<Integer>(codeStats.codestat.size());
        sortedKeys.addAll(codeStats.codestat.keySet());
        Collections.sort(sortedKeys);
        
    	for (int key: sortedKeys) {
    		CodeStat cs = codeStats.codestat.get(key);
    		String data =  machine + MysqlX.row_delim 
				+  key + MysqlX.row_delim 
				+  cs.IA + MysqlX.row_delim 
				+  cs.NAS + MysqlX.row_delim 
				+  cs.IA_AND_NAS + MysqlX.row_delim 
				+  cs.IA_AND_InBerkeley + MysqlX.row_delim 
				+  cs.NAS_AND_InBerkeley + MysqlX.row_delim 
				+  cs.IA_AND_NAS_AND_InBerkeley + MysqlX.row_delim 
				+  cs.IA_AND_LookedupInBerkeley + MysqlX.row_delim 
				+  cs.NAS_AND_LookedupInBerkeley + MysqlX.row_delim 
				+  cs.IA_AND_NAS_AND_LookedupInBerkeley + MysqlX.row_delim 
    			;
			bw.write(data);
			bw.newLine();
        }
		
		bw.close();
    	return true;
    }
}
