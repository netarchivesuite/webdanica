package dk.kb.webdanica.webapp.resources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import com.antiaction.common.filter.Caching;
import com.antiaction.common.html.HtmlEntity;
import com.antiaction.common.templateengine.Template;
import com.antiaction.common.templateengine.TemplateParts;
import com.antiaction.common.templateengine.TemplatePlaceBase;
import com.antiaction.common.templateengine.TemplatePlaceHolder;

import dk.kb.webdanica.webapp.Environment;
import dk.kb.webdanica.webapp.Navbar;
import dk.kb.webdanica.webapp.Servlet;

public class CommonResource {
	
	private static final Logger logger = Logger.getLogger(CommonResource.class.getName());
	
	private static final String ERROR_TEMPLATE = "error_master.html";
	
	public static void show_error(String error, HttpServletResponse resp, Environment env) throws IOException {
		ServletOutputStream out = resp.getOutputStream();
		resp.setContentType("text/html; charset=utf-8");
		String errorStr = error;
		Caching.caching_disable_headers(resp);
		String templateName = ERROR_TEMPLATE;
		Template template = env.getTemplateMaster().getTemplate(templateName);

		TemplatePlaceHolder titlePlace = TemplatePlaceBase.getTemplatePlaceHolder("title");
		TemplatePlaceHolder appnamePlace = TemplatePlaceBase.getTemplatePlaceHolder("appname");
		TemplatePlaceHolder navbarPlace = TemplatePlaceBase.getTemplatePlaceHolder("navbar");
		TemplatePlaceHolder userPlace = TemplatePlaceBase.getTemplatePlaceHolder("user");
		TemplatePlaceHolder menuPlace = TemplatePlaceBase.getTemplatePlaceHolder("menu");
		TemplatePlaceHolder backPlace = TemplatePlaceBase.getTemplatePlaceHolder("back");
		TemplatePlaceHolder headingPlace = TemplatePlaceBase.getTemplatePlaceHolder("heading");
		TemplatePlaceHolder alertPlace = TemplatePlaceBase.getTemplatePlaceHolder("alert");
		TemplatePlaceHolder contentPlace = TemplatePlaceBase.getTemplatePlaceHolder("content");

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

		TemplateParts templateParts = template.filterTemplate(placeHolders, resp.getCharacterEncoding());


		/*
		 * Heading.
		 */
		String heading = "Error showing a page: ";

		/*
		 * Places.
		 */

		if (titlePlace != null) {
			titlePlace.setText(HtmlEntity.encodeHtmlEntities(dk.kb.webdanica.webapp.Constants.WEBAPP_NAME).toString());
		}

		if (appnamePlace != null) {
			appnamePlace.setText(HtmlEntity.encodeHtmlEntities(dk.kb.webdanica.webapp.Constants.WEBAPP_NAME 
					+ dk.kb.webdanica.webapp.Constants.SPACE + env.getVersion()).toString());
		}

		if (navbarPlace != null) {
			navbarPlace.setText(Navbar.getNavbar(Navbar.N_URL_SHOW));
		}
		/*
          if (userPlace != null) {
              userPlace.setText(Navbar.getUserHref(dab_user));
          } 
		 */
		if (backPlace != null) {
			backPlace.setText("<a href=\"" 
					+ Servlet.environment.getSeedsPath() 
					+ "\" class=\"btn btn-primary\"><i class=\"icon-white icon-list\"></i> Tilbage til oversigten</a>");
		} else {
			logger.warning("No back´placeholder found in template '" + templateName+ "'" );
		}

		if (headingPlace != null) {
			headingPlace.setText(heading);
		} else {
			logger.warning("No heading´ placeholder found in template '" + templateName + "'" );
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
			} else {
				logger.warning("No alert placeholder found in template '" + templateName + "'" );
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

	public static void insertInAlertPlace(TemplatePlaceHolder alertPlace,
            String errorStr, String successStr, String templateName,
            Logger logger2) {
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
    }

}
