package dk.kb.webdanica.webapp.resources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.jwat.common.Base64;

import com.antiaction.common.filter.Caching;
import com.antiaction.common.html.HtmlEntity;
import com.antiaction.common.templateengine.Template;
import com.antiaction.common.templateengine.TemplateParts;
import com.antiaction.common.templateengine.TemplatePlaceBase;
import com.antiaction.common.templateengine.TemplatePlaceHolder;
import com.antiaction.common.templateengine.TemplatePlaceTag;

import dk.kb.webdanica.core.datamodel.DanicaStatus;
import dk.kb.webdanica.core.datamodel.Seed;
import dk.kb.webdanica.core.datamodel.Status;
import dk.kb.webdanica.core.datamodel.criteria.CriteriaUtils;
import dk.kb.webdanica.core.datamodel.criteria.SingleCriteriaResult;
import dk.kb.webdanica.core.datamodel.dao.CriteriaResultsDAO;
import dk.kb.webdanica.core.datamodel.dao.HarvestDAO;
import dk.kb.webdanica.core.datamodel.dao.SeedsDAO;
import dk.kb.webdanica.webapp.Constants;
import dk.kb.webdanica.webapp.Environment;
import dk.kb.webdanica.webapp.MenuItem;
import dk.kb.webdanica.webapp.Navbar;
import dk.kb.webdanica.webapp.Pagination;
import dk.kb.webdanica.webapp.Servlet;
import dk.kb.webdanica.webapp.User;
import dk.netarkivet.common.utils.I18n;
import dk.netarkivet.common.webinterface.HTMLUtils;

public class SeedsResource implements ResourceAbstract {

	private static final Logger logger = Logger.getLogger(SeedsResource.class.getName());

	protected static final int A_ACCEPT = 1;

    protected static final int A_REJECT = 2;

    protected static final int A_DELETE = 3;
	
    private Environment environment;

    protected int R_STATUS_LIST = -1;

    protected int R_STATUS_LIST_ID = -1;

    protected int R_STATUS_LIST_ID_DUMP = -1;

    protected int R_URL_WARC_DOWNLOAD = -1;
    protected int R_STATUS_SEED_SHOW = -1;

    public static final String SEED_PATH = "/seed/";
    
    public static final String SEEDS_PATH = "/seeds/";
    
    public static final String SEEDS_NUMERIC_PATH = "/seeds/<numeric>/";
    
    public static final String SEEDS_NUMERIC_DUMP_PATH = "/seeds/<numeric>/dump/";
    
    private static final String SEED_SHOW_TEMPLATE = "seed_master.html";
	
    
    @Override
    public void resources_init(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void resources_add(ResourceManagerAbstract resourceManager) {
        R_STATUS_LIST = resourceManager.resource_add(this, SEEDS_PATH, 
        		environment.getResourcesMap().getResourceByPath(SEEDS_PATH).isSecure()); 
     
        R_STATUS_LIST_ID = resourceManager.resource_add(this, SEEDS_NUMERIC_PATH, 
        		environment.getResourcesMap().getResourceByPath(SEEDS_NUMERIC_PATH).isSecure());
        
        //R_STATUS_LIST_ID_DUMP = resourceManager.resource_add(this, "/seeds/<numeric>/dump/<numeric>/", true);
        R_STATUS_LIST_ID_DUMP = resourceManager.resource_add(this, SEEDS_NUMERIC_DUMP_PATH, 
        		environment.getResourcesMap().getResourceByPath(SEEDS_NUMERIC_DUMP_PATH).isSecure());
        R_STATUS_SEED_SHOW = resourceManager.resource_add(this, SEED_PATH,
        		environment.getResourcesMap().getResourceByPath(SEED_PATH).isSecure()); 
        
        //R_URL_WARC_DOWNLOAD = resourceManager.resource_add(this, "/url/warc/<numeric>/", true);
    }

    @Override
    public void resource_service(ServletContext servletContext, User dab_user,
    		HttpServletRequest req, HttpServletResponse resp,
    		int resource_id, List<Integer> numerics, String pathInfo) throws IOException {
    	
        if (resource_id == R_STATUS_LIST || resource_id == R_STATUS_LIST_ID) {
        	//
            urls_list(dab_user, req, resp, numerics);
        } else if (resource_id == R_STATUS_SEED_SHOW) {
        	SeedRequest seedRequest = SeedRequest.getUrlFromPathinfo(pathInfo, SEED_PATH);
        	url_show(dab_user, req, resp, seedRequest);
        	
        } else if (resource_id == R_STATUS_LIST_ID_DUMP) {
            // Dump all resources in the given state to screen
            // text/plain; charset=utf-8
            urls_list_dump(dab_user, req, resp, numerics);
        } 
    }

    private void changeState(SeedRequest seedRequest, SeedsDAO dao) throws Exception {
    	Seed s = dao.getSeed(seedRequest.getUrl());
    	Status old = s.getStatus();
    	s.setStatus(seedRequest.getNewState());
    	s.setStatusReason("Changed from status '" + old + "' to status '" + seedRequest.getNewState() + "' by user-request "); 
	    dao.updateSeed(s);
    }

	private void url_show(User dab_user, HttpServletRequest req,
            HttpServletResponse resp, SeedRequest sr) throws IOException  {
    	SeedsDAO dao = Servlet.environment.getConfig().getDAOFactory().getSeedsDAO();
    	// Retrieving UUID or maybe name from pathinfo instead of String equivalent of numerics
    	Seed seedToShow = null;
    	try {
	        if (!dao.existsUrl(sr.getUrl())) {
	        	String error = "The given url '" + sr.getUrl() + "' was not found in the database"; 
	        	CommonResource.show_error(error, resp, environment);
	        	return;
	        } else {
	        	if (sr.getNewState() != null) {
	        		changeState(sr, dao);
	        		resp.sendRedirect(Servlet.environment.getSeedsPath()); // redirect to main seedspage
	        		return;
	        	} else {
	        		try {
	        	        seedToShow = dao.getSeed(sr.getUrl());
	                } catch (Exception e) {
	        	        e.printStackTrace();
	        	        String error = "The given url '" + sr.getUrl() + "' could not be retrived from the database due to this error: " + e; 
	        	        CommonResource.show_error(error, resp, environment);
	        	        return;
	                }
	        	}
	        }
        } catch (Exception e) {
	        String errMsg = "Unexpected exception thrown:" + e;
        	logger.log(Level.WARNING, errMsg, e);
        	CommonResource.show_error(errMsg, resp, environment);
        	return;
        	
        }
    	if (seedToShow == null) {
    		String errMsg = "Should not happen. No seed found in database";
        	logger.log(Level.WARNING, errMsg);
        	CommonResource.show_error(errMsg, resp, environment);
        	return;
    	}
    	// Set up the page for showing the seed page
    	
    	ServletOutputStream out = resp.getOutputStream();
        resp.setContentType("text/html; charset=utf-8");
        String errorStr = null;
        String successStr = null;
        Caching.caching_disable_headers(resp);
        String templateName = SEED_SHOW_TEMPLATE;
        Template template = environment.getTemplateMaster().getTemplate(templateName);

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
      <h4>URL: <placeholder id="url" /><br>
  RedirectedUrl: <placeholder id="redirected_url" /></br>  
  Domain: <placeholder id="domain" /></br>
  Hostname: <placeholder id="host" /></br>
  TLD: <placeholder id="tld" /></br>
  Status: <placeholder id="status" /> </br> 
  StatusReason: <placeholder id="status_reason" /></br>
  DanicaStatus: <placeholder id="danica_status" /></br>
  InsertedDate: <placeholder id="inserted_time" /></br>
  UpdatedTime: <placeholder id="updated_time" /></br>
  Exported: <placeholder id="exported" /></br>
  <br>
  Harvests: <placeholder id="harvests" /></br>
  CriteriaResult: <placeholder id="criteriaresults" /></br>
*/
        TemplatePlaceHolder urlPlace = TemplatePlaceBase.getTemplatePlaceHolder("url");
        TemplatePlaceHolder redirectedUrlPlace = TemplatePlaceBase.getTemplatePlaceHolder("redirected_url");
        TemplatePlaceHolder domainPlace = TemplatePlaceBase.getTemplatePlaceHolder("domain");
        TemplatePlaceHolder hostPlace = TemplatePlaceBase.getTemplatePlaceHolder("host");
        TemplatePlaceHolder tldPlace = TemplatePlaceBase.getTemplatePlaceHolder("tld");
        
        TemplatePlaceHolder statusPlace = TemplatePlaceBase.getTemplatePlaceHolder("status");
        TemplatePlaceHolder statusReasonPlace = TemplatePlaceBase.getTemplatePlaceHolder("status_reason");
        TemplatePlaceHolder danicastatusPlace = TemplatePlaceBase.getTemplatePlaceHolder("danica_status");
        TemplatePlaceHolder insertedTimePlace = TemplatePlaceBase.getTemplatePlaceHolder("inserted_time");
        TemplatePlaceHolder updatedTimePlace = TemplatePlaceBase.getTemplatePlaceHolder("updated_time");
        TemplatePlaceHolder exportedPlace = TemplatePlaceBase.getTemplatePlaceHolder("exported");
        TemplatePlaceHolder harvestsPlace = TemplatePlaceBase.getTemplatePlaceHolder("harvests");
        TemplatePlaceHolder criteriaResultsPlace = TemplatePlaceBase.getTemplatePlaceHolder("criteriaresults");
        TemplatePlaceHolder linksPlace = TemplatePlaceBase.getTemplatePlaceHolder("links");
        
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
        placeHolders.add(urlPlace);
        placeHolders.add(redirectedUrlPlace);
        placeHolders.add(domainPlace);
        placeHolders.add(hostPlace);
        placeHolders.add(tldPlace);
        placeHolders.add(statusPlace);
        placeHolders.add(statusReasonPlace);
        placeHolders.add(danicastatusPlace);
        placeHolders.add(insertedTimePlace);
        placeHolders.add(updatedTimePlace);
        placeHolders.add(exportedPlace);
        placeHolders.add(harvestsPlace);
        placeHolders.add(criteriaResultsPlace);
        placeHolders.add(linksPlace);
        
        TemplateParts templateParts = template.filterTemplate(placeHolders, resp.getCharacterEncoding());
        
        
        /*
         * Heading.
         */
        String heading = "Details about seed '" + seedToShow.getUrl() + "': ";
        
        /*
         * Places.
         */

        if (titlePlace != null) {
            titlePlace.setText(HtmlEntity.encodeHtmlEntities(dk.kb.webdanica.webapp.Constants.WEBAPP_NAME).toString());
        }

        if (appnamePlace != null) {
            appnamePlace.setText(HtmlEntity.encodeHtmlEntities(dk.kb.webdanica.webapp.Constants.WEBAPP_NAME 
            		+ dk.kb.webdanica.webapp.Constants.SPACE + environment.getVersion()).toString());
        }

        if (navbarPlace != null) {
            navbarPlace.setText(Navbar.getNavbar(Navbar.N_URL_SHOW));
        }

        if (userPlace != null) {
            userPlace.setText(Navbar.getUserHref(dab_user));
        } 

        if (backPlace != null) {
        	backPlace.setText("<a href=\"" 
        			+ Servlet.environment.getSeedsPath() + seedToShow.getStatus().ordinal() + "/"
        			+ "\" class=\"btn btn-primary\"><i class=\"icon-white icon-list\"></i> Tilbage til oversigten</a>");
        } else {
        	logger.warning("No back´placeholder found in template '" + templateName+ "'" );
        }

        if (headingPlace != null) {
            headingPlace.setText(heading);
        } else {
        	logger.warning("No heading´ placeholder found in template '" + templateName + "'" );
        }
        
        String redirectedUrlText = seedToShow.getRedirectedUrl();
        if (redirectedUrlText == null || redirectedUrlText.isEmpty()) {
        	redirectedUrlText = "N/A";
        }
        Set<String> linkSet = new HashSet<String>();
        Status state = seedToShow.getStatus();
        if (state.equals(Status.ANALYSIS_FAILURE) || state.equals(Status.HARVESTING_FAILED) || state.equals(Status.HARVESTING_IN_PROGRESS) || state.equals(Status.HARVESTING_FINISHED)) {
        	String links = environment.getSeedPath() + HTMLUtils.encode(CriteriaUtils.toBase64(seedToShow.getUrl())) + "/" + Status.READY_FOR_HARVESTING.ordinal() + "/";
     		String retryHarvestLink = "<A href=\"" + links + "\"> Retry harvesting</A>";
     		linkSet.add(retryHarvestLink);
        }
        if (!state.equals(Status.NEW)) {
        	String links = environment.getSeedPath() + HTMLUtils.encode(CriteriaUtils.toBase64(seedToShow.getUrl())) + "/" + Status.NEW.ordinal() + "/";
    		String retryLink = "<A href=\"" +  links + "\"> Reset seed to status NEW</A>";
    		linkSet.add(retryLink);
        }
        DanicaStatus dState = seedToShow.getDanicaStatus();
        if (!dState.equals(DanicaStatus.YES)) {
        	String links = environment.getSeedPath() + HTMLUtils.encode(CriteriaUtils.toBase64(seedToShow.getUrl())) + "/" + 100 + "/";
    		String retryLink = "<A href=\"" +  links + "\"> Accept seed as Danica - LINK NOT WORKING YET</A>";
    		linkSet.add(retryLink);
        }
        if (!dState.equals(DanicaStatus.NO)) {
        	String links = environment.getSeedPath() + HTMLUtils.encode(CriteriaUtils.toBase64(seedToShow.getUrl())) + "/" + 101 + "/";
    		String retryLink = "<A href=\"" +  links + "\"> Reject seed as Danica - LINK NOT WORKING YET</A>";
    		linkSet.add(retryLink);
        }
        ResourceUtils.insertText(linksPlace, "links",  StringUtils.join(linkSet, "&nbsp;&nbsp;"), templateName, logger);
        ResourceUtils.insertText(urlPlace, "url",  seedToShow.getUrl(), templateName, logger);
        ResourceUtils.insertText(redirectedUrlPlace, "redirected_url",  redirectedUrlText, templateName, logger);
        ResourceUtils.insertText(tldPlace, "tld",  seedToShow.getTld() + "", templateName, logger);
        ResourceUtils.insertText(hostPlace, "host",  seedToShow.getHostname()+ "", templateName, logger);
        ResourceUtils.insertText(domainPlace, "domain",  seedToShow.getDomain()+ "", templateName, logger);
        ResourceUtils.insertText(statusPlace, "status",  seedToShow.getStatus() + "", templateName, logger);
        ResourceUtils.insertText(statusReasonPlace, "status_reason",  seedToShow.getStatusReason() + "", templateName, logger);
        ResourceUtils.insertText(danicastatusPlace, "danica_status",  seedToShow.getDanicaStatus() + "", templateName, logger);
        ResourceUtils.insertText(insertedTimePlace, "inserted_time",  ResourceUtils.printDate(seedToShow.getInsertedTime()), templateName, logger);
        ResourceUtils.insertText(updatedTimePlace, "updated_time",  ResourceUtils.printDate(seedToShow.getUpdatedTime()), templateName, logger);
        ResourceUtils.insertText(exportedPlace, "exported",  "" + seedToShow.showExportedState(), templateName, logger);
        String harvestsString = "N/A";
        String criteriaString = "N/A";
        StringBuilder sbCriteriaResults = new StringBuilder();
        try {
        	HarvestDAO hdao = this.environment.getConfig().getDAOFactory().getHarvestDAO();
        	long hcount = hdao.getCountWithSeedurl(seedToShow.getUrl());
        	if (hcount > 0) {
        		String link = environment.getHarvestsPath() + HTMLUtils.encode(CriteriaUtils.toBase64(seedToShow.getUrl())) + "/";
        		harvestsString = "<A href=\"" + link + "\">" + hcount + " harvests</A>";
        	} else {
        		harvestsString = "0 harvests";
        	}
        	CriteriaResultsDAO cdao =  this.environment.getConfig().getDAOFactory().getCriteriaResultsDAO();
        	List<SingleCriteriaResult> cresults = cdao.getResultsByUrl(seedToShow.getUrl());
        	Set<String> clinks = new HashSet<String>();
        	long ccount = cresults.size();
        	sbCriteriaResults.append(ccount + " results");
        	
        	if (!cresults.isEmpty()) {
        		for (SingleCriteriaResult c: cresults) {
        			String linkText = "result from harvest '" + c.harvestName + "'";
        			clinks.add(CriteriaResultsResource.createLink(environment, c.harvestName, c.url, linkText));
        		}
        		StringBuilder sb = new StringBuilder();
                sb.append("</br><pre>\r\n");
                for (String listElement: clinks ) {
            		sb.append(listElement);
            		sb.append("\r\n");
            	}
                sb.append("</pre>\r\n");
                sbCriteriaResults.append(sb);
                criteriaString = sbCriteriaResults.toString();
        	}
        } catch (Throwable e) {
        	String errMsg = "Unexpected exception thrown:" + e;
        	logger.log(Level.WARNING, errMsg, e);
        	CommonResource.show_error(errMsg, resp, environment);
        	return;
        }
        
        ResourceUtils.insertText(harvestsPlace, "harvests",  harvestsString, templateName, logger);
        ResourceUtils.insertText(criteriaResultsPlace, "criteriaresults",  criteriaString, templateName, logger);
        StringBuilder sb = new StringBuilder();
    	ResourceUtils.insertText(contentPlace, "content",  sb.toString(), templateName, logger);
        
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
            	logger.warning("No alert placeholder found in template '" + templateName + "'" );
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
            	logger.warning("No success placeholder found in template '" + templateName + "'" );
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
   }

	
	private void urls_list_dump(User dab_user, HttpServletRequest req,
            HttpServletResponse resp, List<Integer> numerics) throws IOException {
        //UrlRecords urlRecordsInstance = UrlRecords.getInstance(environment.dataSource);
    	SeedsDAO dao = Servlet.environment.getConfig().getDAOFactory().getSeedsDAO();
    	
        int status = 0; //Ordinal for Status.NEW
        if (numerics.size() >= 1) {
            status = numerics.get(0); 
        }
        
        int online_status = 0; // default = alle (alternatives: only_only, offline-only
        if (numerics.size() == 2) {
            online_status = numerics.get(1);
        }
        
        ServletOutputStream out = resp.getOutputStream();
        resp.setContentType("text/plain; charset=utf-8");
        //resp.setHeader("content-disposition", "attachment; filename=\"url_list_status_"+ status + "_" + online_status_text + ".txt\"");
        resp.setHeader("content-disposition", "attachment; filename=\"url_list_status_"+ status + ".txt\"");
        //logger.info("Using encoding in response: " + resp.getCharacterEncoding());

        // TODO this does not scale
        // Make an iterator
        List<Seed> urlRecords = null;
        try {
            // This needs to be further limited (e.g. by domain, and tld)
            urlRecords = dao.getSeeds(Status.fromOrdinal(online_status), 10000);  
        } catch (Exception e) {
        	String errMsg = "Unexpected exception thrown:" + e;
        	logger.log(Level.WARNING, errMsg, e);
        	CommonResource.show_error(errMsg, resp, environment);
        	return;
        }

  
        StringBuilder sb = new StringBuilder();
        sb.append("##\r\n");
        sb.append("## Liste over alle " + urlRecords.size() + " seeds i status "
                + status + "\r\n");
        sb.append("##\r\n");
        
        for (Seed rec: urlRecords) {
            sb.append(rec.getUrl());
            sb.append("\r\n");
        }

        try {
            out.write(sb.toString().getBytes("utf-8"));
            out.flush();
            out.close();
        } catch (IOException e) {
        	logger.warning("IOException thrown: " + e);
        }
    }
    

    public void urls_list(User dab_user, HttpServletRequest req,
            HttpServletResponse resp, List<Integer> numerics)
            throws IOException {
        String errorStr = null;
        String successStr = null;
        SeedsDAO sdao = Servlet.environment.getConfig().getDAOFactory().getSeedsDAO();
        
        int status = 0; //Default state shown: Status.NEW
        if (numerics.size() == 1) {
            status = numerics.get(0);
        }

        String pageStr = req.getParameter("page");
        int page = 1;
        if (pageStr != null && pageStr.length() > 0) {
            try {
                page = Integer.parseInt(pageStr);
            } catch (NumberFormatException e) {
            	e.printStackTrace();
            }
        }
        String itemsperpageStr = req.getParameter("itemsperpage");
        boolean bShowAll = false;
        int itemsPerPage = 25;
        if (itemsperpageStr != null && itemsperpageStr.length() > 0) {
        	try {
        		itemsPerPage = Integer.parseInt(itemsperpageStr);
        	} catch (NumberFormatException e) {
        		logger.warning("The given value of 'itemsperpage': '" + itemsperpageStr
        				+ "' is not a valid integer!. Using the default: 25"); 
        		itemsPerPage = 25;
        		itemsperpageStr = "25";
        	}
        }

        String actionStr = req.getParameter("action");
        String urlIdStr = req.getParameter("url_id");

        
        
        boolean bDecidePerm = false;
        boolean bDeletePerm = false;
        
        
        ServletOutputStream out = resp.getOutputStream();
        resp.setContentType("text/html; charset=utf-8");

        Caching.caching_disable_headers(resp);

        Template template = environment.getTemplateMaster().getTemplate("urls_list.html");

        TemplatePlaceHolder titlePlace = TemplatePlaceBase.getTemplatePlaceHolder("title");
        TemplatePlaceHolder appnamePlace = TemplatePlaceBase.getTemplatePlaceHolder("appname");
        TemplatePlaceHolder navbarPlace = TemplatePlaceBase.getTemplatePlaceHolder("navbar");
        TemplatePlaceHolder userPlace = TemplatePlaceBase.getTemplatePlaceHolder("user");
        TemplatePlaceHolder menuPlace = TemplatePlaceBase.getTemplatePlaceHolder("menu");
        TemplatePlaceHolder statemenuPlace = TemplatePlaceBase.getTemplatePlaceHolder("state_menu");
        TemplatePlaceHolder headingPlace = TemplatePlaceBase.getTemplatePlaceHolder("heading");
        TemplatePlaceHolder actionButtonsPlace = TemplatePlaceBase.getTemplatePlaceHolder("action_buttons");
        TemplatePlaceHolder paginationPlace = TemplatePlaceBase.getTemplatePlaceHolder("pagination");
        TemplatePlaceHolder pagination2Place = TemplatePlaceBase.getTemplatePlaceHolder("pagination2");
        TemplatePlaceTag myformTag = TemplatePlaceTag.getInstance( "form", "myform" );
        TemplatePlaceHolder statusPlace = TemplatePlaceBase.getTemplatePlaceHolder("status");
        TemplatePlaceHolder dumpPlace = TemplatePlaceBase.getTemplatePlaceHolder("dump");
        TemplatePlaceHolder alertPlace = TemplatePlaceBase.getTemplatePlaceHolder("alert");

        List<TemplatePlaceBase> placeHolders = new ArrayList<TemplatePlaceBase>();
        placeHolders.add(titlePlace);
        placeHolders.add(appnamePlace);
        placeHolders.add(navbarPlace);
        placeHolders.add(userPlace);
        placeHolders.add(menuPlace);
        placeHolders.add(statemenuPlace);
        placeHolders.add(headingPlace);
        placeHolders.add(actionButtonsPlace);
        placeHolders.add(paginationPlace);
        placeHolders.add(pagination2Place);
        // placeHolders.add( myformTag );
        placeHolders.add(statusPlace);
        placeHolders.add(dumpPlace);
        placeHolders.add(alertPlace);

        TemplateParts templateParts = template.filterTemplate(placeHolders, resp.getCharacterEncoding());

        boolean bShowReason = false;
        boolean bShowPid = true;
        boolean bShowAcceptReject = false;
        boolean bShowArchiveUrl = false;

        StringBuilder statemenuSb = new StringBuilder();
        String heading = buildStatemenu(statemenuSb, status, sdao);

        /*
         * Menu.
         */

        StringBuilder menuSb = new StringBuilder();
/*
        if (dab_user.hasAnyPermission(URL_ADD_PERMISSION)) {
        	menuSb.append("<div class=\"well sidebar-nav\">\n");
        	menuSb.append("<ul class=\"nav nav-list\">\n");
        	menuSb.append("<li class=\"nav-header\">Valgmuligheder</li>\n");

            menuSb.append("<li id=\"state_1\"");
            menuSb.append("><a href=\"");
            menuSb.append(Servlet.environment.seedsPath);
            menuSb.append("add/\">Opret URL</a></li>\n");

            menuSb.append("<li id=\"state_2\"");
            menuSb.append("><a href=\"");
            menuSb.append(Servlet.environment.seedsPath);
            menuSb.append("upload/\">Upload fil med URL'er</a></li>\n");

        	menuSb.append("</ul>\n");
        	menuSb.append("</div><!--/.well -->\n");
        }
*/
        
        /*
         * Action buttons.
         */

        StringBuilder actionButtonsSb = new StringBuilder();

        if (bDeletePerm || (bShowAcceptReject && bDecidePerm)) {
            actionButtonsSb.append("<a href=\"#\" class=\"btn\" onClick=\"select_all(document.myform.url_check_list); return false;\">Vælg alle</a>\n");
            actionButtonsSb.append("<a href=\"#\" class=\"btn\" onClick=\"deselect_all(document.myform.url_check_list); return false;\">Fravælg alle</a>\n");
            if (bShowAcceptReject && bDecidePerm) {
                actionButtonsSb.append("<button type=\"submit\" name=\"submitaction\" value=\"accept\" class=\"btn btn-success\"><i class=\"icon-white icon-thumbs-up\"></i> Godkend</button>\n");
                actionButtonsSb.append("<button type=\"submit\" name=\"submitaction\" value=\"reject\" class=\"btn btn-inverse\"><i class=\"icon-white icon-thumbs-down\"></i> Afvis</button>\n");
            }
            if (bDeletePerm) {
                actionButtonsSb.append("<button type=\"submit\" name=\"submitaction\" value=\"delete\" class=\"btn btn-danger\"><i class=\"icon-white icon-trash\"></i> Slet</button>\n");
            }
        }

        /*
         * Urls.
         */

        StringBuilder urlListSb = new StringBuilder();

        // FIXME better handling
        List<Seed> urlRecords = new ArrayList<Seed>();
        Status wantedStatus = Status.fromOrdinal(status);
        try {
            urlRecords = sdao.getSeeds(wantedStatus, 10000);
        } catch (Exception e) {
            logger.warning("Exception on retrieving max 10000 seeds with status " + wantedStatus + ": " + e); 
        }
        List<Seed> urlRecordsFiltered = urlRecords;
        Seed urlRecord;
        
        
        // Implementing paging with cassandra
        // https://datastax.github.io/java-driver/manual/paging/
        if (page < 1) {
        	logger.warning("Got negative pagenr '" +  page + "'. Changing it to page=1");
            page = 1;
            
        }
        if (itemsPerPage < 1) {
        	int defaultItemsPerPage = environment.getDefaultItemsPerPage();
        	logger.warning("Got negative itemsPerPage '" +  itemsPerPage + "'. Changing it to itemsPerPage=" + defaultItemsPerPage);
            itemsPerPage = defaultItemsPerPage;
            
        }
        int items = urlRecordsFiltered.size();
        int pages = Pagination.getPages(items, itemsPerPage);
        if (page > pages) {
        	logger.warning("Asked for page " + page + ", but we only have " + pages + ". Set page to maxpage");
            page = pages;
        }
        int fItem = (page - 1) * itemsPerPage;
        int show = itemsPerPage;

        
        urlListSb.append("<table class=\"table table-striped\">\n");
        urlListSb.append("  <thead>\n");
        urlListSb.append("    <tr>\n");
  
        urlListSb.append("      <th>url</th>\n");
        if (status == Status.REJECTED.ordinal()) {
            urlListSb.append("      <th>grund</th>\n");
            bShowReason = true;
        }
        if (bShowArchiveUrl) {
            urlListSb.append("      <th>arkiv-url</th>\n");
        }
  

        urlListSb.append("    </tr>\n");
        urlListSb.append("  </thead>\n");
        urlListSb.append("  <tbody>\n");

        while (fItem < urlRecordsFiltered.size() && show > 0) {
            urlRecord = urlRecordsFiltered.get(fItem);
            urlListSb.append("<tr>");
            urlListSb.append("<td>");
            urlListSb.append("<a href=\"");
            //urlListSb.append(Servlet.environment.getSeedsPath());
            
            urlListSb.append(urlRecord.getUrl());
            urlListSb.append("\">");
            urlListSb.append(makeEllipsis(urlRecord.getUrl(), 40)); // TODO make a setting
            urlListSb.append("</a>");
            // Add link to show details about seed
            String base64Encoded = Base64.encodeString(urlRecord.getUrl());
        	if (base64Encoded == null) {
        		logger.warning("base64 encoding of url '" +  urlRecord.getUrl() + "' gives null");
        		base64Encoded = urlRecord.getUrl();
        	}
        	 String linkToShowPage = Servlet.environment.getSeedPath() + HTMLUtils.encode(base64Encoded) + "/\"";
        	urlListSb.append("(<a href=\"" + linkToShowPage + ">Show details</a>)");
            urlListSb.append("</td>");
            urlListSb.append("<td>");
            
            if (bShowReason) {
            	urlListSb.append("<b>" + urlRecord.getStatusReason() + "</b>");	
            }
            urlListSb.append("</td>");
            urlListSb.append("</tr>\n");
            ++fItem;
            --show;
        }

        urlListSb.append("  </tbody>\n");
        urlListSb.append("</table>\n");

        /*
         * Dump.
         */

        StringBuilder dumpSb = new StringBuilder();
        
        /*
         * Places.
         */

        if (titlePlace != null) {
            titlePlace.setText(HtmlEntity.encodeHtmlEntities(Constants.WEBAPP_NAME).toString());
        }

        if (appnamePlace != null) {
            appnamePlace.setText(HtmlEntity.encodeHtmlEntities(Constants.WEBAPP_NAME + Constants.SPACE + environment.getVersion()).toString());
        }

        if (navbarPlace != null) {
            navbarPlace.setText(Navbar.getNavbar(Navbar.N_URLS));
        }

        if (userPlace != null) {
            userPlace.setText(Navbar.getUserHref(dab_user));
        }

        if (menuPlace != null) {
            menuPlace.setText(menuSb.toString());
        }

        if (statemenuPlace != null) {
            statemenuPlace.setText(statemenuSb.toString());
        }

        if (headingPlace != null) {
            headingPlace.setText(heading);
        }

        if (actionButtonsPlace != null) {
            actionButtonsPlace.setText(actionButtonsSb.toString());
        }

        if (paginationPlace != null) {
            paginationPlace.setText(Pagination.getPagination(page, itemsPerPage, pages, bShowAll));
        }

        if (pagination2Place != null) {
            pagination2Place.setText(Pagination.getPagination(page, itemsPerPage, pages, bShowAll));
        }

        if (dumpPlace != null) {
            dumpPlace.setText(dumpSb.toString());
        }


        if ( myformTag != null && myformTag.htmlItem != null ) {
        	myformTag.htmlItem.setAttribute( "action", "?page=" + page + "&itemsperpage=" + itemsperpageStr );
        }

        /*
         * if ( contentPlace != null ) { contentPlace.setText( sb.toString() );
         * }
         */
        if (statusPlace != null) {
            statusPlace.setText(urlListSb.toString());
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
        	logger.log(Level.WARNING, "Unexpected exception thrown", e);
        }

    }

    /**
     * Truncate a string to max-length plus " .." if string exceeds maxlength.
     * @param orgString
     *            A string
     * @param maxLength
     *            The max length the string must fill
     * @return the original String if the string is less than maxLength long,
     *         otherwise the substring of length maxLength of the original
     *         String followed by " .."
     */
    private String makeEllipsis(final String orgString, final int maxLength) {
        String resultString = orgString;
        if (orgString.length() > maxLength) {
            resultString = orgString.substring(0, maxLength - 1) + " ..";
        }
        return resultString;
    }

    public static String buildStatemenu(StringBuilder statemenuSb, int status, SeedsDAO dao) {
        /*
         * State menu.
         */

        List<MenuItem> menuStatesArr =null;
        try {
            menuStatesArr = makemenuArray(dao);
        } catch (Exception e) {
        	logger.log(Level.WARNING, "Unexpected exception thrown", e);
        }

        String heading = "N/A";

        for (MenuItem item:  menuStatesArr) {
            // javascript:switchToState(0)
            statemenuSb.append("<li id=\"state_");
            statemenuSb.append(item.getOrdinalState()); 
            statemenuSb.append("\"");
            if (status == item.getOrdinalState()) {
                heading =  item.getLongHeaderDescription();
                statemenuSb.append(" class=\"active\"");
            }
            statemenuSb.append("><a href=\"");
            statemenuSb.append(Servlet.environment.getSeedsPath());
            statemenuSb.append(item.getOrdinalState());
            statemenuSb.append("/\">");
            statemenuSb.append(item.getShortHeaderDescription());
            statemenuSb.append(" (");
            statemenuSb.append(item.getCount());
            statemenuSb.append(")</a></li>");
        }

        return heading;
    }

	private static List<MenuItem> makemenuArray(SeedsDAO dao) throws Exception {
		List<MenuItem> result = new ArrayList<MenuItem>();
		I18n i18n = new I18n(dk.kb.webdanica.core.Constants.WEBDANICA_TRANSLATION_BUNDLE);
		Locale locDa = new Locale("da");
		for (int i=0; i <= Status.getMaxValidOrdinal(); i++) {
			Long count = dao.getSeedsCount(Status.fromOrdinal(i));
			result.add(new MenuItem(i, count, locDa, i18n));
		}
	    return result;
    }

}
