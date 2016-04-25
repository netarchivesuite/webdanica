package dk.kb.webdanica.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
//import java.text.SimpleDateFormat;
import java.io.IOException;

public class MysqlLookedupBerkUrlsIngest {
    /*****************************************/
    /** Ingest only Berkely fields from other db machines in 
     ** Domian tables, to be distributed in 
     ** order to optimize lookup             */
    /*****************************************/
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
        /*** Arguments ***************************/
        /*****************************************/
    	String errArgTxt = "Proper args: <JDBC-URL> jdbcUser=<JDBC-username> "
    			+ "datadir=<data dir where 'url-dir' (for out-files) exixts> ";
        if (args.length < 3) {
            System.err.println("Missing args!");
            System.err.println("Got " + args.length + " -- ");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        if (args.length > 3) {
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
        String importDirTxt = datadirTxt + MysqlX.urlsimport_dir;
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
        
    	//SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    	//Date now = new Date(System.currentTimeMillis());

        Set<File> importTableFiles = new HashSet<File>();
    	File[] dirfiles = importDir.listFiles();
        for (File df : dirfiles) {
        	String dfname = df.getName();
        	if (dfname.contains( MysqlRes.domaintable_lookedupberk_all_suffix)) {
        		importTableFiles.add(df);
        	}
        }
        
        for (File f : importTableFiles) {
            BufferedReader fr = new BufferedReader(new FileReader(f));        
            String line ="";
            System.out.println("executing lines in file: " + f.getName());
            
            //read file and ingest
            while ((line = fr.readLine()) != null) {
            	line = line.trim();
                if (!line.isEmpty()) {
                	MysqlRes.execSqlLine(conn,line);
                }
            }
            fr.close();
        }
    }
}
