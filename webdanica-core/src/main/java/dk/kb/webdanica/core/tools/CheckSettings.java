package dk.kb.webdanica.core.tools;

import java.io.File;

import dk.kb.webdanica.core.utils.SettingsUtilities;

public class CheckSettings {

    public static void main(String[] args) {
        
        String settingsFileSetting = "dk.netarkivet.settings.file";
        String settingsFilePath = System.getProperty(settingsFileSetting);
        if (settingsFilePath == null) {
            System.out.println("Undefined setting: " + settingsFileSetting);
            System.exit(1);
        }
        File settingsFile = new File(settingsFilePath);
        if (!settingsFile.isFile()) {
            System.out.println("NAS settingsfile not found or directory: " + settingsFile.getAbsolutePath());
            System.exit(1);
        }
        // Verify the existence of Webdanica_settings also
        String webdanicaSettingsFileSetting = "dk.netarkivet.settings.file";
        String webdanicaSettingsFilePath = System.getProperty(webdanicaSettingsFileSetting);
        if (webdanicaSettingsFilePath == null) {
            System.out.println("Undefined setting: " + webdanicaSettingsFileSetting);
            System.exit(1);
        }
        File webdanicaSettingsFile = new File(webdanicaSettingsFilePath);
        if (!webdanicaSettingsFile.isFile()) {
            System.out.println("Webdanica settingsfile not found or directory: " + webdanicaSettingsFile.getAbsolutePath());
            System.exit(1);
        }
        boolean valid = SettingsUtilities.isValidSimpleXmlSettingsFile(webdanicaSettingsFile, true);
        System.out.println("Settingsfile '" + webdanicaSettingsFile.getAbsolutePath() + "' is " 
                + (valid?"valid": "not valid"));
        
        valid = SettingsUtilities.isValidSimpleXmlSettingsFile(settingsFile, true);
        System.out.println("Settingsfile '" + settingsFile.getAbsolutePath() + "' is " 
                + (valid?"valid": "not valid"));
        
        
    }

}
