/*
 * Created on 12/04/2013
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

package dk.kb.webdanica.webapp;

import java.util.Map;
import java.util.TreeMap;

public class Navbar {

    public static final int N_INDEX = 0;

    public static final int N_URLS_ADD = 1;

    public static final int N_URLS_UPLOAD = 1;

    public static final int N_URLS = 1;

    public static final int N_URL_SHOW = 1;

    public static final int N_SEARCH = 2;

    public static final int N_DOMAINS_ADD = 2;

    public static final int N_DOMAINS_UPLOAD = 2;

    public static final int N_DOMAINS = 2;

    public static final int N_USERS = 3;

    private static Map<Integer, String> navbarMap = new TreeMap<Integer, String>();

    private static String[][] items = null;

    static {
    	items = new String[][] {
    			//{ DABServlet.environment.contextPath + "/insert/", "Opret" },
                { Servlet.environment.contextPath + "/urls/", "Url'er" },
                // { DABServlet.environment.contextPath + "/search/", "Søg" },
                { Servlet.environment.contextPath + "/domains/", "Domæner" },
                { Servlet.environment.contextPath + "/users/", "Brugere" },
                { Servlet.environment.contextPath + "/status/", "Status" }
    	};
    }

    public static synchronized String getNavbar(int menu) {
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

    public static String getUser(User dab_user) {
        StringBuilder sb = new StringBuilder();
        sb.append("<a href=\"");
        sb.append(Servlet.environment.contextPath);
        sb.append("/user/");
        sb.append(dab_user.id);
        sb.append("/\" class=\"navbar-link\">");
        sb.append(dab_user.username);
        sb.append("</a>");
        return sb.toString();
    }

}