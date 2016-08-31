package dk.kb.webdanica.webapp.resources;

import java.io.IOException;
import java.util.ArrayList;
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
import dk.kb.webdanica.webapp.Environment;
import dk.kb.webdanica.webapp.Navbar;
import dk.kb.webdanica.webapp.Servlet;
import dk.kb.webdanica.webapp.User;
import dk.netarkivet.common.webinterface.HTMLUtils;

/**
 * 
 * pathInfo: /criteriaresult/<string>/<string>/
 * 1. string: harvestname
 * 2. string: url
 */
public class CriteriaResultResource implements ResourceAbstract {
	
	private static final Logger logger = Logger.getLogger(CriteriaResultResource.class.getName());
	private static final String CRITERIA_RESULT_SHOW_TEMPLATE = "criteriaresult_master.html";

	public static void main (String[] args) {
		String pathinfo = "/criteriaresult/webdanica-trial-1470219095233/http%3A%2Fhedgehogs.net%2F/";
		String[] infoParts = pathinfo.split(CRITERIA_RESULT_PATH);
		System.out.println(CriteriaResultResource.getCriteriaKeys(infoParts));
/*
		int count=0;
		for (String infopart: infoParts) {
			System.out.println("i=" + count + ", infopart: " + infopart);
			count++;
		}
		*/
	}

	protected int R_CRITERIA_RESULT = -1;

	public static final String CRITERIA_RESULT_PATH = "/criteriaresult/";
	//public static final String CRITERIA_RESULT_PATH = "/criteriaresult/<string>/<string>/";
	private Environment environment;

	private CassandraCriteriaResultsDAO dao;

	@Override
	public void resources_init(Environment environment) {
		this.environment = environment;

	}

	@Override
	public void resources_add(ResourceManagerAbstract resourceManager) {
		R_CRITERIA_RESULT = resourceManager.resource_add(this, CRITERIA_RESULT_PATH, 
				environment.getResourcesMap().getResourceByPath(CRITERIA_RESULT_PATH).isSecure());
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
	        SingleCriteriaResult b;
	        
	        // Retrieving UUID or maybe name from pathinfo instead of String equivalent of numerics
	        CriteriaKeys CK = getCriteriaKeys(pathInfo.split(CRITERIA_RESULT_PATH));
	        if (CK == null) {
	        	// create default dummy blacklist
	        	String errMsg = "No url, and harvestname information found in the path: " + pathInfo;
	        	logger.warning(errMsg);
	        	b = SingleCriteriaResult.createErrorResult(errMsg); 
	        } else {
	            b = dao.getSingleResult(CK.url, CK.harvest);
	            if (b == null) { // no criteria_result found with given url and harvestname
	            	String errMsg = "No result found for url='" + CK.url + "', harvest='" 
        			+ CK.harvest + "'.";
	            	logger.warning(errMsg);
	            	b = SingleCriteriaResult.createErrorResult(errMsg);
	            }
	        } 
	        
	        if (Servlet.environment.getContextPath()== null) {
	        	Servlet.environment.setContextPath(req.getContextPath());
	        }
	        
	        if (Servlet.environment.getCriteriaResultPath() == null) {
	        	Servlet.environment.setCriteriaResultPath(Servlet.environment.getContextPath() + "/criteriaresult/");
	        }
	        if (Servlet.environment.getCriteriaResultsPath() == null) {
	        	Servlet.environment.setCriteriaResultsPath(Servlet.environment.getContextPath() + "/criteriaresults/");
	        }
	        if (resource_id == R_CRITERIA_RESULT) {
	        	criteriaResult_show(dab_user, req, resp, b);
	        } 
	    }   

		public void criteriaResult_show(User dab_user, HttpServletRequest req,
	            HttpServletResponse resp, SingleCriteriaResult b)
	            throws IOException {
	        ServletOutputStream out = resp.getOutputStream();
	        resp.setContentType("text/html; charset=utf-8");
	        // TODO error text
	        String errorStr = null;
	        String successStr = null;
	        Caching.caching_disable_headers(resp);
	        String templateName = CRITERIA_RESULT_SHOW_TEMPLATE;
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
	        
	        TemplatePlaceHolder urlPlace = TemplatePlaceBase.getTemplatePlaceHolder("url");
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
	        placeHolders.add(urlPlace);
	        placeHolders.add(namePlace);
	        placeHolders.add(descriptionPlace);
	        placeHolders.add(lastupdatetimePlace);
	        placeHolders.add(listsizePlace);
	        placeHolders.add(activePlace);

	        TemplateParts templateParts = template.filterTemplate(placeHolders, resp.getCharacterEncoding());
	        
	        
	        /*
	         * Heading.
	         */
	        String heading = "Information about CriteriaResult for url '" + b.url + "', seedUri = '" + b.seedurl + "' harvest='" + b.harvestName + "':";
	        
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
	        	logger.warning("No back´placeholder found in template '" + templateName+ "'" );
	        }

	        if (headingPlace != null) {
	            headingPlace.setText(heading);
	        } else {
	        	logger.warning("No heading´ placeholder found in template '" + templateName + "'" );
	        }
	        
	        ResourceUtils.insertText(urlPlace, "url",  b.url, templateName, logger);
	        //ResourceUtils.insertText(namePlace, "name",  b.getName(), templateName, logger);
	        
	        //ResourceUtils.insertText(descriptionPlace, "description",  b.getDescription(), templateName, logger);
	        ResourceUtils.insertText(lastupdatetimePlace, "last_update_time",  "" + b.insertedDate, templateName, logger);  // Convert to proper date
	        //ResourceUtils.insertText(listsizePlace, "list_size",  blackListSize + "", templateName, logger);
	        //ResourceUtils.insertText(activePlace, "activeStatus",  b.isActive() + "", templateName, logger);
	         
	        StringBuilder sb = new StringBuilder();
	        sb.append("<pre>\r\n");
	        String ROW_DELIM = ",";
	        String KEYVAL_DELIM = ":";
	    	for (String listElement: b.getValuesAsStringList(ROW_DELIM, KEYVAL_DELIM)) {
	    		sb.append(listElement);
	    		sb.append("\r\n");
	    	}
	    	sb.append("</pre>\r\n");
	    	
	    	
	    	
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
	
		public static CriteriaKeys getCriteriaKeys(String[] split) {
			CriteriaKeys resultKeys = null;
	        if (split.length > 1) {
	        	String arguments = split[1];
	            String[] argumentParts = arguments.split("/");
	            if (argumentParts.length == 2) {
	            	resultKeys = new CriteriaKeys(argumentParts[0], HTMLUtils.decode(argumentParts[1]));
	            }
	        }
	        return resultKeys;
        }
		
	    static class CriteriaKeys {
	    	private String url;
	    	private String harvest;

	    	private CriteriaKeys(String harvest, String url) {
	    		this.url = url;
	    		this.harvest = harvest;
	    	}
	    	public String toString() {
	    		return "harvest='" + harvest + "', url= '" + url + "'";
	    	}
	    }

	}
	
	