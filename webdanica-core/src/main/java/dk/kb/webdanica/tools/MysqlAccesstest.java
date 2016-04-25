package dk.kb.webdanica.tools;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class MysqlAccesstest {

    /**
     * @param args JDBC-URL user 
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws SQLException 
     * @throws IOException 
     */
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, IOException {
        if (args.length < 2) {
            System.err.println("Missing args: JDBC-URL USER");
            System.exit(1);
        }
        if (args.length > 2) {
            System.err.println("Too many args: JDBC-URL USER");
            System.exit(1);
        }
        
        
        String jdbcUrl = args[0];
        String user = args[1];
        Class.forName ("com.mysql.jdbc.Driver").newInstance ();
        Connection conn = DriverManager.getConnection (jdbcUrl, user, "");
        
    }
 }