package dk.kb.webdanica.webapp;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import dk.kb.webdanica.utils.UnitTestUtils;

public class EnvironmentTester {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
 
	@Test
	public void testDummy() {
	    
	}
	
	public void testEnvironmentConstruction() throws Exception {
		ServletContext servletContext = mock (ServletContext.class);
		ServletConfig theServletConfig = mock (ServletConfig.class);
	
		when(theServletConfig.getInitParameter("netarchivesuite-settings"))
			.thenReturn("settings_NAS_Webdanica.xml");
		when(theServletConfig.getInitParameter("webdanica-settings"))
			.thenReturn("settings.xml");
		when(theServletConfig.getInitParameter("login-template"))
			.thenReturn("login.html");
		
		// stubbing appears before the actual execution
		//when(mockedList.get(0)).thenReturn("first");
		
		// Used instead of the UnitTestUtils.getTestResourceFile
		// As the below code doesn't seem to work
		//  File loggingPropFile = UnitTestUtils.getTestResourceFile("WEB-INF/logging.properties");
			
		File loggingPropFile = new File("src/test/resources/WEB-INF/logging.properties");
		
		when(servletContext.getRealPath("/WEB-INF/logging.properties")).thenReturn(loggingPropFile.getAbsolutePath());
		
		// Set WEBDANICA_HOME in the environment using reflection 
		File WebdanicaHomeDir = new File("src/test/resources/");
		Map<String, String> currentEnv = System.getenv();
		Map<String, String> newEnv = new HashMap<String,String>();
		newEnv.putAll(currentEnv);
		newEnv.put("WEBDANICA_HOME", WebdanicaHomeDir.getAbsolutePath());
		UnitTestUtils.setEnv(newEnv);
		
		when(servletContext.getRealPath("/")).thenReturn(""); // Dummy path for 
		
		Environment e = new Environment(servletContext, theServletConfig);
		assertFalse("Environment should not be null", e == null); 
		
	}
}
