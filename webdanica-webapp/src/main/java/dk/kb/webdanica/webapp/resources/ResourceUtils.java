package dk.kb.webdanica.webapp.resources;

import java.util.logging.Logger;

import com.antiaction.common.templateengine.TemplatePlaceHolder;

public class ResourceUtils {
	public static void insertText(TemplatePlaceHolder somePlace, String placeholdername, String value, String template, Logger logger) {
	if (somePlace != null) {
        somePlace.setText(value);
     } else {
     	logger.warning("No placeholder '" + placeholdername + "' found in template '" + template + "'" );
     }
	}
}
