package dk.kb.webdanica.core.utils;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.Test;

public class MailConfTester {

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testMailConfconstructor() {
	    MailConf mc = new MailConf();
	    assertFalse("getDontSendMails() should never be true", mc.getDontSendMails());
	    assertTrue("getPassword() should be null", mc.getPassword() == null);
	    assertTrue("getUsername() should be null", mc.getUsername() == null);
	    /* Note, if these defaults are changed, change this unittest also
	    WARNING: The setting 'settings.mail.port' is not defined in the settingsfile. Using the default value: '25'
        WARNING: The setting 'settings.mail.host' is not defined in the settingsfile. Using the default value: 'localhost'
        WARNING: The setting 'settings.mail.admin' is not defined in the settingsfile. Using the default value: 'test@localhost'
	    */
	    
	    assertEquals("mail.port should be 25", mc.getSmtpPort(), 25);
	    assertEquals("mail.host should be localhost", mc.getSmtpHost(), "localhost");
	    assertEquals("mail.admin should be test@localhost", mc.getMailAdmin(), "test@localhost");
	}
}
