package dk.kb.webdanica.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;
import java.io.IOException;

import dk.kb.webdanica.tools.MysqlWorkFlow.IgnoreFile;
import dk.kb.webdanica.tools.MysqlX.DataSource;

/*
String url = "jdbc:mysql://localhost/test|webdanica";
Class.forName ("com.mysql.jdbc.Driver").newInstance ();
Connection conn = DriverManager.getConnection (url, "username", "password");
*/

/*
extract status on dbMaschine
*/

public class MysqlUrlsForTestMerge {

    /**
     * @param args indir=<e.g. /data1/resultater/urls/>  ourdir=<e.g. /data1/resultater/urls_merged/> version=<numberofversion> source=NAS|IA|both ignoreFile=true|false|warning   
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
    			+ "indir=<e.g. /data1/resultater/urls/> ourdir=<e.g. /data1/resultater/urls_merged/> "
    			+ "version=<numberofversion> "
    			+ "source=NAS|IA|both "
    			+ "ignoreFile=true|false|warning";
        if (args.length < 5) {
            System.err.println("Missing args!");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        if (args.length > 5) {
            System.err.println("Too many args!");
            System.err.println(errArgTxt);
            System.err.println("Got:");
            for (int i = 0; i < args.length; i++) {
            	 System.err.println(" - " + args[i]);
            }
            System.exit(1);
        }

        /**** args - in-dir ****/
        String indirTxt = args[0];
        if (!indirTxt.startsWith("indir=")) {
            System.err.println("Missing arg in-dir setting - got " + indirTxt);
            System.err.println(errArgTxt);
            System.exit(1);
        }
        indirTxt = MysqlX.getStringSetting(indirTxt);
    	//System.out.println("indirTxt: " + indirTxt);
        File inDir = new File(indirTxt);
        if (!inDir.isDirectory()) {
            System.err.println("ERROR: The given in-dir '" + inDir.getAbsolutePath() + "' is not a proper directory or does not exist");
            System.err.println(errArgTxt);
            System.exit(1);
        }

        /**** args - out-dir ****/
        String outdirTxt = args[1];
        if (!outdirTxt .startsWith("outdir=")) {
            System.err.println("Missing arg out-dir setting - got " + outdirTxt);
            System.err.println(errArgTxt);
            System.exit(1);
        }
        outdirTxt  = MysqlX.getStringSetting(outdirTxt );
        File outDir = new File(outdirTxt );
        if (!outDir.isDirectory()) {
            System.err.println("ERROR: The given out-dir '" + outDir.getAbsolutePath() + "' is not a proper directory or does not exist");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        
        String versionTxt = args[2];
        if (!versionTxt.startsWith("version=")) {
            System.err.println("Missing arg version=<numberofversion> setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        versionTxt = MysqlX.getStringSetting(versionTxt);
         
       
        /**** args - source ****/
    	DataSource src = DataSource.source_none;
    	String srcTxt = args[3];
        if (!srcTxt.startsWith("source=")) {
            System.err.println("Missing arg source setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
    	if (srcTxt.startsWith("source=")) {
    		srcTxt = MysqlX.getStringSetting(srcTxt);
        }  
    	if (srcTxt.startsWith("both")) {
    		src = DataSource.source_none;     
    	 } else if(srcTxt.startsWith("IA")) {
    		 src = DataSource.source_IA;     
        } else if (srcTxt.equals("NAS")) {
        	src = DataSource.source_NAS;      
        } else {
            System.err.println("Arg source setting is NOT valid - got '" + srcTxt + "'");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        
        /**** args - ignore file ****/
        String ignoreFileTxt = args[4];
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

        /*** Find files for merge ***/
        Set<File> urlsFilesSet = MysqlStatMerge.findStatFiles(inDir, MysqlX.urlextract_fileprefix, src, versionTxt, "");
        String outFilename = getMergeFilename(urlsFilesSet, src);
        		
        /*** Read file lines into Merged urls set ***/
        MergedUrls res =  mergeDataFromFiles(urlsFilesSet);
        
        /*** Write Merged urls set ***/
    	System.out.println("*** Urls");
        if (res.urlsList.size()==0) {
        	System.out.println("- No url lines");
        } else {
	        String fn = writeUrlsFile(outDir, outFilename, res, ignoreFile);
	    	System.out.println("- Status file: " + fn);
		}
	}

	public static MergedUrls mergeDataFromFiles(Set<File> urlsFilesSet) throws IOException {
		List<MergedUrls> resList = new ArrayList<MergedUrls>(urlsFilesSet.size());
        
		//read lines into list of res (MergedUrls) -one file per list item
	    for (File nextfile: urlsFilesSet) {
			BufferedReader fr = new BufferedReader(new FileReader(nextfile));        
	        String line;

	        MergedUrls fres = new MergedUrls();
	        Set<String> tmpUrlsSet = new HashSet<String>();
	        tmpUrlsSet.clear();
	        fres.fileName = nextfile.getAbsolutePath();
	        
	        //get tablenames inf
	        while ((line = fr.readLine()) != null) {
	            if (!line.isEmpty()) {
		        	if (line.startsWith(MysqlX.urlextract_tableline_start)) {
	    	            String[] parts = line.split(MysqlX.row_delim);
	    	            if (parts.length>1) {
	    	            	fres.tableLinePrefix = parts[0];
	    	            	String[] tabparts = parts[1].split(MysqlX.tablename_delim);
	    	            	for (String p : tabparts) {
	        	            	fres.tableList.add(p.trim());
	    	            	}
		    	            fres.cntDelims = parts.length-1;
	    	            }
		        	} else if (line.startsWith(MysqlX.urlextract_title_start)) {
		        		if (fres.headerLine.isEmpty()) {
		    	            fres.headerLine = line;
			            }
		            } else {
		            	tmpUrlsSet.add(line);
		            }
            	}
	           //System.out.println();
        	}
	        fr.close();
	        fres.urlsList.addAll(tmpUrlsSet);
	        resList.add(fres);
	    } 

	    //merge
        MergedUrls res = new MergedUrls();
        res.fileName = resList.get(0).fileName;
        res.headerLine = resList.get(0).headerLine;
        res.tableLinePrefix = resList.get(0).tableLinePrefix;
        res.cntDelims = resList.get(0).cntDelims;
	    
        int maxNoUrlLines = 0;
	    for (MergedUrls subRes: resList) {
	    	int cnt = subRes.urlsList.size();
	    	maxNoUrlLines = ( maxNoUrlLines < cnt ? cnt : maxNoUrlLines);
	    }
	    for (int j=0; j < maxNoUrlLines; j++) {
		    for (MergedUrls subRes: resList) {
		    	if (subRes.urlsList.size() > j) {
		    		String u = subRes.urlsList.get(j);
		    		res.urlsList.add(u);
		    	}
	    	}
	    }

	    for (MergedUrls subRes: resList) {
		    for (String t : subRes.tableList) {
		    	if (res.tableList.contains(t)) {
	            	System.err.println("WARNING: table " + t + " is included in more than one file '" + subRes.fileName + "'");
	        	}
	    		res.tableList.add(t);
	    	}
	    }
	    return res;
	}
	
    public static String writeUrlsFile(File mergedDir, String filename, MergedUrls res, IgnoreFile ignoreFile) throws IOException {
        //make file to write status 
	    File writeFile = new File(mergedDir.getAbsolutePath() + "/" + filename);
		if (writeFile.exists()) {
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
	
        //write header 
	    String hdTxt = res.headerLine;
	    for (int i=1; i<= res.cntDelims; i++) {
	    	hdTxt = hdTxt + MysqlX.row_delim;
	    }
        bw.write(hdTxt);
    	bw.newLine();

    	
        //write tables (and sort
        Collections.sort(res.tableList);
    	String tableTxt =  MysqlX.urlextract_tableline_start  + MysqlX.row_delim + MysqlX.getStringSequence(res.tableList,MysqlX.tablename_delim);
	    for (int i=2; i<= res.cntDelims; i++) {
	    	tableTxt = tableTxt + MysqlX.row_delim;
	    }
	    //System.out.println(tableTxt);
    	bw.write(tableTxt);
    	bw.newLine();

    	//write url lines
	    for (String l: res.urlsList) {
			bw.write(l);
			bw.newLine();
		}
	
		bw.close();
	    return writeFile.getAbsolutePath();
	}
	
	public static String getMergeFilename(Set<File> urlsFilesSet, DataSource src) {
		String newfn = MysqlX.urlextract_fileprefix;
	    if (urlsFilesSet.size()>0) { 
	    	for (File f: urlsFilesSet) {
	    		String fn = f.getName();
	    		int pos1 = fn.indexOf("_C"); 
	    		int pos2 = fn.indexOf("_V"); 
	    		newfn = newfn + "merged" + fn.substring(pos1,pos2) ;
	    		break;
	    	}
			//Date now = new Date(System.currentTimeMillis());
	    	//SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	    	newfn = newfn + "_" +  MysqlX.getSourceInfix(src) + "V" + MysqlIngester.ingest_current_update_no + MysqlX.txtfile_suffix ;//"_T" + dateFormat.format(now) + MysqlX.txtfile_suffix;
	    }
	    return newfn;
	}
	
	public static String getMergeFilename(Set<File> urlsFilesSet) {
	    return getMergeFilename(urlsFilesSet, DataSource.source_none);
	}

	public static class MergedUrls { 
        List<String> urlsList = new ArrayList<String>();
        List<String> tableList = new ArrayList<String>();
	    int cntDelims = 0;
	    String tableLinePrefix = "";
	    String headerLine = "";
	    String fileName = "";
    }
	
}
