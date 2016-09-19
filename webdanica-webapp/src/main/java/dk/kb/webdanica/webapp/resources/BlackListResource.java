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
import java.util.UUID;
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
import dk.kb.webdanica.datamodel.BlackListDAO;
import dk.kb.webdanica.webapp.Environment;
import dk.kb.webdanica.webapp.Navbar;
import dk.kb.webdanica.webapp.Servlet;
import dk.kb.webdanica.webapp.User;

public class BlackListResource implements ResourceAbstract {

    private static final Logger logger = Logger.getLogger(BlackListResource.class.getName());
    
    private static final String BLACKLIST_SHOW_TEMPLATE = "blacklist_master.html";
/*
    protected static final int[] USER_ADD_PERMISSIONS = {Permission.P_USER_ADMIN, Permission.P_USER_ADD};

    protected static final int[] USER_ADMIN_PERMISSIONS = {Permission.P_USER_ADMIN};
*/

    protected int R_BLACKLIST = -1;
    public static final String BLACKLIST_PATH = "/blacklist/";
    private Environment environment;

    private BlackListDAO dao;
    
    @Override
    public void resources_init(Environment environment) {
        this.environment = environment;
        this.dao = environment.getConfig().getBlacklistDao();
    }

    @Override
    public void resources_add(ResourceManagerAbstract resourceManager) {
        //R_BLACKLIST = resourceManager.resource_add(this, "/blacklist/<string>/", true);
        R_BLACKLIST = resourceManager.resource_add(this, BLACKLIST_PATH, 
        		environment.getResourcesMap().getResourceByPath(BLACKLIST_PATH).isSecure());
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
    	logger.info("pathInfo: " + pathInfo);
        logger.info("resource_id: " + resource_id);
        BlackList b = null;
        // FIXME when the uid can be retrieved from the List argument
        // Retrieving UUID or maybe name from pathinfo instead of String equivalent of numerics
        String[] pathInfoParts  = pathInfo.split(BLACKLIST_PATH);
        UUID dummyUUID = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
        List<String> dummyList = new ArrayList<String>();
        if (pathInfoParts.length > 1) {
        	String UUIDString = pathInfoParts[1];
            if (UUIDString.endsWith("/")) {
            	UUIDString = UUIDString.substring(0, UUIDString.length()-1);
            }
            try {
                b = dao.readBlackList(UUID.fromString(UUIDString));
            } catch (Exception e) {
            }
            if (b == null) { // no blacklist found with UID=UUIDString
            	logger.warning("No blacklist found with uid=" + UUIDString);
            	b = new BlackList(dummyUUID, "dummyUUD", "No blacklist found with UUID=" + UUIDString, dummyList, System.currentTimeMillis() , false);
            }
        } else {
        	// create default dummy blacklist
        	logger.warning("No UUID for blacklist given as argument in the path: " + pathInfo);
        	b = new BlackList(dummyUUID, "dummyUUD", "No blacklist designated", dummyList, System.currentTimeMillis() , false);
        }
        
        if (Servlet.environment.getContextPath()== null) {
        	Servlet.environment.setContextPath(req.getContextPath());
        }
        
        if (Servlet.environment.getBlacklistPath() == null) {
        	Servlet.environment.setBlacklistPath(Servlet.environment.getContextPath() + "/blacklist/");
        }
        if (Servlet.environment.getBlacklistsPath() == null) {
        	Servlet.environment.setBlacklistsPath(Servlet.environment.getContextPath() + "/blacklists/");
        }
        if (resource_id == R_BLACKLIST) {
            blacklist_show(dab_user, req, resp, b);
        } 
    }

    public void blacklist_show(User dab_user, HttpServletRequest req,
            HttpServletResponse resp, BlackList b)
            throws IOException {
        ServletOutputStream out = resp.getOutputStream();
        resp.setContentType("text/html; charset=utf-8");
        // TODO error text
        String errorStr = null;
        String successStr = null;
        Caching.caching_disable_headers(resp);

        Template template = environment.getTemplateMaster().getTemplate(BLACKLIST_SHOW_TEMPLATE);

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


        Date blackListLastUpdatedTime = new Date(b.getLastUpdate());
        List<String> blacklist = b.getList();
        long blackListSize = blacklist.size();
        
        /*
         * Heading.
         */
        String heading = "Information about blacklist '" + b.getName() + "' of size " + blackListSize + " :";
        
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
        	logger.warning("No back´placeholder found in template '" + BLACKLIST_SHOW_TEMPLATE + "'" );
        }

        if (headingPlace != null) {
            headingPlace.setText(heading);
        } else {
        	logger.warning("No heading´ placeholder found in template '" + BLACKLIST_SHOW_TEMPLATE + "'" );
        }

        
        ResourceUtils.insertText(uidPlace, "uid",  b.getUid().toString(), BLACKLIST_SHOW_TEMPLATE, logger);
        ResourceUtils.insertText(namePlace, "name",  b.getName(), BLACKLIST_SHOW_TEMPLATE, logger);
        ResourceUtils.insertText(descriptionPlace, "description",  b.getDescription(), BLACKLIST_SHOW_TEMPLATE, logger);
        ResourceUtils.insertText(lastupdatetimePlace, "last_update_time",  blackListLastUpdatedTime.toString(), BLACKLIST_SHOW_TEMPLATE, logger);
        ResourceUtils.insertText(listsizePlace, "list_size",  blackListSize + "", BLACKLIST_SHOW_TEMPLATE, logger);
        ResourceUtils.insertText(activePlace, "activeStatus",  b.isActive() + "", BLACKLIST_SHOW_TEMPLATE, logger);
         
        StringBuilder sb = new StringBuilder();
        sb.append("<pre>\r\n");
    	for (String listElement: blacklist) {
    		sb.append(listElement);
    		sb.append("\r\n");
    	}	
    	
    	ResourceUtils.insertText(contentPlace, "content",  sb.toString(), BLACKLIST_SHOW_TEMPLATE, logger);
        
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
            	logger.warning("No alert placeholder found in template '" + BLACKLIST_SHOW_TEMPLATE + "'" );
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
            	logger.warning("No success placeholder found in template '" + BLACKLIST_SHOW_TEMPLATE + "'" );
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
        
}
