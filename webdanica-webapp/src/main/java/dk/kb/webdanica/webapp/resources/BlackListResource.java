/*
 * Created on 19/08/2013
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

package dk.kb.webdanica.webapp.resources;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.antiaction.common.filter.Caching;
import com.antiaction.common.html.HtmlEntity;
import com.antiaction.common.templateengine.Template;
import com.antiaction.common.templateengine.TemplateParts;
import com.antiaction.common.templateengine.TemplatePlaceBase;
import com.antiaction.common.templateengine.TemplatePlaceHolder;
import com.antiaction.common.templateengine.TemplatePlaceTag;

import dk.kb.webdanica.webapp.Environment;
import dk.kb.webdanica.webapp.Navbar;
import dk.kb.webdanica.webapp.Servlet;
import dk.kb.webdanica.webapp.User;

/*
import dk.netarkivet.dab.webadmin.dao.Domain;
import dk.netarkivet.dab.webadmin.dao.NotificationSubscription;
import dk.netarkivet.dab.webadmin.dao.Organization;
import dk.netarkivet.dab.webadmin.dao.Permission;
import dk.netarkivet.dab.webadmin.dao.UrlRecord;
import dk.netarkivet.dab.webadmin.dao.User;
*/

public class BlackListResource implements ResourceAbstract {

    private static final Logger logger = Logger.getLogger(BlackListResource.class.getName());
/*
    protected static final int[] USER_ADD_PERMISSIONS = {Permission.P_USER_ADMIN, Permission.P_USER_ADD};

    protected static final int[] USER_ADMIN_PERMISSIONS = {Permission.P_USER_ADMIN};
*/

    protected int R_BLACKLIST = -1;
    
    private Environment environment;

    @Override
    public void resources_init(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void resources_add(ResourceManagerAbstract resourceManager) {
        R_BLACKLIST = resourceManager.resource_add(this, "/blacklist/<string>/", true);
        
/*        
        R_USER_PASSWORD = resourceManager.resource_add(this, "/user/<numeric>/change_password/", true);
        R_USER_PERMISSIONS = resourceManager.resource_add(this, "/user/<numeric>/permissions/", true);
        R_USER_NOTIFICATION_SUBSCRIPTIONS = resourceManager.resource_add(this, "/user/<numeric>/notification_subscriptions/", true);
 */       
    }

    //private String servicePath;

    @Override
    public void resource_service(ServletContext servletContext, User dab_user,
    		HttpServletRequest req, HttpServletResponse resp,
    		int resource_id, List<Integer> numerics, String pathInfo) throws IOException {
    	logger.info("pathInfo:" + pathInfo);
    	
        if (Servlet.environment.getContextPath()== null) {
        	Servlet.environment.setContextPath(req.getContextPath());
        }
        /*
        if (servicePath == null) {
            servicePath = req.getContextPath() + req.getServletPath();
        }
        */
        if (Servlet.environment.getBlacklistPath() == null) {
        	Servlet.environment.setBlacklistPath(Servlet.environment.getContextPath() + "/blacklist/");
        }
        if (Servlet.environment.getBlacklistsPath() == null) {
        	Servlet.environment.setBlacklistsPath(Servlet.environment.getContextPath() + "/blacklists/");
        }
        if (resource_id == R_BLACKLIST) {
            blacklist_show(dab_user, req, resp, "");
        } 
 /*       
        
        
        else if (resource_id == R_USER_PASSWORD) {
            user_change_password(dab_user, req, resp, numerics);
        } else if (resource_id == R_USER_PERMISSIONS) {
            user_show_permissions(dab_user, req, resp, numerics);
        }
        else if (resource_id == R_USER_NOTIFICATION_SUBSCRIPTIONS) {
            user_show_notification_subscriptions(dab_user, req, resp, numerics);
        }
   */     
        
    }

    public void blacklist_show(User dab_user, HttpServletRequest req,
            HttpServletResponse resp, String blacklistName)
            throws IOException {
        ServletOutputStream out = resp.getOutputStream();
        resp.setContentType("text/html; charset=utf-8");

        String errorStr = null;
        String successStr = null;

        Caching.caching_disable_headers(resp);

        User user = null;
/*
        
        Connection conn = null;
        try {
            conn = environment.dataSource.getConnection();
            user = User.getUserById(conn, numerics.get(0));
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.toString(), e);
        }
*/                                       
        
/*
        if ("POST".equals(req.getMethod())) {        	
	        if (dab_user.hasAnyPermission(USER_ADMIN_PERMISSIONS)) {
	            String username = req.getParameter("username");
	            String name = req.getParameter("name");
	            String email = req.getParameter("email");
	            String organization = req.getParameter("organization");
	            String active = req.getParameter("active");
	            if (username != null && username.length() > 0) {
	                user.username = username;
	            }
	            if (name != null && name.length() > 0) {
	                user.name = name;
	            }
	            if (email != null && email.length() > 0) {
	                user.email = email;
	            }
	            if (organization != null && organization.length() > 0) {
	            	try {
	            		user.organization = Integer.parseInt(organization);
	            	} catch (NumberFormatException e) {
	            	}
	            }
	            if (active != null && active.length() > 0) {
	            	user.active = true;
	            } else {
	            	user.active = false;
	            }
	            user.store(conn);
	            successStr = "Bruger oplysninger gemt.";
	        } else {
	        	errorStr = "Du har ikke rettighed til at ændre bruger oplysninger!";
	        }
        }
*/


        // TODO error text

        Template template = environment.getTemplateMaster().getTemplate("user_show.html");

        TemplatePlaceHolder titlePlace = TemplatePlaceBase.getTemplatePlaceHolder("title");
        TemplatePlaceHolder appnamePlace = TemplatePlaceBase.getTemplatePlaceHolder("appname");
        TemplatePlaceHolder navbarPlace = TemplatePlaceBase.getTemplatePlaceHolder("navbar");
        TemplatePlaceHolder userPlace = TemplatePlaceBase.getTemplatePlaceHolder("user");
        TemplatePlaceHolder menuPlace = TemplatePlaceBase.getTemplatePlaceHolder("menu");
        TemplatePlaceHolder backPlace = TemplatePlaceBase.getTemplatePlaceHolder("back");
        TemplatePlaceHolder headingPlace = TemplatePlaceBase.getTemplatePlaceHolder("heading");
        TemplatePlaceHolder alertPlace = TemplatePlaceBase.getTemplatePlaceHolder("alert");
        TemplatePlaceHolder contentPlace = TemplatePlaceBase.getTemplatePlaceHolder("content");
        TemplatePlaceTag usernameTag = TemplatePlaceTag.getInstance("input", "username");
        TemplatePlaceTag nameTag = TemplatePlaceTag.getInstance("input", "name");
        TemplatePlaceTag emailTag = TemplatePlaceTag.getInstance("input", "email");
        TemplatePlaceHolder organizationPlace = TemplatePlaceBase.getTemplatePlaceHolder("organizations_input");
        TemplatePlaceTag activeTag = TemplatePlaceTag.getInstance("input", "active");

        List<TemplatePlaceBase> placeHolders = new ArrayList<TemplatePlaceBase>();
        placeHolders.add(titlePlace);
        placeHolders.add(appnamePlace);
        placeHolders.add(navbarPlace);
        placeHolders.add(userPlace);
        placeHolders.add(menuPlace);
        placeHolders.add(backPlace);
        placeHolders.add(headingPlace);
        placeHolders.add(alertPlace);
        placeHolders.add(contentPlace);
        placeHolders.add(usernameTag);
        placeHolders.add(nameTag);
        placeHolders.add(emailTag);
        placeHolders.add(organizationPlace);
        placeHolders.add(activeTag);

        TemplateParts templateParts = template.filterTemplate(placeHolders, resp.getCharacterEncoding());

        /*
         * Menu.
         */

        StringBuilder menuSb = new StringBuilder();

        menuSb.append("<li id=\"state_0\"");
        menuSb.append(" class=\"active\"");
        menuSb.append("><a href=\"");
        menuSb.append(Servlet.environment.getBlacklistPath());
        menuSb.append(user.id);
        menuSb.append("/\">");
        menuSb.append("user.name");
        menuSb.append("</a></li>");

        menuSb.append("<li id=\"state_0\"");
        menuSb.append("><a href=\"");
        menuSb.append(Servlet.environment.getBlacklistPath());
        menuSb.append(user.id);
        menuSb.append("/change_password/\">");
        menuSb.append("Skift adgangskode");
        menuSb.append("</a></li>");

        menuSb.append("<li id=\"state_1\"");
        menuSb.append("><a href=\"");
        menuSb.append(Servlet.environment.getBlacklistPath());
        menuSb.append(user.id);
        menuSb.append("/permissions/\">");
        menuSb.append("Rettigheder");
        menuSb.append("</a></li>");

        menuSb.append("<li id=\"state_1\"");
        menuSb.append("><a href=\"");
        menuSb.append(Servlet.environment.getBlacklistPath());
        menuSb.append(user.id);
        menuSb.append("/notification_subscriptions/\">");
        menuSb.append("Email abonnementer");
        menuSb.append("</a></li>");

        /*
         * Organization.
         */
/*
        StringBuilder orgSb = new StringBuilder();
        List<Organization> orgs = Organization.getOrganizationsList(conn);
        orgSb.append("<select name=\"organization\">");
        for (Organization org: orgs) {
        	orgSb.append("<option value=\"");
        	orgSb.append(org.id);
        	orgSb.append("\"");
        	if (user.organization == org.id) {
        		orgSb.append(" selected=\"1\"");
        	}
        	orgSb.append(">");
        	orgSb.append(org.name);
        	orgSb.append("</option>");
        }
        orgSb.append("</select>");
*/
        /*
         * Heading.
         */
        String blackListName = null;
        Long blackListLastUpdatedTime = null;
        Boolean blackListActive = false;
        long blackListSize = 0;
        String heading = "Oplysninger om blacklist '" + blackListName + "':";

        /*
         * Places.
         */

        if (titlePlace != null) {
            titlePlace.setText(HtmlEntity.encodeHtmlEntities(dk.kb.webdanica.webapp.Constants.WEBAPP_NAME).toString());
        }

        if (appnamePlace != null) {
            appnamePlace.setText(HtmlEntity.encodeHtmlEntities(dk.kb.webdanica.webapp.Constants.WEBAPP_NAME + dk.kb.webdanica.webapp.Constants.SPACE + environment.getVersion()).toString());
        }

        if (navbarPlace != null) {
            navbarPlace.setText(Navbar.getNavbar(Navbar.N_BLACKLISTS));
        }

        if (userPlace != null) {
            userPlace.setText(Navbar.getUserHref(dab_user));
        }

        if (menuPlace != null) {
            menuPlace.setText(menuSb.toString());
        }

        if (backPlace != null) {
        	backPlace.setText("<a href=\"" 
        			+ Servlet.environment.getBlacklistsPath() 
        			+ "\" class=\"btn btn-primary\"><i class=\"icon-white icon-list\"></i> Tilbage til oversigten</a>");
        }

        if (headingPlace != null) {
            headingPlace.setText(heading);
        }

        if (contentPlace != null) {
            //contentPlace.setText(sb.toString());
        }

        if (usernameTag != null) {
        	usernameTag.htmlItem.setAttribute("value", user.username);
        	/*
	        if (!dab_user.hasAnyPermission(USER_ADMIN_PERMISSIONS)) {
	        	usernameTag.htmlItem.setAttribute("disabled", "1");
	        } */
        }

        if (nameTag != null) {
        	nameTag.htmlItem.setAttribute("value", "user.name");
        	/*
	        if (!dab_user.hasAnyPermission(USER_ADMIN_PERMISSIONS)) {
	        	nameTag.htmlItem.setAttribute("disabled", "1");
	        } */
        }

        if (emailTag != null) {
            emailTag.htmlItem.setAttribute("value", "user.email");
            /*
	        if (!dab_user.hasAnyPermission(USER_ADMIN_PERMISSIONS)) {
	        	emailTag.htmlItem.setAttribute("disabled", "1");
	        } */
	        
        }
/*
        
        if (organizationPlace != null) {
        	organizationPlace.setText(orgSb.toString());
        }
*/
        if (activeTag != null) {
        	if (user.active) {
        		activeTag.htmlItem.setAttribute("checked", "1");
        	}
        }

        if (alertPlace != null) {
            StringBuilder alertSb = new StringBuilder();
            if (errorStr != null) {
                alertSb.append("<div class=\"row-fluid\">");
                alertSb.append("<div class=\"span12 bgcolor\">");
                alertSb.append("<div class=\"alert alert-error\">");
                alertSb.append("<a href=\"#\" class=\"close\" data-dismiss=\"alert\">x</a>");
                alertSb.append(errorStr);
                alertSb.append("</div>");
                alertSb.append("</div>");
                alertSb.append("</div>");
                alertPlace.setText(alertSb.toString());
            }
            if (successStr != null) {
                alertSb.append("<div class=\"row-fluid\">");
                alertSb.append("<div class=\"span12 bgcolor\">");
                alertSb.append("<div class=\"alert alert-success\">");
                alertSb.append("<a href=\"#\" class=\"close\" data-dismiss=\"alert\">x</a>");
                alertSb.append(successStr);
                alertSb.append("</div>");
                alertSb.append("</div>");
                alertSb.append("</div>");
                alertPlace.setText(alertSb.toString());
            }
        }

        try {
            for (int i = 0; i < templateParts.parts.size(); ++i) {
                out.write(templateParts.parts.get(i).getBytes());
            }
            out.flush();
            out.close();
        } catch (IOException e) {
        	logger.warning("IOException thrown, but ignored: " + e);        
        }

        /*
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, e.toString(), e);
            }
        }
        */
    }
        
}
