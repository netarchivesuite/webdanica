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

import dk.kb.webdanica.core.datamodel.dao.CriteriaResultsDAO;
import dk.kb.webdanica.core.datamodel.dao.HarvestDAO;
import dk.kb.webdanica.core.interfaces.harvesting.SingleSeedHarvest;
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
        this.hdao = environment.getConfig().getDAOFactory().getHarvestDAO();
        this.cdao = environment.getConfig().getDAOFactory().getCriteriaResultsDAO();
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
        SingleSeedHarvest b = null;
        String harvestName = getHarvestName(pathInfo);
        boolean isError = false;
        if (harvestName == null) {
        	harvestName = "ERROR: unable to extract harvestname from path '" + pathInfo + "'";
        	isError = true;
        	b = SingleSeedHarvest.makeGuiErrorObject(harvestName);
        }
        //resp.sendRedirect(arg0);
        
        if (!isError){ // Try to fetch harvest
        	try {
            	b = hdao.getHarvest(harvestName);
        	} catch (Exception e) {
        	}
        	if (b == null) {
        		harvestName = "ERROR: unable to show harvest with name '" + harvestName + "'. It doesn't exist";
            	isError = true;
            	b = SingleSeedHarvest.makeGuiErrorObject(harvestName);	
        	}
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
            HttpServletResponse resp, SingleSeedHarvest b)
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
        //TemplatePlaceHolder contentPlace = TemplatePlaceBase.getTemplatePlaceHolder("content");
        
/*        
        <h4> Name: <placeholder id="name" /></br>  
        Seed: <placeholder id="seed" /></br>a
        Harvested Time: <placeholder id="harvested_time" /></br>
        CriteriaResults: <placeholder id="criteria_results" /></br>
        Error:<placeholder id="errors" /></br> 
        Successful: <placeholder id="successful" /></br>
        endState:<placeholder id="endState" /></br>
        analysisStatus: <placeholder id="analysisStatus" /></br>
		analysisStatusReason: <placeholder id="analysisStatusReason" /></br>
        </h4>
<h5>Harvested files: </h5>
    <div class="container-fluid">
      <div class="row-fluid">
<placeholder id="harvested_files" />
      </div><!--/row-->
<h5>Fetched urls: </h5>
          <div class="container-fluid">
      <div class="row-fluid">
<placeholder id="harvested_urls" />
      </div><!--/row-->
<h5>SeedReport: </h5>
    <div class="container-fluid">
      <div class="row-fluid">
<placeholder id="seedReport" />
        
        
  */      
        
        TemplatePlaceHolder namePlace = TemplatePlaceBase.getTemplatePlaceHolder("name");
        TemplatePlaceHolder seedPlace = TemplatePlaceBase.getTemplatePlaceHolder("seed");
        TemplatePlaceHolder harvestedTimePlace = TemplatePlaceBase.getTemplatePlaceHolder("harvested_time");
        TemplatePlaceHolder criteriaresultsPlace = TemplatePlaceBase.getTemplatePlaceHolder("criteria_results");
        TemplatePlaceHolder successfullPlace = TemplatePlaceBase.getTemplatePlaceHolder("successful");
        TemplatePlaceHolder endStatePlace = TemplatePlaceBase.getTemplatePlaceHolder("endState");
        TemplatePlaceHolder errorPlace = TemplatePlaceBase.getTemplatePlaceHolder("errors");
        TemplatePlaceHolder harvestedFilesPlace = TemplatePlaceBase.getTemplatePlaceHolder("harvested_files");
        TemplatePlaceHolder harvestedUrlsPlace = TemplatePlaceBase.getTemplatePlaceHolder("harvested_urls");
        TemplatePlaceHolder seedReportPlace = TemplatePlaceBase.getTemplatePlaceHolder("seedReport");
        List<TemplatePlaceBase> placeHolders = new ArrayList<TemplatePlaceBase>();
        placeHolders.add(titlePlace);
        placeHolders.add(appnamePlace);
        placeHolders.add(navbarPlace);
        placeHolders.add(userPlace);
        placeHolders.add(menuPlace);
        placeHolders.add(backPlace);
        placeHolders.add(headingPlace);
        placeHolders.add(alertPlace);
        // add the new placeholders
        placeHolders.add(namePlace);
        placeHolders.add(seedPlace);
        placeHolders.add(harvestedTimePlace);
        placeHolders.add(criteriaresultsPlace);
        placeHolders.add(successfullPlace);
        placeHolders.add(endStatePlace);
        placeHolders.add(errorPlace);
        placeHolders.add(harvestedFilesPlace); // replaces the contentPlace
        placeHolders.add(harvestedUrlsPlace);
        placeHolders.add(seedReportPlace);
        

        TemplateParts templateParts = template.filterTemplate(placeHolders, resp.getCharacterEncoding());
        
        /*
         * Heading.
         */
        String heading = "Information about harvest '" + b.getHarvestName() + "' of seed '" + b.getSeed() + "' :";
        
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
        
        ResourceUtils.insertText(namePlace, "name",  b.getHarvestName(), HARVEST_SHOW_TEMPLATE, logger);
        ResourceUtils.insertText(seedPlace, "seed",  b.getSeed(), HARVEST_SHOW_TEMPLATE, logger);
        
        ResourceUtils.insertText(harvestedTimePlace, "harvested_time", "" + new Date(b.getHarvestedTime()), HARVEST_SHOW_TEMPLATE, logger);
        
        ResourceUtils.insertText(successfullPlace, "successful",  b.isSuccessful() + "", HARVEST_SHOW_TEMPLATE, logger);
        String error = "No errors";
        if (b.getErrMsg() != null && !b.getErrMsg().isEmpty()) {
        	error = b.getErrMsg();
        }
        ResourceUtils.insertText(errorPlace, "errors",  error, HARVEST_SHOW_TEMPLATE, logger);
        ResourceUtils.insertText(endStatePlace, "endState",  b.getFinalState() + "", HARVEST_SHOW_TEMPLATE, logger);

        
        long critCount = 0;
        try {
            critCount = cdao.getCountByHarvest(b.getHarvestName());
        } catch (Exception e) {
        	logger.log(Level.WARNING, "Unable to retrieve number of critresults for this harvest", e);
        }

        String linkToCriteriaresults = "No criteriaresults found for this harvest";
        if (critCount > 0) {
        	linkToCriteriaresults = "<a href=\"" + environment.getCriteriaResultsPath() + b.getHarvestName() + "/\">" + critCount + "</a>";
        }
        ResourceUtils.insertText(criteriaresultsPlace, "criteria_results",  linkToCriteriaresults, HARVEST_SHOW_TEMPLATE, logger);
        
        // printing out the harvested files
        StringBuilder sb = new StringBuilder();
        sb.append("<pre>\r\n");

        for (String listElement: b.getFiles()) {
    		sb.append(listElement);
    		sb.append("\r\n");
    	}
        sb.append("</pre>\r\n");
        
    	ResourceUtils.insertText(harvestedFilesPlace, "harvested_files",  sb.toString(), HARVEST_SHOW_TEMPLATE, logger); 
    	// handling the fetched_urls
    	StringBuilder sbFetchedUrls = new StringBuilder();
    	if (b.getFetchedUrls() != null && !b.getFetchedUrls().isEmpty()) {
    		sbFetchedUrls.append("<pre>\r\n");
            for (String listElement: b.getFetchedUrls()) {
            	sbFetchedUrls.append(listElement);
            	sbFetchedUrls.append("\r\n");
        	}
            sbFetchedUrls.append("</pre>\r\n");
    	} else {
    		sbFetchedUrls.append("<pre>\r\n");
    		sbFetchedUrls.append("None found in database");
    		sbFetchedUrls.append("</pre>\r\n");
    	}
    	
    	ResourceUtils.insertText(harvestedUrlsPlace, "harvested_urls",  sbFetchedUrls.toString(), HARVEST_SHOW_TEMPLATE, logger);
    	
    	// handling the seedReport
    	StringBuilder sbSeedReport = new StringBuilder();
    	if (b.getReports() != null && b.getReports().getSeedReport() != null) {
    		sbSeedReport.append("<pre>\r\n");
    		sbSeedReport.append(b.getReports().getSeedReport().getReportAsString());
    		sbSeedReport.append("</pre>\r\n");
    	} else {
    		sbSeedReport.append("<pre>\r\n");
    		sbSeedReport.append("No Seedreport found for harvest");
    		sbSeedReport.append("</pre>\r\n");
    	}
    	ResourceUtils.insertText(seedReportPlace, "seedReport",  sbSeedReport.toString(), HARVEST_SHOW_TEMPLATE, logger);
  
        try {
            for (int i = 0; i < templateParts.parts.size(); ++i) {
                out.write(templateParts.parts.get(i).getBytes());
            }
            out.flush();
            out.close();
        } catch (IOException e) {
        	logger.warning("IOException thrown, but ignored: " + e);        
        } catch (Throwable e) {
        	String errMsg = "Exception thrown during rendering of harvest:  " + b;
        	CommonResource.show_error(errMsg, resp, environment);
        }
    }
        
}
