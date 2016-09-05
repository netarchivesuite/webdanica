package dk.kb.webdanica.webapp.resources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

import dk.kb.webdanica.datamodel.BlackList;
import dk.kb.webdanica.datamodel.CassandraBlackListDAO;
import dk.kb.webdanica.webapp.Constants;
import dk.kb.webdanica.webapp.Environment;
import dk.kb.webdanica.webapp.Navbar;
import dk.kb.webdanica.webapp.Servlet;
import dk.kb.webdanica.webapp.User;


public class BlackListsResource implements ResourceAbstract {

	    private static final Logger logger = Logger.getLogger(BlackListsResource.class.getName());

	    private Environment environment;

	    /*
	    protected static final int[] USER_ADD_PERMISSIONS = {Permission.P_USER_ADMIN, Permission.P_USER_ADD};
		*/
	    protected int R_BLACKLIST_LIST = -1;

		protected int R_BLACKLIST_ADD = -1;

		public static final String BLACKLIST_LIST_PATH = "/blacklists/";
		
	    @Override
	    public void resources_init(Environment environment) {
	        this.environment = environment;
	        
	    }

	    @Override
	    public void resources_add(ResourceManagerAbstract resourceManager) {
	        R_BLACKLIST_LIST = resourceManager.resource_add(this, BLACKLIST_LIST_PATH, 
	        		   		environment.getResourcesMap().getResourceByPath(BLACKLIST_LIST_PATH).isSecure());
	        //R_BLACKLIST_ADD = resourceManager.resource_add(this, "/blacklists/add", true);
	    }

	    //private String servicePath;

	    @Override
	    public void resource_service(ServletContext servletContext, User dab_user,
	    		HttpServletRequest req, HttpServletResponse resp,
	    		int resource_id, List<Integer> numerics, String pathInfo) throws IOException {
	    	
	    	if (Servlet.environment.getContextPath()== null) {
	        	Servlet.environment.setContextPath(req.getContextPath());
	        }
	        
	        /*
	        if (servicePath == null) {
	            servicePath = req.getContextPath() + req.getServletPath();
	        }
	        */
	        if (Servlet.environment.getBlacklistsPath() == null) {
	        	Servlet.environment.setBlacklistsPath(Servlet.environment.getContextPath() + "/blacklists/");
	        }
	        if (Servlet.environment.getBlacklistPath() == null) {
	        	Servlet.environment.setBlacklistPath(Servlet.environment.getContextPath() + "/blacklist/");
	        }
	        if (resource_id == R_BLACKLIST_LIST) {
	            users_list(dab_user, req, resp);
	        } else if (resource_id == R_BLACKLIST_ADD) {
	            blacklist_add(dab_user, req, resp);
	        }
	    }

	    private void blacklist_add(User dab_user, HttpServletRequest req,
                HttpServletResponse resp) {
	        	logger.warning("NOT YET IMPLEMENTED");
	        
        }

		public void users_list(User dab_user, HttpServletRequest req,
	            HttpServletResponse resp) throws IOException {
	        ServletOutputStream out = resp.getOutputStream();
	        resp.setContentType("text/html; charset=utf-8");

	        Caching.caching_disable_headers(resp);

	        Template template = environment.getTemplateMaster().getTemplate("blacklists_list.html");

	        TemplatePlaceHolder titlePlace = TemplatePlaceBase.getTemplatePlaceHolder("title");
	        TemplatePlaceHolder appnamePlace = TemplatePlaceBase.getTemplatePlaceHolder("appname");
	        TemplatePlaceHolder navbarPlace = TemplatePlaceBase.getTemplatePlaceHolder("navbar");
	        TemplatePlaceHolder userPlace = TemplatePlaceBase.getTemplatePlaceHolder("user");
	        TemplatePlaceHolder menuPlace = TemplatePlaceBase.getTemplatePlaceHolder("menu");
	        TemplatePlaceHolder headingPlace = TemplatePlaceBase.getTemplatePlaceHolder("heading");
	        TemplatePlaceHolder contentPlace = TemplatePlaceBase.getTemplatePlaceHolder("content");
	        TemplatePlaceHolder usersPlace = TemplatePlaceBase.getTemplatePlaceHolder("users");

	        List<TemplatePlaceBase> placeHolders = new ArrayList<TemplatePlaceBase>();
	        placeHolders.add(titlePlace);
	        placeHolders.add(appnamePlace);
	        placeHolders.add(navbarPlace);
	        placeHolders.add(userPlace);
	        placeHolders.add(menuPlace);
	        placeHolders.add(headingPlace);
	        placeHolders.add(contentPlace);
	        placeHolders.add(usersPlace);

	        TemplateParts templateParts = template.filterTemplate(placeHolders, resp.getCharacterEncoding());
	        
	        // Primary textarea
	        StringBuffer sb = new StringBuffer();
	        
	        //Connection conn = null;
	    
	            //conn = environment.dataSource.getConnection();
	            List<BlackList> blacklistList = CassandraBlackListDAO.getInstance().getLists(false);
	            for (BlackList b: blacklistList) {
	                sb.append("<tr>");
	                sb.append("<td>");
	                sb.append("<a href=\"");
	                sb.append(Servlet.environment.getBlacklistPath());
	                sb.append(b.getUid());
	                sb.append("/\">");
	                sb.append(b.getName());
	                sb.append("</a>");
	                sb.append("</td>");
	                sb.append("<td>");
	                sb.append(new Date(b.getLastUpdate()));
	                sb.append("</td>");
	                sb.append("<td>");
	                sb.append(b.isActive()?"Ja": "Nej");
	                sb.append("</td>");
	                sb.append("</tr>\n");
	            }
	       

	        /*
	         * Menu.
	         */

	        StringBuilder menuSb = new StringBuilder();

	        menuSb.append("<li id=\"state_0\"");
	        menuSb.append(" class=\"active\"");
	        menuSb.append("><a href=\"");
	        menuSb.append(Servlet.environment.getBlacklistsPath());
	        menuSb.append("\">");
	        menuSb.append("Liste over blacklister");
	        menuSb.append("</a></li>\n");
/*
	        if (dab_user.hasAnyPermission(USER_ADD_PERMISSIONS)) {
	            menuSb.append("<li id=\"state_1\"");
	            menuSb.append("><a href=\"");
	            menuSb.append(DABServlet.environment.usersPath);
	            menuSb.append("add/\">");
	            menuSb.append("Opret bruger");
	            menuSb.append("</a></li>\n");
	        }
*/
	        /*
	         * Heading.
	         */

	        String heading = "Liste over oprettede blacklists i systemet";

	        /*
	         * Places.
	         */

	        if (titlePlace != null) {
	            titlePlace.setText(HtmlEntity.encodeHtmlEntities(Constants.WEBAPP_NAME).toString());
	        }

	        if (appnamePlace != null) {
	            appnamePlace.setText(HtmlEntity.encodeHtmlEntities(Constants.WEBAPP_NAME +  Constants.SPACE + environment.getVersion()).toString());
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

	        if (headingPlace != null) {
	            headingPlace.setText(heading);
	        }

	        /*
	         * if ( contentPlace != null ) { contentPlace.setText( sb.toString() );
	         * }
	         */

	        if (usersPlace != null) {
	            usersPlace.setText(sb.toString());
	        }
	        
	        // Write out the page requested by the client browser
	        try {
	            for (int i = 0; i < templateParts.parts.size(); ++i) {
	                out.write(templateParts.parts.get(i).getBytes());
	            }
	            out.flush();
	            out.close();
	        } catch (IOException e) {
	        	
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


