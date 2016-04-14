package dk.kb.webdanica.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import dk.kb.webdanica.tools.MysqlWorkFlow.HadoopResItem;
import dk.kb.webdanica.tools.MysqlWorkFlow.ItemNameType;
import dk.kb.webdanica.tools.MysqlWorkFlow.ItemNameVersion;

/*
String url = "jdbc:mysql://localhost/test|webdanica";
Class.forName ("com.mysql.jdbc.Driver").newInstance ();
Connection conn = DriverManager.getConnection (url, "username", "password");
*/

/*
Update TABLE ResFromHadoop with C15b varchar(20) with TLD
*/

public class MysqlNewHadoopDistribute {
    /**
     * @param args statusfile=<fullpath> outdir=<dir for produced files> suffix=<for prodeced file, eg. date> v5file=<fullpath> interval=true|false 
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws SQLException 
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException, FileNotFoundException, InstantiationException, IllegalAccessException, ClassNotFoundException {
    	String errArgTxt = "Proper args: "
    			+ "statfile=<fullpath> "
    			+ "outdir=<dir for produced files> "
    			+ "suffix=<for prodeced file, eg. date> "
    			+ "v5file=<fullpath> "
				+ "interval=true|false ";
    	////////////////////////////////////////////////////////
    	// Read and check arguments
        if (args.length < 5) {
            System.err.println("Missing args!");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        if (args.length > 5) {
            System.err.println("Too many args.");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        
        String statusFileTxt = args[0];
        if (!statusFileTxt .startsWith("statusfile=")) {
            System.err.println("Missing arg statusfile setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        File statusFile = new File(statusFileTxt.substring("statusfile=".length()));
        if (!statusFile.isFile()) {
            System.err.println("The given statfile '" + statusFile.getAbsolutePath() + "' is not a proper file or does not exist");
            System.err.println(errArgTxt);
            System.exit(1);
        }

        String outdirTxt = args[1];
        if (!outdirTxt .startsWith("outdir=")) {
            System.err.println("Missing arg outdir setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        File outDir = new File(outdirTxt.substring("outdir=".length()));
        if (!outDir.isDirectory()) {
            System.err.println("The given outDir '" + outDir.getAbsolutePath() + "' is not a proper directory or does not exist");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        
        String suffixTxt = args[2]; //suffix=<for prodeced files, eg. date> 
        if (!suffixTxt .startsWith("suffix=")) {
            System.err.println("Missing arg suffix setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        suffixTxt=suffixTxt.substring("suffix=".length());
        
        if (args.length == 3) {
            System.err.println("Missing arg v5file setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }

        File v5File = new File ("");
        String v5prefixWithPath = "";
        String v5suffix = "";
        String v5FileTxt = args[3];
        if (!v5FileTxt .startsWith("v5file=")) {
            System.err.println("Missing arg v5file setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        v5FileTxt = v5FileTxt.substring("v5file=".length());
        v5File = new File(v5FileTxt);
        if (!v5File.isFile()) {
            System.err.println("The given v5file '" + v5File.getAbsolutePath() + "' is not a proper file or does not exist");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        v5prefixWithPath = v5FileTxt;
        if (v5prefixWithPath.endsWith(MysqlX.txtfile_suffix)) {
        	v5prefixWithPath = v5prefixWithPath.substring(0, v5prefixWithPath.length()- MysqlX.txtfile_suffix.length());
        	v5suffix = MysqlX.txtfile_suffix;
        }

        String isIntervalTxt = args[4];
        if (!isIntervalTxt .startsWith("interval=")) {
            System.err.println("Missing arg interval setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        boolean isInterval=MysqlX.getBoleanSetting(isIntervalTxt);

        ////////////////////////////////////////////////////////
        // Process
        ////////////////////////////////////////////////////////
        
        /* Init status file */
        System.out.println("*** Getting: file=" + statusFile.getName());
        StatusRes status = getNewStatusFile(statusFile);
        /*String q = "m200-dB-p1-7";
        System.out.println("status.newStatusMap.containsKey(" + q + ") :" + status.newStatusMap.containsKey(q));
        q = "m200-dB-p1-8";
        System.out.println("status.newStatusMap.containsKey(" + q + ") :" + status.newStatusMap.containsKey(q));
        q = "m200-dB-p1-9";
        System.out.println("status.newStatusMap.containsKey(" + q + ") :" + status.newStatusMap.containsKey(q));*/
        
        //for (String s: status.newStatusMap.keySet()) { System.out.println("key: " + s + " status: " +status.newStatusMap.get(s)); }
        
        /* Init V5 items */
        Set <HadoopResItem> V5ItemSet = MysqlWorkFlow.readItemsFromDefFile(v5File, "", "");
        System.out.println("no. items in  V5file: " + V5ItemSet.size());
        //for (HadoopResItem it: itemSet) { System.out.println(it.defFileLine); }
        
        /* find where items belong */
        for (HadoopResItem v5it: V5ItemSet) {
        	Set<String> keySet = new HashSet<String>();
        	if (isInterval) {
            	String key = v5it.getBasicName(MysqlWorkFlow.wf_dir_delim, ItemNameType.inteval_item, ItemNameVersion.V1);
        		if (!key.endsWith(MysqlWorkFlow.wf_interval_prefix + "99")) { 
                    System.err.println("ERROR item should be interval: " + v5it.getname(MysqlWorkFlow.wf_dir_delim));
                    System.exit(1);
        		}
        		key = key.replace(MysqlWorkFlow.wf_interval_prefix + "99", "");
            	Set<String> tmp_keySet = new HashSet<String>();
        		for (String k: status.newStatusMap.keySet()) { // on form m<m>-d<d>-p<p>-i99
        			if (k.startsWith(key)) tmp_keySet.add(k);
        		}
        		if (tmp_keySet.isEmpty()) {
	                System.out.println("Key NOT found 2 (interval): " + key);
	        		status.testUNKNOWNv5Contents.add(v5it.defFileLine);
        		} else {
            		for (String k: tmp_keySet) {
            			keySet.add(k);
            		}
        		}
        	} else {
            	String key = v5it.getBasicName(MysqlWorkFlow.wf_dir_delim, ItemNameType.normal_item, ItemNameVersion.V1);
        		System.out.println("key '" + key + "'");
            	keySet.add(key);
        	}

        	for (String key: keySet) {
        		key = key.trim();
	        	if (status.newStatusMap.containsKey(key)) {
	        		HadoopResItem sit = status.newStatusMap.get(key);
	        		if (sit.dbmachine.equals("test1")) status.test1v5Contents.add(v5it.defFileLine);
	        		else if (sit.dbmachine.equals("test2")) status.test2v5Contents.add(v5it.defFileLine);
	        		else if (sit.dbmachine.equals("test5")) status.test5v5Contents.add(v5it.defFileLine);
	        		else if (sit.dbmachine.equals("test6")) status.test6v5Contents.add(v5it.defFileLine);
	        		else if (sit.dbmachine.equals("test7")) status.test7v5Contents.add(v5it.defFileLine);
	        		else if (sit.dbmachine.equals("test8")) status.test8v5Contents.add(v5it.defFileLine);
	        		else if (sit.dbmachine.equals("test10")) status.test10v5Contents.add(v5it.defFileLine);
	        		else if (sit.dbmachine.equals("prod1")) status.prod1v5Contents.add(v5it.defFileLine);
	        		else if (sit.dbmachine.equals("prod2")) status.prod2v5Contents.add(v5it.defFileLine);
	        		else if (sit.dbmachine.equals("prod3")) status.prod3v5Contents.add(v5it.defFileLine);
	        		else if (sit.dbmachine.equals("sol1")) status.sol1v5Contents.add(v5it.defFileLine);
	        		else { 
	                    System.out.println("Key not found 1: " + key);
	        			status.testUNKNOWNv5Contents.add(v5it.defFileLine);
	        		}
	        	} else if (!isInterval) {
	            	key = v5it.getBasicName(MysqlWorkFlow.wf_dir_delim, ItemNameType.normal_item, ItemNameVersion.V5);
	            	if (status.newStatusMap.containsKey(key)) {
	                    System.out.println("Already done: " + key);
	            	} else {
	                    System.out.println("Key NOT found 2: " + key);
	            		status.testUNKNOWNv5Contents.add(v5it.defFileLine);
	            	}
	        	} //else covered as interval above
            }
        }
        
        System.out.println("cnt status test1: " + status.test1v5Contents.size());
        System.out.println("cnt status test2: " + status.test2v5Contents.size());
        System.out.println("cnt status test5: " + status.test5v5Contents.size());
        System.out.println("cnt status test6: " + status.test6v5Contents.size());
        System.out.println("cnt status test7: " + status.test7v5Contents.size());
        System.out.println("cnt status test8: " + status.test8v5Contents.size());
        System.out.println("cnt status test10: " + status.test10v5Contents.size());
        System.out.println("cnt status prod1: " + status.prod1v5Contents.size());
        System.out.println("cnt status prod2: " + status.prod2v5Contents.size());
        System.out.println("cnt status prod3: " + status.prod3v5Contents.size());
        System.out.println("cnt status sol1: " + status.sol1v5Contents.size());
        System.out.println("cnt status UN: " + status.testUNKNOWNv5Contents.size());
        
        status.test1v5file = new File(v5prefixWithPath + "_t1_" + suffixTxt + v5suffix);
        status.test2v5file = new File(v5prefixWithPath + "_t2_" + suffixTxt + v5suffix);
        status.test5v5file = new File(v5prefixWithPath + "_t5_" + suffixTxt + v5suffix);
        status.test6v5file = new File(v5prefixWithPath + "_t6_" + suffixTxt + v5suffix);
        status.test7v5file = new File(v5prefixWithPath + "_t7_" + suffixTxt + v5suffix);
        status.test8v5file = new File(v5prefixWithPath + "_t8_" + suffixTxt + v5suffix);
        status.test10v5file = new File(v5prefixWithPath + "_t10_" + suffixTxt + v5suffix);
        status.prod1v5file = new File(v5prefixWithPath + "_p1_" + suffixTxt + v5suffix);
        status.prod2v5file = new File(v5prefixWithPath + "_p2_" + suffixTxt + v5suffix);
        status.prod3v5file = new File(v5prefixWithPath + "_p3_" + suffixTxt + v5suffix);
        status.sol1v5file = new File(v5prefixWithPath + "_s1_" + suffixTxt + v5suffix);
        status.testUNKNOWNv5file = new File(v5prefixWithPath + "_UK_" + suffixTxt + v5suffix);
        
    	createV5File(status.test1v5file, status.test1v5Contents, "test1");
    	createV5File(status.test2v5file, status.test2v5Contents, "test2");
    	createV5File(status.test5v5file, status.test5v5Contents, "test5");
    	createV5File(status.test6v5file, status.test6v5Contents, "test6");
    	createV5File(status.test7v5file, status.test7v5Contents, "test7");
    	createV5File(status.test8v5file, status.test8v5Contents, "test8");
    	createV5File(status.test10v5file, status.test10v5Contents, "test10");
    	createV5File(status.prod1v5file, status.prod1v5Contents, "prod1");
    	createV5File(status.prod2v5file, status.prod2v5Contents, "prod2");
    	createV5File(status.prod3v5file, status.prod3v5Contents, "prod3");
    	createV5File(status.sol1v5file, status.sol1v5Contents, "sol1");
    	createV5File(status.testUNKNOWNv5file, status.testUNKNOWNv5Contents, "testUNKNOWN");
    }
    
    public static void createV5File(File file, Set<String> contents, String tag) throws IOException {
        if ( contents.size()>0 ) {
        	//System.out.println("creating file " + file.getAbsolutePath());
        	file.createNewFile();
    		FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter  bw = new BufferedWriter(fw);        
        	for (String l : contents ) {
	        	bw.write(l);
	        	bw.newLine();
        	}
        	bw.close();
        } else {
        	System.out.println("No lines for " + tag);
        }
    }
    
    public static class StatusRes {
    	File newStatusFile;
    	File test1v5file;
    	File test2v5file;
    	File test5v5file;
    	File test6v5file;
    	File test7v5file;
    	File test8v5file;
    	File test10v5file;
    	File prod1v5file;
    	File prod2v5file;
    	File prod3v5file;
    	File sol1v5file;
    	File testUNKNOWNv5file;
    	
        Map <String,HadoopResItem> newStatusMap = new HashMap<String,HadoopResItem>();
    	Set<String> test1v5Contents = new HashSet<String>();
    	Set<String> test2v5Contents = new HashSet<String>();
    	Set<String> test5v5Contents = new HashSet<String>();
    	Set<String> test6v5Contents = new HashSet<String>();
    	Set<String> test7v5Contents = new HashSet<String>();
    	Set<String> test8v5Contents = new HashSet<String>();
    	Set<String> test10v5Contents = new HashSet<String>();
    	Set<String> prod1v5Contents = new HashSet<String>();
    	Set<String> prod2v5Contents = new HashSet<String>();
    	Set<String> prod3v5Contents = new HashSet<String>();
    	Set<String> sol1v5Contents = new HashSet<String>();
    	Set<String> testUNKNOWNv5Contents = new HashSet<String>();
    }
    
    public static StatusRes getNewStatusFile(File getFile)  throws IOException {
    	if (!getFile.exists()) {
            System.err.println("Status file "  + getFile +  " does not exist!");
            System.exit(1);
    	}
    	BufferedReader fr = new BufferedReader(new FileReader(getFile));        
        String nextLine = "";
        if ((nextLine = fr.readLine()) == null ) {
            System.err.println("Emty file file-to-get '" + getFile.getName() + "'");
            System.exit(1);
        } else if ((!nextLine.startsWith("Status for each item")) && (!nextLine.startsWith("New status for each item"))){
            System.err.println("file '" + getFile.getName() + "' does not start with '[New] Status for each item'");
            System.exit(1);
        }

        if ((nextLine = fr.readLine()) == null ) {
            System.err.println("Emty file file-to-get after 1. line '" + getFile.getName() + "'");
            System.exit(1);
        } else if (!nextLine.startsWith("machine_no#")){
            System.err.println("file '" + getFile.getName() + "' line 2 does not start with 'machine_no#'");
            System.exit(1);
        }

        if ((nextLine = fr.readLine()) == null ) {
            System.err.println("Emty file file-to-get after 2. line, i.e. there were no datai in: '" + getFile.getName() + "'");
            System.exit(1);
        }

        StatusRes sr = new StatusRes();
        String line;
        //boolean newStatus = true;
        while ((line = fr.readLine()) != null) {
        	line = line.trim();
        	//System.err.println("line '" + line + "'");
            if (!line.isEmpty()) {
            	HadoopResItem item = new HadoopResItem(line);
            	String key = item.getname(MysqlWorkFlow.wf_dir_delim).trim();
            	//System.out.println("key '" + key + "'");
            	//newStatus = MysqlX.isNumeric(item.hadoop_version);
            	sr.newStatusMap.put(key, item);
        	}
    	}
        fr.close();
        
        //if (newStatus) {
        	sr.newStatusFile = getFile;
        /*} else {
        	
        }*/
        
	    return sr;
    }

}  
