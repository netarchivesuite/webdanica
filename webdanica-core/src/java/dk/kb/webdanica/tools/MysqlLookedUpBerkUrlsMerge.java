package dk.kb.webdanica.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
//import java.text.SimpleDateFormat;
import java.util.*;
import java.io.IOException;

import dk.kb.webdanica.tools.MysqlRes.*;
import dk.kb.webdanica.tools.MysqlX.*;

public class MysqlLookedUpBerkUrlsMerge {
    /**
     * @param args <JDBC-URL> jdbcUser=<JDBC-username> datadir=<dir for out-files> level=all|l1|l2|l3 m=<test1, ...> help_m=<test1, ...>
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
    	String errArgTxt = "Proper args: <JDBC-URL> jdbcUser=<JDBC-username> "
    			+ "datadir=<data for out-files> "
    			+ "level=all|l1|l2|l3 "
    			+ "m=<test1, ...> help_m=<test1, ...> ";
        if (args.length < 4) {
            System.err.println("Missing args!");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        if (args.length > 6) {
            System.err.println("Too many args!");
            System.err.println("got " + args);
            System.err.println(errArgTxt);
            System.exit(1);
        }

        System.out.println("parm1: jdbc" + args[0]);
        String jdbcUrl = args[0];
        //System.out.println("jdbcUrl : '" +jdbcUrl + "'");
        if (!jdbcUrl.startsWith("jdbc:mysql:")) {
            System.err.println("Missing arg jdbc setting starting with 'jdbc:mysql'");
            System.err.println(errArgTxt);
            System.exit(1);
        }

        System.out.println("parm1: jdbc user" + args[1]);
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
        System.out.println("parm1: dir" + args[2]);
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
        String importDirTxt = datadirTxt + "/" + MysqlX.urlsimport_dir;
        File importDir = new File(importDirTxt);
        if (!importDir.isDirectory()) {
            System.err.println("ERROR: The given data-dir '" + dataDir.getAbsolutePath() + "' does not have dir:" + MysqlX.urlsexport_dir);
            System.err.println(errArgTxt);
            System.exit(1);
        }
        //Dir for file with merge table to be exported
        String exportDirTxt = datadirTxt + "/" + MysqlX.urlsexport_dir;
        File exportDir = new File(exportDirTxt);
        if (!exportDir.isDirectory()) {
            System.err.println("ERROR: The given data-dir '" + dataDir.getAbsolutePath() + "' does not have dir:" + MysqlX.urlsexport_dir);
            System.err.println(errArgTxt);
            System.exit(1);
        }
        
        /**** args - level ****/
        System.out.println("parm1: level" + args[3]);
        LookupLevel pLevel = LookupLevel.all;
        String pLevelTxt = args[3];
        if (!pLevelTxt.startsWith("level=")) {
            System.err.println("Missing arg level setting - got " + pLevelTxt);
            System.err.println(errArgTxt);
            System.exit(1);
        }
        pLevelTxt = MysqlX.getStringSetting(pLevelTxt);
    	if (pLevelTxt.startsWith("all")) {
    		pLevel = LookupLevel.all;        	
        } else if (pLevelTxt.startsWith("l1")) {
        	pLevel = LookupLevel.l1;      
        } else if (pLevelTxt.startsWith("l2")) {
        	pLevel = LookupLevel.l2;      
        } else if (pLevelTxt.startsWith("l3")) {
        	pLevel = LookupLevel.l3;      
        } else {
            System.err.println("Arg level setting is NOT valid - got '" + pLevelTxt + "'");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        
    	String m = ""; 
    	String hm = ""; 
        if (args.length > 4) {
            System.out.println("parm1: m" + args[4]);
            System.out.println("parm1: hm" + args[5]);
            System.err.println("m and hm specified");
            if (args.length < 6) {
                System.err.println("inconsisten m spec!");
                System.err.println(errArgTxt);
                System.exit(1);
            } else if (pLevel.equals(LookupLevel.l1)) {
                System.err.println("inconsisten m spec with level 1!");
                System.err.println(errArgTxt);
                System.exit(1);
            }

            /**** args - m (machine) ****/
            String mTxt = args[4];
            if (!mTxt.startsWith("m=")) {
                System.err.println("Missing arg m setting - got " + mTxt);
                System.err.println(errArgTxt);
                System.exit(1);
            }
            m = MysqlX.getStringSetting(mTxt);
            System.err.println("m :" + m );

            /**** args - help_m (helper machine) ****/
            String hmTxt = args[5];
            if (!hmTxt.startsWith("help_m=")) {
                System.err.println("Missing arg help_m setting - got " + hmTxt);
                System.err.println(errArgTxt);
                System.exit(1);
            }
            hm = MysqlX.getStringSetting(hmTxt);
            System.err.println("hm :" + hm );
        }

        /*****************************************/
        /*** Start processing ********************/
    	/*** Writing domain levels into files for likely Danish of finished table data ***/ 
        /*****************************************/
        
        /*********** Import tables by scipts ***************/
    	File[] dirfiles = importDir.listFiles();
        for (File f : dirfiles) {
        	String fname = f.getName();
        	boolean skip = (pLevel !=  LookupLevel.all);
        	if (skip && (pLevel.equals(LookupLevel.l1) && fname.contains("_" + MysqlRes.domaintable_level_infix + 1))) skip = false;
        	if (skip && (pLevel.equals(LookupLevel.l2) && fname.contains("_" + MysqlRes.domaintable_level_infix + 2))) skip = false;
        	if (skip && (pLevel.equals(LookupLevel.l3) && fname.contains("_" + MysqlRes.domaintable_level_infix + 3))) skip = false;
        	if (!skip) {
        		if (m.isEmpty() || fname.contains(m) || fname.contains(hm)) {
		        	System.out.println("executing lines from: " + f.getName());
		            BufferedReader fr = new BufferedReader(new FileReader(f));        
		            String line ="";
		            
		            //read file and ingest
		            while ((line = fr.readLine()) != null) {
		            	line = line.trim();
		                if (!line.isEmpty()) {
		                	//int endpos = Math.min(50, line.length());
		                	MysqlRes.execSqlLine(conn,line);
		                }
		            }
		            fr.close();
        		}
        	}
        }
        
        /*********** Merge and create "ALL" tables ***************/
        //find domain tables - import and export
        Set<String> allTableSet = MysqlRes.getTables(conn);
        System.out.println("*** Finding tables ");
        Set<String> importLevelTableSet = new HashSet<String>();
        Set<String> exportLevelTableSet = new HashSet<String>();
        for (String t : allTableSet) {
        	if (t.startsWith(MysqlRes.domaintable_prefix )) {
        		if (t.contains(MysqlRes.domaintable_lookedupberk_machine_infix)) {
        			if (pLevel ==  LookupLevel.all) {
                		if (m.isEmpty() || t.contains(m) || t.contains(hm)) importLevelTableSet.add(t.trim()); 
        			} else if (pLevel.equals(LookupLevel.l1) && t.contains("_" + MysqlRes.domaintable_level_infix + 1) ) {
                		if (m.isEmpty() || t.contains(m) || t.contains(hm)) importLevelTableSet.add(t.trim());      			
        			} else if (pLevel.equals(LookupLevel.l2) && t.contains("_" + MysqlRes.domaintable_level_infix + 2) ) {
                		if (m.isEmpty() || t.contains(m) || t.contains(hm)) importLevelTableSet.add(t.trim());      			
        			} else if (pLevel.equals(LookupLevel.l3) && t.contains("_" + MysqlRes.domaintable_level_infix + 3) ) {
                		if (m.isEmpty() || t.contains(m) || t.contains(hm)) importLevelTableSet.add(t.trim());      			
            		}
        		}
        		else if (t.contains(MysqlRes.domaintable_lookedupberk_all_suffix)) {
        			if (pLevel ==  LookupLevel.all)  
            			exportLevelTableSet.add(t.trim());       			
        			else if (pLevel.equals(LookupLevel.l1) && t.contains("_" + MysqlRes.domaintable_level_infix + 1) )
            			exportLevelTableSet.add(t.trim());       			
        			else if (pLevel.equals(LookupLevel.l2) && t.contains("_" + MysqlRes.domaintable_level_infix + 2) )
            			exportLevelTableSet.add(t.trim());       			
            		else if (pLevel.equals(LookupLevel.l3) && t.contains("_" + MysqlRes.domaintable_level_infix + 3) )
            			exportLevelTableSet.add(t.trim());       			
        		}
        	}
        }
	    
	    //create export level tables if not there 
        for (int i=1;i<=MysqlX.noDomainLevels;i++) {
        	if (pLevel ==  LookupLevel.all || (i==1 && pLevel.equals(LookupLevel.l1)) || (i==2 && pLevel.equals(LookupLevel.l2)) || (i==3 && pLevel.equals(LookupLevel.l3))) {  
		    	String t = MysqlRes.domainTableName(i, MysqlRes.domaintable_lookedupberk_all_suffix);
	        	if (exportLevelTableSet.contains(t)) {
			        MysqlRes.clearTable(conn, t);
	            } else {
			    	MysqlRes.createLookedUpDomainLevelTable(conn, t);
			    	exportLevelTableSet.add(t);
		        }
	        }
        }

	    //fill export level tables with imported tables data 
        for (int level=1;level<=MysqlX.noDomainLevels;level++) {
        	if (pLevel ==  LookupLevel.all || (level==1 && pLevel.equals(LookupLevel.l1)) || (level==2 && pLevel.equals(LookupLevel.l2)) || (level==3 && pLevel.equals(LookupLevel.l3))) {  
		    	String exportTable = MysqlRes.domainTableName(level, MysqlRes.domaintable_lookedupberk_all_suffix);
	        	
	        	for (String imptable: importLevelTableSet) {
	        		if (imptable.contains(MysqlRes.domaintable_level_infix + level)) {
		        		System.out.println("adding contents of table: " + imptable + " to " + exportTable);
		        		Set<LookedUpBerk> entries = MysqlRes.readLookedUpBerk(conn, imptable);
			        	for (LookedUpBerk entry: entries) {
			        		if (!MysqlRes.existsDomain(conn, entry.Domain, exportTable)) {
			        			MysqlRes.InsertLookedUpDom(conn, exportTable, entry);
			        		}
			        	}
		        	}
	        	}
        	}
    	}
        	
        /*********** Dump contents from created "ALL" tables ***************/
        for (String exportTable: exportLevelTableSet) {
        	File exportFile = new File(exportDirTxt + "/dump_" + exportTable + ".sql");
        	System.out.println("file: " + exportFile.getAbsolutePath());
            exportFile.createNewFile();
            FileWriter fw = new FileWriter(exportFile.getAbsoluteFile());
            BufferedWriter  bw = new BufferedWriter(fw);        

        	Set<LookedUpBerk> d_bs = MysqlRes.readLookedUpBerk(conn, exportTable);
        	
        	// write export dump
        	if (d_bs.isEmpty()) {
                System.out.println("* No data to export from " + exportTable);
        	} else {
                System.out.println("* Dumping data for data " + exportTable);
		        bw.write("DROP TABLE IF EXISTS `" + exportTable + "`;"); bw.newLine();
		        bw.write("CREATE TABLE `" + exportTable + "` ("); 
		        bw.write(" `Domain` varchar(700) NOT NULL DEFAULT '',"); 
		        bw.write(" `CntInBerkeley` int(4) DEFAULT NULL,"); 
		        bw.write(" PRIMARY KEY (`Domain`)"); 
		        bw.write(") ;"); bw.newLine(); // ENGINE=MyISAM DEFAULT CHARSET=latin1 after )
		        bw.write("LOCK TABLES `" + exportTable + "` WRITE;"); bw.newLine();
		        String s = "";
		        int i = 0;
		        for (LookedUpBerk d_b: d_bs) {
		        	i++;
		        	if (i<100) {
		        		s = s + (s.isEmpty()?"":",") + d_b.sql_insertpart();
		        	} else {
		        		s = s + (s.isEmpty()?"":",") + d_b.sql_insertpart();
				        bw.write("INSERT INTO `" + exportTable + "` VALUES " + s + ";");  bw.newLine();
				        s = "";
				        i=0;
			        }
		        }
		        if (i!=0) bw.write("INSERT INTO `" + exportTable + "` VALUES " + s + ";");  bw.newLine();
		        bw.write("UNLOCK TABLES;"); bw.newLine();
	          	bw.close();
            }
        }
    }
}
