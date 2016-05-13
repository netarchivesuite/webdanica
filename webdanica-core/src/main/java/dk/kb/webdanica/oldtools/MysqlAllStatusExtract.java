package dk.kb.webdanica.oldtools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.IOException;

import dk.kb.webdanica.oldtools.MysqlWorkFlow.HadoopResItem;

/*
String url = "jdbc:mysql://localhost/test|webdanica";
Class.forName ("com.mysql.jdbc.Driver").newInstance ();
Connection conn = DriverManager.getConnection (url, "username", "password");
*/

/*
extract status on dbMaschine
*/

public class MysqlAllStatusExtract {

    /**
     * @param args JDBC-URL jdbcUser=<JDBC-username>  dbmachine=<e.g. kb-test-webdania-001> datadir=<e.g. /data1/resultater/>   
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws SQLException 
     * @throws IOException 
     */
	public static void main(String[] args) throws  IOException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
        /*****************************************/
        /*** Arguments ***************************/
        /*****************************************/
        
    	String errArgTxt = "Proper args: <JDBC-URL> jdbcUser=<JDBC-username> "
    			+ "dbmachine=<e.g. kb-test-webdania-001> "
    			+ "datadir=<e.g. /data1/resultater/>";
    			//+ "ignoreFile=true|false|warning";
        if (args.length < 4) {
            System.err.println("Missing args!");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        if (args.length > 4) {
            System.err.println("Too many args!");
            System.err.println(errArgTxt);
            System.err.println("Got:");
            for (int i = 0; i < args.length; i++) {
            	 System.err.println(" - " + args[i]);
            }
            System.exit(1);
        }

        /**** args - jdbc ****/
        /** arg - url **/
        String jdbcUrl = args[0];
        if (!jdbcUrl.startsWith("jdbc:mysql:")) {
            System.err.println("Missing arg jdbc setting starting with 'jdbc:mysql'");
            System.err.println(errArgTxt);
            System.exit(1);
        }

        /** arg - user **/
        String jdbcUser = args[1];
        if (!jdbcUser.startsWith("jdbcUser=")) {
            System.err.println("Missing arg jdbcUser setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        jdbcUser = MysqlX.getStringSetting(jdbcUser).toLowerCase();
        
        Class.forName ("com.mysql.jdbc.Driver").newInstance ();
        Connection conn = DriverManager.getConnection (jdbcUrl, jdbcUser, "");    
        
        /**** args - db-machine ****/
        String dbMachine = args[2];
        if (!dbMachine .startsWith("dbmachine=")) {
            System.err.println("Missing arg dbmachine setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        dbMachine = MysqlX.getStringSetting(dbMachine);

        /**** args - data-dir ****/
        String datadirTxt = args[3];
        if (!datadirTxt.startsWith("datadir=")) {
            System.err.println("Missing arg datadir setting - got " + datadirTxt);
            System.err.println(errArgTxt);
            System.exit(1);
        }
        datadirTxt = MysqlX.getStringSetting(datadirTxt);
    	//System.out.println("datadirTxt: " + datadirTxt);

        /*String ignoreFileTxt = args[8];
        if (!ignoreFileTxt.startsWith("ignoreFile=")) {
            System.err.println("Missing arg ignoreFile setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        ignoreFileTxt = MysqlX.getStringSetting(ignoreFileTxt);
        IgnoreFile ignoreFile = IgnoreFile.if_false;
        if (ignoreFileTxt.equals("false"))  ignoreFile = IgnoreFile.if_false; 
        else if (ignoreFileTxt.equals("true")) ignoreFile = IgnoreFile.if_true; 
        else if (ignoreFileTxt.equals("warning")) ignoreFile = IgnoreFile.if_warning; 
        else {
            System.err.println("ERROR: Arg IgnoreFile setting is not valid - got '" + ignoreFileTxt + "'");
            System.err.println(errArgTxt);
            System.exit(1);
        }*/

        /*****************************************/
        /*** Start processing ********************/
        /*****************************************/

        Map<String,HadoopResItem> mergedItemMap = extractAllStatus(conn, dbMachine, datadirTxt);
        writeAllStatus(dbMachine, datadirTxt, mergedItemMap);

	}
	
	public static void writeAllStatus(String dbMachine, String datadirTxt, Map<String,HadoopResItem> mergedItemMap) throws IOException {
	    File dataDir = new File(datadirTxt);
	    if (!dataDir.isDirectory()) {
	        System.err.println("ERROR: The given data-dir '" + dataDir.getAbsolutePath() + "' is not a proper directory or does not exist");
	        System.exit(1);
	    }
	    String statdir = dataDir.getAbsolutePath() 
				+ "/" + MysqlX.statistics_dir
				+ "/" + MysqlX.statusextract_dir;
	    String statusFilename = statdir + "/" 
	    		+ getStatusFilename(dbMachine, MysqlIngester.ingest_current_update_no)  
	    		+ MysqlX.txtfile_suffix;
	    File statusFile = new File(statusFilename); 
		if (statusFile.exists()) { //otherwise it is allready produces
			System.out.println("WARNING: stat file was already produced: " + statusFile.getAbsolutePath());
		}
	    statusFile.createNewFile();
	    System.out.println("write file" );
	    writeStatusFile(statusFile, mergedItemMap);		
	}
    

	public static Map<String,HadoopResItem> extractAllStatus(Connection conn, String dbMachine, String datadirTxt) throws SQLException {
        /** Init items from tables / dirs */
        Set<String> tableSet = new HashSet<String>();
        Set<String> itemTableSet = new HashSet<String>();
        Set<HadoopResItem> itemSet = new HashSet<HadoopResItem>();
	   
        System.out.println("find tables: ");
        tableSet = MysqlRes.getTables(conn);
        
        
        for (String table : tableSet) { //init with m/d/p ...
	        if (table.startsWith(MysqlRes.wf_table_prefix)) {
	    	    //System.out.println("table " + table);
	        	HadoopResItem item = new HadoopResItem();
	        	item = MysqlWorkFlow.readItemFromTablename(table, datadirTxt, dbMachine, "");
	        	itemSet.add(item);
	        	itemTableSet.add(item.datasubdirname());
	        	//System.out.println("itemTableSet.size: " + itemTableSet.size());
	        }
        }

        //Check with dirs
        File dataDir = new File(datadirTxt);
        if (!dataDir.isDirectory()) {
            System.err.println("ERROR: The given data-dir '" + dataDir.getAbsolutePath() + "' is not a proper directory or does not exist");
            System.exit(1);
        }

        Set<String> itemFileSet = new HashSet<String>();
	    File[] dirfiles = dataDir.listFiles(); //all dirs in datadir
	    for (File dfile: dirfiles) {
	    	String name = dfile.getName();
	    	//System.out.println(name);
	    	if (name.startsWith("m")) {
	    	    //System.out.println("dir " + name);
	    		itemFileSet.add(name);
	    		//check corresponding table to dir
	    		String tabText = MysqlRes.wf_table_prefix + name.replace(MysqlWorkFlow.wf_dir_delim, MysqlWorkFlow.wf_table_delim);
	    		if (!(itemTableSet.contains(tabText))) {
		        	HadoopResItem item = new HadoopResItem();
		        	item = MysqlWorkFlow.readItemFromDataSubdirname(dfile.getAbsolutePath(), dbMachine, "");
		        	itemSet.add(item);
	    		} //update item.hasDir afterwards
	    	}
	    }
	    
	    for (HadoopResItem item : itemSet) {
    	    //System.out.println("item " + item.datasubdir.getName());
	    	if (itemFileSet.contains(item.datasubdir.getName())) {
	    		item.hasDir = true;
	    	}
	    	if (itemTableSet.contains(item.datasubdir.getName())) {
	        	item.hasTable = true;
	        	item.emptytable = (MysqlRes.getTableCntEntries(conn, item.tablename(),"") == 0);
	    	}
	    }

	    System.out.println("Before find items");
    	MysqlWorkFlow.ItemStatusInf itStatSets = new MysqlWorkFlow.ItemStatusInf();

    	System.out.println("Find items ");
        itStatSets.findStatusItemSetsInternal(itemSet, false);
        
    	System.out.println("Merge v5 items ");
    	Map<String,HadoopResItem> mergedItemMap = itStatSets.mergeStatusItemSetsInternal(itemSet); 
    	
    	/** update urllevelsearch field - only if table is url-extracted and all urls are searched */
	    List<Boolean> urlsSearched = new ArrayList<Boolean>();
        for (int level=1;level<=MysqlX.noDomainLevels;level++) 
        	urlsSearched.add(MysqlRes.anyMissingLookedUpBerk(conn, level));
    	for (int i=0;i<MysqlX.noDomainLevels;i++) {
    		if (urlsSearched.get(i)) {
    			for (HadoopResItem item : itemSet) item.urlLevelDbSearched.set(i, item.urlLevelExtracted.get(i));
    	    }
	    }
        return mergedItemMap;
	}
	
    public static String getStatusFilename( String dbMachine, String seqno) {
    	String s = MysqlX.status_fileprefix 
    			+ "M" + dbMachine 
    			+ "_V" + seqno; 
        return s;
    }
    
	public static void writeStatusFile(File writeFile, Map<String,HadoopResItem> itemMap) throws IOException {
		/** On form
		 * <title> # <date> #######...
		 * machine_no # disk_no # part_no # subpart/interval # hadoop_version # db-machine  # hasTable # emptytable--- 8/7
		 *  # hasDir # emptydatadir # extrafiles					       						 --- 3
		 *  # hadoopfileProduced # ingestfileProduced # updatedProduced # updateVersion       	 --- 3
		 *  # copiedOk # copyherefileProduced # copybackfileProduced                   			 --- 3
		 *  # compressedfileProduced # statProduced # statVersion                                --- 3  i.e 19/18
		 */
		
		//make write possible on file
		FileWriter fw = new FileWriter(writeFile.getAbsoluteFile());
        BufferedWriter  bw = new BufferedWriter(fw);        

		List<String> sortedKeys = new ArrayList<String>(itemMap.size());
		sortedKeys.addAll(itemMap.keySet());
        Collections.sort(sortedKeys);

		//init item set for write
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    	Date now = new Date(System.currentTimeMillis());
		for (String key : sortedKeys) {
			HadoopResItem item = itemMap.get(key);
			item.statusDate = dateFormat.format(now);
		}
		/* OBS may convert delim to item.getname(MysqlWorkFlow.wf_dir_delim) for key */ 

		//write
        for (String key: sortedKeys) {
			HadoopResItem it = itemMap.get(key);
			String s = it.getItemStatusLine();
			//System.out.println(" line: " + s);
			bw.write(s);
			bw.newLine();
		}
		bw.close();
	}
}