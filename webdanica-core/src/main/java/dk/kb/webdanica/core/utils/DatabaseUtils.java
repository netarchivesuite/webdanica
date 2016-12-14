package dk.kb.webdanica.core.utils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dk.kb.webdanica.core.Constants;
import dk.kb.webdanica.core.WebdanicaSettings;
import dk.kb.webdanica.core.datamodel.Status;
import dk.kb.webdanica.core.datamodel.dao.CassandraDAOFactory;
import dk.kb.webdanica.core.datamodel.dao.CriteriaResultsDAO;
import dk.kb.webdanica.core.datamodel.dao.DAOFactory;
import dk.kb.webdanica.core.datamodel.dao.HBasePhoenixDAOFactory;
import dk.kb.webdanica.core.datamodel.dao.HarvestDAO;
import dk.kb.webdanica.core.datamodel.dao.SeedsDAO;

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
	public static void printDatabaseStats(DAOFactory daoFactory) throws Exception {
	       SeedsDAO dao = daoFactory.getSeedsDAO();
	       long millisStarted = System.currentTimeMillis();
	       long initialSeedsCount = dao.getSeedsCount(null);
	       Map<Integer, Long> mapcount = new HashMap<Integer,Long>();
	       for (Status s: Status.values()) {
	           mapcount.put(s.ordinal(), dao.getSeedsCount(s));
	       }
	       HarvestDAO hdao = daoFactory.getHarvestDAO();
	       long harvestCount = hdao.getCount();
	       CriteriaResultsDAO cdao = daoFactory.getCriteriaResultsDAO(); 
	       long totalCritResults = cdao.getCountByHarvest(null); // null meaning = get total count
	       
	       long millisEnded = System.currentTimeMillis();
	       System.out.println("Seeds-stats at '" + new Date() + "':");
	       System.out.println("=========================================");
	       System.out.println("Total-seeds: " + initialSeedsCount);
	       for (Status s: Status.values()) {
	           System.out.println("#seeds with status '" + s.name() + "': " + mapcount.get(s.ordinal()));
	       }
	       System.out.println("Total number of entries in 'harvests' table: " + harvestCount);
	       System.out.println("Total number of entries in 'criteria_results' table: " + totalCritResults);
	       System.out.println("Time spent computing the stats in secs: " + ((millisEnded - millisStarted)/1000));
	}
	
	public static DAOFactory getDao() {
		String databaseSystem = SettingsUtilities.getStringSetting(WebdanicaSettings.DATABASE_SYSTEM, 
				Constants.DEFAULT_DATABASE_SYSTEM);
		if ("cassandra".equalsIgnoreCase(databaseSystem)) {
			return new CassandraDAOFactory();
		} else if ("hbase-phoenix".equalsIgnoreCase(databaseSystem)) {
			return new HBasePhoenixDAOFactory();
		} else {
			return new HBasePhoenixDAOFactory();
		}
	}
	
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
