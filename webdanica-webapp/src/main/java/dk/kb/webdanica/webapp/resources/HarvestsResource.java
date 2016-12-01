package dk.kb.webdanica.webapp.resources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

import dk.kb.webdanica.core.interfaces.harvesting.SingleSeedHarvest;
import dk.kb.webdanica.webapp.Constants;
import dk.kb.webdanica.webapp.Environment;
import dk.kb.webdanica.webapp.Navbar;
import dk.kb.webdanica.webapp.Servlet;
import dk.kb.webdanica.webapp.User;

public class HarvestsResource implements ResourceAbstract {

	    private static final Logger logger = Logger.getLogger(HarvestsResource.class.getName());

	    private Environment environment;

	    /*
	    protected static final int[] USER_ADD_PERMISSIONS = {Permission.P_USER_ADMIN, Permission.P_USER_ADD};
		*/
	    protected int R_HARVEST_LIST = -1;

		//protected int R_BLACKLIST_ADD = -1;

		public static final String HARVESTS_PATH = "/harvests/";
		
	    @Override
	    public void resources_init(Environment environment) {
	        this.environment = environment;
	        
	    }

	    @Override
	    public void resources_add(ResourceManagerAbstract resourceManager) {
	        R_HARVEST_LIST = resourceManager.resource_add(this, HARVESTS_PATH, 
	        		   		environment.getResourcesMap().getResourceByPath(HARVESTS_PATH).isSecure());
	    }
	    
	    @Override
	    public void resource_service(ServletContext servletContext, User dab_user,
	    		HttpServletRequest req, HttpServletResponse resp,
	    		int resource_id, List<Integer> numerics, String pathInfo) throws IOException {
	        
	        if (resource_id == R_HARVEST_LIST) {
	            harvests_list(dab_user, req, resp, pathInfo);
	        } 
	    }

		public void harvests_list(User dab_user, HttpServletRequest req,
	            HttpServletResponse resp, String pathInfo) throws IOException {
			
			HarvestRequest hr = HarvestRequest.getRequest(HarvestsResource.HARVESTS_PATH, pathInfo);
			
			
			ServletOutputStream out = resp.getOutputStream();
	        resp.setContentType("text/html; charset=utf-8");

	        Caching.caching_disable_headers(resp);

	        Template template = environment.getTemplateMaster().getTemplate("harvests_list.html");

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

	        List<SingleSeedHarvest> harvestList = null;
	        try {
	        	if (hr.viewAll()) {
	        		harvestList = environment.getConfig().getDAOFactory().getHarvestDAO().getAll();
	        	} else {
	        		harvestList = environment.getConfig().getDAOFactory().getHarvestDAO().getAllWithSeedurl(hr.getSeedUrl());
	        	}
	        } catch (Exception e) {
	        	// Create error-page
	        	String errMsg = "Unexpected exception thrown:" + e;
	        	logger.log(Level.WARNING, errMsg, e);
	        	CommonResource.show_error(errMsg, resp, environment);
	        	return;	
	        }

	        for (SingleSeedHarvest harvest: harvestList) {
	        	sb.append("<tr>");
	        	sb.append("<td>");
	        	sb.append("<a href=\"");
	        	sb.append(Servlet.environment.getHarvestPath());
	        	sb.append(harvest.getHarvestName());
	        	sb.append("/\">");
	        	sb.append(harvest.getHarvestName());
	        	sb.append("</a>");
	        	sb.append("</td>");
	        	sb.append("<td>");
	        	sb.append(harvest.getSeed());
	        	sb.append("</td>");
	        	sb.append("<td>");
	        	sb.append(new Date(harvest.getHarvestedTime()));
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
	        menuSb.append(Servlet.environment.getHarvestsPath());
	        menuSb.append("\">");
	        menuSb.append("Liste over harvests");
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

	        String heading = "Liste over høstninger i systemet";
	        if (!hr.viewAll()) {
	        	heading = "Liste over høstninger i systemet af seedurl '" + hr.getSeedUrl() + "'";
	        }
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
	        	e.printStackTrace();
	        }
	    }
	}


