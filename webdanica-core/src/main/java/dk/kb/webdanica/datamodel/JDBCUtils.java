package dk.kb.webdanica.datamodel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JDBCUtils {

	public static List<String> sqlArrayToArrayList(java.sql.Array sqlArr) throws SQLException {
		List<String> lst = null;
		try {
			if (sqlArr != null) {
				String[] arr = (String[])(sqlArr.getArray());
				lst = new ArrayList<String>();
				for (int i=0; i<arr.length; ++i) {
					lst.add(arr[i]);
				}
			}
		} finally {
			if (sqlArr != null) {
				sqlArr.free();
			}
		}
		return lst;
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> sqlArrayRecordSetToArrayList(java.sql.Array sqlArr, Class<T> clazz) throws SQLException {
		List<T> lst = null;
		ResultSet rs = null;
		try {
			if (sqlArr != null) {
				rs = sqlArr.getResultSet();
				if (rs != null) {
					lst = new ArrayList<T>();
					while (rs.next()) {
						lst.add((T)rs.getObject(1, clazz.getClass()));
					}
				}
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (sqlArr != null) {
				sqlArr.free();
			}
		}
		return lst;
	}

}
