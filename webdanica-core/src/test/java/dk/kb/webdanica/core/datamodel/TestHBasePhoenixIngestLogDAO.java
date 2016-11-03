package dk.kb.webdanica.core.datamodel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import dk.kb.webdanica.core.datamodel.IngestLog;
import dk.kb.webdanica.core.datamodel.dao.HBasePhoenixConnectionManager;
import dk.kb.webdanica.core.datamodel.dao.HBasePhoenixIngestLogDAO;

@RunWith(JUnit4.class)
public class TestHBasePhoenixIngestLogDAO {

	@Test
	public void test_hbasephoenix_ingestlog_dao() {
		HBasePhoenixConnectionManager.register();

		Connection conn = null;
		Properties connprops = new Properties();

		try {
			conn = DriverManager.getConnection( "jdbc:phoenix:localhost", connprops );

			List<String> aList = new ArrayList<String>(3);
			aList.add("One");
			aList.add("Two");
			aList.add("Three");

			IngestLog log = new IngestLog(aList, "filename", 1, 2, 3, 4);

			HBasePhoenixIngestLogDAO dao = new HBasePhoenixIngestLogDAO();
			dao.insertLog(log);

			List<Long>dates = dao.getIngestDates();
			for (int i=0; i<dates.size(); ++i) {
				System.out.println(dates.get(i));
			}

			log = dao.readIngestLog(dates.get(0));
			System.out.println(log.getDate() + " - " + log.getFilename());

			conn.close();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		HBasePhoenixConnectionManager.deregister();
	}

}
