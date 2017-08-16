package dk.kb.webdanica.core.interfaces.harvesting;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dk.kb.webdanica.core.WebdanicaSettings;
import dk.kb.webdanica.core.datamodel.BlackList;
import dk.kb.webdanica.core.datamodel.dao.BlackListDAO;
import dk.kb.webdanica.core.utils.DatabaseUtils;
import dk.kb.webdanica.core.utils.SettingsUtilities;
import dk.netarkivet.harvester.datamodel.GlobalCrawlerTrapList;
import dk.netarkivet.harvester.datamodel.GlobalCrawlerTrapListDAO;


/**
 * Synchronizes the contents of the blacklists in webdanica with the global crawlertraps in the NAS system defined by NetarchiveSuite Settings
 * @author svc
 */
public class SynchronizeCrawlertraps {

    /**
     * When called, synchronizes the contents of the blacklists in webdanica with the global crawlertraps in the NAS system defined by NetarchiveSuite Settings
     * Dies if this does not point to a regular file: -Ddk.netarkivet.settings.file=/full/path/to/settingsfile
     * Dies if this does not point to a regular file: -Dwebdanica.settings.file=/full/path/to/webdanica_settingsfile
     * @throws Exception 
     * 
     */
    public static void main(String[] args) throws Exception {
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
        int maxTrapSize = SettingsUtilities.getIntegerSetting(WebdanicaSettings.CRAWLERTRAPS_MAX_TRAP_SIZE,
                1000);
        
        BlackListDAO dao = DatabaseUtils.getDao().getBlackListDAO();
        List<BlackList> lists = dao.getLists(false);
        
        GlobalCrawlerTrapListDAO gdao = GlobalCrawlerTrapListDAO.getInstance();
        for (BlackList b: lists) {
            ByteArrayInputStream bais = new ByteArrayInputStream(b.getName().getBytes()); // dummy inputStream
            GlobalCrawlerTrapList trap = new GlobalCrawlerTrapList(bais, b.getName(), b.getDescription(), b.isActive());
            Set<String> trapSet = new HashSet<String>();
            // Remove traps from list exceeding max trap size in NetarchiveSuite"
            // or if not valid xml
            int skipped=0;
            for (String t: b.getList()) {
                if (t.length() > maxTrapSize) {
                    System.out.println("Skipping trap w/size " + t.length() + " exceeding max trap size= " +  maxTrapSize + ": " + t);
                    skipped++;
                } else if (!CrawlertrapsUtils.isCrawlertrapsWellformedXML(t)) {
                    System.out.println("Skipping trap w/size " + t.length() + " - it is not valid XML: "
                            + t);
                    skipped++;
                } else {
                    trapSet.add(t);
                }
            } 
            System.out.println("Skipping " + skipped + " traps from list '" + b.getName() + "'. " + trapSet.size() + " in list");
            

            trap.setTraps(trapSet);
            if (gdao.exists(b.getName())) {
                gdao.update(trap);
            } else {
                int id = gdao.create(trap);
                System.out.println("Created trap in NAS with id=" + id + ", and name=" +  trap.getName() + ", isactive=" + b.isActive());
            }
        }
        System.out.println("Finished synchronizing the blacklists and the NAS GlobalCrawlerTrapLists");
    }
}
