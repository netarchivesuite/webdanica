package dk.kb.webdanica.datamodel.criteria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import dk.kb.webdanica.datamodel.criteria.CodesFraction;

public class CalcDanishCode {

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

}
