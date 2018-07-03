package dk.kb.webdanica.core.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import dk.kb.webdanica.core.Constants;
import dk.kb.webdanica.core.WebdanicaSettings;
import dk.kb.webdanica.core.datamodel.BlackList;
import dk.kb.webdanica.core.datamodel.dao.BlackListDAO;
import dk.kb.webdanica.core.datamodel.dao.DAOFactory;
import dk.kb.webdanica.core.datamodel.dao.HBasePhoenixDAOFactory;
import dk.kb.webdanica.core.utils.DatabaseUtils;
import dk.kb.webdanica.core.utils.SettingsUtilities;

/**
 * Program to load blacklists into the webdanica database.
 *  
 *  Remember to call program with -Dwebdanica.settings.file=$LOADSEEDS_HOME/webdanica_settings_file
 *  and -Ddk.netarkivet.settings.file=$LOADSEEDS_HOME/settings_NAS_Webdanica.xml
 *  and -Dlogback.configurationFile=$LOADSEEDS_HOME/silent_logback.xml (to avoid logs to STDOUT)
 *  TESTED on files
 *   webdanica-core/src/test/resources/special_traps.txt
 *   webdanica-core/src/test/resources/standard_netarkivet_traps.txt
 *
 *  
 *  Usage LoadBlackLists <file>
 *  TODO add a description to the arguments  (currently the description is "")
 *  See WEBDAN-243
 */  
public class LoadBlacklists {

	private File blacklistfile;
    private DAOFactory daoFactory;

    /**
	 * Constructor for the LoadBlackLists tool.
	 * @param blacklistfile the file to insert in the database
	 */
	public LoadBlacklists(File blacklistfile) {
	   this.blacklistfile = blacklistfile;
       String databaseSystem = SettingsUtilities.getStringSetting(WebdanicaSettings.DATABASE_SYSTEM, Constants.DEFAULT_DATABASE_SYSTEM);
       if ("hbase-phoenix".equalsIgnoreCase(databaseSystem)) {
           daoFactory = new HBasePhoenixDAOFactory();
       }
    }

	public static void main(String[] args) throws Exception {		
		if (args.length != 1) {
			System.err.println("Need blacklist file as argument");
			System.exit(1);
		}
		File seedsfile = new File(args[0]);
		if (!seedsfile.isFile()){
			System.err.println("The blacklist file located '" + seedsfile.getAbsolutePath() + "' does not exist or is not a proper file");
			System.exit(1);
		}
		
		System.out.println("Inserting blacklists from file '" + seedsfile.getAbsolutePath() + "'");

		LoadBlacklists loadseeds = new LoadBlacklists(seedsfile);
		loadseeds.insertList();
		
		BlackListDAO dao = DatabaseUtils.getDao().getBlackListDAO();
		System.out.println("Showing all existing blacklists:");
		List<BlackList> allLists = dao.getLists(false);
		for (BlackList b: allLists) {
			System.out.println(b);
		}
		System.out.println("Showing all existing active blacklists:");
		List<BlackList> activeListOnly = dao.getLists(true);
		for (BlackList b: activeListOnly) {
			System.out.println(b);
		}
		dao.close();
	}
	   
	
	/**
	 * Insert a blacklist.
	 */
	public void insertList() {
		BlackListDAO bdao = daoFactory.getBlackListDAO();
		String line;
        long entries=0L;
        long empty=0L;
        String trimmedLine = null;
        BufferedReader fr = null;
        String name = blacklistfile.getName();
        List<String> blackListRegexp = new ArrayList<String>();
        try {
        	fr = new BufferedReader(new FileReader(blacklistfile));
	        while ((line = fr.readLine()) != null) {
	            trimmedLine = line.trim();
	            if (trimmedLine.isEmpty()) {
	            	empty++;
	            } else {
	            	entries++;
	            	blackListRegexp.add(trimmedLine);
	            }
	        }
	      //TODO as suggested in WEBDAN-243, the description and isActive arguments to the BlackList constructor should 
	      // be taken from the arguments to the tool at least optionally.
	      BlackList b = new BlackList(name, "", blackListRegexp, true);
	      bdao.insertList(b);
	      System.out.println("Created blacklist with name='" + name + "' and #entries=" + entries + ", ignored empty lines: " + empty);
        } catch (Throwable e) {
	        e.printStackTrace();
        } finally {
        	IOUtils.closeQuietly(fr);
        }
       
}
}