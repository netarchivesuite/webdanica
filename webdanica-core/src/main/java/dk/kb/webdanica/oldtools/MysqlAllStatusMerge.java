package dk.kb.webdanica.oldtools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.IOException;

import dk.kb.webdanica.oldtools.MysqlWorkFlow.HadoopResItem;
import dk.kb.webdanica.oldtools.MysqlWorkFlow.IgnoreFile;
import dk.kb.webdanica.oldtools.MysqlWorkFlow.ItemStatusInf;
import dk.kb.webdanica.oldtools.MysqlX.*;

/*
String url = "jdbc:mysql://localhost/test|webdanica";
Class.forName ("com.mysql.jdbc.Driver").newInstance ();
Connection conn = DriverManager.getConnection (url, "username", "password");
*/

/*
extract status on dbMaschine
*/

public class MysqlAllStatusMerge {

    /**
     * @param args datadir=<e.g. /data1/resultater/> version=<numberofversion> ignoreFile=true|false|warning
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws SQLException 
     * @throws IOException 
     */
	
	public static void main(String[] args) throws  IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        /*****************************************/
        /*** Arguments ***************************/
        /*****************************************/
		//def file has elements on form:
		//hadoop@kb-test-sol-001:/home/hadoop/disk7_instans_m004/combo-r1-m001-disk3_3-3-2-29-08-2014/,m001,disk3,3,3-2  or
		//hadoop@kb-test-sol-001:/home/hadoop/disk7_instans_m004/combo-r1-m001-disk3_3-3-2-29-08-2014/,m001,disk3,3,i00
        
    	String errArgTxt = "Proper args: "
    			+ "datadir=<e.g. /data1/resultater/>  "
    			+ "version=<numberofversion> "
    			+ "ignoreFile=true|false|warning "
    			+ " ";
        if (args.length < 3) {
            System.err.println("Missing args!");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        if (args.length > 3) {
            System.err.println("Too many args!");
            System.err.println(errArgTxt);
            System.err.println("Got:");
            for (int i = 0; i < args.length; i++) {
            	 System.err.println(" - " + args[i]);
            }
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

        String versionTxt = args[1];
        if (!versionTxt.startsWith("version=")) {
            System.err.println("Missing arg version=<numberofversion> setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        versionTxt = MysqlX.getStringSetting(versionTxt);
         
        String ignoreFileTxt = args[2];
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
        }
        
        /*****************************************/
        /*** Start processing ********************/
        /*****************************************/
        //Dir with stat 
        String statDirname = datadirTxt 
        		+ (datadirTxt.substring(datadirTxt.length()-1).equals("/") ? "" : "/" )
        		+ MysqlX.statistics_dir + "/";
        MysqlX.checkDir(statDirname);

        //Dirs with extracted stat files
        String statusDirname = statDirname + "/" + MysqlX.statusextract_dir;
        File statusDir = MysqlX.checkDir(statusDirname);

        //Dir with merged stat files
        String statusmergedirname = statDirname + "/" + MysqlX.statusextract_dir + MysqlX.merge_dir_suffix ;
        File statusmergeDir = MysqlX.checkDir(statusmergedirname);
        
        /*** Find files for merge ***/
        Set<File> statusFilesSet = MysqlStatMerge.findStatFiles(statusDir, MysqlX.status_fileprefix, DataSource.source_none, versionTxt, "all");
        
        /*** Read files into Merged status map ***/
        Map <String,HadoopResItem> statusItemMap =  new HashMap<String,HadoopResItem>();
        statusItemMap = mergeDataFromFiles(statusFilesSet);

        /*** Write Merged-status map ***/
    	System.out.println("*** Item status");
        if (statusItemMap.size()==0) {
        	System.out.println("- No status lines");
        } else {
	        String fn = writeItemStatusFile(statusmergeDir, statusItemMap, ignoreFile);
	    	System.out.println("- Status file: " + fn);
		}
        
        /*** Write Merged-compressed status map ***/
    	System.out.println("*** Compressed item status");
        if (statusItemMap.size()==0) {
        	System.out.println("- No status lines");
        } else {
	        String fn = writeCompressedItemStatusFile(statusmergeDir, statusItemMap, ignoreFile);
	    	System.out.println("- Status file: " + fn);
		}

        /****** 
        /** make stat */
    	System.out.println("*** General status");
        if (statusItemMap.size()==0) {
        	System.out.println("- No status lines");
        } else {
            Set<HadoopResItem> itemSet = new HashSet<HadoopResItem>();
            for (String key: statusItemMap.keySet()) {
            	HadoopResItem item = statusItemMap.get(key);
            	itemSet.add(item);
            }
        	ItemStatusInf itStatSets = new ItemStatusInf();
            itStatSets.findStatusItemSetsExternal(itemSet);
        	String[] resLines = itStatSets.writeStatusItemSets(itemSet.size());
        	String fn = writeListStatusFile(statusmergeDir, resLines);
	    	System.out.println("- Status file: " + fn);
		}
	}

	public static Map <String,HadoopResItem> mergeDataFromFiles(Set<File> statusFilesSet) throws IOException {
        Map <String,HadoopResItem> statusMap = new HashMap<String,HadoopResItem>();
        
	    for (File nextfile: statusFilesSet) {
	        BufferedReader fr = new BufferedReader(new FileReader(nextfile));        
	        String line;

	        while ((line = fr.readLine()) != null) {
	        	line = line.trim();
	            if (!line.isEmpty()) {
	            	HadoopResItem item = new HadoopResItem(line);
	            	String key = item.getname(MysqlWorkFlow.wf_dir_delim);
	            	statusMap.put(key, item);
            	}
        	}
	        fr.close();
	    } //for all files   
	    return statusMap;
	}
	
    public static File createMergedStatusFile(File MergeDir, Level level, Display display, IgnoreFile ignoreFile) throws IOException {
    	String fn = MysqlStatMerge.getMergedStatusFilename(MysqlIngester.ingest_current_update_no,  level, display) 
	    		+ MysqlX.txtfile_suffix;
	    File writeFile = new File(MergeDir.getAbsolutePath() + "/" + fn);
		if (writeFile.exists()) {
			if (ignoreFile.equals(IgnoreFile.if_false)) {
	            System.err.println("ERROR: merge file allready existed " + writeFile.getAbsolutePath() + writeFile.getName());
	            System.exit(1);
			} else if (ignoreFile.equals(IgnoreFile.if_warning)) {
	            System.out.println("WARNING: merge file allready existed " + writeFile.getAbsolutePath());
			} 
		} 
		writeFile.createNewFile();
		return writeFile;
	} 

    public static File createMergedCompressStatusFile(File MergeDir, Level level, Display display, IgnoreFile ignoreFile) throws IOException {
    	String fn = MysqlStatMerge.getMergedStatusFilename(MysqlIngester.ingest_current_update_no,  level, display) + "_compressed"
	    		+ MysqlX.txtfile_suffix;
	    File writeFile = new File(MergeDir.getAbsolutePath() + "/" + fn);
		if (writeFile.exists()) {
			if (ignoreFile.equals(IgnoreFile.if_false)) {
	            System.err.println("ERROR: merge file allready existed " + writeFile.getAbsolutePath() + writeFile.getName());
	            System.exit(1);
			} else if (ignoreFile.equals(IgnoreFile.if_warning)) {
	            System.out.println("WARNING: merge file allready existed " + writeFile.getAbsolutePath());
			} 
		} 
		writeFile.createNewFile();
		return writeFile;
	} 

    public static String writeItemStatusFile(File mergedDir, Map<String,HadoopResItem> itemMap, IgnoreFile ignoreFile) throws IOException {
    	//sort key set
		List<String> sortedKeys = new ArrayList<String>(itemMap.size());
        sortedKeys.addAll(itemMap.keySet());
        Collections.sort(sortedKeys);
        
        //make file to write status , 
        File lf = createMergedStatusFile(mergedDir, Level.allcodes, Display.onlyCodes, ignoreFile);
		FileWriter fw = new FileWriter(lf.getAbsoluteFile());
        BufferedWriter  bw = new BufferedWriter(fw);        

        //write header 
        HadoopResItem dummy = itemMap.get(sortedKeys.get(0));

        bw.write(dummy.getItemStatusFileHeader());
    	bw.newLine();

    	bw.write(dummy.getItemStatusItemHeader());
    	bw.newLine();

    	for (String key: sortedKeys) {
    		bw.write(itemMap.get(key).getItemStatusLine());
			bw.newLine();
		}

    	bw.close();
        return lf.getAbsolutePath();
	}

    public static class ItemCompressed {
    	public String key = "";
    	public Set<HadoopResItem> itemSet = new HashSet<HadoopResItem>();
    	public boolean inclInterval = false;
    	public boolean inclIntervalV5 = false;
    	public Set<String> intervalNoSet = new HashSet<String>();
    	public int intStart = 0;
    	public int intEnd = -1;
    	public int intCnt = 0;
    	public Set<Integer> noSet = new HashSet<Integer>();
    	public int noStart = 0;
    	public int noEnd = -1;
    	public int noCnt = 0;
    	public Set<Integer> v5Set = new HashSet<Integer>();
    	public int v5Cnt = 0;
    	
    	
    	public String getItemStatusLine() {
        	return 
        	key + MysqlX.row_delim 
        	+ inclInterval + MysqlX.row_delim 
        	+ inclIntervalV5 + MysqlX.row_delim
        	+ intStart  + MysqlX.row_delim
        	+ intEnd  + MysqlX.row_delim
        	+ intCnt  + MysqlX.row_delim        	
        	+ (intCnt==(intEnd-intStart+1)?"Interval ok":"Interval ERROR") + MysqlX.row_delim        	

        	+ noStart  + MysqlX.row_delim
        	+ noEnd  + MysqlX.row_delim
        	+ noCnt  + MysqlX.row_delim        	
        	+ (noCnt==(noEnd-noStart+1)?"Seq ok":"Seq ERROR") + MysqlX.row_delim        	
        	
        	+ v5Cnt  + MysqlX.row_delim        	
        	+ (v5Cnt==noCnt?"V5 ok":"V5 ERROR");        	
    	}

    	public String getItemStatusHeader() {
        	return 
        	"key" + MysqlX.row_delim 
        	+ "Has Interval?" + MysqlX.row_delim 
        	+ "V5 for Interval?" + MysqlX.row_delim
        	+ "Interval Start"  + MysqlX.row_delim
        	+ "Interval End"  + MysqlX.row_delim
        	+ "iInterval Cnt"  + MysqlX.row_delim        	
        	+ "Interval status" + MysqlX.row_delim        	

        	+ "Seq Start" + MysqlX.row_delim
        	+ "Seq End" + MysqlX.row_delim
        	+ "Seq Cnt"  + MysqlX.row_delim        	
        	+ "Seq status" + MysqlX.row_delim        	
        	
        	+ "v5 count"  + MysqlX.row_delim        	
        	+ "V5 status";  
    	}

    }

    public static String writeCompressedItemStatusFile(File mergedDir, Map<String,HadoopResItem> itemMap, IgnoreFile ignoreFile) throws IOException {
    	//sort key set
    	Map<String,ItemCompressed> itcompMap = new HashMap<String,ItemCompressed>();
    	
    	for (String key: itemMap.keySet()) {
    		HadoopResItem it = itemMap.get(key);
    		String newkey = "m" + itemMap.get(key).machine_no + "_d" + itemMap.get(key).diskTld_no + "_p" + itemMap.get(key).part_no;
    		ItemCompressed act_compitem = new ItemCompressed();
    		
    		if (itcompMap.containsKey(newkey)) act_compitem = itcompMap.get(newkey);

    		act_compitem.itemSet.add(it);
    		
    		if (act_compitem.key.isEmpty()) {
    			act_compitem.key = newkey;
    			itcompMap.put(act_compitem.key, act_compitem);
    		}
		}

    	for (String key: itcompMap.keySet()) {
    		ItemCompressed itc = itcompMap.get(key);
            //System.out.println("key: " + key);
        	
    		for (HadoopResItem it : itc.itemSet) {
        		if (!it.interval.isEmpty()) {
        			itc.inclInterval = true;
        			itc.intervalNoSet.add(it.interval);     			
        			itc.inclIntervalV5 = !it.hadoop_version.isEmpty();
                    //System.out.println("it.interval: " + it.interval);
        		} else {
        			itc.noSet.add(Integer.decode(it.subpart));
        			if (!it.hadoop_version.isEmpty()) itc.v5Set.add(Integer.getInteger(it.subpart));
        		}
        	}
        	
    		List<String> sortedIntervals = new ArrayList<String>(itc.intervalNoSet.size());
    		sortedIntervals.addAll(itc.intervalNoSet);
            Collections.sort(sortedIntervals);
            
            List<Integer> sortedNos = new ArrayList<Integer>(itc.noSet.size());
    		sortedNos.addAll(itc.noSet);
            Collections.sort(sortedNos);
            
    		//List<Integer> sortedV5s = new ArrayList<Integer>(itc.v5Set.size());
    		//sortedV5s.addAll(itc.v5Set);
            //Collections.sort(sortedV5s);
            

            itc.noCnt = sortedNos.size();
            //System.out.println("noCnt: " + itc.noCnt);
            if (itc.noCnt>0) {
	            itc.noStart = sortedNos.get(0);
	            itc.noEnd = sortedNos.get(sortedNos.size()-1);
            }
        	
            itc.intCnt = sortedIntervals.size();
            //System.out.println("intCnt: " + itc.intCnt);
            if (itc.intCnt >0) {
	            itc.intStart = Integer.decode(sortedIntervals.get(0));
	            String s = sortedIntervals.get(sortedIntervals.size()-1);
	            if (s.startsWith("0")) s = s.substring(1);
	            //System.out.println("sortedIntervals.get(sortedIntervals.size()-1): " + s);
	            itc.intEnd = (int) Integer.decode(s);
            }
        	
            itc.v5Cnt = itc.v5Set.size();
    	}

		List<String> sortedKeys = new ArrayList<String>(itcompMap.keySet().size());
        sortedKeys.addAll(itcompMap.keySet());
        Collections.sort(sortedKeys);
    	
        //make file to write status , 
        File lf = createMergedCompressStatusFile(mergedDir, Level.allcodes, Display.onlyCodes, ignoreFile);
		FileWriter fw = new FileWriter(lf.getAbsoluteFile());
        BufferedWriter  bw = new BufferedWriter(fw);        

        //write header 
        ItemCompressed dummy = new ItemCompressed();
        bw.write(dummy.getItemStatusHeader());
    	bw.newLine();
    	

    	for (String key: sortedKeys) {
    		ItemCompressed itc = itcompMap.get(key);

			bw.write(itc.getItemStatusLine());
			bw.newLine();
		}
    	
    	
    	bw.close();
        return lf.getAbsolutePath();
	}

    public static String writeListStatusFile(File mergedDir, String[] resLines) throws IOException {
	    //make file to write status
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date now = new Date(System.currentTimeMillis());
		String fn = MysqlX.status_fileprefix 
				+ "general_"
				+ "V" + MysqlIngester.ingest_current_update_no + "_"
				+ "T" + dateFormat.format(now)
	    		+ MysqlX.txtfile_suffix;
	    File writeFile = new File(mergedDir.getAbsolutePath() + "/" + fn);
		if (writeFile.exists()) {
			IgnoreFile ignoreFile = IgnoreFile.if_warning;
			if (ignoreFile.equals(IgnoreFile.if_false)) {
	            System.err.println("ERROR: merge file allready existed " + writeFile.getAbsolutePath() + writeFile.getName());
	            System.exit(1);
			} else if (ignoreFile.equals(IgnoreFile.if_warning)) {
	            System.out.println("WARNING: merge file allready existed " + writeFile.getAbsolutePath());
			} 
		} 
		writeFile.createNewFile();
		
		FileWriter fw = new FileWriter(writeFile.getAbsoluteFile());
	    BufferedWriter  bw = new BufferedWriter(fw);        
	
		for (String l: resLines) {
			if (l==null) break;
			bw.write(l);
			bw.newLine();
		}
	
		bw.close();
	    return writeFile.getAbsolutePath();
	}
}
