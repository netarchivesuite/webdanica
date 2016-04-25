package dk.kb.webdanica.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
//import java.text.SimpleDateFormat;
import java.util.*;
import java.io.IOException;

import dk.kb.webdanica.tools.MysqlRes.*;

public class MysqlDomainTableExtract {
    /**
     * @param args <JDBC-URL> jdbcUser=<JDBC-username> dbmachine=<machine-name> datadir=<dir for out-files>    
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws SQLException 
     * @throws IOException 
     */

	// output on form Url # code # forklaring af code?????
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, SQLException {
        /*****************************************/
        /** Extract only Berkely fields fropm 
         ** Domian tables, to be distributed in 
         ** order to optimize lookup             */
        /*****************************************/
    	
        /*****************************************/
        /*** Arguments ***************************/
        /*****************************************/
    	String errArgTxt = "Proper args: <JDBC-URL> jdbcUser=<JDBC-username> "
    			+ "machine=<machine-name> "
    			+ "datadir=<data dir where 'url-dir' (for out-files) exixts> ";
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
        String exportDirTxt = datadirTxt + MysqlX.urlsexport_dir;
        File exportDir = new File(exportDirTxt);
        if (!exportDir.isDirectory()) {
            System.err.println("ERROR: The given data-dir '" + dataDir.getAbsolutePath() + "' does not have dir:" + MysqlX.urlsexport_dir);
            System.err.println(errArgTxt);
            System.exit(1);
        }
        
        /*****************************************/
        /*** Start processing ********************/
    	/*** Writing domain levels into files for likely Danish of finished table data ***/ 
        /*****************************************/
        
        for (int level=1;level<=MysqlX.noDomainLevels;level++) {
        	String exportTable = MysqlRes.domainTableName(level);
        	System.out.println("table: " + exportTable);
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
		        for (LookedUpBerk d_b: d_bs) {
		        	s = s + (s.isEmpty()?"":",") + d_b.sql_insertpart();  
		        }
		        bw.write("INSERT INTO `" + exportTable + "` VALUES " + s + ";");  bw.newLine();
		        bw.write("UNLOCK TABLES;"); bw.newLine();
	          	bw.close();
            }
        }
    }
}
