package dk.kb.webdanica.webapp;

import static org.junit.Assert.*;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import dk.kb.webdanica.utils.Settings;
import dk.kb.webdanica.webapp.resources.BlackListResource;
import dk.kb.webdanica.webapp.resources.BlackListsResource;
import dk.kb.webdanica.webapp.resources.IndexResource;
import dk.kb.webdanica.webapp.resources.ResourceDescription;
import dk.kb.webdanica.webapp.resources.ResourcesMap;
import dk.kb.webdanica.webapp.resources.SeedsResource;
import dk.kb.webdanica.webapp.resources.StaticResource;
import dk.kb.webdanica.webapp.resources.StatusResource;

public class ResourcesMapTester {

	private static File webdanicaSettingsFile;
	private static File webdanicaHomeDir;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		webdanicaHomeDir = new File("src/test/resources/");
		webdanicaSettingsFile = new File(webdanicaHomeDir, "settings.xml");
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
	public void testConstructorSuccess() {
		System.setProperty(Settings.SETTINGS_FILE_PROPERTY, webdanicaSettingsFile.getAbsolutePath());
		Settings.reload();
		new ResourcesMap();
	}
	
	@Test
	public void testConstructorFail() {
		System.setProperty(Settings.SETTINGS_FILE_PROPERTY, "");
		Settings.reload();
		new ResourcesMap();	
	}
	
	@Test
	public void testLookup() {
		System.setProperty(Settings.SETTINGS_FILE_PROPERTY, webdanicaSettingsFile.getAbsolutePath());
		Settings.reload();
		ResourcesMap map = new ResourcesMap();
		map.getResourceByPath("/status/");	
	}
	@Test
	public void testMap() {
		System.setProperty(Settings.SETTINGS_FILE_PROPERTY, webdanicaSettingsFile.getAbsolutePath());
		Settings.reload();
		ResourcesMap map = new ResourcesMap();
		// Test all known resources in the webapp are tested here
		Set<String> knownRequiredKeys = new HashSet<String>();
		testMapAssertion(map, SeedsResource.SEEDS_NUMERIC_DUMP_PATH);
		knownRequiredKeys.add(SeedsResource.SEEDS_NUMERIC_DUMP_PATH);
		
		testMapAssertion(map, SeedsResource.SEEDS_NUMERIC_PATH);
		knownRequiredKeys.add(SeedsResource.SEEDS_NUMERIC_PATH);
		testMapAssertion(map, SeedsResource.SEEDS_PATH);
		knownRequiredKeys.add(SeedsResource.SEEDS_PATH);	
		
		testMapAssertion(map, IndexResource.INDEX_PATH);
		knownRequiredKeys.add(IndexResource.INDEX_PATH);
		
		testMapAssertion(map, StaticResource.CSS_PATH);
		knownRequiredKeys.add(StaticResource.CSS_PATH);
		
		testMapAssertion(map, StaticResource.IMG_PATH);
		knownRequiredKeys.add(StaticResource.IMG_PATH);
		
		testMapAssertion(map, StaticResource.JS_PATH);
		knownRequiredKeys.add(StaticResource.JS_PATH);
		
		
		testMapAssertion(map, BlackListsResource.BLACKLIST_LIST_PATH);
		knownRequiredKeys.add(BlackListsResource.BLACKLIST_LIST_PATH);
		testMapAssertion(map, BlackListResource.BLACKLIST_PATH);
		knownRequiredKeys.add(BlackListResource.BLACKLIST_PATH);
		
		testMapAssertion(map, StatusResource.STATUS_PATH);
		knownRequiredKeys.add(StatusResource.STATUS_PATH);
		
		testMapAssertion(map, StatusResource.STATUS_DEPS_PATH);
		knownRequiredKeys.add(StatusResource.STATUS_DEPS_PATH);
		
		testMapAssertion(map, StatusResource.STATUS_HEALTHY_PATH);
		knownRequiredKeys.add(StatusResource.STATUS_HEALTHY_PATH);
		testMapAssertion(map, StatusResource.STATUS_SQLQUERY_PATH);
		knownRequiredKeys.add(StatusResource.STATUS_SQLQUERY_PATH);
		
		testMapAssertion(map, StatusResource.STATUS_LOG_PATH);
		knownRequiredKeys.add(StatusResource.STATUS_LOG_PATH);
		testMapAssertion(map, StatusResource.STATUS_PROGRESS_PATH);
		knownRequiredKeys.add(StatusResource.STATUS_PROGRESS_PATH);
		testMapAssertion(map, StatusResource.STATUS_PROPS_PATH);
		knownRequiredKeys.add(StatusResource.STATUS_PROPS_PATH);
		
		testMapAssertion(map, StatusResource.STATUS_THREADS_PATH);
		knownRequiredKeys.add(StatusResource.STATUS_THREADS_PATH);
		
		Set<String> foundResourcesPaths = map.getmap().keySet();
		
		//System.out.println("Found " + foundResourcesPaths.size() + " ResourcesPaths ");
	    foundResourcesPaths.removeAll(knownRequiredKeys);
	    //System.out.println("Found " + foundResourcesPaths.size() + " ResourcesPaths not tested by this unittest");
	    assertTrue("Found more resources in settings than we currently test for: " 
	    		+ StringUtils.join(foundResourcesPaths, ","), foundResourcesPaths.isEmpty());
	}
	
	/**
	 * Tests direct lookup and indirect lookup.
	 * @param map
	 * @param path
	 */
	public static void testMapAssertion(ResourcesMap map, String path) {
		ResourceDescription rd = map.getmap().get(path);
		assertFalse("Path '" + path + "' should be in ResourcesMap", rd == null);
		rd = map.getResourceByPath(path);
		assertFalse("Path '" + path + "' should be in ResourcesMap", rd == null);
	}
}
