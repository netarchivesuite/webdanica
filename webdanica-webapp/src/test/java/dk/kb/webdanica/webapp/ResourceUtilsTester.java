package dk.kb.webdanica.webapp;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.Test;

import dk.kb.webdanica.webapp.resources.ResourceUtils;
import dk.kb.webdanica.webapp.resources.SeedRequest;
import dk.kb.webdanica.webapp.resources.SeedsResource;

public class ResourceUtilsTester {

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testGetUrlFromPathinfo() {
		String samplePath = "/seed/aHR0cDovL3Jpc2FnZXIuaW5mby8=/";
		SeedRequest sr = ResourceUtils.getUrlFromPathinfo(samplePath, 
				SeedsResource.SEED_PATH);
		System.out.println(sr.getUrl());
		System.out.println(sr.getNewState());
	}

}
