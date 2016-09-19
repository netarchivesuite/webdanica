package dk.kb.webdanica.datamodel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import dk.kb.webdanica.datamodel.dao.HBasePhoenixBlackListDAO;
import dk.kb.webdanica.datamodel.dao.HBasePhoenixConnectionManager;

@RunWith(JUnit4.class)
public class TestHBasePhoenixBlackListDAO {

	@Test
	public void test_hbasephoenix_blacklist_dao() {
		HBasePhoenixConnectionManager.register();

		Connection conn = null;
		Properties connprops = new Properties();

		try {
			conn = DriverManager.getConnection( "jdbc:phoenix:localhost", connprops );

			List<String> aList = new ArrayList<String>(3);
			aList.add("One");
			aList.add("Two");
			aList.add("Three");

			BlackList aBlackList = new BlackList("name", "description", aList, true);

			HBasePhoenixBlackListDAO dao = new HBasePhoenixBlackListDAO();
			dao.insertList(aBlackList);

			UUID uid = UUID.fromString("cbd23b95-6951-4136-ad26-e609928adc22");
			aBlackList = dao.readBlackList(uid);

			aList = aBlackList.getList();
			for (int i=0; i<aList.size(); ++i) {
				System.out.println(aList.get(i));
			}

			List<BlackList> blacklistList = dao.getLists(true);
			System.out.println(blacklistList.size());

			blacklistList = dao.getLists(false);
			System.out.println(blacklistList.size());

			conn.close();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		HBasePhoenixConnectionManager.deregister();
	}

}
