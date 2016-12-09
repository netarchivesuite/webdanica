package dk.kb.webdanica.core.utils;

import dk.kb.webdanica.core.WebdanicaSettings;
import dk.kb.webdanica.core.Constants;

public class MailConf {

	private static MailConf instance;

	public synchronized static MailConf getInstance() {
		if (instance == null) {
			 instance = new MailConf();
		} 
		return instance;
	}

	private int smtpPort;
	private String smtpHost;
	private String mailAdmin;
	
	public MailConf() {
		smtpPort = SettingsUtilities.getIntegerSetting(WebdanicaSettings.MAIL_PORT, Constants.DEFAULT_MAIL_PORT);
		smtpHost = SettingsUtilities.getStringSetting(WebdanicaSettings.MAIL_SERVER, Constants.DEFAULT_MAIL_HOST);
		mailAdmin = SettingsUtilities.getStringSetting(WebdanicaSettings.MAIL_ADMIN, Constants.DEFAULT_MAIL_ADMIN);
	}
	
	public int getSmtpPort() {
		return smtpPort;
	}
	
	public String getSmtpHost() {
		return smtpHost;
	}
	public String getMailAdmin() {
		return mailAdmin;
	}

	public String getPassword() {
	    return null;
    }

	public String getUsername() {
		return null;
    }

	public boolean getDontSendMails() {
	    return false;
    }
}

