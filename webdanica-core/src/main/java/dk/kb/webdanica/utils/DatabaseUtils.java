package dk.kb.webdanica.utils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import dk.kb.webdanica.datamodel.CriteriaResultsDAO;
import dk.kb.webdanica.datamodel.HarvestDAO;
import dk.kb.webdanica.datamodel.SeedsDAO;
import dk.kb.webdanica.datamodel.Status;
import dk.kb.webdanica.datamodel.dao.DAOFactory;

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
	
}
