package dk.kb.webdanica.webapp.resources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
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

import dk.kb.webdanica.core.datamodel.IngestLog;
import dk.kb.webdanica.core.datamodel.dao.IngestLogDAO;
import dk.kb.webdanica.webapp.Constants;
import dk.kb.webdanica.webapp.Environment;
import dk.kb.webdanica.webapp.Navbar;
import dk.kb.webdanica.webapp.Servlet;
import dk.kb.webdanica.webapp.User;

public class IngestLogResource implements ResourceAbstract {

    private static final Logger logger = Logger.getLogger(IngestLogResource.class.getName());
    
    private static final String INGESTLOG_SHOW_TEMPLATE = "ingestlog_master.html";

    protected int R_INGESTLOG = -1;
    protected int R_INGESTLOG_LIST = -1;
    
    public static final String INGESTLOGS_PATH = "/ingestlogs/";
    public static final String INGESTLOG_PATH = "/ingestlog/";
    private Environment environment;

	private IngestLogDAO dao;
    
    @Override
    public void resources_init(Environment environment) {
        this.environment = environment;
        this.dao = environment.getConfig().getDAOFactory().getIngestLogDAO();
    }

    @Override
    public void resources_add(ResourceManagerAbstract resourceManager) {
        //R_BLACKLIST = resourceManager.resource_add(this, "/blacklist/<string>/", true);
    	R_INGESTLOG_LIST = resourceManager.resource_add(this, INGESTLOGS_PATH, 
        		environment.getResourcesMap().getResourceByPath(INGESTLOGS_PATH).isSecure());
    	R_INGESTLOG = resourceManager.resource_add(this, INGESTLOG_PATH, 
        		environment.getResourcesMap().getResourceByPath(INGESTLOG_PATH).isSecure());
    }

    @Override
    public void resource_service(ServletContext servletContext, User dab_user,
    		HttpServletRequest req, HttpServletResponse resp,
    		int resource_id, List<Integer> numerics, String pathInfo) throws IOException {
    	
        if (resource_id == R_INGESTLOG_LIST) {
            ingestlogs_show(dab_user, req, resp);
        } else if (resource_id == R_INGESTLOG) {
        	IngestLog b = getIngestLogFromPathinfo(INGESTLOG_PATH, pathInfo);
        	if (b== null) {
        		String error = "The given pathInfo '" + pathInfo + "' does not contain a valid identifier to an ingestlog";
        		CommonResource.show_error(error, resp, environment);
        		return;
        	}
        	ingestlog_show(dab_user, req, resp, b);
        }
    }

    private IngestLog getIngestLogFromPathinfo(String ingestlogPath,
            String pathInfo) {
    	IngestLog result = null;
	    if (pathInfo.startsWith(ingestlogPath)) {
	    	String[] pathInfoParts = pathInfo.split(ingestlogPath);
	    	Long id = null;
	    	if (pathInfoParts.length > 1) {
	    		String IdString = pathInfoParts[1]; 
	    		String dateString = IdString.substring(0, IdString.length()-1);
	    		try {
	    			id = Long.parseLong(dateString);
	    			result = dao.readIngestLog(id);
	    		} catch (Throwable e) {
	    			String logMsg = "Either pathinfo '" + pathInfo + "' does not contain a valid identifier for an ingestlog, or some else went wrong";
	    			logger.log(Level.WARNING,logMsg, e);
	    		}
	    	}
	    } 
	    return result;
    }

	private void ingestlogs_show(User dab_user, HttpServletRequest req,
            HttpServletResponse resp) throws IOException {
	    //CommonResource.show_error("ingestlogs_show page not yet implemented", resp, environment);
	        ServletOutputStream out = resp.getOutputStream();
	        resp.setContentType("text/html; charset=utf-8");

	        Caching.caching_disable_headers(resp);
	        String templateName = "ingestlog_list.html";
	        Template template = environment.getTemplateMaster().getTemplate(templateName);

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
	        
            List<Long> logList = null;
            try {
                logList = environment.getConfig().getDAOFactory().getIngestLogDAO().getIngestDates();
            } catch (Exception e) {
            	String logMsg = "Exception occurred during the retrieval of the available ingestlogs";
            	CommonResource.show_error(logMsg, resp, environment, e);
            	return;
            }
            for (Long b: logList) {
                sb.append("<tr>");
                sb.append("<td>");
                sb.append("<a href=\"");
                sb.append(Servlet.environment.getIngestLogPath());
                sb.append(b);
                sb.append("/\">");
                sb.append(new Date(b));
                sb.append("</a>");
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

	        String heading = "Liste over ingestlogs i systemet";

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
	    }
	
	
		public void ingestlog_show(User dab_user, HttpServletRequest req,
            HttpServletResponse resp, IngestLog b)
            throws IOException {
        ServletOutputStream out = resp.getOutputStream();
        resp.setContentType("text/html; charset=utf-8");
        // TODO error text
        String errorStr = null;
        String successStr = null;
        Caching.caching_disable_headers(resp);
        
        String templateName = INGESTLOG_SHOW_TEMPLATE;
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
        
        // my placeholders
        TemplatePlaceHolder insertionDatePlace = TemplatePlaceBase.getTemplatePlaceHolder("insertionDate"); //b.getDate()
        TemplatePlaceHolder filenamePlace = TemplatePlaceBase.getTemplatePlaceHolder("filename"); //b.getFilename()
        TemplatePlaceHolder linecountPlace = TemplatePlaceBase.getTemplatePlaceHolder("linecount"); //b.getLinecount()
        TemplatePlaceHolder insertedcountPlace = TemplatePlaceBase.getTemplatePlaceHolder("insertedcount"); //b.getInsertedcount()
        TemplatePlaceHolder duplicatecountPlace = TemplatePlaceBase.getTemplatePlaceHolder("duplicatecount"); //b.getDuplicatecount()
        TemplatePlaceHolder rejectedcountPlace = TemplatePlaceBase.getTemplatePlaceHolder("rejectedcount"); //b.getRejectedcount()
        TemplatePlaceHolder errorcountPlace = TemplatePlaceBase.getTemplatePlaceHolder("errorcount"); //b.getErrorcount()
             
        TemplatePlaceHolder logsizePlace = TemplatePlaceBase.getTemplatePlaceHolder("logsize");
        
        List<TemplatePlaceBase> placeHolders = new ArrayList<TemplatePlaceBase>();
        placeHolders.add(titlePlace);
        placeHolders.add(appnamePlace);
        placeHolders.add(navbarPlace);
        placeHolders.add(userPlace);
        placeHolders.add(menuPlace);
        placeHolders.add(backPlace);
        placeHolders.add(headingPlace);
        placeHolders.add(alertPlace);
        placeHolders.add(contentPlace); // here the logentries themselves are placed
        // add the new placeholders
        placeHolders.add(insertionDatePlace);
        placeHolders.add(filenamePlace);
        placeHolders.add(linecountPlace);
        placeHolders.add(insertedcountPlace);
        placeHolders.add(duplicatecountPlace);
        placeHolders.add(rejectedcountPlace);
        placeHolders.add(errorcountPlace);
        placeHolders.add(logsizePlace);

        TemplateParts templateParts = template.filterTemplate(placeHolders, resp.getCharacterEncoding());
        
        List<String> logEntries = b.getLogEntries();
        if (logEntries == null) {
        	logEntries = new ArrayList<String>();
        }
        long logSize = logEntries.size();
        
        /*
         * Heading.
         */
        String heading = "Information about ingestlog from ingest at date: '" + b.getDate() + "' of logsize " + logSize + " :";
        
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

        if (backPlace != null) {
        	backPlace.setText("<a href=\"" 
        			+ Servlet.environment.getBlacklistsPath() 
        			+ "\" class=\"btn btn-primary\"><i class=\"icon-white icon-list\"></i> Tilbage til oversigten</a>");
        } else {
        	logger.warning("No back´placeholder found in template '" + templateName + "'" );
        }

        if (headingPlace != null) {
            headingPlace.setText(heading);
        } else {
        	logger.warning("No heading´ placeholder found in template '" + templateName + "'" );
        }
        
        ResourceUtils.insertText(insertionDatePlace, "insertionDate", b.getDate().toString(), templateName, logger);
        ResourceUtils.insertText(filenamePlace, "filename", b.getFilename() + "", templateName, logger);
 
        ResourceUtils.insertText(linecountPlace, "linecount",  b.getLinecount() + "", templateName, logger);
        ResourceUtils.insertText(insertedcountPlace, "insertedcount",  b.getInsertedcount() + "", templateName, logger);
        ResourceUtils.insertText(duplicatecountPlace, "duplicatecount",  b.getDuplicatecount() + "", templateName, logger);
        ResourceUtils.insertText(rejectedcountPlace, "rejectedcount",  b.getRejectedcount() + "", templateName, logger);
        ResourceUtils.insertText(errorcountPlace, "errorcount",  b.getErrorcount() + "", templateName, logger);
        ResourceUtils.insertText(logsizePlace, "logsize",  logSize + "", templateName, logger);
        Set<String> sortedSet = new TreeSet<String>(logEntries); 
        StringBuilder sb = new StringBuilder();
        sb.append("<pre>\r\n");
    	for (String listElement: sortedSet) {
    		sb.append(listElement);
    		sb.append("\r\n");
    	}	
    	
    	ResourceUtils.insertText(contentPlace, "content",  sb.toString(), templateName, logger);
        CommonResource.insertInAlertPlace(alertPlace, errorStr, successStr, templateName, logger);
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
        
}
