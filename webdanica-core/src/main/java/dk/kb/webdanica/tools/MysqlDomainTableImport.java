package dk.kb.webdanica.tools;

//import java.io.BufferedReader;
import java.io.File;
//import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
//import java.text.SimpleDateFormat;
import java.util.*;
import java.io.IOException;

import dk.kb.webdanica.tools.MysqlRes.*;

public class MysqlDomainTableImport {
    /**
     * @param args <JDBC-URL> jdbcUser=<JDBC-username> datadir=<dir for out-files>    
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws SQLException 
     * @throws IOException 
     */

	// output on form Url # code # forklaring af code?????
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, SQLException {
        /*****************************************/
        /** Merges from all db machines the extracted 
         ** Berkely fields from 
         ** Domian tables, to be distributed in 
         ** order to optimize lookup             */
        /*****************************************/

    	/*****************************************/
        /*** Arguments ***************************/
        /*****************************************/
    	String errArgTxt = "Proper args: <JDBC-URL> jdbcUser=<JDBC-username> ";
        if (args.length < 3) {
            System.err.println("Missing args!");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        if (args.length > 3) {
            System.err.println("Too many args!");
            System.err.println(errArgTxt);
            System.exit(1);
        }

        String jdbcUrl = args[0];
        System.out.println("jdbcUrl : '" +jdbcUrl + "'");
        if (!jdbcUrl.startsWith("jdbc:mysql:")) {
            System.err.println("Missing arg jdbc setting starting with 'jdbc:mysql'");
            System.err.println(errArgTxt);
            System.exit(1);
        }

        String jdbcUser = args[1];
        System.out.println("jdbcUser : '" +jdbcUser + "'");
        if (!jdbcUser.startsWith("jdbcUser=")) {
            System.err.println("Missing arg jdbcUser setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        jdbcUser = MysqlX.getStringSetting(jdbcUser).toLowerCase();
        System.out.println("jdbcUser : '" +jdbcUser + "'");
        
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
        //Dir with files to be imported and merged
        String importDirTxt = datadirTxt + MysqlX.tableimport_dir;
        File importDir = new File(importDirTxt);
        if (!importDir.isDirectory()) {
            System.err.println("ERROR: The given data-dir '" + dataDir.getAbsolutePath() + "' does not have dir:" + MysqlX.urlsimport_dir);
            System.err.println(errArgTxt);
            System.exit(1);
        }

        /*****************************************/
        /*** Start processing ********************/
    	/*** Writing domain levels into files for likely Danish of finished table data ***/ 
        /*****************************************/
        
        System.out.println("*** creating tables ");
        Set<File> importTableFiles = new HashSet<File>();
    	File[] dirfiles = importDir.listFiles();
        for (File df : dirfiles) {
        	String dfname = df.getName();
        	if (dfname.contains( MysqlRes.domaintable_resdomain_machine_infix)) {
        		importTableFiles.add(df);
        	} 
        }
        
        //Import tables by scipts
        for (File f: importTableFiles) {
        	String s = "mysql webdanica < " + f.getAbsolutePath();
        	System.out.println("- executing " + s);
            Runtime.getRuntime().exec(s );
        }
        
        
        //Merge
        Set<String> allTableSet = MysqlRes.getTables(conn);
        System.out.println("*** Finding tables ");
        Set<String> importLevelTableSet = new HashSet<String>();
        Set<String> resLevelTableSet = new HashSet<String>();
        for (String t : allTableSet) {
        	if (t.startsWith(MysqlRes.domaintable_prefix )) {
        		if (t.contains(MysqlRes.domaintable_resdomain_machine_infix))  importLevelTableSet.add(t.trim());      			
        		else if (t.contains(MysqlRes.domaintable_resdomain_all_suffix)) resLevelTableSet.add(t.trim());       			
        	}
        }
	    
	    //level tables if not there - 
	    //manual delete of contents if new updates!!!!!
        if (resLevelTableSet.isEmpty()) {
	        for (int i=1;i<=MysqlX.noDomainLevels;i++) {
	        	String t = MysqlRes.domainTableName(i, MysqlRes.domaintable_resdomain_all_suffix);
		    	MysqlRes.createDomainLevelTable(conn, t);
		    	resLevelTableSet.add(t);
	        }
        }

        for (String imtable: importLevelTableSet) {
        	Set<DomainLevel> im_entries = new HashSet<DomainLevel>();
        	String resTable = "";
        	int addLevel=0;

        	for (int i=1;i<=MysqlX.noDomainLevels;i++)
        		if (imtable.contains(i+"")) addLevel = i; 
        	for (String rtable: resLevelTableSet)
        		if (rtable.contains(addLevel+"")) resTable = rtable;
        	
        	System.out.println("adding contents of table: " + imtable + " to " + resTable);
        	if (addLevel == 1) {
        		im_entries = MysqlRes.readDomainLevel(conn, imtable);
	        	for (DomainLevel im_entry: im_entries) {
	        		DomainLevel es_entry = MysqlRes.readIfexistsDomain(conn, resTable, im_entry.Domain);
	    			if (es_entry.Domain.isEmpty()) {
	    				MysqlRes.insertDomainLevel( conn, resTable, im_entry);
	    			} else {
	    				es_entry.InIaData = es_entry.InIaData || im_entry.InIaData;
	    				es_entry.InIaData = es_entry.InNasData || im_entry.InNasData;
	    				es_entry.LookedUpInBerkeley = es_entry.LookedUpInBerkeley || im_entry.LookedUpInBerkeley;
	    				es_entry.CntInBerkeley = Math.max(es_entry.CntInBerkeley, im_entry.CntInBerkeley);
	    	        	for (int c: im_entry.calcCodes) es_entry.calcCodes.add(c);
	    				
	    				MysqlRes.updateDomainLevel(conn, resTable, es_entry);
	        		}
	        	}
	    	} else {
	        	System.out.println("total entries in table: " + MysqlRes.getTableCntEntries(conn, imtable));
	    		for(char ch : "abcdefghijklmnopqrstuvwxyz".toCharArray()) {
		        	System.out.println("total entries in table: " + MysqlRes.getTableCntEntries(conn, imtable, "Domain like '" + ch + "%'"));
	        		im_entries = MysqlRes.readDomainLevel(conn, imtable, "Domain like '" + ch + "%'");

	        		for (DomainLevel im_entry: im_entries) {
		        		DomainLevel es_entry = MysqlRes.readIfexistsDomain(conn, resTable, im_entry.Domain);
		    			if (es_entry.Domain.isEmpty()) {
		    				MysqlRes.insertDomainLevel( conn, resTable, im_entry);
		    			} else {
		    				es_entry.InIaData = es_entry.InIaData || im_entry.InIaData;
		    				es_entry.InIaData = es_entry.InNasData || im_entry.InNasData;
		    				es_entry.LookedUpInBerkeley = es_entry.LookedUpInBerkeley || im_entry.LookedUpInBerkeley;
		    				es_entry.CntInBerkeley = Math.max(es_entry.CntInBerkeley, im_entry.CntInBerkeley);
		    	        	for (int c: im_entry.calcCodes) es_entry.calcCodes.add(c);
		    				
		    				MysqlRes.updateDomainLevel(conn, resTable, es_entry);
		        		}
		        	}
	        	}
	    	}
    	}
    }
}
