package dk.kb.webdanica.utils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class DatabaseUtils {
	public static Long parseLongFromString(String value) {
	    Long result = null;
	    try {
	    	result = Long.parseLong(value);
	    } catch (NumberFormatException e) {
	    	e.printStackTrace();
	    }
	    return result;
    }
    
	public static void setString(PreparedStatement s, int index, String value) throws SQLException {
    	if (value != null) {
    		s.setString(index, value);
    	} else {
    		s.setNull(index, Types.VARCHAR);
    	}
    }
    
	public static void setLong(PreparedStatement s, int index, Long value) throws SQLException {
    	if (value != null) {
    		s.setLong(index, value);
    	} else {
    		s.setNull(index, Types.BIGINT);
    	}
    }
}
