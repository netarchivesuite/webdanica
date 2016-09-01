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

import dk.kb.webdanica.datamodel.criteria.SingleCriteriaResult;
import dk.kb.webdanica.datamodel.harvest.CassandraCriteriaResultsDAO;
import dk.kb.webdanica.utils.UrlUtils;
import dk.kb.webdanica.webapp.Constants;
import dk.kb.webdanica.webapp.Environment;
import dk.kb.webdanica.webapp.Navbar;
import dk.kb.webdanica.webapp.Servlet;
import dk.kb.webdanica.webapp.User;
import dk.netarkivet.common.utils.StringUtils;
import dk.netarkivet.common.webinterface.HTMLUtils;


public class CriteriaResultsResource implements ResourceAbstract {

	    private static final Logger logger = Logger.getLogger(CriteriaResultsResource.class.getName());

	    private Environment environment;

	    /*
	    protected static final int[] USER_ADD_PERMISSIONS = {Permission.P_USER_ADMIN, Permission.P_USER_ADD};
		*/
	    protected int R_CRITERIARESULTS = -1;

		public static final String CRITERIA_RESULTS_PATH = "/criteriaresults/";

		private CassandraCriteriaResultsDAO dao;
		
	    @Override
	    public void resources_init(Environment environment) {
	        this.environment = environment;
	        dao = CassandraCriteriaResultsDAO.getInstance();
	    }

	    @Override
	    public void resources_add(ResourceManagerAbstract resourceManager) {
	        R_CRITERIARESULTS = resourceManager.resource_add(this, CRITERIA_RESULTS_PATH, 
	        		   		environment.getResourcesMap().getResourceByPath(CRITERIA_RESULTS_PATH).isSecure());
	        
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
	        if (Servlet.environment.getCriteriaResultPath() == null) {
	        	Servlet.environment.setCriteriaResultPath(Servlet.environment.getContextPath() + "/criteriaresult/");
	        }
	        if (Servlet.environment.getCriteriaResultsPath() == null) {
	        	Servlet.environment.setCriteriaResultsPath(Servlet.environment.getContextPath() + "/criteriaresults/");
	        }
	        if (resource_id == R_CRITERIARESULTS) {
	            criteriaresults_show(dab_user, req, resp, pathInfo);
	        }
//	        else if (resource_id == R_BLACKLIST_ADD) {
//	            blacklist_add(dab_user, req, resp);
//	        }
	    }

	    private void blacklist_add(User dab_user, HttpServletRequest req,
                HttpServletResponse resp) {
	        	logger.warning("NOT YET IMPLEMENTED");   
        }
	    
	    private String getHarvestName(String pathInfo) {
	    	// Retrieving UUID or maybe name from pathinfo instead of String equivalent of numerics
	        String[] pathInfoParts  = pathInfo.split(CRITERIA_RESULTS_PATH);
	        String harvestName = null;
	        if (pathInfoParts.length > 1) {
	        	harvestName = pathInfoParts[1];
	        	if (harvestName.endsWith("/")) {
	        		harvestName = harvestName.substring(0, harvestName.length()-1);
	            }
	        }
	        return harvestName;
        }

		public void criteriaresults_show(User dab_user, HttpServletRequest req,
	            HttpServletResponse resp, String pathInfo) throws IOException {
	        ServletOutputStream out = resp.getOutputStream();
	        resp.setContentType("text/html; charset=utf-8");

	        Caching.caching_disable_headers(resp);

	        Template template = environment.getTemplateMaster().getTemplate("users_list.html");

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
	        String harvestName = getHarvestName(pathInfo);
	        List<SingleCriteriaResult> blacklistList;
	        if (harvestName == null) {
	        	blacklistList = dao.getResults();
	        } else {
	        	blacklistList = dao.getResultsByHarvestname(harvestName);
	        }
	        for (SingleCriteriaResult b: blacklistList) {
	        	sb.append("<tr>");
	        	sb.append("<td>");
	        	sb.append("<a href=\"");
	        	sb.append(Servlet.environment.getCriteriaResultPath());
	        	sb.append(b.harvestName + "/" + HTMLUtils.encode(b.url));
	        	sb.append("/\">");
	        	sb.append(StringUtils.makeEllipsis(b.url, 50) + " (harvest: " + b.harvestName + ")");
	        	sb.append("</a>");
	        	sb.append("</td>");
	        	sb.append("<td>");
	        	sb.append(new Date(b.insertedDate) + "");
	        	sb.append("</td>");
	        	sb.append("<td>");
	        	sb.append(b.calcDanishCode);
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
	        menuSb.append(Servlet.environment.getCriteriaResultsPath());
	        menuSb.append("\">");
	        menuSb.append("Liste over criteriaresults");
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

	        String heading = "Liste over fundne criteria resultater i systemet";

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
	        	logger.warning("Unexpected exception: " + e);
	        }
	    }

		
	}


