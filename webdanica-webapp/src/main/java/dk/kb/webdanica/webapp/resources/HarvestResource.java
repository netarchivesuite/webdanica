/*
 * Created on 19/08/2013
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

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

import dk.kb.webdanica.datamodel.CriteriaResultsDAO;
import dk.kb.webdanica.datamodel.HarvestDAO;
import dk.kb.webdanica.interfaces.harvesting.HarvestReport;
import dk.kb.webdanica.webapp.Environment;
import dk.kb.webdanica.webapp.Navbar;
import dk.kb.webdanica.webapp.Servlet;
import dk.kb.webdanica.webapp.User;

public class HarvestResource implements ResourceAbstract {

	private static final Logger logger = Logger.getLogger(HarvestResource.class.getName());
    
    private static final String HARVEST_SHOW_TEMPLATE = "harvest_master.html";

    protected int R_HARVEST = -1;
    public static final String HARVEST_PATH = "/harvest/"; // TODO change to "/harvest/<String>/" instead of parsing pathInfo
    private Environment environment;
    private HarvestDAO hdao;
	private CriteriaResultsDAO cdao;
    
    @Override
    public void resources_init(Environment environment) {
        this.environment = environment;
        this.hdao = environment.getConfig().getHarvestDAO();
        this.cdao = environment.getConfig().getCriteriaResultsDao();
    }
    
    @Override
    public void resources_add(ResourceManagerAbstract resourceManager) {
        //R_HARVEST = resourceManager.resource_add(this, "/blacklist/<string>/", true);
        R_HARVEST = resourceManager.resource_add(this, HARVEST_PATH, 
        		environment.getResourcesMap().getResourceByPath(HARVEST_PATH).isSecure());
        logger.info("R_HARVEST = " + R_HARVEST);
    }

    @Override
    public void resource_service(ServletContext servletContext, User dab_user,
    		HttpServletRequest req, HttpServletResponse resp,
    		int resource_id, List<Integer> numerics, String pathInfo) throws IOException {
    	logger.info("pathInfo: " + pathInfo);
        logger.info("resource_id: " + resource_id);
        HarvestReport b = null;
        String harvestName = getHarvestName(pathInfo);
        boolean isError = false;
        if (harvestName == null) {
        	harvestName = "ERROR: unable to extract harvestname from path '" + pathInfo + "'";
        	isError = true;
        	b = HarvestReport.makeErrorObject(harvestName);
        }
        
        if (!isError){ // Try to fetch harvest
        	try {
            	b = hdao.getHarvest(harvestName);
        	} catch (Exception e) {
        	}
        	if (b == null) {
        		harvestName = "ERROR: unable to show harvest with name '" + harvestName + "'. It doesn't exist";
            	isError = true;
            	b = HarvestReport.makeErrorObject(harvestName);	
        	}
        }
        
        if (Servlet.environment.getContextPath()== null) {
        	Servlet.environment.setContextPath(req.getContextPath());
        }
        
        if (Servlet.environment.getHarvestPath() == null) {
        	Servlet.environment.setHarvestPath(Servlet.environment.getContextPath() + "/harvest/");
        }
        if (Servlet.environment.getHarvestsPath() == null) {
        	Servlet.environment.setHarvestsPath(Servlet.environment.getContextPath() + "/harvests/");
        }
        if (resource_id == R_HARVEST) {
            harvest_show(dab_user, req, resp, b);
        } 
    }

    private String getHarvestName(String pathInfo) {
        // Retrieving UUID or maybe name from pathinfo instead of String equivalent of numerics
        String[] pathInfoParts  = pathInfo.split(HARVEST_PATH);
        String harvestName = null;
        if (pathInfoParts.length > 1) {
        	harvestName = pathInfoParts[1];
        	if (harvestName.endsWith("/")) {
        		harvestName = harvestName.substring(0, harvestName.length()-1);
            }
        }
        return harvestName;
    }

	public void harvest_show(User dab_user, HttpServletRequest req,
            HttpServletResponse resp, HarvestReport b)
            throws IOException {
        ServletOutputStream out = resp.getOutputStream();
        resp.setContentType("text/html; charset=utf-8");
        // TODO error text
        String errorStr = null;
        String successStr = null;
        Caching.caching_disable_headers(resp);

        Template template = environment.getTemplateMaster().getTemplate(HARVEST_SHOW_TEMPLATE);

        TemplatePlaceHolder titlePlace = TemplatePlaceBase.getTemplatePlaceHolder("title");
        TemplatePlaceHolder appnamePlace = TemplatePlaceBase.getTemplatePlaceHolder("appname");
        TemplatePlaceHolder navbarPlace = TemplatePlaceBase.getTemplatePlaceHolder("navbar");
        TemplatePlaceHolder userPlace = TemplatePlaceBase.getTemplatePlaceHolder("user");
        TemplatePlaceHolder menuPlace = TemplatePlaceBase.getTemplatePlaceHolder("menu");
        TemplatePlaceHolder backPlace = TemplatePlaceBase.getTemplatePlaceHolder("back");
        TemplatePlaceHolder headingPlace = TemplatePlaceBase.getTemplatePlaceHolder("heading");
        TemplatePlaceHolder alertPlace = TemplatePlaceBase.getTemplatePlaceHolder("alert");
        TemplatePlaceHolder contentPlace = TemplatePlaceBase.getTemplatePlaceHolder("content");
        
/*        
        <h4> Name: <placeholder id="name" /></br>  
        Seed: <placeholder id="seed" /></br>a
        Harvested Time: <placeholder id="harvested_time" /></br>
        CriteriaResults: <placeholder id="criteria_results" /></br>
        Error:<placeholder id="errors" /></br> 
        Successful: <placeholder id="successful" /></br>
        endState:<placeholder id="endState" /></br>
        </h4>
  */      
        
        TemplatePlaceHolder namePlace = TemplatePlaceBase.getTemplatePlaceHolder("name");
        TemplatePlaceHolder seedPlace = TemplatePlaceBase.getTemplatePlaceHolder("seed");
        TemplatePlaceHolder harvestedTimePlace = TemplatePlaceBase.getTemplatePlaceHolder("harvested_time");
        TemplatePlaceHolder criteriaresultsPlace = TemplatePlaceBase.getTemplatePlaceHolder("criteria_results");
        TemplatePlaceHolder successfullPlace = TemplatePlaceBase.getTemplatePlaceHolder("successful");
        TemplatePlaceHolder endStatePlace = TemplatePlaceBase.getTemplatePlaceHolder("endState");
        TemplatePlaceHolder errorPlace = TemplatePlaceBase.getTemplatePlaceHolder("errors");
        
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
        // add the new placeholders
        placeHolders.add(namePlace);
        placeHolders.add(seedPlace);
        placeHolders.add(harvestedTimePlace);
        placeHolders.add(criteriaresultsPlace);
        placeHolders.add(successfullPlace);
        placeHolders.add(endStatePlace);
        placeHolders.add(errorPlace);
        

        TemplateParts templateParts = template.filterTemplate(placeHolders, resp.getCharacterEncoding());
        
        /*
         * Heading.
         */
        String heading = "Information about harvest '" + b.harvestName + "' of seed '" + b.seed + "' :";
        
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
            navbarPlace.setText(Navbar.getNavbar(Navbar.N_BLACKLISTS)); // FIXME
        }

        if (userPlace != null) {
            userPlace.setText(Navbar.getUserHref(dab_user));
        } 

        if (backPlace != null) {
        	backPlace.setText("<a href=\"" 
        			+ Servlet.environment.getHarvestsPath() 
        			+ "\" class=\"btn btn-primary\"><i class=\"icon-white icon-list\"></i> Tilbage til oversigten</a>");
        } else {
        	logger.warning("No back´placeholder found in template '" + HARVEST_SHOW_TEMPLATE + "'" );
        }

        if (headingPlace != null) {
            headingPlace.setText(heading);
        } else {
        	logger.warning("No heading´ placeholder found in template '" + HARVEST_SHOW_TEMPLATE + "'" );
        }

        /*
         * TemplatePlaceHolder namePlace = TemplatePlaceBase.getTemplatePlaceHolder("name");
        TemplatePlaceHolder seedPlace = TemplatePlaceBase.getTemplatePlaceHolder("seed");
        TemplatePlaceHolder harvestedTimePlace = TemplatePlaceBase.getTemplatePlaceHolder("harvested_time");
        TemplatePlaceHolder criteriaresultsPlace = TemplatePlaceBase.getTemplatePlaceHolder("criteria_results");
        TemplatePlaceHolder successfullPlace = TemplatePlaceBase.getTemplatePlaceHolder("successful");
        TemplatePlaceHolder endStatePlace = TemplatePlaceBase.getTemplatePlaceHolder("endState");
        TemplatePlaceHolder errorPlace = TemplatePlaceBase.getTemplatePlaceHolder("errors");
         * 
         * 
         * 
         */
        
        ResourceUtils.insertText(namePlace, "name",  b.harvestName, HARVEST_SHOW_TEMPLATE, logger);
        ResourceUtils.insertText(seedPlace, "seed",  b.seed, HARVEST_SHOW_TEMPLATE, logger);
        
        ResourceUtils.insertText(harvestedTimePlace, "harvested_time", "" + new Date(b.harvestedTime), HARVEST_SHOW_TEMPLATE, logger);
        
        ResourceUtils.insertText(successfullPlace, "successful",  b.successful + "", HARVEST_SHOW_TEMPLATE, logger);
        String error = "No errors";
        if (b.error != null && !b.error.isEmpty()) {
        	error = b.error;
        }
        ResourceUtils.insertText(errorPlace, "errors",  error, HARVEST_SHOW_TEMPLATE, logger);
        ResourceUtils.insertText(endStatePlace, "endState",  b.finalState + "", HARVEST_SHOW_TEMPLATE, logger);

        // FIXME better handling
        long critCount = 0;
        try {
            critCount = cdao.getCountByHarvest(b.harvestName);
        } catch (Exception e) {
        	
        }

        String linkToCriteriaresults = "No criteriaresults found for this harvest";
        if (critCount > 0) {
        	linkToCriteriaresults = "<a href=\"" + environment.getCriteriaResultsPath() + b.harvestName + "/>" + "</a>";
        }
        ResourceUtils.insertText(criteriaresultsPlace, "criteria_results",  linkToCriteriaresults, HARVEST_SHOW_TEMPLATE, logger);
         
        StringBuilder sb = new StringBuilder();
        sb.append("<pre>\r\n");

        for (String listElement: b.getAllFiles()) {
    		sb.append(listElement);
    		sb.append("\r\n");
    	}
        
    	// Currently, all harvested files are written to 'content' placeholder
    	ResourceUtils.insertText(contentPlace, "content",  sb.toString(), HARVEST_SHOW_TEMPLATE, logger); 
        
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
            } else {
            	logger.warning("No alert placeholder found in template '" + HARVEST_SHOW_TEMPLATE + "'" );
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
            } else {
            	logger.warning("No success placeholder found in template '" + HARVEST_SHOW_TEMPLATE + "'" );
            }
        }

        try {
            for (int i = 0; i < templateParts.parts.size(); ++i) {
            	logger.info("Printing out id :" + templateParts.parts.get(i).getId());
                out.write(templateParts.parts.get(i).getBytes());
            }
            out.flush();
            out.close();
        } catch (IOException e) {
        	logger.warning("IOException thrown, but ignored: " + e);        
        }
    }
        
}
