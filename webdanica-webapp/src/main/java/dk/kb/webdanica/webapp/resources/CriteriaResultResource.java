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

import org.apache.commons.lang.StringUtils;

import com.antiaction.common.filter.Caching;
import com.antiaction.common.html.HtmlEntity;
import com.antiaction.common.templateengine.Template;
import com.antiaction.common.templateengine.TemplateParts;
import com.antiaction.common.templateengine.TemplatePlaceBase;
import com.antiaction.common.templateengine.TemplatePlaceHolder;

import dk.kb.webdanica.datamodel.CriteriaResultsDAO;
import dk.kb.webdanica.datamodel.criteria.Codes;
import dk.kb.webdanica.datamodel.criteria.CriteriaUtils;
import dk.kb.webdanica.datamodel.criteria.SingleCriteriaResult;
import dk.kb.webdanica.webapp.Environment;
import dk.kb.webdanica.webapp.Navbar;
import dk.kb.webdanica.webapp.Servlet;
import dk.kb.webdanica.webapp.User;

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
		System.out.println(CriteriaResultResource.getCriteriaKeys(pathinfo));
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

	private CriteriaResultsDAO dao;

	@Override
	public void resources_init(Environment environment) {
		this.environment = environment;
		this.dao = environment.getConfig().getDAOFactory().getCriteriaResultsDAO();
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
	        SingleCriteriaResult b = null;
	        
	        // Retrieving UUID or maybe name from pathinfo instead of String equivalent of numerics
	        CriteriaKeys CK = getCriteriaKeys(pathInfo);
	        if (CK == null) {
	        	// create default dummy blacklist
	        	String errMsg = "No url, and harvestname information found in the path: " + pathInfo;
	        	logger.warning(errMsg);
	        	b = SingleCriteriaResult.createErrorResult(errMsg); 
	        } else {
	        	try {
		            b = dao.getSingleResult(CK.url, CK.harvest);
	        	} catch (Exception e) {
	        	}
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
	        TemplatePlaceHolder harvestNamePlace = TemplatePlaceBase.getTemplatePlaceHolder("harvestName");
	        TemplatePlaceHolder insertedTimePlace = TemplatePlaceBase.getTemplatePlaceHolder("inserted_time");
	        TemplatePlaceHolder seedUrlPlace = TemplatePlaceBase.getTemplatePlaceHolder("seed_url");
	        TemplatePlaceHolder danishcodePlace = TemplatePlaceBase.getTemplatePlaceHolder("danish_code");
	        TemplatePlaceHolder errorPlace = TemplatePlaceBase.getTemplatePlaceHolder("error");
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
	        placeHolders.add(harvestNamePlace);
	        placeHolders.add(insertedTimePlace);
	        placeHolders.add(seedUrlPlace);
	        placeHolders.add(errorPlace);
	        placeHolders.add(danishcodePlace);
	        
	        
	        TemplateParts templateParts = template.filterTemplate(placeHolders, resp.getCharacterEncoding());
	        
	        
	        /*
	         * Heading.
	         */
	        String heading = "Information about CriteriaResult for url \"" + b.url + "\", seedUri = \"" + b.seedurl + "\", harvest=\"" 
	         + b.harvestName + "\":";
	        
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
	        ResourceUtils.insertText(harvestNamePlace, "harvestName",  b.harvestName, templateName, logger);
	        ResourceUtils.insertText(insertedTimePlace, "inserted_time",  "" + new Date(b.insertedDate), templateName, logger);
	        String errStr = b.errorMsg;
	        if (errStr == null) {
	        	errStr = "";
	        }
	        ResourceUtils.insertText(errorPlace, "error", errStr, templateName, logger);
	        ResourceUtils.insertText(seedUrlPlace, "seed_url", b.seedurl, templateName, logger);
	        String danishCodeStr = b.calcDanishCode + "(" + Codes.getCategory(b.calcDanishCode) + ")";
	        ResourceUtils.insertText(danishcodePlace, "danish_code", danishCodeStr, templateName, logger);
	        
	        StringBuilder sb = new StringBuilder();
	        sb.append("<pre>\r\n");
	        String ROW_DELIM = ",";
	        String KEYVAL_DELIM = ":";
	    	for (String listElement: b.getValuesAsStringList(ROW_DELIM, KEYVAL_DELIM)) {
	    		sb.append(listElement);
	    		sb.append("\r\n");
	    	}
	    	sb.append("Ctext: " + b.getCText());
	    	sb.append("\r\n");
	    	sb.append("Clinks: " + StringUtils.join(b.getClinks(), ","));
	    	sb.append("\r\n");
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
	
		public static CriteriaKeys getCriteriaKeys(String pathInfo) {
			String[] split = pathInfo.split(CRITERIA_RESULT_PATH);
			CriteriaKeys resultKeys = null;
	        if (split.length > 1) {
	        	String arguments = split[1];
	            String[] argumentParts = arguments.split("/");
	            if (argumentParts.length == 2) {
	            	resultKeys = new CriteriaKeys(argumentParts[0], CriteriaUtils.fromBase64(argumentParts[1]));
	            	logger.info("Found Criteriakeys: " + resultKeys);
	            } else {
	            	logger.warning("Unable to find harvestname and url from pathinfo: " + pathInfo);
	            }
	        }
	        return resultKeys;
        }
		
	    public static class CriteriaKeys {
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
	
	