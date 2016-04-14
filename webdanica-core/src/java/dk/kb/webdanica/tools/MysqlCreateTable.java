package dk.kb.webdanica.tools;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;


/*
String url = "jdbc:mysql://localhost/test|webdanica";
Class.forName ("com.mysql.jdbc.Driver").newInstance ();
Connection conn = DriverManager.getConnection (url, "username", "password");
*/

/*
Update TABLE ResFromHadoop with C15b varchar(20) with TLD
*/

public class MysqlCreateTable {

    /**
     * @param args  <JDBC-URL> jdbcUser=<JDBC-username> tablePrefix=<tablename-prefix> tableFromNo=<tablename-from-number-suffix> tableToNo=<tablename-to-number-suffix>
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws SQLException 
     * @throws IOException 
     */

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException  {
    	String errArgTxt="Proper args: <JDBC-URL> jdbcUser=<JDBC-username> tablePrefix=<tablename-prefix> tableFromNo=<tablename-from-number-suffix> tableToNo=<tablename-to-number-suffix> "; 

    	if (args.length < 5) {
            System.err.println("Missing args!"); 
            System.err.println(errArgTxt);
            System.exit(1);
        }
        if (args.length > 5) {
            System.err.println("Too many args!");
            System.err.println(errArgTxt);
            System.exit(1);
        }

        /**** args - jdbc ****/
        /** arg 0 - jdbc url **/
        String jdbcUrl = args[0];
        if (!jdbcUrl.startsWith("jdbc:mysql:")) {
            System.err.println("Missing arg jdbc setting starting with 'jdbc:mysql'");
            System.err.println(errArgTxt);
            System.exit(1);
        }

        /** arg 1 - jdbc user **/
        String jdbcUser = args[1];
        if (!jdbcUser.startsWith("jdbcUser=")) {
            System.err.println("Missing arg jdbcUser setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        jdbcUser = MysqlX.getStringSetting(jdbcUser).toLowerCase();
        
        Class.forName ("com.mysql.jdbc.Driver").newInstance ();
        Connection conn = DriverManager.getConnection (jdbcUrl, jdbcUser, "");    
        
        /**** args - table numbers ****/
        /** arg 2 - table allTogether|allSingle|<tablename> **/
        String tablePrefix = args[2];
        if (!tablePrefix.startsWith("tablePrefix=")) {
            System.err.println("Missing arg tablePrefix setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        tablePrefix = MysqlX.getStringSetting(tablePrefix);
        
        String tableFromNoTxt = args[3];
        if (!tableFromNoTxt.startsWith("tableFromNo=")) {
            System.err.println("Missing arg tableFromNoTxt setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        tableFromNoTxt = MysqlX.getStringSetting(tableFromNoTxt);
		int tableFromNo = 0; 
    	if (!tableFromNoTxt.isEmpty() && (tableFromNoTxt.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+"))) {
    		tableFromNo = Integer.parseInt(tableFromNoTxt);
        } else {
            System.err.println("arg tableToNo is not an integer - got " + tableFromNoTxt);
            System.err.println(errArgTxt);
            System.exit(1);
    	} 
        
        String tableToNoTxt = args[4];
        if (!tableToNoTxt.startsWith("tableToNo=")) {
            System.err.println("Missing arg table setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        tableToNoTxt = MysqlX.getStringSetting(tableToNoTxt);
		int tableToNo = 0; 
    	if (!tableToNoTxt.isEmpty()) { 
    		if (tableToNoTxt.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+")) {
    			tableToNo = Integer.parseInt(tableToNoTxt);
        	} else {
                System.err.println("arg tableToNo is not an integer - got " + tableToNoTxt);
                System.err.println(errArgTxt);
                System.exit(1);
        	} 
    	} else {
    		tableToNo = tableFromNo; // only one table
    	}
    	
    	if ((tableFromNo==0) || (tableToNo>0 && tableFromNo>tableToNo)) {
            System.err.println("arg tableFromNo and tableToNo do not give an interval - got "  + tableFromNoTxt + "-"  + tableToNoTxt );
            System.err.println(errArgTxt);
            System.exit(1);
    	}
        
        ////////////////////////////////
        // Start processing

        // Set of tables in database 
        Set<String> tableSetExists = new HashSet<String>();
        tableSetExists = MysqlRes.getTables(conn);
    	
        // Set of tables to create
    	Set<String> tableSetNew = new HashSet<String>();
		for (int i = tableFromNo; i <=tableToNo; i++) {
			String tablename = tablePrefix + i;
			tableSetNew.add( tablename );
	    }

        //Create table and indexes
        for (String nextTable: tableSetNew) {
    		if (tableSetExists.contains(nextTable)) {
    	        System.out.println("Table "  + nextTable + " allready exists");
    		} else {
    			MysqlRes.createTableWithIndexes(conn, nextTable, jdbcUser);
    			System.out.println("Creating " + nextTable + " with indexes");
    		}
        }
        conn.close();
    }
}
