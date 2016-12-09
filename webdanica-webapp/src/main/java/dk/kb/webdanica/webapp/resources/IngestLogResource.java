package dk.kb.webdanica.webapp.resources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
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

import dk.kb.webdanica.core.datamodel.BlackList;
import dk.kb.webdanica.core.datamodel.IngestLog;
import dk.kb.webdanica.core.datamodel.dao.BlackListDAO;
import dk.kb.webdanica.core.datamodel.dao.IngestLogDAO;
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
        	blacklist_show(dab_user, req, resp, b);
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
            HttpServletResponse resp) {
	    // TODO Auto-generated method stub
	    
    }

	public void blacklist_show(User dab_user, HttpServletRequest req,
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
        
        TemplatePlaceHolder uidPlace = TemplatePlaceBase.getTemplatePlaceHolder("uid");
        TemplatePlaceHolder namePlace = TemplatePlaceBase.getTemplatePlaceHolder("name");
        TemplatePlaceHolder descriptionPlace = TemplatePlaceBase.getTemplatePlaceHolder("description");
        TemplatePlaceHolder lastupdatetimePlace = TemplatePlaceBase.getTemplatePlaceHolder("last_update_time");
        TemplatePlaceHolder listsizePlace = TemplatePlaceBase.getTemplatePlaceHolder("list_size");
        TemplatePlaceHolder activePlace = TemplatePlaceBase.getTemplatePlaceHolder("activeStatus");
        
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
        placeHolders.add(uidPlace);
        placeHolders.add(namePlace);
        placeHolders.add(descriptionPlace);
        placeHolders.add(lastupdatetimePlace);
        placeHolders.add(listsizePlace);
        placeHolders.add(activePlace);

        TemplateParts templateParts = template.filterTemplate(placeHolders, resp.getCharacterEncoding());


        //Date blackListLastUpdatedTime = new Date(b.getLastUpdate());
        
        List<String> logEntries = b.getLogEntries();
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

        
        ResourceUtils.insertText(uidPlace, "uid",  b.getUid().toString(), templateName, logger);
        ResourceUtils.insertText(namePlace, "name",  b.getName(), templateName, logger);
        ResourceUtils.insertText(descriptionPlace, "description",  b.getDescription() + "", templateName, logger);
        ResourceUtils.insertText(lastupdatetimePlace, "last_update_time",  blackListLastUpdatedTime + "", templateName, logger);
        ResourceUtils.insertText(listsizePlace, "list_size",  blackListSize + "", templateName, logger);
        ResourceUtils.insertText(activePlace, "activeStatus",  b.isActive() + "", templateName, logger);
         
        StringBuilder sb = new StringBuilder();
        sb.append("<pre>\r\n");
    	for (String listElement: blacklist) {
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
