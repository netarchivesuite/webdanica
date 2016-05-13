package dk.kb.webdanica.oldtools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.io.IOException;

import dk.kb.webdanica.oldtools.MysqlRes.*;
import dk.kb.webdanica.oldtools.MysqlX.*;

/*
extract status on dbMaschine
*/

public class MysqlAddStatExtract {

    /**
     * @param args JDBC-URL jdbcUser=<JDBC-username>  dbmachine=<e.g. kb-test-webdania-001> datadir=<e.g. /data1/resultater/>   
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws SQLException 
     * @throws IOException 
     */

	public static void main(String[] args) throws  IOException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
        /*collects additional statistics */
		
        /*****************************************/
        /*** Arguments ***************************/
        /*****************************************/
        
    	String errArgTxt = "Proper args: <JDBC-URL> jdbcUser=<JDBC-username> "
    			+ "dbmachine=<e.g. kb-test-webdania-001> "
    			+ "datadir=<e.g. /data1/resultater/> "
				+ "onlyDomains=true|false";
    			//+ "ignoreFile=true|false|warning";
        if (args.length < 5) {
            System.err.println("Missing args!");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        if (args.length > 5) {
            System.err.println("Too many args!");
            System.err.println(errArgTxt);
            System.err.println("Got:");
            for (int i = 0; i < args.length; i++) {
            	 System.err.println(" - " + args[i]);
            }
            System.exit(1);
        }

        /**** args - jdbc ****/
        /** arg - url **/
        String jdbcUrl = args[0];
        if (!jdbcUrl.startsWith("jdbc:mysql:")) {
            System.err.println("Missing arg jdbc setting starting with 'jdbc:mysql'");
            System.err.println(errArgTxt);
            System.exit(1);
        }

        /** arg - user **/
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

        /**** args - data-dir ****/
        String datadirTxt = args[3];
        if (!datadirTxt.startsWith("datadir=")) {
            System.err.println("Missing arg datadir setting - got " + datadirTxt);
            System.err.println(errArgTxt);
            System.exit(1);
        }
        datadirTxt = MysqlX.getStringSetting(datadirTxt);

        /**** args - data-dir ****/
        String allTxt = args[4];
        if (!datadirTxt.startsWith("onlyDomains=")) {
            System.err.println("Missing arg datadir setting - got " + datadirTxt);
            System.err.println(errArgTxt);
            System.exit(1);
        }
        boolean all = !(MysqlX.getBoleanSetting(allTxt));

        /*****************************************/
        /*** Start processing ********************/
        /*****************************************/
        
        // TODO: show tables where Tables_in_webdanica like "ResHadoop_%";
        // TODO: # levels distribution (antal / i urler)
        // TODO: size per code
        // TODO: count TLD
        // CntLikelydanishDomains per code ?
        // CntSizeLikelydanishDomains per code ?

        AddStat IAstat = new AddStat(DataSource.source_IA, dbMachine);
        AddStat NASstat = new AddStat(DataSource.source_NAS, dbMachine);

        System.out.println("*** Finding tables ");
        Set<String> allTableSet = MysqlRes.getTables(conn);
        for (String t : allTableSet) {
        	if (t.startsWith(MysqlRes.wf_table_prefix)) {
	        	if (MysqlX.isNAStablename(t)) NASstat.tableSet.add(t.trim());
	        	else IAstat.tableSet.add(t.trim());
        	}
        }

        System.out.println("*** Extract IA data ");
        IAstat = extractAddStatus(conn, datadirTxt, DataSource.source_IA, dbMachine, IAstat.tableSet, all);
        System.out.println("*** Extract NAS data ");
        NASstat = extractAddStatus(conn, datadirTxt, DataSource.source_NAS, dbMachine, NASstat.tableSet, all);
        System.out.println("*** Write IA data ");
        writeAddStatus(datadirTxt, IAstat);
        System.out.println("*** Write NAS data ");
        writeAddStatus(datadirTxt, NASstat);

	}

	
	public static void writeAddStatus(String datadirTxt, AddStat stat) throws IOException {
	    File dataDir = new File(datadirTxt);
	    if (!dataDir.isDirectory()) {
	        System.err.println("ERROR: The given data-dir '" + dataDir.getAbsolutePath() + "' is not a proper directory or does not exist");
	        System.exit(1);
	    }
	    String statdir = dataDir.getAbsolutePath() 
				+ "/" + MysqlX.statistics_dir
				+ "/" + MysqlX.addstatusextract_dir;
	    String statusFilename = statdir + "/" 
	    		+ getAddStatusFilename(stat.dbmachine, stat.src)  
	    		+ MysqlX.txtfile_suffix;
	    File statusFile = new File(statusFilename); 
		if (statusFile.exists()) { //otherwise it is allready produces
			System.out.println("WARNING: stat file was already produced: " + statusFile.getAbsolutePath());
		}
	    statusFile.createNewFile();
	    System.out.println("write file" );
	    writeAddStatusFile(statusFile, stat);		
	}
    

	public static AddStat extractAddStatus(Connection conn, String datadirTxt, DataSource src, String dbMachine, Set<String> tableSet, boolean all) throws SQLException {
		AddStat resStat = new AddStat(src, dbMachine);
		if (all) {
	        for (String table : tableSet) { //init with m/d/p ...
	    	    System.out.println("table " + table);
	        	resStat.CntDomains = resStat.CntDomains + MysqlRes.findCntDomains(conn, table);
	        	resStat.CntSize = resStat.CntSize + MysqlRes.findCntSizeDomains(conn, table);
	        	//stat.CntLikelydanishDomains = MysqlRes.findCntLikelyDanishDomains(conn, table);
	        }
        }
        System.out.println("*** cnt L1 domains ");
        resStat.CntLevel_L1_Domains = MysqlRes.findCntLevelDomains(conn, 1, "");
        System.out.println("*** cnt L2 domains ");
        resStat.CntLevel_L2_Domains = MysqlRes.findCntLevelDomains(conn, 2, "");
        System.out.println("*** cnt L3 domains ");
        resStat.CntLevel_L3_Domains = MysqlRes.findCntLevelDomains(conn, 3, "");
        System.out.println("*** cnt L1 lookedup domains ");
        resStat.CntLevel_L1_LookedupDomains = MysqlRes.findCntLevelDomains(conn, 1, "LookedUpInBerkeley = 1");
        System.out.println("*** cnt L2 lookedup domains ");
        resStat.CntLevel_L2_LookedupDomains = MysqlRes.findCntLevelDomains(conn, 2, "LookedUpInBerkeley = 1");
        System.out.println("*** cnt L3 lookedup domains ");
    	resStat.CntLevel_L3_LookedupDomains = MysqlRes.findCntLevelDomains(conn, 3, "LookedUpInBerkeley = 1");

        return resStat;
	}
	
    public static String getStatusFilename( String dbMachine, String seqno) {
    	String s = MysqlX.status_fileprefix 
    			+ "M" + dbMachine 
    			+ "_V" + seqno; 
        return s;
    }
    
    public static String getAddStatusFilename( String dbMachine, DataSource src) {
    	String s = MysqlX.status_fileprefix 
    			+ "M" + dbMachine 
    			+ "_S" + src.name(); 
        return s;
    }

    public static void writeAddStatusFile(File writeFile, AddStat stat) throws IOException {
		/** On form
		 * <title> # <date> #######...
		 * machine_no # disk_no # part_no # subpart/interval # hadoop_version # db-machine  # hasTable # emptytable--- 8/7
		 *  # hasDir # emptydatadir # extrafiles					       						 --- 3
		 *  # hadoopfileProduced # ingestfileProduced # updatedProduced # updateVersion       	 --- 3
		 *  # copiedOk # copyherefileProduced # copybackfileProduced                   			 --- 3
		 *  # compressedfileProduced # statProduced # statVersion                                --- 3  i.e 19/18
		 */
		
		//make write possible on file
		FileWriter fw = new FileWriter(writeFile.getAbsoluteFile());
        BufferedWriter  bw = new BufferedWriter(fw);        

		//write
		bw.write("dbmachine#");
		bw.write("CntDomains#");
		bw.write("CntSize#");
		bw.write("CntLikelydanishDomains#");
		bw.write("CntLevel_L1_Domains#");
		bw.write("CntLevel_L2_Domains#");
		bw.write("CntLevel_L3_Domains#");
		bw.write("CntLevel_L1_LookedupDomains#");
		bw.write("CntLevel_L2_LookedupDomains#");
		bw.write("CntLevel_L3_LookedupDomains#");
		bw.newLine();
		bw.write(stat.dbmachine + "#");
		bw.write(stat.CntDomains+ "#");
		bw.write(stat.CntSize+ "#");
		bw.write(stat.CntLikelydanishDomains+ "#");
		bw.write(stat.CntLevel_L1_Domains + "#");
		bw.write(stat.CntLevel_L2_Domains + "#");
		bw.write(stat.CntLevel_L3_Domains + "#");
		bw.write(stat.CntLevel_L1_LookedupDomains + "#");
		bw.write(stat.CntLevel_L2_LookedupDomains + "#");
		bw.write(stat.CntLevel_L3_LookedupDomains + "#");
		bw.newLine();
		bw.close();
	}
}