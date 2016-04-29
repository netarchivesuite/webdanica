/*
 * Created on 27/09/2013
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

package dk.kb.webdanica.webapp;

import java.util.Map;
import java.util.TreeMap;

public class StatusBar {

    public static final int N_PROPS = 1;
    public static final int N_DEP = 2;
    public static final int N_THREADS = 3;
    public static final int N_PROGRESS = 4;
    public static final int N_LOG = 5;
    public static final int N_PIDS = 6;

	private static Map<Integer, String> navbarMap = new TreeMap<Integer, String>();

    private static String[][] items = null;

    public static synchronized String getStatusbar(int menu) {
    	if (items == null) {
        	items = new String[][] {
        			{ Servlet.environment.contextPath + "/status/props/", "Properties" },
                    { Servlet.environment.contextPath + "/status/dep/", "Dependencies" },
                    { Servlet.environment.contextPath + "/status/threads/", "Threads" },
                    { Servlet.environment.contextPath + "/status/progress/", "Progress" },
                    { Servlet.environment.contextPath + "/status/log/", "Log" },
                    { Servlet.environment.contextPath + "/status/pids/", "Pids" }
                    // "/status/sysno/pid/<numeric>/"
                    // "/status/pids/*"
        	};
    	}
    	String str = navbarMap.get(menu);
        if (str == null) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < items.length; ++i) {
                sb.append("<li");
                if (i == (menu - 1)) {
                    sb.append(" class=\"active\"");
                }
                sb.append(">");
                sb.append("<a href=\"");
                sb.append(items[i][0]);
                sb.append("\">");
                sb.append(items[i][1]);
                sb.append("</a></li>");
                sb.append("\n");
            }
            str = sb.toString();
            navbarMap.put(menu, str);
        }
        return str;
    }

}
