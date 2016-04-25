package dk.kb.webdanica.tools;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

/* Structures for ResFromHadoop */

public class MysqlUpdateTables {
    /**
     * @param args <JDBC-URL> jdbcUser=<JDBC-username> dbmachine=<e.g. kb-test-webdania-001> updateType=calcDanishCodeToInt|add3ef ignoreOld=true|false
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws SQLException 
     * @throws IOException 
     */
    
	public static void main(String[] args) throws  IOException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
        /*****************************************/
        /*** Arguments ***************************/
        /*****************************************/
		//def file has elements on form:
		//hadoop@kb-test-sol-001:/home/hadoop/disk7_instans_m004/combo-r1-m001-disk3_3-3-2-29-08-2014/,m001,disk3,3,3-2  or
		//hadoop@kb-test-sol-001:/home/hadoop/disk7_instans_m004/combo-r1-m001-disk3_3-3-2-29-08-2014/,m001,disk3,3,i00
        
    	String errArgTxt = "Proper args: <JDBC-URL> jdbcUser=<JDBC-username> "
    			+ "dbmachine=<e.g. kb-test-webdania-001> "
    			+ "updateType=calcDanishCodeToInt "
    			+ "ignoreOld=true|false";

        //System.out.println("args[0]: " + args[0]);    	

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

        /**** args - updateType ****/
        String updTypeTxt = args[3];
        if (!updTypeTxt.startsWith("updateType=")) {
            System.err.println("Missing arg updateType setting");
            System.err.println(updTypeTxt);
            System.exit(1);
        }
        updTypeTxt = MysqlX.getStringSetting(updTypeTxt);
        updateTableType updType =  updateTableType.upt_none;
        if (updTypeTxt.equals("calcDanishCodeToInt")) {
        	updType = updateTableType.upt_calcDanishCodeToInt; 
        } else if ( updTypeTxt.equals("add3ef")) {
        	updType = updateTableType.upt_add3ef;
        } else {
            System.err.println("ERROR: Arg updateType setting is not valid - got '" + updTypeTxt + "'");
            System.err.println(errArgTxt);
            System.exit(1);
        }

        /**** args - ignore old ****/
        String ignoreOldTxt = args[4];
        if (!ignoreOldTxt .startsWith("ignoreOld=")) {
            System.err.println("Missing arg ignoreOld setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        boolean ignoreOld = MysqlX.getBoleanSetting(ignoreOldTxt);

        /*****************************************/
        /*** Start processing ********************/
        /*****************************************/
        Set<String> tableSet = new HashSet<String>();
    	tableSet = MysqlRes.getTables(conn);

        
        if (updType == updateTableType.upt_calcDanishCodeToInt) {
            System.out.println("*****");
        	System.out.println("*** Updateing CalcDanishCode to MediumInt:" );
	    	
	    	for (String table : tableSet) {
	    		if (table.startsWith("ResHadoop_") || !ignoreOld) { // only new
		    		if (isCalcDanisCodeSmallInt(conn, table)) {
		            	System.out.println(" - Updating table " + table );
		    			updateCalcDanisCodeMediumInt(conn, table);
		    		} else {
		            	System.out.println(" - Already updated - table " + table );
		    		}
	    		} else {
	            	System.out.println(" - Ignoring table " + table );
	    		}
	    	}
 
        } else if (updType == updateTableType.upt_add3ef) {
	    	for (String table : tableSet) {
	    		if (table.startsWith("ResHadoop_") || !ignoreOld) { // only new
		    		if (hasNoC3ef(conn, table)) {
		            	System.out.println(" - Updating table " + table );
		    			addC3ef(conn, table);
		    		} else {
		            	System.out.println(" - Already updated - table " + table );
		    		}
	    		} else {
	            	System.out.println(" - Ignoring table " + table );
	    		}
	    	}
        }

    	System.out.println("*** Finished updating tables" );
	}

	public static boolean updateCalcDanisCodeMediumInt(Connection conn, String table) throws SQLException {
    	String sql = "ALTER TABLE " + table + " MODIFY calcDanishCode MEDIUMINT(3)";
    	PreparedStatement s = conn.prepareStatement(sql);
	    s.executeUpdate();
	    s.close();
    	return true;
	}

	public static boolean isCalcDanisCodeSmallInt(Connection conn, String table) throws SQLException {
	    String selectSQL = "SHOW FIELDS FROM " + table;
	    PreparedStatement s = conn.prepareStatement(selectSQL); 
	    ResultSet rs = s.executeQuery();
	    String ft = "";
	    while (rs.next()) {
	    	if (rs.getString("Field").equals("calcDanishCode")) {
	    		ft = rs.getString("Type").toLowerCase();
	    		break;
	    	}
	    }
	    rs.close();
	    s.close();
    	return ft.equals("smallint(2)");
    }
    
	public static boolean hasNoC3ef(Connection conn, String table) throws SQLException {
	    String selectSQL = "SHOW FIELDS FROM " + table;
	    PreparedStatement s = conn.prepareStatement(selectSQL); 
	    ResultSet rs = s.executeQuery();
	    boolean found = false;
	    while (rs.next() && !found) {
	    	if (rs.getString("Field").equals("C3e") || rs.getString("Field").equals("C3f")) {
	    		found = true;
	    		break;
	    	}
	    }
	    rs.close();
	    s.close();
    	return !found;
	}
	
	public static boolean hasNoNewHadoopFields(Connection conn, String table) throws SQLException {
	    String selectSQL = "SHOW FIELDS FROM " + table;
	    PreparedStatement s = conn.prepareStatement(selectSQL); 
	    ResultSet rs = s.executeQuery();
	    boolean found = false;
	    while (rs.next() && !found) {
	    	if (rs.getString("Field").equals("C10c") 
	    			|| rs.getString("Field").equals("C2b")
	    			|| rs.getString("Field").equals("C3g")
	    			|| rs.getString("Field").equals("C6d")
	    			|| rs.getString("Field").equals("C7g")
	    			|| rs.getString("Field").equals("C7h")
	    			|| rs.getString("Field").equals("C8c")
	    			|| rs.getString("Field").equals("C9e")
	    			|| rs.getString("Field").equals("C9f")) {
	    		found = true;
	    		break;
	    	}
	    }
	    rs.close();
	    s.close();
    	return !found;
	}

	public static boolean addC3ef(Connection conn, String table) throws SQLException {
    	String sql = "ALTER TABLE " + table + " ADD (C3e TEXT, C3f varchar(500))";
    	PreparedStatement s = conn.prepareStatement(sql);
	    s.executeUpdate();
	    s.close();
    	return true;
    }

	public static boolean addHadoopFields(Connection conn, String table) throws SQLException {
    	String sql = "ALTER TABLE " + table + " ADD ("
    			+ "C10c TEXT, " 
    			+ "C2b varchar(500), "
    			+ "C3g TEXT, "
				+ "C6d TEXT, "
				+ "C7g TEXT, "
				+ "C7h TEXT, "
				+ "C8c TEXT, "
				+ "C9e TEXT, "
				+ "C9f varchar(500) )";
    	PreparedStatement s = conn.prepareStatement(sql);
	    s.executeUpdate();
	    s.close();
    	return true;
    }

	enum updateTableType{
    	upt_calcDanishCodeToInt,
    	upt_add3ef,
    	upt_none
    }
}
