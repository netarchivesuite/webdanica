package dk.kb.webdanica.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/*
String url = "jdbc:mysql://localhost/test|webdanica";
Class.forName ("com.mysql.jdbc.Driver").newInstance ();
Connection conn = DriverManager.getConnection (url, "username", "password");
*/

/*
Update TABLE ResFromHadoop with C15b varchar(20) with TLD
*/

public class MysqlCheckHadoop {
    /**
     * @param args machine=<machinewith file-to-check> disk=<disk with file-to-check> partition=<partition with file-to-check>
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws SQLException 
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException, FileNotFoundException, InstantiationException, IllegalAccessException, ClassNotFoundException {
    	String errArgTxt = "Proper args: machine=<machinewith file-to-check> "
    			+ "disk=<disk with file-to-check> "
    			+ "partition=<partition with file-to-check>";
    	////////////////////////////////////////////////////////
    	// Read and check arguments
        if (args.length < 3) {
            System.err.println("Missing args!");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        if (args.length > 3) {
            System.err.println("Too many args.");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        
        File checkFile = new File(args[0]);
        if (!checkFile.isFile()) {
            System.err.println("The given checkFile '" + checkFile.getAbsolutePath() + "' is not a proper file or does not exist");
            System.err.println(errArgTxt);
            System.exit(1);
        }

        int startInterval = Integer.valueOf(args[1]);
        int endInterval = Integer.valueOf(args[2]);
        if (startInterval > endInterval) {
            System.err.println("startInterval " + startInterval + " > endInterval " + endInterval);
            System.exit(1);
        } 

        String hadoopTxt = args[3];
        if (!hadoopTxt .startsWith("isHadoopUpdate=")) {
            System.err.println("Missing arg isHadoopUpdate setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        boolean isHadoopUpdate = MysqlX.getBoleanSetting(hadoopTxt);

        ////////////////////////////////////////////////////////
        // Process
        ////////////////////////////////////////////////////////
        System.out.println("*** Checking: file=" + checkFile + ", startInterval=" + startInterval + ", endInterval=" + endInterval );
        MysqlX.CheckResult chkRes = checkUpdateResFile(checkFile, startInterval, endInterval, isHadoopUpdate);
        if (chkRes.missingSet.size()> 0 || chkRes.checkSet.size()> 0)  System.out.println("*** NOT OK");
        else System.out.println("*** OK");

        System.out.println("Res-file #lines " + chkRes.resfileLineCount + " lines");
        System.out.println("Missing " + chkRes.missingSet.size() + " lines");
        for (String s : chkRes.missingSet) {
        	System.out.println("--- MISSING: in '" + checkFile + "' - '" + s + "' may be missing in database");
        }
        System.out.println("Acummulated processed lines in ingest:  " + chkRes.processedCount );
        System.out.println("of which are skipped:                   " + chkRes.skippedCount );
        System.out.println("of which are Updated:                   " + chkRes.ignoredOrUpdateCount );
        for (String s : chkRes.checkSet) {
        	System.out.println("--- CHECK file not processed before interval end in '" + checkFile + "': '" + s + "");
        }
        for (String s : chkRes.warningSet) {
        	System.out.println("--- " + s);
        }
    }
    
    public static MysqlX.CheckResult checkUpdateResFile(File checkFile, int startInterval, int endInterval, boolean hadoop)  throws IOException {
    	if (!checkFile.exists()) {
            System.err.println("Result file "  + checkFile +  " does not exist!");
            System.exit(1);
    	}
    	BufferedReader fr = new BufferedReader(new FileReader(checkFile));        
        String nextLine = "";
        if ((nextLine = fr.readLine()) == null ) {
            System.err.println("Emty file file-to-check '" + checkFile.getName() + "'");
            System.exit(1);
        } else if (!nextLine.startsWith("Running WebdanicaJobs")){
            System.err.println("file '" + checkFile.getName() + "' does not start with 'Running WebdanicaJobs'");
            System.exit(1);
        }

        ////////////////////////////////////////////////////////
        // Reconize blocks of :
        // '--- Processing file:  wd002-d2-m1_part-m-00/part-m-00842 '
        // 'Processed 33479 lines'  // inserted = <Processed lines> - <Skipped lines> - <Ignored lines> 
        // 'Skipped 0 lines'        // lines that could not be prepared or inserted in database
        // 'Ignored 0 lines'        // lines that were already in database 
        // Updated instead of Ignored for hadoop
        //file contains expected start - first line with Running is checked along with file parameter above
        //nextLine already read above 
    	
        MysqlX.CheckResult cr = new MysqlX.CheckResult();
        
        if ((nextLine = fr.readLine()) == null ) {
            System.out.println("No processing lines in file-to-check '" + checkFile.getName() + "'");
        } else { 
	        int ii = startInterval; // iterates from start to end of interval
	        int fileNo; // actual number from 'part'-file to be match in the interval
	
	        /* Check rest of line */ 
	        while (ii<=endInterval && !nextLine.isEmpty()) {
	            String actLine = nextLine.trim();
	            
	            nextLine = "";
				cr.resfileLineCount++;
	          
	            if (!actLine.isEmpty()) {
	                if (!actLine.startsWith("--- Processing file: ")) {
	                    System.err.println("Unknown processing line - '" + actLine + "'");
	                    System.exit(1);
	                }
	
	                /*  Set and check fileNo : from processing file name on form <dir>/part-m-##### */
	            	int pos = actLine.indexOf("/part-m-");
	            	fileNo=Integer.valueOf(actLine.substring(pos + "/part-m-".length()).trim());
	            	//System.out.println("Line: " + actLine + "ii: " + ii + "fileNo: " + fileNo);
	            	if (fileNo>ii) {
	            		for (int j=ii; j<fileNo; j++) {
	            			//cr.checkSet.add("WARNING: MISSING: in '" + checkFile + "' - 'part-m-" + String.format("%05d", j) + "'");
	            			cr.checkSet.add("part-m-" + String.format("%05d", j));
	            		} 
	        			ii = fileNo;
	            	}
	            	if (fileNo<ii) {
	                    System.err.println("Wrong interval fileNo<ii - line '" + actLine + "'");
	                    System.exit(1);
	            	}
	
	            	/* Check Processed, Skipped, Ignores lines, that may be missing due to error */
	
	            	
	            	/* Processed line */
	            	if ((actLine = fr.readLine()) == null) {
	            		//cr.missingSet.add("MISSING: in '" + checkFile + "' - 'part-m-" + String.format("%05d", ii) + "' may be missing in database (EOF)");
	            		cr.missingSet.add("part-m-" + String.format("%05d", ii));
	        			actLine=""; 
	        			nextLine = ""; //will stop while loop
	        			break;
	                }
	            	cr.resfileLineCount++;
	                if (!(actLine.startsWith("Processed ") && actLine.endsWith(" lines"))) { 
	                	//cr.missingSet.add("MISSING: in '" + checkFile + "' - 'part-m-" + String.format("%05d", ii) + "' may be missing in database (more)");
	                	cr.missingSet.add("part-m-" + String.format("%05d", ii));
	        			nextLine = actLine; //can be the next '--- Processing line
	                } else {
		                String[] processedParts = actLine.split(" ");
		            	if (processedParts.length!=3) {
		                    System.err.println("Expected 3 parts of Processed line - got '" + actLine + "'");
		                    System.exit(1);
		                } 
		            	int processed = Integer.parseInt(processedParts[1]);
		            	cr.processedCount = cr.processedCount + processed;
	
		            	
		            	/* Skipped line */
		            	if ((actLine = fr.readLine()) == null) {
		                    System.err.println("Expected Skipped line - got EOF");
		                    System.exit(1);
		                }
		            	cr.resfileLineCount++;
		                if (!(actLine.startsWith("Skipped ") && actLine.endsWith(" lines"))) { 
		                    System.err.println("Expected Skipped line - got '" + actLine + "'");
		                    System.exit(1);
		                }
		                String[] skippedParts = actLine.split(" ");
		            	if (skippedParts.length!=3) {
		                    System.err.println("Expected 3 parts of Skipped line - got '" + actLine + "'");
		                    System.exit(1);
		                } 
		            	int skipped = Integer.parseInt(skippedParts[1]);
		            	if (skipped>0) {
		            		cr.warningSet.add("WARNING: Skipped lines > 0: '" + actLine + "'");
		            		cr.skippedCount = cr.skippedCount + skipped;
		            	}
		            	
		            	
		            	/* Ignored/Updated line */
		            	if ((actLine = fr.readLine()) == null) {
		                    System.err.println("Expected " + (hadoop?"Updated":"Ignored") + " line - got EOF");
		                    System.exit(1);
		                }
		            	cr.resfileLineCount++;
		                if (!(actLine.startsWith((hadoop?"Updated":"Ignored")) && actLine.endsWith(" lines"))) { 
		                    System.err.println("Expected " + (hadoop?"Updated":"Ignored") + " line - got '" + actLine + "'");
		                    System.exit(1);
		                }
		                String[] ignoredParts = actLine.split(" ");
		            	if (ignoredParts.length!=3) {
		                    System.err.println("Expected 3 parts of " + (hadoop?"Updated":"Ignored") + " line - got '" + actLine + "'");
		                    System.exit(1);
		                } 
		            	int ignored = Integer.parseInt(ignoredParts[1]);
		            	if (ignored>0) {
		            		cr.ignoredOrUpdateCount = cr.ignoredOrUpdateCount + ignored;
		            	}
		            	do {
			            	if ((actLine = fr.readLine()) == null) {
			        			actLine=""; 
			        			nextLine = ""; //will stop while loop
			        			break;
			                } else {
			                	cr.resfileLineCount++;
			                }
			            } while (actLine.startsWith(" - ")); //ignore listed urls
			            nextLine = actLine;
			            actLine = "";
		            }
	            } 
	            ii++;
	        }
	        fr.close();
	        
	        if (ii<=endInterval) {
	            //cr.checkSet.add("WARNING: Ended early - ii=" + ii + ", endInterval=" + endInterval);
	        	for (int i=ii; i<=endInterval; i++) {
	        		cr.checkSet.add("part-m-" + String.format("%05d", ii));
	        	}
	        }
	        
	        if (hadoop) {
	        	if (cr.skippedCount > 0 ) {
	        		cr.warningSet.add("WARNING: skipped lines in update (no matching url?)");
	            }  
	        } 
        }
	    return cr;
    }
}  
