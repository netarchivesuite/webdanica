package dk.kb.webdanica.oldtools;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import dk.kb.webdanica.oldtools.MysqlRes.DomainNyeLevel;

public class MysqlNetarkivetLookup {
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, SQLException {
        /*****************************************/
        /*** Arguments ***************************/
        /*****************************************/
    	String errArgTxt = "Proper args: <JDBC-URL> jdbcUser=<JDBC-username> "
    			+ "dbmachine=<machine-name> "
    			+ "datadir=<data dir where Netarkivet seed file exixts under subdir seed> "
    			+ "level=all|l1|l1_l2|l2|l3 ";
        if (args.length < 5) {
            System.err.println("Missing args! only got " + args.length + " ... " + args);
            System.err.println(errArgTxt);
            System.exit(1);
        }
        if (args.length > 5) {
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
        File seedsNAS = new File(dataDir.getAbsolutePath() + "/seeds/netarkivethosts.txt");
        if (!seedsNAS.isFile()) {
            System.err.println("ERROR: The given data-dir does not contains file '" + seedsNAS.getAbsolutePath() + "'");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        
        /**** args - level ****/
        //LookupLevel pLevel = LookupLevel.all;
        String pLevelTxt = args[4];
        if (!pLevelTxt.startsWith("level=")) {
            System.err.println("Missing arg level setting - got " + pLevelTxt);
            System.err.println(errArgTxt);
            System.exit(1);
        }
        pLevelTxt = MysqlX.getStringSetting(pLevelTxt);
        int level = 0;
        String err = "is not implemented (yet)";
    	if (pLevelTxt.equals("all")) { //pLevel = LookupLevel.all;
    		err = "Level " + pLevelTxt + err;
        } else if (pLevelTxt.equals("l1")) { //pLevel = LookupLevel.l1;      
    		level = 1;
        } else if (pLevelTxt.equals("l2")) { //pLevel = LookupLevel.l2;      
    		err = "Level " + pLevelTxt + err;
        } else if (pLevelTxt.equals("l1_l2")) { //pLevel = LookupLevel.l1_l2;      
    		err = "Level " + pLevelTxt + err;
        } else if (pLevelTxt.equals("l3")) { //pLevel = LookupLevel.l3;      
    		err = "Level " + pLevelTxt + err;
        } else {
            err = "Arg level setting is NOT valid - got '" + pLevelTxt + "'";
        }
    	if (level == 0) {
			System.err.println(err);
	        System.err.println(errArgTxt);
	        System.exit(1);
        }

        /*****************************************/
        /*** Start processing ********************/
    	/*** Writing domain levels into files for likely Danish of finished table data ***/ 
        /*****************************************/
        
    	if(!(machine.equals("test9") || machine.equals("ubun"))) {
            System.err.println("Wrong machine: " + machine);
            System.exit(1);
    	}

        /****** 
        /** table to be updated */
        Set<String> allTableSet = MysqlRes.getTables(conn);
    	String table = MysqlRes.domainNyeTableName(level) + MysqlRes.domaintable_all_infix;

	    //Update level tables if not allready done- 
        //for (int i=1;i<=MysqlX.noDomainLevels;i++) { if ( !MysqlX.skipLevel(pLevel, i) ) {
    	if (!allTableSet.contains(table)) {
			System.err.println("Table " + table + "did not exist");
	        System.err.println(errArgTxt);
	        System.exit(1);
    	}
    	
        /****** 
        /** Add Netarkivet fields */
        Set<String> fieldSet = MysqlRes.getFields(conn, table);
        String field = "FoundInNetarkivet";
    	if (fieldSet.contains(field)) {
	        System.out.println("Field " + field + " already added");
    	} else {
        	addNetarkivetLookupField(conn, table, field);
	        System.out.println("Added Field " + field);
    	};

        field = "LookedUpInNetarkivet";
    	if (fieldSet.contains(field)) {
	        System.out.println("Field " + field + " already added");
    	} else {
        	addNetarkivetLookupField(conn, table, field);
	        System.out.println("Added Field " + field);
    	};
    	
        
        /****** 
        /** read Netarkivet domains */
        BufferedReader fr = new BufferedReader(new FileReader(seedsNAS));
    	Set<String> domainsInNas = new HashSet<String>();
        
        String line;
        String trimmedLine;
        int count=0;
		while ((line = fr.readLine()) != null) {
            trimmedLine = line.trim();
            count++;
            if (!trimmedLine.isEmpty()) {
            	domainsInNas.add(trimmedLine);
            } else {
            	System.out.println("Line #" + count + " empty - ignored");
            }
		}
		fr.close();
		System.out.println("added domains: " + domainsInNas.size());
		
        /****** 
        /** read found domains */
        //List<DomSet> domSets = new ArrayList<DomSet>();
        //for (int level=1;level<=MysqlX.noDomainLevels;level++) {
        //	if ( !MysqlX.skipLevel(pLevel, level) ) {
		//	} else {
		//		DomSet domSet = new DomSet(); 
		//    	domSets.add(domSet);
		//    } }
    	DomNyeSet domSet = new DomNyeSet(); 
        domSet = readDomNyeSet(conn, level, table); 
        
        /****** 
        /** update domains with Netarkivet data */
        for (DomainNyeLevel ds: domSet.domSet) {
        	String dom = ds.Domain.toLowerCase().trim();
        	if (domainsInNas.contains(dom)) {
        		ds.FoundInNetarkivet = true;
        	}
    		ds.LookedUpInNetarkivet= true;
    		updateLineNetarkivetDomain(conn, table, ds);
        }
    }
	

    public static boolean updateLineNetarkivetDomain(Connection conn, String tablename, DomainNyeLevel ds) throws SQLException {
    	String sql = "UPDATE " + tablename + " " ;
    	sql = sql  + "SET FoundInNetarkivet = ?, ";
        sql = sql  +  "   LookedUpInNetarkivet = ? ";
    	sql = sql  + "WHERE Domain = ?"; 
 
    	PreparedStatement s = conn.prepareStatement(sql); 
	    
	    int index = 1;
	    s.setBoolean(index, ds.FoundInNetarkivet);
	    index++;
	    s.setBoolean(index, ds.LookedUpInNetarkivet);
	    index++;
	    s.setString(index, ds.Domain);
	    s.executeUpdate();
	    s.close();
	    return true;
    }

    public static boolean addNetarkivetLookupField(Connection conn, String table, String field) throws SQLException {
		String sql = "ALTER TABLE " + table + " ADD ( " + field  + " TINYINT(1) )";
		PreparedStatement s = conn.prepareStatement(sql);
	    s.executeUpdate();
	    s.close();
		return true;
	}

    public static class DomNyeSet {
    	public int level = 0;
    	public  Set<DomainNyeLevel> domSet = new HashSet<DomainNyeLevel>();
    }
    
    public static DomNyeSet readDomNyeSet(Connection conn, int level, String table) throws SQLException {
    	DomNyeSet domSet = new DomNyeSet();
    	domSet.level = level;
    	String selectSQL = "SELECT * FROM " + table; 
	    PreparedStatement s = conn.prepareStatement(selectSQL); 
	    ResultSet rs = s.executeQuery();
	    while (rs.next()) {
	    	DomainNyeLevel dl = new DomainNyeLevel(rs);
	    	domSet.domSet.add(dl);
	    }
    	return domSet;
    }


}
