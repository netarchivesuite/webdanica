package dk.kb.webdanica.webapp;

import static org.junit.Assert.*;

import java.io.File;

import javax.servlet.ServletException;

import org.junit.Test;

import dk.kb.webdanica.WebdanicaSettings;
import dk.kb.webdanica.utils.Settings;
import dk.netarkivet.common.CommonSettings;
import dk.netarkivet.common.utils.StringUtils;

public class EnvironmentTester {

	@Test
	public void test() {
		String good_webdanicaSettings = "/home/svc/devel/webdanica/webdanica-core/src/test/resources/settings.xml";
		String bad_webdanicaSettings = "/home/svc/devel/webdanica/webdanica-core/src/test/resources/bad_settings.xml";
		
		File bad_webdanicaSettingsFile = new File(bad_webdanicaSettings);
		File good_webdanicaSettingsFile = new File(good_webdanicaSettings);
		
		
		//System.setProperty("webdanica.settings.file", webdanicaSettingsFile.getAbsolutePath());
		
		System.out.println(Settings.isValidSettingsfile(bad_webdanicaSettingsFile));
		System.out.println(Settings.isValidSettingsfile(good_webdanicaSettingsFile));
		
		/*
	     for (File f: Settings.getSettingsFiles()) {
	     		System.out.println("using Webdanica settingsfile: " +  f.getAbsolutePath());
	     	}
	     	
	     	String[] ignoredSuffixes = Settings.getAll(WebdanicaSettings.IGNORED_SUFFIXES);
	     	System.out.println("Following suffixes are currently ignored:" + StringUtils.conjoin(",", 
	     			ignoredSuffixes));
		 
		*/
		
	}
	/*
		String webdanicaHomeEnv = System.getenv("WEBDANICA_HOME");
	     if (webdanicaHomeEnv == null) {
	     	throw new ServletException("'WEBDANICA_HOME' must be defined in the environment!");
	     }
	     File webdanicaHomeDir = new File(webdanicaHomeEnv);
	     if (!webdanicaHomeDir.isDirectory()) {
	     	throw new ServletException("The path set by 'WEBDANICA_HOME' does not represent a directory: " 
	     			+ webdanicaHomeDir.getAbsolutePath());
	     }
	     // relative paths in web.xml will be prefixed by this path + /

	     String netarchiveSuiteSettings = servletConfig.getInitParameter("netarchivesuite-settings");
	     String webdanicaSettings = servletConfig.getInitParameter("webdanica-settings");
	     
	     if (!netarchiveSuiteSettings.startsWith("/")) {
	     	netarchiveSuiteSettings = webdanicaHomeDir.getAbsolutePath() + "/" + netarchiveSuiteSettings;
	     }
	     File netarchiveSuiteSettingsFile = new File(netarchiveSuiteSettings);
	     
	     
	     if (netarchiveSuiteSettingsFile.isFile()) {
	     	System.setProperty("dk.netarkivet.settings.file", netarchiveSuiteSettingsFile.getAbsolutePath());
	     } else {
	     	logger.warning("The parameter 'netarchivesuite-settings' refers to non-existing file: " 
	     			+ netarchiveSuiteSettingsFile.getAbsolutePath());
	     }
	     
	     
	     if (!webdanicaSettings.startsWith("/")) {
	     	webdanicaSettings = webdanicaHomeDir.getAbsolutePath() + "/" + webdanicaSettings;
	     }
	     
	     
	     // Code to check, that it works.
	     try {
	     	for (File f: dk.netarkivet.common.utils.Settings.getSettingsFiles()) {
	     		logger.info("using NetarchiveSuite settingsfile: " +  f.getAbsolutePath());
	     	}
	     	logger.info("Connected to NetarchiveSuite system with environmentname: " + 
	     			dk.netarkivet.common.utils.Settings.get(CommonSettings.ENVIRONMENT_NAME));
	     
	     	logger.info("Connected to NetarchiveSuite system with environmentname: " + 
	     			dk.netarkivet.common.utils.Settings.get(CommonSettings.ENVIRONMENT_NAME));
	     
	     	for (File f: Settings.getSettingsFiles()) {
	     		logger.info("using Webdanica settingsfile: " +  f.getAbsolutePath());
	     	}
	     	
	     	String[] ignoredSuffixes = Settings.getAll(WebdanicaSettings.IGNORED_SUFFIXES);
	     	logger.info("Following suffixes are currently ignored:" + StringUtils.conjoin(",", 
	     			ignoredSuffixes));
	     } catch (Throwable e){
	     	e.printStackTrace();
	     	logger.severe(e.getLocalizedMessage());
	     }
	}
*/
	

}
