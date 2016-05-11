package dk.kb.webdanica.utils;

import dk.kb.webdanica.WebdanicaSettings;

public class SettingsTester {

	/**
	 * -Dwebdanica.settings.file=/home/svc/devel/webdanica/webdanica-core/src/test/resources/settings.xml
	 * @param args
	 */
	public static void main(String[] args) {
		String[] ignoredSuffixes = Settings.getAll(WebdanicaSettings.IGNORED_SUFFIXES);
		for (String suffix: ignoredSuffixes) {
			System.out.println(suffix);
		}
		
	}
}
