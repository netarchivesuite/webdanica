package dk.kb.webdanica.datamodel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import dk.kb.webdanica.datamodel.criteria.DataSource;
import dk.kb.webdanica.datamodel.criteria.SingleCriteriaResult;
import dk.kb.webdanica.datamodel.dao.HBasePhoenixConnectionManager;
import dk.kb.webdanica.datamodel.dao.HBasePhoenixCriteriaResultsDAO;

@RunWith(JUnit4.class)
public class TestHBasePhoenixCriteriaResultsDAO {

	@Test
	public void test_hbasephoenix_criteriaresults_dao() {
		HBasePhoenixConnectionManager.register();

		Connection conn = null;
		Properties connprops = new Properties();

		try {
			conn = DriverManager.getConnection( "jdbc:phoenix:localhost", connprops );

			SingleCriteriaResult singleAnalysis = new SingleCriteriaResult("trimmedLine", "harvestName", "seedurl");

			HBasePhoenixCriteriaResultsDAO dao = new HBasePhoenixCriteriaResultsDAO();
			dao.insertRecord(singleAnalysis);

			List<String> clinks = new ArrayList<String>();
			clinks.add("1");
			clinks.add("2");
			clinks.add("3");

			singleAnalysis = new SingleCriteriaResult("trimmedLine", "harvestName", "seedurl");
			singleAnalysis.url = "url";
			singleAnalysis.Cext1 = 1L;
			singleAnalysis.Cext1 = 2L;
			singleAnalysis.Cext1 = 3L;
			for (int i=0; i<SingleCriteriaResult.StringCriteria.length; ++i) {
				singleAnalysis.C.put(SingleCriteriaResult.StringCriteria[i], Integer.toString(i + 1));
			}
			singleAnalysis.intDanish = 3.1415f;
			singleAnalysis.source = DataSource.NETARKIVET;
			singleAnalysis.calcDanishCode = 5;
			singleAnalysis.CLinks = clinks;
		    singleAnalysis.CText = "ctext";

			dao.insertRecord(singleAnalysis);

		    conn.close();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		HBasePhoenixConnectionManager.deregister();
	}

}
