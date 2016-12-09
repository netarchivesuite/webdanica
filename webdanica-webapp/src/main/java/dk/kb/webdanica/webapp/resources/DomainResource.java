package dk.kb.webdanica.webapp.resources;

import java.io.IOException;
import java.util.ArrayList;
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

import dk.kb.webdanica.core.datamodel.Domain;
import dk.kb.webdanica.core.datamodel.dao.DomainsDAO;
import dk.kb.webdanica.webapp.Constants;
import dk.kb.webdanica.webapp.Environment;
import dk.kb.webdanica.webapp.Navbar;
import dk.kb.webdanica.webapp.Servlet;
import dk.kb.webdanica.webapp.User;

public class DomainResource implements ResourceAbstract {

	    private static final Logger logger = Logger.getLogger(DomainResource.class.getName());

	    private Environment environment;

	    protected int R_DOMAINS_LIST = -1;
	    
	    protected int R_DOMAIN_SHOW = -1;

		private String DOMAIN_SHOW_TEMPLATE = "show_domain.html";

		//protected int R_BLACKLIST_ADD = -1;

		public static final String DOMAIN_LIST_PATH = "/domains/";

		public static final String DOMAIN_PATH = "/domain/";
		
	    @Override
	    public void resources_init(Environment environment) {
	        this.environment = environment;
	        
	    }

	    @Override
	    public void resources_add(ResourceManagerAbstract resourceManager) {
	        R_DOMAINS_LIST = resourceManager.resource_add(this, DOMAIN_LIST_PATH, 
	        		   		environment.getResourcesMap().getResourceByPath(DOMAIN_LIST_PATH).isSecure());
	        R_DOMAIN_SHOW = resourceManager.resource_add(this, DOMAIN_PATH, 
    		   		environment.getResourcesMap().getResourceByPath(DOMAIN_PATH).isSecure());
	        //R_BLACKLIST_ADD = resourceManager.resource_add(this, "/blacklists/add", true);
	    }

	    @Override
	    public void resource_service(ServletContext servletContext, User dab_user,
	    		HttpServletRequest req, HttpServletResponse resp,
	    		int resource_id, List<Integer> numerics, String pathInfo) throws IOException {

	        if (resource_id == R_DOMAINS_LIST) {
	            domains_list(dab_user, req, resp);
	        } else if (resource_id == R_DOMAIN_SHOW) {
	        	Domain domain = getDomainFromPathinfo(pathInfo, environment, DOMAIN_PATH);
	            domain_show(dab_user, req, resp, domain);
	        } else {
	        	String error = "No match for pathinfo'" +  pathInfo + "' in resource '" +  this.getClass().getName() + "'";
	        	CommonResource.show_error(error, resp, environment);
	        	return;
	        }
	    }
	    private Domain getDomainFromPathinfo(String pathInfo, 
                Environment environment2, String domainPath) {
	    	DomainsDAO ddao = environment2.getConfig().getDAOFactory().getDomainsDAO();	
	        String[] pathParts = pathInfo.split(domainPath);
	        Domain domain = null;
	    	return domain;
        }

		private void domain_show(User dab_user, HttpServletRequest req,
                HttpServletResponse resp, Domain b) throws IOException {
	    	ServletOutputStream out = resp.getOutputStream();
	        resp.setContentType("text/html; charset=utf-8");
	        // TODO error text
	        String errorStr = null;
	        String successStr = null;
	        Caching.caching_disable_headers(resp);
	        String templateName = DOMAIN_SHOW_TEMPLATE;
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
	        
	        TemplatePlaceHolder tldPlace = TemplatePlaceBase.getTemplatePlaceHolder("tld");
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
	        placeHolders.add(tldPlace);
	        placeHolders.add(namePlace);
	        placeHolders.add(descriptionPlace);
	        placeHolders.add(lastupdatetimePlace);
	        placeHolders.add(listsizePlace);
	        placeHolders.add(activePlace);

	        TemplateParts templateParts = template.filterTemplate(placeHolders, resp.getCharacterEncoding());
        
	        /*
	         * Heading.
	         */
	        String heading = "Information about domain '" + b.getDomain() + "' from tld " +  b.getTld() + " :";
	        
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
	        			+ Servlet.environment.getDomainsPath() 
	        			+ "\" class=\"btn btn-primary\"><i class=\"icon-white icon-list\"></i> Tilbage til oversigten</a>");
	        } else {
	        	logger.warning("No back´placeholder found in template '" + templateName + "'" );
	        }

	        if (headingPlace != null) {
	            headingPlace.setText(heading);
	        } else {
	        	logger.warning("No heading´ placeholder found in template '" + templateName + "'" );
	        }

	        /*
	        ResourceUtils.insertText(uidPlace, "uid",  b.getUid().toString(), BLACKLIST_SHOW_TEMPLATE, logger);
	        ResourceUtils.insertText(namePlace, "name",  b.getName(), BLACKLIST_SHOW_TEMPLATE, logger);
	        ResourceUtils.insertText(descriptionPlace, "description",  b.getDescription() + "", BLACKLIST_SHOW_TEMPLATE, logger);
	        ResourceUtils.insertText(lastupdatetimePlace, "last_update_time",  blackListLastUpdatedTime + "", BLACKLIST_SHOW_TEMPLATE, logger);
	        ResourceUtils.insertText(listsizePlace, "list_size",  blackListSize + "", BLACKLIST_SHOW_TEMPLATE, logger);
	        ResourceUtils.insertText(activePlace, "activeStatus",  b.isActive() + "", BLACKLIST_SHOW_TEMPLATE, logger);
	        */
	
	  /*      
	        StringBuilder sb = new StringBuilder();
	        
	        sb.append("<pre>\r\n");
	    	for (String listElement: blacklist) {
	    		sb.append(listElement);
	    		sb.append("\r\n");
	    	}	
	    	
	    	ResourceUtils.insertText(contentPlace, "content",  sb.toString(), BLACKLIST_SHOW_TEMPLATE, logger);
	    */   
	        
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
	        

		public void domains_list(User dab_user, HttpServletRequest req,
	            HttpServletResponse resp) throws IOException {
	        ServletOutputStream out = resp.getOutputStream();
	        resp.setContentType("text/html; charset=utf-8");

	        Caching.caching_disable_headers(resp);
	        String templatename = "domain_list.html"; 
	        Template template = environment.getTemplateMaster().getTemplate(templatename);

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

	        List<Domain> domainList = null;
            try {
                domainList = environment.getConfig().getDAOFactory().getDomainsDAO().getDomains(null, null, Integer.MAX_VALUE);
            } catch (Exception e) {
            	String errMsg = "System-error: Exception thrown";
            	logger.log(Level.WARNING, errMsg, e);
            	CommonResource.show_error(errMsg, resp, environment);
            	return;
            }
            
            for (Domain b: domainList) {
                sb.append("<tr>");
                sb.append("<td>");    
                sb.append("<a href=\"");
                sb.append(Servlet.environment.getDomainPath());
                sb.append(b.getDomain());
                sb.append("/\">");
                sb.append(b.getDomain());
                sb.append("</a>");
                sb.append("</td>");
                sb.append("<td>");
                sb.append(b.getTld());
                sb.append("</td>");
                sb.append("<td>");
                sb.append(b.getDanicaStatus());
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
	        menuSb.append(Servlet.environment.getDomainsPath());
	        menuSb.append("\">");
	        menuSb.append("Liste over blacklister");
	        menuSb.append("</a></li>\n");
	        
	        /*
	         * Heading.
	         */

	        String heading = "Liste over kendte domæner i systemet";

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
		
		public static String createDomainLink() {
			return null;
		}
		
	}


