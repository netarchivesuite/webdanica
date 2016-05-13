package dk.kb.webdanica.oldtools;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dk.kb.webdanica.criteria.LinksBase;
import dk.kb.webdanica.oldtools.MysqlX.*;

/* Structures for ResFromHadoop */

public class MysqlRes {

    /**
     * @param args None 
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws SQLException 
     * @throws IOException 
     */
    

	public static boolean execSqlLine(Connection conn, String sql) throws SQLException {
        /*    
        System.out.println(res.url);
        */
        PreparedStatement s = conn.prepareStatement(sql); 
	    s.executeUpdate();
	    s.close();
        return true;
    }

	public static boolean existsUrl(Connection conn, SingleCriteriaResult res, String tablename) throws SQLException {
        /*    
        System.out.println(res.url);
        */
    	String selectSQL = "SELECT Url FROM " + tablename + " WHERE Url = ? AND extWDate = ? ";
        PreparedStatement s = conn.prepareStatement(selectSQL); 
        s.setString(1, res.url);
        s.setTimestamp(2, res.Cext3);
        
        ResultSet rs = s.executeQuery();
        boolean found = rs.next();
        s.close();
        return found;
    }

	public static boolean existsDomain(Connection conn, String dom, String tablename) throws SQLException {
        /*    
        System.out.println(res.url);
        */
    	String selectSQL = "SELECT Domain FROM " + tablename + " WHERE Domain = ? ";
        PreparedStatement s = conn.prepareStatement(selectSQL); 
        s.setString(1, dom);
        ResultSet rs = s.executeQuery();
        boolean found = rs.next();
        s.close();
        return found;
    }

	public static DomainLevel readIfexistsDomain(Connection conn, String tablename, String dom) throws SQLException {
		DomainLevel dl = new DomainLevel();
    	String selectSQL = "SELECT * FROM " + tablename + " WHERE Domain = ? ";
    	//System.out.println("selectSQL " + selectSQL);
        PreparedStatement s = conn.prepareStatement(selectSQL); 
        s.setString(1, dom);
    	//System.out.println("selectSQL " + s);
		//try {
			ResultSet rs = s.executeQuery();
	        if (rs.next()) {
	        	dl = new DomainLevel(rs);
	        }
		//} catch(SQLException ex) {
		//}
	    s.close();
        return dl;
    }

	public static DomainNewLevel readIfexistsDomainNew(Connection conn, String tablename, String dom) throws SQLException {
		DomainNewLevel dl = new DomainNewLevel();
    	String selectSQL = "SELECT * FROM " + tablename + " WHERE Domain = ? ";
    	//System.out.println("selectSQL " + selectSQL);
        PreparedStatement s = conn.prepareStatement(selectSQL); 
        s.setString(1, dom);
    	//System.out.println("selectSQL " + s);
		//try {
			ResultSet rs = s.executeQuery();
	        if (rs.next()) {
	        	dl = new DomainNewLevel(rs);
	        }
		//} catch(SQLException ex) {
		//}
	    s.close();
        return dl;
    }

	public static DomainNewLevel readIfexistsDomainNewLevel(Connection conn, String tablename, String dom) throws SQLException {
		DomainNewLevel dl = new DomainNewLevel(0);
    	String selectSQL = "SELECT * FROM " + tablename + " WHERE Domain = ? ";
    	//System.out.println("selectSQL " + selectSQL);
        PreparedStatement s = conn.prepareStatement(selectSQL); 
        s.setString(1, dom);
    	//System.out.println("selectSQL " + s);
		//try {
			ResultSet rs = s.executeQuery();
	        if (rs.next()) {
	        	dl = new DomainNewLevel(rs);
	        }
		//} catch(SQLException ex) {
		//}
	    s.close();
        return dl;
    }

    public static SingleCriteriaResult readUrl(Connection conn, String tablename, SingleCriteriaResult res, boolean extendedNewHadoopTable) throws SQLException {
        /*    
        System.out.println(res.url);
        */
    	String selectSQL = "SELECT * FROM " + tablename + " WHERE Url = ? AND extWDate = ? ";
        PreparedStatement s = conn.prepareStatement(selectSQL); 
        s.setString(1, res.url);
        s.setTimestamp(2, res.Cext3);
        ResultSet rs = s.executeQuery();

        SingleCriteriaResult r = new SingleCriteriaResult();
        if (rs.next()) {
        	r = new SingleCriteriaResult(rs, extendedNewHadoopTable);
        }
        s.close();
        return r;
    }

    public static boolean insertLine(Connection conn, SingleCriteriaResult res, String tablename) throws SQLException {
	    /*    
	    System.out.println(res.url);
	    System.out.println(res.C1a);
	    System.out.println(res.C2a);
	    System.out.println(res.C3a);
	    System.out.println(res.C3b);
	    */
	    PreparedStatement s = conn.prepareStatement("INSERT INTO " + tablename + " "
	            + "(Url, UrlOrig, C1a, C2a, C2b, C3a, C3b,C3c,C3d,C3e,C3f,C3g,C4a,C5a,C5b," // 11+2+2
	            + " C6a, C6b, C6c, C6d,C7a, C7b, C7c, C7d, C7e, C7f, C7g, C7h," //9+3
	            + " C8a, C8b, C8c, C9a, C9b, C9c, C9d, C9e, C9f, C10a, C10b, C10c," // 8+4
	            + " C15a, C15b, C16a, C17a, C18a, " // 5
	            + " extSize, extDblChar,extWDate, extWDateOrig, " //4
	            + " intDanish, IsIASource, calcDanishCode" // 3 - 40+9+2 = 51
	            + ") VALUES ("
	            + "?,?,?,?,?,?,?,?,?,?, " // 10
	            + "?,?,?,?,?,?,?,?,?,?, " // 10
	            + "?,?,?,?,?,?,?,?,?,?, " // 10
	            + "?,?,?,?,?,?,?,?,?,?, " // 10
	            + "?,?,?,?,?,?,?,?,?,?,? )" // 11
	            ); // 51

	    int index = 1;

	    s.setString(index, res.url);

	    index++;
	    s.setString(index, res.urlOrig);
	    
	    index++;
	    if (res.C1a != null) {
	        s.setString(index, res.C1a);
	    } else {
	        s.setNull(index, Types.VARCHAR);
	    } 

	    index++;
	    if (res.C2a != null) {
	        s.setString(index, res.C2a);
	    } else {
	        s.setNull(index, Types.VARCHAR);
	    }
	    index++;
	    if (res.C2b != null) {
	        s.setString(index, res.C2b);
	    } else {
	        s.setNull(index, Types.VARCHAR);
	    }

	    index++;
	    if (res.C3a != null) {
	        s.setString(index, res.C3a);
	    } else {
	        s.setNull(index, Types.VARCHAR);
	    }
	    index++;
	    if (res.C3b != null) {
	        s.setString(index, res.C3b);
	    } else {
	        s.setNull(index, Types.VARCHAR);
	    }
	    
	    //        + " C6a,C6b,C6c
	    //,C7a,C7b,C7c,C7d,C7e, C7f,C8a,C8b,C9a,C9b,C9c,C9d,C10a,"
	    //        + " C10b, C15a, C15b, C16a, C17a, C18a
	    
	    index++;
	    if (res.C3c != null) {
	        s.setString(index, res.C3c);
	    } else {
	        s.setNull(index, Types.VARCHAR);
	    }
	    index++;
	    if (res.C3d != null) {
	        s.setString(index, res.C3d);
	    } else {
	        s.setNull(index, Types.VARCHAR);
	    }
	    index++;
	    if (res.C3e != null) {
	        s.setString(index, res.C3e);
	    } else {
	        s.setNull(index, Types.VARCHAR);
	    }
	    index++;
	    if (res.C3f != null) {
	        s.setString(index, res.C3f);
	    } else {
	        s.setNull(index, Types.VARCHAR);
	    }
	    index++;
	    if (res.C3g != null) {
	        s.setString(index, res.C3g);
	    } else {
	        s.setNull(index, Types.VARCHAR);
	    }

	    index++;
	    if (res.C4a != null) {
	        s.setString(index, res.C4a);
	    } else {
	        s.setNull(index, Types.VARCHAR);
	    }
	    
	    index++;
	    if (res.C5a != null) {
	        s.setString(index, res.C5a);
	    } else {
	        s.setNull(index, Types.VARCHAR);
	    }
	    
	    index++;
	    if (res.C5b != null) {
	        s.setString(index, res.C5b);
	    } else {
	        s.setNull(index, Types.VARCHAR);
	    }
	    //System.out.println("Index for c5b: " + index);
	    //+ " C6a,C6b,C6c
	    
	    index++;
	    if (res.C6a != null) {
	        s.setString(index, res.C6a);
	    } else {
	        s.setNull(index, Types.VARCHAR);
	    }
	    index++;
	    if (res.C6b != null) {
	        s.setString(index, res.C6b);
	    } else {
	        s.setNull(index, Types.VARCHAR);
	    }
	    index++;
	    if (res.C6c != null) {
	        s.setString(index, res.C6c);
	    } else {
	        s.setNull(index, Types.VARCHAR);
	    }
	    index++;
	    if (res.C6d != null) {
	        s.setString(index, res.C6d);
	    } else {
	        s.setNull(index, Types.VARCHAR);
	    }
	    //C7a,C7b,C7c,C7d,C7e, C7f
	    
	    index++;
	    if (res.C7a != null) {
	        s.setString(index, res.C7a);
	    } else {
	        s.setNull(index, Types.VARCHAR);
	    }
	    index++;
	    if (res.C7b != null) {
	        s.setString(index, res.C7b);
	    } else {
	        s.setNull(index, Types.VARCHAR);
	    }
	    index++;
	    if (res.C7c != null) {
	        s.setString(index, res.C7c);
	    } else {
	        s.setNull(index, Types.VARCHAR);
	    }
	    
	    index++;
	    if (res.C7d != null) {
	        s.setString(index, res.C7d);
	    } else {
	        s.setNull(index, Types.VARCHAR);
	    }
	    index++;
	    if (res.C7e != null) {
	        s.setString(index, res.C7e);
	    } else {
	        s.setNull(index, Types.VARCHAR);
	    }
	    index++;
	    if (res.C7f != null) {
	        s.setString(index, res.C7f);
	    } else {
	        s.setNull(index, Types.VARCHAR);
	    }
	    index++;
	    if (res.C7g != null) {
	        s.setString(index, res.C7g);
	    } else {
	        s.setNull(index, Types.VARCHAR);
	    }
	    index++;
	    if (res.C7h != null) {
	        s.setString(index, res.C7h);
	    } else {
	        s.setNull(index, Types.VARCHAR);
	    }
	    
	    index++;
	    if (res.C8a != null) {
	        s.setString(index, res.C8a);
	    } else {
	        s.setNull(index, Types.VARCHAR);
	    }
	    index++;
	    if (res.C8b != null) {
	        s.setString(index, res.C8b);
	    } else {
	        s.setNull(index, Types.VARCHAR);
	    }
	    index++;
	    if (res.C8c != null) {
	        s.setString(index, res.C8c);
	    } else {
	        s.setNull(index, Types.VARCHAR);
	    }
	    
	    index++;
	    if (res.C9a != null) {
	        s.setString(index, res.C9a);
	    } else {
	        s.setNull(index, Types.VARCHAR);
	    }
	    index++;
	    if (res.C9b != null) {
	        s.setString(index, res.C9b);
	    } else {
	        s.setNull(index, Types.VARCHAR);
	    }
	    index++;
	    if (res.C9c != null) {
	        s.setString(index, res.C9c);
	    } else {
	        s.setNull(index, Types.VARCHAR);
	    }
	    
	    index++;
	    if (res.C9d != null) {
	        s.setString(index, res.C9d);
	    } else {
	        s.setNull(index, Types.VARCHAR);
	    }
	    index++;
	    if (res.C9e != null) {
	        s.setString(index, res.C9e);
	    } else {
	        s.setNull(index, Types.VARCHAR);
	    }
	    index++;
	    if (res.C9f != null) {
	        s.setString(index, res.C9f);
	    } else {
	        s.setNull(index, Types.VARCHAR);
	    }
	    
	    index++;
	    if (res.C10a != null) {
	        s.setString(index, res.C10a);
	    } else {
	        s.setNull(index, Types.VARCHAR);
	    }
	    
	    //System.out.println("Index for c10a: " + index);
	    index++;
	    if (res.C10b != null) {
	        s.setString(index, res.C10b);
	    } else {
	        s.setNull(index, Types.VARCHAR);
	    }
	    index++;
	    if (res.C10c != null) {
	        s.setString(index, res.C10c);
	    } else {
	        s.setNull(index, Types.VARCHAR);
	    }
	    
	    //C15a, C15b, C16a, C17a,
	    index++;
	    if (res.C15a != null) {
	        s.setString(index, res.C15a);
	    } else {
	        s.setNull(index, Types.VARCHAR);
	    }
	    
	    index++;
	    if (res.C15b != null) {
	        s.setString(index, res.C15b);
	    } else {
	        s.setNull(index, Types.VARCHAR);
	    }

	    index++;
	    if (res.C16a != null) {
	        s.setLong(index, Long.parseLong(res.C16a));
	    } else {
	        s.setNull(index, Types.BIGINT);
	    }
	    
	    index++;
	    if (res.C17a != null) {
	        s.setLong(index, Long.parseLong(res.C17a));
	    } else {
	        s.setNull(index, Types.BIGINT);
	    }
	
	    index++;
	    if (res.C18a != null) {
	        s.setString(index, res.C18a);
	    } else {
	        s.setNull(index, Types.VARCHAR);
	    }

	    index++;
	    s.setLong(index, res.Cext1); // = extSize in table
	    
	    index++;
	    if (res.Cext2 != null) {
	        s.setLong(index, res.Cext2); // = extDblChar in table (truncated to int during processing)
	    } else {
	        s.setNull(index, Types.BIGINT); // can be null if Cext1 == 0
	    }
	    
	    index++;
	    s.setTimestamp(index, res.Cext3); // = date

	    index++;
	    s.setString(index, res.Cext3Orig); // = date
	    
	    index++;
	    s.setFloat(index, res.intDanish); // Could be added to hadoop job later, so included here 
	    
	    index++;
	    s.setBoolean(index, res.IsIASource); // = extWDate in table (truncated to int during processing)

	    index++;
	    s.setInt(index, res.calcDanishCode); // = extWDate in table (truncated to int during processing)
	    
	    s.executeUpdate();
	    s.close();
	    return true;
    }

    public static boolean updateLineCalcDanishCode(Connection conn, String tablename, int calcDanishCode, String recUrl, java.sql.Timestamp recDate) throws SQLException {
    	String sql = "UPDATE " + tablename + " " ;
    	sql = sql  + "SET calcDanishCode = ? ";
    	sql = sql  + "WHERE Url = ? AND extWDate = ?"; 
 
    	PreparedStatement s = conn.prepareStatement(
    			sql
//    			"UPDATE " + tablename + " " 
//	            + "SET calcDanishCode = ? "
//	    		+ "WHERE Url = ? AND extWDate = ?" 
	           ); 
	    
	    int index = 1;
	    s.setInt(index, calcDanishCode);
	    index++;
	    s.setString(index, recUrl);
	    index++;
	    s.setTimestamp(index, recDate);
	    s.executeUpdate();
	    s.close();
	    return true;
    }

    public static boolean updateDblChar130(Connection conn, String tablename) throws SQLException {
    	String sql = "UPDATE " + tablename + " " ;
    	sql = sql  + "SET calcDanishCode = 220 ";
    	sql = sql  + "WHERE extDblChar>=130 AND calcDanishCode<=0"; 
    	PreparedStatement s = conn.prepareStatement(sql); 
	    s.executeUpdate();
	    s.close();
	    return true;
    }

    public static boolean updateHadoopLines(Connection conn, Set<String> tablenameSet, SingleCriteriaResult res) throws SQLException {
    	boolean found = false;
    	for (String t: tablenameSet) {
        	found = updateHadoopLineSingleTable(conn, t, res);
        	if (found) break;
        }
	    return found;
    }

    public static boolean updateHadoopLineSingleTable(Connection conn, String tablename, SingleCriteriaResult res) throws SQLException {
    	String sql = "UPDATE " + tablename + " " ;
    	sql = sql  + "SET calcDanishCode = ?, ";
        sql = sql  +  "   intDanish = ?, ";
        sql = sql  +  "   C2b = ?, ";
        sql = sql  +  "   C3g = ?, ";
        sql = sql  +  "   C6d = ?, ";
        sql = sql  +  "   C7g = ?, ";
        sql = sql  +  "   C7h = ?, ";
        sql = sql  +  "   C8c = ?, ";
        sql = sql  +  "   C9e = ?, ";
        sql = sql  +  "   C9f = ?, ";
        sql = sql  +  "   C10c = ? ";
    	sql = sql  + "WHERE Url = ? AND extWDate = ?"; 
    	PreparedStatement s = conn.prepareStatement( sql ); 
	    
	    int index = 1;
	    s.setInt(index, res.calcDanishCode);
	    index++;
	    s.setDouble(index, res.intDanish);
	    index++;
	    s.setString(index, res.C2b);
	    index++;
	    s.setString(index, res.C3g);
	    index++;
	    s.setString(index, res.C6d);
	    index++;
	    s.setString(index, res.C7g);
	    index++;
	    s.setString(index, res.C7h);
	    index++;
	    s.setString(index, res.C8c);
	    index++;
	    s.setString(index, res.C9e);
	    index++;
	    s.setString(index, res.C9f);
	    index++;
	    s.setString(index, res.C10c);
	    index++;
	    s.setString(index, res.url);
	    index++;
	    s.setTimestamp(index, res.Cext3);
	    
	    boolean ok = true;
		try {
			s.executeUpdate();                
		} catch(SQLException ex) {
			ok = false;
		}
	    s.close();
	    return ok;
    }
        
    public static boolean updateLineCalcDanishCodeAndIntDanish(Connection conn, String tablename, int calcDanishCode, double intDanish, String recUrl, java.sql.Timestamp recDate) throws SQLException {
    	String sql = "UPDATE " + tablename + " " ;
    	sql = sql  + "SET calcDanishCode = ?, ";
        sql = sql  +  "   intDanish = ? ";
    	sql = sql  + "WHERE Url = ? AND extWDate = ?"; 
	    PreparedStatement s = conn.prepareStatement(
	    		sql
//	    		"UPDATE " + tablename + " " 
//	    		+ "SET"
//	            + "  calcDanishCode = ?, "
//	            + "  intDanish = ? "
//	    		+ "WHERE Url = ? AND extWDate = ?" 
	           ); 
	    int index = 1;
	    s.setInt(index, calcDanishCode);
	    index++;
	    s.setDouble(index, intDanish);
	    index++;
	    s.setString(index, recUrl);
	    index++;
	    s.setTimestamp(index, recDate);
	    s.executeUpdate();
	    s.close();
	    return true;
    }
    
    public static boolean updateLineMysqlHack(Connection conn, String tablename, String hack_field, String val, String recUrl, java.sql.Timestamp recDate) throws SQLException {
    	String sql = "UPDATE " + tablename + " " ;
    	sql = sql  + "SET " + hack_field + " = " + val  + " ";
    	sql = sql  + "WHERE Url = ? AND extWDate = ?"; 
	    PreparedStatement s = conn.prepareStatement( sql ); 
	    int index = 1;
	    s.setString(index, recUrl);
	    index++;
	    s.setTimestamp(index, recDate);
	    s.executeUpdate();
	    s.close();
	    return true;
	}

    public static boolean updateNullsMysqlHack(Connection conn, String tablename,  String hackfield, String oldval, String newval) throws SQLException {
    	String sql = "UPDATE " + tablename + " " ;
    	sql = sql  + "SET " + hackfield + " = " + newval + " "; //hack_notset_value
    	sql = sql  + "WHERE " + hackfield + " = " + oldval; 
	    PreparedStatement s = conn.prepareStatement( sql ); 
	    s.executeUpdate();
	    s.close();
	    System.out.println("hacksql 2" + s.getUpdateCount());
	    return true;
	}
    
	public static long getTableCntEntries(Connection conn, String tablename) throws SQLException {
		return getTableCntEntries(conn, tablename, "");
	}
	   
	public static long getTableCntEntries(Connection conn, String tablename, String where) throws SQLException {
		String selectSQL = "SELECT count(*) as cnt FROM " + tablename + (where.isEmpty()? "": " WHERE " + where);
	    PreparedStatement s = conn.prepareStatement(selectSQL); 
	    ResultSet rs = s.executeQuery();
	    rs.next();
	    return rs.getLong("cnt");
	}

	public static Set<String> getTableUrlEntries(Connection conn, String tablename, String where) throws SQLException {
	    Set<String> urlSet = new HashSet<String>();
		String selectSQL = "SELECT Domain FROM " + tablename + (where.isEmpty()? "": " WHERE " + where);
	    PreparedStatement s = conn.prepareStatement(selectSQL); 
	    ResultSet rs = s.executeQuery();
	    
	    while (rs.next()) {
	    	urlSet.add( rs.getString(1).trim() );
	    }
	    s.close();
    	return urlSet;
	}

	public static boolean createTableWithIndexes(Connection conn, String table, String jdbcUser)  throws SQLException {
		boolean ok = createTable(conn, table, jdbcUser);
		if (ok) ok = createIndexes(conn, table);
		return ok;
	}
	
	public static boolean createIndexes(Connection conn, String table)  throws SQLException {
		boolean ok = false;
		ok = createPrimaryIndexSql(conn, table);
		ok = ok & createIndexSql(conn, table, "calcDanishCode");
		ok = ok & createIndexSql(conn, table, "extSize");
		ok = ok & createIndexSql(conn, table, "intDanish");
		ok = ok & createIndexSql(conn, table, "IsIASource");
		return ok;
	}
	
    private static boolean createIndexSql(Connection conn, String tablename, String field) throws SQLException {
    	String sql = "ALTER TABLE " + tablename + " ADD INDEX index" + field + " (" + field + ") ";
    	PreparedStatement s = conn.prepareStatement(sql);
	    s.executeUpdate();
	    s.close();
    	return true;
    }

    private static boolean createPrimaryIndexSql(Connection conn, String tablename) throws SQLException {
    	//NOTE test machine does not have same limit
    	String sql = "ALTER TABLE " + tablename + " ADD PRIMARY KEY (Url, extWDate) ";
    	PreparedStatement s = conn.prepareStatement(sql);
	    s.executeUpdate();
	    s.close();
    	return true;
    }

    public static boolean createTable(Connection conn, String tablename, String hackUser, Set<String> tableSetExists) throws SQLException {
    	boolean ok = true; 
		ok = createTable(conn, tablename, hackUser);
	    return ok;
    }
    	
    public static boolean createTable(Connection conn, String tablename, String hackUser) throws SQLException {
    	boolean ok = true; 
    	//NOTE test machine does not have same limit
    	String sql = "CREATE TABLE " + tablename + " ( ";
    	if (hackUser.equals("elzi")) {
        	sql = sql + "  Url varchar(500), ";
    	} else {
        	sql = sql + "  Url varchar(900), ";
    	}
    	sql = sql + "  extWDate DATETIME, "; 
    	sql = sql + "  UrlOrig TEXT, "; 
    	sql = sql + "  extWDateOrig varchar(14), "; 
    	sql = sql + "  extSize BIGINT, "; 
    	sql = sql + "  extDblChar INT, "; 
    	sql = sql + "  C1a TEXT, "; 
    	sql = sql + "  C2a varchar(500), "; 
    	sql = sql + "  C2b varchar(500), "; //NewHadoop
    	sql = sql + "  C3a varchar(500), "; 
    	sql = sql + "  C3b TEXT, "; 
    	sql = sql + "  C3c varchar(500), "; 
    	sql = sql + "  C3d TEXT, ";        
    	sql = sql + "  C3e TEXT, ";             //Added 9/9 as 3b  form ae, oe/o, aa but restrcted list
    	sql = sql + "  C3f varchar(500), ";     //Added 9/9 as 3d  form ae, oe/o, aa but restrcted list in URL
    	sql = sql + "  C3g TEXT, "; //NewHadoop
    	sql = sql + "  C4a varchar(100), "; 
    	sql = sql + "  C5a varchar(500), "; 
    	sql = sql + "  C5b varchar(500), ";
    	sql = sql + "  C6a TEXT, "; 
    	sql = sql + "  C6b varchar(100), "; 
    	sql = sql + "  C6c varchar(100), ";
    	sql = sql + "  C6d TEXT, "; //NewHadoop
    	sql = sql + "  C7a TEXT, ";
    	sql = sql + "  C7b varchar(500), "; 
    	sql = sql + "  C7c TEXT, "; 
    	sql = sql + "  C7d varchar(500), ";
    	sql = sql + "  C7e TEXT, "; 
    	sql = sql + "  C7f varchar(500), ";
    	sql = sql + "  C7g TEXT, "; //NewHadoop
    	sql = sql + "  C7h TEXT, "; //NewHadoop
    	sql = sql + "  C8a TEXT, "; 
    	sql = sql + "  C8b varchar(500), ";
    	sql = sql + "  C8c TEXT, "; //NewHadoop
    	sql = sql + "  C9a varchar(500), "; 
    	sql = sql + "  C9b TEXT, "; 
    	sql = sql + "  C9c varchar(500), ";
    	sql = sql + "  C9d varchar(500), ";
    	sql = sql + "  C9e TEXT, "; //NewHadoop
    	sql = sql + "  C9f varchar(500), ";//NewHadoop
    	sql = sql + "  C10a TEXT, "; 
    	sql = sql + "  C10b TEXT, "; 
    	sql = sql + "  C10c TEXT, "; //NewHadoop 
    	sql = sql + "  C15a varchar(10), "; 
    	sql = sql + "  C15b varchar(20), "; 
    	sql = sql + "  C16a BIGINT, "; 
    	sql = sql + "  C17a BIGINT, "; 
    	sql = sql + "  C18a varchar(1), "; 
    	sql = sql + "  intDanish FLOAT, "; 
    	sql = sql + "  IsIASource TINYINT(1), ";
    	sql = sql + "  calcDanishCode MEDIUMINT(3) "; //updated from calcDanishCode SMALLINT(2) 9/9
    	sql = sql + "  )";
    	
    	PreparedStatement s = conn.prepareStatement(sql);
	    s.executeUpdate();
	    s.close();
	    return ok;
    }

    public static boolean createDomainLevelTable(Connection conn, String tablename) throws SQLException {
    	boolean ok = true; 
    	//NOTE test machine does not have same limit
    	String sql = "CREATE TABLE " + tablename + " ( "
    			+ "  Domain varchar(900), "
    			+ "  InIaData TINYINT(1), "
    			+ "  InNasData TINYINT(1), "
    			+ "  CntInBerkeley INT(4), "
    			+ "  LookedUpInBerkeley TINYINT(1), "
    			+ "  OnlyCaseSensitive TINYINT(1), "
    			+ "  calcCodes varchar(100) ";
    	sql = sql + "  )";
    	PreparedStatement s = conn.prepareStatement(sql);
	    s.executeUpdate();
	    s.close();

    	sql = "ALTER TABLE " + tablename + " ADD PRIMARY KEY (DomainLowercase) ";
    	s = conn.prepareStatement(sql);
	    s.executeUpdate();
	    s.close();
	    return ok;
    }
    
    public static boolean createDomainNewLevelTable(Connection conn, String tablename) throws SQLException {
    	boolean ok = true; 
    	//NOTE test machine does not have same limit
    	String sql = "CREATE TABLE " + tablename + " ( "
        			+ "  Domain varchar(900), "
        			+ "  Level INT(3), "
        			+ "  CntInIaData INT(4), "
        			+ "  CntInNasData INT(4), "
        			+ "  CntInBerkeley INT(4), "
        			+ "  LookedUpInBerkeley TINYINT(1), "
        			+ "  OnlyCaseSensitive TINYINT(1), "
        			+ "  CalcCode INT(3), "
        			+ "  CalcCodes varchar(100) ";
    	sql = sql + "  )";
    	PreparedStatement s = conn.prepareStatement(sql);
	    s.executeUpdate();
	    s.close();

    	sql = "ALTER TABLE " + tablename + " ADD PRIMARY KEY (Domain) ";
    	s = conn.prepareStatement(sql);
	    s.executeUpdate();
	    s.close();
	    return ok;
    }

    public static boolean createDomainLevelNYETable(Connection conn, String tablename) throws SQLException {
    	boolean ok = true; 
    	//NOTE test machine does not have same limit
    	String sql = "CREATE TABLE " + tablename + " ( "
    			+ "  Domain varchar(900), "
    			+ "  InIaData TINYINT(1), "
    			+ "  InNasData TINYINT(1), "
    			+ "  CntInBerkeley INT(4), "
    			+ "  LookedUpInBerkeley TINYINT(1), "
    			+ "  FoundInNetarkivet TINYINT(1), "
    			+ "  LookedUpInNetarkivet TINYINT(1), "
    			+ "  OnlyCaseSensitive TINYINT(1), "
    			+ "  calcCodes varchar(100) ";
    	sql = sql + "  )";
    	PreparedStatement s = conn.prepareStatement(sql);
	    s.executeUpdate();
	    s.close();

    	sql = "ALTER TABLE " + tablename + " ADD PRIMARY KEY (DomainLowercase) ";
    	s = conn.prepareStatement(sql);
	    s.executeUpdate();
	    s.close();
	    return ok;
    }

    public static boolean createLookedUpDomainLevelTable(Connection conn, String tablename) throws SQLException {
    	boolean ok = true; 
    	//NOTE test machine does not have same limit
    	String sql = "CREATE TABLE " + tablename + " ( "
        			+ "  Domain varchar(900),"
        			+ "  CntInBerkeley INT(4) "
        			+ " )";
    	PreparedStatement s = conn.prepareStatement(sql);
	    s.executeUpdate();
	    s.close();

    	sql = "ALTER TABLE " + tablename + " ADD PRIMARY KEY (Domain) ";
    	s = conn.prepareStatement(sql);
	    s.executeUpdate();
	    s.close();
	    return ok;
    }

    public static class CriteriaUpdate10 {
        public String url;
        public java.sql.Timestamp extWDate; //Cext3;
        public float intDanish;
        public int calcDanishCode;
        public String C3b;
        public String C3d;
        public String C3e;
        public String C3f;

        public CriteriaUpdate10(ResultSet rs) throws SQLException {
            this.url = rs.getString("Url");
            this.extWDate= rs.getTimestamp("extWDate");
        	this.C3b = rs.getString("C3b");
        	this.C3d = rs.getString("C3d");
        	this.C3e = rs.getString("C3e");
        	this.C3f = rs.getString("C3f");
            this.intDanish = rs.getFloat("intDanish");
            this.calcDanishCode = rs.getInt("calcDanishCode");
        }
    }

    public static class CriteriaUpdateCalcCode {
        public String url;
        public java.sql.Timestamp extWDate; //Cext3;
        public int calcDanishCode;

        public CriteriaUpdateCalcCode(ResultSet rs) throws SQLException {
            this.url = rs.getString("Url");
            this.extWDate= rs.getTimestamp("extWDate");
            this.calcDanishCode = rs.getInt("calcDanishCode");
        }
    }

    public static class CriteriaUpdateCalcCodeAndInt {
        public String url;
        public java.sql.Timestamp extWDate; //Cext3;
        public float intDanish;
        public int calcDanishCode;

        public CriteriaUpdateCalcCodeAndInt(ResultSet rs) throws SQLException {
            this.url = rs.getString("Url");
            this.extWDate= rs.getTimestamp("extWDate");
            this.intDanish = rs.getFloat("intDanish");
            this.calcDanishCode = rs.getInt("calcDanishCode");
        }
    }

    public static class CodesResult {
        public float intDanish;
        public int calcDanishCode;

        public CodesResult() {
        	intDanish = 0F;
        	calcDanishCode = 0;
        }
    }
    
    public static class CriteriaUpdate11 {
        public String url;
        public java.sql.Timestamp extWDate; //Cext3;
        public int extDblChar; //Cext2
        public String C1a;
        public String C2a;
        public float intDanish;
        public int calcDanishCode;

        public CriteriaUpdate11(ResultSet rs) throws SQLException {
            this.url = rs.getString("Url");
            this.extWDate= rs.getTimestamp("extWDate");
            this.extDblChar = rs.getInt("extDblChar");
            this.C1a = rs.getString("C1a");
            this.C2a = rs.getString("C2a");
            this.intDanish = rs.getFloat("intDanish");
            this.calcDanishCode = rs.getInt("calcDanishCode");
        }
    }
    
    public static class CriteriaUpdateC16a {
        public String url;
        public String urlOrig;
        public java.sql.Timestamp extWDate; //Cext3;
        public String C16a;
        public int calcDanishCode;

        public CriteriaUpdateC16a(ResultSet rs) throws SQLException {
            this.url = rs.getString("Url");
            this.urlOrig = rs.getString("UrlOrig");
            this.extWDate= rs.getTimestamp("extWDate");
            this.C16a = rs.getString("C16a");
            this.calcDanishCode = rs.getInt("calcDanishCode");
        }
    }

    public static class CriteriaKeyAndTable {
        public String tablename;
        public String url;
        public String urlOrig; //only set if != url
        public java.sql.Timestamp Cext3;
        public String Cext3Orig; //date
        public SingleCriteriaResult allres = new SingleCriteriaResult();
    }

    public static class SingleCriteriaResult {
        public String url;
        public String urlOrig; //only set if != url
        public Long Cext1;
        public Long Cext2;
        public java.sql.Timestamp Cext3;
        public String Cext3Orig; //date
        public String C1a;
        public String C2a;
        public String C2b; //*
        public String C4a;
        public String C3a;
        public String C6a;
        public String C3b;
        public String C3c;
        public String C3d;
        public String C3e;
        public String C3f;
        public String C3g; //*
        public String C6b;
        public String C6c;
        public String C6d; //*
        public String C5a;
        public String C5b;
        public String C7a;
        public String C7b;
        public String C7c;
        public String C7d;
        public String C7e;
        public String C7f;
        public String C7g; //*
        public String C7h; //*
        public String C8a;
        public String C8b;
        public String C8c; //*
        public String C9a;
        public String C9b;
        public String C9c;
        public String C9d;
        public String C9e; //*
        public String C9f; //*
        public String C10a;
        public String C10b;
        public String C10c; //*
        public String C15a;
        public String C15b;
        public String C16a;
        public String C17a;
        public String C18a;
        public float intDanish;
        public boolean IsIASource;
        public int calcDanishCode;
        public String tablename; //only for UrlExtract

        public SingleCriteriaResult(String trimmedLine, boolean ingestMode) {
            String[] resultParts = trimmedLine.split(",");   
            for (String resultPart: resultParts) {
                String trimmedResultPart = resultPart.trim();
                parseString(trimmedResultPart, ingestMode); // Assigns Values to criteria
            }
        	/*** url hack in order to have PK size < 1000 bytes ***/
    	    if (url.length() > 900) {
    	    	urlOrig =url;
    	    	url =url.substring(0, 900);
    	    } else {
    	    	urlOrig =""; //only set if != url
    	    }
        	/*** date/time hack in order to have efficient PK ***/
    	    
    	    if (Cext3Orig==null || Cext3Orig.isEmpty()) {
    	    	System.err.println("no date for url: " + url + " --- got: " + Cext3Orig);
    	    }
    	    Cext3 = findDateFromString(Cext3Orig);
        }

        public SingleCriteriaResult(ResultSet res, boolean extendedNewHadoopTable)  throws SQLException {
        	parseResultSet(res, extendedNewHadoopTable);
        }

        public SingleCriteriaResult() {
            url="";
            urlOrig=""; //only set if != url
            Cext1 =0L;
            Cext2 =0L;
            Cext3Orig="20140901000000"; //date
            Cext3 = findDateFromString(Cext3Orig);
            C4a="";
            C3a="";
            C6a="";
            C3b="";
            C3c="";
            C3d="";
            C3e="";
            C3f="";
            C3g="";
            C6b="";
            C6c="";
            C6d="";
            C5a="";
            C5b="";
            C1a="";
            C2a="";
            C2b="";
            C7a="";
            C7b="";
            C7c="";
            C7d="";
            C7e="";
            C7f="";
            C7g="";
            C7h="";
            C8a="";
            C8b="";
            C8c="";
            C9a="";
            C9b="";
            C9c="";
            C9d="";
            C9e="";
            C9f="";
            C10a="";
            C10b="";
            C10c="";
            C15a="";
            C15b="";
            C16a="";
            C17a="";
            C18a="";
            intDanish = 0F;
            IsIASource = true;
            calcDanishCode = 0;
        }

	    private void parseString(String trimmedResultPart, boolean ingestMode) {
        	//There may be ',' in URL, therefore unexpected text will be part of Url
        	//Although a criteria cannot be part of an URL
            if (trimmedResultPart.startsWith("http")) {
            	this.url = trimmedResultPart;
            }
            else if (trimmedResultPart.startsWith("C")) 
            {
                String[] valueparts = trimmedResultPart.split(":");
                if (valueparts.length > 1) {
	                if (trimmedResultPart.startsWith("Cext1")) {
	                    //System.out.println("Cext1: " + trimmedResultPart);
	                    this.Cext1 = Long.parseLong(valueparts[1].trim());
	                    //System.out.println("Cext1: " + this.Cext1);
	                } else if (trimmedResultPart.startsWith("Cext2")) {
	                    this.Cext2 = Long.parseLong(valueparts[1].trim());
	                    //System.out.println("Cext2: " + this.Cext2);
	                } else if (trimmedResultPart.startsWith("Cext3")) {
	                    this.Cext3Orig = valueparts[1].trim();
	                    //System.out.println("Cext3: " + this.Cext3);
	                } else if (trimmedResultPart.startsWith("C4a")) {
	                    this.C4a = valueparts[1].trim();
	                    //System.out.println("C4a: " + this.C4a);
	                } else if (trimmedResultPart.startsWith("C1a")) {
	                    this.C1a = valueparts[1].trim();
	                    //System.out.println("C1a: " + this.C1a);
	                } else if (trimmedResultPart.startsWith("C2a")) {
	                    this.C2a = valueparts[1].trim();
	                    //System.out.println("C2a: " + this.C2a);
	                } else if (trimmedResultPart.startsWith("C2b")) {
	                    this.C2b = valueparts[1].trim(); 				//TODO: check with ingestMode?
	                    //System.out.println("C2b: " + this.C2b);
	                } else if (trimmedResultPart.startsWith("C3a")) {
	                    this.C3a = valueparts[1].trim();
	                    //System.out.println("C3a: " + this.C3a); 
	                } else if (trimmedResultPart.startsWith("C3b")) {
	                    this.C3b = valueparts[1].trim();
	                    //System.out.println("C3b: " + this.C3b);
	                } else if (trimmedResultPart.startsWith("C3c")) {
	                    this.C3c = valueparts[1].trim();
	                    //System.out.println("C3c: " + this.C3c);
	                } else if (trimmedResultPart.startsWith("C3d")) {
	                    this.C3d = valueparts[1].trim();
	                    //System.out.println("C3d: " + this.C3d);
	                } else if (trimmedResultPart.startsWith("C3g")) {
	                    this.C3g = valueparts[1].trim(); 				//TODO: check with ingestMode?
	                    //System.out.println("C3g: " + this.C3g);
	                } else if (trimmedResultPart.startsWith("C5a")) {
	                    this.C5a = valueparts[1].trim();
	                    //System.out.println("C5a: " + this.C5a);
	                } else if (trimmedResultPart.startsWith("C5b")) {
	                    this.C5b = valueparts[1].trim();
	                    //System.out.println("C5b: " + this.C5b);
	                } else if (trimmedResultPart.startsWith("C6a")) {
	                    this.C6a = valueparts[1].trim();
	                    //System.out.println("C6a: " + this.C6a);
	                } else if (trimmedResultPart.startsWith("C6b")) {
	                    this.C6b = valueparts[1].trim();
	                    //System.out.println("C6b: " + this.C6b);
	                } else if (trimmedResultPart.startsWith("C6c")) {
	                    this.C6c = valueparts[1].trim();
	                    //System.out.println("C6c: " + this.C6c);
	                } else if (trimmedResultPart.startsWith("C6d")) {
	                    this.C6d = valueparts[1].trim(); 				//TODO: check with ingestMode?
	                    //System.out.println("C6d: " + this.C6d);
	                } else if (trimmedResultPart.startsWith("C7a")) {
	                    this.C7a = valueparts[1].trim();
	                    //System.out.println("C7a: " + this.C7a);
	                } else if (trimmedResultPart.startsWith("C7b")) {
	                    this.C7b = valueparts[1].trim();
	                    //System.out.println("C7b: " + this.C7b);    
	                } else if (trimmedResultPart.startsWith("C7c")) {
	                    this.C7c = valueparts[1].trim();
	                    //System.out.println("C7c: " + this.C7c);    
	                } else if (trimmedResultPart.startsWith("C7d")) {
	                    this.C7d = valueparts[1].trim();
	                    //System.out.println("C7d: " + this.C7d);    
	                } else if (trimmedResultPart.startsWith("C7e")) {
	                    this.C7e = valueparts[1].trim();
	                    //System.out.println("C7e: " + this.C7e);    
	                } else if (trimmedResultPart.startsWith("C7f")) {
	                    this.C7f = valueparts[1].trim();
	                    //System.out.println("C7f: " + this.C7f);    
	                } else if (trimmedResultPart.startsWith("C7g")) {
	                    this.C7g = valueparts[1].trim(); 				//TODO: check with ingestMode?
	                    //System.out.println("C7g: " + this.C7g);
	                } else if (trimmedResultPart.startsWith("C7h")) {
	                    this.C7h = valueparts[1].trim(); 				//TODO: check with ingestMode?
	                    //System.out.println("C7h: " + this.C7h);
	                } else if (trimmedResultPart.startsWith("C8a")) {
	                    this.C8a = valueparts[1].trim();
	                    //System.out.println("C8a: " + this.C8a);    
	                } else if (trimmedResultPart.startsWith("C8b")) {
	                    this.C8b = valueparts[1].trim();
	                    //System.out.println("C8b: " + this.C8b);    
	                } else if (trimmedResultPart.startsWith("C8c")) {
	                    this.C8c = valueparts[1].trim(); 				//TODO: check with ingestMode?
	                    //System.out.println("C8c: " + this.C8c);
	                } else if (trimmedResultPart.startsWith("C9a")) {
	                    this.C9a = valueparts[1].trim();
	                    //System.out.println("C9a: " + this.C9a);    
	                } else if (trimmedResultPart.startsWith("C9b")) {
	                    this.C9b = valueparts[1].trim();
	                    //System.out.println("C9b: " + this.C9b);    
	                } else if (trimmedResultPart.startsWith("C9c")) {
	                    this.C9c = valueparts[1].trim();
	                    //System.out.println("C9c: " + this.C9c);    
	                } else if (trimmedResultPart.startsWith("C9d")) {
	                    this.C9d = valueparts[1].trim();
	                    //System.out.println("C9d: " + this.C9d);    
	                } else if (trimmedResultPart.startsWith("C9e")) {
	                    this.C9e = valueparts[1].trim(); 				//TODO: check with ingestMode?
	                    //System.out.println("C9e: " + this.C9e);
	                } else if (trimmedResultPart.startsWith("C9f")) {
	                    this.C9f = valueparts[1].trim(); 				//TODO: check with ingestMode?
	                    //System.out.println("C9f: " + this.C9f);
	                } else if (trimmedResultPart.startsWith("C10a")) {
	                    this.C10a = valueparts[1].trim();
	                    //System.out.println("C10a: " + this.C10a);    
	                } else if (trimmedResultPart.startsWith("C10b")) {
	                    this.C10b = valueparts[1].trim();
	                    //System.out.println("C10b: " + this.C10b);    
	                } else if (trimmedResultPart.startsWith("C10c")) {
	                    this.C10c = valueparts[1].trim(); 				//TODO: check with ingestMode?
	                    //System.out.println("C10c: " + this.C10c);
	                } else if (trimmedResultPart.startsWith("C15a")) {
	                    this.C15a = valueparts[1].trim();
	                    //System.out.println("C15a: " + this.C15a);    
	                } else if (trimmedResultPart.startsWith("C15b")) {
	                    this.C15b = valueparts[1].trim();
	                    //System.out.println("C15b: " + this.C15b);    
	                } else if (trimmedResultPart.startsWith("C16a")) {
	                    this.C16a = valueparts[1].trim();
	                    //System.out.println("C16a: " + this.C16a);    
	                } else if (trimmedResultPart.startsWith("C17a")) {
	                    this.C17a = valueparts[1].trim();
	                    //System.out.println("C17a: " + this.C17a);   
	                } else if (trimmedResultPart.startsWith("C18a")) {
                        this.C18a = valueparts[1].trim();
                        //System.out.println("C18a: " + this.C18a);    
	                } else {
	                	//not abbr. for criteria thus it is not a criteria, and therefore must be part of Url
	                	this.url = this.url + "," + trimmedResultPart.trim();
	                }
                } else {
                	//no ":" thus it is not a criteria, and therefore must be part of Url
                	this.url = this.url + "," + trimmedResultPart.trim();
                }
            } else {
                //System.out.println("Starts not with c: " + trimmedResultPart);
            	this.url = this.url + "," + trimmedResultPart.trim();
            }   
	    }   

	    private void parseResultSet(ResultSet rs, boolean extendedNewHadoop) throws SQLException {
            this.url = rs.getString("Url");
            this.urlOrig = rs.getString("UrlOrig");
            this.Cext3= rs.getTimestamp("extWDate");
            this.Cext3Orig = rs.getString("extWDateOrig");
            this.Cext1 = rs.getLong("extSize");
            this.Cext2 =rs.getLong("extDblChar");
            this.C1a = rs.getString("C1a");
            this.C2a = rs.getString("C2a");
            if (extendedNewHadoop) this.C2b = rs.getString("C2b");
            this.C3a = rs.getString("C3a");
            this.C3b = rs.getString("C3b");
            this.C3c = rs.getString("C3c");  
            this.C3d = rs.getString("C3d");
            this.C3e = rs.getString("C3e");
            this.C3f = rs.getString("C3f");
            if (extendedNewHadoop) this.C3g = rs.getString("C3g");
            this.C4a = rs.getString("C4a");
            this.C5a = rs.getString("C5a");
            this.C5b = rs.getString("C5b");
            this.C6a = rs.getString("C6a");
            this.C6b = rs.getString("C6b");
            this.C6c = rs.getString("C6c");
            if (extendedNewHadoop) this.C6d = rs.getString("C6d");
            this.C7a = rs.getString("C7a");
            this.C7b = rs.getString("C7b");
            this.C7c = rs.getString("C7c");
            this.C7d = rs.getString("C7d");
            this.C7e = rs.getString("C7e");
            this.C7f = rs.getString("C7f");
            if (extendedNewHadoop) this.C7g = rs.getString("C7g");
            if (extendedNewHadoop) this.C7h = rs.getString("C7h");
            this.C8a = rs.getString("C8a");
            this.C8b = rs.getString("C8b");
            if (extendedNewHadoop) this.C8c = rs.getString("C8c");
            this.C9a = rs.getString("C9a");
            this.C9b = rs.getString("C9b");
            this.C9c = rs.getString("C9c");
            this.C9d = rs.getString("C9d");
            if (extendedNewHadoop) this.C9e = rs.getString("C9e");
            if (extendedNewHadoop) this.C9f = rs.getString("C9f");
            this.C10a = rs.getString("C10a");
            this.C10b = rs.getString("C10b");
            if (extendedNewHadoop) this.C10c = rs.getString("C10c");
            this.C15a = rs.getString("C15a");
            this.C15b = rs.getString("C15b");
            this.C16a = rs.getString("C16a");
            this.C17a = rs.getString("C17a");
            this.C18a = rs.getString("C18a");
            this.intDanish = rs.getFloat("intDanish");
            this.IsIASource = rs.getBoolean("IsIASource");
            this.calcDanishCode = rs.getInt("calcDanishCode");
	    }   
	    
	    public String getValuesInString(String row_delim, String keyval_delim) {
	    	//EXCEPT Url and date!!
	    	String s = "";
	    	s = s + "extSize" + keyval_delim + keyval_delim + this.Cext1; //3
	    	s = s + row_delim + "extDblChar" + keyval_delim + this.Cext2; //4
	    	s = s + row_delim + "C1a" + keyval_delim + (this.C1a!=null?this.C1a.replace(row_delim, ","):""); //5
	    	s = s + row_delim + "C2a" + keyval_delim + (this.C2a!=null?this.C2a.replace(row_delim, ","):""); //6
	    	s = s + row_delim + "C2b" + keyval_delim + (this.C2b!=null?this.C2b.replace(row_delim, ","):""); //7
	    	s = s + row_delim + "C3a" + keyval_delim + (this.C3a!=null?this.C3a.replace(row_delim, ","):""); //8
	    	s = s + row_delim + "C3b" + keyval_delim + (this.C3b!=null?this.C3b.replace(row_delim, ","):""); //9
	    	s = s + row_delim + "C3c" + keyval_delim + (this.C3c!=null?this.C3c.replace(row_delim, ","):""); //10
	    	s = s + row_delim + "C3d" + keyval_delim + (this.C3d!=null?this.C3d.replace(row_delim, ","):""); //11
	    	s = s + row_delim + "C3e" + keyval_delim + (this.C3e!=null?this.C3e.replace(row_delim, ","):""); //12
	    	s = s + row_delim + "C3f" + keyval_delim + (this.C3f!=null?this.C3f.replace(row_delim, ","):""); //13
	    	s = s + row_delim + "C3g" + keyval_delim + (this.C3g!=null?this.C3g.replace(row_delim, ","):""); //14
	    	s = s + row_delim + "C4a" + keyval_delim + (this.C4a!=null?this.C4a.replace(row_delim, ","):""); //15
	    	s = s + row_delim + "C5a" + keyval_delim + (this.C5a!=null?this.C5a.replace(row_delim, ","):""); //16
	    	s = s + row_delim + "C5b" + keyval_delim + (this.C5b!=null?this.C5b.replace(row_delim, ","):""); //17
	    	s = s + row_delim + "C6a" + keyval_delim + (this.C6a!=null?this.C6a.replace(row_delim, ","):""); //18
	    	s = s + row_delim + "C6b" + keyval_delim + (this.C6b!=null?this.C6b.replace(row_delim, ","):""); //19
	    	s = s + row_delim + "C6c" + keyval_delim + (this.C6c!=null?this.C6c.replace(row_delim, ","):""); //20
	    	s = s + row_delim + "C6d" + keyval_delim + (this.C6d!=null?this.C6d.replace(row_delim, ","):""); //21
	    	s = s + row_delim + "C7a" + keyval_delim + (this.C7a!=null?this.C7a.replace(row_delim, ","):""); //22
	    	s = s + row_delim + "C7b" + keyval_delim + (this.C7b!=null?this.C7b.replace(row_delim, ","):""); //23
	    	s = s + row_delim + "C7c" + keyval_delim + (this.C7c!=null?this.C7c.replace(row_delim, ","):""); //24
	    	s = s + row_delim + "C7d" + keyval_delim + (this.C7d!=null?this.C7d.replace(row_delim, ","):""); //25
	    	s = s + row_delim + "C7e" + keyval_delim + (this.C7e!=null?this.C7e.replace(row_delim, ","):""); //26
	    	s = s + row_delim + "C7f" + keyval_delim + (this.C7f!=null?this.C7f.replace(row_delim, ","):""); //27
	    	s = s + row_delim + "C7g" + keyval_delim + (this.C7g!=null?this.C7g.replace(row_delim, ","):""); //28
	    	s = s + row_delim + "C7h" + keyval_delim + (this.C7h!=null?this.C7h.replace(row_delim, ","):""); //29
	    	s = s + row_delim + "C8a" + keyval_delim + (this.C8a!=null?this.C8a.replace(row_delim, ","):""); //30
	    	s = s + row_delim + "C8b" + keyval_delim + (this.C8b!=null?this.C8b.replace(row_delim, ","):""); //31
	    	s = s + row_delim + "C8c" + keyval_delim + (this.C8c!=null?this.C8c.replace(row_delim, ","):""); //32
	    	s = s + row_delim + "C9a" + keyval_delim + (this.C9a!=null?this.C9a.replace(row_delim, ","):""); //33
	    	s = s + row_delim + "C9b" + keyval_delim + (this.C9b!=null?this.C9b.replace(row_delim, ","):""); //34
	    	s = s + row_delim + "C9c" + keyval_delim + (this.C9c!=null?this.C9c.replace(row_delim, ","):""); //35
	    	s = s + row_delim + "C9d" + keyval_delim + (this.C9d!=null?this.C9d.replace(row_delim, ","):""); //36
	    	s = s + row_delim + "C9e" + keyval_delim + (this.C9e!=null?this.C9e.replace(row_delim, ","):""); //37
	    	s = s + row_delim + "C9f" + keyval_delim + (this.C9f!=null?this.C9f.replace(row_delim, ","):""); //38
	    	s = s + row_delim + "C10a" + keyval_delim + (this.C10a!=null?this.C10a.replace(row_delim, ","):""); //39
	    	s = s + row_delim + "C10b" + keyval_delim + (this.C10b!=null?this.C10b.replace(row_delim, ","):""); //40
	    	s = s + row_delim + "C10c" + keyval_delim + (this.C10c!=null?this.C10c.replace(row_delim, ","):""); //41
	    	s = s + row_delim + "C15a" + keyval_delim + (this.C15a!=null?this.C15a.replace(row_delim, ","):""); //42
	    	s = s + row_delim + "C15b" + keyval_delim + (this.C15b!=null?this.C15b.replace(row_delim, ","):""); //43
	    	s = s + row_delim + "C16a" + keyval_delim + (this.C16a!=null?this.C16a.replace(row_delim, ","):""); //44
	    	s = s + row_delim + "C17a" + keyval_delim + (this.C17a!=null?this.C17a.replace(row_delim, ","):""); //45
	    	s = s + row_delim + "C18a" + keyval_delim + (this.C18a!=null?this.C18a.replace(row_delim, ","):""); //46
	    	s = s + row_delim + "intDanish" + keyval_delim + this.intDanish; //47
	    	s = s + row_delim + "IsIASource" + keyval_delim + this.IsIASource; //48
	    	s = s + row_delim + "calcDanishCode" + keyval_delim + this.calcDanishCode; //49
	    	return s;
	    }   
    }
    
    public static Set<String> getTables(Connection conn) throws SQLException {
	    Set<String> tableSet = new HashSet<String>();
	    String selectSQL = "SHOW TABLES";
	    PreparedStatement s = conn.prepareStatement(selectSQL); 
	    ResultSet rs = s.executeQuery();
	    while (rs.next()) {
	    	tableSet.add( rs.getString(1).trim() );
	    }
	    s.close();
    	return tableSet;
    }
    
	public static Set<String> getFields(Connection conn, String table) throws SQLException {
        Set<String> fieldsSet = new HashSet<String>();

	    String selectSQL = "SHOW FIELDS FROM " + table;
	    PreparedStatement s = conn.prepareStatement(selectSQL); 
	    ResultSet rs = s.executeQuery();
	    while (rs.next()) {
	    	fieldsSet.add(rs.getString("Field").trim());
	    }
	    rs.close();
	    s.close();
    	return fieldsSet;
	}

    
    public enum CodesFraction{
    	codes_all,
    	codes_positive,
    	codes_negative,
    	codes_nonpositive,
    	codes_nonnegative
    }

    public static class LookedupBerkSet {
    	public int level = 0;
    	public Map<String, LookedUpBerk> luBerkMap = new HashMap<String, LookedUpBerk>();
    }
    
    public static long findCntDomains(Connection conn, String table) throws SQLException {
    	long cnt = 0;
    	String selectSQL = "SELECT count(*) as cnt FROM " + table; 
	    PreparedStatement s = conn.prepareStatement(selectSQL); 
	    ResultSet rs = s.executeQuery();
	    if (rs.next()) {
	    	cnt = rs.getLong("cnt");
	    }
	    s.close();
    	return cnt;
    }
    
    

    public static long findSumDomains(Connection conn, String table, String field) throws SQLException {
    	long sumfield = 0;
    	String selectSQL = "SELECT sum(" + field + ") as sumfield FROM " + table; 
	    PreparedStatement s = conn.prepareStatement(selectSQL); 
	    ResultSet rs = s.executeQuery();
	    if (rs.next()) {
	    	sumfield = rs.getLong("sumfield");
	    }
	    s.close();
    	return sumfield;
    }

    public static long findSumDomains(Connection conn, String table, String field, String where) throws SQLException {
    	long sumfield = 0;
		String selectSQL = "SELECT sum(" + field + ") as sumfield FROM " + table + (where.isEmpty()? "": " WHERE " + where);
	    PreparedStatement s = conn.prepareStatement(selectSQL); 
	    ResultSet rs = s.executeQuery();
	    if (rs.next()) {
	    	sumfield = rs.getLong("sumfield");
	    }
	    s.close();
    	return sumfield;
    }

    public static long findCntSizeDomains(Connection conn, String table) throws SQLException {
    	long cnt = 0;
    	String selectSQL = "SELECT sum(extSize) as cntSize FROM " + table; 
	    PreparedStatement s = conn.prepareStatement(selectSQL); 
	    ResultSet rs = s.executeQuery();
	    if (rs.next()) {
	    	cnt = rs.getLong("cntSize");
	    }
	    s.close();
    	return cnt;
    }

    public static long findCntLevelDomains(Connection conn, int level, String wherestmt)  throws SQLException {
    	long cnt = 0;
    	String selectSQL = "SELECT count(*) as cnt  FROM " + domainTableName(level) + (wherestmt.isEmpty() ? "" : " WHERE " + wherestmt); 
        System.out.println("sql:  " + selectSQL);
    	
	    PreparedStatement s = conn.prepareStatement(selectSQL); 
	    ResultSet rs = s.executeQuery();
	    if (rs.next()) {
	    	cnt = rs.getLong("cnt");
	    }
	    s.close();
    	return cnt;
    }

    public static class DomNewSet {
    	public int level = 0;
    	public DataSource src = DataSource.source_none;
    	public int code = 0;
    	public Set<Integer> codeSet  = new HashSet<Integer>();
    	public String table  = "";
    	public Map<String,DomainNewLevel> domMap = new HashMap<String,DomainNewLevel>();
    	
        public DomNewSet () {
        	this.level = 0;
        	this.src = DataSource.source_none;
        	this.code = 0;
        	this.codeSet  = new HashSet<Integer>();
        	this.table  = "";
        	this.domMap = new HashMap<String,DomainNewLevel>();     
        }

        public DomNewSet (int lv, Source sr, int cd, String t) {
        	this.level = lv;
        	this.src = (sr.equals(Source.NAS) ? DataSource.source_NAS :DataSource.source_IA );
        	this.code = cd;
        	this.codeSet = new HashSet<Integer>();
        	this.table  = t;
        	this.domMap = new HashMap<String,DomainNewLevel>();        	
        }
    }
    
    public static DomNewSet readDomNewSet(Connection conn, int level, Source src, int code, boolean nye, String where) throws SQLException {
    	DomNewSet domSet = new DomNewSet();
    	domSet.level = level;
    	domSet.src = (src.equals(Source.NAS) ? DataSource.source_NAS :DataSource.source_IA );
    	domSet.code = code;
    	domSet.codeSet = new HashSet<Integer>();
    	if (nye) domSet.table = domainNyeTableName(level, src, code); 
    	else domSet.table = domainNewTableName(level, src, code); 
    	String selectSQL = "SELECT * FROM " + domSet.table; 
    	if (!where.isEmpty()) selectSQL = selectSQL + " WHERE " + where; //  if (op.==berkeley)) selectSQL = selectSQL + " WHERE LookedUpInBerkeley=false";
	    PreparedStatement s = conn.prepareStatement(selectSQL); 
	    ResultSet rs = s.executeQuery();
	    while (rs.next()) {
	    	DomainNewLevel dl = new DomainNewLevel(rs);
	    	domSet.domMap.put(dl.KeyDomain,dl);
	    }
	    rs.close();
	    s.close();
    	return domSet;
    }

    public static DomNewSet readDomNewSet(Connection conn, int level, Source src, int code, boolean nye) throws SQLException {
    	return readDomNewSet(conn, level, src, code, nye, "") ;
    }

    public static DomNewSet readDomNewSet(Connection conn, String tablename, String where) throws SQLException {
    	System.out.println("tablename: " + tablename);
    	DomNewSet domSet = new DomNewSet();
    	domSet.table = tablename;
		if (tablename.contains("_" + Source.IA.name())) domSet.src = DataSource.source_IA;
		else if (tablename.contains("_" + Source.NAS.name())) domSet.src = DataSource.source_NAS;
		else domSet.src = DataSource.source_none;
		
		int pos = tablename.indexOf("_" + domaintable_level_infix); //parts[0].indexOf(wf_maschine_prefix);
		String str = tablename.substring(pos+2, pos+3); //parts[0].substring(pos+1);
    	//System.out.println("level: " + str);
		domSet.level = Integer.parseInt(str); 
		
		pos = tablename.indexOf("_" + domaintable_code_infix);
		if (pos>0) {
			str = tablename.substring(pos+2); 
	    	//System.out.println("code: " + str);
			domSet.code = Integer.parseInt(str);
		} else {
			domSet.code = 0;
		}

    	String selectSQL = "SELECT * FROM " + tablename; 
    	if (!where.isEmpty()) selectSQL = selectSQL + " WHERE " + where; //  if (op.==berkeley)) selectSQL = selectSQL + " WHERE LookedUpInBerkeley=false";
    	//System.out.println("selectSQL: " + selectSQL);
    	
	    PreparedStatement s = conn.prepareStatement(selectSQL); 
	    ResultSet rs = s.executeQuery();
	    while (rs.next()) {
	    	DomainNewLevel dl = new DomainNewLevel(rs);
	    	domSet.domMap.put(dl.KeyDomain,dl);
	    }
	    rs.close();
	    s.close();
    	return domSet;
    }

    public static DomNewSet readDomNewSet(Connection conn, String tablename) throws SQLException {
    	return readDomNewSet(conn, tablename, "") ;
    }

    public static class DomSet {
    	public int level = 0;
    	public  Set<DomainLevel> domSet = new HashSet<DomainLevel>();
    }
    
    public static DomSet readDomSet(Connection conn, int level) throws SQLException {
    	return readDomSet(conn, level, "");
    }
    
    public static DomSet readDomSet(Connection conn, int level, String where) throws SQLException {
    	DomSet domSet = new DomSet();
    	domSet.level = level;
    	String selectSQL = "SELECT * FROM " + domainTableName(level, ""); 
    	if (!where.trim().isEmpty()) {
    		selectSQL = selectSQL + " WHERE " + where;
    	} 
	    PreparedStatement s = conn.prepareStatement(selectSQL); 
	    ResultSet rs = s.executeQuery();
	    while (rs.next()) {
	    	DomainLevel dl = new DomainLevel(rs);
	    	domSet.domSet.add(dl);
	    }
    	return domSet;
    }


    public static LookedupBerkSet readLookedupBerkSet(Connection conn, int level) throws SQLException {
    	LookedupBerkSet luBerkSet = new LookedupBerkSet();
    	luBerkSet.level = level;
    	String selectSQL = "SELECT * FROM " + MysqlRes.domainTableName(level,domaintable_lookedupberk_all_suffix); 
	    PreparedStatement s = conn.prepareStatement(selectSQL); 
	    ResultSet rs = s.executeQuery();
	    while (rs.next()) {
	    	LookedUpBerk luBerk = new LookedUpBerk(rs);
	    	luBerkSet.luBerkMap.put(luBerk.KeyDomain, luBerk);
	    }
    	return luBerkSet;
    }

    public static LookedupBerkSet readLookedupBerkSetFromDonmainLev(Connection conn, int level, boolean nye, String wherestmt) throws SQLException {
    	String tablename = "";
    	LookedupBerkSet luBerkSet = new LookedupBerkSet();
    	luBerkSet.level = level;

    	if (nye) tablename = MysqlRes.domainNyeTableName(level);
    	else tablename = MysqlRes.domainNewTableName(level);
    	
    	String selectSQL = "SELECT * FROM " + tablename + (wherestmt.isEmpty() ? "" : " WHERE " + wherestmt); 
	    PreparedStatement s = conn.prepareStatement(selectSQL); 
	    ResultSet rs = s.executeQuery();
	    while (rs.next()) {
	    	LookedUpBerk luBerk = new LookedUpBerk(rs);
	    	luBerkSet.luBerkMap.put(luBerk.KeyDomain, luBerk);
	    }
    	return luBerkSet;
    }

    public static LookedupBerkSet readLookedupBerkSetFromDonmainLev(Connection conn, int level, String tablename, String wherestmt) throws SQLException {
    	LookedupBerkSet luBerkSet = new LookedupBerkSet();
    	luBerkSet.level = level;
    	String selectSQL = "SELECT * FROM " + tablename + (wherestmt.isEmpty() ? "" : " WHERE " + wherestmt); 
        System.out.println("read luBerk sql " + selectSQL);
	    PreparedStatement s = conn.prepareStatement(selectSQL); 
	    ResultSet rs = s.executeQuery();
	    int i = 0;
	    while (rs.next()) {
	    	i++;
	    	LookedUpBerk luBerk = new LookedUpBerk(rs);
	    	luBerkSet.luBerkMap.put(luBerk.KeyDomain, luBerk);
	    }
        System.out.println("read luBerk cnt " + i);
	    
    	return luBerkSet;
    }

    public static void clearTable(Connection conn, String table) throws SQLException {
    	String selectSQL = "DELETE FROM " + table;
	    PreparedStatement su = conn.prepareStatement(selectSQL);
	    su.executeUpdate();
        su.close();
    }

    public static void UpdateWithLookedUpDomSet(Connection conn, String fromTable, String toTable) throws SQLException {
	    String selectSQL =  "INSERT INTO " + toTable 
	    		+ " ( SELECT Domain, CntInBerkeley FROM " + fromTable + " WHERE LookedUpInBerkeley=1 )";
	    PreparedStatement su2 = conn.prepareStatement(selectSQL); 
	    su2.executeUpdate();
        su2.close();
    }

    public static void InsertLookedUpDom(Connection conn, String table, LookedUpBerk db) throws SQLException {
    	PreparedStatement s = conn.prepareStatement( "INSERT INTO " + table + " ( Domain, CntInBerkeley ) VALUES ( ?,? )" );
	    int index = 1;
	    s.setString(index, db.Domain);
	    index++;
	    s.setInt(index, db.CntInBerkeley);
    	s.executeUpdate();
        s.close();
    }


    public static boolean anyMissingLookedUpBerk(Connection conn, int level) throws SQLException {
        Set<String> allTableSet = MysqlRes.getTables(conn);
        String t = domainTableName(level) ;
        boolean found = false;
        if (allTableSet.contains(t)) {
	    	String selectSQL = "SELECT * FROM " + domainTableName(level) + " WHERE LookedUpInBerkeley = false"; 
		    PreparedStatement s = conn.prepareStatement(selectSQL); 
		    ResultSet rs = s.executeQuery();
		    found = rs.next(); //there are records with LookedUpInBerkeley = false, i.e. they miss lookup
		    rs.close();
        } else {
        	found = false;
        }
    	return !found;
    }

    public static Set<DomainLevel> readDomainLevel(Connection conn, String table) throws SQLException {
    	Set<DomainLevel> dbSet = new HashSet<DomainLevel>();
    	String selectSQL = "SELECT * FROM " + table; 
	    PreparedStatement s = conn.prepareStatement(selectSQL); 
	    ResultSet rs = s.executeQuery();

	    while (rs.next()) {
	    	DomainLevel db = new DomainLevel(rs);
	    	dbSet.add(db);
	    }
	    rs.close();
    	return dbSet;
    }
    
    public static Set<DomainLevel> readDomainLevel(Connection conn, String table, String where) throws SQLException {
    	Set<DomainLevel> dbSet = new HashSet<DomainLevel>();
    	String selectSQL = "SELECT * FROM " + table + " WHERE " + where; 
	    PreparedStatement s = conn.prepareStatement(selectSQL); 
	    ResultSet rs = s.executeQuery();

	    while (rs.next()) {
	    	DomainLevel db = new DomainLevel(rs);
	    	dbSet.add(db);
	    }
	    rs.close();
    	return dbSet;
    }
    
    public static Set<LookedUpBerk> readLookedUpBerk(Connection conn, String table) throws SQLException {
    	Set<LookedUpBerk> dbSet = new HashSet<LookedUpBerk>();
    	String selectSQL = "SELECT * FROM " + table; 
	    PreparedStatement s = conn.prepareStatement(selectSQL); 
	    ResultSet rs = s.executeQuery();

	    while (rs.next()) {
	    	LookedUpBerk db = new LookedUpBerk(rs);
	    	dbSet.add(db);
	    }
	    rs.close();
    	return dbSet;
    }
    
    public static String domainTableName(int level, String suffix) {
    	return domainTableName(level) + suffix;
    }
    
    public static String domainTableName(int level) {
    	return domaintable_prefix + domaintable_level_infix + level;
    }

    public static String domainNyeTableName(int level) {
    	return domaintable_prefix + domaintable_level_infix + level + "_NYE";
    }

    public static String domainNewTableName(int level) {
    	return domaintable_prefix + domaintable_level_infix + level + "_NEW";
    }

    public static String domainNewTableName(int level, Source src) {
    	return domaintable_prefix + domaintable_level_infix + level + "_" + src.name();
    }

    public static String domainNewTableName(int level, Source src, int code) {
    	return domaintable_prefix + domaintable_level_infix + level + "_" + src.name() +  "_C" + code;
    }

    public static String domainNyeTableName(int level, Source src) {
    	return domaintable_prefix + domaintable_level_infix + level + "_NNYYEE_" + src.name();
    }

    public static String domainNyeTableName(int level, Source src, int code) {
    	return domaintable_prefix + domaintable_level_infix + level + "_NNYYEE_" + src.name() +  "_C" + code;
    }

    public static String domainNyeTableName(int level, int code) {
    	return domaintable_prefix + domaintable_level_infix + level + "_NNYYEE_C" + code;
    }

    public static Set<Integer> getCodeSet (Connection conn, String tablename, CodesFraction frac) throws SQLException {
    	Set<Integer> codeSet = new HashSet<Integer>();
	    String selectSQL = "SELECT DISTINCT calcDanishCode FROM "+ tablename;
	    
	    switch (frac) {
	    	case codes_positive: 
	    		selectSQL = selectSQL + " WHERE calcDanishCode>0 ";
	    		break;
	    	case codes_negative: 
	    		selectSQL = selectSQL + " WHERE calcDanishCode<0 "; 
	    		break;
	    	case codes_nonpositive: 
	    		selectSQL = selectSQL + " WHERE calcDanishCode<=0 "; 
	    		break;
	    	case codes_nonnegative: 
	    		selectSQL = selectSQL + " WHERE calcDanishCode>=0 ";
	    		break;
	    	case codes_all: //ignore 
	    }
	    
	    PreparedStatement t = conn.prepareStatement(selectSQL); 
	    ResultSet trs = t.executeQuery();
		while (trs.next()) {
			codeSet.add(trs.getInt("calcDanishCode"));
		}
		trs.close();
	    t.close();
		return codeSet;
    }

    public static class RsRes {
        public ResultSet rs;
        public int rowCount;
        public RsRes(ResultSet prs, int prowCount) {
        	rs = prs;
        	rowCount = prowCount;
        }
        public RsRes() {
        	rowCount = 0;
        }
    }

    public static int urlInBerkley(String url, LinksBase linksBase) throws SQLException {
        Long freq = null;
        try {
            freq = linksBase.getFrequency(url);
        } catch (Exception e) {
            throw new SQLException(
                    "Exception thrown during call to Berkeley DB linksBase", e);
        }
        if (freq == null) {
            return 0;
        } else {
            return freq.intValue();
        }
    }

    
    /*public static String convertCodesToString(Set<String> codes) {
    	String res = "";
    	if (codes.size()>0) {
	    	List<String> sortedCodes = new ArrayList<String>(codes.size());
	    	sortedCodes.addAll(codes);
	        Collections.sort(sortedCodes);
	        res = MysqlX.getStringSequence(sortedCodes, ",");
        }
	    return res;
    }*/
    
    public static String convertCodesToString(Set<Integer> codes) {
    	String res = "";
    	if (codes.size()>0) {
	    	List<Integer> sortedCodes = new ArrayList<Integer>(codes.size());
	    	sortedCodes.addAll(codes);
	        Collections.sort(sortedCodes);
	        res = MysqlX.getIntegerSequence(sortedCodes, ",");
        }
	    return res;
    }
    
    public static boolean updateDomainLevel(Connection conn, String tablename, DomainLevel r) throws SQLException {
    	String sql = "UPDATE " + tablename + " SET " 
					+ " CntInBerkeley = ?, "
					+ " LookedUpInBerkeley = ?, "
					+ " calcCodes = ? "
					+ "WHERE Domain = ?"; 
	    PreparedStatement su = conn.prepareStatement(sql);
	    int index = 1;
	    su.setInt(index, r.CntInBerkeley);
	    index++;
	    su.setBoolean(index, r.LookedUpInBerkeley);
	    index++;
	    su.setString(index, convertCodesToString(r.calcCodes));
	    index++;
	    su.setString(index, r.Domain);
	    su.executeUpdate();
        su.close();
        return true;
    }

    public static boolean updateNewDomainLevel(Connection conn, String tablename, DomainNewLevel dom) throws SQLException {
		String sql = "UPDATE " + tablename + " SET " 
					+ " CntInIaData = ?, "
					+ " CntInNasData = ?, "
					+ " CntInBerkeley = ?, "
					+ " LookedUpInBerkeley = ?, "
					+ " OnlyCaseSensitive = ?, "
					+ " CalcCode = ?, "
					+ " CalcCodes = ? "
					+ " WHERE Domain = ?"; 
	    PreparedStatement su = conn.prepareStatement(sql);
	    int index = 1;
	    su.setLong(index, dom.CntInIaData);
	    index++;
	    su.setLong(index, dom.CntInNasData);
	    index++;
	    su.setLong(index, dom.CntInBerkeley);
	    index++;
	    su.setBoolean(index, dom.LookedUpInBerkeley);
	    index++;
	    su.setBoolean(index, dom.OnlyCaseSensitive);
	    index++;
	    su.setInt(index, dom.CalcCode);
	    index++;
	    su.setString(index, convertCodesToString(dom.CalcCodes));
	    index++;
	    su.setString(index, dom.Domain);
	    su.executeUpdate();
        su.close();
        return true;
    }

    public static boolean updateNewDomainLevelLookupInf(Connection conn, String tablename, DomainNewLevel dom) throws SQLException {
		String sql = "UPDATE " + tablename + " SET " 
					+ " CntInBerkeley = ?, "
					+ " LookedUpInBerkeley = ?, "
					+ " OnlyCaseSensitive = ? "
					+ " WHERE Domain = ?"; 
	    PreparedStatement su = conn.prepareStatement(sql);
	    int index = 1;
	    su.setLong(index, dom.CntInBerkeley);
	    index++;
	    su.setBoolean(index, dom.LookedUpInBerkeley);
	    index++;
	    su.setBoolean(index, dom.OnlyCaseSensitive);
	    index++;
	    su.setString(index, dom.Domain);
	    su.executeUpdate();
        su.close();
        return true;
    }

    public static boolean insertDomainNewLevel(Connection conn, String tablename, DomainNewLevel dom) throws SQLException {
    	//ignore if already there
	    PreparedStatement s = conn.prepareStatement(
	    		"INSERT INTO " + tablename + " ("
	        			+ "Domain, Level, CntInIaData, "
	        			+ "CntInNasData, CntInBerkeley, LookedUpInBerkeley, OnlyCaseSensitive, "
	        			+ "CalcCode, CalcCodes "
	        			+ ") VALUES ("
	        			+ "?,?,?,?,"
	        			+ "?,?,?,"
	        			+ "?,?"
	        			+ ")"
	    		);
	    
	    int index = 1;
	    String domStr = "";
	    if( dom.Domain.length()>=900) {
	    	System.err.println("*** Domain> 900 chars - TRUNCATED " + dom.Domain );
	    	System.out.println("*** Domain> 900 chars - TRUNCATED " + dom.Domain );
	    	domStr = dom.Domain.substring(0, 900);
	    } else {
	    	domStr = dom.Domain;
	    }
	    s.setString(index, domStr);
	    index++;
	    s.setInt(index, dom.Level);
	    index++;
	    s.setLong(index, dom.CntInIaData);
	    index++;
	    s.setLong(index, dom.CntInNasData);
	    index++;
	    s.setLong(index, dom.CntInBerkeley);
	    index++;
	    s.setBoolean(index, dom.LookedUpInBerkeley);
	    index++;
	    s.setBoolean(index, dom.OnlyCaseSensitive);
	    index++;
	    s.setInt(index, dom.CalcCode);
	    index++;
	    s.setString(index, convertCodesToString(dom.CalcCodes));
    	s.executeUpdate();
    	s.close();
	    return true;
    }
    
    public static boolean insertDomainLevel(Connection conn, String tablename, DomainLevel dom) throws SQLException {
    	//ignore if already there
	    PreparedStatement s = conn.prepareStatement(
	    		"INSERT INTO " + tablename + " ("
	        			+ "Domain, InIaData, InNasData, calcCodes, CntInBerkeley, LookedUpInBerkeley "
	        			+ ") VALUES ("
	        			+ "?,?,?,?,?,?"
	        			+ ")"
	    		);
	    int index = 1;
	    String domStr = "";
	    if( dom.Domain.length()>=900) {
	    	System.err.println("*** Domain> 900 chars - TRUNCATED " + dom.Domain );
	    	System.out.println("*** Domain> 900 chars - TRUNCATED " + dom.Domain );
	    	domStr = dom.Domain.substring(0, 900);
	    } else {
	    	domStr = dom.Domain;
	    }
	    s.setString(index, domStr);
	    index++;
	    s.setBoolean(index, dom.InIaData);
	    index++;
	    s.setBoolean(index, dom.InNasData);
	    index++;
	    s.setString(index, convertCodesToString(dom.calcCodes));
	    index++;
	    s.setInt(index, dom.CntInBerkeley);
	    index++;
	    s.setBoolean(index, dom.LookedUpInBerkeley);
    	try {
    	    s.executeUpdate();
        } catch (Exception e) { }    	
	    return true;
    }
    
    public static class AddStat { // per sorce per machine
    	DataSource src; 
    	String dbmachine = "";
    	Set<String> tableSet = new HashSet<String>();
		public long CntDomains = 0;
		public long CntSize = 0;
		public long CntLikelydanishDomains = 0;
		public long CntLevel_L1_Domains = 0;
		public long CntLevel_L2_Domains = 0;
		public long CntLevel_L3_Domains = 0;
		public long CntLevel_L1_LookedupDomains = 0;
		public long CntLevel_L2_LookedupDomains = 0;
		public long CntLevel_L3_LookedupDomains = 0;
   
		public AddStat(DataSource psrc, String pdbmachine)  {
            this.src = psrc;
            this.dbmachine = pdbmachine;
        }
    }

    public static class LookedUpBerk {
		public String Domain = "";
		public String KeyDomain = "";
		public int CntInBerkeley = 0;
   
		public LookedUpBerk()  {
            this.Domain = "";
            this.KeyDomain = "";
            this.CntInBerkeley = 0;
        }

		public LookedUpBerk(ResultSet rs) throws SQLException {
            this.Domain = rs.getString("Domain");
            this.KeyDomain = this.Domain.toLowerCase().trim();
            if (this.KeyDomain.length()>=900) this.KeyDomain = this.KeyDomain.substring(0, 900);
            this.CntInBerkeley = rs.getInt("CntInBerkeley");
        }
		
		public String sql_insertpart()  {
			String s = "";
			s = "('" + this.Domain.replace("'", "''") + "'," + this.CntInBerkeley + ")";
            this.Domain = "";
            this.KeyDomain = "";
            this.CntInBerkeley = 0;
            return s;
        }
    }

    public static class DomainLevel {
		public String Domain = "";
		public String KeyDomain = "";
		public boolean InIaData = false;
		public boolean InNasData = false;
		public int CntInBerkeley = 0;
		public boolean LookedUpInBerkeley = false;
		public Set<Integer> calcCodes = new HashSet<Integer>();
   
		public DomainLevel()  {
            this.Domain = "";
            this.KeyDomain = "";
            this.InIaData = false;
            this.InNasData = false;
            this.CntInBerkeley = 0;
            this.LookedUpInBerkeley = false;
            this.calcCodes = new HashSet<Integer>();
        }

		public DomainLevel(String d, Source src)  {
            this.Domain = d;
            this.KeyDomain = this.Domain.toLowerCase().trim();
            if (this.KeyDomain.length()>=900) this.KeyDomain = this.KeyDomain.substring(0, 900);
            this.InIaData = src.equals(Source.IA);
            this.InNasData = src.equals(Source.NAS);
            this.CntInBerkeley = 0;
            this.LookedUpInBerkeley = false;
            this.calcCodes = new HashSet<Integer>();
        }

		public DomainLevel(ResultSet rs) throws SQLException {
            this.Domain = rs.getString("Domain");
            this.KeyDomain = this.Domain.toLowerCase().trim();
            if (this.KeyDomain.length()>=900) this.KeyDomain = this.KeyDomain.substring(0, 900);
            this.InIaData = rs.getBoolean("InIaData");
            this.InNasData = rs.getBoolean("InNasData");
            this.CntInBerkeley = rs.getInt("CntInBerkeley");
            this.LookedUpInBerkeley = rs.getBoolean("LookedUpInBerkeley");
            String s = rs.getString("calcCodes");
        	String[] codeParts = s.split(",");
        	for (String c: codeParts) 
        		if (!c.trim().isEmpty()) this.calcCodes.add(Integer.parseInt(c));
        }
    }

    public static class DomainNewLevel {
		public String Domain = "";
		public String KeyDomain = "";
		public int Level = 0; 
		public long CntInIaData = 0;
		public long CntInNasData = 0;
		public long CntInBerkeley = 0;
		public boolean LookedUpInBerkeley = false;
		public boolean OnlyCaseSensitive = false;
		public int CalcCode = 0; //only for intial findings
		public Set<Integer> CalcCodes = new HashSet<Integer>();
    	public boolean entryRead = false; //used internally - not in database

    	public void copy (DomainNewLevel dml) {
            this.Domain = dml.Domain;
            this.KeyDomain = this.Domain.toLowerCase().trim();
            if (this.KeyDomain.length()>=900) this.KeyDomain = this.KeyDomain.substring(0, 900);
            this.Level = dml.Level;
            this.CntInIaData = dml.CntInIaData;
            this.CntInNasData = dml.CntInNasData;
            this.CntInBerkeley = dml.CntInBerkeley;
            this.LookedUpInBerkeley = dml.LookedUpInBerkeley;
            this.OnlyCaseSensitive = dml.OnlyCaseSensitive;
            this.CalcCode = dml.CalcCode;
            this.CalcCodes = new HashSet<Integer>();
            for (int c :  dml.CalcCodes) {
            	this.CalcCodes.add(c);
            }
        	this.entryRead = dml.entryRead;
    	}
    	
    	public DomainNewLevel()  {
            this.Domain = "";
            this.KeyDomain = "";
            this.Level = 0;
            this.CntInIaData = 0;
            this.CntInNasData = 0;
            this.CntInBerkeley = 0;
            this.LookedUpInBerkeley = false;
            this.OnlyCaseSensitive = false;
            this.CalcCode = 0;
            this.CalcCodes = new HashSet<Integer>();
        	this.entryRead = false;
        }

    	public DomainNewLevel(int level)  {
            this.Domain = "";
            this.KeyDomain = "";
            this.Level = level;
            this.CntInIaData = 0;
            this.CntInNasData = 0;
            this.CntInBerkeley = 0;
            this.LookedUpInBerkeley = false;
            this.OnlyCaseSensitive = false;
            this.CalcCode = 0;
            this.CalcCodes = new HashSet<Integer>();
        	this.entryRead = false;
        }

    	public DomainNewLevel(int level, int code)  {
            this.Domain = "";
            this.KeyDomain = "";
            this.Level = level;
            this.CntInIaData = 0;
            this.CntInNasData = 0;
            this.CntInBerkeley = 0;
            this.LookedUpInBerkeley = false;
            this.OnlyCaseSensitive = false;
            this.CalcCode = code;
            this.CalcCodes = new HashSet<Integer>();
        	this.entryRead = false;
        }

		public DomainNewLevel(int level, String d, Source src, long cnt, int code)  {
            this.Domain = d;
            this.KeyDomain = this.Domain.toLowerCase().trim();
            if (this.KeyDomain.length()>=900) this.KeyDomain = this.KeyDomain.substring(0, 900);
            this.Level = level;
            this.CntInIaData = (src.equals(Source.IA) ? cnt : 0);
            this.CntInNasData = (src.equals(Source.NAS) ? cnt : 0);
            this.CntInBerkeley = 0;
            this.LookedUpInBerkeley = false;
            this.OnlyCaseSensitive = false;
            this.CalcCode = code;
            this.CalcCodes = new HashSet<Integer>();
        	this.entryRead = false;
        }

		public DomainNewLevel(int level, String d, DataSource src, long cnt, int code)  {
            this.Domain = d;
            this.KeyDomain = this.Domain.toLowerCase().trim();
            if (this.KeyDomain.length()>=900) this.KeyDomain = this.KeyDomain.substring(0, 900);
            this.Level = level;
            this.CntInIaData = (src.equals(DataSource.source_IA) ? cnt : 0);
            this.CntInNasData = (src.equals(DataSource.source_NAS) ? cnt : 0);
            this.CntInBerkeley = 0;
            this.LookedUpInBerkeley = false;
            this.OnlyCaseSensitive = false;
            this.CalcCode = code;
            this.CalcCodes = new HashSet<Integer>();
        	this.entryRead = false;
        }

		public DomainNewLevel(ResultSet rs) throws SQLException {
            this.Domain = rs.getString("Domain");
            this.KeyDomain = this.Domain.toLowerCase().trim();
            if (this.KeyDomain.length()>=900) this.KeyDomain = this.KeyDomain.substring(0, 900);
            this.Level = rs.getInt("Level");
            this.CntInIaData = rs.getLong("CntInIaData");
            this.CntInNasData = rs.getLong("CntInNasData");
            this.CntInBerkeley = rs.getLong("CntInBerkeley");
            this.LookedUpInBerkeley = rs.getBoolean("LookedUpInBerkeley");
            this.OnlyCaseSensitive = rs.getBoolean("OnlyCaseSensitive");
            this.CalcCode = rs.getInt("CalcCode");
            String s = rs.getString("CalcCodes");
        	String[] codeParts = s.trim().split(",");
        	//System.out.println("codeParts: " + s);        	
        	//System.out.println("codeParts: " + codeParts.length);        	
        	for (String c: codeParts) {
        		if (!c.isEmpty()) this.CalcCodes.add( Integer.parseInt(c));
        	}
        	this.entryRead = true;
        }

    }

    public static class DomainNyeLevel {
		public String Domain = "";
		public String KeyDomain = "";
		public int Level = 0; 
		public long CntInIaData = 0;
		public long CntInNasData = 0;
		public long CntInBerkeley = 0;
		public boolean LookedUpInBerkeley = false;
		public boolean FoundInNetarkivet = false;
		public boolean LookedUpInNetarkivet = false;
		public int CalcCode = 0; //only for intial findings
		public Set<Integer> CalcCodes = new HashSet<Integer>();
    	public boolean entryRead = false; //used internally - not in database

    	public void copy (DomainNyeLevel dml) {
            this.Domain = dml.Domain;
            this.KeyDomain = this.Domain.toLowerCase().trim();
            if (this.KeyDomain.length()>=900) this.KeyDomain = this.KeyDomain.substring(0, 900);
            this.Level = dml.Level;
            this.CntInIaData = dml.CntInIaData;
            this.CntInNasData = dml.CntInNasData;
            this.CntInBerkeley = dml.CntInBerkeley;
            this.LookedUpInBerkeley = dml.LookedUpInBerkeley;
            this.FoundInNetarkivet = dml.FoundInNetarkivet;
            this.LookedUpInNetarkivet = dml.LookedUpInNetarkivet;
            this.CalcCode = dml.CalcCode;
            this.CalcCodes = new HashSet<Integer>();
            for (int c :  dml.CalcCodes) {
            	this.CalcCodes.add(c);
            }
        	this.entryRead = dml.entryRead;
    	}
    	
    	public DomainNyeLevel()  {
            this.Domain = "";
            this.KeyDomain = "";
            this.Level = 0;
            this.CntInIaData = 0;
            this.CntInNasData = 0;
            this.CntInBerkeley = 0;
            this.LookedUpInBerkeley = false;
            this.FoundInNetarkivet = false;
            this.LookedUpInNetarkivet = false;
            this.CalcCode = 0;
            this.CalcCodes = new HashSet<Integer>();
        	this.entryRead = false;
        }

    	public DomainNyeLevel(int level)  {
            this.Domain = "";
            this.KeyDomain = "";
            this.Level = level;
            this.CntInIaData = 0;
            this.CntInNasData = 0;
            this.CntInBerkeley = 0;
            this.LookedUpInBerkeley = false;
            this.FoundInNetarkivet = false;
            this.LookedUpInNetarkivet = false;
            this.CalcCode = 0;
            this.CalcCodes = new HashSet<Integer>();
        	this.entryRead = false;
        }

    	public DomainNyeLevel(int level, int code)  {
            this.Domain = "";
            this.KeyDomain = "";
            this.Level = level;
            this.CntInIaData = 0;
            this.CntInNasData = 0;
            this.CntInBerkeley = 0;
            this.LookedUpInBerkeley = false;
            this.FoundInNetarkivet = false;
            this.LookedUpInNetarkivet = false;
            this.CalcCode = code;
            this.CalcCodes = new HashSet<Integer>();
        	this.entryRead = false;
        }

		public DomainNyeLevel(int level, String d, Source src, long cnt, int code)  {
            this.Domain = d;
            this.KeyDomain = this.Domain.toLowerCase().trim();
            if (this.KeyDomain.length()>=900) this.KeyDomain = this.KeyDomain.substring(0, 900);
            this.Level = level;
            this.CntInIaData = (src.equals(Source.IA) ? cnt : 0);
            this.CntInNasData = (src.equals(Source.NAS) ? cnt : 0);
            this.CntInBerkeley = 0;
            this.FoundInNetarkivet = false;
            this.LookedUpInNetarkivet = false;
            this.LookedUpInBerkeley = false;
            this.CalcCode = code;
            this.CalcCodes = new HashSet<Integer>();
        	this.entryRead = false;
        }

		public DomainNyeLevel(int level, String d, DataSource src, long cnt, int code)  {
            this.Domain = d;
            this.KeyDomain = this.Domain.toLowerCase().trim();
            if (this.KeyDomain.length()>=900) this.KeyDomain = this.KeyDomain.substring(0, 900);
            this.Level = level;
            this.CntInIaData = (src.equals(DataSource.source_IA) ? cnt : 0);
            this.CntInNasData = (src.equals(DataSource.source_NAS) ? cnt : 0);
            this.CntInBerkeley = 0;
            this.LookedUpInBerkeley = false;
            this.FoundInNetarkivet = false;
            this.LookedUpInNetarkivet = false;
            this.CalcCode = code;
            this.CalcCodes = new HashSet<Integer>();
        	this.entryRead = false;
        }

		public DomainNyeLevel(ResultSet rs) throws SQLException {
            this.Domain = rs.getString("Domain");
            this.KeyDomain = this.Domain.toLowerCase().trim();
            if (this.KeyDomain.length()>=900) this.KeyDomain = this.KeyDomain.substring(0, 900);
            this.Level = rs.getInt("Level");
            this.CntInIaData = rs.getLong("CntInIaData");
            this.CntInNasData = rs.getLong("CntInNasData");
            this.CntInBerkeley = rs.getLong("CntInBerkeley");
            this.LookedUpInBerkeley = rs.getBoolean("LookedUpInBerkeley");
            this.FoundInNetarkivet = rs.getBoolean("FoundInNetarkivet");
            this.LookedUpInNetarkivet = rs.getBoolean("LookedUpInNetarkivet");
            this.CalcCode = rs.getInt("CalcCode");
            String s = rs.getString("CalcCodes");
        	String[] codeParts = s.trim().split(",");
        	//System.out.println("codeParts: " + s);        	
        	//System.out.println("codeParts: " + codeParts.length);        	
        	for (String c: codeParts) {
        		if (!c.isEmpty()) this.CalcCodes.add( Integer.parseInt(c));
        	}
        	this.entryRead = true;
        }
    }
    
    public static java.sql.Timestamp findDateFromString(String dateString) {
    	java.sql.Timestamp t;
    	t = java.sql.Timestamp.valueOf(//yyyy-[m]m-[d]d hh:mm:ss 
    			dateString.substring(0, 4) + "-"
        		+ dateString.substring(4, 6) + "-"
        		+ dateString.substring(6, 8) + " "
        		+ dateString.substring(8, 10) + ":"
        		+ dateString.substring(10, 12) + ":"
        		+ dateString.substring(12, 14)
        ); 
    	return t;
    }

    public static boolean checkDateFromString(String dateString) {
        boolean res = true;
    	try {
        	findDateFromString(dateString);
        } catch (Exception e) {
        	res = false;
        }
    	return res;
    }

    public static Set<String> getUrlsForCalcCode(Connection conn, String tablenm, int code) throws SQLException {
    	return getUrlsForCalcCode(conn, tablenm, code, "");
    }
    
    public static Set<String> getUrlsForCalcCode(Connection conn, String tablenm, int code, String where) throws SQLException {
    	Set<String> urlSet = new HashSet<String>();
    	String sql = "SELECT url FROM " + tablenm + " WHERE (calcDanishCode=" + code + ")" + (where.isEmpty() ? "" : " AND (" + where  + ")") ; 
	    PreparedStatement s = conn.prepareStatement(sql); 
	    ResultSet rs = s.executeQuery();
	    while (rs.next()) {
	        String url = rs.getString("Url");
	        urlSet.add(url);
	    }
	    rs.close();
	    s.close();
	    return urlSet;
    }
    
	public static String domaintable_level_infix = "L";
	public static String domaintable_code_infix = "C";
	public static String domaintable_prefix = "DomainLev_";
    public static String domaintable_machine_infix = "_M";
    public static String domaintable_all_infix = "_ALL";
    public static String domaintable_lookedupberk_infix = "_LU";
    public static String domaintable_resdomain_infix = "_DM";
    public static String domaintable_lookedupberk_all_suffix = domaintable_lookedupberk_infix + domaintable_all_infix;
    public static String domaintable_lookedupberk_machine_infix = domaintable_lookedupberk_infix + domaintable_machine_infix;
    public static String domaintable_resdomain_all_suffix = domaintable_resdomain_infix + domaintable_all_infix;
    public static String domaintable_resdomain_machine_infix = domaintable_resdomain_infix + domaintable_machine_infix;
    public static String wf_table_prefix = "ResHadoop_";
}
