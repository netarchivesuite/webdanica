package dk.kb.webdanica.oldtools;

import java.io.File;
import java.sql.SQLException;
import java.io.IOException;

public class MysqlDomainStatsMerge {
    /**
     * @param args <JDBC-URL> datadir=<dir for out-files> ignoreFile=true|false|warning    
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws SQLException 
     * @throws IOException 
     */

    // output on form Url # code # forklaring af code?????
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, SQLException {
        /*****************************************/
        /*** Arguments ***************************/
        /*****************************************/
    	String errArgTxt = "Proper args: <JDBC-URL> jdbcUser=<JDBC-username> "
    			+ "machine=<machine-name> "
    			+ "datadir=<data dir where 'url-dir' (for out-files) exixts> "
    			+ "level=all|1|2|3 ignoreFile=true|false|warning";
        if (args.length < 2) {
            System.err.println("Missing args!");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        if (args.length > 2) {
            System.err.println("Too many args!");
            System.err.println(errArgTxt);
            System.exit(1);
        }

        /**** args - data-dir ****/
        String datadirTxt = args[0];
        if (!datadirTxt.startsWith("datadir=")) {
            System.err.println("Missing arg datadir setting - got " + datadirTxt);
            System.err.println(errArgTxt);
            System.exit(1);
        }
        datadirTxt = MysqlX.getStringSetting(datadirTxt);
    	//System.out.println("datadirTxt: " + datadirTxt);
        File dataDir = new File(datadirTxt);
        if (!dataDir.isDirectory()) {
            System.err.println("ERROR: The given data-dir '" + dataDir.getAbsolutePath() + "' is not a proper directory or does not exist");
            System.err.println(errArgTxt);
            System.exit(1);
        }
		File domMergeDir =  new File(dataDir.getAbsolutePath() + "/" + MysqlX.statistics_dir + "/" + MysqlX.domainextract_dir + MysqlX.merge_dir_suffix);
        if (!domMergeDir.isDirectory()) {
            System.err.println("ERROR: The given data-dir does not contains file '" + domMergeDir.getAbsolutePath() + "'");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        
        String ignoreFileTxt = args[1];
        if (!ignoreFileTxt.startsWith("ignoreFile=")) {
            System.err.println("Missing arg ignoreFile setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        /*ignoreFileTxt = MysqlX.getStringSetting(ignoreFileTxt);
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
    }
        //statfiles  one for each macine - ione for each level
        File[] datalevelFiles = new File[MysqlX.noDomainLevels];
        File[] statuslevelFiles = new File[MysqlX.noDomainLevels];
        /*
        for (int level: levels) {
        	int i = level -1;
            File domainDataExtractFile = 
            		new File(domDir.getAbsoluteFile() 
            		          + getExtractDomainFilename(machine, "Data_", level)
            		          + MysqlWorkFlow.wf_txt_suffix );
            datalevelFiles[i] = domainDataExtractFile;
            File domainStatusExtractFile = 
            		new File(domDir.getAbsoluteFile() 
            		          + getExtractDomainFilename(machine, "Status_", level)
            		          + MysqlWorkFlow.wf_txt_suffix );
            statuslevelFiles[i-1] = domainStatusExtractFile;
        }
        for (File f: datalevelFiles) createFile(f, ignoreFile);        	
        for (File f: statuslevelFiles) createFile(f, ignoreFile);

        
        DomStat[] stats = new DomStat[MysqlX.noDomainLevels];
        for (int level=1;level<=MysqlX.noDomainLevels;level++) {
        	int i = level-1;
        	stats[i].level = level;
        	stats[i].cntDomains = MysqlRes.getTableCntEntries(conn, MysqlRes.domainTableName(level), "");
        	stats[i].IA_NAS = MysqlRes.getTableCntEntries(conn, MysqlRes.domainTableName(level), 
        						"InIaData=true AND InNasData=true"); 
        	stats[i].IA_notNAS = MysqlRes.getTableCntEntries(conn, MysqlRes.domainTableName(level), 
        						"InIaData=true AND InNasData=false");
        	stats[i].notIA_NAS = MysqlRes.getTableCntEntries(conn, MysqlRes.domainTableName(level), 
        						"InIaData=false AND InNasData=true");
        	stats[i].notIA_notNAS = MysqlRes.getTableCntEntries(conn, MysqlRes.domainTableName(level), 
        						"InIaData=false AND InNasData=false");
        	stats[i].NAS_count = MysqlRes.getTableCntEntries(conn, MysqlRes.domainTableName(level), 
								"InNasData=true"); 
        	stats[i].NAS_lookedUp = MysqlRes.getTableCntEntries(conn, MysqlRes.domainTableName(level), 
								"InNasData=true AND LookedUpInBerkeley=true");
        	stats[i].NAS_existsInBerkeley = MysqlRes.getTableCntEntries(conn, MysqlRes.domainTableName(level), 
								"InNasData=true AND LookedUpInBerkeley=true AND CntInBerkeley>0");
        	stats[i].IA_count = MysqlRes.getTableCntEntries(conn, MysqlRes.domainTableName(level), 
								"InIaData=true");
        	stats[i].IA_lookedUp = MysqlRes.getTableCntEntries(conn, MysqlRes.domainTableName(level), 
								"InIaData=true AND LookedUpInBerkeley=true");
        	stats[i].IA_existsInBerkeley = MysqlRes.getTableCntEntries(conn, MysqlRes.domainTableName(level), 
								"InIaData=true AND LookedUpInBerkeley=true AND CntInBerkeley>0");
        }
        conn.close();

        for (int level=1;level<=MysqlX.noDomainLevels;level++) {
        	int i = level-1;
        	writeDomainStatusFile(stats[i], statuslevelFiles[i], level);
        	writeDomainDataFile(stats[i], datalevelFiles[i], level);
        }
    }
    
    public static boolean writeDomainStatusFile(DomStat domStat, File f, int level) throws IOException {
        // Stat 1: Update status for all
        // #entries, #ia/not nas, #not ia/nas, #ia/nas, # not ia/not nas (not possible)
		FileWriter fw = new FileWriter(f.getAbsoluteFile());
        BufferedWriter  bw = new BufferedWriter(fw);        

        String header = " No. entries " + MysqlX.row_delim 
        			+   "     IA and     NAS " + MysqlX.row_delim 
        			+   " not IA and     NAS " + MysqlX.row_delim 
        			+   "     IA and not NAS " + MysqlX.row_delim 
        			+   " not IA and not NAS " + MysqlX.row_delim 
        			;
		bw.write(header);
		bw.newLine();

        String data = domStat.cntDomains + MysqlX.row_delim 
    			+  domStat.IA_NAS    + MysqlX.row_delim 
    			+  domStat.notIA_NAS + MysqlX.row_delim 
    			+  domStat.IA_notNAS + MysqlX.row_delim 
    			+  domStat.notIA_notNAS + MysqlX.row_delim 
    			;
		bw.write(data);
		bw.newLine();
		
		bw.close();
    	return true;
    }
    
    public static boolean writeDomainDataFile(DomStat domStat, File f, int level) throws IOException {
        // Stat 2+3 : In berkely for IA and NAS
        // #entries, #lookedup, #exists
		FileWriter fw = new FileWriter(f.getAbsoluteFile());
        BufferedWriter  bw = new BufferedWriter(fw);        

        String header = " Source " + MysqlX.row_delim 
        			+   " No. for source "  + MysqlX.row_delim 
        			+   " Looked-up " + MysqlX.row_delim 
        			+   " is in berkeley " + MysqlX.row_delim 
        			;
		bw.write(header);
		bw.newLine();

		String data = "";
		data = Source.NAS.name() + MysqlX.row_delim 
    		    + domStat.NAS_count + MysqlX.row_delim 
    			+  domStat.NAS_lookedUp + MysqlX.row_delim 
    			+  domStat.NAS_existsInBerkeley + MysqlX.row_delim 
    			;
		bw.write(data);
		bw.newLine();
        data = Source.IA.name() + MysqlX.row_delim 
    		    +  domStat.IA_count + MysqlX.row_delim 
    			+  domStat.IA_lookedUp + MysqlX.row_delim 
    			+  domStat.IA_existsInBerkeley + MysqlX.row_delim 
    			;
		bw.write(data);
		bw.newLine();
		
		bw.close();
    	return true;
    }

    public static String getExtractDomainFilename(String machine, String infix, int level) {
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    	Date now = new Date(System.currentTimeMillis());
    	String s = MysqlX.domain_fileprefix 
    			+ infix
    			+ "M" + machine + "_"
        		+ "L" + level + "_"
				+ "T" + dateFormat.format(now);
        return s;
    }
    
    public static String getMergedDomainFilename(StatType statType, String seqno, DataSource src, Level level, Display d) {
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    	Date now = new Date(System.currentTimeMillis());
    	String s = MysqlX.domain_fileprefix 
    			+ (src.equals(DataSource.source_IA) 
    				? MysqlX.IA_infix 
    				: ( src.equals(DataSource.source_NAS)
    					 ? MysqlX.NAS_infix 
    					 : "")
    			   )
        		+ "L" + level + "_"
				+ "T" + dateFormat.format(now);
        return s;
    }
	
    private static boolean createFile(File f, IgnoreFile ignoreFile) {
		if (f.exists()) {
			if (ignoreFile.equals(IgnoreFile.if_false)) {
	            System.err.println("ERROR: merge file allready existed " + f.getAbsolutePath() + f.getName());
	            System.exit(1);
			} else if (ignoreFile.equals(IgnoreFile.if_warning)) {
	            System.out.println("WARNING: merge file allready existed " + f.getAbsolutePath());
			} 
		} 
		return true;
	} 

    public static class DomStatFiles {
        // Stat 1: Update status for all
        // #entries, #ia/not nas, #not ia/nas, #ia/nas, # not ia/not nas (not possible)

        // Stat 2+3 : In berkely for IA and NAS
        // #entries, #lookedup, #exists
    	public int level = 0;
    	public long cntDomains = 0;
    	public long IA_NAS = 0;
    	public long IA_notNAS = 0;
    	public long notIA_NAS = 0;
    	public long notIA_notNAS = 0;
    	public long NAS_count = 0;
    	public long NAS_lookedUp = 0;
    	public long NAS_existsInBerkeley = 0;
    	public long IA_count = 0;
    	public long IA_lookedUp = 0;
    	public long IA_existsInBerkeley = 0;
    }*/
}
